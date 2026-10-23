package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.localization.LocalAppLanguage
import com.example.localization.StationTranslator
import com.example.localization.StringKey
import com.example.localization.appString
import com.example.localization.localizedName
import com.example.model.PlaybackStatus
import com.example.ui.components.LiveStatusBadge
import com.example.ui.components.SleepTimerDialog
import com.example.ui.components.SpatialAudioDialog
import com.example.ui.components.WaveformAnimation
import com.example.ui.theme.LiveCoral
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.StatusError
import com.example.viewmodel.RadioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullPlayerScreen(
    viewModel: RadioViewModel,
    onCollapse: () -> Unit,
    modifier: Modifier = Modifier
) {
    val playerState by viewModel.playerState.collectAsState()
    val spatialConfig by viewModel.spatialAudioConfig.collectAsState()
    val station = playerState.currentStation
    val context = LocalContext.current
    var isSleepTimerOpen by remember { mutableStateOf(false) }
    var isSpatialAudioOpen by remember { mutableStateOf(false) }

    val isPlaying = playerState.status == PlaybackStatus.PLAYING
    val isConnecting = playerState.status == PlaybackStatus.CONNECTING || playerState.status == PlaybackStatus.BUFFERING
    val isError = playerState.status == PlaybackStatus.ERROR

    val pulseTransition = rememberInfiniteTransition(label = "pulseArtwork")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isPlaying) 1.03f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = appString(StringKey.PLAYER_NOW_PLAYING),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onCollapse,
                        modifier = Modifier.testTag("full_player_collapse_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Collapse Player",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                actions = {
                    // Share Button
                    IconButton(
                        onClick = {
                            if (station != null) {
                                val sendIntent: Intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "${station.name}\n${station.streamUrl}"
                                    )
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Share Station")
                                context.startActivity(shareIntent)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = appString(StringKey.PLAYER_SHARE_STATION),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Large Artwork / Station Icon in App Themed Container with transparent PNG support
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(pulseScale)
                    .shadow(16.dp, RoundedCornerShape(32.dp), ambientColor = SkyBluePrimary.copy(alpha = 0.25f), spotColor = Color.Black.copy(alpha = 0.15f))
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                SkyBluePrimary.copy(alpha = 0.2f),
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                            )
                        )
                    )
                    .border(
                        BorderStroke(1.5.dp, SkyBluePrimary.copy(alpha = 0.4f)),
                        RoundedCornerShape(32.dp)
                    )
                    .testTag("full_player_artwork"),
                contentAlignment = Alignment.Center
            ) {
                if (station?.logoUrl?.isNotBlank() == true) {
                    AsyncImage(
                        model = station.logoUrl,
                        contentDescription = station.name,
                        modifier = Modifier
                            .size(110.dp)
                            .padding(8.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Radio,
                        contentDescription = null,
                        tint = SkyBluePrimary,
                        modifier = Modifier.size(72.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Live Status Indicator & Waveform
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LiveStatusBadge(
                    status = playerState.status,
                    isAutoRetrying = playerState.isAutoRetrying
                )
                if (playerState.isUsingBackupStream) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "بث احتياطي",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                if (isPlaying) {
                    WaveformAnimation(
                        isPlaying = true,
                        color = SkyBluePrimary,
                        barCount = 5,
                        maxHeight = 18.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Station Title & Details
            val currentLang = LocalAppLanguage.current
            val localizedStationName = station?.localizedName(currentLang) ?: appString(StringKey.APP_NAME)
            val localizedSubtitle = if (station?.reciter != null) {
                StationTranslator.getReciterName(station.reciterId, station.reciter, "", currentLang)
            } else if (station != null) {
                val cName = StationTranslator.getCountryName(station.countryCode ?: "", station.country, "", currentLang)
                val cityName = StationTranslator.getCityName(station.city, currentLang)
                if (cityName.isNotBlank()) "$cName • $cityName" else cName
            } else {
                ""
            }

            Text(
                text = localizedStationName,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 22.sp
                ),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (localizedSubtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = localizedSubtitle,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = SkyBluePrimary,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )
            }

            if (station?.description?.isNotBlank() == true) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = station.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    ),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Error / Retry Banner if any
            if (playerState.errorMessage != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (playerState.isAutoRetrying) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (playerState.isAutoRetrying) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = SkyBluePrimary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = StatusError,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = playerState.errorMessage ?: "",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (playerState.isAutoRetrying) SkyBluePrimary else MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        if (!playerState.isAutoRetrying && isError) {
                            Button(
                                onClick = { viewModel.retry() },
                                colors = ButtonDefaults.buttonColors(containerColor = StatusError),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text(appString(StringKey.RETRY), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Main Playback Controls (Previous, Play/Pause, Next)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous Station
                Surface(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .clickable { viewModel.playPrevious() }
                        .testTag("full_player_prev_btn"),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shadowElevation = 1.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous Station",
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Play / Pause Large SkyBlue Button matching theme
                Surface(
                    modifier = Modifier
                        .size(76.dp)
                        .shadow(12.dp, CircleShape, ambientColor = SkyBluePrimary.copy(alpha = 0.4f), spotColor = Color.Black.copy(alpha = 0.3f))
                        .clip(CircleShape)
                        .clickable { viewModel.togglePlayPause() }
                        .testTag("full_player_main_play_btn"),
                    shape = CircleShape,
                    color = SkyBluePrimary,
                    contentColor = PureWhite
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(76.dp)
                    ) {
                        if (isConnecting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(34.dp),
                                color = PureWhite,
                                strokeWidth = 3.dp
                            )
                        } else if (isPlaying) {
                            Icon(
                                imageVector = Icons.Default.Pause,
                                contentDescription = appString(StringKey.MINI_PLAYER_PAUSE),
                                modifier = Modifier.size(38.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = appString(StringKey.MINI_PLAYER_PLAY),
                                modifier = Modifier.size(42.dp)
                            )
                        }
                    }
                }

                // Next Station
                Surface(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .clickable { viewModel.playNext() }
                        .testTag("full_player_next_btn"),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shadowElevation = 1.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next Station",
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Secondary Quick Actions (Favorite, Sleep Timer)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Favorite Button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = { station?.id?.let { viewModel.toggleFavorite(it) } },
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), CircleShape)
                            .testTag("full_player_fav_btn")
                    ) {
                        val isFav = station?.isFavorite == true
                        Icon(
                            imageVector = if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = appString(StringKey.NAV_FAVORITES),
                            tint = if (isFav) LiveCoral else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = appString(StringKey.NAV_FAVORITES),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                // Spatial Audio / Reverb Button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = { isSpatialAudioOpen = true },
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(
                                if (spatialConfig.isEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            )
                            .border(
                                BorderStroke(
                                    1.dp,
                                    if (spatialConfig.isEnabled) SkyBluePrimary else MaterialTheme.colorScheme.outline
                                ),
                                CircleShape
                            )
                            .testTag("full_player_spatial_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Headphones,
                            contentDescription = "الصوت المحيطي",
                            tint = if (spatialConfig.isEnabled) SkyBluePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (spatialConfig.isEnabled) spatialConfig.environment.titleAr else "الصوت المحيطي",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (spatialConfig.isEnabled) SkyBluePrimary else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (spatialConfig.isEnabled) FontWeight.Bold else FontWeight.Medium
                        )
                    )
                }

                // Sleep Timer Button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = { isSleepTimerOpen = true },
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(
                                if (playerState.isSleepTimerActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            )
                            .border(BorderStroke(1.dp, if (playerState.isSleepTimerActive) SkyBluePrimary else MaterialTheme.colorScheme.outline), CircleShape)
                            .testTag("full_player_timer_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AvTimer,
                            contentDescription = appString(StringKey.PLAYER_SLEEP_TIMER),
                            tint = if (playerState.isSleepTimerActive) SkyBluePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (playerState.isSleepTimerActive && playerState.sleepTimerRemainingSeconds > 0) {
                            val mins = playerState.sleepTimerRemainingSeconds / 60
                            val secs = playerState.sleepTimerRemainingSeconds % 60
                            "%02d:%02d".format(mins, secs)
                        } else {
                            appString(StringKey.PLAYER_SLEEP_TIMER)
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (playerState.isSleepTimerActive) SkyBluePrimary else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (playerState.isSleepTimerActive) FontWeight.Bold else FontWeight.Medium
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Sleep Timer Dialog
    SleepTimerDialog(
        isOpen = isSleepTimerOpen,
        isTimerActive = playerState.isSleepTimerActive,
        remainingSeconds = playerState.sleepTimerRemainingSeconds,
        onSelectMinutes = { viewModel.setSleepTimer(it) },
        onCancelTimer = { viewModel.cancelSleepTimer() },
        onDismiss = { isSleepTimerOpen = false }
    )

    // Spatial Audio & Mosques Simulation Dialog
    SpatialAudioDialog(
        isOpen = isSpatialAudioOpen,
        config = spatialConfig,
        onConfigChange = { newConfig ->
            viewModel.setSpatialAudioConfig(newConfig)
        },
        onDismiss = { isSpatialAudioOpen = false }
    )
}

