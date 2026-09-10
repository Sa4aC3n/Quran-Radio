package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.example.model.HaramLivePreferences
import com.example.model.HaramSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HaramPreferencesRepository(context: Context) {

    private val appContext = context.applicationContext

    companion object {
        private val KEY_ENABLED = booleanPreferencesKey("haram_live_enabled")
        private val KEY_SOURCE = stringPreferencesKey("haram_live_source")
        private val KEY_FAV_ONLY = booleanPreferencesKey("haram_live_fav_only")
        private val KEY_FAV_SURAHS = stringSetPreferencesKey("haram_live_fav_surahs")
        private val KEY_DAILY_LIMIT = intPreferencesKey("haram_live_daily_limit")
        private val KEY_SENT_COUNT_TODAY = intPreferencesKey("haram_live_sent_count_today")
        private val KEY_LAST_SENT_DATE = stringPreferencesKey("haram_live_last_sent_date")
    }

    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    val preferences: Flow<HaramLivePreferences> = appContext.dataStore.data.map { prefs ->
        val today = getTodayDateString()
        val lastDate = prefs[KEY_LAST_SENT_DATE] ?: ""
        val count = if (lastDate == today) prefs[KEY_SENT_COUNT_TODAY] ?: 0 else 0

        val favSurahStrings = prefs[KEY_FAV_SURAHS] ?: setOf("18", "36", "67", "55", "56", "2", "3")
        val favSurahs = favSurahStrings.mapNotNull { it.toIntOrNull() }.toSet()

        HaramLivePreferences(
            isEnabled = prefs[KEY_ENABLED] ?: false, // Opt-in by default
            source = HaramSource.fromId(prefs[KEY_SOURCE] ?: HaramSource.BOTH.id),
            favoriteSurahsOnly = prefs[KEY_FAV_ONLY] ?: false,
            favoriteSurahs = favSurahs,
            dailyLimit = prefs[KEY_DAILY_LIMIT] ?: 4,
            notificationsSentToday = count,
            lastNotificationDate = lastDate
        )
    }

    suspend fun setEnabled(enabled: Boolean) {
        appContext.dataStore.edit { prefs ->
            prefs[KEY_ENABLED] = enabled
        }
    }

    suspend fun setSource(source: HaramSource) {
        appContext.dataStore.edit { prefs ->
            prefs[KEY_SOURCE] = source.id
        }
    }

    suspend fun setFavoriteSurahsOnly(favoriteOnly: Boolean) {
        appContext.dataStore.edit { prefs ->
            prefs[KEY_FAV_ONLY] = favoriteOnly
        }
    }

    suspend fun toggleFavoriteSurah(surahNumber: Int) {
        appContext.dataStore.edit { prefs ->
            val current = prefs[KEY_FAV_SURAHS]?.toMutableSet()
                ?: mutableSetOf("18", "36", "67", "55", "56", "2", "3")
            val numStr = surahNumber.toString()
            if (current.contains(numStr)) {
                current.remove(numStr)
            } else {
                current.add(numStr)
            }
            prefs[KEY_FAV_SURAHS] = current
        }
    }

    suspend fun setFavoriteSurahs(surahs: Set<Int>) {
        appContext.dataStore.edit { prefs ->
            prefs[KEY_FAV_SURAHS] = surahs.map { it.toString() }.toSet()
        }
    }

    suspend fun setDailyLimit(limit: Int) {
        appContext.dataStore.edit { prefs ->
            prefs[KEY_DAILY_LIMIT] = limit.coerceIn(1, 10)
        }
    }

    suspend fun canSendNotification(surahNumber: Int, incomingSource: HaramSource): Boolean {
        val currentPrefs = preferences.first()
        if (!currentPrefs.isEnabled) return false

        // Check source filter
        if (currentPrefs.source != HaramSource.BOTH && currentPrefs.source != incomingSource) {
            return false
        }

        // Check Surah filter
        if (currentPrefs.favoriteSurahsOnly && !currentPrefs.favoriteSurahs.contains(surahNumber)) {
            return false
        }

        // Check daily rate limit
        val today = getTodayDateString()
        val count = if (currentPrefs.lastNotificationDate == today) currentPrefs.notificationsSentToday else 0
        if (count >= currentPrefs.dailyLimit) {
            return false
        }

        return true
    }

    suspend fun recordNotificationSent() {
        val today = getTodayDateString()
        appContext.dataStore.edit { prefs ->
            val lastDate = prefs[KEY_LAST_SENT_DATE] ?: ""
            val count = if (lastDate == today) prefs[KEY_SENT_COUNT_TODAY] ?: 0 else 0
            prefs[KEY_LAST_SENT_DATE] = today
            prefs[KEY_SENT_COUNT_TODAY] = count + 1
        }
    }
}
