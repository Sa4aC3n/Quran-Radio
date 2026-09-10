package com.example.model

enum class HaramSource(val id: String, val titleAr: String, val emoji: String, val defaultStationId: String) {
    MAKKAH("makkah", "الحرم المكي الشريف", "🕋", "haram_makkah_live"),
    MADINAH("madinah", "المسجد النبوي الشريف", "🕌", "haram_madinah_live"),
    BOTH("both", "كلا الحرمين الشريفين", "🕋🕌", "haram_makkah_live");

    companion object {
        fun fromId(id: String): HaramSource {
            return entries.find { it.id == id } ?: BOTH
        }
    }
}

data class HaramLivePreferences(
    val isEnabled: Boolean = false,
    val source: HaramSource = HaramSource.BOTH,
    val favoriteSurahsOnly: Boolean = false,
    val favoriteSurahs: Set<Int> = setOf(18, 36, 67, 55, 56, 2, 3), // Al-Kahf, Yasin, Al-Mulk, Ar-Rahman, Al-Waqi'ah, Al-Baqarah, Aal-Imran
    val dailyLimit: Int = 4,
    val notificationsSentToday: Int = 0,
    val lastNotificationDate: String = ""
)

data class QuranSurahItem(
    val number: Int,
    val nameAr: String,
    val englishName: String,
    val versesCount: Int,
    val isPopular: Boolean = false
)

