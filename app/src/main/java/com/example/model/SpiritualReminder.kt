package com.example.model

import com.example.R

enum class SpiritualReminderType(
    val id: String,
    val categoryAr: String,
    val defaultTitle: String,
    val deepLinkRoute: String
) {
    QURAN_RECITATION(
        id = "quran_recitation",
        categoryAr = "تلاوة قرآنية",
        defaultTitle = "استمع لآيات من الذكر الحكيم 📖",
        deepLinkRoute = "reciters"
    ),
    LIVE_RADIO(
        id = "live_radio",
        categoryAr = "إذاعة القرآن الكريم",
        defaultTitle = "بث مباشر لإذاعة القرآن الكريم 📻",
        deepLinkRoute = "countries"
    ),
    TAFSIR_AND_TADABBUR(
        id = "tafsir_tadabbur",
        categoryAr = "تفسير وتدبر",
        defaultTitle = "تأمل في معاني القرآن وتفسيره ✨",
        deepLinkRoute = "home"
    ),
    GROUP_KHATMA(
        id = "group_khatma",
        categoryAr = "ختمة القرآن",
        defaultTitle = "شارك في الختمة الجماعية اليوم 🤲",
        deepLinkRoute = "group_khatma"
    ),
    PERSONAL_TRACKER(
        id = "personal_tracker",
        categoryAr = "متتبع الختمة",
        defaultTitle = "واصل استماعك لوردك اليومي 🎧",
        deepLinkRoute = "group_khatma"
    ),
    DHIKR_AND_DUA(
        id = "dhikr_dua",
        categoryAr = "أذكار وأدعية",
        defaultTitle = "ألا بذكر الله تطمئن القلوب 🤍",
        deepLinkRoute = "home"
    )
}

data class SpiritualReminderMessage(
    val id: String,
    val type: SpiritualReminderType,
    val title: String,
    val body: String,
    val targetDeepLink: String, // e.g. "reciter/1", "country/EG", "group_khatma", "station/cairo"
    val stationId: String? = null
)

object SpiritualReminderBank {
    val messages = listOf(
        SpiritualReminderMessage(
            id = "rem_1",
            type = SpiritualReminderType.QURAN_RECITATION,
            title = "راحة وطمأنينة لقلبك 🌿",
            body = "استمع لتلاوة خاشعة بصوت الشيخ عبد الباسط عبد الصمد",
            targetDeepLink = "reciters"
        ),
        SpiritualReminderMessage(
            id = "rem_2",
            type = SpiritualReminderType.LIVE_RADIO,
            title = "إذاعة القرآن الكريم من القاهرة 🎙️",
            body = "استمع الآن للبث المباشر لأعرق إذاعة قرآنية في العالم الإسلامي",
            targetDeepLink = "countries",
            stationId = "cairo_quran"
        ),
        SpiritualReminderMessage(
            id = "rem_3",
            type = SpiritualReminderType.TAFSIR_AND_TADABBUR,
            title = "آية ومعنى وتدبر ✨",
            body = "«أَفَلَا يَتَدَبَّرُونَ الْقُرْآنَ» — استمع لتفسير وتأملات قرآنية مباركة تنير يومك",
            targetDeepLink = "home"
        ),
        SpiritualReminderMessage(
            id = "rem_4",
            type = SpiritualReminderType.GROUP_KHATMA,
            title = "الختمة القرآنية الجماعية 📖",
            body = "شارك جزءاً من كتاب الله مع إخوانك في الختمة الجماعية ونل الأجر العظيم",
            targetDeepLink = "group_khatma"
        ),
        SpiritualReminderMessage(
            id = "rem_5",
            type = SpiritualReminderType.QURAN_RECITATION,
            title = "تلاوة عذبة بصوت الشيخ مشاري العفاسي 🎧",
            body = "جدد إيمانك واستمع لروائع السور القرآنية بأعذب الأصوات",
            targetDeepLink = "reciters"
        ),
        SpiritualReminderMessage(
            id = "rem_6",
            type = SpiritualReminderType.LIVE_RADIO,
            title = "إذاعة القرآن الكريم من مكة المكرمة 🕋",
            body = "عطّر يومك بنفحات الحرم المكي الشريف وتلاوات أئمة الحرمين الشريفين",
            targetDeepLink = "countries",
            stationId = "saudi_quran"
        ),
        SpiritualReminderMessage(
            id = "rem_7",
            type = SpiritualReminderType.PERSONAL_TRACKER,
            title = "وردك القرآني اليومي 🤲",
            body = "واصل متابعة الختمة واستمع للجزء القادم من وردك المبارك",
            targetDeepLink = "group_khatma"
        ),
        SpiritualReminderMessage(
            id = "rem_8",
            type = SpiritualReminderType.DHIKR_AND_DUA,
            title = "ألا بذكر الله تطمئن القلوب 🤍",
            body = "حصّن نفسك ويومك بالاستغفار، الأذكار، والصلاة على النبي ﷺ",
            targetDeepLink = "home"
        ),
        SpiritualReminderMessage(
            id = "rem_9",
            type = SpiritualReminderType.QURAN_RECITATION,
            title = "تلاوة الشيخ محمد صديق المنشاوي 🌸",
            body = "تلاوة ترتيل ترق لها القلوب وتخشع لها الأبصار — استمع الآن",
            targetDeepLink = "reciters"
        ),
        SpiritualReminderMessage(
            id = "rem_10",
            type = SpiritualReminderType.LIVE_RADIO,
            title = "إذاعات العالم الإسلامي 🌍",
            body = "استكشف مئات الإذاعات القرآنية المباشرة من مختلف الدول الإسلامية",
            targetDeepLink = "countries"
        ),
        SpiritualReminderMessage(
            id = "rem_11",
            type = SpiritualReminderType.TAFSIR_AND_TADABBUR,
            title = "زاد الروح وتفسير القرآن 🌟",
            body = "استمع لدروس التفسير الميسر وغريب القرآن لتفهم كتاب ربك",
            targetDeepLink = "home"
        ),
        SpiritualReminderMessage(
            id = "rem_12",
            type = SpiritualReminderType.QURAN_RECITATION,
            title = "تلاوة الشيخ محمود خليل الحصري 🎙️",
            body = "شيخ المقارئ المصرية وإتقان أحكام التلاوة والتجويد",
            targetDeepLink = "reciters"
        ),
        SpiritualReminderMessage(
            id = "rem_13",
            type = SpiritualReminderType.GROUP_KHATMA,
            title = "ختمة القرآن الكريم 🤲",
            body = "خطوة نحو إتمام ختمتك — استمع لورد اليوم واختم مع مجتمع التطبيق",
            targetDeepLink = "group_khatma"
        ),
        SpiritualReminderMessage(
            id = "rem_14",
            type = SpiritualReminderType.LIVE_RADIO,
            title = "بث مباشر لإذاعة الشارقة للقرآن 📻",
            body = "استمع لتلاوات متواصلة بأصوات نخبة من أشهر قراء العالم الإسلامي",
            targetDeepLink = "countries"
        )
    )

    fun getRandomMessage(): SpiritualReminderMessage {
        return messages.random()
    }
}
