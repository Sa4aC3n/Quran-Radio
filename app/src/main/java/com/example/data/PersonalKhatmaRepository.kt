package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.model.PersonalKhatma
import com.example.model.PersonalKhatmaSourceType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class PersonalKhatmaRepository(context: Context) {

    private val appContext = context.applicationContext
    private val KEY_PERSONAL_KHATMAS = stringPreferencesKey("personal_khatmas_list_json")
    private val KEY_ACTIVE_KHATMA_ID = stringPreferencesKey("personal_active_khatma_id")

    val activeKhatmaId: Flow<String?> = appContext.dataStore.data.map { preferences ->
        preferences[KEY_ACTIVE_KHATMA_ID]
    }

    val khatmasFlow: Flow<List<PersonalKhatma>> = appContext.dataStore.data.map { preferences ->
        val jsonString = preferences[KEY_PERSONAL_KHATMAS]
        if (jsonString.isNullOrBlank()) {
            val initial = getInitialDefaultKhatma()
            initial
        } else {
            try {
                deserializeKhatmas(jsonString)
            } catch (e: Exception) {
                getInitialDefaultKhatma()
            }
        }
    }

    suspend fun createKhatma(
        title: String,
        sourceName: String,
        sourceType: PersonalKhatmaSourceType,
        sourceId: String? = null,
        sourceLogoUrl: String? = null,
        startJuzNumber: Int = 1
    ): PersonalKhatma {
        val trimmedTitle = title.trim().ifBlank { "ختمتي من $sourceName" }
        val parts = MutableList(30) { index ->
            // If user started from e.g. Juz 5, parts 1..4 are already completed
            index < (startJuzNumber - 1)
        }

        val newKhatma = PersonalKhatma(
            id = UUID.randomUUID().toString(),
            title = trimmedTitle,
            sourceName = sourceName.trim(),
            sourceType = sourceType,
            sourceId = sourceId,
            sourceLogoUrl = sourceLogoUrl,
            createdAtMillis = System.currentTimeMillis(),
            lastUpdatedAtMillis = System.currentTimeMillis(),
            completedParts = parts,
            isArchived = false
        )

        updateKhatmasList { currentList ->
            listOf(newKhatma) + currentList
        }

        setActiveKhatmaId(newKhatma.id)
        return newKhatma
    }

    suspend fun togglePart(khatmaId: String, juzNumber: Int) {
        if (juzNumber !in 1..30) return
        val targetIndex = juzNumber - 1

        updateKhatmasList { list ->
            list.map { khatma ->
                if (khatma.id == khatmaId) {
                    val updatedParts = khatma.completedParts.toMutableList()
                    updatedParts[targetIndex] = !updatedParts[targetIndex]
                    khatma.copy(
                        completedParts = updatedParts,
                        lastUpdatedAtMillis = System.currentTimeMillis()
                    )
                } else khatma
            }
        }
        setActiveKhatmaId(khatmaId)
    }

    suspend fun setPart(khatmaId: String, juzNumber: Int, completed: Boolean) {
        if (juzNumber !in 1..30) return
        val targetIndex = juzNumber - 1

        updateKhatmasList { list ->
            list.map { khatma ->
                if (khatma.id == khatmaId) {
                    val updatedParts = khatma.completedParts.toMutableList()
                    updatedParts[targetIndex] = completed
                    khatma.copy(
                        completedParts = updatedParts,
                        lastUpdatedAtMillis = System.currentTimeMillis()
                    )
                } else khatma
            }
        }
        setActiveKhatmaId(khatmaId)
    }

    suspend fun markAllParts(khatmaId: String, completed: Boolean) {
        updateKhatmasList { list ->
            list.map { khatma ->
                if (khatma.id == khatmaId) {
                    khatma.copy(
                        completedParts = List(30) { completed },
                        lastUpdatedAtMillis = System.currentTimeMillis()
                    )
                } else khatma
            }
        }
    }

    suspend fun setActiveKhatmaId(khatmaId: String) {
        appContext.dataStore.edit { prefs ->
            prefs[KEY_ACTIVE_KHATMA_ID] = khatmaId
        }
    }

    suspend fun deleteKhatma(khatmaId: String) {
        updateKhatmasList { list ->
            list.filterNot { it.id == khatmaId }
        }
    }

    suspend fun toggleArchiveKhatma(khatmaId: String) {
        updateKhatmasList { list ->
            list.map { khatma ->
                if (khatma.id == khatmaId) {
                    khatma.copy(
                        isArchived = !khatma.isArchived,
                        lastUpdatedAtMillis = System.currentTimeMillis()
                    )
                } else khatma
            }
        }
    }

    private suspend fun updateKhatmasList(transform: (List<PersonalKhatma>) -> List<PersonalKhatma>) {
        appContext.dataStore.edit { prefs ->
            val currentJson = prefs[KEY_PERSONAL_KHATMAS]
            val currentList = if (currentJson.isNullOrBlank()) {
                getInitialDefaultKhatma()
            } else {
                try {
                    deserializeKhatmas(currentJson)
                } catch (e: Exception) {
                    getInitialDefaultKhatma()
                }
            }
            val newList = transform(currentList)
            prefs[KEY_PERSONAL_KHATMAS] = serializeKhatmas(newList)
        }
    }

    private fun serializeKhatmas(khatmas: List<PersonalKhatma>): String {
        val root = JSONArray()
        for (k in khatmas) {
            val obj = JSONObject()
            obj.put("id", k.id)
            obj.put("title", k.title)
            obj.put("sourceName", k.sourceName)
            obj.put("sourceType", k.sourceType.name)
            if (k.sourceId != null) obj.put("sourceId", k.sourceId)
            if (k.sourceLogoUrl != null) obj.put("sourceLogoUrl", k.sourceLogoUrl)
            obj.put("createdAtMillis", k.createdAtMillis)
            obj.put("lastUpdatedAtMillis", k.lastUpdatedAtMillis)
            obj.put("isArchived", k.isArchived)

            val partsArr = JSONArray()
            k.completedParts.forEach { partsArr.put(it) }
            obj.put("completedParts", partsArr)

            root.put(obj)
        }
        return root.toString()
    }

    private fun deserializeKhatmas(json: String): List<PersonalKhatma> {
        val list = mutableListOf<PersonalKhatma>()
        val root = JSONArray(json)
        for (i in 0 until root.length()) {
            val obj = root.getJSONObject(i)
            val id = obj.getString("id")
            val title = obj.getString("title")
            val sourceName = obj.optString("sourceName", "إذاعة القرآن الكريم")
            val sourceType = try {
                PersonalKhatmaSourceType.valueOf(obj.optString("sourceType", "STATION"))
            } catch (e: Exception) {
                PersonalKhatmaSourceType.STATION
            }
            val sourceId = if (obj.has("sourceId") && !obj.isNull("sourceId")) obj.getString("sourceId") else null
            val sourceLogoUrl = if (obj.has("sourceLogoUrl") && !obj.isNull("sourceLogoUrl")) obj.getString("sourceLogoUrl") else null
            val createdAtMillis = obj.optLong("createdAtMillis", System.currentTimeMillis())
            val lastUpdatedAtMillis = obj.optLong("lastUpdatedAtMillis", System.currentTimeMillis())
            val isArchived = obj.optBoolean("isArchived", false)

            val partsList = mutableListOf<Boolean>()
            if (obj.has("completedParts")) {
                val partsArr = obj.getJSONArray("completedParts")
                for (j in 0 until partsArr.length()) {
                    partsList.add(partsArr.getBoolean(j))
                }
            }
            while (partsList.size < 30) {
                partsList.add(false)
            }

            list.add(
                PersonalKhatma(
                    id = id,
                    title = title,
                    sourceName = sourceName,
                    sourceType = sourceType,
                    sourceId = sourceId,
                    sourceLogoUrl = sourceLogoUrl,
                    createdAtMillis = createdAtMillis,
                    lastUpdatedAtMillis = lastUpdatedAtMillis,
                    completedParts = partsList.take(30),
                    isArchived = isArchived
                )
            )
        }
        return list
    }

    private fun getInitialDefaultKhatma(): List<PersonalKhatma> {
        // Initial sample personal khatma (Juz 1, 2 completed)
        val parts = MutableList(30) { index -> index < 2 }
        val sample = PersonalKhatma(
            id = "default_sample_khatma",
            title = "ختمتي الشخصية",
            sourceName = "إذاعة القرآن الكريم - القاهرة",
            sourceType = PersonalKhatmaSourceType.STATION,
            sourceId = "cairo_quran",
            createdAtMillis = System.currentTimeMillis() - 86400000L * 3,
            lastUpdatedAtMillis = System.currentTimeMillis() - 86400000L * 1,
            completedParts = parts,
            isArchived = false
        )
        return listOf(sample)
    }
}
