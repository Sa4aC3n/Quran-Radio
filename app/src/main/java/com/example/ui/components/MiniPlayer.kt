package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.PlaybackStatus
import com.example.model.PlayerState
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.LiveCoral
import com.example.ui.theme.NavyMuted
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.NavySecondary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SkyBlueLight
import com.example.ui.theme.SkyBluePrimary

@Composable
fun MiniPlayer(
    playerState: PlayerState,
    onExpandFullPlayer: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onClosePlayer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val station = playerState.currentStation
    val isVisible = station != null && playerState.status != PlaybackStatus.STOPPED

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        if (station != null) {
            val isPlaying = playerState.status == PlaybackStatus.PLAYING
            val isConnecting = playerState.status == PlaybackStatus.CONNECTING || playerState.status == PlaybackStatus.BUFFERING

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .shadow(12.dp, RoundedCornerShape(22.dp), ambientColor = SkyBluePrimary.copy(alpha = 0.2f), spotColor = Color.Black.copy(alpha = 0.2f))
                    .clip(RoundedCornerShape(22.dp))
                    .clickable { onExpandFullPlayer() }
                    .testTag("mini_player_bar"),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Top Progress Bar for connecting state
                    if (isConnecting) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.5.dp)
                                .background(SkyBluePrimary)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Station Thumbnail in SkyBlue box matching theme
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(SkyBluePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            if (station.logoUrl.isNotBlank()) {
                                AsyncImage(
                                    model = station.logoUrl,
                                    contentDescription = station.name,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(14.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Radio,
                                    contentDescription = null,
                                    tint = PureWhite,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            if (isPlaying) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color.Black.copy(alpha = 0.45f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    WaveformAnimation(
                                        isPlaying = true,
                                        color = PureWhite,
                                        barCount = 3,
                                        maxHeight = 14.dp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Station Title & Status Subtitle
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = station.name,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 13.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = if (isConnecting) "جارٍ الاتصال بالبث..." else if (isPlaying) "مباشر الآن..." else (station.reciter ?: station.country),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isPlaying || isConnecting) SkyBluePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Play/Pause Action with Button matching theme
                        Surface(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onTogglePlayPause() }
                                .testTag("mini_player_play_pause"),
                            shape = RoundedCornerShape(12.dp),
                            color = SkyBluePrimary,
                            contentColor = PureWhite,
                            shadowElevation = 2.dp
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(40.dp)
                            ) {
                                if (isConnecting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = PureWhite,
                                        strokeWidth = 2.dp
                                    )
                                } else if (isPlaying) {
                                    Icon(
                                        imageVector = Icons.Default.Pause,
                                        contentDescription = "إيقاف مؤقت",
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "استئناف التشغيل",
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Close Button
                        IconButton(
                            onClick = onClosePlayer,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "إغلاق المشغل",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
