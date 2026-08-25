package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.PlaybackStatus
import com.example.ui.components.StationCard
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SkyBluePrimary
import com.example.viewmodel.RadioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReciterDetailScreen(
    reciterId: String,
    viewModel: RadioViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reciter = viewModel.getReciter(reciterId)
    val reciterStations = viewModel.getStationsForReciter(reciterId)
    val playerState by viewModel.playerState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = reciter?.nameAr ?: "القارئ",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("reciter_detail_list"),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // Reciter Hero Profile Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    SkyBluePrimary.copy(alpha = 0.85f),
                                    MaterialTheme.colorScheme.background
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (reciter?.avatarUrl?.isNotBlank() == true) {
                                AsyncImage(
                                    model = reciter.avatarUrl,
                                    contentDescription = reciter.nameAr,
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = SkyBluePrimary,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = reciter?.nameAr ?: "",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${reciter?.styleAr ?: ""} • ${reciter?.country ?: ""}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = reciter?.bioAr ?: "",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Quick Live Play Button
                        if (reciterStations.isNotEmpty()) {
                            val primaryStation = reciterStations.first()
                            val isPlaying = playerState.currentStation?.id == primaryStation.id && playerState.status == PlaybackStatus.PLAYING
                            Button(
                                onClick = {
                                    if (isPlaying) {
                                        viewModel.togglePlayPause()
                                    } else {
                                        viewModel.playStation(primaryStation)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isPlaying) SkyBluePrimary else MaterialTheme.colorScheme.surface,
                                    contentColor = if (isPlaying) PureWhite else SkyBluePrimary
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isPlaying) "البث المباشر قيد التشغيل الآن" else "بدء الاستماع للبث المباشر (24/7)",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Stations list for this reciter
            item {
                Text(
                    text = "محطات البث المباشر للقارئ (${reciterStations.size})",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            items(reciterStations, key = { it.id }) { station ->
                val isCurrentStation = playerState.currentStation?.id == station.id
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) {
                    StationCard(
                        station = station,
                        isCurrentStation = isCurrentStation,
                        playbackStatus = if (isCurrentStation) playerState.status else PlaybackStatus.IDLE,
                        onStationClick = {
                            if (isCurrentStation) {
                                viewModel.togglePlayPause()
                            } else {
                                viewModel.playStation(station)
                            }
                        },
                        onPlayToggle = {
                            if (isCurrentStation) {
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
