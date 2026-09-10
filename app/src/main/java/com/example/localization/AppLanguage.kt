package com.example.localization

/**
 * Supported application languages (Exactly 12 official languages in order):
 *
 * 1.  العربية (افتراضية) - ar - RTL
 * 2.  English - en - LTR
 * 3.  Français - fr - LTR
 * 4.  Italiano - it - LTR
 * 5.  日本語 - ja - LTR
 * 6.  Deutsch - de - LTR
 * 7.  Русский - ru - LTR
 * 8.  中文 (简体) - zh - LTR
 * 9.  فارسی - fa - RTL
 * 10. اردو - ur - RTL
 * 11. Hausa - ha - LTR
 * 12. Кыргызча - ky - LTR
 */
enum class AppLanguage(
    val code: String,
    val nativeName: String,
    val englishName: String,
    val flag: String,
    val isRtl: Boolean
) {
    AR("ar", "العربية", "Arabic", "🇸🇦", true),
    EN("en", "English", "English", "🇺🇸", false),
    FR("fr", "Français", "French", "🇫🇷", false),
    IT("it", "Italiano", "Italian", "🇮🇹", false),
    JA("ja", "日本語", "Japanese", "🇯🇵", false),
    DE("de", "Deutsch", "German", "🇩🇪", false),
    RU("ru", "Русский", "Russian", "🇷🇺", false),
    ZH("zh", "中文 (简体)", "Chinese", "🇨🇳", false),
    FA("fa", "فارسی", "Persian", "🇮🇷", true),
    UR("ur", "اردو", "Urdu", "🇵🇰", true),
    HA("ha", "Hausa", "Hausa", "🇳🇬", false),
    KY("ky", "Кыргызча", "Kyrgyz", "🇰🇬", false);

    val displayName: String
        get() = "$nativeName — $englishName"

    companion object {
        fun fromCode(code: String?): AppLanguage {
            if (code.isNullOrBlank()) return AR
            val cleanCode = code.trim().lowercase()
            return entries.find { 
                it.code.equals(cleanCode, ignoreCase = true) ||
                cleanCode.startsWith(it.code) ||
                (it == ZH && (cleanCode.contains("zh") || cleanCode.contains("cn")))
            } ?: AR
        }
    }
}
