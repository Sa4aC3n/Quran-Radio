package com.example.model

/**
 * Source type for personal Khatma tracker.
 */
enum class PersonalKhatmaSourceType(val titleAr: String) {
    STATION("إذاعة قرآنية"),
    RECITER("قارئ محدد"),
    CUSTOM("تلاوة مخصصة")
}

/**
 * Model representing a single personal Quran listening khatma.
 */
data class PersonalKhatma(
    val id: String,
    val title: String,
    val sourceName: String,
    val sourceType: PersonalKhatmaSourceType = PersonalKhatmaSourceType.STATION,
    val sourceId: String? = null,
    val sourceLogoUrl: String? = null,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val lastUpdatedAtMillis: Long = System.currentTimeMillis(),
    val completedParts: List<Boolean> = List(30) { false },
    val isArchived: Boolean = false
) {
    val completedCount: Int
        get() = completedParts.count { it }

    val progressFraction: Float
        get() = if (completedParts.isEmpty()) 0f else (completedCount.toFloat() / 30f).coerceIn(0f, 1f)

    val progressPercent: Int
        get() = (progressFraction * 100).toInt()

    val isFullyCompleted: Boolean
        get() = completedCount == 30

    /**
     * Calculates the next expected part number (1..30) to listen to.
     */
    val nextExpectedJuz: Int
        get() {
            val firstUncompletedIndex = completedParts.indexOfFirst { !it }
            return if (firstUncompletedIndex >= 0) firstUncompletedIndex + 1 else 30
        }

    /**
     * Days since last update
     */
    val daysSinceLastUpdate: Int
        get() {
            val diffMillis = System.currentTimeMillis() - lastUpdatedAtMillis
            return (diffMillis / 86400000L).toInt().coerceAtLeast(0)
        }

    /**
     * Inactivity reminder check: 3 or more days without activity and not yet finished.
     */
    val isInactiveReminderNeeded: Boolean
        get() = !isFullyCompleted && !isArchived && daysSinceLastUpdate >= 3

    companion object {
        val JUZ_META = listOf(
            Pair("الجزء الأول", "الفاتحة ١ - البقرة ١٤١"),
            Pair("الجزء الثاني", "البقرة ١٤٢ - البقرة ٢٥٢"),
            Pair("الجزء الثالث", "البقرة ٢٥٣ - آل عمران ٩٢"),
            Pair("الجزء الرابع", "آل عمران ٩٣ - النساء ٢٣"),
            Pair("الجزء الخامس", "النساء ٢٤ - النساء ١٤٧"),
            Pair("الجزء السادس", "النساء ١٤٨ - المائدة ٨١"),
            Pair("الجزء السابع", "المائدة ٨٢ - الأنعام ١١٠"),
            Pair("الجزء الثامن", "الأنعام ١١١ - الأعراف ٨٧"),
            Pair("الجزء التاسع", "الأعراف ٨٨ - الأنفال ٤٠"),
            Pair("الجزء العاشر", "الأنفال ٤١ - التوبة ٩٢"),
            Pair("الجزء الحادي عشر", "التوبة ٩٣ - هود ٥"),
            Pair("الجزء الثاني عشر", "هود ٦ - يوسف ٥٢"),
            Pair("الجزء الثالث عشر", "يوسف ٥٣ - إبراهيم ٥٢"),
            Pair("الجزء الرابع عشر", "الحجر ١ - النحل ١٢٨"),
            Pair("الجزء الخامس عشر", "الإسراء ١ - الكهف ٧٤"),
            Pair("الجزء السادس عشر", "الكهف ٧٥ - طه ١٣٥"),
            Pair("الجزء السابع عشر", "الأنبياء ١ - الحج ٧٨"),
            Pair("الجزء الثامن عشر", "المؤمنون ١ - الفرقان ٢٠"),
            Pair("الجزء التاسع عشر", "الفرقان ٢١ - النمل ٥٥"),
            Pair("الجزء العشرون", "النمل ٥٦ - العنكبوت ٤٥"),
            Pair("الجزء الحادي والعشرون", "العنكبوت ٤٦ - الأحزاب ٣٠"),
            Pair("الجزء الثاني والعشرون", "الأحزاب ٣١ - يس ٢٧"),
            Pair("الجزء الثالث والعشرون", "يس ٢٨ - الزمر ٣١"),
            Pair("الجزء الرابع والعشرون", "الزمر ٣٢ - فصلت ٤٦"),
            Pair("الجزء الخامس والعشرون", "فصلت ٤٧ - الجاثية ٣٧"),
            Pair("الجزء السادس والعشرون", "الأحقاف ١ - الذاريات ٣٠"),
            Pair("الجزء السابع والعشرون", "الذاريات ٣١ - الحديد ٢٩"),
            Pair("الجزء الثامن والعشرون", "المجادلة ١ - التحريم ١٢"),
            Pair("الجزء التاسع والعشرون", "الملك ١ - المرسلات ٥٠"),
            Pair("الجزء الثلاثون", "النبأ ١ - الناس ٦")
        )
    }
}
