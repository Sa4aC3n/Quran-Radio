package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.AcousticEnvironment
import com.example.model.AmbientSoundscape
import com.example.model.AppThemeMode
import com.example.model.SpatialAudioConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "quran_radio_prefs")

class UserPreferencesRepository(context: Context) {

    private val appContext = context.applicationContext

    private val KEY_FAVORITES = stringSetPreferencesKey("favorite_station_ids")
    private val KEY_AUTO_PLAY = booleanPreferencesKey("auto_play_on_start")
    private val KEY_BACKGROUND_PLAY = booleanPreferencesKey("continue_background_playback")
    private val KEY_WIFI_ONLY = booleanPreferencesKey("wifi_only_streaming")
    private val KEY_NOTIFICATIONS = booleanPreferencesKey("media_notifications_enabled")
    private val KEY_NOTIFICATION_FREQUENCY = stringPreferencesKey("notification_frequency")
    private val KEY_NOTIFICATION_TIME_WINDOW = stringPreferencesKey("notification_time_window")
    private val KEY_LAST_STATION_ID = stringPreferencesKey("last_played_station_id")
    private val KEY_AUDIO_QUALITY = stringPreferencesKey("preferred_audio_quality")
    private val KEY_THEME_MODE = stringPreferencesKey("app_theme_mode")
    private val KEY_APP_LANGUAGE = stringPreferencesKey("app_language")
    private val KEY_COUNTRIES_GRID_COLUMNS = intPreferencesKey("countries_grid_columns")
    private val KEY_SPATIAL_ENABLED = booleanPreferencesKey("spatial_audio_enabled")
    private val KEY_SPATIAL_ENVIRONMENT = stringPreferencesKey("spatial_audio_environment")
    private val KEY_SPATIAL_WET_LEVEL = floatPreferencesKey("spatial_audio_wet_level")
    private val KEY_AMBIENT_SOUNDSCAPE = stringPreferencesKey("spatial_ambient_soundscape")
    private val KEY_AMBIENT_VOLUME = floatPreferencesKey("spatial_ambient_volume")

    val spatialAudioConfig: Flow<SpatialAudioConfig> = appContext.dataStore.data.map { preferences ->
        SpatialAudioConfig(
            isEnabled = preferences[KEY_SPATIAL_ENABLED] ?: false,
            environment = AcousticEnvironment.fromId(preferences[KEY_SPATIAL_ENVIRONMENT] ?: AcousticEnvironment.ORIGINAL.id),
            wetLevel = preferences[KEY_SPATIAL_WET_LEVEL] ?: 0.30f,
            ambientSoundscape = AmbientSoundscape.fromId(preferences[KEY_AMBIENT_SOUNDSCAPE] ?: AmbientSoundscape.NONE.id),
            ambientVolume = preferences[KEY_AMBIENT_VOLUME] ?: 0.08f
        )
    }

    val countriesGridColumns: Flow<Int> = appContext.dataStore.data.map { preferences ->
        preferences[KEY_COUNTRIES_GRID_COLUMNS] ?: 3
    }

    val appLanguage: Flow<com.example.localization.AppLanguage> = appContext.dataStore.data.map { preferences ->
        com.example.localization.AppLanguage.fromCode(preferences[KEY_APP_LANGUAGE] ?: com.example.localization.AppLanguage.AR.code)
    }

    val favoriteStationIds: Flow<Set<String>> = appContext.dataStore.data.map { preferences ->
        preferences[KEY_FAVORITES] ?: setOf("cairo_quran", "saudi_quran", "reciter_abdulbasit_mojawwad", "reciter_minshawi_mojawwad")
    }

    val themeMode: Flow<AppThemeMode> = appContext.dataStore.data.map { preferences ->
        AppThemeMode.fromKey(preferences[KEY_THEME_MODE] ?: AppThemeMode.LIGHT.key)
    }

