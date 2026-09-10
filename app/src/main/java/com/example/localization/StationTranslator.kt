package com.example.localization

import com.example.model.Country
import com.example.model.RadioStation
import com.example.model.Reciter

/**
 * Universal content translator for Radio Stations, Reciters, Countries, and cities
 * across all 7 supported languages: Arabic, English, Persian, Urdu, Kyrgyz, Hausa, French.
 */
object StationTranslator {

    // --- Station Translations by ID or Name Pattern ---
    private val stationTranslationMap = mapOf(
        "haram_makkah_live" to mapOf(
            AppLanguage.AR to "إذاعة تلاوات الحرم المكي الشريف - بث مباشر 🕋",
            AppLanguage.EN to "Holy Makkah Haram Recitations Radio - Live 🕋",
            AppLanguage.FA to "رادیو تلاوت‌های مسجد الحرام مکه مکرمه - پخش زنده 🕋",
            AppLanguage.UR to "حرم مکی شریف کی تلاوتوں کا ریڈیو - لائیو 🕋",
            AppLanguage.KY to "Ыйык Мекке Харам кыраат радиосу - Түз эфир 🕋",
            AppLanguage.HA to "Gidan Rediyon Karatun Haramin Makka - Kai Tsaye 🕋",
            AppLanguage.FR to "Radio Récitations de la Mosquée Sacrée de La Mecque - Direct 🕋"
        ),
        "haram_madinah_live" to mapOf(
            AppLanguage.AR to "إذاعة تلاوات المسجد النبوي الشريف - بث مباشر 🕌",
            AppLanguage.EN to "Prophet's Mosque Madinah Recitations Radio - Live 🕌",
            AppLanguage.FA to "رادیو تلاوت‌های مسجد نبوی شریف مدینه منوره - پخش زنده 🕌",
            AppLanguage.UR to "مسجد نبوی شریف کی تلاوتوں کا ریڈیو - لائیو 🕌",
            AppLanguage.KY to "Пайгамбардын Мечити Мадина кыраат радиосу - Түз эфир 🕌",
            AppLanguage.HA to "Gidan Rediyon Karatun Masallacin Annabi Madina - Kai Tsaye 🕌",
            AppLanguage.FR to "Radio Récitations de la Mosquée du Prophète Médine - Direct 🕌"
        ),
        "cairo_quran" to mapOf(
            AppLanguage.AR to "إذاعة القرآن الكريم من القاهرة",
            AppLanguage.EN to "Holy Quran Radio from Cairo",
            AppLanguage.FA to "رادیو قرآن کریم از قاهره",
            AppLanguage.UR to "قاہرہ سے قرآن کریم ریڈیو",
            AppLanguage.KY to "Каирден Ыйык Куран радиосу",
            AppLanguage.HA to "Gidan Rediyon Alkur'ani Mai Girma daga Alkahira",
            AppLanguage.FR to "Radio du Saint Coran du Caire"
        ),
        "mp3quran_tarateel" to mapOf(
            AppLanguage.AR to "إذاعة mp3 Quran - تراتيل قصيرة متميزة",
            AppLanguage.EN to "mp3 Quran Radio - Short Masterpiece Recitations",
            AppLanguage.FA to "رادیو mp3 Quran - ترتیل‌های کوتاه برگزیده",
            AppLanguage.UR to "mp3 قرآن ریڈیو - مختصر اور منتخب تلاوتیں",
            AppLanguage.KY to "mp3 Куран радиосу - Тандалган кыска кырааттар",
            AppLanguage.HA to "Rediyon mp3 Quran - Gajerun Karatuttuka Masu Kayatarwa",
            AppLanguage.FR to "Radio mp3 Quran - Récitations courtes d'excellence"
        ),
        "tafseer_alshaarawi" to mapOf(
            AppLanguage.AR to "إذاعة تفسير الشيخ الشعراوي",
            AppLanguage.EN to "Sheikh Al-Shaarawy Quran Tafsir Radio",
            AppLanguage.FA to "رادیو تفسیر شیخ شعراوی",
            AppLanguage.UR to "شیخ الشعراوی کی تفسیر قرآن ریڈیو",
            AppLanguage.KY to "Шейх Шааравинин Куран тафсири радиосу",
            AppLanguage.HA to "Rediyon Tafsirin Alkur'ani na Sheikh Shaarawi",
            AppLanguage.FR to "Radio Exégèse (Tafsir) de Cheikh Al-Shaarawi"
        ),
        "station_mukhtasartafsir" to mapOf(
            AppLanguage.AR to "إذاعة المختصر في تفسير القرآن الكريم",
            AppLanguage.EN to "Al-Mukhtasar in Quran Tafsir Radio",
            AppLanguage.FA to "رادیو المختصر فی تفسیر القرآن الکریم",
            AppLanguage.UR to "المختصر فی تفسیر القرآن الکریم ریڈیو",
            AppLanguage.KY to "Куран тафсириндеги Аль-Мухтасар радиосу",
            AppLanguage.HA to "Rediyon Mukhtasar na Tafsirin Alkur'ani",
            AppLanguage.FR to "Radio Al-Moukhtasar dans l'Exégèse du Saint Coran"
        ),
        "station_mukhtasartafsir_english" to mapOf(
            AppLanguage.AR to "إذاعة المختصر في تفسير القرآن الكريم (باللغة الإنجليزية)",
            AppLanguage.EN to "Al-Mukhtasar in Quran Tafsir Radio (English)",
            AppLanguage.FA to "رادیو المختصر فی تفسیر القرآن الکریم (به زبان انگلیسی)",
            AppLanguage.UR to "المختصر فی تفسیر القرآن الکریم ریڈیو (انگریزی میں)",
            AppLanguage.KY to "Куран тафсириндеги Аль-Мухтасар радиосу (Англис тилинде)",
            AppLanguage.HA to "Rediyon Mukhtasar na Tafsirin Alkur'ani (Turanci)",
            AppLanguage.FR to "Radio Al-Moukhtasar dans l'Exégèse du Coran (En Anglais)"
        ),
        "ruqyah_sharia_live" to mapOf(
            AppLanguage.AR to "إذاعة الرقية الشرعية الشاملة 24/7",
            AppLanguage.EN to "Comprehensive Ruqyah Sharia Radio 24/7",
            AppLanguage.FA to "رادیو رقیه شرعیه جامع ۲۴/۷",
            AppLanguage.UR to "جامع شرعی دم (رقیہ) ریڈیو 24/7",
            AppLanguage.KY to "Шарияттык Дем салуу (Рукыя) радиосу 24/7",
            AppLanguage.HA to "Rediyon Rukiyyar Shari'a 24/7",
            AppLanguage.FR to "Radio Roqya Shariya Complète 24/7"
        ),
        "adhkar_morning_evening" to mapOf(
            AppLanguage.AR to "إذاعة أذكار الصباح والمساء والأدعية",
            AppLanguage.EN to "Morning & Evening Adhkar and Supplications Radio",
            AppLanguage.FA to "رادیو اذکار صبح و شام و دعاها",
            AppLanguage.UR to "صبح اور شام کے اذکار و دعاؤں کا ریڈیو",
            AppLanguage.KY to "Эртең мененки жана кечки зикирлер радиосу",
            AppLanguage.HA to "Rediyon Azkar na Safe da Yamma da Addu'o'i",
            AppLanguage.FR to "Radio Invocations (Adhkar) du Matin, Soir et Douas"
        ),
        "sunnah_sahih_bukhari" to mapOf(
            AppLanguage.AR to "إذاعة صحيح البخاري والحديث النبوي الشريف",
            AppLanguage.EN to "Sahih Al-Bukhari & Prophetic Hadith Radio",
            AppLanguage.FA to "رادیو صحیح بخاری و احادیث نبوی شریف",
            AppLanguage.UR to "صحیح بخاری اور احادیث نبویہ کا ریڈیو",
            AppLanguage.KY to "Сахих аль-Бухари жана Пайгамбардын хадистери радиосу",
            AppLanguage.HA to "Rediyon Sahihul Bukhari da Hadisan Annabi",
            AppLanguage.FR to "Radio Sahih Al-Bukhari & Hadiths Prophétiques"
        ),
        "fatwa_nur_ala_darb" to mapOf(
            AppLanguage.AR to "إذاعة فتاوى برنامج نور على الدرب",
            AppLanguage.EN to "Noor Ala Al-Darb Fatwa Radio",
            AppLanguage.FA to "رادیو فتاوای برنامه نور علی الدرب",
            AppLanguage.UR to "نور علی الدرب فتاویٰ ریڈیو",
            AppLanguage.KY to "Нур ала ад-Дарб фатвалар радиосу",
            AppLanguage.HA to "Rediyon Fatawowin Nur Ala Al-Darb",
            AppLanguage.FR to "Radio des Fatwas Nour Ala Al-Darb"
        ),
        "seerah_nabawiyya_live" to mapOf(
            AppLanguage.AR to "إذاعة السيرة النبوية العطرة وقصص الأنبياء",
            AppLanguage.EN to "Prophet's Biography & Stories of Prophets Radio",
            AppLanguage.FA to "رادیو سیره مطهره نبوی و داستان‌های پیامبران",
            AppLanguage.UR to "سیرت النبی ﷺ اور قصص الانبیاء ریڈیو",
            AppLanguage.KY to "Пайгамбардын өмүр баяны жана пайгамбарлар баяны радиосу",
            AppLanguage.HA to "Rediyon Tarihin Annabi da Labaran Annabawa",
            AppLanguage.FR to "Radio Biographie du Prophète & Histoires des Prophètes"
        ),
        "quran_translation_english" to mapOf(
            AppLanguage.AR to "إذاعة ترجمة معاني القرآن الكريم باللغة الإنجليزية",
            AppLanguage.EN to "Holy Quran English Meaning Translation Radio",
            AppLanguage.FA to "رادیو ترجمه انگلیسی معانی قرآن کریم",
            AppLanguage.UR to "انگریزی ترجمہ قرآن ریڈیو",
            AppLanguage.KY to "Англис тилиндеги Куран маанилеринин котормосу радиосу",
            AppLanguage.HA to "Rediyon FassPlugin Alkur'ani da Harshen Turanci",
            AppLanguage.FR to "Radio Traduction des sens du Coran en Anglais"
        ),
        "quran_translation_french" to mapOf(
            AppLanguage.AR to "إذاعة ترجمة معاني القرآن الكريم باللغة الفرنسية",
            AppLanguage.EN to "Holy Quran French Meaning Translation Radio",
            AppLanguage.FA to "رادیو ترجمه فرانسوی معانی قرآن کریم",
            AppLanguage.UR to "فرانسیسی ترجمہ قرآن ریڈیو",
            AppLanguage.KY to "Француз тилиндеги Куран маанилеринин котормосу радиосу",
            AppLanguage.HA to "Rediyon FassPlugin Alkur'ani da Harshen Faransanci",
            AppLanguage.FR to "Radio Traduction des sens du Coran en Français"
        ),
        "quran_translation_urdu" to mapOf(
            AppLanguage.AR to "إذاعة ترجمة معاني القرآن الكريم باللغة الأردية",
            AppLanguage.EN to "Holy Quran Urdu Meaning Translation Radio",
            AppLanguage.FA to "رادیو ترجمه اردوی معانی قرآن کریم",
            AppLanguage.UR to "اردو ترجمہ قرآن مجید ریڈیو",
            AppLanguage.KY to "Урду тилиндеги Куран маанилеринин котормосу радиосу",
            AppLanguage.HA to "Rediyon FassPlugin Alkur'ani da Harshen Urdu",
            AppLanguage.FR to "Radio Traduction des sens du Coran en Ourdou"
        ),
        "quran_translation_persian" to mapOf(
            AppLanguage.AR to "إذاعة ترجمة معاني القرآن الكريم باللغة الفارسية",
            AppLanguage.EN to "Holy Quran Persian Meaning Translation Radio",
            AppLanguage.FA to "رادیو ترجمه فارسی معانی قرآن کریم",
            AppLanguage.UR to "فارسی ترجمہ قرآن ریڈیو",
            AppLanguage.KY to "Фарси тилиндеги Куран маанилеринин котормосу радиосу",
            AppLanguage.HA to "Rediyon FassPlugin Alkur'ani da Harshen Farisanci",
            AppLanguage.FR to "Radio Traduction des sens du Coran en Persan"
        ),
        "quran_translation_hausa" to mapOf(
            AppLanguage.AR to "إذاعة ترجمة معاني القرآن الكريم بلغة الهوسا",
            AppLanguage.EN to "Holy Quran Hausa Meaning Translation Radio",
            AppLanguage.FA to "رادیو ترجمه هوسا معانی قرآن کریم",
            AppLanguage.UR to "ہاؤسا ترجمہ قرآن ریڈیو",
            AppLanguage.KY to "Хауса тилиндеги Куран маанилеринин котормосу радиосу",
            AppLanguage.HA to "Rediyon FassPlugin Alkur'ani da Harshen Hausa",
            AppLanguage.FR to "Radio Traduction des sens du Coran en Haoussa"
        ),
        "quran_translation_kyrgyz" to mapOf(
            AppLanguage.AR to "إذاعة ترجمة معاني القرآن الكريم باللغة القيرغيزية",
            AppLanguage.EN to "Holy Quran Kyrgyz Meaning Translation Radio",
            AppLanguage.FA to "رادیو ترجمه قرقیزی معانی قرآن کریم",
            AppLanguage.UR to "کرغیز ترجمہ قرآن ریڈیو",
            AppLanguage.KY to "Кыргыз тилиндеги Ыйык Куран маанилеринин котормосу радиосу",
            AppLanguage.HA to "Rediyon FassPlugin Alkur'ani da Harshen Kyrgyz",
            AppLanguage.FR to "Radio Traduction des sens du Coran en Kirghize"
        )
    )

