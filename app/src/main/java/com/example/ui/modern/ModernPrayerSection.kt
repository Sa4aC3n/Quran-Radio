package com.example.ui.modern

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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.PrayerViewModel

/**
 * Modern Prayer Times & Adhan Alarm Section (2 Side-by-Side Cards).
 * Directly matches the prayer cards in the design mockup.
 */
@Composable
fun ModernPrayerSection(
    prayerViewModel: PrayerViewModel?,
    onNavigateToPrayerTimes: () -> Unit,
    onOpenAdhanSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val schedule = prayerViewModel?.dailySchedule?.collectAsState()?.value
    val nextPrayerName = schedule?.nextPrayer?.type?.arabicName ?: "الصلاة القادمة"
    val timeRemaining = if (schedule != null) {
        val totalSec = (schedule.millisUntilNextPrayer / 1000).coerceAtLeast(0)
        val hrs = totalSec / 3600
        val mins = (totalSec % 3600) / 60
        if (hrs > 0) "$hrs س $mins د" else "$mins دقيقة"
    } else ""

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Right Card (RTL): مواقيت الصلاة (Deep Blue Card)
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(115.dp)
                .shadow(3.dp, RoundedCornerShape(14.dp))
                .clip(RoundedCornerShape(14.dp))
                .clickable(onClick = onNavigateToPrayerTimes),
            shape = RoundedCornerShape(14.dp),
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                if (isDark) Color(0xFF102A45) else Color(0xFF1565C0),
                                if (isDark) Color(0xFF1E3A5F) else Color(0xFF1E88E5)
                            )
                        )
                    )
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Header Row: Title & Clock Icon
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "مواقيت الصلاة",
                                color = Color.White,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (timeRemaining.isNotEmpty()) "$nextPrayerName بعد $timeRemaining" else "وأذان الصلاة",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }

                    // Action Button Pill: "عرض المواقيت >"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.22f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "عرض المواقيت",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(8.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. Left Card (RTL): منبه الأذان (Light Card)
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(115.dp)
                .shadow(2.dp, RoundedCornerShape(14.dp))
                .clip(RoundedCornerShape(14.dp))
                .clickable(onClick = onOpenAdhanSettings),
            shape = RoundedCornerShape(14.dp),
            color = if (isDark) ModernColors.DarkCardBackground else Color(0xFFF0F7FD),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = if (isDark) ModernColors.DarkCardBorder else ModernColors.CardBorder
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Header Row: Title & Alarm Icon
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "منبه الأذان",
                                color = if (isDark) ModernColors.DarkTextPrimary else ModernColors.TextPrimary,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "لا تفوت الصلاة أبدًا",
                                color = if (isDark) ModernColors.DarkTextSecondary else ModernColors.TextSecondary,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(ModernColors.TileIconBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = ModernColors.TileIconTint,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }

                    // Action Button Pill: "إعداد المنبه >"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isDark) ModernColors.DarkTileIconBg else Color.White)
                            .border(1.dp, ModernColors.CardBorder, RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "إعداد المنبه",
                                color = if (isDark) ModernColors.DarkTextPrimary else ModernColors.TileIconTint,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = null,
                                tint = if (isDark) ModernColors.DarkTextPrimary else ModernColors.TileIconTint,
                                modifier = Modifier.size(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
