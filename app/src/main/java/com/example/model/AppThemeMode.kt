package com.example.model

enum class AppThemeMode(val key: String, val titleAr: String) {
    LIGHT("light", "الوضع الفاتح (الافتراضي)"),
    DARK("dark", "الوضع الليلي (الداكن)"),
    SYSTEM("system", "تلقائي (حسب النظام)");

    companion object {
        fun fromKey(key: String?): AppThemeMode {
            return entries.find { it.key == key } ?: LIGHT
        }
    }
}
