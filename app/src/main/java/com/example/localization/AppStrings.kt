package com.example.localization

import com.example.localization.languages.stringsAr
import com.example.localization.languages.stringsDe
import com.example.localization.languages.stringsEn
import com.example.localization.languages.stringsFa
import com.example.localization.languages.stringsFr
import com.example.localization.languages.stringsHa
import com.example.localization.languages.stringsIt
import com.example.localization.languages.stringsJa
import com.example.localization.languages.stringsKy
import com.example.localization.languages.stringsRu
import com.example.localization.languages.stringsUr
import com.example.localization.languages.stringsZh

/**
 * Centralized translation repository containing full localization across 12 languages:
 * Arabic (العربية), English, French (Français), Italian (Italiano), Japanese (日本語),
 * German (Deutsch), Russian (Русский), Chinese (中文), Persian (فارسی), Urdu (اردو),
 * Hausa, Kyrgyz (Кыргызча).
 */
object AppStrings {

    fun get(key: StringKey, language: AppLanguage): String {
        val map = when (language) {
            AppLanguage.AR -> stringsAr
            AppLanguage.EN -> stringsEn
            AppLanguage.FR -> stringsFr
            AppLanguage.IT -> stringsIt
            AppLanguage.JA -> stringsJa
            AppLanguage.DE -> stringsDe
            AppLanguage.RU -> stringsRu
            AppLanguage.ZH -> stringsZh
            AppLanguage.FA -> stringsFa
            AppLanguage.UR -> stringsUr
            AppLanguage.HA -> stringsHa
            AppLanguage.KY -> stringsKy
        }
        return map[key] ?: stringsEn[key] ?: stringsAr[key] ?: key.name
    }

    fun format(key: StringKey, language: AppLanguage, vararg args: Any): String {
        val template = get(key, language)
        return try {
            String.format(template, *args)
        } catch (e: Exception) {
            template
        }
    }
}
