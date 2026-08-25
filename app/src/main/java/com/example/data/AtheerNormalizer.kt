package com.example.data

import java.net.URI
import java.util.Locale

object AtheerNormalizer {

    private val DIACRITICS_REGEX = Regex("[\\u064B-\\u065F\\u0670]")
    private val TATWEEL_REGEX = Regex("[ـ]")
    private val NON_ALPHANUMERIC_REGEX = Regex("[^\\p{L}\\p{Nd}]+")

    /**
     * Normalizes Arabic strings for reliable deduplication and search matching.
     */
    fun normalizeArabicName(text: String): String {
        if (text.isBlank()) return ""

        var normalized = text.trim()
            // Remove diacritics / Harakat
            .replace(DIACRITICS_REGEX, "")
            // Remove Tatweel
            .replace(TATWEEL_REGEX, "")
            // Normalize Alefs
            .replace(Regex("[أإآٱ]"), "ا")
            // Normalize Ta Marbuta to Ha
            .replace('ة', 'ه')
            // Normalize Alef Maksura to Ya
            .replace('ى', 'ي')
            // Normalize Hamza forms
            .replace(Regex("[ؤئ]"), "ء")

        // Remove common radio/station prefix tokens
        val prefixTokens = listOf(
            "اذاعة", "اذاعه", "إذاعة", "إذاعه",
            "الشيخ", "القارئ", "القارء", "فضيلة",
            "راديو", "بث مباشر", "تلاوة", "تلاوات",
            "محطة", "قناة", "تسجيلات"
        )

        for (token in prefixTokens) {
            val normalizedToken = token
                .replace(Regex("[أإآٱ]"), "ا")
                .replace('ة', 'ه')
                .replace('ى', 'ي')
            normalized = normalized.replace(Regex("^\\s*$normalizedToken\\s*", RegexOption.IGNORE_CASE), "")
            normalized = normalized.replace(Regex("\\b$normalizedToken\\b", RegexOption.IGNORE_CASE), "")
        }

        // Clean extra non-alphanumeric symbols and whitespace
        normalized = normalized.replace(NON_ALPHANUMERIC_REGEX, " ").trim()
        return normalized.lowercase(Locale.getDefault())
    }

    /**
     * Normalizes Stream URLs to detect duplicate audio endpoints.
     */
    fun normalizeStreamUrl(rawUrl: String): String {
        if (rawUrl.isBlank()) return ""
        return try {
            val trimmed = rawUrl.trim()
            val uri = URI(trimmed)
            val scheme = uri.scheme?.lowercase(Locale.ROOT) ?: "http"
            val host = uri.host?.lowercase(Locale.ROOT) ?: ""
            var port = uri.port
            if ((scheme == "http" && port == 80) || (scheme == "https" && port == 443)) {
                port = -1
            }
            var path = uri.path ?: ""
            while (path.endsWith("/")) {
                path = path.substring(0, path.length - 1)
            }
            // qurango and backup.qurango are the same endpoint cluster
            val normalizedHost = if (host == "backup.qurango.net") "qurango.net" else host
            val portStr = if (port != -1) ":$port" else ""
            
            "$normalizedHost$portStr$path".lowercase(Locale.ROOT)
        } catch (e: Exception) {
            rawUrl.trim()
                .replace("https://", "")
                .replace("http://", "")
                .replace("backup.qurango.net", "qurango.net")
                .trimEnd('/')
                .lowercase(Locale.ROOT)
        }
    }

    /**
     * Checks if two station names match logically.
     */
    fun areNamesEquivalent(name1: String, name2: String): Boolean {
        val n1 = normalizeArabicName(name1)
        val n2 = normalizeArabicName(name2)
        if (n1 == n2) return true
        if (n1.isNotBlank() && n2.isNotBlank()) {
            if (n1.contains(n2) || n2.contains(n1)) return true
        }
        return false
    }

    /**
     * Checks if two stream URLs are equivalent.
     */
    fun areUrlsEquivalent(url1: String, url2: String): Boolean {
        val u1 = normalizeStreamUrl(url1)
        val u2 = normalizeStreamUrl(url2)
        return u1.isNotBlank() && u1 == u2
    }
}
