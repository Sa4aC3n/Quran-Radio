package com.example

import com.example.model.RadioStation
import com.example.model.SmartContextType
import com.example.model.StationCategory
import com.example.player.SmartContextEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class SmartContextEngineTest {

    private fun createCalendar(dayOfWeek: Int, hour: Int, minute: Int): Calendar {
        return Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, dayOfWeek)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }
    }

    private val sampleStations = listOf(
        RadioStation(
            id = "cairo_quran",
            name = "إذاعة القرآن الكريم من القاهرة",
            country = "مصر",
            countryCode = "EG",
            category = StationCategory.OFFICIAL,
            streamUrl = "https://stream.radiojar.com/8s5u8tpdtwzuv"
        ),
        RadioStation(
            id = "mp3quran_tarateel",
            name = "تراتيل قصار السور",
            country = "السعودية",
            countryCode = "SA",
            category = StationCategory.QURAN,
            streamUrl = "https://qurango.net/radio/tarateel"
        ),
        RadioStation(
            id = "haram_makkah_live",
            name = "بث مباشر الحرم المكي",
            country = "السعودية",
            countryCode = "SA",
            category = StationCategory.OFFICIAL,
            streamUrl = "https://stream.makkah.org"
        ),
        RadioStation(
            id = "tafseer_alshaarawi",
            name = "تفسير خواطر الشعراوي",
            country = "مصر",
            countryCode = "EG",
            category = StationCategory.TAFSIR,
            streamUrl = "https://qurango.net/radio/shaarawi"
        ),
        RadioStation(
            id = "reciter_maher_almuaiqly",
            name = "الشيخ ماهر المعيقلي",
            country = "السعودية",
            countryCode = "SA",
            category = StationCategory.RECITER,
            streamUrl = "https://qurango.net/radio/maher"
        )
    )

    @Test
    fun testDawnContextDetection() {
        // Monday 5:30 AM -> FAJR_DAWN
        val cal = createCalendar(Calendar.MONDAY, 5, 30)
        val context = SmartContextEngine.determineContextType(calendar = cal, isRamadan = false)
        assertEquals(SmartContextType.FAJR_DAWN, context)
        assertEquals("🌅 إشراقة الفجر والصباح", context.badgeAr)
    }

    @Test
    fun testDayWorkContextDetection() {
        // Tuesday 11:30 AM -> DAY_WORK_AND_DHIKR
        val cal = createCalendar(Calendar.TUESDAY, 11, 30)
        val context = SmartContextEngine.determineContextType(calendar = cal, isRamadan = false)
        assertEquals(SmartContextType.DAY_WORK_AND_DHIKR, context)
        assertEquals("☀️ بركة النهار وصلاة الضحى", context.badgeAr)
    }

    @Test
    fun testFridayBlessingsContextDetection() {
        // Friday 10:00 AM -> FRIDAY_BLESSINGS
        val fridayCal = createCalendar(Calendar.FRIDAY, 10, 0)
        val context = SmartContextEngine.determineContextType(calendar = fridayCal, isRamadan = false)
        assertEquals(SmartContextType.FRIDAY_BLESSINGS, context)
        assertEquals("🕌 يوم الجمعة المبارك", context.badgeAr)

        // Thursday 8:00 PM (Night of Friday) -> FRIDAY_BLESSINGS
        val thursNightCal = createCalendar(Calendar.THURSDAY, 20, 0)
        val thursContext = SmartContextEngine.determineContextType(calendar = thursNightCal, isRamadan = false)
        assertEquals(SmartContextType.FRIDAY_BLESSINGS, thursContext)
    }

    @Test
    fun testEveningSerenityContextDetection() {
        // Wednesday 5:00 PM -> EVENING_SERENITY
        val cal = createCalendar(Calendar.WEDNESDAY, 17, 0)
        val context = SmartContextEngine.determineContextType(calendar = cal, isRamadan = false)
        assertEquals(SmartContextType.EVENING_SERENITY, context)
        assertEquals("🌇 أصيل العصر والمساء", context.badgeAr)
    }

    @Test
    fun testNightTranquilityContextDetection() {
        // Sunday 11:30 PM -> NIGHT_TRANQUILITY
        val cal = createCalendar(Calendar.SUNDAY, 23, 30)
        val context = SmartContextEngine.determineContextType(calendar = cal, isRamadan = false)
        assertEquals(SmartContextType.NIGHT_TRANQUILITY, context)
        assertEquals("🌙 سكينة الليل والتهجد", context.badgeAr)
    }

    @Test
    fun testRamadanNightsContextDetection() {
        // Ramadan 9:30 PM -> RAMADAN_NIGHTS
        val cal = createCalendar(Calendar.MONDAY, 21, 30)
        val context = SmartContextEngine.determineContextType(calendar = cal, isRamadan = true)
        assertEquals(SmartContextType.RAMADAN_NIGHTS, context)
        assertEquals("✨ ليالي رمضان المباركة", context.badgeAr)
    }

    @Test
    fun testGenerateRecommendationProducesRealStationsAndCycles() {
        val dawnCal = createCalendar(Calendar.MONDAY, 6, 0)
        val rec = SmartContextEngine.generateRecommendation(
            allStations = sampleStations,
            calendar = dawnCal,
            selectedIndex = 0
        )

        assertNotNull(rec)
        assertEquals(SmartContextType.FAJR_DAWN, rec.contextType)
        assertTrue(rec.recommendedStations.isNotEmpty())

        val firstStation = rec.currentStation
        assertNotNull(firstStation)
        assertEquals("cairo_quran", firstStation!!.id)

        // Test Cycling / Next Recommendation
        val nextRec = SmartContextEngine.generateRecommendation(
            allStations = sampleStations,
            calendar = dawnCal,
            selectedIndex = 1
        )
        assertNotNull(nextRec.currentStation)
        if (rec.recommendedStations.size > 1) {
            assertTrue(nextRec.currentStation!!.id != firstStation.id || rec.recommendedStations.size == 1)
        }
    }
}
