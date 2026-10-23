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
    modifier: Modifier = Modifier
) {
    val searchResults by viewModel.searchResults.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val favoriteStations by viewModel.favoriteStations.collectAsState()
    val countries by viewModel.countries.collectAsState()
    val reciters by viewModel.reciters.collectAsState()
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

    val categories: List<Pair<StationCategory?, String>> = listOf(
        null to appString(StringKey.CAT_ALL),
        StationCategory.OFFICIAL to appString(StringKey.CAT_OFFICIAL),
        StationCategory.TAFSIR to appString(StringKey.CAT_TAFSIR),
        StationCategory.SUNNAH to appString(StringKey.CAT_SUNNAH),
        StationCategory.FATWA to appString(StringKey.CAT_FATWA),
        StationCategory.ADHKAR to appString(StringKey.CAT_ADHKAR),
        StationCategory.RECITATION to appString(StringKey.CAT_RECITATION),
        StationCategory.RUQYAH to appString(StringKey.CAT_RUQYAH),
        StationCategory.SEERAH_AND_STORIES to appString(StringKey.CAT_SEERAH_AND_STORIES),
        StationCategory.TRANSLATION to appString(StringKey.CAT_TRANSLATION),
        StationCategory.RECITER to appString(StringKey.CAT_RECITER)
    )

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

                    Spacer(modifier = Modifier.height(18.dp))

                    // Search Field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(18.dp))
                            .testTag("home_search_bar"),
                        placeholder = {
                            Text(
                                text = appString(StringKey.HOME_SEARCH_PLACEHOLDER),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = appString(StringKey.SEARCH),
                                tint = SkyBluePrimary
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = appString(StringKey.SEARCH_CLEAR),
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = SkyBluePrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            cursorColor = SkyBluePrimary
                        ),
                        singleLine = true
                    )
                }
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
        if (searchQuery.isBlank() && selectedCategory == null) {
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
        if (searchQuery.isBlank() && selectedCategory == null) {
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

        // 2. Category Filter Chips (Pills)
        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { (category, label) ->
                    val isSelected = selectedCategory == category
                    ElevatedFilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectCategory(category) },
                        label = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp
                                )
                            )
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = FilterChipDefaults.elevatedFilterChipColors(
                            selectedContainerColor = SkyBluePrimary,
                            selectedLabelColor = PureWhite,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        elevation = FilterChipDefaults.filterChipElevation(elevation = if (isSelected) 3.dp else 0.5.dp)
                    )
                }
            }
        }

        // 3. Quick Favorites Carousel
        if (searchQuery.isBlank() && selectedCategory == null && favoriteStations.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(LiveCoral)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = appString(StringKey.HOME_FAVORITES_TITLE),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                    TextButton(onClick = onNavigateToFavorites) {
                        Text(
                            text = appString(StringKey.HOME_VIEW_ALL),
                            color = SkyBluePrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(favoriteStations) { station ->
                        val isPlaying = playerState.currentStation?.id == station.id && playerState.status == PlaybackStatus.PLAYING
                        Surface(
                            modifier = Modifier
                                .width(165.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .clickable {
                                    if (playerState.currentStation?.id == station.id) {
                                        viewModel.togglePlayPause()
                                    } else {
                                        viewModel.playStation(station)
                                    }
                                }
                                .testTag("favorite_card_${station.id}"),
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, if (isPlaying) SkyBluePrimary else MaterialTheme.colorScheme.outline),
                            shadowElevation = if (isPlaying) 3.dp else 1.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (isPlaying) SkyBluePrimary else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (station.logoUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = station.logoUrl,
                                            contentDescription = station.name,
                                            modifier = Modifier
                                                .size(28.dp)
                                                .padding(1.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Radio,
                                            contentDescription = null,
                                            tint = if (isPlaying) PureWhite else SkyBluePrimary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = station.localizedName(currentLanguage),
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 12.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = if (station.reciter != null) {
                                        StationTranslator.getReciterName(station.reciterId, station.reciter, "", currentLanguage)
                                    } else {
                                        StationTranslator.getCountryName(station.countryCode ?: "", station.country, "", currentLanguage)
                                    },
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
                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        // 4. Quick Nav Discovery Cards (Countries & Reciters)
        if (searchQuery.isBlank() && selectedCategory == null) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Countries Card
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onNavigateToCountries() }
                            .testTag("home_quick_countries"),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Public,
                                    contentDescription = null,
                                    tint = SkyBluePrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = appString(StringKey.HOME_BY_COUNTRIES),
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 13.sp
                                    )
                                )
                                Text(
                                    text = appString(StringKey.HOME_ISLAMIC_COUNTRIES_COUNT, countries.size),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }

                    // Reciters Card
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onNavigateToReciters() }
                            .testTag("home_quick_reciters"),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = SkyBluePrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = appString(StringKey.HOME_TOP_RECITERS),
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 13.sp
                                    )
                                )
                                Text(
                                    text = appString(StringKey.HOME_AUTHENTIC_RECITERS_COUNT, reciters.size),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        // 5. Main Section Header (Live Now) & Add Station Button
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(LiveCoral)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val headerTitle = when {
                        searchQuery.isNotBlank() -> appString(StringKey.SEARCH_RESULTS_COUNT, searchResults.size)
                        selectedCategory != null -> selectedCategory?.localizedTitle(currentLanguage) ?: appString(StringKey.CAT_ALL)
                        else -> appString(StringKey.HOME_LIVE_NOW)
                    }
                    Text(
                        text = headerTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                // Add Station pill button
                Surface(
                    onClick = { isAdminDialogOpen = true },
                    shape = RoundedCornerShape(12.dp),
                    color = SkyBluePrimary.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, SkyBluePrimary.copy(alpha = 0.3f)),
                    modifier = Modifier.testTag("home_add_station_section_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = SkyBluePrimary,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = appString(StringKey.ADMIN_ADD_TITLE),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = SkyBluePrimary,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }

        // 6. Stations List
        if (searchResults.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Radio,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = appString(StringKey.SEARCH_NO_RESULTS),
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            }
        } else {
            items(
                items = searchResults,
                key = { it.id }
            ) { station ->
                val isCurrent = playerState.currentStation?.id == station.id
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) {
                    StationCard(
                        station = station,
                        isCurrentStation = isCurrent,
                        playbackStatus = playerState.status,
                        onStationClick = { onNavigateToStationDetail(station) },
                        onPlayToggle = {
                            if (isCurrent) {
                                viewModel.togglePlayPause()
                            } else {
                                viewModel.playStation(station)
                            }
                        },
                        onToggleFavorite = { viewModel.toggleFavorite(it) }
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

