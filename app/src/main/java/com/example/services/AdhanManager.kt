package com.example.services

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.PowerManager
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import com.example.QuranRadioApplication
import com.example.data.PrayerPreferencesRepository
import com.example.model.AdhanPlaybackState
import com.example.model.PlaybackStatus
import com.example.model.PrayerType
import com.example.model.RadioStation
import com.example.player.PlayerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * AdhanManager: Central robust controller for the Adhan system.
 * 
 * Guarantees:
 * 1. Automatic clean Pause of Quran Radio when Adhan begins.
 * 2. Proper Android Audio Focus management with no audio overlapping.
 * 3. NO extra Adhan notifications generated in the Status Bar or Notification Shade.
 * 4. Automatic clean Resume of Quran Radio when Adhan finishes (only if it was playing before).
 * 5. Instant manual stop ("إيقاف الأذان") support resuming the previous radio state.
 * 6. Protection against duplicate triggers for the same prayer.
 * 7. Comprehensive logging under the [ADHAN] tag.
 */
class AdhanManager private constructor(context: Context) {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val powerManager = appContext.getSystemService(Context.POWER_SERVICE) as PowerManager

    private var wakeLock: PowerManager.WakeLock? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private val volumeDetector = AdhanVolumeKeyDetector(appContext)
    private var mediaSession: MediaSessionCompat? = null

    private val _adhanState = MutableStateFlow(AdhanPlaybackState.IDLE)
    val adhanState: StateFlow<AdhanPlaybackState> = _adhanState.asStateFlow()

    private val _currentPrayer = MutableStateFlow<PrayerType?>(null)
    val currentPrayer: StateFlow<PrayerType?> = _currentPrayer.asStateFlow()

    private val _isAdhanActive = MutableStateFlow(false)
    val isAdhanActive: StateFlow<Boolean> = _isAdhanActive.asStateFlow()

    // Radio State Preservation
    private var wasRadioPlaying: Boolean = false
    private var stationBeforeAdhan: RadioStation? = null

