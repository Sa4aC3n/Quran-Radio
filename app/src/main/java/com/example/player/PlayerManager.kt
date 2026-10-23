package com.example.player

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
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
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.exoplayer.audio.MediaCodecAudioRenderer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import com.example.QuranRadioApplication
import com.example.data.RadioRepository
import com.example.data.UserPreferencesRepository
import com.example.model.PlaybackStatus
import com.example.model.PlayerState
import com.example.model.RadioStation
import com.example.model.RecognizedRecitation
import com.example.model.SpatialAudioConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
class PlayerManager private constructor(
    context: Context,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val radioRepository: RadioRepository
) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    val spatialAcousticProcessor = SpatialAcousticAudioProcessor()
    val ambientSoundscapePlayer = AmbientSoundscapePlayer()
    val rollingBufferProcessor = RollingAudioBufferProcessor()
    val spatialAudioConfig: StateFlow<SpatialAudioConfig> = userPreferencesRepository.spatialAudioConfig
        .stateIn(scope, SharingStarted.Eagerly, SpatialAudioConfig())

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private var exoPlayer: ExoPlayer? = null
    var mediaSession: MediaSession? = null

    private var retryJob: Job? = null
    private var sleepTimerJob: Job? = null
    private var durationTrackerJob: Job? = null

    private var wakeLock: PowerManager.WakeLock? = null
    private var wifiLock: WifiManager.WifiLock? = null

    private val connectivityManager =
        appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            Log.d("PlayerManager", "[NETWORK] Internet connection restored")
            val state = _playerState.value
            val station = state.currentStation
            if (station != null && !state.isUserStopped) {
                if (state.status == PlaybackStatus.ERROR || state.isAutoRetrying ||
                    (state.status == PlaybackStatus.CONNECTING && (exoPlayer == null || exoPlayer?.playbackState == Player.STATE_IDLE))
                ) {
                    Log.i("PlayerManager", "[NETWORK_RECONNECT] Internet restored, auto-resuming station '${station.name}'")
                    scope.launch(Dispatchers.Main) {
                        retryJob?.cancel()
                        retry()
                    }
                }
            }
        }
    }

    init {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                connectivityManager.registerDefaultNetworkCallback(networkCallback)
            }
        } catch (e: Exception) {
            Log.w("PlayerManager", "Failed to register network callback: ${e.message}")
        }

        scope.launch {
            spatialAudioConfig.collect { config ->
                spatialAcousticProcessor.config = config
                ambientSoundscapePlayer.updateConfig(config.ambientSoundscape, config.ambientVolume)
            }
        }
    }

    private fun acquireLocks() {
        try {
            if (wakeLock == null) {
                val powerManager = appContext.getSystemService(Context.POWER_SERVICE) as? PowerManager
                wakeLock = powerManager?.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "QuranRadioWorld:PlaybackWakeLock"
                )?.apply {
                    setReferenceCounted(false)
                }
            }
            if (wakeLock?.isHeld == false) {
                // Maximum 6 hours safety timeout to prevent permanent lock
                wakeLock?.acquire(6 * 60 * 60 * 1000L)
            }

            if (wifiLock == null) {
                val wifiManager = appContext.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                val lockMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    WifiManager.WIFI_MODE_FULL_LOW_LATENCY
                } else {
                    WifiManager.WIFI_MODE_FULL
                }
                wifiLock = wifiManager?.createWifiLock(
                    lockMode,
                    "QuranRadioWorld:PlaybackWifiLock"
                )?.apply {
                    setReferenceCounted(false)
                }
            }
            if (wifiLock?.isHeld == false) {
                wifiLock?.acquire()
            }
        } catch (e: Exception) {
            Log.w("PlayerManager", "Failed to acquire wake/wifi locks: ${e.message}")
        }
    }

    private fun releaseLocks() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
            if (wifiLock?.isHeld == true) {
                wifiLock?.release()
            }
        } catch (e: Exception) {
            Log.w("PlayerManager", "Failed to release wake/wifi locks: ${e.message}")
        }
    }

    private fun initializePlayer() {
        if (exoPlayer != null) return

        try {
            val httpDataSourceFactory = DefaultHttpDataSource.Factory()
                .setUserAgent("Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36 QuranRadioWorld/1.0")
                .setConnectTimeoutMs(8000)
                .setReadTimeoutMs(10000)
                .setAllowCrossProtocolRedirects(true)
                .setKeepPostFor302Redirects(true)
                .setDefaultRequestProperties(
                    mapOf(
                        "Accept" to "*/*"
                    )
                )

            val loadControl = DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                    5000,   // Min buffer: 5 seconds for smooth mobile radio playback
                    30000,  // Max buffer: 30 seconds
                    2000,   // Buffer for playback start: 2 seconds
                    3500    // Buffer for playback after rebuffer: 3.5 seconds
                )
                .build()

            val mediaSourceFactory = DefaultMediaSourceFactory(httpDataSourceFactory)

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                .build()

            val renderersFactory = object : androidx.media3.exoplayer.DefaultRenderersFactory(appContext) {
                override fun buildAudioSink(
                    context: Context,
                    enableFloatOutput: Boolean,
                    enableAudioTrackPlaybackParams: Boolean
                ): AudioSink? {
                    return DefaultAudioSink.Builder(context)
                        .setEnableFloatOutput(false)
                        .setEnableAudioTrackPlaybackParams(enableAudioTrackPlaybackParams)
                        .setAudioProcessors(arrayOf(spatialAcousticProcessor, rollingBufferProcessor))
                        .build()
                }

                override fun buildVideoRenderers(
                    context: Context,
                    extensionRendererMode: Int,
                    mediaCodecSelector: androidx.media3.exoplayer.mediacodec.MediaCodecSelector,
                    enableDecoderFallback: Boolean,
                    eventHandler: android.os.Handler,
                    eventListener: androidx.media3.exoplayer.video.VideoRendererEventListener,
                    allowedVideoJoiningTimeMs: Long,
                    out: java.util.ArrayList<androidx.media3.exoplayer.Renderer>
                ) {
                    // Audio-only radio playback: omit video renderers to prevent Codec2Client
                    // from querying hardware video decoder component interfaces.
                }

                override fun buildCameraMotionRenderers(
                    context: Context,
                    extensionRendererMode: Int,
                    out: java.util.ArrayList<androidx.media3.exoplayer.Renderer>
                ) {
                    // Audio-only radio: no camera motion needed
                }

                override fun buildTextRenderers(
                    context: Context,
                    output: androidx.media3.exoplayer.text.TextOutput,
                    outputLooper: android.os.Looper,
                    extensionRendererMode: Int,
                    out: java.util.ArrayList<androidx.media3.exoplayer.Renderer>
                ) {
                    // Audio-only radio: no text/subtitle rendering needed
                }

                override fun buildImageRenderers(
                    out: java.util.ArrayList<androidx.media3.exoplayer.Renderer>
                ) {
                    // Audio-only radio: no image rendering needed
                }
            }.apply {
                setExtensionRendererMode(androidx.media3.exoplayer.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)
                setEnableDecoderFallback(true)
                setMediaCodecSelector(androidx.media3.exoplayer.mediacodec.MediaCodecSelector.DEFAULT)
            }

            exoPlayer = ExoPlayer.Builder(appContext, renderersFactory)
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
                exoPlayer = ExoPlayer.Builder(appContext).build().apply {
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
    private val failoverEngine = MultiStreamFailoverEngine()
    private val recognitionEngine = AcousticQuranRecognitionEngine()
    val currentRecitation: StateFlow<RecognizedRecitation> = recognitionEngine.recitationState

    fun playStation(station: RadioStation) {
        val candidates = failoverEngine.resolveCandidates(station)

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
        rollingBufferProcessor.clearBuffer()
        recognitionEngine.onStationChanged(station)

        val isBackup = currentCandidateIndex > 0
        val candidateInfo = if (streamCandidates.size > 1) {
            "سيرفر ${currentCandidateIndex + 1} من ${streamCandidates.size}"
        } else null

        _playerState.update {
            it.copy(
                currentStation = station,
                status = PlaybackStatus.CONNECTING,
                errorMessage = null,
                retryCount = retryCount,
                isAutoRetrying = isAutoRetry,
                isUserStopped = false,
                isUsingBackupStream = isBackup,
                streamCandidateInfo = candidateInfo
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
            val artworkUri = if (station.logoUrl.isNotBlank()) {
                Uri.parse(station.logoUrl)
            } else {
                Uri.parse("android.resource://${appContext.packageName}/${com.example.R.mipmap.ic_launcher}")
            }

            val mediaMetadata = MediaMetadata.Builder()
                .setTitle(station.name)
                .setDisplayTitle(station.name)
                .setArtist(station.reciter ?: "${station.country} - ${station.city}")
                .setSubtitle(station.reciter ?: "إذاعات القرآن الكريم")
                .setAlbumTitle("إذاعات القرآن الكريم – Quran Radio World")
                .setArtworkUri(artworkUri)
                .setIsPlayable(true)
                .build()

            val mediaItem = MediaItem.Builder()
                .setUri(uri)
                .setMediaId(station.id)
                .setMediaMetadata(mediaMetadata)
                .setLiveConfiguration(
                    MediaItem.LiveConfiguration.Builder()
                        .setMaxPlaybackSpeed(1.0f)
                        .setMinPlaybackSpeed(1.0f)
                        .build()
                )
                .build()

            try {
                acquireLocks()
                startMediaService()
                exoPlayer?.let { player ->
                    player.stop()
                    player.clearMediaItems()
                    player.setMediaItem(mediaItem)
                    player.prepare()
                    player.play()
                }
                startDurationTracker()
            } catch (e: Exception) {
                releaseLocks()
                handlePlaybackError("تعذر بدء تشغيل المحطة: ${e.localizedMessage}")
            }
        }
    }

    private fun startMediaService() {
        if (mediaSession != null) {
            // MediaService is already running and bound to the player session
            return
        }
        try {
            val intent = Intent(appContext, QuranMediaService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(appContext, intent)
            } else {
                appContext.startService(intent)
            }
        } catch (e: Exception) {
            Log.w("PlayerManager", "Could not start QuranMediaService: ${e.message}")
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

    private var fadeJob: Job? = null

    fun pause() {
        fadeJob?.cancel()
        releaseLocks()
        ambientSoundscapePlayer.onRadioPlaybackStateChanged(false)
        exoPlayer?.pause()
        _playerState.update { it.copy(status = PlaybackStatus.PAUSED, isUserStopped = true) }
    }

    fun fadeOutAndPause(durationMs: Long = 1200L, onCompleted: (() -> Unit)? = null) {
        fadeJob?.cancel()
        val player = exoPlayer
        if (player == null || !player.isPlaying) {
            pause()
            onCompleted?.invoke()
            return
        }

        fadeJob = scope.launch(Dispatchers.Main) {
            val steps = 12
            val stepTime = (durationMs / steps).coerceAtLeast(20L)
            val initialVol = player.volume.coerceAtLeast(0.1f)

            for (i in steps downTo 0) {
                val currentVol = initialVol * (i.toFloat() / steps.toFloat())
                player.volume = currentVol
                delay(stepTime)
            }

            pause()
            player.volume = 1.0f // Restore volume level for future playbacks
            onCompleted?.invoke()
        }
    }

    fun resume() {
        fadeJob?.cancel()
        val currentStation = _playerState.value.currentStation
        acquireLocks()
        startMediaService()
        if (currentStation != null && (exoPlayer == null || exoPlayer?.playbackState == Player.STATE_IDLE || exoPlayer?.playbackState == Player.STATE_ENDED)) {
            playStation(currentStation)
        } else {
            exoPlayer?.volume = 1.0f
            exoPlayer?.play()
            _playerState.update { it.copy(status = PlaybackStatus.PLAYING, isUserStopped = false) }
        }
        ambientSoundscapePlayer.onRadioPlaybackStateChanged(true)
    }

    fun fadeInAndResume(durationMs: Long = 1400L, targetVolume: Float = 1.0f) {
        fadeJob?.cancel()
        val player = exoPlayer
        if (player == null) {
            resume()
            return
        }

        player.volume = 0.0f
        resume()

        fadeJob = scope.launch(Dispatchers.Main) {
            val steps = 14
            val stepTime = (durationMs / steps).coerceAtLeast(20L)

            for (i in 1..steps) {
                val currentVol = targetVolume * (i.toFloat() / steps.toFloat())
                player.volume = currentVol
                delay(stepTime)
            }
            player.volume = targetVolume
        }
    }

    fun stop() {
        releaseLocks()
        ambientSoundscapePlayer.onRadioPlaybackStateChanged(false)
        retryJob?.cancel()
        durationTrackerJob?.cancel()
        exoPlayer?.stop()
        _playerState.update {
            it.copy(
                status = PlaybackStatus.STOPPED,
                errorMessage = null,
                retryCount = 0,
                isAutoRetrying = false,
                isUserStopped = true
            )
        }
    }

    fun setSpatialAudioConfig(config: SpatialAudioConfig) {
        scope.launch {
            userPreferencesRepository.setSpatialAudioConfig(config)
        }
    }

    fun retry() {
        val station = _playerState.value.currentStation
        if (station != null) {
            playStation(station)
        }
    }

    fun playNext() {
        scope.launch {
            try {
                val list = radioRepository.stations.first()
                playNextStation(list)
            } catch (e: Exception) {
                Log.e("PlayerManager", "Failed to play next station", e)
            }
        }
    }

    fun playPrevious() {
        scope.launch {
            try {
                val list = radioRepository.stations.first()
                playPreviousStation(list)
            } catch (e: Exception) {
                Log.e("PlayerManager", "Failed to play previous station", e)
            }
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
            val failedUrl = currentActiveUrl ?: station.streamUrl
            failoverEngine.recordFailure(failedUrl)
            Log.w("PlayerManager", "[FAILOVER] Stream failed for '${station.name}': $failedUrl ($message)")

            // 1. If there is another candidate stream for this station, switch to it immediately
            if (currentCandidateIndex < streamCandidates.size - 1) {
                currentCandidateIndex++
                val nextStreamUrl = streamCandidates[currentCandidateIndex]
                Log.i("PlayerManager", "[FAILOVER] Backup Selected: $nextStreamUrl (Candidate ${currentCandidateIndex + 1}/${streamCandidates.size})")

                _playerState.update {
                    it.copy(
                        status = PlaybackStatus.CONNECTING,
                        isUsingBackupStream = true,
                        errorMessage = "انقطع السيرفر الرئيسي، جارٍ التحويل للبث البديل فورياً (${currentCandidateIndex + 1}/${streamCandidates.size})..."
                    )
                }
                retryJob?.cancel()
                retryJob = scope.launch {
                    delay(150)
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

        releaseLocks()
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
            var accumulatedSeconds = 0
            while (true) {
                delay(1000)
                if (_playerState.value.status == PlaybackStatus.PLAYING) {
                    _playerState.update { it.copy(playbackDurationMs = it.playbackDurationMs + 1000) }
                    accumulatedSeconds++
                    if (accumulatedSeconds >= 30) {
                        accumulatedSeconds = 0
                        val currentStation = _playerState.value.currentStation
                        try {
                            QuranRadioApplication.instance.listeningStatsRepository.recordListeningSeconds(
                                seconds = 30,
                                stationName = currentStation?.name
                            )
                        } catch (_: Exception) {}
                    }
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
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            Log.w("PlayerManager", "Failed to unregister network callback: ${e.message}")
        }
        releaseLocks()
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
                    currentActiveUrl?.let { failoverEngine.recordSuccess(it) }
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
            ambientSoundscapePlayer.onRadioPlaybackStateChanged(isPlaying)
            _playerState.update {
                it.copy(
                    status = if (isPlaying) PlaybackStatus.PLAYING else {
                        if (it.status == PlaybackStatus.PLAYING) PlaybackStatus.PAUSED else it.status
                    }
                )
            }
        }

        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            val title = mediaMetadata.title?.toString() ?: mediaMetadata.displayTitle?.toString()
            val artist = mediaMetadata.artist?.toString()
            recognitionEngine.onMetadataReceived(title, artist, _playerState.value.currentStation)
        }

        override fun onPlayerError(error: PlaybackException) {
            Log.e("PlayerManager", "onPlayerError: errorCode=${error.errorCode}, errorCodeName=${error.errorCodeName}, message=${error.message}", error)
            retryJob?.cancel()
            val userFriendlyMessage = when (error.errorCode) {
                PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
                PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT -> "انقطع الاتصال بالخادم، جارٍ إعادة المحاولة..."
                PlaybackException.ERROR_CODE_DECODER_INIT_FAILED,
                PlaybackException.ERROR_CODE_DECODER_QUERY_FAILED,
                PlaybackException.ERROR_CODE_DECODING_FAILED -> "تعذر فك تشفير البث، جارٍ التبديل للرابط الاحتياطي..."
                else -> "خطأ في البث: ${error.message ?: error.errorCodeName}"
            }
            handlePlaybackError(userFriendlyMessage)
        }
    }

    fun getRecordedClipSeconds(): Float {
        return rollingBufferProcessor.getRecordedSeconds()
    }

    fun exportRecitationClip(seconds: Int, context: Context): java.io.File? {
        val pcm = rollingBufferProcessor.extractLastSeconds(seconds)
        if (pcm.isEmpty()) return null
        val station = _playerState.value.currentStation
        val stationName = station?.name ?: "Quran_Radio"
        val file = AudioClipExporter.createClipFile(context, stationName, seconds)
        val success = AudioClipExporter.exportToWav(
            pcmData = pcm,
            sampleRate = rollingBufferProcessor.currentSampleRate,
            channelCount = rollingBufferProcessor.currentChannelCount,
            outputFile = file
        )
        return if (success) file else null
    }

    fun refreshRecognition() {
        recognitionEngine.recognizeCurrentStream()
    }

    fun navigateRecitationAyah(ayahNumber: Int) {
        recognitionEngine.selectAyah(ayahNumber)
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
