package com.example.model

/**
 * Supported Islamic Seasons detected by astronomical Hijri calendar.
 */
enum class IslamicSeason(val id: String, val titleAr: String, val titleEn: String) {
    NORMAL("normal", "الأيام العادية", "Normal Days"),
    RAMADAN("ramadan", "شهر رمضان المبارك", "Holy Month of Ramadan"),
    HAJJ("hajj", "موسم الحج المبارك", "Hajj Season"),
    ARAFAH("arafah", "يوم عرفة المبارك", "Day of Arafah"),
    EID_FITR("eid_fitr", "عيد الفطر المبارك", "Eid Al-Fitr"),
    EID_ADHA("eid_adha", "عيد الأضحى المبارك", "Eid Al-Adha");

    companion object {
        fun fromId(id: String): IslamicSeason =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: NORMAL
    }
}

/**
 * Override mode for previewing or testing seasons.
 */
enum class SeasonModeOverride(val id: String, val titleAr: String, val titleEn: String) {
    AUTO("auto", "تلقائي (حسب التقويم الهجري)", "Automatic (By Hijri Calendar)"),
    FORCE_RAMADAN("force_ramadan", "معاينة وضع رمضان 🌙", "Preview Ramadan Mode"),
    FORCE_HAJJ("force_hajj", "معاينة موسم الحج 🕋", "Preview Hajj Season"),
    FORCE_ARAFAH("force_arafah", "معاينة يوم عرفة المبارك 🤲", "Preview Day of Arafah"),
    FORCE_EID("force_eid", "معاينة أجواء العيد المبارك 🎉", "Preview Eid Mubarak"),
    DISABLED("disabled", "إيقاف الميزة (الوضع العادي دائماً)", "Disabled (Normal Mode Only)");

    companion object {
        fun fromId(id: String): SeasonModeOverride =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: AUTO
    }
}

/**
 * Taraweeh & Tahajjud Live stream status.
 */
enum class TaraweehLiveStatus(val titleAr: String) {
    UPCOMING("قريباً موعد صلاة التراويح"),
    LIVE_NOW("🔴 بث مباشر صلاة التراويح والتهجد من الحرمين الشريفين"),
    COMPLETED("انتهت صلاة التراويح لليوم")
}

/**
 * Preferred stream source for Ramadan / Hajj.
 */
enum class SeasonStreamSource(val id: String, val titleAr: String, val titleEn: String) {
    MAKKAH("makkah", "الحرم المكي الشريف (مكة المكرمة)", "Makkah Holy Mosque"),
    MADINAH("madinah", "الحرم النبوي الشريف (المدينة المنورة)", "Madinah Prophet Mosque"),
    CAIRO("cairo", "إذاعة القرآن الكريم من القاهرة", "Cairo Quran Radio");

    companion object {
        fun fromId(id: String): SeasonStreamSource =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: MAKKAH
    }
}

/**
 * Hijri Date detailed component.
 */
data class HijriDateDetails(
    val day: Int,
    val month: Int,
    val year: Int,
    val monthNameAr: String,
    val monthNameEn: String,
    val formattedAr: String
)

/**
 * Daily Hajj ritual model for days 8-13 Dhul-Hijjah.
 */
data class HajjRitualInfo(
    val dayNumber: Int,
    val dayNameAr: String,
    val locationAr: String,
    val summaryAr: String,
    val detailsAr: String,
    val keyDuaAr: String,
    val talbiyahTextAr: String,
    val isArafah: Boolean = false,
    val isEidDay: Boolean = false
)

/**
 * Daily Ramadan authentic Dua & Dhikr.
 */
data class DailyRamadanDua(
    val day: Int,
    val title: String,
    val arabicText: String,
    val translation: String,
    val reference: String,
    val virtue: String
)

/**
 * Ramadan Khatmah state (30 Juz tracking).
 */
data class RamadanKhatmahState(
    val completedJuzSet: Set<Int> = emptySet(),
    val totalJuz: Int = 30
) {
    val completedCount: Int get() = completedJuzSet.size
    val progressPercentage: Float get() = (completedCount.toFloat() / totalJuz.toFloat()).coerceIn(0f, 1f)
    val nextJuzToRead: Int get() = (1..30).firstOrNull { !completedJuzSet.contains(it) } ?: 30
}

/**
 * Complete UI State for Islamic Seasons.
 */
data class IslamicSeasonState(
    val season: IslamicSeason = IslamicSeason.NORMAL,
    val overrideMode: SeasonModeOverride = SeasonModeOverride.AUTO,
    val hijriDetails: HijriDateDetails = HijriDateDetails(1, 9, 1447, "رمضان", "Ramadan", "1 رمضان 1447 هـ"),
    val isRamadanActive: Boolean = false,
    val isHajjActive: Boolean = false,
    val isArafahActive: Boolean = false,
    val isEidActive: Boolean = false,
    val ramadanBannerEnabled: Boolean = true,
    val hajjBannerEnabled: Boolean = true,
    val taraweehLiveStatus: TaraweehLiveStatus = TaraweehLiveStatus.UPCOMING,
    val millisUntilTaraweeh: Long = 0L,
    val millisUntilIftar: Long = 0L,
    val millisUntilImsak: Long = 0L,
    val millisUntilLastThird: Long = 0L,
    val iftarTimeFormatted: String = "",
    val imsakTimeFormatted: String = "",
    val lastThirdTimeFormatted: String = "",
    val currentHajjRitual: HajjRitualInfo? = null,
    val dailyDua: DailyRamadanDua? = null,
    val khatmahState: RamadanKhatmahState = RamadanKhatmahState(),
    val selectedStreamSource: SeasonStreamSource = SeasonStreamSource.MAKKAH,
    val liveSeasonStation: RadioStation? = null
)
