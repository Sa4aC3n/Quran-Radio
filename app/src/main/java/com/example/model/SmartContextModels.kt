package com.example.model

import java.util.Calendar

/**
 * Types of spiritual, temporal, and daily smart contexts detected by the engine.
 */
enum class SmartContextType(
    val id: String,
    val badgeAr: String,
    val titleAr: String,
    val descriptionAr: String,
    val preferredStationIds: List<String>,
    val preferredCategories: List<StationCategory>
) {
    FAJR_DAWN(
        id = "fajr_dawn",
        badgeAr = "🌅 إشراقة الفجر والصباح",
        titleAr = "سكينة الصباح والذكر الحكيم",
        descriptionAr = "تلاوات قرآنية ندية وأذكار الصباح لبدء يومك ببركة وسكينة",
        preferredStationIds = listOf(
            "cairo_quran",
            "mp3quran_tarateel",
            "reciter_mohammad_refaat",
            "reciter_mahmoud_khalil_alhussary",
            "saudi_quran"
        ),
        preferredCategories = listOf(
            StationCategory.OFFICIAL,
            StationCategory.ADHKAR,
            StationCategory.QURAN
        )
    ),

    FRIDAY_BLESSINGS(
        id = "friday_blessings",
        badgeAr = "🕌 يوم الجمعة المبارك",
        titleAr = "نفحات وسكينة يوم الجمعة",
        descriptionAr = "سورة الكهف، تلاوات خاشعة، وبث مباشر لخطبة الحرمين الشريفين",
        preferredStationIds = listOf(
            "haram_makkah_live",
            "haram_madinah_live",
            "cairo_quran",
            "reciter_abdulbasit_abdulsamad_mojawwad",
            "saudi_quran"
        ),
        preferredCategories = listOf(
            StationCategory.OFFICIAL,
            StationCategory.QURAN,
            StationCategory.RECITATION
        )
    ),

    DAY_WORK_AND_DHIKR(
        id = "day_work",
        badgeAr = "☀️ بركة النهار وصلاة الضحى",
        titleAr = "مصحف الترتيل وبركة السعي",
        descriptionAr = "تلاوات مرتلة متواصلة وتفسير ميسر يصحبك في عملك ودراستك",
        preferredStationIds = listOf(
            "mp3quran_tarateel",
            "tafseer_alshaarawi",
            "reciter_mishary_alafasy",
            "reciter_saad_alghamdi",
            "kuwait_quran"
        ),
        preferredCategories = listOf(
            StationCategory.TAFSIR,
            StationCategory.RECITER,
            StationCategory.QURAN
        )
    ),

    EVENING_SERENITY(
        id = "evening_serenity",
        badgeAr = "🌇 أصيل العصر والمساء",
        titleAr = "سكينة المساء وختام اليوم",
        descriptionAr = "أذكار حفظ المساء وتلاوات مجودة تريح النفس بعد عناء اليوم",
        preferredStationIds = listOf(
            "saudi_sunnah",
            "cairo_quran",
            "reciter_mustafa_ismail",
            "reciter_mohammad_sedeeq_almenshawi_mojawwad"
        ),
        preferredCategories = listOf(
            StationCategory.ADHKAR,
            StationCategory.SUNNAH,
            StationCategory.RECITATION
        )
    ),

    NIGHT_TRANQUILITY(
        id = "night_tranquility",
        badgeAr = "🌙 سكينة الليل والتهجد",
        titleAr = "تلاوات السحر والتهجد والنوم",
        descriptionAr = "تلاوات هادئة خاشعة والرقية الشرعية تساعد على الاسترخاء والنوم بطمأنينة",
        preferredStationIds = listOf(
            "reciter_maher_almuaiqly",
            "reciter_saud_alshuraim",
            "reciter_yasser_aldosari",
            "mp3quran_tarateel"
        ),
        preferredCategories = listOf(
            StationCategory.RUQYAH,
            StationCategory.RECITATION,
            StationCategory.QURAN
        )
    ),

    RAMADAN_NIGHTS(
        id = "ramadan_nights",
        badgeAr = "✨ ليالي رمضان المباركة",
        titleAr = "صلاة التراويح وروحانية الشهر الفضيل",
        descriptionAr = "بث مباشر لصلاتي التراويح والتهجد وتلاوات عطرة من الحرمين الشريفين",
        preferredStationIds = listOf(
            "haram_makkah_live",
            "haram_madinah_live",
            "cairo_quran",
            "reciter_abdulrahman_alsudaes"
        ),
        preferredCategories = listOf(
            StationCategory.OFFICIAL,
            StationCategory.QURAN
        )
    )
}

/**
 * Encapsulates the resolved smart context recommendation for the current instant.
 */
data class SmartContextRecommendation(
    val contextType: SmartContextType,
    val recommendedStations: List<RadioStation>,
    val selectedIndex: Int = 0,
    val isFriday: Boolean = false,
    val prayerContextName: String? = null
) {
    val currentStation: RadioStation?
        get() = if (recommendedStations.isNotEmpty()) {
            recommendedStations[selectedIndex.coerceIn(0, recommendedStations.size - 1)]
        } else null
}