object QuranSurahsData {
    val SURAHS: List<QuranSurahItem> = listOf(
        QuranSurahItem(1, "الفاتحة", "Al-Fatihah", 7, true),
        QuranSurahItem(2, "البقرة", "Al-Baqarah", 286, true),
        QuranSurahItem(3, "آل عمران", "Aal-Imran", 200, true),
        QuranSurahItem(4, "النساء", "An-Nisa", 176, false),
        QuranSurahItem(5, "المائدة", "Al-Ma'idah", 120, false),
        QuranSurahItem(6, "الأنعام", "Al-An'am", 165, false),
        QuranSurahItem(7, "الأعراف", "Al-A'raf", 206, false),
        QuranSurahItem(8, "الأنفال", "Al-Anfal", 75, false),
        QuranSurahItem(9, "التوبة", "At-Tawbah", 129, false),
        QuranSurahItem(10, "يونس", "Yunus", 109, false),
        QuranSurahItem(11, "هود", "Hud", 123, false),
        QuranSurahItem(12, "يوسف", "Yusuf", 111, true),
        QuranSurahItem(13, "الرعد", "Ar-Ra'd", 43, false),
        QuranSurahItem(14, "إبراهيم", "Ibrahim", 52, false),
        QuranSurahItem(15, "الحجر", "Al-Hijr", 99, false),
        QuranSurahItem(16, "النحل", "An-Nahl", 128, false),
        QuranSurahItem(17, "الإسراء", "Al-Isra", 111, true),
        QuranSurahItem(18, "الكهف", "Al-Kahf", 110, true),
        QuranSurahItem(19, "مريم", "Maryam", 98, true),
        QuranSurahItem(20, "طه", "Ta-Ha", 135, true),
        QuranSurahItem(21, "الأنبياء", "Al-Anbiya", 112, false),
        QuranSurahItem(22, "الحج", "Al-Hajj", 78, true),
        QuranSurahItem(23, "المؤمنون", "Al-Mu'minun", 118, false),
        QuranSurahItem(24, "النور", "An-Nur", 64, true),
        QuranSurahItem(25, "الفرقان", "Al-Furqan", 77, false),
        QuranSurahItem(26, "الشعراء", "Ash-Shu'ara", 227, false),
        QuranSurahItem(27, "النمل", "An-Naml", 93, false),
        QuranSurahItem(28, "القصص", "Al-Qasas", 88, false),
        QuranSurahItem(29, "العنكبوت", "Al-Ankabut", 69, false),
        QuranSurahItem(30, "الروم", "Ar-Rum", 60, false),
        QuranSurahItem(31, "لقمان", "Luqman", 34, true),
        QuranSurahItem(32, "السجدة", "As-Sajdah", 30, true),
        QuranSurahItem(33, "الأحزاب", "Al-Ahzab", 73, false),
        QuranSurahItem(34, "سبأ", "Saba", 54, false),
        QuranSurahItem(35, "فاطر", "Fatir", 45, false),
        QuranSurahItem(36, "يس", "Ya-Sin", 83, true),
        QuranSurahItem(37, "الصافات", "As-Saffat", 182, false),
        QuranSurahItem(38, "ص", "Sad", 88, false),
        QuranSurahItem(39, "الزمر", "Az-Zumar", 75, false),
        QuranSurahItem(40, "غافر", "Ghafir", 85, false),
        QuranSurahItem(41, "فصلت", "Fussilat", 54, false),
        QuranSurahItem(42, "الشورى", "Ash-Shura", 53, false),
        QuranSurahItem(43, "الزخرف", "Az-Zukhruf", 89, false),
        QuranSurahItem(44, "الدخان", "Ad-Dukhan", 59, true),
        QuranSurahItem(45, "الجاثية", "Al-Jathiyah", 37, false),
        QuranSurahItem(46, "الأحقاف", "Al-Ahqaf", 35, false),
        QuranSurahItem(47, "محمد", "Muhammad", 38, false),
        QuranSurahItem(48, "الفتح", "Al-Fath", 29, true),
        QuranSurahItem(49, "الحجرات", "Al-Hujurat", 18, true),
        QuranSurahItem(50, "ق", "Qaf", 45, true),
        QuranSurahItem(51, "الذاريات", "Adh-Dhariyat", 60, false),
        QuranSurahItem(52, "الطور", "At-Tur", 49, false),
        QuranSurahItem(53, "النجم", "An-Najm", 62, false),
        QuranSurahItem(54, "القمر", "Al-Qamar", 55, false),
        QuranSurahItem(55, "الرحمن", "Ar-Rahman", 78, true),
        QuranSurahItem(56, "الواقعة", "Al-Waqi'ah", 96, true),
        QuranSurahItem(57, "الحديد", "Al-Hadid", 29, false),
        QuranSurahItem(58, "المجادلة", "Al-Mujadilah", 22, false),
        QuranSurahItem(59, "الحشر", "Al-Hashr", 24, false),
        QuranSurahItem(60, "الممتحنة", "Al-Mumtahanah", 13, false),
        QuranSurahItem(61, "الصف", "As-Saff", 14, false),
        QuranSurahItem(62, "الجمعة", "Al-Jumu'ah", 11, true),
        QuranSurahItem(63, "المنافقون", "Al-Munafiqun", 11, false),
        QuranSurahItem(64, "التغابن", "At-Taghabun", 18, false),
        QuranSurahItem(65, "الطلاق", "At-Talaq", 12, false),
        QuranSurahItem(66, "التحريم", "At-Tahrim", 12, false),
        QuranSurahItem(67, "الملك", "Al-Mulk", 30, true),
        QuranSurahItem(68, "القلم", "Al-Qalam", 52, false),
        QuranSurahItem(69, "الحاقة", "Al-Haqqah", 52, false),
        QuranSurahItem(70, "المعارج", "Al-Ma'arij", 44, false),
        QuranSurahItem(71, "نوح", "Nuh", 28, false),
        QuranSurahItem(72, "الجن", "Al-Jinn", 28, false),
        QuranSurahItem(73, "المزمل", "Al-Muzzammil", 20, true),
        QuranSurahItem(74, "المدثر", "Al-Muddaththir", 56, false),
        QuranSurahItem(75, "القيامة", "Al-Qiyamah", 40, false),
        QuranSurahItem(76, "الإنسان", "Al-Insan", 31, true),
        QuranSurahItem(77, "المرسلات", "Al-Mursalat", 50, false),
        QuranSurahItem(78, "النبأ", "An-Naba", 40, true),
        QuranSurahItem(79, "النازعات", "An-Nazi'at", 46, false),
        QuranSurahItem(80, "عبس", "Abasa", 42, false),
        QuranSurahItem(81, "التكوير", "At-Takwir", 29, false),
        QuranSurahItem(82, "الانفطار", "Al-Infitar", 19, false),
        QuranSurahItem(83, "المطففين", "Al-Mutaffifin", 36, false),
        QuranSurahItem(84, "الانشقاق", "Al-Inshiqaq", 25, false),
        QuranSurahItem(85, "البروج", "Al-Buruj", 22, false),
        QuranSurahItem(86, "الطارق", "At-Tariq", 17, false),
        QuranSurahItem(87, "الأعلى", "Al-A'la", 19, true),
        QuranSurahItem(88, "الغاشية", "Al-Ghashiyah", 26, true),
        QuranSurahItem(89, "الفجر", "Al-Fajr", 30, true),
        QuranSurahItem(90, "البلد", "Al-Balad", 20, false),
        QuranSurahItem(91, "الشمس", "Ash-Shams", 15, false),
        QuranSurahItem(92, "الليل", "Al-Layl", 21, false),
        QuranSurahItem(93, "الضحى", "Ad-Duha", 11, true),
        QuranSurahItem(94, "الشرح", "Ash-Sharh", 8, true),
        QuranSurahItem(95, "التين", "At-Tin", 8, false),
        QuranSurahItem(96, "العلق", "Al-Alaq", 19, false),
        QuranSurahItem(97, "القدر", "Al-Qadr", 5, true),
        QuranSurahItem(98, "البينة", "Al-Bayyinah", 8, false),
        QuranSurahItem(99, "الزلزلة", "Az-Zalzalah", 8, false),
        QuranSurahItem(100, "العاديات", "Al-Adiyat", 11, false),
        QuranSurahItem(101, "القارعة", "Al-Qari'ah", 11, false),
        QuranSurahItem(102, "التكاثر", "At-Takathur", 8, false),
        QuranSurahItem(103, "العصر", "Al-Asr", 3, true),
        QuranSurahItem(104, "الهمزة", "Al-Humazah", 9, false),
        QuranSurahItem(105, "الفيل", "Al-Fil", 5, false),
        QuranSurahItem(106, "قريش", "Quraysh", 4, false),
        QuranSurahItem(107, "الماعون", "Al-Ma'un", 7, false),
        QuranSurahItem(108, "الكوثر", "Al-Kawthar", 3, true),
        QuranSurahItem(109, "الكافرون", "Al-Kafirun", 6, true),
        QuranSurahItem(110, "النصر", "An-Nasr", 3, false),
        QuranSurahItem(111, "المسد", "Al-Masad", 5, false),
        QuranSurahItem(112, "الإخلاص", "Al-Ikhlas", 4, true),
        QuranSurahItem(113, "الفلق", "Al-Falaq", 5, true),
        QuranSurahItem(114, "الناس", "An-Nas", 6, true)
    )

    fun getSurah(number: Int): QuranSurahItem? = SURAHS.find { it.number == number }
    fun getSurahByName(name: String): QuranSurahItem? {
        val cleanName = name.replace("سورة", "").trim()
        return SURAHS.find { it.nameAr.contains(cleanName) || cleanName.contains(it.nameAr) }
    }
}