    // Common Reciter Translations
    private val reciterTranslations = mapOf(
        "abdulbasit_abdulsamad" to mapOf(
            AppLanguage.AR to "الشيخ عبد الباسط عبد الصمد",
            AppLanguage.EN to "Sheikh Abdul Basit Abdul Samad",
            AppLanguage.FA to "شیخ عبدالباسط عبدالصمد",
            AppLanguage.UR to "شیخ عبد الباسط عبد الصمد",
            AppLanguage.KY to "Шейх Абдулбасыт Абдуссамад",
            AppLanguage.HA to "Sheikh Abdulbasit Abdussamad",
            AppLanguage.FR to "Cheikh Abdul Basit Abdul Samad"
        ),
        "al_minshawi" to mapOf(
            AppLanguage.AR to "الشيخ محمد صديق المنشاوي",
            AppLanguage.EN to "Sheikh Mohamed Siddiq Al-Minshawi",
            AppLanguage.FA to "شیخ محمد صدیق منشاوی",
            AppLanguage.UR to "شیخ محمد صدیق المنشاوی",
            AppLanguage.KY to "Шейх Мухаммед Сыддык Аль-Миншави",
            AppLanguage.HA to "Sheikh Muhammad Siddiq Al-Minshawi",
            AppLanguage.FR to "Cheikh Mohamed Siddiq Al-Minshawi"
        ),
        "mishary_alafasy" to mapOf(
            AppLanguage.AR to "الشيخ مشاري راشد العفاسي",
            AppLanguage.EN to "Sheikh Mishary Rashid Alafasy",
            AppLanguage.FA to "شیخ مشاری راشد العفاسی",
            AppLanguage.UR to "شیخ مشاری راشد العفاسی",
            AppLanguage.KY to "Шейх Мишари Рашид Аль-Афаси",
            AppLanguage.HA to "Sheikh Mishary Rashid Alafasy",
            AppLanguage.FR to "Cheikh Mishary Rashid Alafasy"
        ),
        "mahmoud_khalil_alhusary" to mapOf(
            AppLanguage.AR to "الشيخ محمود خليل الحصري",
            AppLanguage.EN to "Sheikh Mahmoud Khalil Al-Husary",
            AppLanguage.FA to "شیخ محمود خلیل الحصری",
            AppLanguage.UR to "شیخ محمود خلیل الحصری",
            AppLanguage.KY to "Шейх Махмуд Халил Аль-Хусари",
            AppLanguage.HA to "Sheikh Mahmoud Khalil Al-Husary",
            AppLanguage.FR to "Cheikh Mahmoud Khalil Al-Hussary"
        ),
        "mustafa_ismail" to mapOf(
            AppLanguage.AR to "الشيخ مصطفى إسماعيل",
            AppLanguage.EN to "Sheikh Mustafa Ismail",
            AppLanguage.FA to "شیخ مصطفی اسماعیل",
            AppLanguage.UR to "شیخ مصطفیٰ اسماعیل",
            AppLanguage.KY to "Шейх Мустафа Исмаил",
            AppLanguage.HA to "Sheikh Mustafa Ismail",
            AppLanguage.FR to "Cheikh Moustafa Ismaïl"
        ),
        "saad_alghamdi" to mapOf(
            AppLanguage.AR to "الشيخ سعد الغامدي",
            AppLanguage.EN to "Sheikh Saad Al-Ghamdi",
            AppLanguage.FA to "شیخ سعد الغامدی",
            AppLanguage.UR to "شیخ سعد الغامدی",
            AppLanguage.KY to "Шейх Саад Аль-Гамди",
            AppLanguage.HA to "Sheikh Saad Al-Ghamdi",
            AppLanguage.FR to "Cheikh Saad Al-Ghamdi"
        ),
        "abdul_rahman_alsudais" to mapOf(
            AppLanguage.AR to "الشيخ عبد الرحمن السديس",
            AppLanguage.EN to "Sheikh Abdul Rahman Al-Sudais",
            AppLanguage.FA to "شیخ عبدالرحمن السدیس",
            AppLanguage.UR to "شیخ عبد الرحمن السدیس",
            AppLanguage.KY to "Шейх Абдурахман ас-Судейс",
            AppLanguage.HA to "Sheikh Abdul Rahman Al-Sudais",
            AppLanguage.FR to "Cheikh Abdul Rahman Al-Soudais"
        ),
        "saud_alshuraim" to mapOf(
            AppLanguage.AR to "الشيخ سعود الشريم",
            AppLanguage.EN to "Sheikh Saud Al-Shuraim",
            AppLanguage.FA to "شیخ سعود الشریم",
            AppLanguage.UR to "شیخ سعود الشریم",
            AppLanguage.KY to "Шейх Сауд аш-Шурейм",
            AppLanguage.HA to "Sheikh Saud Al-Shuraim",
            AppLanguage.FR to "Cheikh Saoud Al-Chouraym"
        ),
        "maher_almuaiqly" to mapOf(
            AppLanguage.AR to "الشيخ ماهر المعيقلي",
            AppLanguage.EN to "Sheikh Maher Al-Muaiqly",
            AppLanguage.FA to "شیخ ماهر المعیقلی",
            AppLanguage.UR to "شیخ ماہر المعیقلی",
            AppLanguage.KY to "Шейх Махер аль-Муайкли",
            AppLanguage.HA to "Sheikh Maher Al-Muaiqly",
            AppLanguage.FR to "Cheikh Maher Al-Muaiqly"
        ),
        "yasser_aldosari" to mapOf(
            AppLanguage.AR to "الشيخ ياسر الدوسري",
            AppLanguage.EN to "Sheikh Yasser Al-Dosari",
            AppLanguage.FA to "شیخ یاسر الدوسری",
            AppLanguage.UR to "شیخ یاسر الدوسری",
            AppLanguage.KY to "Шейх Ясир ад-Досари",
            AppLanguage.HA to "Sheikh Yasser Al-Dosari",
            AppLanguage.FR to "Cheikh Yasser Al-Dossari"
        ),
        "bader_alturki" to mapOf(
            AppLanguage.AR to "الشيخ بدر التركي",
            AppLanguage.EN to "Sheikh Bader Al-Turki",
            AppLanguage.FA to "شیخ بدر الترکی",
            AppLanguage.UR to "شیخ بدر الترکی",
            AppLanguage.KY to "Шейх Бадер Аль-Турки",
            AppLanguage.HA to "Sheikh Bader Al-Turki",
            AppLanguage.FR to "Cheikh Bader Al-Turki"
        ),
        "bandar_baleela" to mapOf(
            AppLanguage.AR to "الشيخ بندر بليلة",
            AppLanguage.EN to "Sheikh Bandar Baleela",
            AppLanguage.FA to "شیخ بندر بليله",
            AppLanguage.UR to "شیخ بندر بلیلۃ",
            AppLanguage.KY to "Шейх Бандар Балила",
            AppLanguage.HA to "Sheikh Bandar Baleela",
            AppLanguage.FR to "Cheikh Bandar Baleela"
        ),
        "nasser_alqatami" to mapOf(
            AppLanguage.AR to "الشيخ ناصر القطامي",
            AppLanguage.EN to "Sheikh Nasser Al-Qatami",
            AppLanguage.FA to "شیخ ناصر القطامی",
            AppLanguage.UR to "شیخ ناصر القطامی",
            AppLanguage.KY to "Шейх Насер Аль-Катами",
            AppLanguage.HA to "Sheikh Nasser Al-Qatami",
            AppLanguage.FR to "Cheikh Nasser Al-Qatami"
        ),
        "ali_jaber" to mapOf(
            AppLanguage.AR to "الشيخ علي عبد الله جابر",
            AppLanguage.EN to "Sheikh Ali Jaber",
            AppLanguage.FA to "شیخ علی جابر",
            AppLanguage.UR to "شیخ علی عبد اللہ جابر",
            AppLanguage.KY to "Шейх Али Жабер",
            AppLanguage.HA to "Sheikh Ali Jaber",
            AppLanguage.FR to "Cheikh Ali Jaber"
        ),
        "mohammed_ayyub" to mapOf(
            AppLanguage.AR to "الشيخ محمد أيوب",
            AppLanguage.EN to "Sheikh Muhammad Ayyub",
            AppLanguage.FA to "شیخ محمد ایوب",
            AppLanguage.UR to "شیخ محمد ایوب",
            AppLanguage.KY to "Шейх Мухаммед Аюб",
            AppLanguage.HA to "Sheikh Muhammad Ayyub",
            AppLanguage.FR to "Cheikh Muhammad Ayyub"
        ),
        "abu_bakr_alshatri" to mapOf(
            AppLanguage.AR to "الشيخ أبو بكر الشاطري",
            AppLanguage.EN to "Sheikh Abu Bakr Al-Shatri",
            AppLanguage.FA to "شیخ ابوبکر الشاطری",
            AppLanguage.UR to "شیخ ابو بکر الشاطری",
            AppLanguage.KY to "Шейх Абу Бакр аш-Шатри",
            AppLanguage.HA to "Sheikh Abu Bakr Al-Shatri",
            AppLanguage.FR to "Cheikh Abou Bakr Al-Shatri"
        ),
        "ahmed_alajmy" to mapOf(
            AppLanguage.AR to "الشيخ أحمد بن علي العجمي",
            AppLanguage.EN to "Sheikh Ahmed Al-Ajmy",
            AppLanguage.FA to "شیخ احمد العجمی",
            AppLanguage.UR to "شیخ احمد بن علی العجمی",
            AppLanguage.KY to "Шейх Ахмед Аль-Ажми",
            AppLanguage.HA to "Sheikh Ahmed Al-Ajmy",
            AppLanguage.FR to "Cheikh Ahmed Al-Ajmi"
        ),
        "fares_abbad" to mapOf(
            AppLanguage.AR to "الشيخ فارس عباد",
            AppLanguage.EN to "Sheikh Fares Abbad",
            AppLanguage.FA to "شیخ فارس عباد",
            AppLanguage.UR to "شیخ فارس عباد",
            AppLanguage.KY to "Шейх Фарис Аббад",
            AppLanguage.HA to "Sheikh Fares Abbad",
            AppLanguage.FR to "Cheikh Fares Abbad"
        ),
        "idrees_abkar" to mapOf(
            AppLanguage.AR to "الشيخ إدريس أبكر",
            AppLanguage.EN to "Sheikh Idrees Abkar",
            AppLanguage.FA to "شیخ ادریس ابکر",
            AppLanguage.UR to "شیخ ادریس ابکر",
            AppLanguage.KY to "Шейх Идрис Абкар",
            AppLanguage.HA to "Sheikh Idrees Abkar",
            AppLanguage.FR to "Cheikh Idrees Abkar"
        ),
        "khalid_aljalil" to mapOf(
            AppLanguage.AR to "الشيخ خالد الجليل",
            AppLanguage.EN to "Sheikh Khalid Al-Jalil",
            AppLanguage.FA to "شیخ خالد الجلیل",
            AppLanguage.UR to "شیخ خالد الجلیل",
            AppLanguage.KY to "Шейх Халид Аль-Жалил",
            AppLanguage.HA to "Sheikh Khalid Al-Jalil",
            AppLanguage.FR to "Cheikh Khalid Al-Jalil"
        ),
        "ali_alhudhaify" to mapOf(
            AppLanguage.AR to "الشيخ علي بن عبد الرحمن الحذيفي",
            AppLanguage.EN to "Sheikh Ali Al-Hudhaify",
            AppLanguage.FA to "شیخ علی الحذیفی",
            AppLanguage.UR to "شیخ علی بن عبد الرحمن الحذیفی",
            AppLanguage.KY to "Шейх Али Аль-Хузейфи",
            AppLanguage.HA to "Sheikh Ali Al-Hudhaify",
            AppLanguage.FR to "Cheikh Ali Al-Houdhaïfi"
        ),
        "abdulhadi_kanakeri" to mapOf(
            AppLanguage.AR to "الشيخ عبد الهادي كناكري",
            AppLanguage.EN to "Sheikh Abdulhadi Kanakeri",
            AppLanguage.FA to "شیخ عبدالهادی کناکري",
            AppLanguage.UR to "شیخ عبد الہادی کناکري",
            AppLanguage.KY to "Шейх Абдулхади Канакери",
            AppLanguage.HA to "Sheikh Abdulhadi Kanakeri",
            AppLanguage.FR to "Cheikh Abdulhadi Kanakeri"
        ),
        "abdulrashid_sufi" to mapOf(
            AppLanguage.AR to "الشيخ عبد الرشيد صوفي",
            AppLanguage.EN to "Sheikh Abdul Rashid Sufi",
            AppLanguage.FA to "شیخ عبدالرشید صوفی",
            AppLanguage.UR to "شیخ عبد الرشید صوفی",
            AppLanguage.KY to "Шейх Абдурашид Суфи",
            AppLanguage.HA to "Sheikh Abdul Rashid Sufi",
            AppLanguage.FR to "Cheikh Abdul Rashid Soufi"
        ),
        "muhammad_jibreel" to mapOf(
            AppLanguage.AR to "الشيخ محمد جبريل",
            AppLanguage.EN to "Sheikh Muhammad Jibreel",
            AppLanguage.FA to "شیخ محمد جبریل",
            AppLanguage.UR to "شیخ محمد جبریل",
            AppLanguage.KY to "Шейх Мухаммед Жибрил",
            AppLanguage.HA to "Sheikh Muhammad Jibreel",
            AppLanguage.FR to "Cheikh Muhammad Jibreel"
        ),
        "mohamed_al_tablawi" to mapOf(
            AppLanguage.AR to "الشيخ محمد محمود الطبلاوي",
            AppLanguage.EN to "Sheikh Mohamed Mahmoud Al-Tablawi",
            AppLanguage.FA to "شیخ محمد محمود طبلاوی",
            AppLanguage.UR to "شیخ محمد محمود الطبلاوی",
            AppLanguage.KY to "Шейх Мухаммед Махмуд Ат-Таблави",
            AppLanguage.HA to "Sheikh Mohamed Al-Tablawi",
            AppLanguage.FR to "Cheikh Mohamed Mahmoud Al-Tablawi"
        ),
        "khalifa_altunaiji" to mapOf(
            AppLanguage.AR to "الشيخ خليفة الطنيجي",
            AppLanguage.EN to "Sheikh Khalifa Al-Tunaiji",
            AppLanguage.FA to "شیخ خلیفه الطنیجی",
            AppLanguage.UR to "شیخ خلیفہ الطنیجی",
            AppLanguage.KY to "Шейх Халифа Аль-Тунейжи",
            AppLanguage.HA to "Sheikh Khalifa Al-Tunaiji",
            AppLanguage.FR to "Cheikh Khalifa Al-Tunaiji"
        ),
        "mahmoud_ali_albanna" to mapOf(
            AppLanguage.AR to "الشيخ محمود علي البنا",
            AppLanguage.EN to "Sheikh Mahmoud Ali Al-Banna",
            AppLanguage.FA to "شیخ محمود علی البنا",
            AppLanguage.UR to "شیخ محمود علی البنا",
            AppLanguage.KY to "Шейх Махмуд Али Аль-Банна",
            AppLanguage.HA to "Sheikh Mahmoud Ali Al-Banna",
            AppLanguage.FR to "Cheikh Mahmoud Ali Al-Banna"
        ),
        "al_oyoun_al_koushi" to mapOf(
            AppLanguage.AR to "الشيخ العيون الكوشي",
            AppLanguage.EN to "Sheikh Al-Oyoun Al-Koushi",
            AppLanguage.FA to "شیخ العیون الکوشی",
            AppLanguage.UR to "شیخ العیون الکوشی",
            AppLanguage.KY to "Шейх Аль-Уюн Аль-Куши",
            AppLanguage.HA to "Sheikh Al-Oyoun Al-Koushi",
            AppLanguage.FR to "Cheikh Al-Oyoun Al-Kouchi"
        )
    )

