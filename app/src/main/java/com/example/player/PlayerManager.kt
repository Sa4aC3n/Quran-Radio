package com.example.player

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import com.example.data.RadioRepository
import com.example.data.UserPreferencesRepository
import com.example.model.PlaybackStatus
import com.example.model.PlayerState
import com.example.model.RadioStation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
class PlayerManager private constructor(
    private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val radioRepository: RadioRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private var exoPlayer: ExoPlayer? = null
    var mediaSession: MediaSession? = null

    private var retryJob: Job? = null
    private var sleepTimerJob: Job? = null
    private var durationTrackerJob: Job? = null

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    init {
        initializePlayer()
    }

    private fun initializePlayer() {
        if (exoPlayer != null) return

        try {
            val httpDataSourceFactory = DefaultHttpDataSource.Factory()
                .setUserAgent("QuranRadioWorld/1.0 (Android; ExoPlayer)")
                .setConnectTimeoutMs(15000)
                .setReadTimeoutMs(15000)
                .setAllowCrossProtocolRedirects(true)
                .setDefaultRequestProperties(mapOf("Icy-MetaData" to "1"))

            val loadControl = DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                    3000,   // Min buffer: 3 seconds for fast radio start
                    15000,  // Max buffer: 15 seconds
                    1500,   // Buffer for playback start: 1.5 seconds
                    2500    // Buffer for playback after rebuffer: 2.5 seconds
                )
                .build()

            val mediaSourceFactory = DefaultMediaSourceFactory(httpDataSourceFactory)

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                .build()

            val renderersFactory = androidx.media3.exoplayer.DefaultRenderersFactory(context.applicationContext)
                .setExtensionRendererMode(androidx.media3.exoplayer.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)
                .setEnableAudioTrackPlaybackParams(true)

            exoPlayer = ExoPlayer.Builder(context.applicationContext, renderersFactory)
                .setMediaSourceFactory(mediaSourceFactory)
                .setLoadControl(loadControl)
                .setAudioAttributes(audioAttributes, true)
                .setHandleAudioBecomingNoisy(true)
                .setWakeMode(C.WAKE_MODE_NETWORK)
                .build().apply {
                    addListener(PlayerEventListener())
                }
        } catch (e: Exception) {
            // Fallback initialization
            try {
                exoPlayer = ExoPlayer.Builder(context).build().apply {
                    addListener(PlayerEventListener())
                }
            } catch (_: Exception) {}
        }
    }

    fun getPlayer(): ExoPlayer? {
        if (exoPlayer == null) {
            initializePlayer()
        }
        return exoPlayer
    }

    private var currentActiveUrl: String? = null
    private var streamCandidates: List<String> = emptyList()
    private var currentCandidateIndex: Int = 0

    fun playStation(station: RadioStation) {
        val candidates = mutableListOf<String>()
        if (station.streams.isNotEmpty()) {
            val sortedStreams = station.streams
                .filter { it.isActive }
                .sortedBy { it.priority }
            for (s in sortedStreams) {
                if (s.url.isNotBlank() && !candidates.contains(s.url)) {
                    candidates.add(s.url)
                }
            }
        }
        if (station.streamUrl.isNotBlank() && !candidates.contains(station.streamUrl)) {
            candidates.add(0, station.streamUrl)
        }
        if (!station.fallbackStreamUrl.isNullOrBlank() && !candidates.contains(station.fallbackStreamUrl)) {
            candidates.add(station.fallbackStreamUrl)
        }
        if (candidates.isEmpty()) {
            candidates.add(station.streamUrl)
        }

        streamCandidates = candidates
        currentCandidateIndex = 0
        currentActiveUrl = candidates.firstOrNull() ?: station.streamUrl

        Log.d("PlayerManager", "[STREAM_INIT] Station '${station.name}' has ${candidates.size} stream candidate(s). Starting with: $currentActiveUrl")
        playStationInternal(station, currentActiveUrl ?: station.streamUrl, retryCount = 0, isAutoRetry = false)
    }

    private fun playStationInternal(
        station: RadioStation,
        streamUrl: String,
        retryCount: Int,
        isAutoRetry: Boolean
    ) {
        if (exoPlayer == null) {
            initializePlayer()
        }
        retryJob?.cancel()
        currentActiveUrl = streamUrl

        _playerState.update {
            it.copy(
                currentStation = station,
                status = PlaybackStatus.CONNECTING,
                errorMessage = null,
                retryCount = retryCount,
                isAutoRetrying = isAutoRetry
            )
        }

        scope.launch {
            userPreferencesRepository.setLastPlayedStationId(station.id)

            // 1. Check Network Connectivity
            if (!isNetworkConnected()) {
                _playerState.update {
                    it.copy(
                        status = PlaybackStatus.ERROR,
                        errorMessage = "لا يوجد اتصال بالإنترنت. يرجى التحقق من الشبكة والمحاولة مجددًا."
                    )
                }
                return@launch
            }

            // 2. Check Wi-Fi only preference
            val isWifiOnly = userPreferencesRepository.isWifiOnlyEnabled.first()
            if (isWifiOnly && !isWifiConnected()) {
                _playerState.update {
                    it.copy(
                        status = PlaybackStatus.ERROR,
                        errorMessage = "تم تفعيل خيار 'Wi-Fi فقط' في الإعدادات، ولا يوجد اتصال بشبكة Wi-Fi حاليًا."
                    )
                }
                return@launch
            }

            // 3. Prepare MediaItem with Islamic Radio Metadata
            val uri = Uri.parse(streamUrl)
            val mediaMetadata = MediaMetadata.Builder()
                .setTitle(station.name)
                .setArtist(station.reciter ?: "${station.country} - ${station.city}")
                .setAlbumTitle("إذاعات القرآن الكريم")
                .setDisplayTitle(station.name)
                .setArtworkUri(if (station.logoUrl.isNotBlank()) Uri.parse(station.logoUrl) else null)
                .build()

            val mediaItem = MediaItem.Builder()
                .setUri(uri)
                .setMediaId(station.id)
                .setMediaMetadata(mediaMetadata)
                .build()

            try {
                exoPlayer?.let { player ->
                    player.stop()
                    player.clearMediaItems()
                    player.setMediaItem(mediaItem)
                    player.prepare()
                    player.play()
                }
                startDurationTracker()
            } catch (e: Exception) {
                handlePlaybackError("تعذر بدء تشغيل المحطة: ${e.localizedMessage}")
            }
        }
    }

    fun togglePlayPause() {
        val currentState = _playerState.value
        when (currentState.status) {
            PlaybackStatus.PLAYING -> pause()
            PlaybackStatus.PAUSED -> resume()
            PlaybackStatus.ERROR, PlaybackStatus.STOPPED, PlaybackStatus.IDLE -> {
                currentState.currentStation?.let { playStation(it) }
            }
            PlaybackStatus.CONNECTING, PlaybackStatus.BUFFERING -> pause()
        }
    }

    fun pause() {
        exoPlayer?.pause()
        _playerState.update { it.copy(status = PlaybackStatus.PAUSED) }
    }

    fun resume() {
        val currentStation = _playerState.value.currentStation
        if (currentStation != null && exoPlayer?.playbackState == Player.STATE_IDLE) {
            playStation(currentStation)
        } else {
            exoPlayer?.play()
            _playerState.update { it.copy(status = PlaybackStatus.PLAYING) }
        }
    }

    fun stop() {
        retryJob?.cancel()
        durationTrackerJob?.cancel()
        exoPlayer?.stop()
        _playerState.update {
            it.copy(
                status = PlaybackStatus.STOPPED,
                errorMessage = null,
                retryCount = 0,
                isAutoRetrying = false
            )
        }
    }

    fun retry() {
        val station = _playerState.value.currentStation
        if (station != null) {
            playStation(station)
        }
    }

    fun playNextStation(stationsList: List<RadioStation>) {
        if (stationsList.isEmpty()) return
        val current = _playerState.value.currentStation
        val currentIndex = stationsList.indexOfFirst { it.id == current?.id }
        val nextIndex = if (currentIndex != -1 && currentIndex < stationsList.size - 1) {
            currentIndex + 1
        } else {
            0
        }
        playStation(stationsList[nextIndex])
    }

    fun playPreviousStation(stationsList: List<RadioStation>) {
        if (stationsList.isEmpty()) return
        val current = _playerState.value.currentStation
        val currentIndex = stationsList.indexOfFirst { it.id == current?.id }
        val prevIndex = if (currentIndex > 0) {
            currentIndex - 1
        } else {
            stationsList.size - 1
        }
        playStation(stationsList[prevIndex])
    }

    // ==========================================
    // Sleep Timer
    // ==========================================
    fun setSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        if (minutes <= 0) {
            _playerState.update {
                it.copy(
                    isSleepTimerActive = false,
                    sleepTimerRemainingSeconds = 0L
                )
            }
            return
        }

        val totalSeconds = minutes * 60L
        _playerState.update {
            it.copy(
                isSleepTimerActive = true,
                sleepTimerRemainingSeconds = totalSeconds
            )
        }

        sleepTimerJob = scope.launch {
            var remaining = totalSeconds
            while (remaining > 0) {
                delay(1000)
                remaining--
                _playerState.update { it.copy(sleepTimerRemainingSeconds = remaining) }
            }
            // Timer expired, gracefully stop playback
            stop()
            _playerState.update {
                it.copy(
                    isSleepTimerActive = false,
                    sleepTimerRemainingSeconds = 0L
                )
            }
        }
    }

    fun cancelSleepTimer() {
        setSleepTimer(0)
    }

    // ==========================================
    // Auto-Retry on network glitches and Multi-Stream Failover
    // ==========================================
    private fun handlePlaybackError(message: String) {
        val currentRetries = _playerState.value.retryCount
        val maxRetries = _playerState.value.maxRetries
        val station = _playerState.value.currentStation

        if (station != null) {
            Log.w("PlayerManager", "[FAILOVER] Stream failed for '${station.name}': $currentActiveUrl ($message)")

            // 1. If there is another candidate stream for this station, switch to it immediately
            if (currentCandidateIndex < streamCandidates.size - 1) {
                currentCandidateIndex++
                val nextStreamUrl = streamCandidates[currentCandidateIndex]
                Log.i("PlayerManager", "[FAILOVER] Backup Selected: $nextStreamUrl (Candidate ${currentCandidateIndex + 1}/${streamCandidates.size})")

                _playerState.update {
                    it.copy(
                        status = PlaybackStatus.CONNECTING,
                        errorMessage = "تعذر الاتصال بالبث الأساسي، جارٍ التبديل للرابط الاحتياطي..."
                    )
                }
                retryJob?.cancel()
                retryJob = scope.launch {
                    delay(1000)
                    if (_playerState.value.currentStation?.id == station.id) {
                        playStationInternal(station, nextStreamUrl, retryCount = 0, isAutoRetry = true)
                    }
                }
                return
            }

            // 2. All candidates tried once. Auto-retry with backoff on the primary or best candidate
            if (currentRetries < maxRetries) {
                val nextRetry = currentRetries + 1
                val delaySeconds = when (nextRetry) {
                    1 -> 2L
                    2 -> 5L
                    else -> 10L
                }

                _playerState.update {
                    it.copy(
                        status = PlaybackStatus.CONNECTING,
                        retryCount = nextRetry,
                        isAutoRetrying = true,
                        errorMessage = "انقطع البث، جارٍ إعادة الاتصال تلقائيًا ($nextRetry/$maxRetries) بعد $delaySeconds ثوانٍ..."
                    )
                }

                retryJob?.cancel()
                retryJob = scope.launch {
                    delay(delaySeconds * 1000)
                    if (_playerState.value.isAutoRetrying && _playerState.value.currentStation?.id == station.id) {
                        // Reset candidate index to try from start again
                        currentCandidateIndex = 0
                        val urlToTry = streamCandidates.firstOrNull() ?: currentActiveUrl ?: station.streamUrl
                        playStationInternal(station, urlToTry, retryCount = nextRetry, isAutoRetry = true)
                    }
                }
                return
            }
        }

        _playerState.update {
            it.copy(
                status = PlaybackStatus.ERROR,
                isAutoRetrying = false,
                errorMessage = "البث غير متاح حاليًا. يرجى التأكد من اتصال الإنترنت أو اختيار إذاعة أخرى."
            )
        }
    }

    private fun startDurationTracker() {
        durationTrackerJob?.cancel()
        durationTrackerJob = scope.launch {
            while (true) {
                delay(1000)
                if (_playerState.value.status == PlaybackStatus.PLAYING) {
                    _playerState.update { it.copy(playbackDurationMs = it.playbackDurationMs + 1000) }
                }
            }
        }
    }

    private fun isNetworkConnected(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun isWifiConnected(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    fun release() {
        retryJob?.cancel()
        sleepTimerJob?.cancel()
        durationTrackerJob?.cancel()
        exoPlayer?.release()
        exoPlayer = null
    }

    private inner class PlayerEventListener : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_BUFFERING -> {
                    _playerState.update {
                        it.copy(
                            status = if (it.status == PlaybackStatus.CONNECTING) PlaybackStatus.CONNECTING else PlaybackStatus.BUFFERING
                        )
                    }
                }
                Player.STATE_READY -> {
                    val isPlaying = exoPlayer?.playWhenReady == true
                    _playerState.update {
                        it.copy(
                            status = if (isPlaying) PlaybackStatus.PLAYING else PlaybackStatus.PAUSED,
                            errorMessage = null,
                            retryCount = 0,
                            isAutoRetrying = false
                        )
                    }
                }
                Player.STATE_ENDED -> {
                    _playerState.update { it.copy(status = PlaybackStatus.STOPPED) }
                }
                Player.STATE_IDLE -> {
                    if (_playerState.value.status != PlaybackStatus.ERROR &&
                        _playerState.value.status != PlaybackStatus.CONNECTING
                    ) {
                        _playerState.update { it.copy(status = PlaybackStatus.IDLE) }
                    }
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _playerState.update {
                it.copy(
                    status = if (isPlaying) PlaybackStatus.PLAYING else {
                        if (it.status == PlaybackStatus.PLAYING) PlaybackStatus.PAUSED else it.status
                    }
                )
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            handlePlaybackError("خطأ في البث: ${error.message}")
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: PlayerManager? = null

        fun getInstance(
            context: Context,
            userPreferencesRepository: UserPreferencesRepository,
            radioRepository: RadioRepository
        ): PlayerManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PlayerManager(
                    context.applicationContext,
                    userPreferencesRepository,
                    radioRepository
                ).also { INSTANCE = it }
            }
        }
    }
}
