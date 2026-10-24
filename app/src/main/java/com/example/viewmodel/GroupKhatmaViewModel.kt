package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GroupKhatmaRepository
import com.example.model.GroupKhatma
import com.example.model.KhatmaCompletionType
import com.example.model.KhatmaPart
import com.example.model.MyKhatmaStats
import com.example.model.UserKhatmaProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GroupKhatmaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GroupKhatmaRepository(application)

    val allKhatmas: StateFlow<List<GroupKhatma>> = repository.khatmasFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserKhatmaProfile> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserKhatmaProfile())

    val myStats: StateFlow<MyKhatmaStats> = combine(
        repository.khatmasFlow,
        repository.userProfile
    ) { khatmas, profile ->
        val userName = profile.userName
        if (userName.isBlank()) {
            MyKhatmaStats(
                totalKhatmasParticipated = khatmas.size,
                totalPartsAssigned = khatmas.sumOf { k -> k.parts.count { !it.isAvailable } },
                totalPartsCompleted = khatmas.sumOf { k -> k.parts.count { it.isCompleted } },
                activeKhatmasCount = khatmas.count { !it.isFullyCompleted },
                completedKhatmasCount = khatmas.count { it.isFullyCompleted }
            )
        } else {
            val myKhatmas = khatmas.filter { k ->
                k.creatorName.equals(userName, ignoreCase = true) ||
                        k.parts.any { it.assignedMemberName.equals(userName, ignoreCase = true) }
            }
            val partsAssigned = khatmas.sumOf { k ->
                k.parts.count { it.assignedMemberName.equals(userName, ignoreCase = true) }
            }
            val partsCompleted = khatmas.sumOf { k ->
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
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MyKhatmaStats())

    private val _selectedKhatmaId = MutableStateFlow<String?>(null)
    val selectedKhatmaId: StateFlow<String?> = _selectedKhatmaId.asStateFlow()

    private val _userFeedbackMessage = MutableStateFlow<String?>(null)
    val userFeedbackMessage: StateFlow<String?> = _userFeedbackMessage.asStateFlow()

    fun selectKhatma(id: String) {
        _selectedKhatmaId.value = id
        viewModelScope.launch(Dispatchers.IO) {
            repository.setLastOpenedKhatmaId(id)
        }
    }

    fun clearFeedback() {
        _userFeedbackMessage.value = null
    }

    fun saveUserName(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveUserName(name)
        }
    }

    fun createKhatma(
        title: String,
        creatorName: String,
        targetDateMillis: Long?,
        description: String? = null,
        onSuccess: (GroupKhatma) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val created = repository.createKhatma(title, creatorName, targetDateMillis, description)
            _userFeedbackMessage.value = "تم إنشاء الختمة بنجاح! كود الدعوة: ${created.inviteCode}"
            onSuccess(created)
        }
    }

    fun joinKhatma(code: String, userName: String, onSuccess: (GroupKhatma) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.joinKhatmaByCode(code, userName)
            result.onSuccess {
                _userFeedbackMessage.value = "تم الانضمام إلى الختمة المباركة بنجاح"
                onSuccess(it)
            }.onFailure {
                onError(it.message ?: "فشل الانضمام بالكود")
            }
        }
    }

    fun reservePart(khatmaId: String, juzNumber: Int, memberName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.reservePart(khatmaId, juzNumber, memberName)
            _userFeedbackMessage.value = "تم حجز الجزء $juzNumber باسم $memberName"
        }
    }

    fun unreservePart(khatmaId: String, juzNumber: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.unreservePart(khatmaId, juzNumber)
            _userFeedbackMessage.value = "تم إلغاء حجز الجزء $juzNumber"
        }
    }

    fun completePart(
        khatmaId: String,
        juzNumber: Int,
        memberName: String,
        type: KhatmaCompletionType,
        note: String?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.completePart(khatmaId, juzNumber, memberName, type, note)
            _userFeedbackMessage.value = "تقبل الله منكم! تم تسجيل إنجاز الجزء $juzNumber"
        }
    }

    fun togglePartCompletion(khatmaId: String, juzNumber: Int, isCompleted: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.togglePartCompletion(khatmaId, juzNumber, isCompleted)
        }
    }

    fun autoDistributeRemaining(khatmaId: String, memberNames: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.autoDistributeRemaining(khatmaId, memberNames)
            _userFeedbackMessage.value = "تم توزيع الأجزاء المتبقية بالتساوي على الأعضاء"
        }
    }

    fun deleteKhatma(khatmaId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteKhatma(khatmaId)
            _userFeedbackMessage.value = "تم حذف الختمة"
        }
    }

    fun shareKhatmaInvite(context: Context, khatma: GroupKhatma) {
        val encodedTitle = try { java.net.URLEncoder.encode(khatma.title, "UTF-8") } catch (_: Exception) { khatma.title }
        val encodedCreator = try { java.net.URLEncoder.encode(khatma.creatorName, "UTF-8") } catch (_: Exception) { khatma.creatorName }
        val shareLink = "quranradio://khatma?code=${khatma.inviteCode}&title=$encodedTitle&creator=$encodedCreator"

        val shareText = """
            🕌 دعوة للمشاركة في الختمة القرآنية الجماعية
            
            📖 اسم الختمة: ${khatma.title}
            👤 منشئ الختمة: ${khatma.creatorName}
            🔑 كود الانضمام: ${khatma.inviteCode}
            
            🔗 رابط الانضمام المباشر:
            $shareLink
            
            تطبيق إذاعات القرآن الكريم:
            https://play.google.com/store/apps/details?id=com.aistudio.quranradioworld.live
            
            نسأل الله القبول للجميع 🤲
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "دعوة للختمة الجماعية: ${khatma.title}")
            putExtra(Intent.EXTRA_TEXT, shareText)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(Intent.createChooser(intent, "مشاركة دعوة الختمة"))
    }
}
