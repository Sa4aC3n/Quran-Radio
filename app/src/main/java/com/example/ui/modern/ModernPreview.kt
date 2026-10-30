package com.example.ui.modern

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.model.PlaybackStatus
import com.example.model.PlayerState
import com.example.model.RadioStation
import com.example.model.StationCategory

/**
 * Android Studio Compose Previews for the Modern UI components.
 * Can be previewed directly inside the IDE Preview pane.
 */
@Preview(name = "Modern Homepage - Complete Preview", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun ModernHomepagePreview() {
    val sampleCairoStation = RadioStation(
        id = "cairo_quran",
        name = "إذاعة القرآن الكريم من القاهرة",
        country = "مصر",
        city = "القاهرة",
        category = StationCategory.OFFICIAL,
        streamUrl = "https://service.webvideocore.net/CL1olYogIrDWvwqiIKK7eCxOS4PStqG9DuEjAr2ZjZQtvS3d4y9r0cvRhvS17SGN/a_7a4vuubc6mo8.m3u8",
        fallbackStreamUrl = "https://stream.radiojar.com/8s5u5tpdtwzuv",
        logoUrl = "https://images.unsplash.com/photo-1591604129939-f1efa4d9f7fa?w=800&q=80",
        description = "البث المباشر الرسمي لإذاعة القرآن الكريم من القاهرة",
        isFeatured = true
    )

    val samplePlayerState = PlayerState(
        currentStation = sampleCairoStation,
        status = PlaybackStatus.PLAYING
    )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ModernColors.ScreenBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                ModernHeaderBar(
                    searchQuery = "",
                    onSearchQueryChange = {},
                    onOpenSettings = {}
                )

                // Hero Cairo Card
                ModernHeroCairoCard(
                    cairoStation = sampleCairoStation,
                    playerState = samplePlayerState,
                    onPlayCairo = {},
                    onPause = {},
                    modifier = Modifier.padding(16.dp)
                )

                // 12 Category Tiles
                ModernCategoryGrid(
                    onSelectCategory = {},
                    onNavigateToCountries = {},
                    onNavigateToReciters = {},
                    onNavigateToQibla = {},
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )

                // Prayer & Adhan Section
                ModernPrayerSection(
                    prayerViewModel = null,
                    onNavigateToPrayerTimes = {},
                    onOpenAdhanSettings = {},
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // Khatmat Section
                ModernKhatmatSection(
                    personalKhatmaViewModel = null,
                    onNavigateToPersonalKhatma = {},
                    onNavigateToGroupKhatma = {},
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}
