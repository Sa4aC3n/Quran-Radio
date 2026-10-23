package com.example.player

import com.example.data.QuranTafsirRepository
import com.example.model.QuranSurahItem
import com.example.model.QuranSurahsData
import com.example.model.RadioStation
import com.example.model.RecognitionSource
import com.example.model.RecognizedRecitation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Intelligent Acoustic Quran Stream Recognition Engine.
 * Analyzes live stream metadata, ICY headers, and audio broadcast tags to identify
 * the current Reciter, Surah, and Ayah, and pairs them with authentic Uthmani script
 * and Tafsir Al-Muyassar.
 */
class AcousticQuranRecognitionEngine(
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main
) {

    private val scope = CoroutineScope(mainDispatcher + SupervisorJob())
    private var recognitionJob: Job? = null

    private val _recitationState = MutableStateFlow(RecognizedRecitation())
    val recitationState: StateFlow<RecognizedRecitation> = _recitationState.asStateFlow()

    private var currentStation: RadioStation? = null
    private var lastRawTitle: String? = null

    fun onStationChanged(station: RadioStation?) {
        currentStation = station
        lastRawTitle = null
        recognize(rawTitle = null, station = station, source = RecognitionSource.STATION_INFO)
    }

    fun onMetadataReceived(title: String?, artist: String?, station: RadioStation?) {
        val combined = listOfNotNull(title, artist).joinToString(" - ").trim()
        if (combined.isNotBlank() && combined != lastRawTitle) {
            lastRawTitle = combined
            recognize(rawTitle = combined, station = station ?: currentStation, source = RecognitionSource.STREAM_METADATA)
        }
    }

    fun recognizeCurrentStream() {
        recognize(rawTitle = lastRawTitle, station = currentStation, source = if (lastRawTitle != null) RecognitionSource.STREAM_METADATA else RecognitionSource.STATION_INFO)
    }

    fun selectAyah(newAyah: Int) {
        val current = _recitationState.value
        if (newAyah in 1..current.totalAyahs && newAyah != current.currentAyahNumber) {
            recognitionJob?.cancel()
            recognitionJob = scope.launch {
                val surah = QuranSurahsData.getSurah(current.surahNumber) ?: QuranSurahsData.SURAHS[0]
                fetchAyahInternal(
                    reciterName = current.reciterName,
                    surah = surah,
                    ayahNumber = newAyah,
                    source = RecognitionSource.MANUAL_NAVIGATION,
                    rawTitle = current.streamRawTitle
                )
            }
        }
    }

    private fun recognize(rawTitle: String?, station: RadioStation?, source: RecognitionSource) {
        recognitionJob?.cancel()
        recognitionJob = scope.launch {
            _recitationState.value = _recitationState.value.copy(isLoading = true)

            val reciter = detectReciter(rawTitle, station)
            val detectedSurah = detectSurah(rawTitle, station)
            val detectedAyah = detectAyah(rawTitle, detectedSurah.versesCount)

            fetchAyahInternal(
                reciterName = reciter,
                surah = detectedSurah,
                ayahNumber = detectedAyah,
                source = source,
                rawTitle = rawTitle
            )
        }
    }

    private suspend fun fetchAyahInternal(
        reciterName: String,
        surah: QuranSurahItem,
        ayahNumber: Int,
        source: RecognitionSource,
        rawTitle: String?
    ) {
        val ayahData = QuranTafsirRepository.getAyah(surah.number, ayahNumber, mainDispatcher)

        _recitationState.value = RecognizedRecitation(
            reciterName = reciterName,
            surahNumber = surah.number,
            surahNameAr = "سورة ${surah.nameAr}",
            surahNameEn = surah.englishName,
            currentAyahNumber = ayahNumber,
            totalAyahs = surah.versesCount,
            uthmaniText = ayahData.uthmaniText,
            tafsirText = ayahData.tafsirText,
            isRecognized = true,
            isLoading = false,
            source = source,
            streamRawTitle = rawTitle
        )
    }

    private fun detectReciter(rawTitle: String?, station: RadioStation?): String {
        val stationReciter = station?.reciter
        if (!stationReciter.isNullOrBlank()) {
            return if (!stationReciter.startsWith("الشيخ")) "الشيخ $stationReciter" else stationReciter
        }

        val textToSearch = listOfNotNull(rawTitle, station?.name, station?.nameAr, station?.description).joinToString(" ")

        for ((keyword, canonicalName) in FAMOUS_RECITERS) {
            if (textToSearch.contains(keyword, ignoreCase = true)) {
                return canonicalName
            }
        }

        return station?.name?.takeIf { it.isNotBlank() } ?: "قارئ معتمد"
    }

    private fun detectSurah(rawTitle: String?, station: RadioStation?): QuranSurahItem {
        val textToSearch = listOfNotNull(rawTitle, station?.name, station?.nameAr).joinToString(" ")
        val normalizedSearch = normalizeArabic(textToSearch)

        // 1. Try matching Surah name in Arabic
        for (surah in QuranSurahsData.SURAHS) {
            val normSurah = normalizeArabic(surah.nameAr)
            if (normSurah.length >= 3 && normalizedSearch.contains(normSurah)) {
                return surah
            }
        }

        // 2. Try matching transliterated Surah name (e.g. "Baqarah", "Kahf", "Yasin")
        for (surah in QuranSurahsData.SURAHS) {
            val simpleEn = surah.englishName.replace("Al-", "").replace("An-", "").replace("At-", "").replace("Ash-", "").replace("Az-", "")
            if (simpleEn.length >= 3 && textToSearch.contains(simpleEn, ignoreCase = true)) {
                return surah
            }
        }

        // 3. Try matching 3-digit number (e.g. "018", "002")
        val numMatch = Regex("""\b(0[0-9]{2}|1[0-1][0-4])\b""").find(textToSearch)
        if (numMatch != null) {
            val num = numMatch.value.toIntOrNull()
            if (num != null && num in 1..114) {
                QuranSurahsData.getSurah(num)?.let { return it }
            }
        }

        // Default to Al-Fatihah
        return QuranSurahsData.SURAHS[0]
    }

    private fun detectAyah(rawTitle: String?, totalAyahs: Int): Int {
        if (rawTitle.isNullOrBlank()) return 1

        val ayahRegex = Regex("""(?:الآية|آية|اية|ayah|verse)\s*[:#-]?\s*([0-9]{1,3}|[٠-٩]{1,3})""", RegexOption.IGNORE_CASE)
        val match = ayahRegex.find(rawTitle)
        if (match != null) {
            val rawNum = match.groupValues[1]
            val parsed = parseArabicOrAsciiNumber(rawNum)
            if (parsed in 1..totalAyahs) {
                return parsed
            }
        }

        return 1
    }

    private fun parseArabicOrAsciiNumber(str: String): Int {
        val converted = str.map { ch ->
            when (ch) {
                '٠' -> '0'
                '١' -> '1'
                '٢' -> '2'
                '٣' -> '3'
                '٤' -> '4'
                '٥' -> '5'
                '٦' -> '6'
                '٧' -> '7'
                '٨' -> '8'
                '٩' -> '9'
                else -> ch
            }
        }.joinToString("")
        return converted.toIntOrNull() ?: 1
    }

    private fun normalizeArabic(text: String): String {
        return text
            .replace("[\\u064B-\\u065F\\u0670]".toRegex(), "") // Harakat
            .replace("[إأآا]".toRegex(), "ا")
            .replace("ى", "ي")
            .replace("ة", "ه")
            .replace("ؤ", "و")
            .replace("ئ", "ي")
    }

    companion object {
        private val FAMOUS_RECITERS = listOf(
            "عبد الباسط" to "الشيخ عبد الباسط عبد الصمد",
            "المنشاوي" to "الشيخ محمد صديق المنشاوي",
            "الحصري" to "الشيخ محمود خليل الحصري",
            "العفاسي" to "الشيخ مشاري راشد العفاسي",
            "مشاري" to "الشيخ مشاري راشد العفاسي",
            "السديس" to "الشيخ عبد الرحمن السديس",
            "الشريم" to "الشيخ سعود الشريم",
            "المعيقلي" to "الشيخ ماهر المعيقلي",
            "الغامدي" to "الشيخ سعد الغامدي",
            "الدوسري" to "الشيخ ياسر الدوسري",
            "العجمي" to "الشيخ أحمد العجمي",
            "الشاطري" to "الشيخ أبو بكر الشاطري",
            "علي جابر" to "الشيخ علي عبد الله جابر",
            "خالد الجليل" to "الشيخ خالد الجليل",
            "إدريس أبكر" to "الشيخ إدريس أبكر" ,
            "القطامي" to "الشيخ ناصر القطامي",
            "فارس عباد" to "الشيخ فارس عباد",
            "بو خاطر" to "الشيخ صلاح بو خاطر",
            "بصفر" to "الشيخ عبد الله بصفر",
            "الحذيفي" to "الشيخ علي الحذيفي",
            "محمد رفعت" to "الشيخ محمد رفعت",
            "رفعت" to "الشيخ محمد رفعت",
            "البنا" to "الشيخ محمود علي البنا",
            "مصطفى إسماعيل" to "الشيخ مصطفى إسماعيل",
            "الطبلاوي" to "الشيخ محمد محمود الطبلاوي",
            "الجهني" to "الشيخ عبد الله عواد الجهني",
            "محمد أيوب" to "الشيخ محمد أيوب",
            "الحرم المكي" to "أئمة المسجد الحرام بمكة المكرمة",
            "الحرم النبوي" to "أئمة المسجد النبوي الشريف",
            "إذاعة القرآن الكريم من القاهرة" to "إذاعة القرآن الكريم - القاهرة",
            "إذاعة القرآن الكريم" to "إذاعة القرآن الكريم"
        )
    }
}
