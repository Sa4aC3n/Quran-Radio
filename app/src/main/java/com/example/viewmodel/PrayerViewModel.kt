package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.core.content.ContextCompat
import com.example.QuranRadioApplication
import com.example.data.IslamicSeasonEngine
import com.example.data.PrayerCalculationEngine
import com.example.data.QiblaEngine
import com.example.model.AdhanSound
import com.example.model.AsrJuristic
import com.example.model.CalculationMethod
import com.example.model.CityLocation
import com.example.model.DailyPrayerSchedule
import com.example.model.IslamicSeason
import com.example.model.IslamicSeasonState
import com.example.model.PrayerTimeItem
import com.example.model.PrayerType
import com.example.model.PresetCities
import com.example.model.QiblaInfo
import com.example.model.RamadanKhatmahState
import com.example.model.SeasonModeOverride
import com.example.model.SeasonStreamSource
import com.example.services.AdhanAlarmScheduler
import com.example.services.AdhanNotificationHelper
import com.example.services.AdhanPlaybackService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

class PrayerViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as QuranRadioApplication
    private val prefs = app.prayerPreferencesRepository
    private val locationManager = app.appLocationManager
    private val sensorManager = app.qiblaSensorManager
    val adhanAudioPlayer = app.adhanAudioPlayer

    val useAutoLocation: StateFlow<Boolean> = prefs.useAutoLocation
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val latitude: StateFlow<Double> = prefs.latitude
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 30.0444)

    val longitude: StateFlow<Double> = prefs.longitude
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 31.2357)

    val cityName: StateFlow<String> = prefs.cityName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "القاهرة")

    val countryName: StateFlow<String> = prefs.countryName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "مصر")

    val countryCode: StateFlow<String> = prefs.countryCode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "EG")

    val timeZoneId: StateFlow<String> = prefs.timeZoneId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Africa/Cairo")

    val calculationMethod: StateFlow<CalculationMethod> = prefs.calculationMethod
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CalculationMethod.EGYPTIAN)

    val asrJuristic: StateFlow<AsrJuristic> = prefs.asrJuristic
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AsrJuristic.STANDARD)

    val selectedAdhanSound: StateFlow<AdhanSound> = prefs.selectedAdhanSound
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AdhanSound.MAKKAH)

    val isAdhanMasterEnabled: StateFlow<Boolean> = prefs.isAdhanMasterEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isAdhanActive: StateFlow<Boolean> = app.adhanManager.isAdhanActive
    val adhanPlaybackState = app.adhanManager.adhanState
    val currentAdhanPrayer = app.adhanManager.currentPrayer

    val alertSettings: StateFlow<Map<PrayerType, Boolean>> = prefs.alertSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val iqamahAlertSettings: StateFlow<Map<PrayerType, Boolean>> = prefs.iqamahAlertSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val iqamahDelays: StateFlow<Map<PrayerType, Int>> = prefs.iqamahDelays
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val minuteOffsets: StateFlow<Map<PrayerType, Int>> = prefs.minuteOffsets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Islamic Season flows
    val seasonModeOverride: StateFlow<SeasonModeOverride> = prefs.seasonModeOverride
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SeasonModeOverride.AUTO)

    val hijriOffsetDays: StateFlow<Int> = prefs.hijriOffsetDays
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val isRamadanBannerEnabled: StateFlow<Boolean> = prefs.isRamadanBannerEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isHajjBannerEnabled: StateFlow<Boolean> = prefs.isHajjBannerEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isTaraweehAlertEnabled: StateFlow<Boolean> = prefs.isTaraweehAlertEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isSuhoorAlertEnabled: StateFlow<Boolean> = prefs.isSuhoorAlertEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isArafahAlertEnabled: StateFlow<Boolean> = prefs.isArafahAlertEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val seasonStreamSource: StateFlow<SeasonStreamSource> = prefs.seasonStreamSource
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SeasonStreamSource.MAKKAH)

    val khatmahCompletedJuz: StateFlow<Set<Int>> = prefs.khatmahCompletedJuz
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    // Live Qibla state from sensor
    val qiblaState: StateFlow<QiblaInfo> = sensorManager.qiblaState

    // Periodic second clock for live countdown
    private val _currentTimeMillis = MutableStateFlow(System.currentTimeMillis())
    val currentTimeMillis: StateFlow<Long> = _currentTimeMillis.asStateFlow()

    private val _isLocating = MutableStateFlow(false)
    val isLocating: StateFlow<Boolean> = _isLocating.asStateFlow()

    private val _locationError = MutableStateFlow<String?>(null)
    val locationError: StateFlow<String?> = _locationError.asStateFlow()

    // City search in dialog
    val citySearchQuery = MutableStateFlow("")
    val filteredCities: StateFlow<List<CityLocation>> = citySearchQuery.combine(
        MutableStateFlow(PresetCities.list)
    ) { query, list ->
        if (query.isBlank()) list else PresetCities.searchCities(query)
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PresetCities.list)

    // Current daily prayer schedule combined with ticking clock
    private val locationParams = combine(latitude, longitude, timeZoneId, cityName, countryName) { lat, lng, tzId, city, country ->
        Triple(lat to lng, tzId, city to country)
    }

    private val methodParams = combine(calculationMethod, asrJuristic, minuteOffsets) { method, juristic, offsets ->
        Triple(method, juristic, offsets)
    }

    val dailySchedule: StateFlow<DailyPrayerSchedule?> = combine(
        locationParams,
        methodParams,
        _currentTimeMillis
    ) { (coords, tzId, names), (method, juristic, offsets), _ ->
        val tz = TimeZone.getTimeZone(tzId)
        val cal = Calendar.getInstance(tz)
        PrayerCalculationEngine.calculatePrayerTimes(
            calendar = cal,
            latitude = coords.first,
            longitude = coords.second,
            timeZone = tz,
            method = method,
            asrJuristic = juristic,
            minuteOffsets = offsets,
            cityName = names.first,
            countryName = names.second
        )
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Islamic Season State combined calculation
    private val seasonPreferences = combine(
        seasonModeOverride,
        hijriOffsetDays,
        isRamadanBannerEnabled,
        isHajjBannerEnabled,
        seasonStreamSource
    ) { overrideMode, offset, ramadanEnabled, hajjEnabled, streamSource ->
        Triple(overrideMode to offset, ramadanEnabled to hajjEnabled, streamSource)
    }

    val islamicSeasonState: StateFlow<IslamicSeasonState> = combine(
        dailySchedule,
        seasonPreferences,
        khatmahCompletedJuz,
        _currentTimeMillis
    ) { schedule, (modeAndOffset, banners, streamSource), completedJuz, now ->
        val (overrideMode, offset) = modeAndOffset
        val (ramadanBanner, hajjBanner) = banners

        val tz = TimeZone.getTimeZone(timeZoneId.value)
        val cal = Calendar.getInstance(tz).apply { timeInMillis = now }

        val hijriDetails = IslamicSeasonEngine.computeHijriDate(cal, offset)
        val resolvedSeason = IslamicSeasonEngine.resolveSeason(hijriDetails, overrideMode)

        val isRamadan = resolvedSeason == IslamicSeason.RAMADAN
        val isHajj = resolvedSeason == IslamicSeason.HAJJ || resolvedSeason == IslamicSeason.ARAFAH
        val isArafah = resolvedSeason == IslamicSeason.ARAFAH || (resolvedSeason == IslamicSeason.HAJJ && hijriDetails.day == 9)
        val isEid = resolvedSeason == IslamicSeason.EID_FITR || resolvedSeason == IslamicSeason.EID_ADHA

        val (taraweehStatus, millisUntilTaraweeh) = IslamicSeasonEngine.calculateTaraweehStatus(schedule, now)
        val nightMilestones = IslamicSeasonEngine.calculateNightMilestones(schedule, now)

        val currentRitual = if (isHajj || isArafah) {
            IslamicSeasonEngine.getHajjRitualForDay(if (isArafah) 9 else hijriDetails.day)
        } else null

        val dailyDua = if (isRamadan) {
            IslamicSeasonEngine.getDuaForDay(hijriDetails.day)
        } else null

        val liveStation = IslamicSeasonEngine.getStationForSource(streamSource)

        IslamicSeasonState(
            season = resolvedSeason,
            overrideMode = overrideMode,
            hijriDetails = hijriDetails,
            isRamadanActive = isRamadan,
            isHajjActive = isHajj,
            isArafahActive = isArafah,
            isEidActive = isEid,
            ramadanBannerEnabled = ramadanBanner,
            hajjBannerEnabled = hajjBanner,
            taraweehLiveStatus = taraweehStatus,
            millisUntilTaraweeh = millisUntilTaraweeh,
            millisUntilIftar = nightMilestones.millisUntilIftar,
            millisUntilImsak = nightMilestones.millisUntilImsak,
            millisUntilLastThird = nightMilestones.millisUntilLastThird,
            iftarTimeFormatted = nightMilestones.iftarFormatted,
            imsakTimeFormatted = nightMilestones.imsakFormatted,
            lastThirdTimeFormatted = nightMilestones.lastThirdFormatted,
            currentHajjRitual = currentRitual,
            dailyDua = dailyDua,
            khatmahState = RamadanKhatmahState(completedJuzSet = completedJuz),
            selectedStreamSource = streamSource,
            liveSeasonStation = liveStation
        )
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), IslamicSeasonState())

    init {
        // Start continuous 1-second ticker for countdown and prayer updates
        viewModelScope.launch {
            while (isActive) {
                _currentTimeMillis.value = System.currentTimeMillis()
                delay(1000L)
            }
        }

        // Synchronize sensor coordinates whenever location updates
        viewModelScope.launch {
            combine(latitude, longitude) { lat, lng ->
                Pair(lat, lng)
            }.collect { (lat, lng) ->
                sensorManager.updateCoordinates(lat, lng)
            }
        }
    }

    fun hasLocationPermission(): Boolean {
        return locationManager.hasLocationPermission()
    }

    fun refreshLocation() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLocating.value = true
            _locationError.value = null
            try {
                val result = locationManager.getCurrentLocation()
                if (result != null) {
                    prefs.setLocation(
                        latitude = result.latitude,
                        longitude = result.longitude,
                        cityName = result.cityName,
                        countryName = result.countryName,
                        countryCode = result.countryCode,
                        timeZoneId = result.timeZoneId,
                        isAuto = true
                    )
                    AdhanAlarmScheduler.scheduleUpcomingAlarms(app)
                } else {
                    _locationError.value = "تعذر تحديد الموقع تلقائيًا، يمكنك اختيار مدينتك يدويًا."
                }
            } catch (e: Exception) {
                _locationError.value = e.localizedMessage ?: "حدث خطأ أثناء تحديد الموقع"
            } finally {
                _isLocating.value = false
            }
        }
    }

    fun selectPresetCity(city: CityLocation) {
        viewModelScope.launch(Dispatchers.IO) {
            prefs.setLocation(
                latitude = city.latitude,
                longitude = city.longitude,
                cityName = city.nameAr,
                countryName = city.countryAr,
                countryCode = city.countryCode,
                timeZoneId = city.timeZoneId,
                isAuto = false
            )
            prefs.setCalculationMethod(city.recommendedMethod)
            AdhanAlarmScheduler.scheduleUpcomingAlarms(app)
        }
    }

    fun setCalculationMethod(method: CalculationMethod) {
        viewModelScope.launch(Dispatchers.IO) {
            prefs.setCalculationMethod(method)
            AdhanAlarmScheduler.scheduleUpcomingAlarms(app)
        }
    }

    fun setAsrJuristic(juristic: AsrJuristic) {
        viewModelScope.launch(Dispatchers.IO) {
            prefs.setAsrJuristic(juristic)
            AdhanAlarmScheduler.scheduleUpcomingAlarms(app)
        }
    }

    fun setAdhanSound(sound: AdhanSound) {
        viewModelScope.launch(Dispatchers.IO) {
            prefs.setAdhanSound(sound)
        }
    }

    fun togglePrayerAlert(type: PrayerType, enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            prefs.togglePrayerAlert(type, enabled)
            AdhanAlarmScheduler.scheduleUpcomingAlarms(app)
        }
    }

    fun toggleIqamahAlert(type: PrayerType, enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            prefs.toggleIqamahAlert(type, enabled)
            AdhanAlarmScheduler.scheduleUpcomingAlarms(app)
        }
    }

    fun setIqamahDelay(type: PrayerType, minutes: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            prefs.setIqamahDelay(type, minutes)
            AdhanAlarmScheduler.scheduleUpcomingAlarms(app)
        }
    }

    fun setMinuteOffset(type: PrayerType, minutes: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            prefs.setMinuteOffset(type, minutes)
            AdhanAlarmScheduler.scheduleUpcomingAlarms(app)
        }
    }

    fun startQiblaSensors() {
        sensorManager.updateCoordinates(latitude.value, longitude.value)
        sensorManager.startListening()
    }

    fun stopQiblaSensors() {
        sensorManager.stopListening()
    }

    fun isBatteryOptimizationIgnored(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            return powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: true
        }
        return true
    }

    fun requestIgnoreBatteryOptimization(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                // Fallback to battery saver settings
                try {
                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }
    }

    /**
     * Generate weekly schedules for Calendar preview sheet.
     */
    fun getWeeklySchedule(daysAhead: Int = 7): List<DailyPrayerSchedule> {
        val lat = latitude.value
        val lng = longitude.value
        val tz = TimeZone.getTimeZone(timeZoneId.value)
        val method = calculationMethod.value
        val juristic = asrJuristic.value
        val offsets = minuteOffsets.value
        val city = cityName.value
        val country = countryName.value

        val list = mutableListOf<DailyPrayerSchedule>()
        for (i in 0 until daysAhead) {
            val cal = Calendar.getInstance(tz).apply {
                add(Calendar.DAY_OF_YEAR, i)
            }
            list.add(
                PrayerCalculationEngine.calculatePrayerTimes(
                    calendar = cal,
                    latitude = lat,
                    longitude = lng,
                    timeZone = tz,
                    method = method,
                    asrJuristic = juristic,
                    minuteOffsets = offsets,
                    cityName = city,
                    countryName = country
                )
            )
        }
        return list
    }

    fun setSeasonModeOverride(mode: SeasonModeOverride) {
        viewModelScope.launch(Dispatchers.IO) {
            prefs.setSeasonModeOverride(mode)
        }
    }

    fun setHijriOffsetDays(offset: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            prefs.setHijriOffsetDays(offset)
        }
    }

    fun setRamadanBannerEnabled(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            prefs.setRamadanBannerEnabled(enabled)
        }
    }

    fun setHajjBannerEnabled(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            prefs.setHajjBannerEnabled(enabled)
        }
    }

    fun setTaraweehAlertEnabled(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            prefs.setTaraweehAlertEnabled(enabled)
        }
    }

    fun setSuhoorAlertEnabled(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            prefs.setSuhoorAlertEnabled(enabled)
        }
    }

    fun setArafahAlertEnabled(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            prefs.setArafahAlertEnabled(enabled)
        }
    }

    fun setSeasonStreamSource(source: SeasonStreamSource) {
        viewModelScope.launch(Dispatchers.IO) {
            prefs.setSeasonStreamSource(source)
        }
    }

    fun toggleKhatmahJuz(juzNumber: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            prefs.toggleKhatmahJuz(juzNumber)
        }
    }

    fun resetKhatmah() {
        viewModelScope.launch(Dispatchers.IO) {
            prefs.resetKhatmah()
        }
    }

    fun setAdhanMasterEnabled(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            prefs.setAdhanMasterEnabled(enabled)
            AdhanAlarmScheduler.scheduleUpcomingAlarms(app)
        }
    }

    fun triggerAdhanNotificationTest(prayerType: PrayerType = PrayerType.MAGHRIB) {
        app.adhanManager.playAdhan(prayerType, isTest = true)
    }

    fun stopAdhanService() {
        app.adhanManager.stopAdhan()
    }
}