    val isAutoPlayEnabled: Flow<Boolean> = appContext.dataStore.data.map { preferences ->
        preferences[KEY_AUTO_PLAY] ?: false
    }

    val isBackgroundPlayEnabled: Flow<Boolean> = appContext.dataStore.data.map { preferences ->
        preferences[KEY_BACKGROUND_PLAY] ?: true
    }

    val isWifiOnlyEnabled: Flow<Boolean> = appContext.dataStore.data.map { preferences ->
        preferences[KEY_WIFI_ONLY] ?: false
    }

    val isNotificationsEnabled: Flow<Boolean> = appContext.dataStore.data.map { preferences ->
        preferences[KEY_NOTIFICATIONS] ?: true
    }

    val notificationFrequency: Flow<String> = appContext.dataStore.data.map { preferences ->
        preferences[KEY_NOTIFICATION_FREQUENCY] ?: "all"
    }

    val notificationTimeWindow: Flow<String> = appContext.dataStore.data.map { preferences ->
        preferences[KEY_NOTIFICATION_TIME_WINDOW] ?: "all_day"
    }

    val lastPlayedStationId: Flow<String?> = appContext.dataStore.data.map { preferences ->
        preferences[KEY_LAST_STATION_ID]
    }

    val preferredAudioQuality: Flow<String> = appContext.dataStore.data.map { preferences ->
        preferences[KEY_AUDIO_QUALITY] ?: "high"
    }

    suspend fun toggleFavorite(stationId: String) {
        appContext.dataStore.edit { preferences ->
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
        appContext.dataStore.edit { preferences ->
            preferences[KEY_AUTO_PLAY] = enabled
        }
    }

    suspend fun setBackgroundPlay(enabled: Boolean) {
        appContext.dataStore.edit { preferences ->
            preferences[KEY_BACKGROUND_PLAY] = enabled
        }
    }

    suspend fun setWifiOnly(enabled: Boolean) {
        appContext.dataStore.edit { preferences ->
            preferences[KEY_WIFI_ONLY] = enabled
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        appContext.dataStore.edit { preferences ->
            preferences[KEY_NOTIFICATIONS] = enabled
        }
    }

    suspend fun setNotificationFrequency(frequency: String) {
        appContext.dataStore.edit { preferences ->
            preferences[KEY_NOTIFICATION_FREQUENCY] = frequency
        }
    }

    suspend fun setNotificationTimeWindow(window: String) {
        appContext.dataStore.edit { preferences ->
            preferences[KEY_NOTIFICATION_TIME_WINDOW] = window
        }
    }

    suspend fun setLastPlayedStationId(stationId: String) {
        appContext.dataStore.edit { preferences ->
            preferences[KEY_LAST_STATION_ID] = stationId
        }
    }

    suspend fun setPreferredAudioQuality(quality: String) {
        appContext.dataStore.edit { preferences ->
            preferences[KEY_AUDIO_QUALITY] = quality
        }
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        appContext.dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = mode.key
        }
    }

    suspend fun setAppLanguage(language: com.example.localization.AppLanguage) {
        appContext.dataStore.edit { preferences ->
            preferences[KEY_APP_LANGUAGE] = language.code
        }
    }

    suspend fun setCountriesGridColumns(columns: Int) {
        appContext.dataStore.edit { preferences ->
            preferences[KEY_COUNTRIES_GRID_COLUMNS] = columns
        }
    }

    suspend fun setSpatialAudioConfig(config: SpatialAudioConfig) {
        appContext.dataStore.edit { preferences ->
            preferences[KEY_SPATIAL_ENABLED] = config.isEnabled
            preferences[KEY_SPATIAL_ENVIRONMENT] = config.environment.id
            preferences[KEY_SPATIAL_WET_LEVEL] = config.wetLevel
            preferences[KEY_AMBIENT_SOUNDSCAPE] = config.ambientSoundscape.id
            preferences[KEY_AMBIENT_VOLUME] = config.ambientVolume
        }
    }
}
