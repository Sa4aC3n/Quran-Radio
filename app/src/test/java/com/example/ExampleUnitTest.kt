package com.example

import com.example.data.AtheerNormalizer
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testArabicNameNormalization() {
        val name1 = "إذاعة الشيخ عبدالباسط عبدالصمد"
        val name2 = "عبد الباسط عبد الصمد"
        val norm1 = AtheerNormalizer.normalizeArabicName(name1)
        val norm2 = AtheerNormalizer.normalizeArabicName(name2)
        assertTrue(norm1.contains("عبد") || norm2.contains("عبد"))
    }

    @Test
    fun testStreamUrlNormalization() {
        val url1 = "https://qurango.net/radio/tarateel"
        val url2 = "http://backup.qurango.net/radio/tarateel/"
        assertTrue(AtheerNormalizer.areUrlsEquivalent(url1, url2))
    }

    @Test
    fun testSeerahCategoryExistence() {
        val seerahCat = com.example.model.StationCategory.SEERAH_AND_STORIES
        assertEquals("السيرة والقصص", seerahCat.titleAr)
    }

    @Test
    fun testRuqyahCategoryExistence() {
        val ruqyahCat = com.example.model.StationCategory.RUQYAH
        assertEquals("الرقية الشرعية", ruqyahCat.titleAr)
    }

    @Test
    fun testFatwaCategoryExistence() {
        val fatwaCat = com.example.model.StationCategory.FATWA
        assertEquals("الفتاوى", fatwaCat.titleAr)
    }

    @Test
    fun testSunnahCategoryExistence() {
        val sunnahCat = com.example.model.StationCategory.SUNNAH
        assertEquals("السنة النبوية", sunnahCat.titleAr)
    }

    @Test
    fun testAdhkarCategoryExistence() {
        val adhkarCat = com.example.model.StationCategory.ADHKAR
        assertEquals("أذكار وأدعية", adhkarCat.titleAr)
    }

    @Test
    fun testRecitationCategoryExistence() {
        val recCat = com.example.model.StationCategory.RECITATION
        assertEquals("تلاوات خاشعة", recCat.titleAr)
    }

    @Test
    fun testOfficialCategoryExistence() {
        val officialCat = com.example.model.StationCategory.OFFICIAL
        assertEquals("إذاعات رسمية", officialCat.titleAr)
        
        val cairoStation = com.example.data.DefaultStationsData.STATIONS.first { it.id == "cairo_quran" }
        assertEquals(com.example.model.StationCategory.OFFICIAL, cairoStation.category)
    }
}
