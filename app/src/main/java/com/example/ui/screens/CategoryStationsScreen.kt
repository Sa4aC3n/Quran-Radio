package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.PlaybackStatus
import com.example.model.RadioStation
import com.example.model.StationCategory
import com.example.ui.components.StationCard
import com.example.viewmodel.RadioViewModel

@Composable
fun CategoryStationsScreen(
    category: StationCategory,
    viewModel: RadioViewModel,
    onBackClick: () -> Unit,
    onNavigateToStationDetail: (RadioStation) -> Unit,
    modifier: Modifier = Modifier
) {
    val stations by viewModel.allStations.collectAsState()
    val playerState by viewModel.playerState.collectAsState()

    val categoryStations = stations.filter { station ->
        when (category) {
            StationCategory.TAFSIR -> station.category == StationCategory.TAFSIR || station.category == StationCategory.TAFSEER
            StationCategory.TAFSEER -> station.category == StationCategory.TAFSEER || station.category == StationCategory.TAFSIR
            StationCategory.SEERAH_AND_STORIES -> station.category == StationCategory.SEERAH_AND_STORIES || station.category == StationCategory.SEERAH
            StationCategory.SEERAH -> station.category == StationCategory.SEERAH || station.category == StationCategory.SEERAH_AND_STORIES
            StationCategory.RUQYAH -> station.category == StationCategory.RUQYAH || station.category == StationCategory.ROQIAH
            StationCategory.ROQIAH -> station.category == StationCategory.ROQIAH || station.category == StationCategory.RUQYAH
            StationCategory.FATWA -> station.category == StationCategory.FATWA || station.category == StationCategory.FATAWA
            StationCategory.FATAWA -> station.category == StationCategory.FATAWA || station.category == StationCategory.FATWA
            else -> station.category == category
        }
    }

    val title = when (category) {
        StationCategory.OFFICIAL -> "إذاعات رسمية"
        StationCategory.RECITATION -> "تلاوات خاشعة"
        StationCategory.TAFSIR, StationCategory.TAFSEER -> "تفسير القرآن"
        StationCategory.TRANSLATION -> "ترجمات المعاني"
        StationCategory.SUNNAH -> "السنة النبوية"
        StationCategory.SEERAH, StationCategory.SEERAH_AND_STORIES -> "السيرة والقصص"
        StationCategory.FATWA, StationCategory.FATAWA -> "الفتاوى والأحكام"
        StationCategory.ADHKAR -> "أذكار الصباح والمساء"
        StationCategory.RUQYAH, StationCategory.ROQIAH -> "الرقية الشرعية"
        StationCategory.RECITER -> "إذاعات القراء"
        else -> "القسم"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("category_stations_screen")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "رجوع",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "${categoryStations.size} محطة متاحة",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (categoryStations.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Radio,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.65f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "لا توجد محطات متاحة في هذا القسم حاليًا",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categoryStations, key = { it.id }) { station ->
                    val isCurrent = playerState.currentStation?.id == station.id
                    StationCard(
                        station = station,
                        isCurrentStation = isCurrent,
                        playbackStatus = playerState.status,
                        onStationClick = { onNavigateToStationDetail(station) },
                        onPlayToggle = {
                            if (isCurrent && playerState.status == PlaybackStatus.PLAYING) {
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
}
