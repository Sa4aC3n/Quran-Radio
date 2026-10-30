package com.example.player

import com.example.model.RadioStation
import java.util.concurrent.ConcurrentHashMap

/**
 * High-Reliability Zero-Downtime Multi-Stream Failover Engine.
 * Resolves, health-checks, and prioritizes primary, fallback, and mirror stream URLs
 * to ensure non-stop live audio broadcasting even if primary servers fail.
 */
class MultiStreamFailoverEngine {

    // Track recently failed stream URLs (URL -> timestamp in ms)
    private val failedUrlTimestamps = ConcurrentHashMap<String, Long>()

    // Cooldown duration before re-attempting a known failed URL
    private val failureCooldownMs = 3 * 60 * 1000L // 3 minutes

    /**
     * Curated verified mirror networks for high-profile stations to guarantee zero downtime.
     */
    private val officialMirrors = mapOf(
        "cairo_quran" to listOf(
            "https://stream.radiojar.com/8s5u5tpdtwzuv"
        ),
        "haram_makkah_live" to listOf(
            "https://qurango.net/radio/makkah",
            "https://stream.zeno.fm/f3wvbbqmdg8uv",
            "https://live.mp3quran.net:9998/"
        ),
        "haram_madinah_live" to listOf(
            "https://qurango.net/radio/medina",
            "https://stream.zeno.fm/k2k3v3qmdg8uv",
            "https://live.mp3quran.net:9999/"
        ),
        "saudi_quran" to listOf(
            "https://qurango.net/radio/saudi",
            "https://stream.zeno.fm/2v2807pmdg8uv"
        ),
        "mp3quran_tarateel" to listOf(
            "https://qurango.net/radio/tarateel",
            "http://backup.qurango.net/radio/tarateel/"
        )
    )

    /**
     * Resolves all possible stream candidates for a station, ordered by health and priority.
     */
    fun resolveCandidates(
        station: RadioStation,
        allStations: List<RadioStation> = emptyList()
    ): List<String> {
        val candidates = LinkedHashSet<String>()

        // 1. Primary configured URL
        if (station.streamUrl.isNotBlank()) {
            candidates.add(cleanUrl(station.streamUrl))
        }

        // 2. Multi-stream list from station metadata (sorted by priority)
        if (station.streams.isNotEmpty()) {
            val sortedStreams = station.streams
                .filter { it.isActive && it.url.isNotBlank() }
                .sortedBy { it.priority }
            for (s in sortedStreams) {
                candidates.add(cleanUrl(s.url))
            }
        }

        // 3. Dedicated fallbackStreamUrl
        if (!station.fallbackStreamUrl.isNullOrBlank()) {
            candidates.add(cleanUrl(station.fallbackStreamUrl))
        }

        // 4. Official high-reliability mirror network if known
        val mirrors = officialMirrors[station.id]
        if (mirrors != null) {
            for (mirrorUrl in mirrors) {
                candidates.add(cleanUrl(mirrorUrl))
            }
        }

        // 5. Cross-Station Failover for Reciters: If it's a specific reciter, find other stations for that reciter
        if (candidates.size < 3 && allStations.isNotEmpty() && (!station.reciterId.isNullOrBlank() || !station.reciter.isNullOrBlank())) {
            val siblingReciterStations = allStations.filter { other ->
                other.id != station.id && (
                    (station.reciterId != null && other.reciterId == station.reciterId) ||
                    (station.reciter != null && other.reciter.equals(station.reciter, ignoreCase = true))
                )
            }
            for (sibling in siblingReciterStations) {
                if (sibling.streamUrl.isNotBlank()) {
                    candidates.add(cleanUrl(sibling.streamUrl))
                }
                if (candidates.size >= 5) break
            }
        }

        // 6. Sort candidates: Healthy streams first, recently failed streams at the tail
        val now = System.currentTimeMillis()
        val sortedList = candidates.toList().sortedBy { url ->
            val failedAt = failedUrlTimestamps[url]
            if (failedAt != null && (now - failedAt) < failureCooldownMs) {
                1 // Demote failed URL to the end
            } else {
                0 // Healthy / untried stream gets priority
            }
        }

        return if (sortedList.isNotEmpty()) sortedList else listOf(station.streamUrl)
    }

    fun recordFailure(url: String) {
        val cleaned = cleanUrl(url)
        failedUrlTimestamps[cleaned] = System.currentTimeMillis()
    }

    fun recordSuccess(url: String) {
        val cleaned = cleanUrl(url)
        failedUrlTimestamps.remove(cleaned)
    }

    fun isUrlHealthy(url: String): Boolean {
        val cleaned = cleanUrl(url)
        val failedAt = failedUrlTimestamps[cleaned] ?: return true
        return (System.currentTimeMillis() - failedAt) > failureCooldownMs
    }

    private fun cleanUrl(rawUrl: String): String {
        return rawUrl.trim()
    }
}
