package com.example.ui.components

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DailyRamadanDua
import com.example.model.IslamicSeasonState
import com.example.model.RadioStation
import com.example.model.SeasonStreamSource
import com.example.model.TaraweehLiveStatus
import com.example.ui.theme.DeepEmerald
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LiveCoral
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SkyBluePrimary

/**
 * High-craft, intelligent Ramadan Smart Banner.
 * Shows Live Taraweeh / Tahajjud stream with one-click play,
 * Iftar & Imsak countdowns, Ramadan Khatmah tracker, and authentic daily Duas.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RamadanBanner(
    seasonState: IslamicSeasonState,
    onPlayStation: (RadioStation) -> Unit,
    onSelectStreamSource: (SeasonStreamSource) -> Unit,
    onToggleKhatmahJuz: (Int) -> Unit,
    onResetKhatmah: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isDuaSheetOpen by remember { mutableStateOf(false) }
    var isKhatmahSheetOpen by remember { mutableStateOf(false) }
    var isSourcePickerOpen by remember { mutableStateOf(false) }

    // Live pulse animation for Taraweeh live state
    val infiniteTransition = rememberInfiniteTransition(label = "ramadan_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("ramadan_smart_banner"),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            DeepEmerald.copy(alpha = 0.95f),
                            Color(0xFF0F3A2E)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            // 1. Header: Season Title & Hijri Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GoldAccent.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🌙", fontSize = 18.sp)
                    }
                    Column {
                        Text(
                            text = "شهر رمضان المبارك",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent,
                                fontSize = 16.sp
                            )
                        )
                        Text(
                            text = seasonState.hijriDetails.formattedAr,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = PureWhite.copy(alpha = 0.85f),
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                // Stream Source Badge / Switcher
                Surface(
                    onClick = { isSourcePickerOpen = true },
                    shape = RoundedCornerShape(12.dp),
                    color = PureWhite.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.4f)),
                    modifier = Modifier.testTag("ramadan_stream_source_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mosque,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = when (seasonState.selectedStreamSource) {
                                SeasonStreamSource.MAKKAH -> "الحرم المكي"
                                SeasonStreamSource.MADINAH -> "الحرم النبوي"
                                SeasonStreamSource.CAIRO -> "إذاعة القاهرة"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = PureWhite,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Taraweeh Live / Upcoming Card
            val isLive = seasonState.taraweehLiveStatus == TaraweehLiveStatus.LIVE_NOW
            val taraweehStation = seasonState.liveSeasonStation

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                        taraweehStation?.let { onPlayStation(it) }
                    }
                    .testTag("ramadan_taraweeh_live_card"),
                shape = RoundedCornerShape(16.dp),
                color = if (isLive) LiveCoral.copy(alpha = 0.2f) else PureWhite.copy(alpha = 0.08f),
                border = BorderStroke(
                    1.dp,
                    if (isLive) LiveCoral.copy(alpha = 0.8f) else GoldAccent.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isLive) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .scale(pulseScale)
                                    .clip(CircleShape)
                                    .background(LiveCoral)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.NightsStay,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = if (isLive) "🔴 بث مباشر صلاة التراويح والتهجد" else "صلاة التراويح والتهجد من الحرمين",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLive) PureWhite else GoldAccent,
                                    fontSize = 13.sp
                                )
                            )
                            Text(
                                text = if (isLive) "استمع للبث المباشر الحي الآن بضغطة واحدة"
                                else formatCountdown(seasonState.millisUntilTaraweeh, "موعد التراويح بعد: "),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = PureWhite.copy(alpha = 0.8f),
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    // Play button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isLive) LiveCoral else GoldAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "استماع",
                            tint = PureWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Iftar & Imsak Countdowns Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Iftar Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite.copy(alpha = 0.08f)),
                    border = BorderStroke(0.5.dp, PureWhite.copy(alpha = 0.15f))
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "موعد الإفطار (المغرب)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = PureWhite.copy(alpha = 0.85f),
                                    fontSize = 10.sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = seasonState.iftarTimeFormatted.ifEmpty { "--:--" },
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = GoldAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        )
                        Text(
                            text = formatShortCountdown(seasonState.millisUntilIftar),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = PureWhite.copy(alpha = 0.75f),
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                // Imsak / Suhoor Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite.copy(alpha = 0.08f)),
                    border = BorderStroke(0.5.dp, PureWhite.copy(alpha = 0.15f))
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WbSunny,
                                contentDescription = null,
                                tint = SkyBluePrimary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "الإمساك والسحور",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = PureWhite.copy(alpha = 0.85f),
                                    fontSize = 10.sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = seasonState.imsakTimeFormatted.ifEmpty { "--:--" },
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = PureWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        )
                        Text(
                            text = formatShortCountdown(seasonState.millisUntilImsak),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = PureWhite.copy(alpha = 0.75f),
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4. Quick Actions: Daily Dua & Khatmah Tracker
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Daily Dua Button
                Surface(
                    onClick = { isDuaSheetOpen = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = GoldAccent.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(text = "🤲", fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "دعاء اليوم ${seasonState.hijriDetails.day}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = PureWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // Khatmah Tracker Button
                Surface(
                    onClick = { isKhatmahSheetOpen = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = SkyBluePrimary.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, SkyBluePrimary.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = SkyBluePrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ختمة رمضان (${seasonState.khatmahState.completedCount}/30)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = PureWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }
    }

    // Modal: Stream Source Picker
    if (isSourcePickerOpen) {
        ModalBottomSheet(
            onDismissRequest = { isSourcePickerOpen = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "اختر مصدر البث المباشر لصلاة التراويح",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(14.dp))
                SeasonStreamSource.entries.forEach { source ->
                    val isSelected = seasonState.selectedStreamSource == source
                    Surface(
                        onClick = {
                            onSelectStreamSource(source)
                            isSourcePickerOpen = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) GoldAccent.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (isSelected) BorderStroke(1.dp, GoldAccent) else null
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = source.titleAr,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) GoldAccent else MaterialTheme.colorScheme.onSurface
                                )
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = GoldAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Modal: Daily Ramadan Dua Details
    if (isDuaSheetOpen && seasonState.dailyDua != null) {
        val dua = seasonState.dailyDua
        ModalBottomSheet(
            onDismissRequest = { isDuaSheetOpen = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "دعاء اليوم ${dua.day} من شهر رمضان المبارك",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )
                )
                Text(
                    text = dua.title,
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = GoldAccent.copy(alpha = 0.08f)),
                    border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = dua.arabicText,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 36.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = GoldAccent.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "المصدر: ${dua.reference}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                        Text(
                            text = "الفضل: ${dua.virtue}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = DeepEmerald,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "🌙 دعاء اليوم ${dua.day} من رمضان:\n\n${dua.arabicText}\n\n📖 المصدر: ${dua.reference}\n(من تطبيق إذاعات القرآن الكريم ومواقيت الصلاة)")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "مشاركة الدعاء"))
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("مشاركة الدعاء")
                    }

                    OutlinedButton(
                        onClick = { isDuaSheetOpen = false },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إغلاق")
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Modal: Ramadan Khatmah Tracker (30 Juz)
    if (isKhatmahSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { isKhatmahSheetOpen = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "متتبع ختمة القرآن في رمضان",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "أنجزت ${seasonState.khatmahState.completedCount} من أصل 30 جزءاً (${(seasonState.khatmahState.progressPercentage * 100).toInt()}%)",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                    IconButton(onClick = { onResetKhatmah() }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "إعادة ضبط", tint = MaterialTheme.colorScheme.error)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { seasonState.khatmahState.progressPercentage },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(CircleShape),
                    color = SkyBluePrimary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "اضغط على الجزء لتحديده كمقروء/مستمع إليه:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (1..30).forEach { juz ->
                        val isDone = seasonState.khatmahState.completedJuzSet.contains(juz)
                        Surface(
                            onClick = { onToggleKhatmahJuz(juz) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isDone) DeepEmerald else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(
                                1.dp,
                                if (isDone) DeepEmerald else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.size(54.dp, 44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "جـ $juz",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isDone) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isDone) PureWhite else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

private fun formatCountdown(millis: Long, prefix: String): String {
    if (millis <= 0L) return "حان وقت الصلاة"
    val hours = millis / 3600000L
    val minutes = (millis % 3600000L) / 60000L
    return "$prefix $hours س $minutes د"
}

private fun formatShortCountdown(millis: Long): String {
    if (millis <= 0L) return "الآن"
    val hours = millis / 3600000L
    val minutes = (millis % 3600000L) / 60000L
    return if (hours > 0) "باقي $hours س و $minutes د" else "باقي $minutes دقيقة"
}
