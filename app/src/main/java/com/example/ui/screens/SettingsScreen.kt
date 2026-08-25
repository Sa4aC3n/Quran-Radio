package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppThemeMode
import com.example.ui.components.AdminStationDialog
import com.example.ui.components.SleepTimerDialog
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.NavyMuted
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.NavySecondary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SkyBlueLight
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SoftSkyBackground
import com.example.viewmodel.RadioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: RadioViewModel,
    onNavigateToAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentThemeMode by viewModel.themeMode.collectAsState()
    val isAutoPlay by viewModel.isAutoPlayEnabled.collectAsState()
    val isBackgroundPlay by viewModel.isBackgroundPlayEnabled.collectAsState()
    val isWifiOnly by viewModel.isWifiOnlyEnabled.collectAsState()
    val isNotifications by viewModel.isNotificationsEnabled.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    val importReport by viewModel.importReport.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val allStations by viewModel.allStations.collectAsState()

    var isSleepTimerOpen by remember { mutableStateOf(false) }
    var isAdminDialogOpen by remember { mutableStateOf(false) }
    var isAtheerDialogOpen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "الإعدادات والتفضيلات",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("settings_screen_content")
        ) {
            // Section 1: Theme & Appearance (Light / Dark Mode Switcher)
            Text(
                text = "المظهر والوضع الليلي",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = SkyBluePrimary
                ),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("theme_switcher_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = SkyBluePrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "نمط المظهر",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = when (currentThemeMode) {
                                    AppThemeMode.SYSTEM -> "تلقائي (يتبع الوضع الافتراضي للجهاز)"
                                    AppThemeMode.LIGHT -> "الوضع النهاري الفاتح"
                                    AppThemeMode.DARK -> "الوضع الليلي الداكن"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3-Segment Theme Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeOptionButton(
                            title = "فاتح",
                            subtitle = "الافتراضي",
                            icon = Icons.Default.LightMode,
                            isSelected = currentThemeMode == AppThemeMode.LIGHT,
                            onClick = { viewModel.setThemeMode(AppThemeMode.LIGHT) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("theme_btn_light")
                        )

                        ThemeOptionButton(
                            title = "داكن",
                            subtitle = "ليلي",
                            icon = Icons.Default.DarkMode,
                            isSelected = currentThemeMode == AppThemeMode.DARK,
                            onClick = { viewModel.setThemeMode(AppThemeMode.DARK) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("theme_btn_dark")
                        )

                        ThemeOptionButton(
                            title = "تلقائي",
                            subtitle = "النظام",
                            icon = Icons.Default.BrightnessAuto,
                            isSelected = currentThemeMode == AppThemeMode.SYSTEM,
                            onClick = { viewModel.setThemeMode(AppThemeMode.SYSTEM) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("theme_btn_system")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 2: Playback Settings
            Text(
                text = "إعدادات التشغيل والبث",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = SkyBluePrimary
                ),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingSwitchRow(
                        icon = Icons.Default.PlayCircle,
                        title = "التشغيل التلقائي عند فتح التطبيق",
                        description = "مواصلة تشغيل آخر محطة كنت تستمع إليها مباشرة عند بدء التطبيق",
                        checked = isAutoPlay,
                        onCheckedChange = { viewModel.setAutoPlay(it) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SettingSwitchRow(
                        icon = Icons.Default.PlayCircle,
                        title = "الاستمرار في الخلفية",
                        description = "استمرار تشغيل التلاوة عند إغلاق الشاشة أو الانتقال لتطبيقات أخرى",
                        checked = isBackgroundPlay,
                        onCheckedChange = { viewModel.setBackgroundPlay(it) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SettingSwitchRow(
                        icon = Icons.Default.Wifi,
                        title = "البث عبر Wi-Fi فقط",
                        description = "توفير بيانات الهاتف ومنع الاستماع إلا عند الاتصال بشبكة Wi-Fi",
                        checked = isWifiOnly,
                        onCheckedChange = { viewModel.setWifiOnly(it) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SettingSwitchRow(
                        icon = Icons.Default.Notifications,
                        title = "إشعارات التحكم في المشغل",
                        description = "إظهار أزرار الإيقاف والتشغيل والتنقل في شريط الإشعارات وشاشة القفل",
                        checked = isNotifications,
                        onCheckedChange = { viewModel.setNotificationsEnabled(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 3: Tools & Timer
            Text(
                text = "الأدوات والمؤقت",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = SkyBluePrimary
                ),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Sleep timer row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { isSleepTimerOpen = true }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AvTimer,
                                contentDescription = null,
                                tint = SkyBluePrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "مؤقت النوم الإيقافي",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = if (playerState.isSleepTimerActive) "المؤقت نشط (${playerState.sleepTimerRemainingSeconds / 60} دقيقة متبقية)" else "حدد وقتًا لإيقاف البث تلقائيًا قبل النوم",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = null,
                            tint = NavyMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Add Custom Station / Admin Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { isAdminDialogOpen = true }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = null,
                                tint = SkyBluePrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "إضافة محطة إذاعية مخصصة",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = "إدخال رابط بث مباشر Stream URL خاص بك",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = null,
                            tint = NavyMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Atheer Radio Library Sync & Multi-Stream Status
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { isAtheerDialogOpen = true }
                            .padding(vertical = 6.dp)
                            .testTag("settings_atheer_status_item"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = null,
                                tint = SkyBluePrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "مكتبة إذاعات أثير راديو (Atheer Radio)",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = "فحص ومزامنة روابط البث المتعددة والروابط الاحتياطية",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = null,
                            tint = NavyMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 4: About Application
            Text(
                text = "حول التطبيق",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = SkyBluePrimary
                ),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_about_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onNavigateToAbout() }
                            .padding(vertical = 6.dp)
                            .testTag("settings_about_item"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "حول التطبيق",
                                tint = SkyBluePrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "حول التطبيق",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "إذاعات القرآن الكريم • الإصدار ${com.example.BuildConfig.VERSION_NAME}",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = null,
                            tint = NavyMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(90.dp))
        }
    }

    // Sleep Timer Dialog
    SleepTimerDialog(
        isOpen = isSleepTimerOpen,
        isTimerActive = playerState.isSleepTimerActive,
        remainingSeconds = playerState.sleepTimerRemainingSeconds,
        onSelectMinutes = { viewModel.setSleepTimer(it) },
        onCancelTimer = { viewModel.cancelSleepTimer() },
        onDismiss = { isSleepTimerOpen = false }
    )

    // Admin Dialog
    AdminStationDialog(
        isOpen = isAdminDialogOpen,
        onSave = { viewModel.addCustomStation(it) },
        onDismiss = { isAdminDialogOpen = false }
    )

    // Atheer Radio Status & Sync Dialog
    com.example.ui.components.AtheerStatusDialog(
        isOpen = isAtheerDialogOpen,
        report = importReport,
        isSyncing = isSyncing,
        totalDatabaseStations = allStations.size,
        onSyncNow = { viewModel.syncAtheerRadios() },
        onDismiss = { isAtheerDialogOpen = false }
    )
}

@Composable
private fun ThemeOptionButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) SkyBluePrimary else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (isSelected) PureWhite else MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(
            1.5.dp,
            if (isSelected) SkyBluePrimary else MaterialTheme.colorScheme.outline
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) PureWhite else SkyBluePrimary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isSelected) PureWhite.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            )
        }
    }
}

@Composable
private fun SettingSwitchRow(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SkyBluePrimary,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PureWhite,
                checkedTrackColor = SkyBluePrimary,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}
