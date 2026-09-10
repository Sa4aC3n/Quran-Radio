package com.example.data

import android.content.Context
import android.util.Log
import com.example.model.RadioStation
import com.example.model.StationCategory
import com.example.model.StationStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class AtheerImportReport(
    val totalFound: Int = 0,
    val newStations: Int = 0,
    val existingStations: Int = 0,
    val newBackupStreams: Int = 0,
    val duplicateUrls: Int = 0,
    val verifiedOnline: Int = 0,
    val offline: Int = 0,
    val unknown: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

data class MergeResult(
    val mergedStations: List<RadioStation>,
    val report: AtheerImportReport
)

class AtheerRadioImporter(
    private val context: Context,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()
) {

    companion object {
        private const val TAG = "AtheerRadioImporter"
        const val SEED_FILE = "atheer_stations_seed.json"
        const val ATHEER_API_BASE = "https://api.mp3quran.net/api_2/atheer"
    }

    /**
     * Loads the bundled verified seed stations from assets.
     */
    suspend fun loadFromSeed(): List<RadioStation> = withContext(Dispatchers.IO) {
        val stations = mutableListOf<RadioStation>()
        try {
            val jsonStr = context.assets.open(SEED_FILE).bufferedReader().use { it.readText() }
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val station = parseStationJson(obj)
                if (station != null) {
                    stations.add(station)
                }
            }
            Log.d(TAG, "[ATHEER_IMPORT] Loaded ${stations.size} stations from assets seed.")
        } catch (e: Exception) {
            Log.e(TAG, "[ATHEER_IMPORT] Error reading seed JSON", e)
        }
        stations
    }

    /**
     * Fetches live categories and radios directly from Atheer API endpoints.
     */
    suspend fun fetchLiveFromAtheer(): List<RadioStation> = withContext(Dispatchers.IO) {
        val stations = mutableListOf<RadioStation>()
        try {
            val catsReq = Request.Builder()
                .url("$ATHEER_API_BASE/radios_cats?language=ar")
                .header("User-Agent", "QuranRadioWorld/1.0")
                .build()

            val reads = client.newCall(catsReq).execute().use { catsResp ->
                if (!catsResp.isSuccessful) {
                    Log.w(TAG, "[ATHEER_IMPORT] Failed to fetch categories: ${catsResp.code}")
                    return@withContext loadFromSeed()
                }
                val catsBody = catsResp.body?.string() ?: ""
                val catsJson = JSONObject(catsBody)
                catsJson.optJSONArray("reads") ?: JSONArray()
            }

            for (i in 0 until reads.length()) {
                val catObj = reads.getJSONObject(i)
                val catId = catObj.optInt("id")
                val catName = catObj.optString("name")

                val radiosReq = Request.Builder()
                    .url("$ATHEER_API_BASE/radios?radio_cat=$catId&language=ar")
                    .header("User-Agent", "QuranRadioWorld/1.0")
                    .build()

                client.newCall(radiosReq).execute().use { radiosResp ->
                    if (radiosResp.isSuccessful) {
                        val radiosBody = radiosResp.body?.string() ?: ""
                        val rJson = JSONObject(radiosBody)
                        val rArray = rJson.optJSONArray("radios") ?: rJson.optJSONArray("reads") ?: JSONArray()

                        for (j in 0 until rArray.length()) {
                            val rObj = rArray.getJSONObject(j)
                            val id = rObj.optInt("id")
                            val name = rObj.optString("name")
                            val rawUrl = rObj.optString("URL")

                            if (rawUrl.isNotBlank() && rawUrl.startsWith("http")) {
                                val primaryUrl = if (rawUrl.contains("backup.qurango.net")) {
                                    rawUrl.replace("backup.qurango.net", "qurango.net")
                                } else {
                                    rawUrl
                                }
                                val backupUrl = if (rawUrl.contains("backup.qurango.net")) {
                                    rawUrl
                                } else if (rawUrl.contains("qurango.net")) {
                                    rawUrl.replace("qurango.net", "backup.qurango.net")
                                } else null

                                val catEnum = mapCategory(catId, catName)
                                val reciterName = if (catEnum == StationCategory.RECITER) name else null

                                val streamList = mutableListOf<StationStream>()
                                streamList.add(
                                    StationStream(
                                        id = "atheer_${id}_primary",
                                        stationId = "atheer_$id",
                                        url = primaryUrl,
                                        priority = 1,
                                        isPrimary = true,
                                        isBackup = false,
                                        status = "ONLINE",
                                        healthScore = 100
                                    )
                                )
                                if (backupUrl != null) {
                                    streamList.add(
                                        StationStream(
                                            id = "atheer_${id}_backup",
                                            stationId = "atheer_$id",
                                            url = backupUrl,
                                            priority = 2,
                                            isPrimary = false,
                                            isBackup = true,
                                            status = "ONLINE",
                                            healthScore = 95
                                        )
                                    )
                                }

                                stations.add(
                                    RadioStation(
                                        id = "atheer_$id",
                                        name = name,
                                        nameAr = name,
                                        country = if (name.contains("السعودية") || catId == 13) "المملكة العربية السعودية" else "العالم الإسلامي",
                                        countryCode = if (name.contains("السعودية") || catId == 13) "SA" else "GLOBAL",
                                        category = catEnum,
                                        reciter = reciterName,
                                        reciterId = if (reciterName != null) "reciter_$id" else null,
                                        streamUrl = primaryUrl,
                                        fallbackStreamUrl = backupUrl,
                                        streams = streamList,
                                        sourceName = "Atheer Radio",
                                        sourceWebsite = "https://www.atheer-radio.com/library",
                                        sourceUrl = "https://www.atheer-radio.com/library/$catId",
                                        logoUrl = "https://www.atheer-radio.com/assets/imgs/cat$catId.png",
                                        description = "بث مباشر لـ $name عبر شبكة إذاعات أثير راديو.",
                                        isVerified = true,
                                        streamStatus = "ONLINE",
                                        priority = 50
                                    )
                                )
                            }
                        }
                    }
                }
            }
            Log.d(TAG, "[ATHEER_IMPORT] Live API fetch completed: ${stations.size} stations.")
        } catch (e: Exception) {
            Log.e(TAG, "[ATHEER_IMPORT] Live fetch error, falling back to seed", e)
            return@withContext loadFromSeed()
        }
        if (stations.isEmpty()) loadFromSeed() else stations
    }

    /**
     * Merges Atheer stations with existing application database without data loss.
     * Duplicate stations will have their new stream added as a Backup stream.
     */
    fun merge(existingStations: List<RadioStation>, atheerStations: List<RadioStation>): MergeResult {
        val resultList = existingStations.toMutableList()
        var newStationCount = 0
        var existingMatchedCount = 0
        var backupStreamsAdded = 0
        var duplicateUrlsCount = 0
        var verifiedCount = 0
        var offlineCount = 0

        for (atheer in atheerStations) {
            if (atheer.isVerified && atheer.streamStatus == "ONLINE") {
                verifiedCount++
            } else if (atheer.streamStatus == "OFFLINE") {
                offlineCount++
            }

            // Find matching existing station by URL or Normalized Name
            val existingIndex = resultList.indexOfFirst { ex ->
                ex.id == atheer.id ||
                AtheerNormalizer.areUrlsEquivalent(ex.streamUrl, atheer.streamUrl) ||
                (AtheerNormalizer.areNamesEquivalent(ex.name, atheer.name) && ex.category == atheer.category)
            }

            if (existingIndex != -1) {
                // Existing station matched! Add Atheer's stream as Backup stream if different
                existingMatchedCount++
                val exStation = resultList[existingIndex]
                val existingStreams = if (exStation.streams.isNotEmpty()) {
                    exStation.streams.toMutableList()
                } else {
                    val initialList = mutableListOf<StationStream>()
                    initialList.add(
                        StationStream(
                            id = "${exStation.id}_primary",
                            stationId = exStation.id,
                            url = exStation.streamUrl,
                            priority = 1,
                            isPrimary = true,
                            isBackup = false,
                            status = "ONLINE",
                            healthScore = 100
                        )
                    )
                    if (!exStation.fallbackStreamUrl.isNullOrBlank()) {
                        initialList.add(
                            StationStream(
                                id = "${exStation.id}_backup",
                                stationId = exStation.id,
                                url = exStation.fallbackStreamUrl,
                                priority = 2,
                                isPrimary = false,
                                isBackup = true,
                                status = "ONLINE",
                                healthScore = 95
                            )
                        )
                    }
                    initialList
                }

                // Check if atheer streams are already in existing streams
                var streamAdded = false
                for (s in atheer.streams) {
                    val isUrlPresent = existingStreams.any { AtheerNormalizer.areUrlsEquivalent(it.url, s.url) }
                    if (!isUrlPresent) {
                        val newBackup = s.copy(
                            id = "${exStation.id}_atheer_backup_${existingStreams.size + 1}",
                            stationId = exStation.id,
                            priority = existingStreams.size + 1,
                            isPrimary = false,
                            isBackup = true
                        )
                        existingStreams.add(newBackup)
                        backupStreamsAdded++
                        streamAdded = true
                        Log.d(TAG, "[ATHEER_STREAM] Backup Added for '${exStation.name}': ${s.url}")
                    } else {
                        duplicateUrlsCount++
                    }
                }

                val fallbackUrl = exStation.fallbackStreamUrl ?: atheer.fallbackStreamUrl ?: atheer.streamUrl
                resultList[existingIndex] = exStation.copy(
                    streams = existingStreams,
                    fallbackStreamUrl = if (fallbackUrl != exStation.streamUrl) fallbackUrl else exStation.fallbackStreamUrl,
                    isVerified = true
                )
            } else {
                // Completely new station!
                newStationCount++
                val preparedStreams = if (atheer.streams.isNotEmpty()) {
                    atheer.streams
                } else {
                    listOf(
                        StationStream(
                            id = "${atheer.id}_primary",
                            stationId = atheer.id,
                            url = atheer.streamUrl,
                            priority = 1,
                            isPrimary = true,
                            isBackup = false,
                            status = "ONLINE",
                            healthScore = 100
                        )
                    )
                }

                resultList.add(
                    atheer.copy(
                        streams = preparedStreams,
                        sourceName = "Atheer Radio",
                        sourceWebsite = "https://www.atheer-radio.com/library"
                    )
                )
                Log.d(TAG, "[ATHEER_IMPORT] Station Found & Added: ${atheer.name}")
            }
        }

        val report = AtheerImportReport(
            totalFound = atheerStations.size,
            newStations = newStationCount,
            existingStations = existingMatchedCount,
            newBackupStreams = backupStreamsAdded,
            duplicateUrls = duplicateUrlsCount,
            verifiedOnline = verifiedCount,
            offline = offlineCount,
            unknown = atheerStations.size - (verifiedCount + offlineCount),
            timestamp = System.currentTimeMillis()
        )

        Log.i(TAG, "[ATHEER_IMPORT] MERGE SUMMARY: $report")
        return MergeResult(mergedStations = resultList, report = report)
    }

    private fun mapCategory(catId: Int, catName: String): StationCategory {
        val c = catName.trim()
        return when {
            catId == 1 || c.contains("قراء") -> StationCategory.RECITER
            catId == 2 || c.contains("تفسير") -> StationCategory.TAFSIR
            catId == 3 || c.contains("ترجمة") || c.contains("ترجمات") -> StationCategory.TRANSLATION
            catId == 4 || c.contains("سيرة") || c.contains("قصص") || c.contains("صحابة") || c.contains("أنبياء") -> StationCategory.SEERAH_AND_STORIES
            catId == 5 || c.contains("تلاوات") || c.contains("خاشع") || c.contains("متميز") || c.contains("تراتيل") -> StationCategory.RECITATION
            catId == 6 || c.contains("رقية") || c.contains("سكينة") -> StationCategory.RUQYAH
            catId == 7 || c.contains("فتاوى") || c.contains("فتاوي") || c.contains("فقه") -> StationCategory.FATWA
            catId == 8 || c.contains("أذكار") || c.contains("أدعية") -> StationCategory.ADHKAR
            catId == 12 || c.contains("سنة") || c.contains("حديث") -> StationCategory.SUNNAH
            catId == 13 || c.contains("سعودية") || c.contains("إذاعة القرآن الكريم") -> StationCategory.OFFICIAL
            else -> StationCategory.ISLAMIC
        }
    }

    private fun parseStationJson(obj: JSONObject): RadioStation? {
        return try {
            val id = obj.optString("id")
            val name = obj.optString("name", obj.optString("nameAr", ""))
            val streamUrl = obj.optString("streamUrl")
            if (id.isBlank() || name.isBlank() || streamUrl.isBlank()) return null

            val fallbackStreamUrl = obj.optString("fallbackStreamUrl").takeIf { it.isNotBlank() }
            val catStr = obj.optString("category", "QURAN")
            val category = try {
                StationCategory.valueOf(catStr)
            } catch (e: Exception) {
                StationCategory.QURAN
            }

            val streamsArray = obj.optJSONArray("streams")
            val streams = mutableListOf<StationStream>()
            if (streamsArray != null) {
                for (k in 0 until streamsArray.length()) {
                    val sObj = streamsArray.getJSONObject(k)
                    streams.add(
                        StationStream(
                            id = sObj.optString("id", "${id}_stream_$k"),
                            stationId = id,
                            url = sObj.optString("url"),
                            format = sObj.optString("format", "MP3"),
                            priority = sObj.optInt("priority", k + 1),
                            isPrimary = sObj.optBoolean("isPrimary", k == 0),
                            isBackup = sObj.optBoolean("isBackup", k > 0),
                            isActive = sObj.optBoolean("isActive", true),
                            status = sObj.optString("status", "ONLINE"),
                            healthScore = sObj.optInt("healthScore", 100)
                        )
                    )
                }
            } else {
                streams.add(
                    StationStream(
                        id = "${id}_primary",
                        stationId = id,
                        url = streamUrl,
                        priority = 1,
                        isPrimary = true,
                        isBackup = false,
                        status = "ONLINE",
                        healthScore = 100
                    )
                )
                if (fallbackStreamUrl != null) {
                    streams.add(
                        StationStream(
                            id = "${id}_backup",
                            stationId = id,
                            url = fallbackStreamUrl,
                            priority = 2,
                            isPrimary = false,
                            isBackup = true,
                            status = "ONLINE",
                            healthScore = 95
                        )
                    )
                }
            }

            RadioStation(
                id = id,
                name = name,
                nameAr = obj.optString("nameAr", name),
                nameEn = obj.optString("nameEn", ""),
                country = obj.optString("country", "العالم الإسلامي"),
                countryCode = obj.optString("countryCode", "GLOBAL"),
                city = obj.optString("city", ""),
                category = category,
                reciter = obj.optString("reciter").takeIf { it.isNotBlank() },
                reciterId = obj.optString("reciterId").takeIf { it.isNotBlank() },
                streamUrl = streamUrl,
                fallbackStreamUrl = fallbackStreamUrl,
                streams = streams,
                sourceName = obj.optString("sourceName", "Atheer Radio"),
                sourceWebsite = obj.optString("sourceWebsite", "https://www.atheer-radio.com/library"),
                sourceUrl = obj.optString("sourceUrl", ""),
                logoUrl = obj.optString("logoUrl", ""),
                description = obj.optString("description", ""),
                isVerified = obj.optBoolean("isVerified", true),
                streamStatus = obj.optString("streamStatus", "ONLINE"),
                bitrate = obj.optString("bitrate", "128 kbps"),
                isFeatured = obj.optBoolean("isFeatured", false),
                priority = obj.optInt("priority", 50)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing station object", e)
            null
        }
    }
}
