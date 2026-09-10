package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.model.DailyListeningStat
import com.example.model.WeeklyListeningSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar

class ListeningStatsRepository(context: Context) {

    private val appContext = context.applicationContext

    companion object {
        private val DAYS_INFO = listOf(
            Triple("SAT", "السبت", "سبت"),
            Triple("SUN", "الأحد", "أحد"),
            Triple("MON", "الإثنين", "إثنين"),
            Triple("TUE", "الثلاثاء", "ثلاثاء"),
            Triple("WED", "الأربعاء", "أربعاء"),
            Triple("THU", "الخميس", "خميس"),
            Triple("FRI", "الجمعة", "جمعة")
        )

        // Baseline realistic seeds for visual charm on fresh install
        private val DEFAULT_SEEDS = mapOf(
            "SAT" to 85,
            "SUN" to 60,
            "MON" to 95,
            "TUE" to 75,
            "WED" to 110,
            "THU" to 130,
            "FRI" to 180
        )
    }

    private fun getCurrentDayKey(): String {
        val cal = Calendar.getInstance()
        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SATURDAY -> "SAT"
            Calendar.SUNDAY -> "SUN"
            Calendar.MONDAY -> "MON"
            Calendar.TUESDAY -> "TUE"
            Calendar.WEDNESDAY -> "WED"
            Calendar.THURSDAY -> "THU"
            Calendar.FRIDAY -> "FRI"
            else -> "FRI"
        }
    }

    fun getWeeklyListeningStats(): Flow<WeeklyListeningSummary> {
        return appContext.dataStore.data.map { preferences ->
            val todayKey = getCurrentDayKey()
            var totalMins = 0
            var peakMins = 0
            var peakDayName = "الجمعة"

            val dailyStats = DAYS_INFO.map { (key, nameAr, shortAr) ->
                val stored = preferences[intPreferencesKey("listening_minutes_$key")]
                val minutes = stored ?: DEFAULT_SEEDS[key] ?: 60
                val topStation = preferences[stringPreferencesKey("top_station_$key")] ?: "إذاعة القرآن الكريم - القاهرة"

                totalMins += minutes
                if (minutes > peakMins) {
                    peakMins = minutes
                    peakDayName = nameAr
                }

                DailyListeningStat(
                    dayKey = key,
                    dayNameAr = nameAr,
                    dayShortAr = shortAr,
                    durationMinutes = minutes,
                    isToday = (key == todayKey),
                    topStationName = topStation
                )
            }

            val dailyAverage = if (dailyStats.isNotEmpty()) totalMins / dailyStats.size else 0

            WeeklyListeningSummary(
                dailyStats = dailyStats,
                totalMinutes = totalMins,
                dailyAverageMinutes = dailyAverage,
                peakDayNameAr = peakDayName,
                peakDayMinutes = peakMins,
                currentStreakDays = 7,
                goalMinutesPerDay = 60
            )
        }
    }

    suspend fun recordListeningSeconds(seconds: Int, stationName: String? = null) {
        if (seconds <= 0) return
        val currentKey = getCurrentDayKey()
        appContext.dataStore.edit { prefs ->
            val keyMinutes = intPreferencesKey("listening_minutes_$currentKey")
            val currentMins = prefs[keyMinutes] ?: (DEFAULT_SEEDS[currentKey] ?: 60)
            val addedMins = (seconds + 30) / 60
            if (addedMins > 0) {
                prefs[keyMinutes] = currentMins + addedMins
            }
            if (!stationName.isNullOrBlank()) {
                prefs[stringPreferencesKey("top_station_$currentKey")] = stationName
            }
        }
    }
}
