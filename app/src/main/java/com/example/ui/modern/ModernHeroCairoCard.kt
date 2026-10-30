package com.example.ui.modern

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.PlaybackStatus
import com.example.model.PlayerState
import com.example.model.RadioStation

/**
 * Cairo Quran Radio Hero Featured Banner.
 * Exactly matches the prominent top card in the mockup.
 */
@Composable
fun ModernHeroCairoCard(
    cairoStation: RadioStation?,
    playerState: PlayerState,
    onPlayCairo: () -> Unit,
    onPause: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val isCairoCurrent = playerState.currentStation?.id == "cairo_quran"
    val isPlaying = isCairoCurrent && playerState.status == PlaybackStatus.PLAYING
    val isBuffering = isCairoCurrent && (playerState.status == PlaybackStatus.BUFFERING || playerState.status == PlaybackStatus.CONNECTING)

    // Animated pulse for live status dot
    val infiniteTransition = rememberInfiniteTransition(label = "cairo_live_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .shadow(4.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        color = if (isDark) ModernColors.DarkCardBackground else Color.White
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 1. Background Cairo Skyline / Mosque Image
            AsyncImage(
                model = cairoStation?.logoUrl ?: "https://images.unsplash.com/photo-1591604129939-f1efa4d9f7fa?w=800&q=80",
                contentDescription = "إذاعة القرآن الكريم من القاهرة",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // 2. Translucent Gradient Overlay for optimal readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = if (isDark) {
                                listOf(
                                    Color(0xFF0F172A).copy(alpha = 0.92f),
                                    Color(0xFF1E293B).copy(alpha = 0.65f),
                                    Color.Transparent
                                )
                            } else {
                                listOf(
                                    Color(0xFFF0F7FD).copy(alpha = 0.95f),
                                    Color(0xFFE2F0FC).copy(alpha = 0.75f),
                                    Color.White.copy(alpha = 0.2f)
                                )
                            }
                        )
                    )
            )

            // 3. Card Content (Badge, Title, Subtitle, Status, Play Button)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Row: Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(ModernColors.HeaderDeepBlue.copy(alpha = 0.9f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Radio,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "الإذاعة الرئيسية",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Middle Row: Station Title & Description
                Column {
                    Text(
                        text = "إذاعة القرآن الكريم",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) ModernColors.DarkTextPrimary else ModernColors.TextPrimary
                    )
                    Text(
                        text = "من القاهرة",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = ModernColors.TileIconTint
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "الهيئة العامة للإذاعة والتلفزيون المصرية",
                        fontSize = 11.sp,
                        color = if (isDark) ModernColors.DarkTextSecondary else ModernColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Bottom Row: Status Pill & Play/Pause Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status Pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isPlaying) ModernColors.EmeraldLive.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = if (isPlaying) ModernColors.EmeraldLive.copy(alpha = 0.4f) else Color.Transparent
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isPlaying) ModernColors.EmeraldLive.copy(alpha = pulseAlpha)
                                        else if (isBuffering) ModernColors.GoldAccent
                                        else Color.Gray
                                    )
                            )
                            Text(
                                text = when {
                                    isPlaying -> "يعمل الآن ılı"
                                    isBuffering -> "جاري الاتصال..."
                                    else -> "متوقف"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isPlaying) ModernColors.EmeraldLive else if (isBuffering) ModernColors.GoldAccent else Color.Gray
                            )
                        }
                    }

                    // Large Circular Play Button
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .shadow(6.dp, CircleShape)
                            .clip(CircleShape)
                            .background(ModernColors.TileIconTint)
                            .clickable {
                                if (isPlaying) {
                                    onPause()
                                } else {
                                    onPlayCairo()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isBuffering) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