    // Country Translations
    private val countryTranslations = mapOf(
        "EG" to mapOf(
            AppLanguage.AR to "مصر",
            AppLanguage.EN to "Egypt",
            AppLanguage.FA to "مصر",
            AppLanguage.UR to "مصر",
            AppLanguage.KY to "Египет",
            AppLanguage.HA to "Misra",
            AppLanguage.FR to "Égypte"
        ),
        "SA" to mapOf(
            AppLanguage.AR to "المملكة العربية السعودية",
            AppLanguage.EN to "Saudi Arabia",
            AppLanguage.FA to "عربستان سعودی",
            AppLanguage.UR to "سعودی عرب",
            AppLanguage.KY to "Сауд Арабиясы",
            AppLanguage.HA to "Kasar Makka (Saudi Arabia)",
            AppLanguage.FR to "Arabie Saoudite"
        ),
        "AE" to mapOf(
            AppLanguage.AR to "الإمارات العربية المتحدة",
            AppLanguage.EN to "United Arab Emirates",
            AppLanguage.FA to "امارات متحده عربی",
            AppLanguage.UR to "متحدہ عرب امارات",
            AppLanguage.KY to "Бириккен Араб Эмираттары",
            AppLanguage.HA to "Hadaddiyar Daular Larabawa",
            AppLanguage.FR to "Émirats Arabes Unis"
        ),
        "KW" to mapOf(
            AppLanguage.AR to "الكويت",
            AppLanguage.EN to "Kuwait",
            AppLanguage.FA to "کویت",
            AppLanguage.UR to "کویت",
            AppLanguage.KY to "Кувейт",
            AppLanguage.HA to "Kuwait",
            AppLanguage.FR to "Koweït"
        ),
        "QA" to mapOf(
            AppLanguage.AR to "قطر",
            AppLanguage.EN to "Qatar",
            AppLanguage.FA to "قطر",
            AppLanguage.UR to "قطر",
            AppLanguage.KY to "Катар",
            AppLanguage.HA to "Katar",
            AppLanguage.FR to "Qatar"
        ),
        "BH" to mapOf(
            AppLanguage.AR to "البحرين",
            AppLanguage.EN to "Bahrain",
            AppLanguage.FA to "بحرین",
            AppLanguage.UR to "بحرین",
            AppLanguage.KY to "Бахрейн",
            AppLanguage.HA to "Bahrain",
            AppLanguage.FR to "Bahreïn"
        ),
        "OM" to mapOf(
            AppLanguage.AR to "سلطنة عمان",
            AppLanguage.EN to "Oman",
            AppLanguage.FA to "عمان",
            AppLanguage.UR to "عمان",
            AppLanguage.KY to "Оман",
            AppLanguage.HA to "Oman",
            AppLanguage.FR to "Oman"
        ),
        "JO" to mapOf(
            AppLanguage.AR to "الأردن",
            AppLanguage.EN to "Jordan",
            AppLanguage.FA to "اردن",
            AppLanguage.UR to "اردن",
            AppLanguage.KY to "Иордания",
            AppLanguage.HA to "Jordan",
            AppLanguage.FR to "Jordanie"
        ),
        "PS" to mapOf(
            AppLanguage.AR to "فلسطين",
            AppLanguage.EN to "Palestine",
            AppLanguage.FA to "فلسطین",
            AppLanguage.UR to "فلسطین",
            AppLanguage.KY to "Палестина",
            AppLanguage.HA to "Palasdinu",
            AppLanguage.FR to "Palestine"
        ),
        "DZ" to mapOf(
            AppLanguage.AR to "الجزائر",
            AppLanguage.EN to "Algeria",
            AppLanguage.FA to "الجزایر",
            AppLanguage.UR to "الجزائر",
            AppLanguage.KY to "Алжир",
            AppLanguage.HA to "Aljeriya",
            AppLanguage.FR to "Algérie"
        ),
        "MA" to mapOf(
            AppLanguage.AR to "المغرب",
            AppLanguage.EN to "Morocco",
            AppLanguage.FA to "مراکش",
            AppLanguage.UR to "مراکش",
            AppLanguage.KY to "Марокко",
            AppLanguage.HA to "Maroko",
            AppLanguage.FR to "Maroc"
        ),
        "TN" to mapOf(
            AppLanguage.AR to "تونس",
            AppLanguage.EN to "Tunisia",
            AppLanguage.FA to "تونس",
            AppLanguage.UR to "تیونس",
            AppLanguage.KY to "Тунис",
            AppLanguage.HA to "Tunisiya",
            AppLanguage.FR to "Tunisie"
        ),
        "LY" to mapOf(
            AppLanguage.AR to "ليبيا",
            AppLanguage.EN to "Libya",
            AppLanguage.FA to "لیبی",
            AppLanguage.UR to "لیبیا",
            AppLanguage.KY to "Ливия",
            AppLanguage.HA to "Libiya",
            AppLanguage.FR to "Libye"
        ),
        "SD" to mapOf(
            AppLanguage.AR to "السودان",
            AppLanguage.EN to "Sudan",
            AppLanguage.FA to "سودان",
            AppLanguage.UR to "سوڈان",
            AppLanguage.KY to "Судан",
            AppLanguage.HA to "Sudan",
            AppLanguage.FR to "Soudan"
        ),
        "YE" to mapOf(
            AppLanguage.AR to "اليمن",
            AppLanguage.EN to "Yemen",
            AppLanguage.FA to "یمن",
            AppLanguage.UR to "یمن",
            AppLanguage.KY to "Йемен",
            AppLanguage.HA to "Yaman",
            AppLanguage.FR to "Yémen"
        ),
        "SY" to mapOf(
            AppLanguage.AR to "سوريا",
            AppLanguage.EN to "Syria",
            AppLanguage.FA to "سوریه",
            AppLanguage.UR to "شام",
            AppLanguage.KY to "Сирия",
            AppLanguage.HA to "Siriya",
            AppLanguage.FR to "Syrie"
        ),
        "LB" to mapOf(
            AppLanguage.AR to "لبنان",
            AppLanguage.EN to "Lebanon",
            AppLanguage.FA to "لبنان",
            AppLanguage.UR to "لبنان",
            AppLanguage.KY to "Ливан",
            AppLanguage.HA to "Lebanon",
            AppLanguage.FR to "Liban"
        ),
        "IQ" to mapOf(
            AppLanguage.AR to "العراق",
            AppLanguage.EN to "Iraq",
            AppLanguage.FA to "عراق",
            AppLanguage.UR to "عراق",
            AppLanguage.KY to "Ирак",
            AppLanguage.HA to "Iraki",
            AppLanguage.FR to "Irak"
        ),
        "SO" to mapOf(
            AppLanguage.AR to "الصومال",
            AppLanguage.EN to "Somalia",
            AppLanguage.FA to "سومالی",
            AppLanguage.UR to "صومالیہ",
            AppLanguage.KY to "Сомали",
            AppLanguage.HA to "Somaliya",
            AppLanguage.FR to "Somalie"
        ),
        "TR" to mapOf(
            AppLanguage.AR to "تركيا",
            AppLanguage.EN to "Turkey",
            AppLanguage.FA to "ترکیه",
            AppLanguage.UR to "ترکی",
            AppLanguage.KY to "Түркия",
            AppLanguage.HA to "Turkiyya",
            AppLanguage.FR to "Turquie"
        ),
        "MY" to mapOf(
            AppLanguage.AR to "ماليزيا",
            AppLanguage.EN to "Malaysia",
            AppLanguage.FA to "مالزی",
            AppLanguage.UR to "ملائیشیا",
            AppLanguage.KY to "Малайзия",
            AppLanguage.HA to "Malesiya",
            AppLanguage.FR to "Malaisie"
        ),
        "ID" to mapOf(
            AppLanguage.AR to "إندونيسيا",
            AppLanguage.EN to "Indonesia",
            AppLanguage.FA to "اندونزی",
            AppLanguage.UR to "انڈونیشیا",
            AppLanguage.KY to "Индонезия",
            AppLanguage.HA to "Indonisiya",
            AppLanguage.FR to "Indonésie"
        )
    )