    // Duplicate prevention: track prayer + day
    private var lastPlayedPrayerKey: String? = null
    private var lastPlayedTimestamp: Long = 0L

    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                Log.w(TAG, "[ADHAN] Audio focus loss permanent, stopping adhan")
                stopAdhan()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                Log.w(TAG, "[ADHAN] Audio focus loss transient")
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                Log.d(TAG, "[ADHAN] Audio focus regained")
            }
        }
    }

    /**
     * Main entry point to trigger the Adhan for a specific prayer.
     * Can be invoked from AlarmReceiver, BackgroundScheduler, or UI test buttons.
     */
    fun playAdhan(prayerType: PrayerType, isTest: Boolean = false) {
        scope.launch {
            try {
                val app = appContext as? QuranRadioApplication
                val prefs = app?.prayerPreferencesRepository ?: PrayerPreferencesRepository(appContext)
                val playerManager = app?.playerManager

                // 1. Check if Adhan is enabled overall and for this specific prayer
                if (!isTest) {
                    if (!AdhanNotificationHelper.isNotificationAllowed(appContext, prayerType)) {
                        Log.d(TAG, "[ADHAN] Notifications / alert disabled by user notification preferences. Skipping.")
                        return@launch
                    }

                    val isMasterEnabled = prefs.isAdhanMasterEnabled.first()
                    if (!isMasterEnabled) {
                        Log.d(TAG, "[ADHAN] Master Adhan toggle is OFF. Skipping playback.")
                        return@launch
                    }

                    val alertSettings = prefs.alertSettings.first()
                    val isPrayerEnabled = alertSettings[prayerType] ?: true
                    if (!isPrayerEnabled) {
                        Log.d(TAG, "[ADHAN] Adhan for ${prayerType.arabicName} is disabled in settings. Skipping.")
                        return@launch
                    }

                    // Duplicate check: Prevent playing twice for same prayer on the same day within 30 minutes
                    val todayKey = "${prayerType.id}_${Calendar.getInstance().get(Calendar.DAY_OF_YEAR)}_${Calendar.getInstance().get(Calendar.YEAR)}"
                    val now = System.currentTimeMillis()
                    if (lastPlayedPrayerKey == todayKey && (now - lastPlayedTimestamp) < 30 * 60 * 1000L) {
                        Log.w(TAG, "[ADHAN] Adhan already played recently for $todayKey. Skipping duplicate trigger.")
                        return@launch
                    }
                    lastPlayedPrayerKey = todayKey
                    lastPlayedTimestamp = now
                }

                Log.i(TAG, "[ADHAN] Prayer time reached: ${prayerType.arabicName} (isTest=$isTest)")
                _currentPrayer.value = prayerType
                _adhanState.value = AdhanPlaybackState.STARTING
                _isAdhanActive.value = true

                // 2. Save Radio State & Pause if currently active
                if (playerManager != null) {
                    val currentRadioStatus = playerManager.playerState.value.status
                    val isRadioActive = (currentRadioStatus == PlaybackStatus.PLAYING ||
                            currentRadioStatus == PlaybackStatus.BUFFERING ||
                            currentRadioStatus == PlaybackStatus.CONNECTING)

                    wasRadioPlaying = isRadioActive
                    stationBeforeAdhan = playerManager.playerState.value.currentStation

                    if (wasRadioPlaying) {
                        val isFadeEnabled = app?.userPreferencesRepository?.isSmartAdhanFadeEnabled?.first() ?: true
                        if (isFadeEnabled) {
                            Log.i(TAG, "[ADHAN] Smoothly fading out radio (Station: ${stationBeforeAdhan?.name ?: "Unknown"})")
                            playerManager.fadeOutAndPause(durationMs = 1200L)
                        } else {
                            Log.i(TAG, "[ADHAN] Pausing radio (Station: ${stationBeforeAdhan?.name ?: "Unknown"})")
                            playerManager.pause()
                        }
                    } else {
                        Log.i(TAG, "[ADHAN] Radio was not playing before Adhan.")
                    }
                }

                // 3. Request Audio Focus
                requestAudioFocus()

                // 4. Acquire WakeLock for the duration of playback
                acquireWakeLock()

                // 5. Retrieve selected Muezzin sound
                val selectedSound = prefs.selectedAdhanSound.first()
                val audioPlayer = app?.adhanAudioPlayer ?: AdhanAudioPlayer(appContext)
                val currentCity = prefs.cityName.first()

                Log.i(TAG, "[ADHAN] Starting adhan with voice: ${selectedSound.titleAr}")
                _adhanState.value = AdhanPlaybackState.PLAYING
                Log.i(TAG, "[ADHAN] Adhan playing")

                // Setup MediaSession for System UI Media Player Notification
                try {
                    mediaSession?.release()
                    mediaSession = MediaSessionCompat(appContext, "AdhanMediaSession").apply {
                        isActive = true
                        setCallback(object : MediaSessionCompat.Callback() {
                            override fun onStop() {
                                Log.i(TAG, "[ADHAN] MediaSession onStop triggered")
                                stopAdhan()
                            }
                            override fun onPause() {
                                Log.i(TAG, "[ADHAN] MediaSession onPause triggered")
                                stopAdhan()
                            }
                        })
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "[ADHAN] Failed to create MediaSession: ${e.message}")
                }

                // Start hardware volume button double-press detector
                volumeDetector.startListening {
                    Log.i(TAG, "[ADHAN] Hardware volume key double-press detected! Stopping Adhan.")
                    stopAdhan()
                }

                // Display Adhan notification (MediaStyle with artwork and Stop button)
                AdhanNotificationHelper.showAdhanNotification(
                    context = appContext,
                    prayerType = prayerType,
                    cityName = currentCity,
                    muezzinName = selectedSound.titleAr,
                    sessionToken = mediaSession?.sessionToken
                )

                // 6. Play Audio
                audioPlayer.playFullAdhan(
                    sound = selectedSound,
                    prayerType = prayerType
                ) {
                    // Completion Callback
                    Log.i(TAG, "[ADHAN] Adhan completed")
                    _adhanState.value = AdhanPlaybackState.COMPLETED
                    cleanupAdhanMedia()
                    finishAdhan(playerManager)
                }
            } catch (e: Exception) {
                Log.e(TAG, "[ADHAN] Playback error: ${e.message}", e)
                _adhanState.value = AdhanPlaybackState.FAILED
                cleanupAdhanMedia()
                val app = appContext as? QuranRadioApplication
                finishAdhan(app?.playerManager)
            }
        }
    }

    /**
     * Stops the Adhan playback immediately (e.g. from UI "إيقاف الأذان" button).
     * Releases focus, wakelock, and automatically resumes the Radio if it was playing.
     */
    fun stopAdhan() {
        if (!_isAdhanActive.value && _adhanState.value == AdhanPlaybackState.IDLE) return

        Log.i(TAG, "[ADHAN] Adhan manually stopped by user")
        _adhanState.value = AdhanPlaybackState.STOPPING

        cleanupAdhanMedia()

        val app = appContext as? QuranRadioApplication
        val audioPlayer = app?.adhanAudioPlayer ?: AdhanAudioPlayer(appContext)
        audioPlayer.stopAdhan()

        finishAdhan(app?.playerManager)
    }

    /**
     * Handles physical volume key events forwarded from the foreground activity.
     */
    fun handleVolumeKeyEvent(): Boolean {
        if (!_isAdhanActive.value) return false
        return volumeDetector.onKeyEvent()
    }

    private fun cleanupAdhanMedia() {
        volumeDetector.stopListening()
        try {
            mediaSession?.isActive = false
            mediaSession?.release()
        } catch (_: Exception) {}
        mediaSession = null
        AdhanNotificationHelper.cancelAdhanNotification(appContext)
        try {
            val stopIntent = android.content.Intent(appContext, AdhanPlaybackService::class.java).apply {
                action = AdhanPlaybackService.ACTION_STOP_ADHAN
            }
            appContext.startService(stopIntent)
        } catch (_: Exception) {}
    }

    /**
     * Called when the user explicitly chooses a new radio station during Adhan playback.
     */
    fun notifyUserChangedStation(newStation: RadioStation) {
        if (_isAdhanActive.value) {
            stationBeforeAdhan = newStation
            wasRadioPlaying = true
        }
    }

    private fun finishAdhan(playerManager: PlayerManager?) {
        scope.launch {
            // 0. Clean up media notification, volume listener, and MediaSession
            cleanupAdhanMedia()

            // 1. Release wakeLock and Audio Focus
            releaseWakeLock()
            abandonAudioFocus()

            // 2. Handle Radio Resume
            if (wasRadioPlaying && playerManager != null) {
                val currentStatus = playerManager.playerState.value.status
                // Only resume if the radio is still in paused state and wasn't stopped by the user
                if (currentStatus == PlaybackStatus.PAUSED || currentStatus == PlaybackStatus.IDLE) {
                    val app = appContext as? QuranRadioApplication
                    val isFadeEnabled = app?.userPreferencesRepository?.isSmartAdhanFadeEnabled?.first() ?: true
                    if (isFadeEnabled) {
                        Log.i(TAG, "[ADHAN] Smoothly fading in radio (${stationBeforeAdhan?.name})")
                        playerManager.fadeInAndResume(durationMs = 1500L)
                    } else {
                        Log.i(TAG, "[ADHAN] Resuming radio automatically (${stationBeforeAdhan?.name})")
                        playerManager.resume()
                    }
                } else {
                    Log.d(TAG, "[ADHAN] Radio status is $currentStatus, skipping auto-resume.")
                }
            } else {
                Log.d(TAG, "[ADHAN] Radio was not playing prior to Adhan. Keeping radio OFF.")
            }

            // 3. Reset internal tracking state
            wasRadioPlaying = false
            stationBeforeAdhan = null
            _currentPrayer.value = null
            _isAdhanActive.value = false
            _adhanState.value = AdhanPlaybackState.IDLE
        }
    }

    private fun requestAudioFocus() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val playbackAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()

                audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                    .setAudioAttributes(playbackAttributes)
                    .setAcceptsDelayedFocusGain(false)
                    .setOnAudioFocusChangeListener(audioFocusChangeListener)
                    .build()

                audioFocusRequest?.let { audioManager.requestAudioFocus(it) }
            } else {
                @Suppress("DEPRECATION")
                audioManager.requestAudioFocus(
                    audioFocusChangeListener,
                    AudioManager.STREAM_ALARM,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to request audio focus: ${e.message}")
        }
    }

    private fun abandonAudioFocus() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
                audioFocusRequest = null
            } else {
                @Suppress("DEPRECATION")
                audioManager.abandonAudioFocus(audioFocusChangeListener)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to abandon audio focus: ${e.message}")
        }
    }

    private fun acquireWakeLock() {
        try {
            if (wakeLock == null) {
                wakeLock = powerManager.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "QuranRadio::AdhanManagerWakeLock"
                ).apply {
                    setReferenceCounted(false)
                }
            }
            wakeLock?.acquire(4 * 60 * 1000L) // 4 minutes safety timeout
        } catch (e: Exception) {
            Log.e(TAG, "Failed to acquire wakelock: ${e.message}")
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to release wakelock: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "AdhanManager"

        @Volatile
        private var INSTANCE: AdhanManager? = null

        fun getInstance(context: Context): AdhanManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AdhanManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
