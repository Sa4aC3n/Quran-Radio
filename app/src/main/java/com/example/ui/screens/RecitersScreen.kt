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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.LocalAppLanguage
import com.example.localization.StationTranslator
import com.example.localization.StringKey
import com.example.localization.appString
import com.example.localization.localizedName
import com.example.model.PlaybackStatus
import com.example.model.Reciter
import com.example.ui.components.ReciterCard
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SkyBluePrimary
import com.example.viewmodel.RadioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecitersScreen(
    viewModel: RadioViewModel,
    onReciterClick: (Reciter) -> Unit,
    modifier: Modifier = Modifier
) {
    val reciters by viewModel.reciters.collectAsState()
    val allStations by viewModel.allStations.collectAsState()
    val playerState by viewModel.playerState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedStyleFilter by remember { mutableStateOf<String?>(null) }

    val styles = listOf(
        null to appString(StringKey.CAT_ALL),
        "مرتل" to appString(StringKey.RECITERS_STYLE_MURATTAL),
        "مجود" to appString(StringKey.RECITERS_STYLE_MUJAWWAD),
        "أئمة الحرمين" to appString(StringKey.RECITERS_STYLE_HARAMAIN)
    )

    val currentLang = LocalAppLanguage.current

    val filteredReciters = reciters.filter { reciter ->
        val localizedName = reciter.localizedName(currentLang)
        val matchesSearch = searchQuery.isBlank() ||
                localizedName.contains(searchQuery.trim(), ignoreCase = true) ||
                reciter.nameAr.contains(searchQuery.trim(), ignoreCase = true) ||
                reciter.nameEn.contains(searchQuery.trim(), ignoreCase = true)

        val matchesStyle = when (selectedStyleFilter) {
            null -> true
            "أئمة الحرمين" -> reciter.styleAr.contains("الحرم") || 
                    reciter.styleAr.contains("الحرمين") || 
                    reciter.bioAr.contains("الحرم") || 
                    reciter.bioAr.contains("المسجد الحرام") || 
                    reciter.bioAr.contains("المسجد النبوي")
            else -> reciter.styleAr.contains(selectedStyleFilter ?: "")
        }
        matchesSearch && matchesStyle
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = appString(StringKey.RECITERS_TITLE),
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .testTag("reciters_list"),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Search Bar & Filter Header
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(1.dp, RoundedCornerShape(18.dp))
                            .testTag("reciters_search_input"),
                        placeholder = {
                            Text(
                                text = appString(StringKey.RECITERS_SEARCH_PLACEHOLDER),
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
                                IconButton(onClick = { searchQuery = "" }) {
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

                    Spacer(modifier = Modifier.height(10.dp))

                    // Style Filter Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(styles) { (style, label) ->
                            val isSelected = selectedStyleFilter == style
                            ElevatedFilterChip(
                                selected = isSelected,
                                onClick = { selectedStyleFilter = style },
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
                                elevation = FilterChipDefaults.filterChipElevation(elevation = if (isSelected) 2.dp else 0.5.dp)
                            )
                        }
                    }
                }
            }

            // Reciters Items
            items(filteredReciters, key = { it.id }) { reciter ->
                val reciterStation = allStations.find { s ->
                    s.reciterId == reciter.id ||
                            (s.reciter != null && s.reciter.contains(reciter.nameAr, ignoreCase = true)) ||
                            s.name.contains(reciter.nameAr, ignoreCase = true)
                }
                val isCurrentReciter = playerState.currentStation != null && (
                        playerState.currentStation?.reciterId == reciter.id ||
                                (reciterStation != null && playerState.currentStation?.id == reciterStation.id) ||
                                (playerState.currentStation?.reciter?.contains(reciter.nameAr, ignoreCase = true) == true) ||
                                (playerState.currentStation?.name?.contains(reciter.nameAr, ignoreCase = true) == true)
                        )
                val playbackStatus = if (isCurrentReciter) playerState.status else PlaybackStatus.IDLE

                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) {
                    ReciterCard(
                        reciter = reciter,
                        isCurrentReciter = isCurrentReciter,
                        playbackStatus = playbackStatus,
                        onReciterClick = { onReciterClick(reciter) },
                        onPlayReciterLive = {
                            if (isCurrentReciter) {
                                viewModel.togglePlayPause()
                            } else if (reciterStation != null) {
                                viewModel.playStation(reciterStation)
                            }
                        }
                    )
                }
            }
        }
    }
}