    // City translations
    private val cityTranslations = mapOf(
        "القاهرة" to mapOf(
            AppLanguage.AR to "القاهرة",
            AppLanguage.EN to "Cairo",
            AppLanguage.FA to "قاهره",
            AppLanguage.UR to "قاہرہ",
            AppLanguage.KY to "Каир",
            AppLanguage.HA to "Alkahira",
            AppLanguage.FR to "Le Caire"
        ),
        "مكة المكرمة" to mapOf(
            AppLanguage.AR to "مكة المكرمة",
            AppLanguage.EN to "Makkah Al-Mukarramah",
            AppLanguage.FA to "مکه مکرمه",
            AppLanguage.UR to "مکہ مکرمہ",
            AppLanguage.KY to "Ыйык Мекке",
            AppLanguage.HA to "Makkah Al-Mukarramah",
            AppLanguage.FR to "La Mecque"
        ),
        "المدينة المنورة" to mapOf(
            AppLanguage.AR to "المدينة المنورة",
            AppLanguage.EN to "Madinah Al-Munawwarah",
            AppLanguage.FA to "مدینه منوره",
            AppLanguage.UR to "مدینہ منورہ",
            AppLanguage.KY to "Мадина Мунаввара",
            AppLanguage.HA to "Madina Al-Munawwarah",
            AppLanguage.FR to "Médine"
        ),
        "الرياض" to mapOf(
            AppLanguage.AR to "الرياض",
            AppLanguage.EN to "Riyadh",
            AppLanguage.FA to "ریاض",
            AppLanguage.UR to "ریاض",
            AppLanguage.KY to "Эр-Рияд",
            AppLanguage.HA to "Riyadh",
            AppLanguage.FR to "Riyad"
        ),
        "أبوظبي" to mapOf(
            AppLanguage.AR to "أبوظبي",
            AppLanguage.EN to "Abu Dhabi",
            AppLanguage.FA to "ابوظبی",
            AppLanguage.UR to "ابوظہبی",
            AppLanguage.KY to "Абу-Даби",
            AppLanguage.HA to "Abu Dhabi",
            AppLanguage.FR to "Abou Dabi"
        ),
        "دبي" to mapOf(
            AppLanguage.AR to "دبي",
            AppLanguage.EN to "Dubai",
            AppLanguage.FA to "دبی",
            AppLanguage.UR to "دبئی",
            AppLanguage.KY to "Дубай",
            AppLanguage.HA to "Dubai",
            AppLanguage.FR to "Dubaï"
        ),
        "الدوحة" to mapOf(
            AppLanguage.AR to "الدوحة",
            AppLanguage.EN to "Doha",
            AppLanguage.FA to "دوحه",
            AppLanguage.UR to "دوحہ",
            AppLanguage.KY to "Доха",
            AppLanguage.HA to "Doha",
            AppLanguage.FR to "Doha"
        ),
        "الكويت" to mapOf(
            AppLanguage.AR to "الكويت",
            AppLanguage.EN to "Kuwait City",
            AppLanguage.FA to "کویت",
            AppLanguage.UR to "کویت سٹی",
            AppLanguage.KY to "Кувейт",
            AppLanguage.HA to "Birnin Kuwait",
            AppLanguage.FR to "Koweït"
        ),
        "المنامة" to mapOf(
            AppLanguage.AR to "المنامة",
            AppLanguage.EN to "Manama",
            AppLanguage.FA to "منامه",
            AppLanguage.UR to "منامہ",
            AppLanguage.KY to "Манама",
            AppLanguage.HA to "Manama",
            AppLanguage.FR to "Manama"
        ),
        "مسقط" to mapOf(
            AppLanguage.AR to "مسقط",
            AppLanguage.EN to "Muscat",
            AppLanguage.FA to "مسقط",
            AppLanguage.UR to "مسقط",
            AppLanguage.KY to "Маскат",
            AppLanguage.HA to "Muscat",
            AppLanguage.FR to "Mascate"
        ),
        "عمان" to mapOf(
            AppLanguage.AR to "عمان",
            AppLanguage.EN to "Amman",
            AppLanguage.FA to "امان",
            AppLanguage.UR to "عمان",
            AppLanguage.KY to "Амман",
            AppLanguage.HA to "Amman",
            AppLanguage.FR to "Amman"
        ),
        "القدس" to mapOf(
            AppLanguage.AR to "القدس الشريف",
            AppLanguage.EN to "Al-Quds (Jerusalem)",
            AppLanguage.FA to "قدس شریف",
            AppLanguage.UR to "القدس الشریف",
            AppLanguage.KY to "Ыйык Куддус",
            AppLanguage.HA to "Kudus",
            AppLanguage.FR to "Jérusalem (Al-Qods)"
        ),
        "الجزائر" to mapOf(
            AppLanguage.AR to "الجزائر",
            AppLanguage.EN to "Algiers",
            AppLanguage.FA to "الجزیره",
            AppLanguage.UR to "الجزائر",
            AppLanguage.KY to "Алжир",
            AppLanguage.HA to "Aljeriya",
            AppLanguage.FR to "Alger"
        ),
        "الرباط" to mapOf(
            AppLanguage.AR to "الرباط",
            AppLanguage.EN to "Rabat",
            AppLanguage.FA to "رباط",
            AppLanguage.UR to "رباط",
            AppLanguage.KY to "Рабат",
            AppLanguage.HA to "Rabat",
            AppLanguage.FR to "Rabat"
        ),
        "تونس" to mapOf(
            AppLanguage.AR to "تونس",
            AppLanguage.EN to "Tunis",
            AppLanguage.FA to "تونس",
            AppLanguage.UR to "تیونس",
            AppLanguage.KY to "Тунис",
            AppLanguage.HA to "Tunis",
            AppLanguage.FR to "Tunis"
        ),
        "طرابلس" to mapOf(
            AppLanguage.AR to "طرابلس",
            AppLanguage.EN to "Tripoli",
            AppLanguage.FA to "طرابلس",
            AppLanguage.UR to "طرابلس",
            AppLanguage.KY to "Триполи",
            AppLanguage.HA to "Tripoli",
            AppLanguage.FR to "Tripoli"
        ),
        "الخرطوم" to mapOf(
            AppLanguage.AR to "الخرطوم",
            AppLanguage.EN to "Khartoum",
            AppLanguage.FA to "خارطوم",
            AppLanguage.UR to "خرطوم",
            AppLanguage.KY to "Хартум",
            AppLanguage.HA to "Khartoum",
            AppLanguage.FR to "Khartoum"
        ),
        "صنعاء" to mapOf(
            AppLanguage.AR to "صنعاء",
            AppLanguage.EN to "Sana'a",
            AppLanguage.FA to "صنعا",
            AppLanguage.UR to "صنعاء",
            AppLanguage.KY to "Сана",
            AppLanguage.HA to "Sana'a",
            AppLanguage.FR to "Sanaa"
        ),
        "دمشق" to mapOf(
            AppLanguage.AR to "دمشق",
            AppLanguage.EN to "Damascus",
            AppLanguage.FA to "دمشق",
            AppLanguage.UR to "دمشق",
            AppLanguage.KY to "Дамаск",
            AppLanguage.HA to "Damascus",
            AppLanguage.FR to "Damas"
        ),
        "بيروت" to mapOf(
            AppLanguage.AR to "بيروت",
            AppLanguage.EN to "Beirut",
            AppLanguage.FA to "بیروت",
            AppLanguage.UR to "بیروت",
            AppLanguage.KY to "Бейрут",
            AppLanguage.HA to "Beirut",
            AppLanguage.FR to "Beyrouth"
        ),
        "بغداد" to mapOf(
            AppLanguage.AR to "بغداد",
            AppLanguage.EN to "Baghdad",
            AppLanguage.FA to "بغداد",
            AppLanguage.UR to "بغداد",
            AppLanguage.KY to "Багдад",
            AppLanguage.HA to "Baghdad",
            AppLanguage.FR to "Bagdad"
        ),
        "إسطنبول" to mapOf(
            AppLanguage.AR to "إسطنبول",
            AppLanguage.EN to "Istanbul",
            AppLanguage.FA to "استانبول",
            AppLanguage.UR to "استنبول",
            AppLanguage.KY to "Стамбул",
            AppLanguage.HA to "Istanbul",
            AppLanguage.FR to "Istanbul"
        ),
        "كوالالمبور" to mapOf(
            AppLanguage.AR to "كوالالمبور",
            AppLanguage.EN to "Kuala Lumpur",
            AppLanguage.FA to "کوالالامپور",
            AppLanguage.UR to "کوالالمپور",
            AppLanguage.KY to "Куала-Лумпур",
            AppLanguage.HA to "Kuala Lumpur",
            AppLanguage.FR to "Kuala Lumpur"
        ),
        "جاكرتا" to mapOf(
            AppLanguage.AR to "جاكرتا",
            AppLanguage.EN to "Jakarta",
            AppLanguage.FA to "جاکارتا",
            AppLanguage.UR to "جکارتہ",
            AppLanguage.KY to "Жакарта",
            AppLanguage.HA to "Jakarta",
            AppLanguage.FR to "Jakarta"
        )
    )

