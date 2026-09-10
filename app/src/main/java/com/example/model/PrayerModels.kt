package com.example.model

import com.example.R
import java.util.Calendar

/**
 * The specific prayer and solar milestones.
 */
enum class PrayerType(
    val id: String,
    val arabicName: String,
    val englishName: String,
    val isFardh: Boolean = true
) {
    FAJR("fajr", "الفجر", "Fajr", true),
    SUNRISE("sunrise", "الشروق", "Sunrise", false),
    DHUHR("dhuhr", "الظهر", "Dhuhr", true),
    ASR("asr", "العصر", "Asr", true),
    MAGHRIB("maghrib", "المغرب", "Maghrib", true),
    ISHA("isha", "العشاء", "Isha", true);

    companion object {
        fun fromId(id: String): PrayerType {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: FAJR
        }
    }
}

/**
 * Islamic Calculation Methods for prayer times.
 */
enum class CalculationMethod(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val fajrAngle: Double,
    val ishaAngle: Double,
    val ishaIntervalMinutes: Int = 0,
    val defaultCountryCode: String = ""
) {
    EGYPTIAN(
        id = "egypt",
        titleAr = "الهيئة المصرية العامة للمساحة",
        titleEn = "Egyptian General Authority of Survey",
        fajrAngle = 19.5,
        ishaAngle = 17.5,
        defaultCountryCode = "EG"
    ),
    UMM_AL_QURA(
        id = "makkah",
        titleAr = "أم القرى - مكة المكرمة",
        titleEn = "Umm Al-Qura University, Makkah",
        fajrAngle = 18.5,
        ishaAngle = 0.0,
        ishaIntervalMinutes = 90,
        defaultCountryCode = "SA"
    ),
    MUSLIM_WORLD_LEAGUE(
        id = "mwl",
        titleAr = "رابطة العالم الإسلامي (MWL)",
        titleEn = "Muslim World League",
        fajrAngle = 18.0,
        ishaAngle = 17.0,
        defaultCountryCode = "WORLD"
    ),
    KARACHI(
        id = "karachi",
        titleAr = "جامعة العلوم الإسلامية بكراتشي",
        titleEn = "University of Islamic Sciences, Karachi",
        fajrAngle = 18.0,
        ishaAngle = 18.0,
        defaultCountryCode = "PK"
    ),
    ISNA(
        id = "isna",
        titleAr = "الجمعية الإسلامية لأمريكا الشمالية (ISNA)",
        titleEn = "Islamic Society of North America",
        fajrAngle = 15.0,
        ishaAngle = 15.0,
        defaultCountryCode = "US"
    ),
    DIYANET(
        id = "diyanet",
        titleAr = "رئاسة الشؤون الدينية التركية (Diyanet)",
        titleEn = "Presidency of Religious Affairs, Turkey",
        fajrAngle = 18.0,
        ishaAngle = 17.0,
        defaultCountryCode = "TR"
    ),
    DUBAI(
        id = "dubai",
        titleAr = "دائرة الشؤون الإسلامية بدبي",
        titleEn = "Dubai Islamic Affairs",
        fajrAngle = 18.2,
        ishaAngle = 18.2,
        defaultCountryCode = "AE"
    ),
    KUWAIT(
        id = "kuwait",
        titleAr = "وزارة الأوقاف والشؤون الإسلامية بالكويت",
        titleEn = "Ministry of Awqaf, Kuwait",
        fajrAngle = 18.0,
        ishaAngle = 17.5,
        defaultCountryCode = "KW"
    ),
    QATAR(
        id = "qatar",
        titleAr = "وزارة الأوقاف القطرية",
        titleEn = "Ministry of Awqaf, Qatar",
        fajrAngle = 18.0,
        ishaAngle = 0.0,
        ishaIntervalMinutes = 90,
        defaultCountryCode = "QA"
    ),
    SINGAPORE(
        id = "singapore",
        titleAr = "المجلس الإسلامي بسنغافورة (MUIS)",
        titleEn = "Majlis Ugama Islam Singapura",
        fajrAngle = 20.0,
        ishaAngle = 18.0,
        defaultCountryCode = "SG"
    ),
    TEHRAN(
        id = "tehran",
        titleAr = "معهد ليفا بجامعة طهران",
        titleEn = "Institute of Geophysics, University of Tehran",
        fajrAngle = 17.7,
        ishaAngle = 14.0,
        defaultCountryCode = "IR"
    );

    companion object {
        fun fromId(id: String): CalculationMethod {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: EGYPTIAN
        }

        fun forCountryCode(code: String): CalculationMethod {
            return when (code.uppercase()) {
                "EG", "SD", "LY", "SY", "LB", "JO", "PS" -> EGYPTIAN
                "SA", "YE", "OM", "BH" -> UMM_AL_QURA
                "AE" -> DUBAI
                "KW" -> KUWAIT
                "QA" -> QATAR
                "PK", "IN", "BD", "AF" -> KARACHI
                "TR", "AZ" -> DIYANET
                "US", "CA", "GB", "FR", "DE" -> ISNA
                "ID", "MY", "SG" -> SINGAPORE
                "IR", "IQ" -> TEHRAN
                else -> MUSLIM_WORLD_LEAGUE
            }
        }
    }
}

/**
 * Juristic method for Asr prayer shadow length.
 */
