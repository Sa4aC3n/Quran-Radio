package com.example.data

import android.content.Context
import android.util.Log
import com.example.model.Country
import com.example.model.RadioStation
import com.example.model.Reciter
import com.example.model.StationCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class RadioRepository(context: Context) {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(Dispatchers.IO)
    private val importer = AtheerRadioImporter(appContext)

    private val _stations = MutableStateFlow<List<RadioStation>>(DefaultStationsData.STATIONS)
    val stations: Flow<List<RadioStation>> = _stations.asStateFlow()

    private val _countries = MutableStateFlow<List<Country>>(DefaultStationsData.COUNTRIES)
    val countries: Flow<List<Country>> = _countries.asStateFlow()

    private val _reciters = MutableStateFlow<List<Reciter>>(DefaultStationsData.RECITERS)
    val reciters: Flow<List<Reciter>> = _reciters.asStateFlow()

    private val _importReport = MutableStateFlow<AtheerImportReport?>(null)
    val importReport: Flow<AtheerImportReport?> = _importReport.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: Flow<Boolean> = _isSyncing.asStateFlow()

    init {
        // Initial merge with verified Atheer Seed Stations
        scope.launch {
            try {
                val seedStations = importer.loadFromSeed()
                if (seedStations.isNotEmpty()) {
                    val mergeResult = importer.merge(DefaultStationsData.STATIONS, seedStations)
                    _stations.value = mergeResult.mergedStations
                    _importReport.value = mergeResult.report
                    Log.i("RadioRepository", "[ATHEER_IMPORT] Seed merge completed. Total stations in DB: ${_stations.value.size}")
                }
            } catch (e: Exception) {
                Log.e("RadioRepository", "[ATHEER_IMPORT] Error during initial seed merge", e)
            }
        }
    }

    /**
     * Re-syncs stations by fetching live data from Atheer Radio and merging safely.
     */
    suspend fun syncWithAtheerLive(): AtheerImportReport = withContext(Dispatchers.IO) {
        _isSyncing.value = true
        try {
            val liveStations = importer.fetchLiveFromAtheer()
            val mergeResult = importer.merge(_stations.value, liveStations)
            _stations.value = mergeResult.mergedStations
            _importReport.value = mergeResult.report
            _isSyncing.value = false
            mergeResult.report
        } catch (e: Exception) {
            Log.e("RadioRepository", "[ATHEER_IMPORT] Live sync failed", e)
            _isSyncing.value = false
            _importReport.value ?: AtheerImportReport()
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    fun getStationById(stationId: String): RadioStation? {
        return _stations.value.find { it.id == stationId }
    }

    fun getCountryByCode(code: String): Country? {
        return _countries.value.find { it.code.equals(code, ignoreCase = true) }
    }

    fun getReciterById(reciterId: String): Reciter? {
        return _reciters.value.find { it.id == reciterId }
    }

    fun getStationsByCountry(countryCode: String): Flow<List<RadioStation>> {
        return combine(stations, MutableStateFlow(countryCode)) { list, code ->
            list.filter { it.countryCode.equals(code, ignoreCase = true) }
        }
    }

    fun getStationsByCategory(category: StationCategory): Flow<List<RadioStation>> {
        return combine(stations, MutableStateFlow(category)) { list, cat ->
            list.filter { it.category == cat }
        }
    }

    fun getFeaturedStations(): Flow<List<RadioStation>> {
        return combine(stations, MutableStateFlow(Unit)) { list, _ ->
            list.filter { it.isFeatured }
        }
    }

    fun getReciterStations(): Flow<List<RadioStation>> {
        return combine(stations, MutableStateFlow(Unit)) { list, _ ->
            list.filter { it.category == StationCategory.RECITER || it.reciter != null }
        }
    }

    /**
     * Admin/Remote Config capability: update or add stations dynamically without releasing a new app version.
     */
    fun updateStationStream(stationId: String, newStreamUrl: String, isActive: Boolean = true) {
        val currentList = _stations.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == stationId }
        if (index != -1) {
            val existing = currentList[index]
            currentList[index] = existing.copy(streamUrl = newStreamUrl, isActive = isActive)
            _stations.value = currentList
        }
    }

    fun addCustomStation(station: RadioStation) {
        val currentList = _stations.value.toMutableList()
        currentList.removeAll { it.id == station.id }
        currentList.add(0, station)
        _stations.value = currentList
    }

    fun deleteStation(stationId: String) {
        val currentList = _stations.value.toMutableList()
        currentList.removeAll { it.id == stationId }
        _stations.value = currentList
    }

    /**
     * Test stream health on-demand before starting playback.
     */
    suspend fun checkStreamHealth(url: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "QuranRadioWorld/1.0 (Android; ExoPlayer)")
                .header("Range", "bytes=0-100")
                .head()
                .build()

            val response = okHttpClient.newCall(request).execute()
            val isSuccess = response.isSuccessful || response.code in 200..399
            response.close()
            isSuccess
        } catch (e: Exception) {
            // Some shoutcast servers only answer GET requests
            try {
                val getRequest = Request.Builder()
                    .url(url)
                    .header("User-Agent", "QuranRadioWorld/1.0 (Android; ExoPlayer)")
                    .header("Range", "bytes=0-100")
                    .get()
                    .build()
                val response = okHttpClient.newCall(getRequest).execute()
                val isSuccess = response.isSuccessful || response.code in 200..399
                response.close()
                isSuccess
            } catch (e2: Exception) {
                false
            }
        }
    }
}
