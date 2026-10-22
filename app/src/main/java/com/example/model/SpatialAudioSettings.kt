package com.example.model

enum class AcousticEnvironment(
    val id: String,
    val titleAr: String,
    val subtitleAr: String,
    val iconEmoji: String,
    val roomSize: Float,
    val damping: Float,
    val stereoWidth: Float,
    val defaultWetLevel: Float
) {
    ORIGINAL(
        id = "original",
        titleAr = "الاستوديو النقي",
        subtitleAr = "البث الأصلي المباشر كما هو بدون أي صدى مضاف",
        iconEmoji = "🎙️",
        roomSize = 0.0f,
        damping = 0.0f,
        stereoWidth = 1.0f,
        defaultWetLevel = 0.0f
    ),
    MAKKAH_HARAM(
        id = "makkah",
        titleAr = "رحاب الحرم المكي",
        subtitleAr = "محاكاة الفضاء الفسيح لصحن الطواف وتوسعة الكعبة المشرفة",
        iconEmoji = "🕋",
        roomSize = 0.88f,
        damping = 0.32f,
        stereoWidth = 1.6f,
        defaultWetLevel = 0.32f
    ),
    MADINAH_HARAM(
        id = "madinah",
        titleAr = "أروقة المسجد النبوي",
        subtitleAr = "صدى معماري دافئ ومريح يحاكي جنبات الروضة الشريفة",
        iconEmoji = "🕌",
        roomSize = 0.76f,
        damping = 0.44f,
        stereoWidth = 1.35f,
        defaultWetLevel = 0.26f
    ),
    AL_AQSA(
        id = "aqsa",
        titleAr = "المسجد الأقصى المبارك",
        subtitleAr = "أصداء تاريخية في المصلى القبلي وقبة الصخرة",
        iconEmoji = "✨",
        roomSize = 0.82f,
        damping = 0.38f,
        stereoWidth = 1.45f,
        defaultWetLevel = 0.28f
    );

    companion object {
        fun fromId(id: String): AcousticEnvironment {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: ORIGINAL
        }
    }
}

enum class AmbientSoundscape(
    val id: String,
    val titleAr: String,
    val subtitleAr: String,
    val iconEmoji: String
) {
    NONE(
        id = "none",
        titleAr = "بدون مؤثرات طبيعية",
        subtitleAr = "القرآن الكريم فقط بدون طبقة خلفية",
        iconEmoji = "🚫"
    ),
    GENTLE_RAIN(
        id = "rain",
        titleAr = "صوت المطر الرقيق",
        subtitleAr = "قطرات مطر هادئة تبعث على السكينة والخشوع",
        iconEmoji = "🌧️"
    ),
    DAWN_BREEZE(
        id = "breeze",
        titleAr = "نسيم الفجر العليل",
        subtitleAr = "حفيف رقيق وهواء عليل وقت السحر",
        iconEmoji = "🍃"
    ),
    NIGHT_CALM(
        id = "night",
        titleAr = "هدوء الليل والسكينة",
        subtitleAr = "أجواء تأملية هادئة تساعد على الاسترخاء والنوم",
        iconEmoji = "🌙"
    );

    companion object {
        fun fromId(id: String): AmbientSoundscape {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: NONE
        }
    }
}

data class SpatialAudioConfig(
    val isEnabled: Boolean = false,
    val environment: AcousticEnvironment = AcousticEnvironment.ORIGINAL,
    val wetLevel: Float = 0.30f,
    val ambientSoundscape: AmbientSoundscape = AmbientSoundscape.NONE,
    val ambientVolume: Float = 0.08f
)