enum class AsrJuristic(val id: String, val titleAr: String, val titleEn: String) {
    STANDARD("standard", "الجمهور (الشافعي، المالكي، الحنبلي)", "Standard (Shafi'i, Maliki, Hanbali)"),
    HANAFI("hanafi", "المذهب الحنفي (ظل المثلين)", "Hanafi");

    companion object {
        fun fromId(id: String): AsrJuristic {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: STANDARD
        }
    }
}

/**
 * Selectable Adhan reciter audio sounds embedded offline with high-fidelity compression.
 */
enum class AdhanSound(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val muezzinAr: String,
    val rawResId: Int,
    val fajrRawResId: Int = rawResId
) {
    MAKKAH(
        id = "makkah",
        titleAr = "أذان الحرم المكي الشريف",
        titleEn = "Makkah Haram Adhan",
        muezzinAr = "الشيخ علي بن أحمد ملا - المسجد الحرام",
        rawResId = R.raw.adhan_makkah,
        fajrRawResId = R.raw.adhan_fajr_makkah
    ),
    MADINAH(
        id = "madinah",
        titleAr = "أذان المسجد النبوي الشريف",
        titleEn = "Madinah Nabawi Adhan",
        muezzinAr = "مؤذنو المسجد النبوي الشريف",
        rawResId = R.raw.adhan_madinah
    ),
    CAIRO(
        id = "cairo",
        titleAr = "أذان القاهرة (الإذاعة المصرية)",
        titleEn = "Cairo Radio Adhan",
        muezzinAr = "الشيخ محمد رفعت - إذاعة القرآن الكريم",
        rawResId = R.raw.adhan_cairo
    ),
    AFASY(
        id = "afasy",
        titleAr = "أذان الشيخ مشاري العفاسي",
        titleEn = "Mishary Al-Afasy Adhan",
        muezzinAr = "الشيخ مشاري راشد العفاسي",
        rawResId = R.raw.adhan_afasy
    ),
    AL_AQSA(
        id = "al_aqsa",
        titleAr = "أذان المسجد الأقصى المبارك",
        titleEn = "Al-Aqsa Mosque Adhan",
        muezzinAr = "مؤذنو المسجد الأقصى المبارك - القدس",
        rawResId = R.raw.adhan_al_aqsa
    ),
    ABDULBASIT(
        id = "abdulbasit",
        titleAr = "أذان الشيخ عبد الباسط عبد الصمد",
        titleEn = "Abdulbasit Abdusamad Adhan",
        muezzinAr = "الشيخ عبد الباسط عبد الصمد",
        rawResId = R.raw.adhan_abdulbasit
    ),
    NAQSHBANDI(
        id = "naqshbandi",
        titleAr = "أذان الشيخ سيد النقشبندي",
        titleEn = "Sayed Al-Naqshbandi Adhan",
        muezzinAr = "الشيخ سيد النقشبندي",
        rawResId = R.raw.adhan_naqshbandi
    ),
    GENTLE_TAKBEER(
        id = "gentle_takbeer",
        titleAr = "تكبيرات وتنبيه هادئ",
        titleEn = "Gentle Takbeer Tone",
        muezzinAr = "تكبيرات الأذان الهادئة",
        rawResId = R.raw.adhan_gentle_takbeer
    );

    companion object {
        fun fromId(id: String): AdhanSound {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: MAKKAH
        }
    }
}

/**
 * Individual prayer time for today or given date.
 */
data class PrayerTimeItem(
    val type: PrayerType,
    val timeInMillis: Long,
    val formattedTime12h: String,
    val formattedTime24h: String,
    val isAdhanAlertEnabled: Boolean = true,
    val isIqamahAlertEnabled: Boolean = false,
    val iqamahDelayMinutes: Int = 15,
    val isCurrent: Boolean = false,
    val isNext: Boolean = false
)

/**
 * Full day prayer schedule with calendar metadata.
 */
data class DailyPrayerSchedule(
    val dateInMillis: Long,
    val gregorianDateFormatted: String,
    val hijriDateFormatted: String,
    val cityName: String,
    val countryName: String,
    val latitude: Double,
    val longitude: Double,
    val calculationMethod: CalculationMethod,
    val asrJuristic: AsrJuristic,
    val fajr: PrayerTimeItem,
    val sunrise: PrayerTimeItem,
    val dhuhr: PrayerTimeItem,
    val asr: PrayerTimeItem,
    val maghrib: PrayerTimeItem,
    val isha: PrayerTimeItem,
    val nextPrayer: PrayerTimeItem,
    val millisUntilNextPrayer: Long,
    val progressToNextPrayer: Float
) {
    val allPrayers: List<PrayerTimeItem>
        get() = listOf(fajr, sunrise, dhuhr, asr, maghrib, isha)
}

/**
 * Qibla Compass state and bearing info.
 */
data class QiblaInfo(
    val userLatitude: Double,
    val userLongitude: Double,
    val qiblaBearing: Float, // Bearing in degrees from True North towards Mecca (0..360)
    val distanceKm: Double,  // Distance to Kaaba in kilometers
    val currentAzimuth: Float = 0f, // Current device azimuth relative to True North (0..360)
    val relativeAngle: Float = 0f, // (qiblaBearing - currentAzimuth) normalized to -180..180
    val isAligned: Boolean = false, // True if pointing directly to Qibla (within ±2.5°)
    val accuracy: Int = 3, // SensorManager accuracy: 3 = HIGH, 2 = MEDIUM, 1 = LOW, 0 = UNRELIABLE
    val isSensorAvailable: Boolean = true
)