    /**
     * Translates a radio station's display name into the given app language.
     */
    fun getStationName(station: RadioStation, language: AppLanguage): String {
        if (language == AppLanguage.AR) {
            return station.nameAr.ifBlank { station.name }
        }

        // 1. Direct ID match
        stationTranslationMap[station.id]?.get(language)?.let { return it }

        // 2. If reciter station: "Sheikh [Reciter Name] Radio"
        val reciterId = station.reciterId
        if (!reciterId.isNullOrBlank()) {
            val translatedReciter = getReciterName(id = reciterId, nameAr = station.reciter ?: "", nameEn = "", language = language)
            return when (language) {
                AppLanguage.AR -> "إذاعة $translatedReciter"
                AppLanguage.EN -> "$translatedReciter Radio"
                AppLanguage.FR -> "Radio $translatedReciter"
                AppLanguage.IT -> "Radio $translatedReciter"
                AppLanguage.JA -> "$translatedReciter ラジオ"
                AppLanguage.DE -> "Radio $translatedReciter"
                AppLanguage.RU -> "Радио $translatedReciter"
                AppLanguage.ZH -> "$translatedReciter 电台"
                AppLanguage.FA -> "رادیو $translatedReciter"
                AppLanguage.UR -> "ریڈیو $translatedReciter"
                AppLanguage.HA -> "Rediyon $translatedReciter"
                AppLanguage.KY -> "$translatedReciter радиосу"
            }
        }

        // 3. Match by name or nameEn
        if (station.nameEn.isNotBlank() && (language == AppLanguage.EN || language == AppLanguage.FR || language == AppLanguage.IT || language == AppLanguage.DE)) {
            return station.nameEn
        }

        // 4. Fallback pattern matching
        return translateStationNameByPattern(station.name, language)
    }

