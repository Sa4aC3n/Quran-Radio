package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.LocalAppLanguage
import com.example.localization.localizedName
import com.example.model.PlaybackStatus
import com.example.model.RadioStation
import com.example.ui.components.StationCard
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SkyBluePrimary
import com.example.viewmodel.RadioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecitersScreen(
    viewModel: RadioViewModel,
    onStationClick: (RadioStation) -> Unit,
    modifier: Modifier = Modifier
) {
    val stations by viewModel.allStations.collectAsState()
    val reciters by viewModel.reciters.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    val currentLanguage = LocalAppLanguage.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("الكل") }

    val filters = listOf("مرتل", "مجود", "أئمة الحرمين", "الكل")
    val recitersById = remember(reciters) { reciters.associateBy { it.id } }

    val filteredStations = stations.filter { station ->
        val query = searchQuery.trim()
        val matchesSearch = query.isBlank() ||
            station.localizedName(currentLanguage).contains(query, ignoreCase = true) ||
            station.name.contains(query, ignoreCase = true) ||
            station.nameAr.contains(query, ignoreCase = true) ||
            station.nameEn.contains(query, ignoreCase = true) ||
            station.country.contains(query, ignoreCase = true) ||
            station.city.contains(query, ignoreCase = true) ||
            station.reciter?.contains(query, ignoreCase = true) == true
        val reciter = station.reciterId?.let { recitersById[it] }
        val matchesFilter = when (selectedFilter) {
            "مرتل" -> reciter?.styleAr?.contains("مرتل", ignoreCase = true) == true
            "مجود" -> reciter?.styleAr?.contains("مجود", ignoreCase = true) == true
            "أئمة الحرمين" -> {
                val placeMatch = station.countryCode.equals("SA", true) &&
                    (station.city.contains("مكة") || station.city.contains("المدينة") ||
                     station.name.contains("الحرم") || station.name.contains("المسجد النبوي"))
                val reciterMatch = reciter?.let {
                    it.styleAr.contains("الحرم") || it.bioAr.contains("الحرم") ||
                        it.bioAr.contains("المسجد النبوي")
                } == true
                placeMatch || reciterMatch
            }
            else -> true
        }
        matchesSearch && matchesFilter
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (currentLanguage == com.example.localization.AppLanguage.AR)
                            "الإذاعات" else "Radio Stations",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .testTag("radio_stations_list"),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth()
                            .shadow(1.dp, RoundedCornerShape(18.dp))
                            .testTag("radio_stations_search_input"),
                        placeholder = {
                            Text(if (currentLanguage == com.example.localization.AppLanguage.AR)
                                "ابحث عن إذاعة أو قارئ أو دولة..." else "Search stations, reciters or countries...")
                        },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SkyBluePrimary) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
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
                    Spacer(Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filters) { filter ->
                            val selected = selectedFilter == filter
                            ElevatedFilterChip(
                                selected = selected,
                                onClick = { selectedFilter = filter },
                                label = {
                                    Text(filter, style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp
                                    ))
                                },
                                shape = RoundedCornerShape(20.dp),
                                colors = FilterChipDefaults.elevatedFilterChipColors(
                                    selectedContainerColor = SkyBluePrimary,
                                    selectedLabelColor = PureWhite,
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                ),
                                border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                elevation = FilterChipDefaults.filterChipElevation(
                                    elevation = if (selected) 2.dp else 0.5.dp
                                )
                            )
                        }
                    }
                    Text(
                        text = if (currentLanguage == com.example.localization.AppLanguage.AR)
                            "عدد الإذاعات: ${filteredStations.size}" else "Stations: ${filteredStations.size}",
                        modifier = Modifier.padding(top = 12.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (filteredStations.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Radio, contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                            Spacer(Modifier.height(12.dp))
                            Text(
                                if (currentLanguage == com.example.localization.AppLanguage.AR)
                                    "لا توجد إذاعات مطابقة للبحث أو التصنيف"
                                else "No stations match this search or filter",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredStations, key = { it.id }) { station ->
                    val isCurrent = playerState.currentStation?.id == station.id
                    Box(Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) {
                        StationCard(
                            station = station,
                            isCurrentStation = isCurrent,
                            playbackStatus = if (isCurrent) playerState.status else PlaybackStatus.IDLE,
                            onStationClick = onStationClick,
                            onPlayToggle = { selected ->
                                if (isCurrent) viewModel.togglePlayPause() else viewModel.playStation(selected)
                            },
                            onToggleFavorite = { viewModel.toggleFavorite(it) }
                        )
                    }
                }
            }
        }
    }
}
