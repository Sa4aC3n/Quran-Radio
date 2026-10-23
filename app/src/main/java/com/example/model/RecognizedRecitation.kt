package com.example.model

enum class RecognitionSource {
    STREAM_METADATA,   // Detected via live ICY StreamTitle / artist metadata
    STATION_INFO,      // Inferred from station identity and reciter catalog
    MANUAL_NAVIGATION  // User navigated through Surah verses
}

data class RecognizedRecitation(
    val reciterName: String = "",
    val surahNumber: Int = 1,
    val surahNameAr: String = "الفاتحة",
    val surahNameEn: String = "Al-Fatihah",
    val currentAyahNumber: Int = 1,
    val totalAyahs: Int = 7,
    val uthmaniText: String = "",
    val tafsirText: String = "",
    val isRecognized: Boolean = false,
    val isLoading: Boolean = false,
    val source: RecognitionSource = RecognitionSource.STATION_INFO,
    val streamRawTitle: String? = null
)
