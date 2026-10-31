package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.localization.AppLanguage
import com.example.localization.LanguageSelectionDialog
import com.example.localization.LocalAppLanguage
import com.example.localization.StationTranslator
import com.example.localization.StringKey
import com.example.localization.appString
import com.example.localization.localizedName
import com.example.localization.localizedTitle
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.outlined.LiveTv
import com.example.model.PlaybackStatus
import com.example.model.RadioStation
import com.example.model.StationCategory
import com.example.ui.components.AdminStationDialog
import com.example.ui.components.HajjBanner
import com.example.ui.components.HaramainLiveCard
import com.example.ui.components.HaramLiveSettingsDialog
import com.example.ui.components.IslamicSeasonSettingsDialog
import com.example.ui.components.RamadanBanner
import com.example.ui.components.SleepTimerDialog
import com.example.ui.components.SmartContextCard
import com.example.ui.components.StationCard
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LiveCoral
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SkyBluePrimary
import com.example.viewmodel.PrayerViewModel
import com.example.viewmodel.RadioViewModel

@Composable
fun HomeScreen(
    viewModel: RadioViewModel,
    prayerViewModel: PrayerViewModel? = null,
    onNavigateToStationDetail: (RadioStation) -> Unit,
    onNavigateToCountries: () -> Unit,
    onNavigateToReciters: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToPrayer: () -> Unit = {},
    onNavigateToQibla: () -> Unit = {},
    onNavigateToGroupKhatma: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onOpenCategory: (StationCategory) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val searchResults by viewModel.searchResults.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    val currentLanguage by viewModel.appLanguage.collectAsState()
    val seasonState = prayerViewModel?.islamicSeasonState?.collectAsState()?.value
    val haramPreferences by viewModel.haramLivePreferences.collectAsState()
    val smartContextRecommendation by viewModel.smartContextRecommendation.collectAsState()

    var isSleepTimerOpen by remember { mutableStateOf(false) }
    var isAdminDialogOpen by remember { mutableStateOf(false) }
    var isLanguageDialogOpen by remember { mutableStateOf(false) }
    var isSeasonSettingsOpen by remember { mutableStateOf(false) }
    var isHaramDialogOpen by remember { mutableStateOf(false) }
    var isHaramainCardVisible by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("home_screen_list"),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // 1. Top Header Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                SkyBluePrimary.copy(alpha = 0.85f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
                    .statusBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 28.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = appString(StringKey.APP_TITLE),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 24.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = appString(StringKey.APP_SUBTITLE),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            IconButton(
                                onClick = onNavigateToSettings,
                                modifier = Modifier.size(32.dp).testTag("home_settings_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "الإعدادات",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(19.dp)
                                )
                            }

                            // 1. Language Selector Quick Button (Transparent, opacity 0.7 on press)
                            val langInteraction = remember { MutableInteractionSource() }
                            val isLangPressed by langInteraction.collectIsPressedAsState()
                            val langAlpha by animateFloatAsState(
                                targetValue = if (isLangPressed) 0.7f else 1.0f,
                                label = "lang_btn_alpha"
                            )
                            IconButton(
                                onClick = { isLanguageDialogOpen = true },
                                interactionSource = langInteraction,
                                modifier = Modifier
                                    .size(32.dp)
                                    .alpha(langAlpha)
                                    .testTag("home_language_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Translate,
                                    contentDescription = "تغيير اللغة",
                                    tint = SkyBluePrimary,
                                    modifier = Modifier.size(19.dp)
                                )
                            }

                            // 2. Islamic Season Quick Mode Button (Transparent, opacity 0.7 on press)
                            if (seasonState != null) {
                                val seasonInteraction = remember { MutableInteractionSource() }
                                val isSeasonPressed by seasonInteraction.collectIsPressedAsState()
                                val seasonAlpha by animateFloatAsState(
                                    targetValue = if (isSeasonPressed) 0.7f else 1.0f,
                                    label = "season_btn_alpha"
                                )
                                IconButton(
                                    onClick = { isSeasonSettingsOpen = true },
                                    interactionSource = seasonInteraction,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .alpha(seasonAlpha)
                                        .testTag("home_season_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "المناسبات الإسلامية",
                                        tint = SkyBluePrimary,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }

                            // 3. Haram Live Quick Button
                            val haramInteraction = remember { MutableInteractionSource() }
                            val isHaramPressed by haramInteraction.collectIsPressedAsState()
                            val haramAlpha by animateFloatAsState(
                                targetValue = if (isHaramPressed) 0.7f else 1.0f,
                                label = "haram_btn_alpha"
                            )
                            IconButton(
                                onClick = { isHaramDialogOpen = true },
                                interactionSource = haramInteraction,
                                modifier = Modifier
                                    .size(32.dp)
                                    .alpha(haramAlpha)
                                    .testTag("home_haram_btn")
                            ) {
                                Text(
                                    text = "🕋",
                                    fontSize = 16.sp
                                )
                            }

                            // 4. Sleep Timer Quick Button (Transparent, opacity 0.7 on press)
                            val timerInteraction = remember { MutableInteractionSource() }
                            val isTimerPressed by timerInteraction.collectIsPressedAsState()
                            val timerAlpha by animateFloatAsState(
                                targetValue = if (isTimerPressed) 0.7f else 1.0f,
                                label = "timer_btn_alpha"
                            )
                            IconButton(
                                onClick = { isSleepTimerOpen = true },
                                interactionSource = timerInteraction,
                                modifier = Modifier
                                    .size(32.dp)
                                    .alpha(timerAlpha)
                                    .testTag("home_timer_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AvTimer,
                                    contentDescription = appString(StringKey.HOME_SLEEP_TIMER),
                                    tint = if (playerState.isSleepTimerActive) LiveCoral else SkyBluePrimary,
                                    modifier = Modifier.size(19.dp)
                                )
                            }

                            // 5. Haramain Live TV Toggle Button (Transparent, opacity 0.7 on press)
                            val tvInteraction = remember { MutableInteractionSource() }
                            val isTvPressed by tvInteraction.collectIsPressedAsState()
                            val tvAlpha by animateFloatAsState(
                                targetValue = if (isTvPressed) 0.7f else 1.0f,
                                label = "tv_btn_alpha"
                            )
                            IconButton(
                                onClick = { isHaramainCardVisible = !isHaramainCardVisible },
                                interactionSource = tvInteraction,
                                modifier = Modifier
                                    .size(32.dp)
                                    .alpha(tvAlpha)
                                    .testTag("home_haramain_tv_toggle_btn")
                            ) {
                                Icon(
                                    imageVector = if (isHaramainCardVisible) Icons.Filled.LiveTv else Icons.Outlined.LiveTv,
                                    contentDescription = "البث المباشر للحرمين",
                                    tint = if (isHaramainCardVisible) GoldAccent else SkyBluePrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }


                }
            }
        }

        // Modern homepage sections: wired to the existing application actions.
        if (selectedCategory == null) {
            val cairoStation = searchResults.firstOrNull { it.id == "cairo_quran" }
            item {
                CairoRadioHeroCard(
                    isPlaying = cairoStation != null &&
                        playerState.currentStation?.id == cairoStation.id &&
                        playerState.status == PlaybackStatus.PLAYING,
                    onPlay = {
                        if (cairoStation != null) {
                            if (playerState.currentStation?.id == cairoStation.id) {
                                viewModel.togglePlayPause()
                            } else {
                                viewModel.playStation(cairoStation)
                            }
                        }
                    }
                )
            }
            item {
                HomeCategoryGrid(
                    onCountries = onNavigateToCountries,
                    onReciters = onNavigateToReciters,
                    onQibla = onNavigateToQibla,
                    onOpenCategory = onOpenCategory
                )
            }
            item {
                HomePrayerCards(
                    onPrayerTimes = onNavigateToPrayer,
                    onAdhanSettings = onNavigateToPrayer
                )
            }
            item {
                HomeKhatmaCards(onOpenKhatma = onNavigateToGroupKhatma)
            }
            item {
                HomeQuickShortcuts(
                    onLanguage = { isLanguageDialogOpen = true },
                    onSeason = { isSeasonSettingsOpen = true },
                    onHaram = { isHaramDialogOpen = true },
                    onSleepTimer = { isSleepTimerOpen = true },
                    onHaramain = { isHaramainCardVisible = !isHaramainCardVisible },
                    onFavorites = onNavigateToFavorites
                )
            }
        }

        // 1.4 Haramain Live TV Card (Animated Visibility - Hidden by default)
        item {
            AnimatedVisibility(
                visible = isHaramainCardVisible,
                enter = fadeIn(tween(300)) + expandVertically(tween(350)),
                exit = fadeOut(tween(250)) + shrinkVertically(tween(300))
            ) {
                HaramainLiveCard(
                    onStopRadio = {
                        if (playerState.status == PlaybackStatus.PLAYING) {
                            viewModel.pause()
                        }
                    }
                )
            }
        }

        // 1.5 Islamic Seasonal Smart Banners (Ramadan / Hajj)
        if (selectedCategory == null) {
            if (seasonState != null) {
                if (seasonState.isRamadanActive && seasonState.ramadanBannerEnabled) {
                    item {
                        RamadanBanner(
                            seasonState = seasonState,
                            onPlayStation = { station ->
                                viewModel.playStation(station)
                            },
                            onSelectStreamSource = { source ->
                                prayerViewModel?.setSeasonStreamSource(source)
                            },
                            onToggleKhatmahJuz = { juz ->
                                prayerViewModel?.toggleKhatmahJuz(juz)
                            },
                            onResetKhatmah = {
                                prayerViewModel?.resetKhatmah()
                            }
                        )
                    }
                } else if (seasonState.isHajjActive && seasonState.hajjBannerEnabled) {
                    item {
                        HajjBanner(
                            seasonState = seasonState,
                            onPlayStation = { station ->
                                viewModel.playStation(station)
                            }
                        )
                    }
                }
            }
        }

        // 1.6 Smart Context Recommendation Card (Adapts to Dawn, Day, Friday, Evening, Night)
        if (selectedCategory == null) {
            val smartContext = smartContextRecommendation
            if (smartContext != null && smartContext.currentStation != null) {
                item {
                    SmartContextCard(
                        recommendation = smartContext,
                        currentPlayingStation = playerState.currentStation,
                        isPlaying = playerState.status == PlaybackStatus.PLAYING,
                        onPlayStation = { station ->
                            viewModel.playStation(station)
                        },
                        onTogglePlayPause = {
                            viewModel.togglePlayPause()
                        },
                        onNextRecommendation = {
                            viewModel.nextSmartContextRecommendation()
                        }
                    )
                }
            }
        }




    }

    // Language Selector Dialog
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

    // Admin Add Station Dialog
    AdminStationDialog(
        isOpen = isAdminDialogOpen,
        onSave = { viewModel.addCustomStation(it) },
        onDismiss = { isAdminDialogOpen = false }
    )

    // Islamic Season Settings Dialog
    if (isSeasonSettingsOpen && seasonState != null && prayerViewModel != null) {
        IslamicSeasonSettingsDialog(
            seasonState = seasonState,
            onSetSeasonMode = { prayerViewModel.setSeasonModeOverride(it) },
            onSetHijriOffset = { delta ->
                val current = seasonState.hijriDetails.day // We can calculate new offset
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
}



/** Featured Cairo station card. The stream action uses the real RadioViewModel. */
@Composable
private fun CairoRadioHeroCard(
    isPlaying: Boolean,
    onPlay: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, Color(0xFFCAE7F5)),
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(216.dp)
        ) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(com.example.R.drawable.cairo_nile_hero),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier.matchParentSize().background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, Color(0x22FFFFFF), Color(0xBFFFFFFF), Color(0xF5FFFFFF))
                    )
                )
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.82f)
                    .align(Alignment.CenterEnd)
                    .padding(start = 10.dp, end = 16.dp, top = 15.dp, bottom = 15.dp),
                horizontalAlignment = Alignment.End
            ) {
                Surface(
                    color = Color(0xFF0878B8),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(
                        text = "الإذاعة الرئيسية",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
                Spacer(Modifier.height(9.dp))
                Text(
                    text = "إذاعة القرآن الكريم من القاهرة",
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF124A78),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Right,
                    maxLines = 1,
                    softWrap = false
                )
                Text(
                    text = "الهيئة العامة للإذاعة والتلفزيون",
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF527B98),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Right
                )
                Spacer(Modifier.height(13.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Right side: station identity and live status.
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (isPlaying) "يعمل الآن" else "بث مباشر",
                                color = if (isPlaying) Color(0xFF16845B) else Color(0xFF124A78),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "استمع إلى إذاعة القرآن",
                                color = Color(0xFF527B98),
                                fontSize = 9.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0x22168BC8)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mosque,
                                contentDescription = "إذاعة القرآن الكريم",
                                tint = Color(0xFF168BC8),
                                modifier = Modifier.size(23.dp)
                            )
                        }
                    }
                    // Left side: prominent modern play control.
                    Surface(
                        onClick = onPlay,
                        shape = CircleShape,
                        color = Color(0xFF168BC8),
                        modifier = Modifier.size(52.dp),
                        shadowElevation = 5.dp
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF35B4E8), Color(0xFF0878B8))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل إذاعة القاهرة",
                                tint = Color.White,
                                modifier = Modifier.size(29.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class HomeCategoryAction(
    val title: String,
    val icon: ImageVector,
    val action: () -> Unit
)

@Composable
private fun HomeCategoryGrid(
    onCountries: () -> Unit,
    onReciters: () -> Unit,
    onQibla: () -> Unit,
    onOpenCategory: (StationCategory) -> Unit
) {
    val actions = listOf(
        HomeCategoryAction("إذاعات القراء", Icons.Default.Mic, onReciters),
        HomeCategoryAction("إذاعات الدول", Icons.Default.Public, onCountries),
        HomeCategoryAction("إذاعات رسمية", Icons.Default.Radio) { onOpenCategory(StationCategory.OFFICIAL) },
        HomeCategoryAction("تلاوات خاشعة", Icons.Default.AutoStories) { onOpenCategory(StationCategory.RECITATION) },
        HomeCategoryAction("تفسير القرآن", Icons.Default.MenuBook) { onOpenCategory(StationCategory.TAFSIR) },
        HomeCategoryAction("ترجمات المعاني", Icons.Default.Translate) { onOpenCategory(StationCategory.TRANSLATION) },
        HomeCategoryAction("السنة النبوية", Icons.Default.AccountBalance) { onOpenCategory(StationCategory.SUNNAH) },
        HomeCategoryAction("السيرة والقصص", Icons.Default.Mosque) { onOpenCategory(StationCategory.SEERAH_AND_STORIES) },
        HomeCategoryAction("الفتاوى والأحكام", Icons.Default.Balance) { onOpenCategory(StationCategory.FATWA) },
        HomeCategoryAction("أذكار الصباح والمساء", Icons.Default.WbSunny) { onOpenCategory(StationCategory.ADHKAR) },
        HomeCategoryAction("الرقية الشرعية", Icons.Default.Spa) { onOpenCategory(StationCategory.RUQYAH) },
        HomeCategoryAction("القبلة", Icons.Default.Explore, onQibla)
    )
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Text(
            text = "اكتشف الأقسام",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(bottom = 2.dp)
        )
        actions.chunked(4).forEach { rowActions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowActions.forEach { action ->
                    Surface(
                        onClick = action.action,
                        modifier = Modifier.weight(1f).height(106.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        shadowElevation = 1.dp
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp, vertical = 9.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier.size(39.dp).clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = action.icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Text(
                                text = action.title,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                maxLines = 2,
                                lineHeight = 13.sp
                            )
                        }
                    }
                }
                repeat(4 - rowActions.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun HomePrayerCards(
    onPrayerTimes: () -> Unit,
    onAdhanSettings: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Text(
            text = "ركن الصلاة",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        HomeFeatureActionCard(
            title = "مواقيت الصلاة",
            subtitle = "تابع مواقيت الصلاة حسب موقعك",
            icon = Icons.Default.AccessTime,
            dark = true,
            onClick = onPrayerTimes,
            modifier = Modifier.weight(1f)
        )
        HomeFeatureActionCard(
            title = "منبه الأذان",
            subtitle = "إعدادات التنبيه ومتابعة الصلاة",
            icon = Icons.Default.NotificationsActive,
            dark = false,
            onClick = onAdhanSettings,
            modifier = Modifier.weight(1f)
        )
        }
    }
}

@Composable
private fun HomeFeatureActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    dark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val foreground = if (dark) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer
    Surface(
        onClick = onClick,
        modifier = modifier.height(132.dp),
        shape = RoundedCornerShape(17.dp),
        color = if (dark) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Icon(icon, contentDescription = null, tint = foreground, modifier = Modifier.size(30.dp))
            Text(title, color = foreground, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Text(subtitle, color = foreground.copy(alpha = .82f), fontSize = 9.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center, maxLines = 2)
        }
    }
}

@Composable
private fun HomeKhatmaCards(onOpenKhatma: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "الختمات",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            HomeKhatmaAction("ختمة جماعية", "شارك القراءة مع الآخرين", Icons.Default.Groups, onOpenKhatma, Modifier.weight(1f))
            HomeKhatmaAction("ختمة خاصة", "تابع ختمتك الشخصية", Icons.Default.MenuBook, onOpenKhatma, Modifier.weight(1f))
        }
    }
}

