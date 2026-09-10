package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DailyPrayerSchedule
import com.example.model.PrayerTimeItem
import com.example.model.PrayerType
import com.example.ui.components.AdhanSettingsBottomSheet
import com.example.ui.components.BatteryOptimizationDialog
import com.example.ui.components.LocationPickerDialog
import com.example.ui.components.WeeklyScheduleDialog
import com.example.ui.theme.DeepEmerald
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.SkyBlueDark
import com.example.ui.theme.SkyBluePrimary
import com.example.viewmodel.PrayerViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerTimesScreen(
    viewModel: PrayerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val dailySchedule by viewModel.dailySchedule.collectAsState()
    val cityName by viewModel.cityName.collectAsState()
    val countryName by viewModel.countryName.collectAsState()
    val isLocating by viewModel.isLocating.collectAsState()
    val locationError by viewModel.locationError.collectAsState()
    val selectedSound by viewModel.selectedAdhanSound.collectAsState()
    val previewSoundId by viewModel.adhanAudioPlayer.isPlayingPreview.collectAsState()
    val calcMethod by viewModel.calculationMethod.collectAsState()
    val asrJuristic by viewModel.asrJuristic.collectAsState()
    val alertSettings by viewModel.alertSettings.collectAsState()
    val iqamahSettings by viewModel.iqamahAlertSettings.collectAsState()
    val iqamahDelays by viewModel.iqamahDelays.collectAsState()
    val minuteOffsets by viewModel.minuteOffsets.collectAsState()
    val isAdhanActive by viewModel.isAdhanActive.collectAsState()
    val currentAdhanPrayer by viewModel.currentAdhanPrayer.collectAsState()
    val filteredCities by viewModel.filteredCities.collectAsState()
    val citySearchQuery by viewModel.citySearchQuery.collectAsState()

    var showLocationDialog by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showBatteryGuideDialog by remember { mutableStateOf(false) }
    var showWeeklyDialog by remember { mutableStateOf(false) }

    val settingsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Location Permission Launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.refreshLocation()
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("تم رفض صلاحية الموقع، يمكنك اختيار مدينتك يدوياً.")
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "مواقيت الصلاة والأذان",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "$cityName، $countryName",
                            style = MaterialTheme.typography.bodySmall,
                            color = SkyBluePrimary
                        )
                    }
                },
                actions = {
                    // Location button
                    IconButton(
                        onClick = { showLocationDialog = true },
                        modifier = Modifier.testTag("btn_open_location_picker")
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "تغيير الموقع",
                            tint = SkyBluePrimary
                        )
                    }

                    // Settings button
                    IconButton(
                        onClick = { showSettingsSheet = true },
                        modifier = Modifier.testTag("btn_open_prayer_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "الإعدادات",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Location Bar & Date details
            item {
                LocationAndDateBar(
                    gregorianDate = dailySchedule?.gregorianDateFormatted ?: "",
                    hijriDate = dailySchedule?.hijriDateFormatted ?: "",
                    cityName = cityName,
                    isLocating = isLocating,
                    onChangeLocationClick = { showLocationDialog = true },
                    onRefreshLocationClick = {
                        if (viewModel.hasLocationPermission()) {
                            viewModel.refreshLocation()
                        } else {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    }
                )
            }

            // Active Adhan Playing In-App Banner (Allows instant manual stop)
            if (isAdhanActive) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .testTag("active_adhan_live_banner"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.error)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "🕌", fontSize = 26.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "صوت الأذان يعمل الآن (${currentAdhanPrayer?.arabicName ?: "الصلاة"})",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    Text(
                                        text = "تم إيقاف الإذاعة مؤقتاً وستستأنف تلقائياً بعد الأذان",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { viewModel.stopAdhanService() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("btn_stop_active_adhan")
                            ) {
                                Text(
                                    text = "إيقاف الأذان",
                                    color = MaterialTheme.colorScheme.onError,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // Next Prayer Hero Card with live animated countdown
            item {
                dailySchedule?.let { schedule ->
                    NextPrayerHeroCard(
                        schedule = schedule,
                        onOpenSettings = { showSettingsSheet = true }
                    )
                }
            }

            // Quick Tools Action Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. مواقيت الأسبوع Pill
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 1.dp,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .clickable { showWeeklyDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "مواقيت الأسبوع",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = SkyBluePrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // 2. صوت الأذان Pill
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 1.dp,
                        modifier = Modifier
                            .weight(1.1f)
                            .clip(RoundedCornerShape(24.dp))
                            .clickable { showSettingsSheet = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = selectedSound.titleAr.take(11) + if (selectedSound.titleAr.length > 11) ".." else "",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = SkyBluePrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // 3. تحسين البطارية Pill
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 1.dp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .clickable { showBatteryGuideDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.BatteryAlert,
                                contentDescription = "دليل تحسين البطارية",
                                tint = GoldAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Section Title
            item {
                Text(
                    text = "صلوات اليوم",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // List of prayers for today
            dailySchedule?.let { schedule ->
                items(schedule.allPrayers, key = { it.type.id }) { prayer ->
                    val isAlertEnabled = alertSettings[prayer.type] ?: true
                    val isIqamahEnabled = iqamahSettings[prayer.type] ?: false
                    val iqamahDelay = iqamahDelays[prayer.type] ?: 15

                    PrayerRowCard(
                        prayer = prayer,
                        isAlertEnabled = isAlertEnabled,
                        isIqamahEnabled = isIqamahEnabled,
                        iqamahDelay = iqamahDelay,
                        onToggleAlert = { enabled ->
                            viewModel.togglePrayerAlert(prayer.type, enabled)
                        }
                    )
                }
            }

            // Calculation Method Info Footer
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                        .clickable { showSettingsSheet = true }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "طريقة الحساب: ${calcMethod.titleAr}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "مذهب العصر: ${asrJuristic.titleAr} • انقر للتعديل",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = SkyBluePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    // Dialogs & Sheets
    if (showLocationDialog) {
        LocationPickerDialog(
            cities = filteredCities,
            searchQuery = citySearchQuery,
            currentCityName = cityName,
            isLocating = isLocating,
            onSearchQueryChange = { viewModel.citySearchQuery.value = it },
            onSelectCity = { city ->
                viewModel.selectPresetCity(city)
                showLocationDialog = false
            },
            onUseGpsLocation = {
                if (viewModel.hasLocationPermission()) {
                    viewModel.refreshLocation()
                } else {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            },
            onDismiss = { showLocationDialog = false }
        )
    }

    if (showSettingsSheet) {
        AdhanSettingsBottomSheet(
            sheetState = settingsSheetState,
            currentMethod = calcMethod,
            currentAsrJuristic = asrJuristic,
            selectedAdhanSound = selectedSound,
            previewPlayingSoundId = previewSoundId,
            minuteOffsets = minuteOffsets,
            iqamahSettings = iqamahSettings,
            iqamahDelays = iqamahDelays,
            onSelectMethod = { viewModel.setCalculationMethod(it) },
            onSelectAsrJuristic = { viewModel.setAsrJuristic(it) },
            onSelectAdhanSound = { viewModel.setAdhanSound(it) },
            onPlayPreviewSound = { viewModel.adhanAudioPlayer.playPreview(it) },
            onStopPreviewSound = { viewModel.adhanAudioPlayer.stopPreview() },
            onTestAdhanNotification = { viewModel.triggerAdhanNotificationTest() },
            onStopAdhanNotification = { viewModel.stopAdhanService() },
            onToggleIqamah = { type, enabled -> viewModel.toggleIqamahAlert(type, enabled) },
            onSetIqamahDelay = { type, min -> viewModel.setIqamahDelay(type, min) },
            onSetMinuteOffset = { type, min -> viewModel.setMinuteOffset(type, min) },
            onOpenBatteryGuide = {
                showSettingsSheet = false
                showBatteryGuideDialog = true
            },
            onDismiss = { showSettingsSheet = false }
        )
    }

    if (showBatteryGuideDialog) {
        com.example.ui.components.BackgroundPermissionExplanationDialog(
            isOpen = true,
            onDismiss = { showBatteryGuideDialog = false }
        )
    }

    if (showWeeklyDialog) {
        WeeklyScheduleDialog(
            schedules = viewModel.getWeeklySchedule(7),
            onDismiss = { showWeeklyDialog = false }
        )
    }
}

@Composable
private fun LocationAndDateBar(
    gregorianDate: String,
    hijriDate: String,
    cityName: String,
    isLocating: Boolean,
    onChangeLocationClick: () -> Unit,
    onRefreshLocationClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = hijriDate,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = SkyBluePrimary
                )
                Text(
                    text = gregorianDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.clickable { onChangeLocationClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = SkyBluePrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = cityName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onRefreshLocationClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    if (isLocating) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "تحديث الموقع",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NextPrayerHeroCard(
    schedule: DailyPrayerSchedule,
    onOpenSettings: () -> Unit
) {
    val next = schedule.nextPrayer
    val millis = schedule.millisUntilNextPrayer

    val totalSec = millis / 1000
    val hours = totalSec / 3600
    val minutes = (totalSec % 3600) / 60
    val seconds = totalSec % 60

    val countdownFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds)

    val animatedProgress by animateFloatAsState(
        targetValue = schedule.progressToNextPrayer,
        animationSpec = tween(600),
        label = "hero_progress"
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Color(0xFF263238)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hero_next_prayer_card")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF1E293B),
                            Color(0xFF0F172A)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.08f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = getPrayerIcon(next.type),
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "الصلاة القادمة: صلاة ${next.type.arabicName}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Huge live countdown
                Text(
                    text = countdownFormatted,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 2.sp
                )

                Text(
                    text = "موعد الأذان: ${next.formattedTime12h}",
                    color = Color.White.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Smooth day progress bar
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "التقدم نحو موعد الصلاة",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "${(animatedProgress * 100).toInt()}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = GoldAccent,
                        trackColor = Color.White.copy(alpha = 0.25f),
                        strokeCap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

@Composable
private fun PrayerRowCard(
    prayer: PrayerTimeItem,
    isAlertEnabled: Boolean,
    isIqamahEnabled: Boolean,
    iqamahDelay: Int,
    onToggleAlert: (Boolean) -> Unit
) {
    val isHighlighted = prayer.isNext || prayer.isCurrent

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (prayer.isNext) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            } else if (prayer.isCurrent) {
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("prayer_card_${prayer.type.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = if (isHighlighted) SkyBluePrimary else MaterialTheme.colorScheme.surface,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getPrayerIcon(prayer.type),
                    contentDescription = null,
                    tint = if (isHighlighted) Color.White else SkyBluePrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Prayer Name and status badge
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = prayer.type.arabicName,
                        fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (prayer.isNext) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SkyBluePrimary
                        ) {
                            Text(
                                text = "القادمة",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else if (prayer.isCurrent) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = GoldAccent
                        ) {
                            Text(
                                text = "الحالية",
                                color = Color.Black,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                if (isIqamahEnabled && prayer.type != PrayerType.SUNRISE) {
                    Text(
                        text = "الإقامة: +$iqamahDelay دقيقة",
                        style = MaterialTheme.typography.labelSmall,
                        color = SkyBluePrimary
                    )
                }
            }

            // Formatted Time (12H)
            Text(
                text = prayer.formattedTime12h,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = if (isHighlighted) SkyBluePrimary else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Notification Bell Toggle
            if (prayer.type != PrayerType.SUNRISE) {
                IconButton(
                    onClick = { onToggleAlert(!isAlertEnabled) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isAlertEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                        contentDescription = if (isAlertEnabled) "تعطيل التنبيه" else "تفعيل التنبيه",
                        tint = if (isAlertEnabled) SkyBluePrimary else MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(36.dp))
            }
        }
    }
}

private fun getPrayerIcon(type: PrayerType): ImageVector {
    return when (type) {
        PrayerType.FAJR -> Icons.Default.WbTwilight
        PrayerType.SUNRISE -> Icons.Default.WbSunny
        PrayerType.DHUHR -> Icons.Default.WbSunny
        PrayerType.ASR -> Icons.Default.WbTwilight
        PrayerType.MAGHRIB -> Icons.Default.DarkMode
        PrayerType.ISHA -> Icons.Default.DarkMode
    }
}
