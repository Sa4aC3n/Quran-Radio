package com.example.ui.components

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
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
import com.example.model.Reciter
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SkyBluePrimary

@Composable
fun ReciterCard(
    reciter: Reciter,
    isCurrentReciter: Boolean = false,
    playbackStatus: PlaybackStatus = PlaybackStatus.IDLE,
    onReciterClick: (Reciter) -> Unit,
    onPlayReciterLive: (Reciter) -> Unit,
    modifier: Modifier = Modifier
) {
    val isPlayingThis = isCurrentReciter && playbackStatus == PlaybackStatus.PLAYING
    val isConnecting = isCurrentReciter && (playbackStatus == PlaybackStatus.CONNECTING || playbackStatus == PlaybackStatus.BUFFERING)

    val cardBorder = if (isCurrentReciter) {
        BorderStroke(1.5.dp, SkyBluePrimary)
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    }

    val cardBg = if (isCurrentReciter) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onReciterClick(reciter) }
            .testTag("reciter_card_${reciter.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = cardBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrentReciter) 2.dp else 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reciter Avatar / Icon in SkyBlue rounded container with transparent PNG support
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isCurrentReciter) SkyBluePrimary.copy(alpha = 0.18f)
                        else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (reciter.avatarUrl.isNotBlank()) {
                    AsyncImage(
                        model = reciter.avatarUrl,
                        contentDescription = reciter.nameAr,
                        modifier = Modifier
                            .size(32.dp)
                            .padding(2.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = SkyBluePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Overlay playing waveform animation if active
                if (isPlayingThis) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black.copy(alpha = 0.55f)),
                        contentAlignment = Alignment.Center
                    ) {
                        WaveformAnimation(
                            isPlaying = true,
                            color = PureWhite,
                            barCount = 4,
                            maxHeight = 16.dp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Details
            val currentLanguage = LocalAppLanguage.current
            val localizedReciterName = reciter.localizedName(currentLanguage)
            val localizedStyle = StationTranslator.getReciterStyle(reciter.styleAr, currentLanguage)
            val localizedBio = StationTranslator.getReciterBio(reciter, currentLanguage)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = localizedReciterName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = localizedStyle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = SkyBluePrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = localizedBio,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Live Radio Action Button (Toggles between Play, Loading, Pause)
            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable { onPlayReciterLive(reciter) }
                    .testTag("play_reciter_${reciter.id}"),
                shape = CircleShape,
                color = if (isPlayingThis) SkyBluePrimary else MaterialTheme.colorScheme.primaryContainer,
                contentColor = if (isPlayingThis) PureWhite else SkyBluePrimary,
                border = BorderStroke(1.dp, if (isPlayingThis) Color.Transparent else MaterialTheme.colorScheme.outline),
                shadowElevation = 2.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(40.dp)
                ) {
                    if (isConnecting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = if (isPlayingThis) PureWhite else SkyBluePrimary,
                            strokeWidth = 2.dp
                        )
                    } else if (isPlayingThis) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = appString(StringKey.MINI_PLAYER_PAUSE),
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = appString(StringKey.MINI_PLAYER_PLAY),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