    /**
     * Translates a country's name into the given app language.
     */
    fun getCountryName(country: Country, language: AppLanguage): String {
        return getCountryName(country.code, country.nameAr, country.nameEn, language)
    }

    fun getCountryName(code: String, fallbackAr: String, fallbackEn: String = "", language: AppLanguage): String {
        if (language == AppLanguage.AR) return fallbackAr
        countryTranslations[code.uppercase()]?.get(language)?.let { return it }
        if (language == AppLanguage.EN && fallbackEn.isNotBlank()) return fallbackEn
        return if (fallbackEn.isNotBlank()) fallbackEn else fallbackAr
    }

    /**
     * Translates a reciter's name into the given app language.
     */
    fun getReciterName(reciter: Reciter, language: AppLanguage): String {
        return getReciterName(reciter.id, reciter.nameAr, reciter.nameEn, language)
    }

    fun getReciterName(id: String?, nameAr: String, nameEn: String = "", language: AppLanguage): String {
        if (language == AppLanguage.AR) return nameAr
        if (!id.isNullOrBlank()) {
            reciterTranslations[id]?.get(language)?.let { return it }
        }
        // Match by standard arabic name
        reciterTranslations.values.firstOrNull { it[AppLanguage.AR] == nameAr }?.get(language)?.let { return it }

        if (nameEn.isNotBlank()) return nameEn
        return nameAr
    }

