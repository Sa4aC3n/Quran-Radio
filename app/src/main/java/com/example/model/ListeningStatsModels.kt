package com.example.model

/**
 * Model representing daily listening stats for Quran audio streams.
 */
data class DailyListeningStat(
    val dayKey: String, // e.g. "SAT", "SUN", "MON", "TUE", "WED", "THU", "FRI"
    val dayNameAr: String, // e.g. "السبت", "الأحد", ...
    val dayShortAr: String, // e.g. "سبت", "أحد", ...
    val durationMinutes: Int,
    val isToday: Boolean = false,
    val topStationName: String? = null
) {
    val formattedDuration: String
        get() {
            val hours = durationMinutes / 60
            val mins = durationMinutes % 60
            return when {
                hours > 0 && mins > 0 -> "$hours س و $mins د"
                hours > 0 -> "$hours س"
                mins > 0 -> "$mins دقيقة"
                else -> "0 د"
            }
        }

    val hoursFloat: Float
        get() = durationMinutes / 60f
}

/**
 * Summary model for weekly Quran listening times.
 */
data class WeeklyListeningSummary(
    val dailyStats: List<DailyListeningStat> = emptyList(),
    val totalMinutes: Int = 0,
    val dailyAverageMinutes: Int = 0,
    val peakDayNameAr: String = "الجمعة",
    val peakDayMinutes: Int = 0,
    val currentStreakDays: Int = 5,
    val goalMinutesPerDay: Int = 60
) {
    val formattedTotalTime: String
        get() {
            val hours = totalMinutes / 60
            val mins = totalMinutes % 60
            return when {
                hours > 0 && mins > 0 -> "$hours ساعة و $mins دقيقة"
                hours > 0 -> "$hours ساعة"
                else -> "$mins دقيقة"
            }
        }

    val formattedDailyAverage: String
        get() {
            val hours = dailyAverageMinutes / 60
            val mins = dailyAverageMinutes % 60
            return when {
                hours > 0 && mins > 0 -> "$hours س $mins د / يوم"
                hours > 0 -> "$hours ساعة / يوم"
                else -> "$mins دقيقة / يوم"
            }
        }
}
