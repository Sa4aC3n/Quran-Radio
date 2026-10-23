package com.example

import com.example.data.QuranTafsirRepository
import com.example.model.RadioStation
import com.example.model.StationCategory
import com.example.player.AcousticQuranRecognitionEngine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AcousticQuranRecognitionTest {

    @Test
    fun testQuranTafsirRepositoryPreSeededVerses() = runTest {
        // Test Al-Fatihah 1:1
        val fatihah1 = QuranTafsirRepository.getAyah(1, 1)
        assertEquals("بِسۡمِ ٱللَّهِ ٱلرَّحۡمَٰنِ ٱلرَّحِيمِ", fatihah1.uthmaniText)
        assertTrue(fatihah1.tafsirText.contains("ابتدئ قراءتي للقرآن باسم الله"))

        // Test Ayat Al-Kursi 2:255
        val ayatAlKursi = QuranTafsirRepository.getAyah(2, 255)
        assertTrue(ayatAlKursi.uthmaniText.startsWith("ٱللَّهُ لَآ إِلَٰهَ إِلَّا هُوَ ٱلۡحَيُّ ٱلۡقَيُّومُۚ"))
        assertTrue(ayatAlKursi.tafsirText.contains("لا يستحق الألوهية"))

        // Test Al-Ikhlas 112:1
        val ikhlas = QuranTafsirRepository.getAyah(112, 1)
        assertEquals("قُلۡ هُوَ ٱللَّهُ أَحَدٌ", ikhlas.uthmaniText)
        assertTrue(ikhlas.tafsirText.contains("المعبود الحق"))
    }

    @Test
    fun testReciterAndSurahDetectionFromStation() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val engine = AcousticQuranRecognitionEngine(testDispatcher)
        val station = RadioStation(
            id = "test_station_1",
            name = "إذاعة الشيخ عبد الباسط عبد الصمد - سورة مريم",
            country = "مصر",
            countryCode = "EG",
            streamUrl = "https://example.com/stream",
            reciter = "عبد الباسط عبد الصمد",
            category = StationCategory.QURAN
        )

        engine.onStationChanged(station)
        testScheduler.advanceUntilIdle()

        val state = engine.recitationState.value
        assertEquals("الشيخ عبد الباسط عبد الصمد", state.reciterName)
        assertEquals(19, state.surahNumber)
        assertEquals("سورة مريم", state.surahNameAr)
        assertEquals("Maryam", state.surahNameEn)
        assertEquals(1, state.currentAyahNumber)
    }

    @Test
    fun testIcyMetadataRecognitionWithAyah() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val engine = AcousticQuranRecognitionEngine(testDispatcher)
        val station = RadioStation(
            id = "test_station_2",
            name = "إذاعة القرآن الكريم",
            country = "السعودية",
            countryCode = "SA",
            streamUrl = "https://example.com/stream2",
            category = StationCategory.QURAN
        )

        engine.onStationChanged(station)
        testScheduler.advanceUntilIdle()

        // Metadata arrives: "مشاري العفاسي - سورة الكهف - آية 10"
        engine.onMetadataReceived(
            title = "مشاري العفاسي - سورة الكهف - آية 10",
            artist = null,
            station = station
        )
        testScheduler.advanceUntilIdle()

        val state = engine.recitationState.value
        assertEquals("الشيخ مشاري راشد العفاسي", state.reciterName)
        assertEquals(18, state.surahNumber)
        assertEquals("سورة الكهف", state.surahNameAr)
        assertEquals(10, state.currentAyahNumber)
    }

    @Test
    fun testAyahNavigationWithinSurah() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val engine = AcousticQuranRecognitionEngine(testDispatcher)
        val station = RadioStation(
            id = "test_station_fatihah",
            name = "سورة الفاتحة",
            country = "مصر",
            countryCode = "EG",
            streamUrl = "https://example.com/stream3"
        )

        engine.onStationChanged(station)
        testScheduler.advanceUntilIdle()

        assertEquals(1, engine.recitationState.value.currentAyahNumber)

        // Navigate to Ayah 2
        engine.selectAyah(2)
        testScheduler.advanceUntilIdle()

        assertEquals(2, engine.recitationState.value.currentAyahNumber)
        assertEquals("ٱلۡحَمۡدُ لِلَّهِ رَبِّ ٱلۡعَٰلَمِينَ", engine.recitationState.value.uthmaniText)
    }
}
