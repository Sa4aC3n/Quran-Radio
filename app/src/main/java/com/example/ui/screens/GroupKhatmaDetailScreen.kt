package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Diversity3
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GroupKhatma
import com.example.model.KhatmaCompletionType
import com.example.model.KhatmaPart
import com.example.ui.components.AutoDistributeDialog
import com.example.ui.components.PartActionDialog
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyMuted
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SkyBluePrimary
import com.example.viewmodel.GroupKhatmaViewModel

enum class PartFilter(val title: String) {
    ALL("الكل (٣٠)"),
    AVAILABLE("المتاح"),
    RESERVED("المحجوز"),
    COMPLETED("المكتمل"),
    MINE("أجزائي")
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GroupKhatmaDetailScreen(
    khatmaId: String,
    viewModel: GroupKhatmaViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val allKhatmas by viewModel.allKhatmas.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    val khatma = allKhatmas.firstOrNull { it.id == khatmaId }

    var selectedPartForAction by remember { mutableStateOf<KhatmaPart?>(null) }
    var isAutoDistributeOpen by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf(PartFilter.ALL) }

    if (khatma == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = SkyBluePrimary)
        }
        return
    }

    val currentUserName = userProfile.userName
    val filteredParts = remember(khatma.parts, selectedFilter, currentUserName) {
        when (selectedFilter) {
            PartFilter.ALL -> khatma.parts
            PartFilter.AVAILABLE -> khatma.parts.filter { it.isAvailable }
            PartFilter.RESERVED -> khatma.parts.filter { it.isReserved }
            PartFilter.COMPLETED -> khatma.parts.filter { it.isCompleted }
            PartFilter.MINE -> khatma.parts.filter {
                currentUserName.isNotBlank() && it.assignedMemberName.equals(currentUserName, ignoreCase = true)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = khatma.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "كود الدعوة: ${khatma.inviteCode}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SkyBluePrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("btn_khatma_detail_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    // Copy code action
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(khatma.inviteCode))
                            Toast.makeText(context, "تم نسخ كود الدعوة: ${khatma.inviteCode}", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "نسخ الكود",
                            tint = SkyBluePrimary
                        )
                    }

                    // Share action
                    IconButton(
                        onClick = { viewModel.shareKhatmaInvite(context, khatma) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "مشاركة الختمة",
                            tint = SkyBluePrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Progress & Celebration Header Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("khatma_progress_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(
                        1.dp,
                        if (khatma.isFullyCompleted) EmeraldAccent.copy(alpha = 0.5f) else SkyBluePrimary.copy(alpha = 0.3f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        // Celebration Banner if 30/30 complete
                        if (khatma.isFullyCompleted) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = EmeraldAccent.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, EmeraldAccent.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "🎉", fontSize = 24.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "الحمد لله، اكتملت الختمة المباركة!",
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = EmeraldAccent
                                            )
                                        )
                                        Text(
                                            text = "تقبل الله منا ومنكم صالح الأعمال وجعلها في ميزان حسناتكم 🤲",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // Progress Stats Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "إنجاز الختمة",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${khatma.completedCount} من ٣٠ جزءاً",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (khatma.isFullyCompleted) EmeraldAccent else SkyBluePrimary
                                    )
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (khatma.isFullyCompleted) EmeraldAccent.copy(alpha = 0.15f) else SkyBluePrimary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "${khatma.progressPercent}%",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (khatma.isFullyCompleted) EmeraldAccent else SkyBluePrimary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = { khatma.progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(CircleShape),
                            color = if (khatma.isFullyCompleted) EmeraldAccent else SkyBluePrimary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Stats Summary Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatMiniBadge(
                                label = "المتاح",
                                count = "${khatma.availableCount}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            StatMiniBadge(
                                label = "قيد القراءة",
                                count = "${khatma.reservedCount}",
                                color = GoldAccent
                            )
                            StatMiniBadge(
                                label = "المكتمل",
                                count = "${khatma.completedCount}",
                                color = EmeraldAccent
                            )
                            StatMiniBadge(
                                label = "الأعضاء",
                                count = "${khatma.distinctMembers.size}",
                                color = SkyBluePrimary
                            )
                        }
                    }
                }
            }

            // 2. Action Toolbar: Auto-distribute & Share
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Auto-distribute remaining parts button
                    if (khatma.availableCount > 0) {
                        Button(
                            onClick = { isAutoDistributeOpen = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("btn_auto_distribute"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary.copy(alpha = 0.9f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Diversity3,
                                contentDescription = null,
                                tint = PureWhite,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "توزيع تلقائي بالتساوي",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite
                            )
                        }
                    }

                    // Share button
                    OutlinedButton(
                        onClick = { viewModel.shareKhatmaInvite(context, khatma) },
                        modifier = Modifier
                            .weight(if (khatma.availableCount > 0) 1f else 2f)
                            .height(44.dp)
                            .testTag("btn_share_invite"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, SkyBluePrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = SkyBluePrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "دعوة مشاركين",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SkyBluePrimary
                        )
                    }
                }
            }

            // 3. Filter Chips
            item {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PartFilter.entries.forEach { filter ->
                        val isSelected = selectedFilter == filter
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = filter },
                            label = {
                                Text(
                                    text = filter.title,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SkyBluePrimary.copy(alpha = 0.15f),
                                selectedLabelColor = SkyBluePrimary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) SkyBluePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }

            // 4. Section Title: 30 Parts Grid (شبكة الأجزاء الثلاثين)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "أجزاء القرآن الكريم (${filteredParts.size})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "اضغط على الجزء لحجزه أو تسجيل إنجازه",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            // 5. Grid of 30 Juzs (represented clearly in responsive rows)
            items(filteredParts.chunked(3)) { rowParts ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowParts.forEach { part ->
                        Box(modifier = Modifier.weight(1f)) {
                            JuzGridItem(
                                part = part,
                                isCurrentUserPart = currentUserName.isNotBlank() && part.assignedMemberName.equals(currentUserName, ignoreCase = true),
                                onClick = { selectedPartForAction = part }
                            )
                        }
                    }
                    // Fill remaining slots in row if not exactly 3
                    repeat(3 - rowParts.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            // 6. Section: Participants List (الأعضاء المشاركون)
            if (khatma.distinctMembers.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "الأعضاء المشاركون (${khatma.distinctMembers.size})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                items(khatma.distinctMembers) { memberName ->
                    val memberParts = khatma.parts.filter { it.assignedMemberName.equals(memberName, ignoreCase = true) }
                    val completedCount = memberParts.count { it.isCompleted }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(SkyBluePrimary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = SkyBluePrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = memberName,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    Text(
                                        text = "الأجزاء: ${memberParts.joinToString(", ") { "ج ${it.juzNumber}" }}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (completedCount == memberParts.size) EmeraldAccent.copy(alpha = 0.15f) else GoldAccent.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "$completedCount / ${memberParts.size} مكتمل",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (completedCount == memberParts.size) EmeraldAccent else GoldAccent
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Part Action Dialog
    selectedPartForAction?.let { part ->
        PartActionDialog(
            part = part,
            currentUserName = userProfile.userName,
            onDismiss = { selectedPartForAction = null },
            onReserve = { name ->
                viewModel.reservePart(khatma.id, part.juzNumber, name)
                selectedPartForAction = null
            },
            onUnreserve = {
                viewModel.unreservePart(khatma.id, part.juzNumber)
                selectedPartForAction = null
            },
            onComplete = { name, type, note ->
                viewModel.completePart(khatma.id, part.juzNumber, name, type, note)
                selectedPartForAction = null
            },
            onToggleUncomplete = {
                viewModel.togglePartCompletion(khatma.id, part.juzNumber, false)
                selectedPartForAction = null
            }
        )
    }

    // Auto-distribute Dialog
    if (isAutoDistributeOpen) {
        AutoDistributeDialog(
            existingMembers = khatma.distinctMembers,
            currentUserName = userProfile.userName,
            availablePartsCount = khatma.availableCount,
            onDismiss = { isAutoDistributeOpen = false },
            onDistribute = { names ->
                viewModel.autoDistributeRemaining(khatma.id, names)
                isAutoDistributeOpen = false
            }
        )
    }
}

@Composable
private fun JuzGridItem(
    part: KhatmaPart,
    isCurrentUserPart: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        part.isCompleted -> EmeraldAccent.copy(alpha = 0.15f)
        part.isReserved -> GoldAccent.copy(alpha = 0.15f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    }

    val borderColor = when {
        part.isCompleted -> EmeraldAccent.copy(alpha = 0.7f)
        part.isReserved -> GoldAccent.copy(alpha = 0.7f)
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("juz_card_${part.juzNumber}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(
            if (isCurrentUserPart) 2.dp else 1.dp,
            if (isCurrentUserPart) SkyBluePrimary else borderColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (part.isCompleted || part.isReserved) 1.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Juz Number & Status Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${part.juzNumber}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = when {
                            part.isCompleted -> EmeraldAccent
                            part.isReserved -> GoldAccent
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                )

                if (part.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "مكتمل",
                        tint = EmeraldAccent,
                        modifier = Modifier.size(16.dp)
                    )
                } else if (part.isReserved) {
                    Text(text = "⏳", fontSize = 12.sp)
                } else {
                    Text(
                        text = "متاح",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 9.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Surah range subtitle
            Text(
                text = part.surahRange,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Assigned Member Name / Status
            if (part.assignedMemberName != null) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when {
                        part.isCompleted -> EmeraldAccent.copy(alpha = 0.2f)
                        else -> GoldAccent.copy(alpha = 0.25f)
                    }
                ) {
                    Text(
                        text = "${part.completionType.emoji} ${part.assignedMemberName}",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.5.sp,
                            color = when {
                                part.isCompleted -> EmeraldAccent
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            } else {
                Text(
                    text = "اضغط للحجز",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.5.sp,
                        color = SkyBluePrimary,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

@Composable
private fun StatMiniBadge(
    label: String,
    count: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )
        )
    }
}
