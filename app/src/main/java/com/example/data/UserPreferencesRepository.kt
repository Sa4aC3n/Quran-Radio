package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.AppThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "quran_radio_prefs")

class UserPreferencesRepository(private val context: Context) {

    private val KEY_FAVORITES = stringSetPreferencesKey("favorite_station_ids")
    private val KEY_AUTO_PLAY = booleanPreferencesKey("auto_play_on_start")
    private val KEY_BACKGROUND_PLAY = booleanPreferencesKey("continue_background_playback")
    private val KEY_WIFI_ONLY = booleanPreferencesKey("wifi_only_streaming")
    private val KEY_NOTIFICATIONS = booleanPreferencesKey("media_notifications_enabled")
    private val KEY_LAST_STATION_ID = stringPreferencesKey("last_played_station_id")
    private val KEY_AUDIO_QUALITY = stringPreferencesKey("preferred_audio_quality")
    private val KEY_THEME_MODE = stringPreferencesKey("app_theme_mode")

    val favoriteStationIds: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[KEY_FAVORITES] ?: setOf("cairo_quran", "saudi_quran", "reciter_abdulbasit_mojawwad", "reciter_minshawi_mojawwad")
    }

    val themeMode: Flow<AppThemeMode> = context.dataStore.data.map { preferences ->
        AppThemeMode.fromKey(preferences[KEY_THEME_MODE] ?: AppThemeMode.LIGHT.key)
    }

    val isAutoPlayEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_AUTO_PLAY] ?: false
    }

    val isBackgroundPlayEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_BACKGROUND_PLAY] ?: true
    }

    val isWifiOnlyEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_WIFI_ONLY] ?: false
    }

    val isNotificationsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_NOTIFICATIONS] ?: true
    }

    val lastPlayedStationId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_LAST_STATION_ID]
    }

    val preferredAudioQuality: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_AUDIO_QUALITY] ?: "high"
    }

    suspend fun toggleFavorite(stationId: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[KEY_FAVORITES]?.toMutableSet() ?: mutableSetOf("cairo_quran", "saudi_quran", "reciter_abdulbasit_mojawwad", "reciter_minshawi_mojawwad")
            if (current.contains(stationId)) {
                current.remove(stationId)
            } else {
                current.add(stationId)
            }
            preferences[KEY_FAVORITES] = current
        }
    }

    suspend fun setAutoPlay(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_AUTO_PLAY] = enabled
        }
    }

    suspend fun setBackgroundPlay(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_BACKGROUND_PLAY] = enabled
        }
    }

    suspend fun setWifiOnly(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_WIFI_ONLY] = enabled
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_NOTIFICATIONS] = enabled
        }
    }

    suspend fun setLastPlayedStationId(stationId: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LAST_STATION_ID] = stationId
        }
    }

    suspend fun setPreferredAudioQuality(quality: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_AUDIO_QUALITY] = quality
        }
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = mode.key
        }
    }
}