@Composable
private fun HomeKhatmaAction(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(112.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, Color(0xFFD8EAF4)),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF1684BE), modifier = Modifier.size(25.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(title, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp, maxLines = 2)
            }
        }
    }
}

@Composable
private fun HomeQuickShortcuts(
    onLanguage: () -> Unit,
    onSeason: () -> Unit,
    onHaram: () -> Unit,
    onSleepTimer: () -> Unit,
    onHaramain: () -> Unit,
    onFavorites: () -> Unit
) {
    val shortcuts = listOf(
        Triple("اللغة", Icons.Default.Translate, onLanguage),
        Triple("رمضان والحج", Icons.Default.AutoAwesome, onSeason),
        Triple("بث الحرم", Icons.Default.Mosque, onHaram),
        Triple("مؤقت النوم", Icons.Default.AvTimer, onSleepTimer),
        Triple("مكة والمدينة", Icons.Default.LiveTv, onHaramain),
        Triple("المفضلة", Icons.Default.Favorite, onFavorites)
    )
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = "اختصارات سريعة",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            shortcuts.forEach { (title, icon, action) ->
                Column(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = action)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, Color(0xFFD8EAF4), RoundedCornerShape(12.dp))
                        .padding(horizontal = 3.dp, vertical = 9.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(icon, contentDescription = title, tint = Color(0xFF1684BE), modifier = Modifier.size(22.dp))
                    Spacer(Modifier.height(5.dp))
                    Text(
                        title,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        maxLines = 2,
                        lineHeight = 11.sp
                    )
                }
            }
        }
    }
}

