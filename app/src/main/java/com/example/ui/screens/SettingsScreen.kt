package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.ui.graphics.Brush
import com.example.ui.theme.SkyBlueDark

import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.AppLanguage
import com.example.localization.LanguageSelectionDialog
import com.example.localization.LocalAppLanguage
import com.example.localization.StringKey
import com.example.localization.appString
import com.example.model.AppThemeMode
import com.example.ui.components.AdminStationDialog
import com.example.ui.components.AppUpdateDialog
import com.example.ui.components.HaramLiveSettingsDialog
import com.example.ui.components.IslamicSeasonSettingsDialog
import com.example.ui.components.NotificationSettingsDialog
import com.example.ui.components.SleepTimerDialog
import com.example.ui.components.WeeklyListeningChartCard
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyMuted
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SkyBluePrimary
import com.example.util.AppUpdateHelper
import com.example.util.AppUpdateUiState
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.InstallStatus
import androidx.compose.runtime.DisposableEffect
import androidx.compose.material.icons.filled.MenuBook
import com.example.viewmodel.GroupKhatmaViewModel
import com.example.viewmodel.PrayerViewModel
import com.example.viewmodel.RadioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: RadioViewModel,
    prayerViewModel: PrayerViewModel? = null,
    groupKhatmaViewModel: GroupKhatmaViewModel? = null,
    onNavigateToAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentThemeMode by viewModel.themeMode.collectAsState()
    val currentLanguage by viewModel.appLanguage.collectAsState()
    val isAutoPlay by viewModel.isAutoPlayEnabled.collectAsState()
    val isBackgroundPlay by viewModel.isBackgroundPlayEnabled.collectAsState()
    val isWifiOnly by viewModel.isWifiOnlyEnabled.collectAsState()
    val isSmartAdhanFade by viewModel.isSmartAdhanFadeEnabled.collectAsState()
    val isNotifications by viewModel.isNotificationsEnabled.collectAsState()
    val notificationFrequency by viewModel.notificationFrequency.collectAsState()
    val notificationTimeWindow by viewModel.notificationTimeWindow.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    val importReport by viewModel.importReport.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val allStations by viewModel.allStations.collectAsState()
    val seasonState = prayerViewModel?.islamicSeasonState?.collectAsState()?.value
    val khatmaStats = groupKhatmaViewModel?.myStats?.collectAsState()?.value
    val weeklyStats by viewModel.weeklyListeningSummary.collectAsState()
    val haramPreferences by viewModel.haramLivePreferences.collectAsState()
    val context = LocalContext.current

    var isLanguageDialogOpen by remember { mutableStateOf(false) }
    var isSleepTimerOpen by remember { mutableStateOf(false) }
    var isAdminDialogOpen by remember { mutableStateOf(false) }
    var isAtheerDialogOpen by remember { mutableStateOf(false) }
    var isSeasonSettingsOpen by remember { mutableStateOf(false) }
    var isBackgroundPermissionsOpen by remember { mutableStateOf(false) }
    var isHaramDialogOpen by remember { mutableStateOf(false) }
    var isNotificationSettingsDialogOpen by remember { mutableStateOf(false) }
    var isAdhanSheetOpen by remember { mutableStateOf(false) }

    val appUpdateManager = remember { AppUpdateManagerFactory.create(context) }
    var appUpdateState by remember { mutableStateOf<AppUpdateUiState>(AppUpdateUiState.Idle) }
    var activeLegalDialogTitle by remember { mutableStateOf<String?>(null) }
    var activeLegalDialogContent by remember { mutableStateOf<String?>(null) }

    DisposableEffect(appUpdateManager) {
        val listener = InstallStateUpdatedListener { state ->
            if (state.installStatus() == InstallStatus.DOWNLOADED) {
                appUpdateState = AppUpdateUiState.UpdateDownloaded
            }
        }
        appUpdateManager.registerListener(listener)
        onDispose {
            appUpdateManager.unregisterListener(listener)
        }
    }

    val isAdhanMasterEnabled = prayerViewModel?.isAdhanMasterEnabled?.collectAsState()?.value ?: true
    val isAdhanActive = prayerViewModel?.isAdhanActive?.collectAsState()?.value ?: false
    val currentAdhanPrayer = prayerViewModel?.currentAdhanPrayer?.collectAsState()?.value
    val selectedAdhanSound = prayerViewModel?.selectedAdhanSound?.collectAsState()?.value ?: com.example.model.AdhanSound.MAKKAH
    val alertSettings = prayerViewModel?.alertSettings?.collectAsState()?.value ?: emptyMap()
    val calcMethod = prayerViewModel?.calculationMethod?.collectAsState()?.value ?: com.example.model.CalculationMethod.EGYPTIAN
    val asrJuristic = prayerViewModel?.asrJuristic?.collectAsState()?.value ?: com.example.model.AsrJuristic.STANDARD
    val minuteOffsets = prayerViewModel?.minuteOffsets?.collectAsState()?.value ?: emptyMap()
    val iqamahSettings = prayerViewModel?.iqamahAlertSettings?.collectAsState()?.value ?: emptyMap()
    val iqamahDelays = prayerViewModel?.iqamahDelays?.collectAsState()?.value ?: emptyMap()
    val previewSoundId = prayerViewModel?.adhanAudioPlayer?.isPlayingPreview?.collectAsState()?.value
    val adhanSheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = appString(StringKey.SETTINGS_TITLE),
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
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("settings_screen_content")
        ) {
            // Section 0: Language Switcher (Multilingual Selection)
            Text(
                text = appString(StringKey.LANGUAGE_SETTINGS_SECTION),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = SkyBluePrimary
                ),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { isLanguageDialogOpen = true }
                    .testTag("settings_language_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(SkyBluePrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentLanguage.flag,
                            fontSize = 22.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = appString(StringKey.SETTINGS_LANGUAGE),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${currentLanguage.nativeName} (${currentLanguage.englishName})",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SkyBluePrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SkyBluePrimary.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, SkyBluePrimary.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = appString(StringKey.LANGUAGE_CURRENT),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SkyBluePrimary,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 1: Theme & Appearance (Light / Dark Mode Switcher)
            Text(
                text = appString(StringKey.SETTINGS_THEME),
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
                                text = appString(StringKey.SETTINGS_THEME),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = when (currentThemeMode) {
                                    AppThemeMode.SYSTEM -> appString(StringKey.THEME_SYSTEM)
                                    AppThemeMode.LIGHT -> appString(StringKey.THEME_LIGHT)
                                    AppThemeMode.DARK -> appString(StringKey.THEME_DARK)
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
                            title = appString(StringKey.THEME_LIGHT),
                            icon = Icons.Default.LightMode,
                            isSelected = currentThemeMode == AppThemeMode.LIGHT,
                            onClick = { viewModel.setThemeMode(AppThemeMode.LIGHT) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("theme_btn_light")
                        )

                        ThemeOptionButton(
                            title = appString(StringKey.THEME_DARK),
                            icon = Icons.Default.DarkMode,
                            isSelected = currentThemeMode == AppThemeMode.DARK,
                            onClick = { viewModel.setThemeMode(AppThemeMode.DARK) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("theme_btn_dark")
                        )

                        ThemeOptionButton(
                            title = appString(StringKey.THEME_SYSTEM),
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
                text = appString(StringKey.SETTINGS_PLAYBACK_SECTION),
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
                        title = appString(StringKey.SETTINGS_AUTO_PLAY),
                        description = appString(StringKey.SETTINGS_AUTO_PLAY_DESC),
                        checked = isAutoPlay,
                        onCheckedChange = { viewModel.setAutoPlay(it) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SettingSwitchRow(
                        icon = Icons.Default.PlayCircle,
                        title = appString(StringKey.SETTINGS_BACKGROUND_PLAY),
                        description = appString(StringKey.SETTINGS_BACKGROUND_PLAY_DESC),
                        checked = isBackgroundPlay,
                        onCheckedChange = { viewModel.setBackgroundPlay(it) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SettingSwitchRow(
                        icon = Icons.Default.Wifi,
                        title = appString(StringKey.SETTINGS_WIFI_ONLY),
                        description = appString(StringKey.SETTINGS_WIFI_ONLY_DESC),
                        checked = isWifiOnly,
                        onCheckedChange = { viewModel.setWifiOnly(it) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SettingSwitchRow(
                        icon = Icons.Default.GraphicEq,
                        title = "الخفض الذكي للصوت عند رفع الأذان",
                        description = "خفض صوت الإذاعة تدريجياً وبنعومة عند الأذان ثم استئنافها بعد انتهاء الصلاة",
                        checked = isSmartAdhanFade,
                        onCheckedChange = { viewModel.setSmartAdhanFadeEnabled(it) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Notifications Row with Direct customization
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { isNotificationSettingsDialogOpen = true }
                            .padding(vertical = 6.dp)
                            .testTag("settings_notifications_item"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isNotifications) SkyBluePrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isNotifications) Icons.Default.Notifications else Icons.Default.Notifications,
                                contentDescription = null,
                                tint = if (isNotifications) SkyBluePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = appString(StringKey.SETTINGS_NOTIFICATIONS),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = SkyBluePrimary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "تخصيص",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SkyBluePrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (isNotifications) {
                                    val freqText = when (notificationFrequency) {
                                        "prayers_5" -> "الصلوات الخمس فقط"
                                        "twice_daily" -> "إشعاران يومياً"
                                        "once_daily" -> "إشعار واحد يومياً"
                                        else -> "كافة الإشعارات"
                                    }
                                    val windowText = when (notificationTimeWindow) {
                                        "daytime_only" -> "فترة النهار"
                                        "quiet_night" -> "الهدوء الليلي"
                                        "custom_hours" -> "07:00 ص - 10:00 م"
                                        else -> "طوال اليوم"
                                    }
                                    "$freqText • $windowText (اضغط للتعديل)"
                                } else {
                                    "متوقفة تماماً (اضغط للتشغيل والتخصيص)"
                                },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isNotifications,
                            onCheckedChange = { viewModel.setNotificationsEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PureWhite,
                                checkedTrackColor = SkyBluePrimary
                            ),
                            modifier = Modifier.testTag("switch_notifications_quick")
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Haram Live Recitation Notification Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { isHaramDialogOpen = true }
                            .padding(vertical = 6.dp)
                            .testTag("settings_haram_live_item"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(GoldAccent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🕋",
                                fontSize = 20.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "الآن يُقرأ في الحرم",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (haramPreferences.isEnabled) EmeraldAccent.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = if (haramPreferences.isEnabled) "مفعّل" else "اختياري",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (haramPreferences.isEnabled) EmeraldAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (haramPreferences.isEnabled)
                                    "تنبيه لطيف لتلاوات ${haramPreferences.source.titleAr}"
                                else
                                    "إشعار لطيف واختياري عند بدء تلاوة سورة في البث المباشر للحرمين",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = null,
                            tint = NavyMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Background Permissions & Battery Optimization Guidance Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { isBackgroundPermissionsOpen = true }
                            .padding(vertical = 6.dp)
                            .testTag("settings_background_permissions_item"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SkyBluePrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = SkyBluePrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "دقة الأذان والتطبيق مغلق (إدارة البطارية)",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = "إلغاء قيود البطارية وأذونات المنبهات الدقيقة وتشغيل الأذان 100%",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
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

            // Section 3: Tools & Timer
            Text(
                text = appString(StringKey.SETTINGS_TOOLS_SECTION),
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
                                text = appString(StringKey.SETTINGS_SLEEP_TIMER),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            val timerDesc = if (playerState.isSleepTimerActive) {
                                "${appString(StringKey.SLEEP_TIMER_STATUS_ACTIVE)} (${playerState.sleepTimerRemainingSeconds / 60} ${appString(StringKey.SLEEP_TIMER_MINUTES)})"
                            } else {
                                appString(StringKey.SETTINGS_SLEEP_TIMER_DESC)
                            }
                            Text(
                                text = timerDesc,
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
                                text = appString(StringKey.SETTINGS_CUSTOM_STATION),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = appString(StringKey.SETTINGS_CUSTOM_STATION_DESC),
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
                                text = appString(StringKey.ATHEER_SYNC_TITLE),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = appString(StringKey.ATHEER_SYNC_SUBTITLE),
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

            // Section 3.2: Adhan & Prayer Integration
            if (prayerViewModel != null) {
                Text(
                    text = "الأذان ومواقيت الصلاة",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = SkyBluePrimary
                    ),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_adhan_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, SkyBluePrimary.copy(alpha = 0.35f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Live In-App Adhan Playing Banner
                        if (isAdhanActive) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 14.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "🕌 صوت الأذان يعمل الآن (${currentAdhanPrayer?.arabicName ?: "الصلاة"})",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "تم إيقاف الإذاعة مؤقتاً وستستأنف تلقائياً بعد الأذان",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    androidx.compose.material3.Button(
                                        onClick = { prayerViewModel.stopAdhanService() },
                                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("إيقاف", color = MaterialTheme.colorScheme.onError, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        // Master Adhan Switch
                        SettingSwitchRow(
                            icon = Icons.Default.Notifications,
                            title = "تشغيل صوت الأذان التلقائي",
                            description = "إيقاف الإذاعة مؤقتاً وتشغيل الأذان عند دخول الوقت ثم استئنافها تلقائياً",
                            checked = isAdhanMasterEnabled,
                            onCheckedChange = { prayerViewModel.setAdhanMasterEnabled(it) }
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Adhan Voice Picker Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { isAdhanSheetOpen = true }
                                .padding(vertical = 6.dp)
                                .testTag("settings_adhan_voice_item"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🕌", fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "صوت الأذان والمؤذن",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${selectedAdhanSound.titleAr} (${selectedAdhanSound.muezzinAr})",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = SkyBluePrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = null,
                                tint = NavyMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Test / Stop Button & Settings Sheet Trigger
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isAdhanActive) {
                                androidx.compose.material3.Button(
                                    onClick = { prayerViewModel.stopAdhanService() },
                                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("⏹️ إيقاف صوت الأذان", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            } else {
                                androidx.compose.material3.Button(
                                    onClick = { prayerViewModel.triggerAdhanNotificationTest(com.example.model.PrayerType.MAGHRIB) },
                                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("▶️ اختبار الأذان مع الإذاعة", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }

                            androidx.compose.material3.OutlinedButton(
                                onClick = { isAdhanSheetOpen = true },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("تخصيص المواقيت", fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Section 3.5: Ramadan & Hajj Season Settings
            if (seasonState != null && prayerViewModel != null) {
                Text(
                    text = "وضع رمضان وموسم الحج المبارك",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    ),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_season_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { isSeasonSettingsOpen = true }
                                .padding(vertical = 6.dp)
                                .testTag("settings_season_item"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(GoldAccent.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (seasonState.isRamadanActive) "🌙" else if (seasonState.isHajjActive) "🕋" else "✨",
                                    fontSize = 20.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "تخصيص وضع المواسم (رمضان والحج)",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${seasonState.overrideMode.titleAr} • ${seasonState.hijriDetails.formattedAr}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = null,
                                tint = GoldAccent
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Section: Weekly Quran Listening Stats (إحصائيات استماع القرآن خلال الأسبوع)
            Text(
                text = "إحصائيات الاستماع للقرآن",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = SkyBluePrimary
                ),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            WeeklyListeningChartCard(summary = weeklyStats)

            Spacer(modifier = Modifier.height(20.dp))

            // Section: My Group Khatmas Stats (ختماتي القرآنية)
            if (khatmaStats != null) {
                Text(
                    text = "الختمات الجماعية والقرآن الكريم",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = SkyBluePrimary
                    ),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_khatma_stats_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, SkyBluePrimary.copy(alpha = 0.3f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SkyBluePrimary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = SkyBluePrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "إحصائيات مشاركاتك",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = "الختمات النشطة والأجزاء التي قرأتها",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${khatmaStats.totalKhatmasJoined}",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = SkyBluePrimary
                                    )
                                )
                                Text(
                                    text = "ختمة مشارك بها",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${khatmaStats.totalPartsCompleted}",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = com.example.ui.theme.EmeraldAccent
                                    )
                                )
                                Text(
                                    text = "أجزاء أتممتها",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${khatmaStats.totalPartsReserved}",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = GoldAccent
                                    )
                                )
                                Text(
                                    text = "قيد القراءة",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Section 4: About Application
            Text(
                text = appString(StringKey.NAV_ABOUT),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = SkyBluePrimary
                ),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. About Card (عن التطبيق)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(22.dp))
                        .clickable { onNavigateToAbout() }
                        .testTag("settings_about_card"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(13.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = appString(StringKey.NAV_ABOUT),
                                    tint = SkyBluePrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = null,
                                tint = NavyMuted.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = appString(StringKey.NAV_ABOUT),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${appString(StringKey.APP_TITLE)} • ${com.example.BuildConfig.VERSION_NAME}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            maxLines = 1
                        )
                    }
                }

                // 2. App Update Card (تحديث التطبيق)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(22.dp))
                        .clickable {
                            AppUpdateHelper.checkForAppUpdate(
                                context = context,
                                appUpdateManager = appUpdateManager,
                                onStateChange = { appUpdateState = it }
                            )
                        }
                        .testTag("settings_update_app_card"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(13.dp))
                                    .background(SkyBluePrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SystemUpdate,
                                    contentDescription = "تحديث التطبيق",
                                    tint = SkyBluePrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = null,
                                tint = NavyMuted.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "تحديث التطبيق",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "فحص أحدث نسخة",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Our Apps on Google Play (تطبيقاتنا على متجر جوجل)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .clickable { openDeveloperPage(context) }
                    .testTag("settings_dev_apps_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(SkyBluePrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shop,
                            contentDescription = "تطبيقاتنا على متجر جوجل",
                            tint = SkyBluePrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "تطبيقاتنا على متجر جوجل",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 15.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "استكشف المزيد من تطبيقاتنا الإسلامية والنافعة",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = null,
                        tint = NavyMuted.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Share & Rate Card (انشر تؤجر • شارك تطبيق القرآن الكريم)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_contact_share_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    SkyBlueDark,
                                    SkyBluePrimary,
                                    Color(0xFF004D40)
                                )
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "انشر تؤجر • شارك تطبيق القرآن الكريم",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            ),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "قال ﷺ: «مَن دلَّ على خيرٍ فله مثلُ أجرِ فاعله»",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.95f),
                                fontSize = 13.sp
                            ),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Share Button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.Black.copy(alpha = 0.22f))
                                    .border(
                                        border = BorderStroke(1.2.dp, Color.White.copy(alpha = 0.65f)),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable { shareAppFromSettings(context) }
                                    .testTag("btn_settings_share_app"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = null,
                                        tint = Color(0xFFFFD54F),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "مشاركة التطبيق",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 14.sp
                                        )
                                    )
                                }
                            }

                            // Rate Button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.Black.copy(alpha = 0.22f))
                                    .border(
                                        border = BorderStroke(1.2.dp, Color.White.copy(alpha = 0.65f)),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable { openPlayStoreFromSettings(context) }
                                    .testTag("btn_settings_rate_app"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFFFD54F),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "تقييم التطبيق",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 14.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5. Section: Legal Info (المعلومات القانونية والخصوصية)
            Text(
                text = "المعلومات القانونية والخصوصية",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = SkyBluePrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_legal_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Privacy Policy
                    SettingsLegalRowItem(
                        icon = Icons.Default.PrivacyTip,
                        title = "سياسة الخصوصية",
                        subtitle = "تطبيقنا لا يجمع أي بيانات شخصية خاصة بك",
                        onClick = {
                            activeLegalDialogTitle = "سياسة الخصوصية"
                            activeLegalDialogContent = PRIVACY_POLICY_TEXT
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Terms of Service
                    SettingsLegalRowItem(
                        icon = Icons.Default.Gavel,
                        title = "شروط الاستخدام",
                        subtitle = "تطبيق مجاني غير تجاري لوجه الله تعالى",
                        onClick = {
                            activeLegalDialogTitle = "شروط الاستخدام"
                            activeLegalDialogContent = TERMS_OF_SERVICE_TEXT
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 6. Elegant Footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .testTag("settings_footer"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Quran Radio World",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp
                    )
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "إذاعات القرآن الكريم",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = SkyBluePrimary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "صنع بكل حـــــ❤️ـــــب في مصر",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Quran Radio World 2026 ©",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        fontSize = 11.sp
                    )
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "جميع الحقوق محفوظة لكل المسلمين",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(90.dp))
        }
    }

    // Language Dialog
    if (isLanguageDialogOpen) {
        LanguageSelectionDialog(
            currentLanguage = currentLanguage,
            onLanguageSelected = { lang ->
                viewModel.setAppLanguage(lang)
            },
            onDismiss = { isLanguageDialogOpen = false }
        )
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

    // Islamic Season Settings Dialog
    if (isSeasonSettingsOpen && seasonState != null && prayerViewModel != null) {
        IslamicSeasonSettingsDialog(
            seasonState = seasonState,
            onSetSeasonMode = { prayerViewModel.setSeasonModeOverride(it) },
            onSetHijriOffset = { delta ->
                prayerViewModel.setHijriOffsetDays((prayerViewModel.hijriOffsetDays.value + delta).coerceIn(-3, 3))
            },
            onToggleRamadanBanner = { prayerViewModel.setRamadanBannerEnabled(it) },
            onToggleHajjBanner = { prayerViewModel.setHajjBannerEnabled(it) },
            onToggleTaraweehAlert = { prayerViewModel.setTaraweehAlertEnabled(it) },
            onToggleSuhoorAlert = { prayerViewModel.setSuhoorAlertEnabled(it) },
            onSetStreamSource = { prayerViewModel.setSeasonStreamSource(it) },
            onDismiss = { isSeasonSettingsOpen = false }
        )
    }

    // Haram Live Settings Dialog
    HaramLiveSettingsDialog(
        isOpen = isHaramDialogOpen,
        preferences = haramPreferences,
        onToggleEnabled = { viewModel.setHaramLiveEnabled(it) },
        onSetSource = { viewModel.setHaramLiveSource(it) },
        onToggleFavoriteOnly = { viewModel.setHaramLiveFavoriteOnly(it) },
        onToggleFavoriteSurah = { viewModel.toggleHaramFavoriteSurah(it) },
        onSetDailyLimit = { viewModel.setHaramDailyLimit(it) },
        onTestNotification = { src ->
            viewModel.testHaramLiveNotification(source = src)
        },
        onDismiss = { isHaramDialogOpen = false }
    )

    // Background Permissions & Battery Optimization Guide Dialog
    com.example.ui.components.BackgroundPermissionExplanationDialog(
        isOpen = isBackgroundPermissionsOpen,
        onDismiss = { isBackgroundPermissionsOpen = false }
    )

    // Adhan Customization Bottom Sheet
    if (isAdhanSheetOpen && prayerViewModel != null) {
        com.example.ui.components.AdhanSettingsBottomSheet(
            sheetState = adhanSheetState,
            currentMethod = calcMethod,
            currentAsrJuristic = asrJuristic,
            selectedAdhanSound = selectedAdhanSound,
            previewPlayingSoundId = previewSoundId,
            minuteOffsets = minuteOffsets,
            iqamahSettings = iqamahSettings,
            iqamahDelays = iqamahDelays,
            onSelectMethod = { prayerViewModel.setCalculationMethod(it) },
            onSelectAsrJuristic = { prayerViewModel.setAsrJuristic(it) },
            onSelectAdhanSound = { prayerViewModel.setAdhanSound(it) },
            onPlayPreviewSound = { prayerViewModel.adhanAudioPlayer.playPreview(it) },
            onStopPreviewSound = { prayerViewModel.adhanAudioPlayer.stopPreview() },
            onTestAdhanNotification = { prayerViewModel.triggerAdhanNotificationTest(com.example.model.PrayerType.MAGHRIB) },
            onStopAdhanNotification = { prayerViewModel.stopAdhanService() },
            onToggleIqamah = { prayer, enabled -> prayerViewModel.toggleIqamahAlert(prayer, enabled) },
            onSetIqamahDelay = { prayer, delay -> prayerViewModel.setIqamahDelay(prayer, delay) },
            onSetMinuteOffset = { prayer, offset -> prayerViewModel.setMinuteOffset(prayer, offset) },
            onOpenBatteryGuide = {
                isAdhanSheetOpen = false
                isBackgroundPermissionsOpen = true
            },
            onDismiss = { isAdhanSheetOpen = false }
        )
    }

    // App Update Dialog
    AppUpdateDialog(
        uiState = appUpdateState,
        onUpdateNow = {
            val currentInfo = (appUpdateState as? AppUpdateUiState.UpdateAvailable)?.appUpdateInfo
            AppUpdateHelper.startUpdate(
                context = context,
                appUpdateManager = appUpdateManager,
                appUpdateInfo = currentInfo,
                onDismiss = { appUpdateState = AppUpdateUiState.Idle }
            )
        },
        onOpenPlayStore = {
            AppUpdateHelper.openPlayStore(context)
            appUpdateState = AppUpdateUiState.Idle
        },
        onRetry = {
            AppUpdateHelper.checkForAppUpdate(
                context = context,
                appUpdateManager = appUpdateManager,
                onStateChange = { appUpdateState = it }
            )
        },
        onCompleteFlexibleUpdate = {
            appUpdateManager.completeUpdate()
            appUpdateState = AppUpdateUiState.Idle
        },
        onDismiss = {
            appUpdateState = AppUpdateUiState.Idle
        }
    )

    // Notification Customization Dialog (Frequency, Duration & Master Stop)
    NotificationSettingsDialog(
        isOpen = isNotificationSettingsDialogOpen,
        isNotificationsEnabled = isNotifications,
        notificationFrequency = notificationFrequency,
        notificationTimeWindow = notificationTimeWindow,
        onToggleNotifications = { viewModel.setNotificationsEnabled(it) },
        onSetFrequency = { viewModel.setNotificationFrequency(it) },
        onSetTimeWindow = { viewModel.setNotificationTimeWindow(it) },
        onDismiss = { isNotificationSettingsDialogOpen = false }
    )

    // Legal Info / Privacy Policy Dialog
    if (activeLegalDialogTitle != null && activeLegalDialogContent != null) {
        AlertDialog(
            onDismissRequest = {
                activeLegalDialogTitle = null
                activeLegalDialogContent = null
            },
            title = {
                Text(
                    text = activeLegalDialogTitle ?: "",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = activeLegalDialogContent ?: "",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        activeLegalDialogTitle = null
                        activeLegalDialogContent = null
                    }
                ) {
                    Text("حسناً", fontWeight = FontWeight.Bold, color = SkyBluePrimary)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
private fun ThemeOptionButton(
    title: String,
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
                .padding(vertical = 14.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) PureWhite else SkyBluePrimary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 12.5.sp
                ),
                maxLines = 1,
                textAlign = TextAlign.Center
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

private fun shareAppFromSettings(
    context: Context,
    text: String = "استمع إلى القرآن الكريم من إذاعات العالم عبر تطبيق Quran Radio World ❤️\nhttps://play.google.com/store/apps/details?id=com.aistudio.quranradioworld.live"
) {
    try {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "مشاركة التطبيق")
        context.startActivity(shareIntent)
    } catch (_: Exception) {}
}

private fun openPlayStoreFromSettings(context: Context) {
    val packageName = "com.aistudio.quranradioworld.live"
    try {
        val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(marketIntent)
    } catch (e: Exception) {
        try {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(webIntent)
        } catch (_: Exception) {}
    }
}



@Composable
private fun SettingsLegalRowItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(SkyBluePrimary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = SkyBluePrimary,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

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
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronLeft,
            contentDescription = null,
            tint = NavyMuted.copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp)
        )
    }
}

private fun openDeveloperPage(context: Context) {
    val devUrl = "https://play.google.com/store/apps/dev?id=6839550047456072292"
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(devUrl)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (_: Exception) {}
}

private const val PRIVACY_POLICY_TEXT = """نحن في تطبيق «إذاعات القرآن الكريم» (Quran Radio World) نولي خصوصيتك أهمية قصوى ونلتزم بحمايتها بالكامل:

١. عدم جمع البيانات الشخصية:
التطبيق لا يقوم بجمع أو تخزين أو مشاركة أي بيانات شخصية أو معلومات تعريفية خاصة بالمستخدم إطلاقاً.

٢. التخزين المحلي (Local Storage):
تفضيلاتك الشخصية مثل (المحطات المفضلة، سجل الختمات القرآنية، إعدادات الأذان، مؤقت النوم، واللغة) يتم حفظها محلياً فقط على جهازك باستخدام تقنية التخزين الآمن، ولا يتم رفعها إلى أي خوادم خارجية.

٣. الأذونات المستخدمة والغرض منها:
• الاتصال بالإنترنت: يُستخدم فقط للاتصال بسيرفرات البث الصوتي المباشر لإذاعات القرآن الكريم.
• خدمة التشغيل في الخلفية (Foreground Service): لضمان استمرار الاستماع للقرآن الكريم والأذان بوضوح دون انقطاع عند قفل الشاشة أو استخدام تطبيقات أخرى.
• إذن المنبه الدقيق (Exact Alarm): لجدولة وتنبيهك بمواقيت الصلاة والأذان في موعدها المضبوط.
• إشعار التحكم بالصوت: لعرض بطاقة التحكم في الصوت (تشغيل / إيقاف / المحطة التالية) في ستارة الإشعارات وشاشة القفل وشاشات السيارات.

٤. أمان الاتصال:
يتم تشغيل كافة محطات البث عبر بروتوكولات آمنة ومشفرة لضمان استقرار البث وحمايتك.

٥. خلو التطبيق من الإعلانات:
التطبيق وقف خيري مجاني لوجه الله تعالى وخالٍ تماماً من أي إعلانات تجارية أو أدوات تتبع."""

private const val TERMS_OF_SERVICE_TEXT = """مرحباً بكم في تطبيق «إذاعات القرآن الكريم» (Quran Radio World). باستخدامك للتطبيق فإنك توافق على الشروط التالية:

١. الغرض من التطبيق:
التطبيق مشروع وقفي وخيري يهدف إلى نشر كتاب الله تعالى وتيسير الاستماع لتلاوات القرآن الكريم من مختلف إذاعات العالم الإسلامي على مدار الساعة مجاناً وبدون أي مقابل مادي.

٢. الاستخدام الشخصي وغير التجاري:
يُتاح التطبيق للاستخدام الشخصي المجاني لوجه الله تعالى، ولا يجوز إعادة بيعه أو استغلاله تجارياً بأي شكل من الأشكال.

٣. حقوق البث والمحتوى القرآني:
جميع التلاوات والبرامج الإذاعية المبثة تعود ملكيتها الفكرية وحقوق بثها إلى هيئات وإذاعات القرآن الكريم الرسمية المعنية ووزارات الأوقاف والشؤون الإسلامية في بلدانها. دور التطبيق يقتصر على ربط وتسهيل الوصول للبثوث المباشرة العامة المتاحة للمسلمين.

٤. التحديثات والتطوير:
نعمل باستمرار على تحسين جودة البث وإضافة إذاعات جديدة وتحديث الروابط الاحتياطية تلقائياً لضمان تجربة استماع نقية ومستقرة.

٥. الدعاء والمشاركة:
نسألكم صالح الدعاء لكل من ساهم في تطوير ونشر هذا التطبيق، ويسعدنا دائماً استقبال مقترحاتكم لخدمة كتاب الله عز وجل."""
