package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.QuranRadioApplication
import com.example.model.AppThemeMode
import com.example.model.Country
import com.example.model.PlayerState
import com.example.model.RadioStation
import com.example.model.Reciter
import com.example.model.StationCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RadioViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as QuranRadioApplication
    private val radioRepository = app.radioRepository
    private val userPreferencesRepository = app.userPreferencesRepository
    private val playerManager = app.playerManager

    val playerState: StateFlow<PlayerState> = playerManager.playerState

    val themeMode: StateFlow<AppThemeMode> = userPreferencesRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppThemeMode.LIGHT)

    val favoriteIds: StateFlow<Set<String>> = userPreferencesRepository.favoriteStationIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val isAutoPlayEnabled: StateFlow<Boolean> = userPreferencesRepository.isAutoPlayEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isBackgroundPlayEnabled: StateFlow<Boolean> = userPreferencesRepository.isBackgroundPlayEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isWifiOnlyEnabled: StateFlow<Boolean> = userPreferencesRepository.isWifiOnlyEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isNotificationsEnabled: StateFlow<Boolean> = userPreferencesRepository.isNotificationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val preferredAudioQuality: StateFlow<String> = userPreferencesRepository.preferredAudioQuality
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "high")

    val countries: StateFlow<List<Country>> = radioRepository.countries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reciters: StateFlow<List<Reciter>> = radioRepository.reciters
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStations: StateFlow<List<RadioStation>> = combine(
        radioRepository.stations,
        favoriteIds
    ) { stationsList, favSet ->
        stationsList.map { station ->
            station.copy(isFavorite = favSet.contains(station.id))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteStations: StateFlow<List<RadioStation>> = allStations.combine(favoriteIds) { stations, _ ->
        stations.filter { it.isFavorite }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val featuredStations: StateFlow<List<RadioStation>> = allStations.combine(favoriteIds) { stations, _ ->
        stations.filter { it.isFeatured }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search and Filter state
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow<StationCategory?>(null)

    val searchResults: StateFlow<List<RadioStation>> = combine(
        allStations,
        searchQuery,
        selectedCategory
    ) { stations, query, category ->
        val trimmed = query.trim()
        stations.filter { station ->
            val matchesCategory = category == null || 
                    station.category == category || 
                    (category == StationCategory.TAFSIR && station.category == StationCategory.TAFSEER) ||
                    (category == StationCategory.TAFSEER && station.category == StationCategory.TAFSIR) ||
                    (category == StationCategory.SEERAH_AND_STORIES && station.category == StationCategory.SEERAH) ||
                    (category == StationCategory.SEERAH && station.category == StationCategory.SEERAH_AND_STORIES) ||
                    (category == StationCategory.RUQYAH && station.category == StationCategory.ROQIAH) ||
                    (category == StationCategory.ROQIAH && station.category == StationCategory.RUQYAH) ||
                    (category == StationCategory.FATWA && station.category == StationCategory.FATAWA) ||
                    (category == StationCategory.FATAWA && station.category == StationCategory.FATWA)
            val matchesQuery = if (trimmed.isBlank()) {
                true
            } else {
                station.name.contains(trimmed, ignoreCase = true) ||
                        station.country.contains(trimmed, ignoreCase = true) ||
                        station.city.contains(trimmed, ignoreCase = true) ||
                        (station.reciter?.contains(trimmed, ignoreCase = true) == true) ||
                        station.description.contains(trimmed, ignoreCase = true)
            }
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        checkAutoPlayOnStartup()
    }

    private fun checkAutoPlayOnStartup() {
        viewModelScope.launch {
            val autoPlay = userPreferencesRepository.isAutoPlayEnabled.first()
            if (autoPlay) {
                val lastId = userPreferencesRepository.lastPlayedStationId.first()
                if (lastId != null) {
                    val station = radioRepository.getStationById(lastId)
                    if (station != null && station.isActive) {
                        playStation(station)
                    }
                }
            }
        }
    }

    fun playStation(station: RadioStation) {
        playerManager.playStation(station)
    }

    fun togglePlayPause() {
        playerManager.togglePlayPause()
    }

    fun pause() {
        playerManager.pause()
    }

    fun resume() {
        playerManager.resume()
    }

    fun stop() {
        playerManager.stop()
    }

    fun retry() {
        playerManager.retry()
    }

    fun playNext() {
        playerManager.playNextStation(allStations.value)
    }

    fun playPrevious() {
        playerManager.playPreviousStation(allStations.value)
    }

    fun toggleFavorite(stationId: String) {
        viewModelScope.launch {
            userPreferencesRepository.toggleFavorite(stationId)
        }
    }

    fun setSleepTimer(minutes: Int) {
        playerManager.setSleepTimer(minutes)
    }

    fun cancelSleepTimer() {
        playerManager.cancelSleepTimer()
    }

    fun setAutoPlay(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setAutoPlay(enabled)
        }
    }

    fun setBackgroundPlay(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setBackgroundPlay(enabled)
        }
    }

    fun setWifiOnly(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setWifiOnly(enabled)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setNotificationsEnabled(enabled)
        }
    }

    fun setAudioQuality(quality: String) {
        viewModelScope.launch {
            userPreferencesRepository.setPreferredAudioQuality(quality)
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch {
            userPreferencesRepository.setThemeMode(mode)
        }
    }

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun selectCategory(category: StationCategory?) {
        selectedCategory.value = category
    }

    fun getStationsForCountry(countryCode: String): List<RadioStation> {
        return allStations.value.filter { it.countryCode.equals(countryCode, ignoreCase = true) }
    }

    val importReport: StateFlow<com.example.data.AtheerImportReport?> = radioRepository.importReport
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isSyncing: StateFlow<Boolean> = radioRepository.isSyncing
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun syncAtheerRadios() {
        viewModelScope.launch {
            radioRepository.syncWithAtheerLive()
        }
    }

    fun getCountry(countryCode: String): Country? {
        return radioRepository.getCountryByCode(countryCode)
    }

    fun getReciter(reciterId: String): Reciter? {
        return radioRepository.getReciterById(reciterId)
    }

    fun getStationsForReciter(reciterId: String): List<RadioStation> {
        return allStations.value.filter { it.reciterId == reciterId }
    }

    fun updateStationStream(stationId: String, newUrl: String, isActive: Boolean = true) {
        radioRepository.updateStationStream(stationId, newUrl, isActive)
    }

    fun addCustomStation(station: RadioStation) {
        radioRepository.addCustomStation(station)
    }

    fun deleteStation(stationId: String) {
        radioRepository.deleteStation(stationId)
    }
}
