package com.example.ui.components

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.data.HaramainStreamRepository
import com.example.model.HaramainChannelId
import com.example.model.HaramainLiveChannel
import com.example.model.HaramainPlayerUiState
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DeepEmerald
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LiveCoral
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SkyBlueDark
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.StatusConnecting
import com.example.ui.theme.StatusError
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "HARAMAIN"

@Composable
fun HaramainLiveCard(
    modifier: Modifier = Modifier,
    initialChannelId: HaramainChannelId? = null,
    onStopRadio: () -> Unit = {},
    onClose: () -> Unit = {}
) {
    val repository = remember { HaramainStreamRepository() }
    val channels = remember { repository.getChannels() }
    val makkahChannel = remember { repository.getChannel(HaramainChannelId.MAKKAH) }
    val madinahChannel = remember { repository.getChannel(HaramainChannelId.MADINAH) }

    var activeChannel by remember { mutableStateOf<HaramainLiveChannel?>(null) }

    LaunchedEffect(initialChannelId) {
        if (initialChannelId != null) {
            onStopRadio()
            activeChannel = repository.getChannel(initialChannelId)
        }
    }
    var isFullWidth by remember { mutableStateOf(false) }
    var isFullScreen by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Automatically trigger fullscreen landscape when user rotates phone to landscape while live stream is open
    LaunchedEffect(isLandscape, activeChannel) {
        if (activeChannel != null) {
            if (isLandscape && !isFullScreen) {
                isFullScreen = true
            } else if (!isLandscape && isFullScreen) {
                isFullScreen = false
            }
        }
    }

    val horizontalPadding by animateDpAsState(
        targetValue = if (isFullWidth && activeChannel != null) 0.dp else 16.dp,
        label = "haramain_h_padding"
    )
    val verticalPadding by animateDpAsState(
        targetValue = if (isFullWidth && activeChannel != null) 2.dp else 6.dp,
        label = "haramain_v_padding"
    )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding, vertical = verticalPadding)
        ) {
            if (activeChannel == null) {
                // Two side-by-side cards view
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. Right side (RTL): 🕋 Makkah Live Channel
                    HaramainChannelThumbCard(
                        channel = makkahChannel,
                        iconEmoji = "🕋",
                        bgGradient = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364)),
                        accentColor = GoldAccent,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("haramain_card_makkah"),
                        onClick = {
                            Log.d(TAG, "User selected Makkah live stream")
                            onStopRadio()
                            activeChannel = makkahChannel
                        }
                    )

                    // 2. Left side (RTL): 🕌 Madinah Live Channel
                    HaramainChannelThumbCard(
                        channel = madinahChannel,
                        iconEmoji = "🕌",
                        bgGradient = listOf(Color(0xFF0D3222), Color(0xFF134E33), Color(0xFF064E3B)),
                        accentColor = Color(0xFF10B981),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("haramain_card_madinah"),
                        onClick = {
                            Log.d(TAG, "User selected Madinah live stream")
                            onStopRadio()
                            activeChannel = madinahChannel
                        }
                    )
                }
            } else {
                // Embedded Media3 Video Player
                val current = activeChannel!!
                HaramainEmbeddedPlayer(
                    channel = current,
                    isFullWidth = isFullWidth,
                    onToggleFullWidth = {
                        isFullWidth = !isFullWidth
                    },
                    onClose = {
                        activeChannel = null
                        isFullWidth = false
                        onClose()
                    },
                    onSwitchChannel = {
                        val next = if (current.id == HaramainChannelId.MAKKAH) madinahChannel else makkahChannel
                        activeChannel = next
                    },
                    onFullScreenToggle = {
                        isFullScreen = true
                    }
                )
            }
        }
    }

    // Full Screen Overlay Mode
    if (isFullScreen && activeChannel != null) {
        val current = activeChannel!!
        HaramainFullScreenDialog(
            channel = current,
            onDismiss = {
                isFullScreen = false
            },
            onSwitchChannel = {
                val next = if (current.id == HaramainChannelId.MAKKAH) madinahChannel else makkahChannel
                activeChannel = next
            }
        )
    }
}

