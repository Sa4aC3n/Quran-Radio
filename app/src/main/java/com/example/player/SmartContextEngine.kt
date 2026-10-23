package com.example.player

import com.example.model.DailyPrayerSchedule
import com.example.model.RadioStation
import com.example.model.SmartContextRecommendation
import com.example.model.SmartContextType
import java.util.Calendar

/**
 * Real-time Smart Context Engine.
 * Analyzes temporal, prayer, and weekly context to recommend the most spiritually
 * fitting authentic Quran broadcasts dynamically without cloud dependency or fake data.
 */
object SmartContextEngine {

    fun determineContextType(
        calendar: Calendar = Calendar.getInstance(),
        isRamadan: Boolean = false,
        dailySchedule: DailyPrayerSchedule? = null
    ): SmartContextType {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val timeInMinutes = hour * 60 + minute

        // 1. Ramadan Night Context (Taraweeh & Tahajjud broadcast hours)
        if (isRamadan && (hour >= 20 || hour <= 3)) {
            return SmartContextType.RAMADAN_NIGHTS
        }

        // 2. Friday Context: From Thursday Maghrib (approx 17:45) until Friday sunset (approx 18:00)
        val isThursdayNight = dayOfWeek == Calendar.THURSDAY && timeInMinutes >= (17 * 60 + 45)
        val isFridayDay = dayOfWeek == Calendar.FRIDAY && timeInMinutes < (18 * 60)
        if (isThursdayNight || isFridayDay) {
            return SmartContextType.FRIDAY_BLESSINGS
        }

        // 3. Temporal time-of-day slots
        return when {
            // Dawn & Early Morning: 3:30 AM -> 9:30 AM
            timeInMinutes in (3 * 60 + 30) until (9 * 60 + 30) -> SmartContextType.FAJR_DAWN

            // Day, Work & Duha: 9:30 AM -> 3:30 PM
            timeInMinutes in (9 * 60 + 30) until (15 * 60 + 30) -> SmartContextType.DAY_WORK_AND_DHIKR

            // Evening & Asr: 3:30 PM -> 8:30 PM
            timeInMinutes in (15 * 60 + 30) until (20 * 60 + 30) -> SmartContextType.EVENING_SERENITY

            // Night, Sleep & Tahajjud: 8:30 PM -> 3:30 AM
            else -> SmartContextType.NIGHT_TRANQUILITY
        }
    }

    fun generateRecommendation(
        allStations: List<RadioStation>,
        calendar: Calendar = Calendar.getInstance(),
        isRamadan: Boolean = false,
        dailySchedule: DailyPrayerSchedule? = null,
        selectedIndex: Int = 0
    ): SmartContextRecommendation {
        val contextType = determineContextType(calendar, isRamadan, dailySchedule)

        val candidateStations = mutableListOf<RadioStation>()
        val seenIds = mutableSetOf<String>()

        // 1. Gather stations matching specific preferred IDs in order
        for (preferredId in contextType.preferredStationIds) {
            val match = allStations.find { it.id.equals(preferredId, ignoreCase = true) }
            if (match != null && seenIds.add(match.id)) {
                candidateStations.add(match)
            }
        }

        // 2. Augment with stations matching preferred categories if needed
        if (candidateStations.size < 4) {
            for (category in contextType.preferredCategories) {
                val matches = allStations.filter { it.category == category && seenIds.add(it.id) }
                for (station in matches) {
                    candidateStations.add(station)
                    if (candidateStations.size >= 6) break
                }
                if (candidateStations.size >= 6) break
            }
        }

        // 3. Fallback: if candidates are still sparse, include verified official stations
        if (candidateStations.isEmpty()) {
            val fallbackMatches = allStations.filter { it.isFeatured || it.isVerified }
            for (st in fallbackMatches) {
                if (seenIds.add(st.id)) candidateStations.add(st)
                if (candidateStations.size >= 4) break
            }
        }

        // Safety fallback to first available stations if any
        if (candidateStations.isEmpty() && allStations.isNotEmpty()) {
            candidateStations.addAll(allStations.take(4))
        }

        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val isFriday = dayOfWeek == Calendar.FRIDAY || (dayOfWeek == Calendar.THURSDAY && calendar.get(Calendar.HOUR_OF_DAY) >= 18)

        val nextPrayerInfo = dailySchedule?.nextPrayer?.let { next ->
            "الصلاة القادمة: ${next.type.arabicName} (${next.formattedTime12h})"
        }

        val safeIndex = if (candidateStations.isNotEmpty()) {
            selectedIndex % candidateStations.size
        } else 0

        return SmartContextRecommendation(
            contextType = contextType,
            recommendedStations = candidateStations,
            selectedIndex = safeIndex,
            isFriday = isFriday,
            prayerContextName = nextPrayerInfo
        )
    }
}
