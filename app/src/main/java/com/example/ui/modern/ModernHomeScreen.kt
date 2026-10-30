package com.example.ui.modern

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RadioStation
import com.example.model.StationCategory
import com.example.ui.components.HaramainLiveCard
import com.example.ui.components.StationCard
import com.example.viewmodel.PersonalKhatmaViewModel
import com.example.viewmodel.PrayerViewModel
import com.example.viewmodel.RadioViewModel

/**
 * Modern Islamic Home Screen Architecture.
 * Built strictly inside the isolated side package (com.example.ui.modern)
 * without touching or impacting the original production screens.
 */
@Composable
fun ModernHomeScreen(
    viewModel: RadioViewModel,
    prayerViewModel: PrayerViewModel? = null,
    personalKhatmaViewModel: PersonalKhatmaViewModel? = null,
    onNavigateToStationDetail: (RadioStation) -> Unit,
    onNavigateToCountries: () -> Unit,
    onNavigateToReciters: () -> Unit,
    onNavigateToQibla: () -> Unit = {},
    onNavigateToPrayerTimes: () -> Unit = {},
    onNavigateToPersonalKhatma: () -> Unit = {},
    onNavigateToGroupKhatma: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val searchResults by viewModel.searchResults.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    val favoriteStations by viewModel.favoriteStations.collectAsState()
    val isDark = isSystemInDarkTheme()

    // Find verified Cairo station directly from repo
    val cairoStation = remember(searchResults) {
        searchResults.find { it.id == "cairo_quran" }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(if (isDark) ModernColors.DarkScreenBackground else ModernColors.ScreenBackground)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                // 1. Header with Brand, Search Bar, and Settings
                item {
                    ModernHeaderBar(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { viewModel.searchQuery.value = it },
                        onOpenSettings = onOpenSettings
                    )
                }

                // 2. Filter Active Indicator (When user selects a category or searches)
                if (selectedCategory != null || searchQuery.isNotBlank()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (searchQuery.isNotBlank()) "نتائج البحث عن: \"$searchQuery\""
                                else "قسم: ${selectedCategory?.name ?: ""}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) ModernColors.DarkTextPrimary else ModernColors.TextPrimary
                            )

                            ElevatedFilterChip(
                                selected = true,
                                onClick = {
                                    viewModel.searchQuery.value = ""
                                    viewModel.selectedCategory.value = null
                                },
                                label = { Text("إلغاء التصفية", fontSize = 11.sp) },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "إلغاء",
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                },
                                colors = FilterChipDefaults.elevatedFilterChipColors(
                                    selectedContainerColor = ModernColors.TileIconTint,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    // Matching Station Cards
                    items(searchResults, key = { it.id }) { station ->
                        StationCard(
                            station = station,
                            isCurrentStation = playerState.currentStation?.id == station.id,
                            playbackStatus = playerState.status,
                            onStationClick = { onNavigateToStationDetail(station) },
                            onPlayToggle = { viewModel.playStation(station) },
                            onToggleFavorite = { viewModel.toggleFavorite(station.id) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    // Default State: Full Modern Homepage Grid matching mockup

                    // 3. Featured Hero Card: Cairo Quran Radio
                    item {
                        ModernHeroCairoCard(
                            cairoStation = cairoStation,
                            playerState = playerState,
                            onPlayCairo = {
                                if (cairoStation != null) {
                                    viewModel.playStation(cairoStation)
                                }
                            },
                            onPause = { viewModel.pause() },
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 10.dp)
                        )
                    }

                    // 4. Live Haramain Camera Card (Makkah & Madinah Live)
                    item {
                        HaramainLiveCard(
                            modifier = Modifier.padding(bottom = 6.dp),
                            onStopRadio = { viewModel.stop() }
                        )
                    }

                    // 5. 12 Core Category Tiles (3x4 Grid)
                    item {
                        ModernCategoryGrid(
                            onSelectCategory = { cat -> viewModel.selectedCategory.value = cat },
                            onNavigateToCountries = onNavigateToCountries,
                            onNavigateToReciters = onNavigateToReciters,
                            onNavigateToQibla = onNavigateToQibla,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    // 6. Prayer Times & Adhan Alarm Section
                    item {
                        ModernPrayerSection(
                            prayerViewModel = prayerViewModel,
                            onNavigateToPrayerTimes = onNavigateToPrayerTimes,
                            onOpenAdhanSettings = onOpenSettings,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    // 7. Khatmat Section
                    item {
                        ModernKhatmatSection(
                            personalKhatmaViewModel = personalKhatmaViewModel,
                            onNavigateToPersonalKhatma = onNavigateToPersonalKhatma,
                            onNavigateToGroupKhatma = onNavigateToGroupKhatma,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