/**
 * Thumbnail Card for selecting either Makkah or Madinah
 */
@Composable
private fun HaramainChannelThumbCard(
    channel: HaramainLiveChannel,
    iconEmoji: String,
    bgGradient: List<Color>,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_trans")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .shadow(4.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(bgGradient))
                .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top row: Live Badge & Emoji Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Live Badge
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(LiveCoral.copy(alpha = 0.25f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(LiveCoral.copy(alpha = pulseAlpha))
                        )
                        Text(
                            text = "مباشر",
                            color = PureWhite,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Channel Icon/Emoji
                    Text(
                        text = iconEmoji,
                        fontSize = 20.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Big Play Circle Button in Center
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.25f))
                        .border(1.5.dp, accentColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "تشغيل ${channel.nameAr}",
                        tint = PureWhite,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Title
                Text(
                    text = channel.nameAr,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = PureWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Subtitle
                Text(
                    text = channel.subtitleAr,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = PureWhite.copy(alpha = 0.75f),
                        fontSize = 10.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Embedded Native Media3 ExoPlayer inside the Card
 */
@OptIn(UnstableApi::class)
@Composable
private fun HaramainEmbeddedPlayer(
    channel: HaramainLiveChannel,
    isFullWidth: Boolean,
    onToggleFullWidth: () -> Unit,
    onClose: () -> Unit,
    onSwitchChannel: () -> Unit,
    onFullScreenToggle: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val cornerRadius by animateDpAsState(
        targetValue = if (isFullWidth) 0.dp else 20.dp,
        label = "player_corner_radius"
    )
    val shadowElevation by animateDpAsState(
        targetValue = if (isFullWidth) 0.dp else 6.dp,
        label = "player_shadow"
    )
    val borderWidth by animateDpAsState(
        targetValue = if (isFullWidth) 0.dp else 1.dp,
        label = "player_border"
    )

    var playerUiState by remember { mutableStateOf(HaramainPlayerUiState.CONNECTING) }
    var isPlaying by remember { mutableStateOf(true) }
    var isMuted by remember { mutableStateOf(false) }
    var resizeMode by remember { mutableIntStateOf(AspectRatioFrameLayout.RESIZE_MODE_ZOOM) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var currentStreamIndex by remember { mutableIntStateOf(0) }
    var retryAttempt by remember { mutableIntStateOf(0) }
    var retryJob by remember { mutableStateOf<Job?>(null) }

    val streamUrls = remember(channel) { channel.getAllStreamUrls() }

    // Pulse animation for live badge
    val infiniteTransition = rememberInfiniteTransition(label = "player_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "player_pulse_alpha"
    )

    // Build dedicated ExoPlayer instance for Video Stream
    val exoPlayer = remember(context) {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36 QuranRadioWorld/LiveTv")
            .setConnectTimeoutMs(4000)
            .setReadTimeoutMs(6000)
            .setAllowCrossProtocolRedirects(true)
            .setKeepPostFor302Redirects(true)

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                2000,   // Min buffer: 2.0s for fast live start
                12000,  // Max buffer: 12s
                1000,   // Buffer for playback start: 1.0s
                1500    // Buffer for playback after rebuffer: 1.5s
            )
            .build()

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .build()

        val renderersFactory = object : DefaultRenderersFactory(context) {
            override fun buildAudioSink(
                context: Context,
                enableFloatOutput: Boolean,
                enableAudioTrackPlaybackParams: Boolean
            ): AudioSink? {
                return DefaultAudioSink.Builder(context)
                    .setEnableFloatOutput(false)
                    .setEnableAudioTrackPlaybackParams(enableAudioTrackPlaybackParams)
                    .build()
            }
        }.apply {
            setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)
            setEnableDecoderFallback(true)
            setMediaCodecSelector(androidx.media3.exoplayer.mediacodec.MediaCodecSelector.DEFAULT)
        }

        ExoPlayer.Builder(context, renderersFactory)
            .setMediaSourceFactory(DefaultMediaSourceFactory(httpDataSourceFactory))
            .setLoadControl(loadControl)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build()
    }

    // Function to load and play stream index with error handling
    fun playStreamAtIndex(index: Int) {
        if (index >= streamUrls.size) {
            playerUiState = HaramainPlayerUiState.ERROR
            errorMessage = "تعذر الاتصال بالبث حاليًا. يرجى المحاولة مرة أخرى."
            Log.e(TAG, "All stream fallbacks exhausted for ${channel.nameAr}")
            return
        }

        val targetUrl = streamUrls[index]
        Log.d(TAG, "Playing [HARAMAIN] ${channel.nameAr} - Stream #$index: $targetUrl")
        playerUiState = HaramainPlayerUiState.CONNECTING
        errorMessage = null

        try {
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            val mediaItem = MediaItem.fromUri(targetUrl)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        } catch (e: Exception) {
            Log.e(TAG, "Error setting media item", e)
            playerUiState = HaramainPlayerUiState.ERROR
            errorMessage = e.localizedMessage
        }
    }

    // Connect player listener
    DisposableEffect(exoPlayer, channel) {
        currentStreamIndex = 0
        retryAttempt = 0
        retryJob?.cancel()

        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_BUFFERING -> {
                        playerUiState = HaramainPlayerUiState.BUFFERING
                        Log.d(TAG, "[HARAMAIN] Buffering ${channel.nameAr}...")
                    }
                    Player.STATE_READY -> {
                        playerUiState = if (exoPlayer.playWhenReady) HaramainPlayerUiState.PLAYING else HaramainPlayerUiState.PAUSED
                        isPlaying = exoPlayer.playWhenReady
                        errorMessage = null
                        retryAttempt = 0
                        Log.d(TAG, "[HARAMAIN] Playback started for ${channel.nameAr}")
                    }
                    Player.STATE_ENDED -> {
                        playerUiState = HaramainPlayerUiState.PAUSED
                    }
                    Player.STATE_IDLE -> {
                        // idle
                    }
                }
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
                if (playing) {
                    playerUiState = HaramainPlayerUiState.PLAYING
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.w(TAG, "[HARAMAIN] Playback error: ${error.message}", error)
                retryJob?.cancel()
                if (currentStreamIndex + 1 < streamUrls.size) {
                    currentStreamIndex++
                    Log.d(TAG, "[HARAMAIN] Trying fallback stream #$currentStreamIndex")
                    playerUiState = HaramainPlayerUiState.CONNECTING
                    retryJob = coroutineScope.launch {
                        delay(400)
                        playStreamAtIndex(currentStreamIndex)
                    }
                } else if (retryAttempt < 3) {
                    playerUiState = HaramainPlayerUiState.CONNECTING
                    retryJob = coroutineScope.launch {
                        retryAttempt++
                        val waitSeconds = when (retryAttempt) {
                            1 -> 2
                            2 -> 4
                            else -> 6
                        }
                        Log.d(TAG, "[HARAMAIN] Retrying cycle in ${waitSeconds}s (attempt #$retryAttempt)...")
                        delay(waitSeconds * 1000L)
                        currentStreamIndex = 0
                        playStreamAtIndex(0)
                    }
                } else {
                    playerUiState = HaramainPlayerUiState.ERROR
                    errorMessage = "تعذر الاتصال بالبث حاليًا. يرجى المحاولة مرة أخرى."
                }
            }
        }

        exoPlayer.addListener(listener)
        playStreamAtIndex(0)

        onDispose {
            retryJob?.cancel()
            exoPlayer.removeListener(listener)
            exoPlayer.stop()
            exoPlayer.release()
            Log.d(TAG, "[HARAMAIN] Player released for ${channel.nameAr}")
        }
    }

    // Sync volume with isMuted
    LaunchedEffect(isMuted) {
        exoPlayer.volume = if (isMuted) 0f else 1f
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(cornerRadius))
            .shadow(shadowElevation, RoundedCornerShape(cornerRadius)),
        shape = RoundedCornerShape(cornerRadius),
        color = DarkBackground
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(Color.Black)
                .then(
                    if (isFullWidth) Modifier
                    else Modifier.border(borderWidth, SkyBluePrimary.copy(alpha = 0.4f), RoundedCornerShape(cornerRadius))
                )
        ) {
            // 1. AndroidView Media3 PlayerView (Video Surface)
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false // Custom Compose Controls Overlay
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        setShutterBackgroundColor(android.graphics.Color.BLACK)
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                update = { view ->
                    view.player = exoPlayer
                },
                modifier = Modifier.fillMaxSize()
            )

            // 2. Top Header Bar Overlay
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.75f), Color.Transparent)
                        )
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Channel Name & Live Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val emoji = if (channel.id == HaramainChannelId.MAKKAH) "🕋" else "🕌"
                    Text(
                        text = "$emoji ${channel.nameAr}",
                        color = PureWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Live Badge
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when (playerUiState) {
                                    HaramainPlayerUiState.PLAYING -> LiveCoral
                                    HaramainPlayerUiState.CONNECTING, HaramainPlayerUiState.BUFFERING -> StatusConnecting
                                    HaramainPlayerUiState.ERROR -> StatusError
                                    else -> Color.Gray
                                }.copy(alpha = 0.85f)
                            )
                            .padding(horizontal = 5.dp, vertical = 1.5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        if (playerUiState == HaramainPlayerUiState.PLAYING) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(PureWhite.copy(alpha = pulseAlpha))
                            )
                        }
                        Text(
                            text = when (playerUiState) {
                                HaramainPlayerUiState.PLAYING -> "● مباشر"
                                HaramainPlayerUiState.CONNECTING -> "جاري الاتصال"
                                HaramainPlayerUiState.BUFFERING -> "تحميل..."
                                HaramainPlayerUiState.ERROR -> "غير متاح"
                                else -> "متوقف"
                            },
                            color = PureWhite,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Top Actions: Switch Channel, Landscape Fullscreen & Close Player
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Switch to other channel button
                    val otherChannelName = if (channel.id == HaramainChannelId.MAKKAH) "المسجد النبوي" else "الحرم المكي"
                    IconButton(
                        onClick = onSwitchChannel,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "التبديل إلى $otherChannelName",
                            tint = SkyBluePrimary,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // Fullscreen Landscape Dialog button
                    IconButton(
                        onClick = onFullScreenToggle,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = "عرض ملء الشاشة أفقيًا",
                            tint = PureWhite,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // Close Player button (Returns to 2 cards)
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق البث",
                            tint = PureWhite,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }

            // 3. Loading / Buffering Spinner
            if (playerUiState == HaramainPlayerUiState.CONNECTING || playerUiState == HaramainPlayerUiState.BUFFERING) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = SkyBluePrimary,
                            strokeWidth = 2.5.dp
                        )
                        Text(
                            text = "🟡 جاري الاتصال بالبث...",
                            color = PureWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // 4. Error State Overlay
            if (playerUiState == HaramainPlayerUiState.ERROR) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.8f))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "🔴 تعذر تشغيل البث",
                            color = PureWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "يرجى التحقق من الاتصال أو المحاولة مرة أخرى.",
                            color = PureWhite.copy(alpha = 0.75f),
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        currentStreamIndex = 0
                                        retryAttempt = 0
                                        playStreamAtIndex(0)
                                    },
                                shape = RoundedCornerShape(12.dp),
                                color = SkyBluePrimary
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "إعادة المحاولة",
                                        tint = PureWhite,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "إعادة المحاولة",
                                        color = PureWhite,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onSwitchChannel() },
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "تجربة القناة الأخرى",
                                    color = PureWhite,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 5. Bottom Controls Bar Overlay
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                        )
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Play / Pause Toggle Button
                IconButton(
                    onClick = {
                        if (isPlaying) {
                            exoPlayer.pause()
                            isPlaying = false
                            playerUiState = HaramainPlayerUiState.PAUSED
                        } else {
                            exoPlayer.play()
                            isPlaying = true
                            playerUiState = HaramainPlayerUiState.PLAYING
                        }
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل",
                        tint = PureWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Center Subtitle
                Text(
                    text = channel.subtitleAr,
                    color = PureWhite.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    maxLines = 1
                )

                // Right Actions: Mute & Full Screen
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Mute / Unmute
                    IconButton(
                        onClick = { isMuted = !isMuted },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                            contentDescription = if (isMuted) "إلغاء كتم الصوت" else "كتم الصوت",
                            tint = PureWhite,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // Full Width Expand Button (زر تكبير حجم العرض بعرض الشاشة)
                    IconButton(
                        onClick = onToggleFullWidth,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isFullWidth) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = if (isFullWidth) "تصغير العرض" else "تكبير بعرض الشاشة",
                            tint = if (isFullWidth) GoldAccent else PureWhite,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Fullscreen Interactive Dialog Player for Haramain Live TV
 */
@OptIn(UnstableApi::class)
@Composable
private fun HaramainFullScreenDialog(
    channel: HaramainLiveChannel,
    onDismiss: () -> Unit,
    onSwitchChannel: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        val activity = context as? Activity
        val originalOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        onDispose {
            activity?.requestedOrientation = originalOrientation
        }
    }

    var isPlaying by remember { mutableStateOf(true) }
    var isMuted by remember { mutableStateOf(false) }
    var resizeMode by remember { mutableIntStateOf(AspectRatioFrameLayout.RESIZE_MODE_ZOOM) }
    var playerUiState by remember { mutableStateOf(HaramainPlayerUiState.CONNECTING) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var currentStreamIndex by remember { mutableIntStateOf(0) }
    var retryJob by remember { mutableStateOf<Job?>(null) }

    val streamUrls = remember(channel) { channel.getAllStreamUrls() }

    val exoPlayer = remember(context) {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36 QuranRadioWorld/LiveTvFullscreen")
            .setConnectTimeoutMs(4000)
            .setReadTimeoutMs(6000)
            .setAllowCrossProtocolRedirects(true)
            .setKeepPostFor302Redirects(true)

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(2000, 12000, 1000, 1500)
            .build()

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .build()

        val renderersFactory = object : DefaultRenderersFactory(context) {
            override fun buildAudioSink(
                context: Context,
                enableFloatOutput: Boolean,
                enableAudioTrackPlaybackParams: Boolean
            ): AudioSink? {
                return DefaultAudioSink.Builder(context)
                    .setEnableFloatOutput(false)
                    .setEnableAudioTrackPlaybackParams(enableAudioTrackPlaybackParams)
                    .build()
            }
        }.apply {
            setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)
            setEnableDecoderFallback(true)
            setMediaCodecSelector(androidx.media3.exoplayer.mediacodec.MediaCodecSelector.DEFAULT)
        }

        ExoPlayer.Builder(context, renderersFactory)
            .setMediaSourceFactory(DefaultMediaSourceFactory(httpDataSourceFactory))
            .setLoadControl(loadControl)
            .setAudioAttributes(audioAttributes, true)
            .build()
    }

    fun playStreamAtIndex(index: Int) {
        if (index >= streamUrls.size) {
            playerUiState = HaramainPlayerUiState.ERROR
            errorMessage = "تعذر الاتصال بالبث حاليًا."
            return
        }

        val targetUrl = streamUrls[index]
        playerUiState = HaramainPlayerUiState.CONNECTING
        errorMessage = null

        try {
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            exoPlayer.setMediaItem(MediaItem.fromUri(targetUrl))
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        } catch (e: Exception) {
            playerUiState = HaramainPlayerUiState.ERROR
        }
    }

    DisposableEffect(exoPlayer, channel) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_BUFFERING -> playerUiState = HaramainPlayerUiState.BUFFERING
                    Player.STATE_READY -> {
                        playerUiState = if (exoPlayer.playWhenReady) HaramainPlayerUiState.PLAYING else HaramainPlayerUiState.PAUSED
                        isPlaying = exoPlayer.playWhenReady
                    }
                    Player.STATE_ENDED -> playerUiState = HaramainPlayerUiState.PAUSED
                    Player.STATE_IDLE -> {}
                }
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e(TAG, "[HARAMAIN-FS] Playback error: ${error.message}", error)
                retryJob?.cancel()
                if (currentStreamIndex + 1 < streamUrls.size) {
                    currentStreamIndex++
                    playerUiState = HaramainPlayerUiState.CONNECTING
                    playStreamAtIndex(currentStreamIndex)
                } else {
                    playerUiState = HaramainPlayerUiState.ERROR
                }
            }
        }

        exoPlayer.addListener(listener)
        playStreamAtIndex(0)

        onDispose {
            retryJob?.cancel()
            exoPlayer.removeListener(listener)
            exoPlayer.stop()
            exoPlayer.release()
        }
    }

    LaunchedEffect(isMuted) {
        exoPlayer.volume = if (isMuted) 0f else 1f
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        val dialogView = LocalView.current
        DisposableEffect(dialogView) {
            val window = (dialogView.parent as? DialogWindowProvider)?.window
                ?: (dialogView.rootView as? DialogWindowProvider)?.window
            if (window != null) {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
                insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
            onDispose {}
        }

        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                // Video Surface
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = exoPlayer
                            useController = false
                            this.resizeMode = resizeMode
                            setShutterBackgroundColor(android.graphics.Color.BLACK)
                            layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                    },
                    update = { view ->
                        view.resizeMode = resizeMode
                        view.player = exoPlayer
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent)
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val emoji = if (channel.id == HaramainChannelId.MAKKAH) "🕋" else "🕌"
                    Text(
                        text = "$emoji ${channel.nameAr} - مباشر",
                        color = PureWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Toggle Aspect Ratio (Fit vs Zoom/Stretch)
                        IconButton(
                            onClick = {
                                resizeMode = if (resizeMode == AspectRatioFrameLayout.RESIZE_MODE_ZOOM) {
                                    AspectRatioFrameLayout.RESIZE_MODE_FIT
                                } else {
                                    AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                }
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (resizeMode == AspectRatioFrameLayout.RESIZE_MODE_ZOOM) Icons.Default.FitScreen else Icons.Default.CropFree,
                                contentDescription = if (resizeMode == AspectRatioFrameLayout.RESIZE_MODE_ZOOM) "ملاءمة الشاشة" else "تمديد الشاشة بالكامل",
                                tint = PureWhite
                            )
                        }

                        // Switch channel
                        IconButton(
                            onClick = onSwitchChannel,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "التبديل للقناة الأخرى",
                                tint = SkyBluePrimary
                            )
                        }

                        // Exit Fullscreen
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FullscreenExit,
                                contentDescription = "الخروج من ملء الشاشة",
                                tint = PureWhite
                            )
                        }
                    }
                }

                // Loading / Buffering
                if (playerUiState == HaramainPlayerUiState.CONNECTING || playerUiState == HaramainPlayerUiState.BUFFERING) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = SkyBluePrimary)
                    }
                }

                // Bottom Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = {
                            if (isPlaying) {
                                exoPlayer.pause()
                                isPlaying = false
                            } else {
                                exoPlayer.play()
                                isPlaying = true
                            }
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل",
                            tint = PureWhite,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    IconButton(
                        onClick = { isMuted = !isMuted },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                            contentDescription = if (isMuted) "كتم الصوت" else "تشغيل الصوت",
                            tint = PureWhite,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}
