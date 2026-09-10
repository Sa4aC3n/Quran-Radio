package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.model.AdhanSound
import com.example.model.AsrJuristic
import com.example.model.CalculationMethod
import com.example.model.PrayerType
import com.example.model.SeasonModeOverride
import com.example.model.SeasonStreamSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PrayerPreferencesRepository(context: Context) {

    private val appContext = context.applicationContext

    private val KEY_USE_AUTO_LOCATION = booleanPreferencesKey("prayer_use_auto_location")
    private val KEY_LATITUDE = doublePreferencesKey("prayer_latitude")
    private val KEY_LONGITUDE = doublePreferencesKey("prayer_longitude")
    private val KEY_CITY_NAME = stringPreferencesKey("prayer_city_name")
    private val KEY_COUNTRY_NAME = stringPreferencesKey("prayer_country_name")
    private val KEY_COUNTRY_CODE = stringPreferencesKey("prayer_country_code")
    private val KEY_TIMEZONE_ID = stringPreferencesKey("prayer_timezone_id")

    private val KEY_CALC_METHOD = stringPreferencesKey("prayer_calc_method")
    private val KEY_ASR_JURISTIC = stringPreferencesKey("prayer_asr_juristic")
    private val KEY_ADHAN_SOUND = stringPreferencesKey("prayer_adhan_sound")
    private val KEY_ADHAN_MASTER_ENABLED = booleanPreferencesKey("prayer_adhan_master_enabled")

    // Per prayer alert toggles
    private val KEY_ALERT_FAJR = booleanPreferencesKey("alert_fajr")
    private val KEY_ALERT_SUNRISE = booleanPreferencesKey("alert_sunrise")
    private val KEY_ALERT_DHUHR = booleanPreferencesKey("alert_dhuhr")
    private val KEY_ALERT_ASR = booleanPreferencesKey("alert_asr")
    private val KEY_ALERT_MAGHRIB = booleanPreferencesKey("alert_maghrib")
    private val KEY_ALERT_ISHA = booleanPreferencesKey("alert_isha")

    // Per prayer Iqamah alert toggles
    private val KEY_IQAMAH_FAJR = booleanPreferencesKey("iqamah_fajr")
    private val KEY_IQAMAH_DHUHR = booleanPreferencesKey("iqamah_dhuhr")
    private val KEY_IQAMAH_ASR = booleanPreferencesKey("iqamah_asr")
    private val KEY_IQAMAH_MAGHRIB = booleanPreferencesKey("iqamah_maghrib")
    private val KEY_IQAMAH_ISHA = booleanPreferencesKey("iqamah_isha")

    // Per prayer Iqamah delays in minutes
    private val KEY_IQAMAH_DELAY_FAJR = intPreferencesKey("iqamah_delay_fajr")
    private val KEY_IQAMAH_DELAY_DHUHR = intPreferencesKey("iqamah_delay_dhuhr")
    private val KEY_IQAMAH_DELAY_ASR = intPreferencesKey("iqamah_delay_asr")
    private val KEY_IQAMAH_DELAY_MAGHRIB = intPreferencesKey("iqamah_delay_maghrib")
    private val KEY_IQAMAH_DELAY_ISHA = intPreferencesKey("iqamah_delay_isha")

    // Manual minute adjustments (+/-)
    private val KEY_OFFSET_FAJR = intPreferencesKey("offset_fajr")
    private val KEY_OFFSET_SUNRISE = intPreferencesKey("offset_sunrise")
    private val KEY_OFFSET_DHUHR = intPreferencesKey("offset_dhuhr")
    private val KEY_OFFSET_ASR = intPreferencesKey("offset_asr")
    private val KEY_OFFSET_MAGHRIB = intPreferencesKey("offset_maghrib")
    private val KEY_OFFSET_ISHA = intPreferencesKey("offset_isha")

    // Islamic Seasons Preferences (Ramadan & Hajj Mode)
    private val KEY_SEASON_OVERRIDE = stringPreferencesKey("season_override_mode")
    private val KEY_HIJRI_OFFSET_DAYS = intPreferencesKey("season_hijri_offset_days")
    private val KEY_RAMADAN_BANNER_ENABLED = booleanPreferencesKey("season_ramadan_banner_enabled")
    private val KEY_HAJJ_BANNER_ENABLED = booleanPreferencesKey("season_hajj_banner_enabled")
    private val KEY_TARAWEEH_ALERT_ENABLED = booleanPreferencesKey("season_taraweeh_alert_enabled")
    private val KEY_SUHOOR_ALERT_ENABLED = booleanPreferencesKey("season_suhoor_alert_enabled")
    private val KEY_ARAFAH_ALERT_ENABLED = booleanPreferencesKey("season_arafah_alert_enabled")
    private val KEY_SEASON_STREAM_SOURCE = stringPreferencesKey("season_stream_source")
    private val KEY_KHATMAH_COMPLETED_JUZ = stringPreferencesKey("season_khatmah_completed_juz")

    val useAutoLocation: Flow<Boolean> = appContext.dataStore.data.map {
        it[KEY_USE_AUTO_LOCATION] ?: true
    }

    val latitude: Flow<Double> = appContext.dataStore.data.map {
        it[KEY_LATITUDE] ?: 30.0444 // Default Cairo
    }

    val longitude: Flow<Double> = appContext.dataStore.data.map {
        it[KEY_LONGITUDE] ?: 31.2357 // Default Cairo
    }

    val cityName: Flow<String> = appContext.dataStore.data.map {
        it[KEY_CITY_NAME] ?: "القاهرة"
    }

    val countryName: Flow<String> = appContext.dataStore.data.map {
        it[KEY_COUNTRY_NAME] ?: "مصر"
    }

    val countryCode: Flow<String> = appContext.dataStore.data.map {
        it[KEY_COUNTRY_CODE] ?: "EG"
    }

    val timeZoneId: Flow<String> = appContext.dataStore.data.map {
        it[KEY_TIMEZONE_ID] ?: "Africa/Cairo"
    }

    val calculationMethod: Flow<CalculationMethod> = appContext.dataStore.data.map {
        CalculationMethod.fromId(it[KEY_CALC_METHOD] ?: CalculationMethod.EGYPTIAN.id)
    }

    val asrJuristic: Flow<AsrJuristic> = appContext.dataStore.data.map {
        AsrJuristic.fromId(it[KEY_ASR_JURISTIC] ?: AsrJuristic.STANDARD.id)
    }

    val selectedAdhanSound: Flow<AdhanSound> = appContext.dataStore.data.map {
        AdhanSound.fromId(it[KEY_ADHAN_SOUND] ?: AdhanSound.MAKKAH.id)
    }

    val isAdhanMasterEnabled: Flow<Boolean> = appContext.dataStore.data.map {
        it[KEY_ADHAN_MASTER_ENABLED] ?: true
    }

    val alertSettings: Flow<Map<PrayerType, Boolean>> = appContext.dataStore.data.map { prefs ->
        mapOf(
            PrayerType.FAJR to (prefs[KEY_ALERT_FAJR] ?: true),
            PrayerType.SUNRISE to (prefs[KEY_ALERT_SUNRISE] ?: false),
            PrayerType.DHUHR to (prefs[KEY_ALERT_DHUHR] ?: true),
            PrayerType.ASR to (prefs[KEY_ALERT_ASR] ?: true),
            PrayerType.MAGHRIB to (prefs[KEY_ALERT_MAGHRIB] ?: true),
            PrayerType.ISHA to (prefs[KEY_ALERT_ISHA] ?: true)
        )
    }

    val iqamahAlertSettings: Flow<Map<PrayerType, Boolean>> = appContext.dataStore.data.map { prefs ->
        mapOf(
            PrayerType.FAJR to (prefs[KEY_IQAMAH_FAJR] ?: false),
            PrayerType.DHUHR to (prefs[KEY_IQAMAH_DHUHR] ?: false),
            PrayerType.ASR to (prefs[KEY_IQAMAH_ASR] ?: false),
            PrayerType.MAGHRIB to (prefs[KEY_IQAMAH_MAGHRIB] ?: false),
            PrayerType.ISHA to (prefs[KEY_IQAMAH_ISHA] ?: false)
        )
    }

    val iqamahDelays: Flow<Map<PrayerType, Int>> = appContext.dataStore.data.map { prefs ->
        mapOf(
            PrayerType.FAJR to (prefs[KEY_IQAMAH_DELAY_FAJR] ?: 20),
            PrayerType.DHUHR to (prefs[KEY_IQAMAH_DELAY_DHUHR] ?: 15),
            PrayerType.ASR to (prefs[KEY_IQAMAH_DELAY_ASR] ?: 15),
            PrayerType.MAGHRIB to (prefs[KEY_IQAMAH_DELAY_MAGHRIB] ?: 10),
            PrayerType.ISHA to (prefs[KEY_IQAMAH_DELAY_ISHA] ?: 15)
        )
    }

    val minuteOffsets: Flow<Map<PrayerType, Int>> = appContext.dataStore.data.map { prefs ->
        mapOf(
            PrayerType.FAJR to (prefs[KEY_OFFSET_FAJR] ?: 0),
            PrayerType.SUNRISE to (prefs[KEY_OFFSET_SUNRISE] ?: 0),
            PrayerType.DHUHR to (prefs[KEY_OFFSET_DHUHR] ?: 0),
            PrayerType.ASR to (prefs[KEY_OFFSET_ASR] ?: 0),
            PrayerType.MAGHRIB to (prefs[KEY_OFFSET_MAGHRIB] ?: 0),
            PrayerType.ISHA to (prefs[KEY_OFFSET_ISHA] ?: 0)
        )
    }

    // Islamic Season flows
    val seasonModeOverride: Flow<SeasonModeOverride> = appContext.dataStore.data.map {
        SeasonModeOverride.fromId(it[KEY_SEASON_OVERRIDE] ?: SeasonModeOverride.AUTO.id)
    }

    val hijriOffsetDays: Flow<Int> = appContext.dataStore.data.map {
        it[KEY_HIJRI_OFFSET_DAYS] ?: 0
    }

    val isRamadanBannerEnabled: Flow<Boolean> = appContext.dataStore.data.map {
        it[KEY_RAMADAN_BANNER_ENABLED] ?: true
    }

    val isHajjBannerEnabled: Flow<Boolean> = appContext.dataStore.data.map {
        it[KEY_HAJJ_BANNER_ENABLED] ?: true
    }

    val isTaraweehAlertEnabled: Flow<Boolean> = appContext.dataStore.data.map {
        it[KEY_TARAWEEH_ALERT_ENABLED] ?: true
    }

    val isSuhoorAlertEnabled: Flow<Boolean> = appContext.dataStore.data.map {
        it[KEY_SUHOOR_ALERT_ENABLED] ?: true
    }

    val isArafahAlertEnabled: Flow<Boolean> = appContext.dataStore.data.map {
        it[KEY_ARAFAH_ALERT_ENABLED] ?: true
    }

    val seasonStreamSource: Flow<SeasonStreamSource> = appContext.dataStore.data.map {
        SeasonStreamSource.fromId(it[KEY_SEASON_STREAM_SOURCE] ?: SeasonStreamSource.MAKKAH.id)
    }

    val khatmahCompletedJuz: Flow<Set<Int>> = appContext.dataStore.data.map { prefs ->
        val raw = prefs[KEY_KHATMAH_COMPLETED_JUZ] ?: ""
        if (raw.isBlank()) emptySet()
        else raw.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
    }

    suspend fun setLocation(
        latitude: Double,
        longitude: Double,
        cityName: String,
        countryName: String,
        countryCode: String,
        timeZoneId: String,
        isAuto: Boolean
    ) {
        appContext.dataStore.edit { prefs ->
            prefs[KEY_LATITUDE] = latitude
            prefs[KEY_LONGITUDE] = longitude
            prefs[KEY_CITY_NAME] = cityName
            prefs[KEY_COUNTRY_NAME] = countryName
            prefs[KEY_COUNTRY_CODE] = countryCode
            prefs[KEY_TIMEZONE_ID] = timeZoneId
            prefs[KEY_USE_AUTO_LOCATION] = isAuto

            // If user hasn't customized calculation method, automatically set matching method
            if (!prefs.contains(KEY_CALC_METHOD)) {
                prefs[KEY_CALC_METHOD] = CalculationMethod.forCountryCode(countryCode).id
            }
        }
    }

    suspend fun setCalculationMethod(method: CalculationMethod) {
        appContext.dataStore.edit { it[KEY_CALC_METHOD] = method.id }
    }

    suspend fun setAsrJuristic(juristic: AsrJuristic) {
        appContext.dataStore.edit { it[KEY_ASR_JURISTIC] = juristic.id }
    }

    suspend fun setAdhanSound(sound: AdhanSound) {
        appContext.dataStore.edit { it[KEY_ADHAN_SOUND] = sound.id }
    }

    suspend fun setAdhanMasterEnabled(enabled: Boolean) {
        appContext.dataStore.edit { it[KEY_ADHAN_MASTER_ENABLED] = enabled }
    }

    suspend fun togglePrayerAlert(type: PrayerType, enabled: Boolean) {
        appContext.dataStore.edit { prefs ->
            when (type) {
                PrayerType.FAJR -> prefs[KEY_ALERT_FAJR] = enabled
                PrayerType.SUNRISE -> prefs[KEY_ALERT_SUNRISE] = enabled
                PrayerType.DHUHR -> prefs[KEY_ALERT_DHUHR] = enabled
                PrayerType.ASR -> prefs[KEY_ALERT_ASR] = enabled
                PrayerType.MAGHRIB -> prefs[KEY_ALERT_MAGHRIB] = enabled
                PrayerType.ISHA -> prefs[KEY_ALERT_ISHA] = enabled
            }
        }
    }

    suspend fun toggleIqamahAlert(type: PrayerType, enabled: Boolean) {
        appContext.dataStore.edit { prefs ->
            when (type) {
                PrayerType.FAJR -> prefs[KEY_IQAMAH_FAJR] = enabled
                PrayerType.SUNRISE -> { /* No Iqamah for sunrise */ }
                PrayerType.DHUHR -> prefs[KEY_IQAMAH_DHUHR] = enabled
                PrayerType.ASR -> prefs[KEY_IQAMAH_ASR] = enabled
                PrayerType.MAGHRIB -> prefs[KEY_IQAMAH_MAGHRIB] = enabled
                PrayerType.ISHA -> prefs[KEY_IQAMAH_ISHA] = enabled
            }
        }
    }

    suspend fun setIqamahDelay(type: PrayerType, minutes: Int) {
        appContext.dataStore.edit { prefs ->
            when (type) {
                PrayerType.FAJR -> prefs[KEY_IQAMAH_DELAY_FAJR] = minutes
                PrayerType.DHUHR -> prefs[KEY_IQAMAH_DELAY_DHUHR] = minutes
                PrayerType.ASR -> prefs[KEY_IQAMAH_DELAY_ASR] = minutes
                PrayerType.MAGHRIB -> prefs[KEY_IQAMAH_DELAY_MAGHRIB] = minutes
                PrayerType.ISHA -> prefs[KEY_IQAMAH_DELAY_ISHA] = minutes
                else -> {}
            }
        }
    }

    suspend fun setMinuteOffset(type: PrayerType, minutes: Int) {
        appContext.dataStore.edit { prefs ->
            when (type) {
                PrayerType.FAJR -> prefs[KEY_OFFSET_FAJR] = minutes
                PrayerType.SUNRISE -> prefs[KEY_OFFSET_SUNRISE] = minutes
                PrayerType.DHUHR -> prefs[KEY_OFFSET_DHUHR] = minutes
                PrayerType.ASR -> prefs[KEY_OFFSET_ASR] = minutes
                PrayerType.MAGHRIB -> prefs[KEY_OFFSET_MAGHRIB] = minutes
                PrayerType.ISHA -> prefs[KEY_OFFSET_ISHA] = minutes
            }
        }
    }

    suspend fun setSeasonModeOverride(mode: SeasonModeOverride) {
        appContext.dataStore.edit { it[KEY_SEASON_OVERRIDE] = mode.id }
    }

    suspend fun setHijriOffsetDays(offset: Int) {
        appContext.dataStore.edit { it[KEY_HIJRI_OFFSET_DAYS] = offset }
    }

    suspend fun setRamadanBannerEnabled(enabled: Boolean) {
        appContext.dataStore.edit { it[KEY_RAMADAN_BANNER_ENABLED] = enabled }
    }

    suspend fun setHajjBannerEnabled(enabled: Boolean) {
        appContext.dataStore.edit { it[KEY_HAJJ_BANNER_ENABLED] = enabled }
    }

    suspend fun setTaraweehAlertEnabled(enabled: Boolean) {
        appContext.dataStore.edit { it[KEY_TARAWEEH_ALERT_ENABLED] = enabled }
    }

    suspend fun setSuhoorAlertEnabled(enabled: Boolean) {
        appContext.dataStore.edit { it[KEY_SUHOOR_ALERT_ENABLED] = enabled }
    }

    suspend fun setArafahAlertEnabled(enabled: Boolean) {
        appContext.dataStore.edit { it[KEY_ARAFAH_ALERT_ENABLED] = enabled }
    }

    suspend fun setSeasonStreamSource(source: SeasonStreamSource) {
        appContext.dataStore.edit { it[KEY_SEASON_STREAM_SOURCE] = source.id }
    }

    suspend fun toggleKhatmahJuz(juzNumber: Int) {
        appContext.dataStore.edit { prefs ->
            val raw = prefs[KEY_KHATMAH_COMPLETED_JUZ] ?: ""
            val currentSet = if (raw.isBlank()) mutableSetOf()
            else raw.split(",").mapNotNull { it.trim().toIntOrNull() }.toMutableSet()

            if (currentSet.contains(juzNumber)) {
                currentSet.remove(juzNumber)
            } else {
                currentSet.add(juzNumber)
            }
            prefs[KEY_KHATMAH_COMPLETED_JUZ] = currentSet.sorted().joinToString(",")
        }
    }

    suspend fun resetKhatmah() {
        appContext.dataStore.edit { prefs ->
            prefs[KEY_KHATMAH_COMPLETED_JUZ] = ""
        }
    }
}
