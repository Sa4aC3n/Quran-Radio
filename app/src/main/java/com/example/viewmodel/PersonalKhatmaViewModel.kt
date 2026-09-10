package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.PersonalKhatmaRepository
import com.example.model.PersonalKhatma
import com.example.model.PersonalKhatmaSourceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PersonalKhatmaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PersonalKhatmaRepository(application)

    val allKhatmas: StateFlow<List<PersonalKhatma>> = repository.khatmasFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeKhatmaId: StateFlow<String?> = repository.activeKhatmaId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeKhatma: StateFlow<PersonalKhatma?> = combine(
        repository.khatmasFlow,
        repository.activeKhatmaId
    ) { list, activeId ->
        if (list.isEmpty()) null
        else if (activeId != null) {
            list.firstOrNull { it.id == activeId } ?: list.firstOrNull { !it.isArchived } ?: list.firstOrNull()
        } else {
            list.firstOrNull { !it.isArchived } ?: list.firstOrNull()
        }
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _userFeedbackMessage = MutableStateFlow<String?>(null)
    val userFeedbackMessage: StateFlow<String?> = _userFeedbackMessage.asStateFlow()

    fun clearFeedback() {
        _userFeedbackMessage.value = null
    }

    fun selectKhatma(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.setActiveKhatmaId(id)
        }
    }

    fun createKhatma(
        title: String,
        sourceName: String,
        sourceType: PersonalKhatmaSourceType,
        sourceId: String? = null,
        sourceLogoUrl: String? = null,
        startJuzNumber: Int = 1,
        onSuccess: (PersonalKhatma) -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val created = repository.createKhatma(
                title = title,
                sourceName = sourceName,
                sourceType = sourceType,
                sourceId = sourceId,
                sourceLogoUrl = sourceLogoUrl,
                startJuzNumber = startJuzNumber
            )
            _userFeedbackMessage.value = "تم بدء ختمة جديدة بنجاح 🌿"
            onSuccess(created)
        }
    }

    fun togglePart(khatmaId: String, juzNumber: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.togglePart(khatmaId, juzNumber)
        }
    }

    fun setPart(khatmaId: String, juzNumber: Int, completed: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.setPart(khatmaId, juzNumber, completed)
        }
    }

    fun markAllParts(khatmaId: String, completed: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.markAllParts(khatmaId, completed)
        }
    }

    fun deleteKhatma(khatmaId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteKhatma(khatmaId)
            _userFeedbackMessage.value = "تم حذف الختمة"
        }
    }

    fun toggleArchiveKhatma(khatmaId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleArchiveKhatma(khatmaId)
        }
    }
}
