package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.example.ui.components.CreateKhatmaDialog
import com.example.ui.components.JoinKhatmaDialog
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SkyBluePrimary
import com.example.viewmodel.GroupKhatmaViewModel
import com.example.viewmodel.PersonalKhatmaViewModel
import com.example.viewmodel.RadioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupKhatmaListScreen(
    viewModel: GroupKhatmaViewModel,
    personalKhatmaViewModel: PersonalKhatmaViewModel? = null,
    radioViewModel: RadioViewModel? = null,
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Group Khatma State
    val groupKhatmas by viewModel.allKhatmas.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val stats by viewModel.myStats.collectAsState()

    // Personal Khatma State
    val personalActiveKhatma = personalKhatmaViewModel?.activeKhatma?.collectAsState()?.value

    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0: Group Khatma, 1: Personal Tracker
    var isCreateGroupDialogOpen by remember { mutableStateOf(false) }
    var isJoinGroupDialogOpen by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (selectedTabIndex == 0) SkyBluePrimary.copy(alpha = 0.15f)
                                    else EmeraldAccent.copy(alpha = 0.15f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (selectedTabIndex == 0) Icons.Default.Group else Icons.Default.Headphones,
                                contentDescription = null,
                                tint = if (selectedTabIndex == 0) SkyBluePrimary else EmeraldAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "الختمات",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = if (selectedTabIndex == 0) "الختمة الجماعية وتوزيع الأجزاء مع الأهل والأصدقاء" else "متتبع الختمة الفردية وسماع القرآن الكريم كاملاً",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            // 2 Big Prominent Switcher Cards at top (Styled to match the fluid discovery cards design)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card 1: Group Khatma
                val isGroupSelected = selectedTabIndex == 0
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { selectedTabIndex = 0 }
                        .testTag("card_tab_group_khatma"),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        if (isGroupSelected) 1.5.dp else 1.dp,
                        if (isGroupSelected) SkyBluePrimary else MaterialTheme.colorScheme.outline
                    ),
                    shadowElevation = if (isGroupSelected) 2.5.dp else 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isGroupSelected) SkyBluePrimary.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.primaryContainer
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = SkyBluePrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "الختمة الجماعية",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isGroupSelected) SkyBluePrimary else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 13.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (groupKhatmas.isNotEmpty()) "${groupKhatmas.size} ختمة نشطة" else "مشاركة الأهل",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Card 2: Khatma Tracker
                val isTrackerSelected = selectedTabIndex == 1
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { selectedTabIndex = 1 }
                        .testTag("card_tab_personal_tracker"),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        if (isTrackerSelected) 1.5.dp else 1.dp,
                        if (isTrackerSelected) SkyBluePrimary else MaterialTheme.colorScheme.outline
                    ),
                    shadowElevation = if (isTrackerSelected) 2.5.dp else 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isTrackerSelected) SkyBluePrimary.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.primaryContainer
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Headphones,
                                contentDescription = null,
                                tint = SkyBluePrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "متتبع الختمة",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isTrackerSelected) SkyBluePrimary else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 13.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (personalActiveKhatma != null) "${personalActiveKhatma.progressPercent}% تم إنجازه" else "استماع شخصي",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Animated Switch between Tab 0 (Group Khatma) and Tab 1 (Personal Tracker)
            AnimatedContent(
                targetState = selectedTabIndex,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "KhatmaTabContent",
                modifier = Modifier.fillMaxSize()
            ) { targetTab ->
                when (targetTab) {
                    0 -> {
                        // Group Khatma Content
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Hero Card with Action Buttons
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("khatma_hero_card"),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, SkyBluePrimary.copy(alpha = 0.3f)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        SkyBluePrimary.copy(alpha = 0.12f),
                                                        MaterialTheme.colorScheme.surface
                                                    )
                                                )
                                            )
                                            .padding(18.dp)
                                    ) {
                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "﴿ وَفِي ذَٰلِكَ فَلْيَتَنَافَسِ الْمُتَنَافِسُونَ ﴾",
                                                        style = MaterialTheme.typography.titleSmall.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            color = GoldAccent
                                                        )
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = "أنشئ ختمة خاصة بعائلتك أو أصدقائك، أو انضم لختمة قائمة بكود الدعوة",
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            fontSize = 11.5.sp
                                                        )
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(16.dp))

                                            // 2 Prominent Action Buttons
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                // Create Button
                                                Button(
                                                    onClick = { isCreateGroupDialogOpen = true },
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(46.dp)
                                                        .testTag("btn_create_khatma_main"),
                                                    shape = RoundedCornerShape(14.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Add,
                                                        contentDescription = null,
                                                        tint = PureWhite,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "إنشاء ختمة جديدة",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.5.sp,
                                                        color = PureWhite
                                                    )
                                                }

                                                // Join Button
                                                OutlinedButton(
                                                    onClick = { isJoinGroupDialogOpen = true },
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(46.dp)
                                                        .testTag("btn_join_khatma_main"),
                                                    shape = RoundedCornerShape(14.dp),
                                                    border = BorderStroke(1.5.dp, SkyBluePrimary),
                                                    colors = ButtonDefaults.outlinedButtonColors(
                                                        contentColor = SkyBluePrimary
                                                    )
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.GroupAdd,
                                                        contentDescription = null,
                                                        tint = SkyBluePrimary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "الانضمام بكود",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.5.sp,
                                                        color = SkyBluePrimary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Section: My Khatmas List
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp, vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "الختمات الحالية (${groupKhatmas.size})",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )

                                    if (stats.totalPartsCompleted > 0) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = EmeraldAccent.copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                text = "أنجزت ${stats.totalPartsCompleted} جزءاً 🌟",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = EmeraldAccent,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            if (groupKhatmas.isEmpty()) {
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
                                                imageVector = Icons.Default.MenuBook,
                                                contentDescription = null,
                                                tint = SkyBluePrimary.copy(alpha = 0.5f),
                                                modifier = Modifier.size(48.dp)
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                text = "لا توجد ختمات جماعية نشطة حالياً",
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "ابدأ بإنشاء ختمة وشارك كود الدعوة مع أحبابك",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            )
                                        }
                                    }
                                }
                            } else {
                                items(groupKhatmas, key = { it.id }) { khatma ->
                                    KhatmaCard(
                                        khatma = khatma,
                                        onClick = { onNavigateToDetail(khatma.id) },
                                        onShare = { viewModel.shareKhatmaInvite(context, khatma) },
                                        onCopyCode = {
                                            clipboardManager.setText(AnnotatedString(khatma.inviteCode))
                                            Toast.makeText(context, "تم نسخ كود الدعوة: ${khatma.inviteCode}", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }
                        }
                    }
                    1 -> {
                        // Personal Khatma Tracker Content
                        if (personalKhatmaViewModel != null) {
                            PersonalKhatmaTrackerContent(
                                viewModel = personalKhatmaViewModel,
                                radioViewModel = radioViewModel
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("جارِ تحميل متتبع الختمة...")
                            }
                        }
                    }
                }
            }
        }
    }

    // Create Group Khatma Dialog
    if (isCreateGroupDialogOpen) {
        CreateKhatmaDialog(
            initialUserName = userProfile.userName,
            onDismiss = { isCreateGroupDialogOpen = false },
            onCreateKhatma = { title, creator, days ->
                val targetMillis = if (days != null) System.currentTimeMillis() + (days * 86400000L) else null
                viewModel.createKhatma(title, creator, targetMillis) { created ->
                    isCreateGroupDialogOpen = false
                    onNavigateToDetail(created.id)
                }
            }
        )
    }

    // Join Group Khatma Dialog
    if (isJoinGroupDialogOpen) {
        JoinKhatmaDialog(
            initialUserName = userProfile.userName,
            onDismiss = { isJoinGroupDialogOpen = false },
            onJoinKhatma = { code, name ->
                viewModel.joinKhatma(
                    code = code,
                    userName = name,
                    onSuccess = { joined ->
                        isJoinGroupDialogOpen = false
                        onNavigateToDetail(joined.id)
                    },
                    onError = { err ->
                        Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        )
    }
}

@Composable
private fun KhatmaCard(
    khatma: GroupKhatma,
    onClick: () -> Unit,
    onShare: () -> Unit,
    onCopyCode: () -> Unit
) {
    val isCompleted = khatma.isFullyCompleted

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag("khatma_card_${khatma.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            1.dp,
            if (isCompleted) EmeraldAccent.copy(alpha = 0.5f) else SkyBluePrimary.copy(alpha = 0.25f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Title & Invite Code
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = khatma.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isCompleted) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "🎉", fontSize = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "أنشأها: ${khatma.creatorName}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    )
                }

                // Invite Code Chip
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    border = BorderStroke(0.8.dp, SkyBluePrimary.copy(alpha = 0.4f)),
                    modifier = Modifier.clickable(onClick = onCopyCode)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "نسخ الكود",
                            tint = SkyBluePrimary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = khatma.inviteCode,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = SkyBluePrimary,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar and counts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${khatma.completedCount} من ٣٠ جزءاً مكتمل",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted) EmeraldAccent else SkyBluePrimary,
                        fontSize = 12.sp
                    )
                )

                Text(
                    text = "${khatma.progressPercent}%",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { khatma.progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = if (isCompleted) EmeraldAccent else SkyBluePrimary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Footer info & Share
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${khatma.distinctMembers.size} أعضاء",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "${khatma.availableCount} جزء متاح",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (khatma.availableCount > 0) GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (khatma.availableCount > 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "مشاركة",
                            tint = SkyBluePrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
