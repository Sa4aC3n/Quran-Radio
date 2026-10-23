package com.example

import com.example.model.RadioStation
import com.example.model.StationCategory
import com.example.model.StationStream
import com.example.player.MultiStreamFailoverEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MultiStreamFailoverEngineTest {

    @Test
    fun testResolveCandidatesAggregatesAllUrlsWithDeduplication() {
        val engine = MultiStreamFailoverEngine()
        val station = RadioStation(
            id = "test_station",
            name = "إذاعة تجريبية",
            country = "السعودية",
            countryCode = "SA",
            category = StationCategory.OFFICIAL,
            streamUrl = "https://primary.example.com/live",
            fallbackStreamUrl = "https://backup1.example.com/live",
            streams = listOf(
                StationStream(id = "s2", stationId = "test_station", url = "https://stream2.example.com/live", priority = 2),
                StationStream(id = "s1", stationId = "test_station", url = "https://primary.example.com/live", priority = 1) // duplicate of primary
            )
        )

        val candidates = engine.resolveCandidates(station)

        assertTrue(candidates.contains("https://primary.example.com/live"))
        assertTrue(candidates.contains("https://backup1.example.com/live"))
        assertTrue(candidates.contains("https://stream2.example.com/live"))
        assertEquals(3, candidates.size) // No duplicates
    }

    @Test
    fun testOfficialMirrorsInjectedForMajorStations() {
        val engine = MultiStreamFailoverEngine()
        val cairo = RadioStation(
            id = "cairo_quran",
            name = "إذاعة القرآن الكريم من القاهرة",
            country = "مصر",
            countryCode = "EG",
            category = StationCategory.OFFICIAL,
            streamUrl = "https://stream.radiojar.com/8s5u8tpdtwzuv"
        )

        val candidates = engine.resolveCandidates(cairo)

        // Must include official mirror network
        assertTrue(candidates.size >= 3)
        assertTrue(candidates.any { it.contains("radiojar") })
        assertTrue(candidates.any { it.contains("zeno.fm") || it.contains("qurango") })
    }

    @Test
    fun testFailedStreamIsDemotedToTail() {
        val engine = MultiStreamFailoverEngine()
        val primaryUrl = "https://primary.example.com/live"
        val backupUrl = "https://backup.example.com/live"

        val station = RadioStation(
            id = "test_failover",
            name = "إذاعة اختبار",
            country = "مصر",
            countryCode = "EG",
            category = StationCategory.QURAN,
            streamUrl = primaryUrl,
            fallbackStreamUrl = backupUrl
        )

        // Initially, primary is first
        val initial = engine.resolveCandidates(station)
        assertEquals(primaryUrl, initial.first())

        // Mark primary as failed
        engine.recordFailure(primaryUrl)
        assertFalse(engine.isUrlHealthy(primaryUrl))

        // After failure, healthy backup must be promoted to the first position
        val afterFail = engine.resolveCandidates(station)
        assertEquals(backupUrl, afterFail.first())
        assertEquals(primaryUrl, afterFail.last())

        // Mark primary as recovered
        engine.recordSuccess(primaryUrl)
        assertTrue(engine.isUrlHealthy(primaryUrl))
        val afterRecovery = engine.resolveCandidates(station)
        assertEquals(primaryUrl, afterRecovery.first())
    }
}