    /**
     * Translates a city name into the given app language.
     */
    fun getCityName(cityAr: String, language: AppLanguage): String {
        if (cityAr.isBlank() || language == AppLanguage.AR) return cityAr
        cityTranslations[cityAr]?.get(language)?.let { return it }
        return cityAr
    }

    /**
     * Translates reciter recitation style into the target language.
     */
    fun getReciterStyle(styleAr: String, language: AppLanguage): String {
        val key = when {
            styleAr.contains("مرتل") -> StringKey.RECITERS_STYLE_MURATTAL
            styleAr.contains("مجود") -> StringKey.RECITERS_STYLE_MUJAWWAD
            styleAr.contains("الحرم") || styleAr.contains("الحرمين") -> StringKey.RECITERS_STYLE_HARAMAIN
            else -> null
        }
        return if (key != null) AppStrings.get(key, language) else styleAr
    }

    /**
     * Translates reciter biography.
     */
    fun getReciterBio(reciter: Reciter, language: AppLanguage): String {
        if (language == AppLanguage.AR) return reciter.bioAr
        return when (language) {
            AppLanguage.AR -> reciter.bioAr
            AppLanguage.EN -> "Prominent and accredited Quran reciter broadcasted across international Quran radios."
            AppLanguage.FR -> "Récitateur émérite du Saint Coran diffusé sur les stations de radio coraniques mondiales."
            AppLanguage.IT -> "Eminente recitatore del Sacro Corano trasmesso sulle radio coraniche internazionali."
            AppLanguage.JA -> "国際的なクルアーンラジオ局で放送されている高名なクルアーン読誦者。"
            AppLanguage.DE -> "Bedeutender Koran-Rezitator, dessen Rezitationen auf weltweiten Koran-Radiosendern ausgestrahlt werden."
            AppLanguage.RU -> "Известный чтец Священного Корана, транслируемый на международных исламских радиостанциях."
            AppLanguage.ZH -> "著名古兰经诵读家，在各大国际古兰经广播电台播出。"
            AppLanguage.FA -> "قاری برجسته و معتبر قرآن کریم با پخش زنده در رادیوهای بین‌المللی قرآن."
            AppLanguage.UR -> "قرآن پاک کے ممتاز اور مستند قاری، جن کی تلاوت عالمی ریڈیوز پر نشر ہوتی ہے۔"
            AppLanguage.HA -> "Fitaccen mai karatun Alkur'ani mai girma wanda ake watsawa a gidajen rediyon duniya."
            AppLanguage.KY -> "Дүйнөлүк Куран радиолорунда уктурулган белгилүү жана таанымал Куран каарысы."
        }
    }

