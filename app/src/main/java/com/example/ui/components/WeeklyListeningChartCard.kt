package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DailyListeningStat
import com.example.model.WeeklyListeningSummary
import com.example.ui.theme.DeepEmerald
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyMuted
import com.example.ui.theme.SkyBluePrimary

/**
 * Recharts-inspired interactive weekly listening duration chart in Jetpack Compose.
 */
@Composable
fun WeeklyListeningChartCard(
    summary: WeeklyListeningSummary,
    modifier: Modifier = Modifier
) {
    var selectedDayKey by remember {
        mutableStateOf(summary.dailyStats.firstOrNull { it.isToday }?.dayKey ?: summary.dailyStats.lastOrNull()?.dayKey)
    }

    val selectedDayStat = summary.dailyStats.firstOrNull { it.dayKey == selectedDayKey }
        ?: summary.dailyStats.firstOrNull()

    // Animation progress for chart bars
    var startAnimation by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        startAnimation = true
    }

    val animProgress by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "chartBars"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("weekly_listening_stats_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, SkyBluePrimary.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header: Title & Total Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SkyBluePrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Equalizer,
                            contentDescription = null,
                            tint = SkyBluePrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "أوقات استماع القرآن هذا الأسبوع",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = "رسم بياني تفاعلي لمعدل الاستماع اليومي",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // Total hours chip
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = SkyBluePrimary.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, SkyBluePrimary.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Headphones,
                            contentDescription = null,
                            tint = SkyBluePrimary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = summary.formattedTotalTime,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SkyBluePrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Recharts-Style Interactive Tooltip Box
            AnimatedVisibility(
                visible = selectedDayStat != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                selectedDayStat?.let { day ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        border = BorderStroke(1.dp, SkyBluePrimary.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(if (day.isToday) GoldAccent else SkyBluePrimary, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "يوم ${day.dayNameAr}" + if (day.isToday) " (اليوم)" else "",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = day.formattedDuration,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    color = SkyBluePrimary
                                )
                                if (day.durationMinutes >= summary.goalMinutesPerDay) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "✓ حقق الهدف",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepEmerald
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Recharts-Style Native Bar Chart Canvas
            val maxDuration = (summary.dailyStats.maxOfOrNull { it.durationMinutes } ?: 180).coerceAtLeast(180)
            val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            val textColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            val activeGlowColor = SkyBluePrimary

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    summary.dailyStats.forEach { dayStat ->
                        val isSelected = dayStat.dayKey == selectedDayKey
                        val heightFraction = ((dayStat.durationMinutes.toFloat() / maxDuration) * animProgress).coerceIn(0.04f, 1f)

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .clickable { selectedDayKey = dayStat.dayKey },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            // Top value indicator when selected or peak
                            if (isSelected) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = SkyBluePrimary,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                ) {
                                    Text(
                                        text = "${dayStat.durationMinutes}د",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.height(18.dp))
                            }

                            // The Bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.55f)
                                    .fillMaxSize(heightFraction)
                                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = if (isSelected) {
                                                listOf(GoldAccent, SkyBluePrimary)
                                            } else if (dayStat.isToday) {
                                                listOf(SkyBluePrimary, SkyBluePrimary.copy(alpha = 0.6f))
                                            } else {
                                                listOf(SkyBluePrimary.copy(alpha = 0.75f), SkyBluePrimary.copy(alpha = 0.35f))
                                            }
                                        )
                                    )
                                    .then(
                                        if (isSelected) {
                                            Modifier.border(
                                                width = 1.5.dp,
                                                color = GoldAccent,
                                                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
                                            )
                                        } else Modifier
                                    )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Day Label below bar
                            Text(
                                text = dayStat.dayShortAr,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected || dayStat.isToday) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) SkyBluePrimary else if (dayStat.isToday) MaterialTheme.colorScheme.onSurface else textColor,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Recharts-Style Metrics Summary Row (Total, Average, Peak Day)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Metric 1: Daily Average
                StatMiniCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Timer,
                    iconTint = SkyBluePrimary,
                    title = "المعدل اليومي",
                    value = summary.formattedDailyAverage
                )

                // Metric 2: Peak Day
                StatMiniCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.LocalFireDepartment,
                    iconTint = GoldAccent,
                    title = "أكثر الأيام استماعاً",
                    value = "يوم ${summary.peakDayNameAr}"
                )

                // Metric 3: Streak
                StatMiniCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.DateRange,
                    iconTint = DeepEmerald,
                    title = "استمرار الاستماع",
                    value = "${summary.currentStreakDays} أيام متتالية"
                )
            }
        }
    }
}

@Composable
private fun StatMiniCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
