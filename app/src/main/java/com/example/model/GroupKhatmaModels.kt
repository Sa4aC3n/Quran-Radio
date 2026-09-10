package com.example.model

/**
 * Type of completion for a Juz / Quran part.
 */
enum class KhatmaCompletionType(val titleAr: String, val emoji: String) {
    READING("قراءة", "📖"),
    LISTENING("استماع", "🎧");

    companion object {
        fun fromName(name: String?): KhatmaCompletionType {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: READING
        }
    }
}

/**
 * Representation of one of the 30 Juzs of the Holy Quran.
 */
data class KhatmaPart(
    val juzNumber: Int,                     // 1..30
    val titleAr: String,                    // e.g. "الجزء الأول"
    val surahRange: String,                 // e.g. "الفاتحة ١ - البقرة ١٤١"
    val assignedMemberName: String? = null, // null = available / فارغ
    val isCompleted: Boolean = false,
    val completionType: KhatmaCompletionType = KhatmaCompletionType.READING,
    val completionNote: String? = null,     // Optional note e.g. "تمت القراءة بحمد الله"
    val completedAtMillis: Long? = null,
    val reservedAtMillis: Long? = null
) {
    val isAvailable: Boolean
        get() = assignedMemberName.isNullOrBlank()

    val isReserved: Boolean
        get() = !assignedMemberName.isNullOrBlank() && !isCompleted
}

/**
 * Representation of a Group Khatma (الختمة الجماعية).
 */
data class GroupKhatma(
    val id: String,
    val title: String,
    val inviteCode: String,
    val creatorName: String,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val targetDateMillis: Long? = null,
    val description: String? = null,
    val parts: List<KhatmaPart> = defaultKhatmaParts()
) {
    val completedCount: Int
        get() = parts.count { it.isCompleted }

    val reservedCount: Int
        get() = parts.count { it.isReserved }

    val availableCount: Int
        get() = parts.count { it.isAvailable }

    val progressFraction: Float
        get() = if (parts.isEmpty()) 0f else completedCount.toFloat() / parts.size.toFloat()

    val progressPercent: Int
        get() = (progressFraction * 100).toInt()

    val isFullyCompleted: Boolean
        get() = parts.isNotEmpty() && completedCount == parts.size

    val distinctMembers: List<String>
        get() = parts.mapNotNull { it.assignedMemberName }.filter { it.isNotBlank() }.distinct()

    companion object {
        fun defaultKhatmaParts(): List<KhatmaPart> {
            val juzNames = listOf(
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

            return (1..30).map { index ->
                val meta = juzNames[index - 1]
                KhatmaPart(
                    juzNumber = index,
                    titleAr = meta.first,
                    surahRange = meta.second,
                    assignedMemberName = null,
                    isCompleted = false
                )
            }
        }
    }
}

/**
 * Statistics of personal Khatma participation.
 */
data class MyKhatmaStats(
    val totalKhatmasParticipated: Int = 0,
    val totalPartsAssigned: Int = 0,
    val totalPartsCompleted: Int = 0,
    val activeKhatmasCount: Int = 0,
    val completedKhatmasCount: Int = 0
) {
    val totalKhatmasJoined: Int get() = totalKhatmasParticipated
    val totalPartsReserved: Int get() = (totalPartsAssigned - totalPartsCompleted).coerceAtLeast(0)
}

/**
 * Saved local profile of the user.
 */
data class UserKhatmaProfile(
    val userName: String = "",
    val lastOpenedKhatmaId: String? = null
)
