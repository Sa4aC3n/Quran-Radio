package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.RadioStation
import com.example.model.SmartContextRecommendation
import com.example.model.SmartContextType
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SkyBluePrimary

@Composable
fun SmartContextCard(
    recommendation: SmartContextRecommendation,
    currentPlayingStation: RadioStation?,
    isPlaying: Boolean,
    onPlayStation: (RadioStation) -> Unit,
    onTogglePlayPause: () -> Unit,
    onNextRecommendation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val station = recommendation.currentStation ?: return
    val isThisStationActive = currentPlayingStation?.id == station.id
    val isActuallyPlaying = isThisStationActive && isPlaying

    val gradientColors = when (recommendation.contextType) {
        SmartContextType.FAJR_DAWN -> listOf(
            Color(0xFF2C3E50).copy(alpha = 0.95f),
            Color(0xFF3498DB).copy(alpha = 0.85f),
            Color(0xFFF39C12).copy(alpha = 0.65f)
        )
        SmartContextType.FRIDAY_BLESSINGS -> listOf(
            Color(0xFF0F382A).copy(alpha = 0.95f),
            Color(0xFF1E824C).copy(alpha = 0.85f),
            Color(0xFFD4AF37).copy(alpha = 0.60f)
        )
        SmartContextType.DAY_WORK_AND_DHIKR -> listOf(
            Color(0xFF1A5276).copy(alpha = 0.95f),
            Color(0xFF2980B9).copy(alpha = 0.85f),
            Color(0xFF5DADE2).copy(alpha = 0.60f)
        )
        SmartContextType.EVENING_SERENITY -> listOf(
            Color(0xFF2C3E50).copy(alpha = 0.95f),
            Color(0xFFD35400).copy(alpha = 0.85f),
            Color(0xFF8E44AD).copy(alpha = 0.60f)
        )
        SmartContextType.NIGHT_TRANQUILITY -> listOf(
            Color(0xFF0B1021).copy(alpha = 0.98f),
            Color(0xFF1B263B).copy(alpha = 0.90f),
            Color(0xFF415A77).copy(alpha = 0.70f)
        )
        SmartContextType.RAMADAN_NIGHTS -> listOf(
            Color(0xFF140D2B).copy(alpha = 0.95f),
            Color(0xFF2E1A47).copy(alpha = 0.85f),
            Color(0xFFD4AF37).copy(alpha = 0.65f)
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("smart_context_card"),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(gradientColors))
                .padding(18.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top Row: Badge + Recommendation Cycle button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Badge Chip
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PureWhite.copy(alpha = 0.20f),
                        border = BorderStroke(0.5.dp, PureWhite.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = recommendation.contextType.badgeAr,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PureWhite,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }

                    // Next Recommendation Shuffle Button
                    if (recommendation.recommendedStations.size > 1) {
                        Surface(
                            shape = CircleShape,
                            color = PureWhite.copy(alpha = 0.18f),
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable(onClick = onNextRecommendation)
                                .testTag("btn_smart_context_next")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "اقتراح آخر",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = PureWhite.copy(alpha = 0.9f),
                                        fontSize = 11.sp
                                    )
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Icon(
                                    imageVector = Icons.Default.Shuffle,
                                    contentDescription = "اقتراح آخر",
                                    tint = PureWhite,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Context Title & Subtitle
                Text(
                    text = recommendation.contextType.titleAr,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = PureWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = recommendation.contextType.descriptionAr,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = PureWhite.copy(alpha = 0.85f),
                        lineHeight = 18.sp
                    )
                )

                // Prayer indicator if available
                if (recommendation.prayerContextName != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• ${recommendation.prayerContextName}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = GoldAccent.copy(alpha = 0.95f),
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Recommended Station Box (Interactive Card)
                AnimatedContent(
                    targetState = station,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(250, easing = FastOutSlowInEasing)) togetherWith
                                fadeOut(animationSpec = tween(200))
                    },
                    label = "smartStationContent"
                ) { currentTargetStation ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                if (isThisStationActive) {
                                    onTogglePlayPause()
                                } else {
                                    onPlayStation(currentTargetStation)
                                }
                            },
                        color = PureWhite.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Logo or Radio Icon
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PureWhite.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (currentTargetStation.logoUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = currentTargetStation.logoUrl,
                                        contentDescription = currentTargetStation.name,
                                        modifier = Modifier.size(46.dp),
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
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Station Name and Category
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentTargetStation.nameAr.ifBlank { currentTargetStation.name },
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = PureWhite,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = currentTargetStation.reciter?.takeIf { it.isNotBlank() }
                                        ?: currentTargetStation.category.titleAr,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = PureWhite.copy(alpha = 0.80f)
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // Play / Pause Action Button
                            Button(
                                onClick = {
                                    if (isThisStationActive) {
                                        onTogglePlayPause()
                                    } else {
                                        onPlayStation(currentTargetStation)
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isActuallyPlaying) GoldAccent else PureWhite,
                                    contentColor = Color(0xFF1E3C72)
                                ),
                                contentPadding = ButtonDefaults.ContentPadding,
                                modifier = Modifier
                                    .height(38.dp)
                                    .testTag("btn_smart_context_play")
                            ) {
                                Icon(
                                    imageVector = if (isActuallyPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isActuallyPlaying) "إيقاف مؤقت" else "تشغيل الآن",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isActuallyPlaying) "استماع..." else "استمع الآن",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
