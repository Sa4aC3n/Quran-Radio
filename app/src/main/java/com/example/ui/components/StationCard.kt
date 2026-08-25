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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.model.PlaybackStatus
import com.example.model.RadioStation
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.WatermelonAccent

@Composable
fun StationCard(
    station: RadioStation,
    isCurrentStation: Boolean,
    playbackStatus: PlaybackStatus,
    onStationClick: (RadioStation) -> Unit,
    onPlayToggle: (RadioStation) -> Unit,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isPlayingThis = isCurrentStation && playbackStatus == PlaybackStatus.PLAYING

    val cardBorder = if (isCurrentStation) {
        BorderStroke(1.5.dp, SkyBluePrimary)
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    }

    val cardBg = if (isCurrentStation) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onStationClick(station) }
            .testTag("station_card_${station.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = cardBorder,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCurrentStation) 2.dp else 0.5.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Station Logo / Icon Container
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (station.logoUrl.isNotBlank()) {
                    AsyncImage(
                        model = station.logoUrl,
                        contentDescription = station.name,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(18.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Radio,
                        contentDescription = null,
                        tint = SkyBluePrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Overlay playing waveform animation if active
                if (isPlayingThis) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.Black.copy(alpha = 0.55f)),
                        contentAlignment = Alignment.Center
                    ) {
                        WaveformAnimation(
                            isPlaying = true,
                            color = PureWhite,
                            barCount = 4
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Station Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = station.name,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = if (station.reciter != null) station.reciter else "${station.country} • ${station.city}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isCurrentStation) {
                        LiveStatusBadge(status = playbackStatus)
                    } else {
                        LiveStatusBadge(status = PlaybackStatus.IDLE)
                    }
                    QualityBadge(bitrate = station.bitrate)
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Action Buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Favorite Button
                IconButton(
                    onClick = { onToggleFavorite(station.id) },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("fav_btn_${station.id}")
                ) {
                    Icon(
                        imageVector = if (station.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (station.isFavorite) "إزالة من المفضلة" else "إضافة للمفضلة",
                        tint = if (station.isFavorite) WatermelonAccent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Play / Pause Round Button matching theme aesthetic
                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable { onPlayToggle(station) }
                        .testTag("play_btn_${station.id}"),
                    shape = CircleShape,
                    color = if (isPlayingThis) SkyBluePrimary else MaterialTheme.colorScheme.primaryContainer,
                    contentColor = if (isPlayingThis) PureWhite else SkyBluePrimary,
                    border = BorderStroke(1.dp, if (isPlayingThis) Color.Transparent else MaterialTheme.colorScheme.outline)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(40.dp)
                    ) {
                        if (isPlayingThis) {
                            Icon(
                                imageVector = Icons.Default.Pause,
                                contentDescription = "إيقاف مؤقت",
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "تشغيل المحطة",
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
