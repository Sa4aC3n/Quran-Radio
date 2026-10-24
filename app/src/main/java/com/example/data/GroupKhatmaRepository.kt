package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.model.GroupKhatma
import com.example.model.KhatmaCompletionType
import com.example.model.KhatmaPart
import com.example.model.MyKhatmaStats
import com.example.model.UserKhatmaProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import kotlin.random.Random

class GroupKhatmaRepository(context: Context) {

    private val appContext = context.applicationContext
    private val KEY_KHATMAS_JSON = stringPreferencesKey("group_khatmas_list_json")
    private val KEY_USER_NAME = stringPreferencesKey("khatma_user_name")
    private val KEY_LAST_OPENED_ID = stringPreferencesKey("khatma_last_opened_id")

    val userProfile: Flow<UserKhatmaProfile> = appContext.dataStore.data.map { preferences ->
        UserKhatmaProfile(
            userName = preferences[KEY_USER_NAME] ?: "",
            lastOpenedKhatmaId = preferences[KEY_LAST_OPENED_ID]
        )
    }

    val khatmasFlow: Flow<List<GroupKhatma>> = appContext.dataStore.data.map { preferences ->
        val jsonString = preferences[KEY_KHATMAS_JSON]
        if (jsonString.isNullOrBlank()) {
            emptyList()
        } else {
            try {
                deserializeKhatmas(jsonString)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    fun getKhatmaById(khatmaId: String): Flow<GroupKhatma?> {
        return khatmasFlow.map { list -> list.firstOrNull { it.id == khatmaId } }
    }

    fun getMyKhatmaStats(userName: String): Flow<MyKhatmaStats> {
        return khatmasFlow.map { list ->
            if (userName.isBlank()) {
                MyKhatmaStats(
                    totalKhatmasParticipated = list.size,
                    totalPartsAssigned = list.sumOf { k -> k.parts.count { !it.isAvailable } },
                    totalPartsCompleted = list.sumOf { k -> k.parts.count { it.isCompleted } },
                    activeKhatmasCount = list.count { !it.isFullyCompleted },
                    completedKhatmasCount = list.count { it.isFullyCompleted }
                )
            } else {
                val myKhatmas = list.filter { k ->
                    k.creatorName.equals(userName, ignoreCase = true) ||
                            k.parts.any { it.assignedMemberName.equals(userName, ignoreCase = true) }
                }
                val partsAssigned = list.sumOf { k ->
                    k.parts.count { it.assignedMemberName.equals(userName, ignoreCase = true) }
                }
                val partsCompleted = list.sumOf { k ->
                    k.parts.count { it.assignedMemberName.equals(userName, ignoreCase = true) && it.isCompleted }
                }

                MyKhatmaStats(
                    totalKhatmasParticipated = myKhatmas.size,
                    totalPartsAssigned = partsAssigned,
                    totalPartsCompleted = partsCompleted,
                    activeKhatmasCount = myKhatmas.count { !it.isFullyCompleted },
                    completedKhatmasCount = myKhatmas.count { it.isFullyCompleted }
                )
            }
        }
    }

    suspend fun saveUserName(name: String) {
        appContext.dataStore.edit { prefs ->
            prefs[KEY_USER_NAME] = name.trim()
        }
    }

    suspend fun setLastOpenedKhatmaId(khatmaId: String) {
        appContext.dataStore.edit { prefs ->
            prefs[KEY_LAST_OPENED_ID] = khatmaId
        }
    }

    suspend fun createKhatma(
        title: String,
        creatorName: String,
        targetDateMillis: Long?,
        description: String? = null
    ): GroupKhatma {
        val trimmedTitle = title.trim().ifBlank { "ختمة مباركة" }
        val trimmedCreator = creatorName.trim().ifBlank { "فاعل خير" }
        val generatedCode = generateInviteCode()

        val newKhatma = GroupKhatma(
            id = UUID.randomUUID().toString(),
            title = trimmedTitle,
            inviteCode = generatedCode,
            creatorName = trimmedCreator,
            createdAtMillis = System.currentTimeMillis(),
            targetDateMillis = targetDateMillis,
            description = description,
            parts = GroupKhatma.defaultKhatmaParts()
        )

        updateKhatmasList { currentList ->
            listOf(newKhatma) + currentList
        }

        saveUserName(trimmedCreator)
        setLastOpenedKhatmaId(newKhatma.id)
        return newKhatma
    }

    suspend fun joinKhatmaByCode(rawCode: String, userName: String): Result<GroupKhatma> {
        val trimmedInput = rawCode.trim()
        val trimmedUser = userName.trim()

        if (trimmedInput.isBlank()) {
            return Result.failure(IllegalArgumentException("يرجى إدخال كود الدعوة"))
        }

        // Extract invite code (e.g. KH-XXXXX) if present
        val codeRegex = Regex("""KH-[A-Z0-9]{4,10}""", RegexOption.IGNORE_CASE)
        val extractedCode = codeRegex.find(trimmedInput)?.value?.uppercase()
            ?: (if (!trimmedInput.contains("\n") && !trimmedInput.contains(" ") && trimmedInput.length <= 15) trimmedInput.uppercase() else "")

        // Parse optional offline sharing metadata (title, creator) from URL, deep link, or text
        var parsedTitle: String? = null
        var parsedCreator: String? = null

        if (trimmedInput.contains("quranradio://khatma") || trimmedInput.contains("code=")) {
            try {
                val queryStr = if (trimmedInput.contains("?")) trimmedInput.substringAfter("?") else trimmedInput
                val params = queryStr.split("&").associate {
                    val parts = it.split("=")
                    if (parts.size >= 2) parts[0].trim() to java.net.URLDecoder.decode(parts[1].trim(), "UTF-8") else parts[0].trim() to ""
                }
                params["title"]?.let { if (it.isNotBlank()) parsedTitle = it }
                params["creator"]?.let { if (it.isNotBlank()) parsedCreator = it }
            } catch (_: Exception) {}
        }

        if (parsedTitle == null) {
            val titleRegex = Regex("""(?:اسم الختمة|الختمة)\s*:\s*([^\n\r]+)""")
            titleRegex.find(trimmedInput)?.groupValues?.getOrNull(1)?.trim()?.let {
                if (it.isNotBlank()) parsedTitle = it
            }
        }

        if (parsedCreator == null) {
            val creatorRegex = Regex("""(?:منشئ الختمة|أنشأها|المنشئ|بواسطة)\s*:\s*([^\n\r]+)""")
            creatorRegex.find(trimmedInput)?.groupValues?.getOrNull(1)?.trim()?.let {
                if (it.isNotBlank()) parsedCreator = it
            }
        }

        val targetCode = when {
            extractedCode.isNotBlank() -> extractedCode
            trimmedInput.length <= 15 && !trimmedInput.contains("\n") -> trimmedInput.uppercase()
            else -> "KH-${UUID.randomUUID().toString().take(5).uppercase()}"
        }

        var foundKhatma: GroupKhatma? = null

        updateKhatmasList { currentList ->
            val existing = currentList.firstOrNull { it.inviteCode.equals(targetCode, ignoreCase = true) }
            if (existing != null) {
                foundKhatma = existing
                currentList
            } else if (parsedTitle != null) {
                val importedKhatma = GroupKhatma(
                    id = UUID.randomUUID().toString(),
                    title = parsedTitle!!,
                    inviteCode = targetCode,
                    creatorName = parsedCreator ?: "فاعل خير",
                    createdAtMillis = System.currentTimeMillis(),
                    targetDateMillis = null,
                    description = null,
                    parts = GroupKhatma.defaultKhatmaParts()
                )
                foundKhatma = importedKhatma
                listOf(importedKhatma) + currentList
            } else {
                currentList
            }
        }

        if (trimmedUser.isNotBlank() && foundKhatma != null) {
            saveUserName(trimmedUser)
        }

        return if (foundKhatma != null) {
            setLastOpenedKhatmaId(foundKhatma!!.id)
            Result.success(foundKhatma!!)
        } else {
            Result.failure(IllegalArgumentException("لم يتم العثور على ختمة مسجلة بهذا الكود على هذا الجهاز. يمكنك نسخ رسالة الدعوة كاملة أو فتح الرابط للانضمام تلقائياً."))
        }
    }

    suspend fun reservePart(khatmaId: String, juzNumber: Int, memberName: String) {
        val trimmedName = memberName.trim().ifBlank { "مشارك" }
        updateKhatmasList { list ->
            list.map { khatma ->
                if (khatma.id == khatmaId) {
                    val updatedParts = khatma.parts.map { part ->
                        if (part.juzNumber == juzNumber) {
                            part.copy(
                                assignedMemberName = trimmedName,
                                reservedAtMillis = System.currentTimeMillis()
                            )
                        } else part
                    }
                    khatma.copy(parts = updatedParts)
                } else khatma
            }
        }
        saveUserName(trimmedName)
    }

    suspend fun unreservePart(khatmaId: String, juzNumber: Int) {
        updateKhatmasList { list ->
            list.map { khatma ->
                if (khatma.id == khatmaId) {
                    val updatedParts = khatma.parts.map { part ->
                        if (part.juzNumber == juzNumber) {
                            part.copy(
                                assignedMemberName = null,
                                isCompleted = false,
                                completionNote = null,
                                completedAtMillis = null,
                                reservedAtMillis = null
                            )
                        } else part
                    }
                    khatma.copy(parts = updatedParts)
                } else khatma
            }
        }
    }

    suspend fun completePart(
        khatmaId: String,
        juzNumber: Int,
        memberName: String,
        completionType: KhatmaCompletionType,
        note: String?
    ) {
        val trimmedName = memberName.trim().ifBlank { "مشارك" }
        updateKhatmasList { list ->
            list.map { khatma ->
                if (khatma.id == khatmaId) {
                    val updatedParts = khatma.parts.map { part ->
                        if (part.juzNumber == juzNumber) {
                            part.copy(
                                assignedMemberName = trimmedName,
                                isCompleted = true,
                                completionType = completionType,
                                completionNote = note?.trim()?.ifBlank { null },
                                completedAtMillis = System.currentTimeMillis()
                            )
                        } else part
                    }
                    khatma.copy(parts = updatedParts)
                } else khatma
            }
        }
        saveUserName(trimmedName)
    }

    suspend fun togglePartCompletion(khatmaId: String, juzNumber: Int, isCompleted: Boolean) {
        updateKhatmasList { list ->
            list.map { khatma ->
                if (khatma.id == khatmaId) {
                    val updatedParts = khatma.parts.map { part ->
                        if (part.juzNumber == juzNumber) {
                            part.copy(
                                isCompleted = isCompleted,
                                completedAtMillis = if (isCompleted) System.currentTimeMillis() else null
                            )
                        } else part
                    }
                    khatma.copy(parts = updatedParts)
                } else khatma
            }
        }
    }

    suspend fun autoDistributeRemaining(khatmaId: String, memberNames: List<String>) {
        val cleanMembers = memberNames.map { it.trim() }.filter { it.isNotBlank() }
        if (cleanMembers.isEmpty()) return

        updateKhatmasList { list ->
            list.map { khatma ->
                if (khatma.id == khatmaId) {
                    val unassignedParts = khatma.parts.filter { it.isAvailable }
                    if (unassignedParts.isEmpty()) return@map khatma

                    var memberIndex = 0
                    val updatedParts = khatma.parts.map { part ->
                        if (part.isAvailable) {
                            val assigned = cleanMembers[memberIndex % cleanMembers.size]
                            memberIndex++
                            part.copy(
                                assignedMemberName = assigned,
                                reservedAtMillis = System.currentTimeMillis()
                            )
                        } else part
                    }
                    khatma.copy(parts = updatedParts)
                } else khatma
            }
        }
    }

    suspend fun deleteKhatma(khatmaId: String) {
        updateKhatmasList { list ->
            list.filterNot { it.id == khatmaId }
        }
    }

    private suspend fun updateKhatmasList(transform: (List<GroupKhatma>) -> List<GroupKhatma>) {
        appContext.dataStore.edit { prefs ->
            val currentJson = prefs[KEY_KHATMAS_JSON]
            val currentList = if (currentJson.isNullOrBlank()) {
                emptyList()
            } else {
                try {
                    deserializeKhatmas(currentJson)
                } catch (e: Exception) {
                    emptyList()
                }
            }
            val newList = transform(currentList)
            prefs[KEY_KHATMAS_JSON] = serializeKhatmas(newList)
        }
    }

    private fun generateInviteCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val randomStr = (1..5).map { chars[Random.nextInt(chars.length)] }.joinToString("")
        return "KH-$randomStr"
    }

    private fun serializeKhatmas(khatmas: List<GroupKhatma>): String {
        val root = JSONArray()
        for (k in khatmas) {
            val obj = JSONObject()
            obj.put("id", k.id)
            obj.put("title", k.title)
            obj.put("inviteCode", k.inviteCode)
            obj.put("creatorName", k.creatorName)
            obj.put("createdAtMillis", k.createdAtMillis)
            if (k.targetDateMillis != null) obj.put("targetDateMillis", k.targetDateMillis)
            if (k.description != null) obj.put("description", k.description)

            val partsArr = JSONArray()
            for (p in k.parts) {
                val pObj = JSONObject()
                pObj.put("juz", p.juzNumber)
                pObj.put("title", p.titleAr)
                pObj.put("range", p.surahRange)
                if (p.assignedMemberName != null) pObj.put("member", p.assignedMemberName)
                pObj.put("completed", p.isCompleted)
                pObj.put("type", p.completionType.name)
                if (p.completionNote != null) pObj.put("note", p.completionNote)
                if (p.completedAtMillis != null) pObj.put("completedAt", p.completedAtMillis)
                if (p.reservedAtMillis != null) pObj.put("reservedAt", p.reservedAtMillis)
                partsArr.put(pObj)
            }
            obj.put("parts", partsArr)
            root.put(obj)
        }
        return root.toString()
    }

    private fun deserializeKhatmas(json: String): List<GroupKhatma> {
        val list = mutableListOf<GroupKhatma>()
        val root = JSONArray(json)
        val defaultParts = GroupKhatma.defaultKhatmaParts()

        for (i in 0 until root.length()) {
            val obj = root.getJSONObject(i)
            val id = obj.getString("id")
            val title = obj.getString("title")
            val inviteCode = obj.optString("inviteCode", "KH-7788")
            val creatorName = obj.optString("creatorName", "مشارك")
            val createdAtMillis = obj.optLong("createdAtMillis", System.currentTimeMillis())
            val targetDateMillis = if (obj.has("targetDateMillis")) obj.getLong("targetDateMillis") else null
            val description = if (obj.has("description")) obj.getString("description") else null

            val partsList = mutableListOf<KhatmaPart>()
            if (obj.has("parts")) {
                val partsArr = obj.getJSONArray("parts")
                for (j in 0 until partsArr.length()) {
                    val pObj = partsArr.getJSONObject(j)
                    val juzNum = pObj.getInt("juz")
                    val defaultMeta = defaultParts.getOrNull(juzNum - 1)
                    val titleAr = pObj.optString("title", defaultMeta?.titleAr ?: "الجزء $juzNum")
                    val range = pObj.optString("range", defaultMeta?.surahRange ?: "")
                    val member = if (pObj.has("member") && !pObj.isNull("member")) pObj.getString("member") else null
                    val completed = pObj.optBoolean("completed", false)
                    val type = KhatmaCompletionType.fromName(pObj.optString("type", "READING"))
                    val note = if (pObj.has("note") && !pObj.isNull("note")) pObj.getString("note") else null
                    val completedAt = if (pObj.has("completedAt")) pObj.getLong("completedAt") else null
                    val reservedAt = if (pObj.has("reservedAt")) pObj.getLong("reservedAt") else null

                    partsList.add(
                        KhatmaPart(
                            juzNumber = juzNum,
                            titleAr = titleAr,
                            surahRange = range,
                            assignedMemberName = member,
                            isCompleted = completed,
                            completionType = type,
                            completionNote = note,
                            completedAtMillis = completedAt,
                            reservedAtMillis = reservedAt
                        )
                    )
                }
            } else {
                partsList.addAll(defaultParts)
            }

            list.add(
                GroupKhatma(
                    id = id,
                    title = title,
                    inviteCode = inviteCode,
                    creatorName = creatorName,
                    createdAtMillis = createdAtMillis,
                    targetDateMillis = targetDateMillis,
                    description = description,
                    parts = if (partsList.size == 30) partsList else defaultParts
                )
            )
        }
        return list
    }
}