    private fun translateStationNameByPattern(name: String, language: AppLanguage): String {
        // Simple prefix translation helper
        var result = name
        when (language) {
            AppLanguage.EN -> {
                result = result.replace("إذاعة القرآن الكريم من", "Holy Quran Radio from")
                result = result.replace("إذاعة تلاوات", "Recitations Radio -")
                result = result.replace("إذاعة", "Radio")
                result = result.replace("بث مباشر", "Live")
                result = result.replace("الشيخ", "Sheikh")
            }
            AppLanguage.FA -> {
                result = result.replace("إذاعة القرآن الكريم من", "رادیو قرآن کریم از")
                result = result.replace("إذاعة تلاوات", "رادیو تلاوت‌های")
                result = result.replace("إذاعة", "رادیو")
                result = result.replace("بث مباشر", "پخش زنده")
                result = result.replace("الشيخ", "شیخ")
            }
            AppLanguage.UR -> {
                result = result.replace("إذاعة القرآن الكريم من", "قرآن کریم ریڈیو از")
                result = result.replace("إذاعة تلاوات", "تلاوتوں کا ریڈیو")
                result = result.replace("إذاعة", "ریڈیو")
                result = result.replace("بث مباشر", "لائیو")
                result = result.replace("الشيخ", "شیخ")
            }
            AppLanguage.KY -> {
                result = result.replace("إذاعة القرآن الكريم من", "Куран радиосу:")
                result = result.replace("إذاعة تلاوات", "Кыраат радиосу:")
                result = result.replace("إذاعة", "Радио")
                result = result.replace("بث مباشر", "Түз эфир")
                result = result.replace("الشيخ", "Шейх")
            }
            AppLanguage.HA -> {
                result = result.replace("إذاعة القرآن الكريم من", "Rediyon Alkur'ani daga")
                result = result.replace("إذاعة تلاوات", "Rediyon Karatuttukan")
                result = result.replace("إذاعة", "Rediyon")
                result = result.replace("بث مباشر", "Kai Tsaye")
                result = result.replace("الشيخ", "Sheikh")
            }
            AppLanguage.FR -> {
                result = result.replace("إذاعة القرآن الكريم من", "Radio du Saint Coran de")
                result = result.replace("إذاعة تلاوات", "Radio Récitations -")
                result = result.replace("إذاعة", "Radio")
                result = result.replace("بث مباشر", "En direct")
                result = result.replace("الشيخ", "Cheikh")
            }
            AppLanguage.IT -> {
                result = result.replace("إذاعة القرآن الكريم من", "Radio del Sacro Corano da")
                result = result.replace("إذاعة تلاوات", "Radio Recitazioni -")
                result = result.replace("إذاعة", "Radio")
                result = result.replace("بث مباشر", "In diretta")
                result = result.replace("الشيخ", "Sceicco")
            }
            AppLanguage.JA -> {
                result = result.replace("إذاعة القرآن الكريم من", "聖クルアーンラジオ：")
                result = result.replace("إذاعة تلاوات", "読誦ラジオ：")
                result = result.replace("إذاعة", "ラジオ")
                result = result.replace("بث مباشر", "ライブ")
                result = result.replace("الشيخ", "シェイク")
            }
            AppLanguage.DE -> {
                result = result.replace("إذاعة القرآن الكريم من", "Heiliger Koran Radio aus")
                result = result.replace("إذاعة تلاوات", "Rezitationen Radio -")
                result = result.replace("إذاعة", "Radio")
                result = result.replace("بث مباشر", "Live")
                result = result.replace("الشيخ", "Scheich")
            }
            AppLanguage.RU -> {
                result = result.replace("إذاعة القرآن الكريم من", "Радио Священного Корана из")
                result = result.replace("إذاعة تلاوات", "Радио чтений -")
                result = result.replace("إذاعة", "Радио")
                result = result.replace("بث مباشر", "Прямой эфир")
                result = result.replace("الشيخ", "Шейх")
            }
            AppLanguage.ZH -> {
                result = result.replace("إذاعة القرآن الكريم من", "古兰经广播电台：")
                result = result.replace("إذاعة تلاوات", "诵读广播电台：")
                result = result.replace("إذاعة", "广播电台")
                result = result.replace("بث مباشر", "直播")
                result = result.replace("الشيخ", "谢赫")
            }
            AppLanguage.AR -> {}
        }
        return result
    }
}
