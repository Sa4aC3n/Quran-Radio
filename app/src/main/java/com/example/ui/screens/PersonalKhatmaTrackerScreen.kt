package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PersonalKhatma
import com.example.model.PersonalKhatmaSourceType
import com.example.ui.components.CreatePersonalKhatmaDialog
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyDark
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SkyBluePrimary
import com.example.viewmodel.PersonalKhatmaViewModel
import com.example.viewmodel.RadioViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PersonalKhatmaTrackerContent(
    viewModel: PersonalKhatmaViewModel,
    radioViewModel: RadioViewModel? = null,
    onNavigateToStation: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allKhatmas by viewModel.allKhatmas.collectAsState()
    val activeKhatma by viewModel.activeKhatma.collectAsState()

    var isCreateDialogOpen by remember { mutableStateOf(false) }
    var khatmaToDelete by remember { mutableStateOf<PersonalKhatma?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Action Bar & Switcher
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "متتبع الختمة الفردية",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "سجل إنجازك الشخصي لسماع القرآن الكريم كاملاً",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    )
                }

                Button(
                    onClick = { isCreateDialogOpen = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent),
                    modifier = Modifier.testTag("btn_start_new_personal_khatma")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = PureWhite,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "ختمة جديدة",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                }
            }
        }

        // Multiple Khatmas Selector Chips (if user has more than 1)
        if (allKhatmas.size > 1) {
            item {
                Column {
                    Text(
                        text = "ختماتي النشطة:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(allKhatmas, key = { it.id }) { k ->
                            val isSelected = k.id == activeKhatma?.id
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) EmeraldAccent.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) EmeraldAccent else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier
                                    .clickable { viewModel.selectKhatma(k.id) }
                                    .testTag("khatma_chip_${k.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = when (k.sourceType) {
                                            PersonalKhatmaSourceType.STATION -> Icons.Default.Radio
                                            PersonalKhatmaSourceType.RECITER -> Icons.Default.Mic
                                            PersonalKhatmaSourceType.CUSTOM -> Icons.Default.MenuBook
                                        },
                                        contentDescription = null,
                                        tint = if (isSelected) EmeraldAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = k.title,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) EmeraldAccent else MaterialTheme.colorScheme.onSurface
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${k.completedCount}/30",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (k.isFullyCompleted) GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Active Khatma Display
        val currentKhatma = activeKhatma
        if (currentKhatma != null) {
            // Inactivity Reminder Banner (if 3+ days inactive and not completed)
            if (currentKhatma.isInactiveReminderNeeded) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = GoldAccent.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(GoldAccent.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = GoldAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "تذكير لطيف بمتابعة الختمة 🌿",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = "مضت ${currentKhatma.daysSinceLastUpdate} أيام منذ آخر استماع. الجزء المتوقع هو: ${PersonalKhatma.JUZ_META.getOrNull(currentKhatma.nextExpectedJuz - 1)?.first}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.5.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Celebration Banner when Completed (30/30)
            if (currentKhatma.isFullyCompleted) {
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = EmeraldAccent.copy(alpha = 0.15f),
                        border = BorderStroke(1.5.dp, EmeraldAccent),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "🎉", fontSize = 32.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "الحمد لله، أتممت ختمة من ${currentKhatma.sourceName}!",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldAccent
                                ),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "هنيئاً لك إتمام الاستماع لكتاب الله كاملاً، نسأل الله أن يجعله نوراً لك وشفيعاً يوم القيامة.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    fontSize = 11.5.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { isCreateDialogOpen = true },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = PureWhite)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("ابدأ ختمة جديدة", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                OutlinedButton(
                                    onClick = { viewModel.markAllParts(currentKhatma.id, false) },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, EmeraldAccent)
                                ) {
                                    Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp), tint = EmeraldAccent)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("إعادة الختمة", color = EmeraldAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Hero Progress Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("personal_khatma_hero_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, EmeraldAccent.copy(alpha = 0.35f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        EmeraldAccent.copy(alpha = 0.12f),
                                        MaterialTheme.colorScheme.surface
                                    )
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Column {
                            // Header row with source info & menu
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(EmeraldAccent.copy(alpha = 0.18f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when (currentKhatma.sourceType) {
                                                PersonalKhatmaSourceType.STATION -> Icons.Default.Radio
                                                PersonalKhatmaSourceType.RECITER -> Icons.Default.Mic
                                                PersonalKhatmaSourceType.CUSTOM -> Icons.Default.MenuBook
                                            },
                                            contentDescription = null,
                                            tint = EmeraldAccent,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = currentKhatma.title,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "المصدر: ${currentKhatma.sourceName}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 11.sp
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                // Options dropdown
                                var isMenuExpanded by remember { mutableStateOf(false) }
                                Box {
                                    IconButton(onClick = { isMenuExpanded = true }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "حذف الختمة",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = isMenuExpanded,
                                        onDismissRequest = { isMenuExpanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("إعادة تعيين الأجزاء", fontSize = 12.sp) },
                                            leadingIcon = { Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                            onClick = {
                                                isMenuExpanded = false
                                                viewModel.markAllParts(currentKhatma.id, false)
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("تعليم الكل كمكتمل", fontSize = 12.sp) },
                                            leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                            onClick = {
                                                isMenuExpanded = false
                                                viewModel.markAllParts(currentKhatma.id, true)
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("حذف الختمة", color = MaterialTheme.colorScheme.error, fontSize = 12.sp) },
                                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp)) },
                                            onClick = {
                                                isMenuExpanded = false
                                                khatmaToDelete = currentKhatma
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Big Progress stats
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Text(
                                        text = "تم إنجاز ${currentKhatma.completedCount} من 30 جزءاً",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldAccent,
                                            fontSize = 15.sp
                                        )
                                    )
                                    val dateStr = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("ar")).format(Date(currentKhatma.lastUpdatedAtMillis))
                                    Text(
                                        text = "آخر تحديث: $dateStr",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 10.sp
                                        )
                                    )
                                }

                                Text(
                                    text = "${currentKhatma.progressPercent}%",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (currentKhatma.isFullyCompleted) GoldAccent else EmeraldAccent
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Horizontal smooth progress bar
                            LinearProgressIndicator(
                                progress = { currentKhatma.progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(CircleShape),
                                color = if (currentKhatma.isFullyCompleted) GoldAccent else EmeraldAccent,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            // Next expected part badge
                            if (!currentKhatma.isFullyCompleted) {
                                Spacer(modifier = Modifier.height(14.dp))
                                val nextJuz = currentKhatma.nextExpectedJuz
                                val nextMeta = PersonalKhatma.JUZ_META.getOrNull(nextJuz - 1)
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.togglePart(currentKhatma.id, nextJuz) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = GoldAccent,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = "الجزء التالي المتوقع: ${nextMeta?.first}",
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                )
                                                Text(
                                                    text = nextMeta?.second ?: "",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        fontSize = 10.sp
                                                    )
                                                )
                                            }
                                        }

                                        Text(
                                            text = "اضغط للتعليم ✓",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = EmeraldAccent,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 30 Juz Grid Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "أجزاء القرآن الكريم (٣٠ جزءاً)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "اضغط على أي جزء لتحديده كمنجز",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.5.sp
                        )
                    )
                }
            }

            // 30 Juzs Grid Item (Rendered cleanly with FlowRow or 3 items per row)
            item {
                val nextExpected = currentKhatma.nextExpectedJuz
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Group into chunks of 3 for a nice grid
                    val chunks = (1..30).chunked(3)
                    for (row in chunks) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (juzNum in row) {
                                val isCompleted = currentKhatma.completedParts.getOrElse(juzNum - 1) { false }
                                val isNext = juzNum == nextExpected && !isCompleted
                                val meta = PersonalKhatma.JUZ_META[juzNum - 1]

                                JuzGridCard(
                                    juzNumber = juzNum,
                                    titleAr = meta.first,
                                    surahRange = meta.second,
                                    isCompleted = isCompleted,
                                    isNextExpected = isNext,
                                    onClick = { viewModel.togglePart(currentKhatma.id, juzNum) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // No Khatmas state
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Headphones,
                            contentDescription = null,
                            tint = EmeraldAccent.copy(alpha = 0.6f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "لا توجد ختمة فردية حالياً",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "ابدأ ختمة استماع شخصية من إذاعتك أو قارئك المفضل وتتبع إنجازك جزءاً بجزء",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { isCreateDialogOpen = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = PureWhite, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ابدأ ختمة جديدة الآن", color = PureWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Create New Khatma Dialog
    if (isCreateDialogOpen) {
        CreatePersonalKhatmaDialog(
            onDismiss = { isCreateDialogOpen = false },
            onCreateKhatma = { title, sourceName, sourceType, sourceId, sourceLogo, startJuz ->
                viewModel.createKhatma(
                    title = title,
                    sourceName = sourceName,
                    sourceType = sourceType,
                    sourceId = sourceId,
                    sourceLogoUrl = sourceLogo,
                    startJuzNumber = startJuz,
                    onSuccess = {
                        isCreateDialogOpen = false
                        Toast.makeText(context, "تم بدء الختمة المباركة بنجاح 🌿", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        )
    }

    // Delete Confirmation Dialog
    if (khatmaToDelete != null) {
        AlertDialog(
            onDismissRequest = { khatmaToDelete = null },
            title = { Text("حذف الختمة", fontWeight = FontWeight.Bold) },
            text = { Text("هل أنت متأكد من حذف '${khatmaToDelete?.title}'؟ لا يمكن التراجع عن هذا الإجراء.") },
            confirmButton = {
                Button(
                    onClick = {
                        khatmaToDelete?.let { viewModel.deleteKhatma(it.id) }
                        khatmaToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("حذف", color = PureWhite)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { khatmaToDelete = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
private fun JuzGridCard(
    juzNumber: Int,
    titleAr: String,
    surahRange: String,
    isCompleted: Boolean,
    isNextExpected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardColor = when {
        isCompleted -> EmeraldAccent.copy(alpha = 0.14f)
        isNextExpected -> GoldAccent.copy(alpha = 0.12f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    }

    val borderColor = when {
        isCompleted -> EmeraldAccent.copy(alpha = 0.8f)
        isNextExpected -> GoldAccent
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
    }

    val borderWidth = if (isCompleted || isNextExpected) 1.5.dp else 1.dp

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("juz_card_$juzNumber"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = BorderStroke(borderWidth, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isNextExpected) 2.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row: Juz Number circle & Status Check
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            if (isCompleted) EmeraldAccent
                            else if (isNextExpected) GoldAccent
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$juzNumber",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isCompleted || isNextExpected) PureWhite else MaterialTheme.colorScheme.onSurface,
                            fontSize = 11.sp
                        )
                    )
                }

                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "مكتمل",
                        tint = EmeraldAccent,
                        modifier = Modifier.size(18.dp)
                    )
                } else if (isNextExpected) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "التالي",
                        tint = GoldAccent,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircleOutline,
                        contentDescription = "لم يستمع بعد",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = titleAr,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isCompleted) EmeraldAccent else MaterialTheme.colorScheme.onSurface,
                    fontSize = 11.5.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = surahRange,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 9.sp,
                    textAlign = TextAlign.Center
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
