package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PlaybackStatus
import com.example.model.RadioStation
import com.example.model.StationCategory
import com.example.ui.components.AdminStationDialog
import com.example.ui.components.SleepTimerDialog
import com.example.ui.components.StationCard
import com.example.ui.theme.LiveCoral
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SkyBluePrimary
import com.example.viewmodel.RadioViewModel

@Composable
fun HomeScreen(
    viewModel: RadioViewModel,
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

    var isSleepTimerOpen by remember { mutableStateOf(false) }
    var isAdminDialogOpen by remember { mutableStateOf(false) }

    val categories: List<Pair<StationCategory?, String>> = listOf(
        null to "الكل",
        StationCategory.OFFICIAL to "إذاعات رسمية",
        StationCategory.TAFSIR to "تفسير وعلوم القرآن",
        StationCategory.SUNNAH to "السنة النبوية",
        StationCategory.FATWA to "الفتاوى",
        StationCategory.ADHKAR to "الأدعية والأذكار",
        StationCategory.RECITATION to "تلاوات خاشعة",
        StationCategory.RUQYAH to "الرقية الشرعية",
        StationCategory.SEERAH_AND_STORIES to "السيرة والقصص",
        StationCategory.TRANSLATION to "ترجمة معاني القرآن الكريم",
        StationCategory.RECITER to "إذاعات القراء"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("home_screen_list"),
        contentPadding = PaddingValues(bottom = 90.dp)
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
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 28.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "إذاعات القرآن",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 24.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "صوت القرآن من كل مكان",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Sleep Timer Quick Button
                            IconButton(
                                onClick = { isSleepTimerOpen = true },
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                                    .testTag("home_timer_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AvTimer,
                                    contentDescription = "مؤقت النوم",
                                    tint = if (playerState.isSleepTimerActive) LiveCoral else SkyBluePrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Add Station Button (Admin / Custom stream)
                            IconButton(
                                onClick = { isAdminDialogOpen = true },
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                                    .testTag("home_add_station_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "إضافة محطة إذاعية",
                                    tint = SkyBluePrimary,
                                    modifier = Modifier.size(22.dp)
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
                                text = "ابحث عن محطة أو قارئ...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "بحث",
                                tint = SkyBluePrimary
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "مسح البحث",
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
                            text = "محطاتي المفضلة",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                    TextButton(onClick = onNavigateToFavorites) {
                        Text("عرض الكل", color = SkyBluePrimary, fontWeight = FontWeight.Bold)
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
                                        .background(if (isPlaying) SkyBluePrimary else MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Radio,
                                        contentDescription = null,
                                        tint = if (isPlaying) PureWhite else SkyBluePrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = station.name,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 12.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = station.country,
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
                                    text = "حسب الدول",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 13.sp
                                    )
                                )
                                Text(
                                    text = "${countries.size} دولة إسلامية",
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
                                    text = "كبار القراء",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 13.sp
                                    )
                                )
                                Text(
                                    text = "${reciters.size} قارئ معتمد",
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

        // 5. Main Section Header (Live Now)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(LiveCoral)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when {
                        searchQuery.isNotBlank() -> "نتائج البحث (${searchResults.size})"
                        selectedCategory != null -> selectedCategory?.titleAr ?: "الإذاعات"
                        else -> "مباشر الآن"
                    },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
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
                            text = "لا توجد نتائج مطابقة لبحثك",
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
}
