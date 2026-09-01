package com.anto426.uniapp.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.UniAppDataSource
import com.anto426.uniapp.feedback.runtime.AppToastSink
import com.anto426.uniapp.feedback.runtime.error
import com.anto426.uniapp.feedback.runtime.success
import com.anto426.uniapp.model.settings.LanguageInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException

data class LanguageUiState(
    val languages: List<LanguageInfo> = emptyList(),
    val selectedLanguageCode: String = "",
)

class LanguageViewModel(
    languages: List<LanguageInfo>,
    private val dataSource: UniAppDataSource,
    private val toastSink: AppToastSink = AppToastSink.None,
) : ViewModel() {
    private val mutableUiState =
        MutableStateFlow(
            LanguageUiState(
                languages = languages,
                selectedLanguageCode = languages.firstOrNull()?.code.orEmpty(),
            ),
        )
    val uiState: StateFlow<LanguageUiState> = mutableUiState.asStateFlow()

    init {
        viewModelScope.launch {
            dataSource.readPreference(LANGUAGE_KEY)?.let(::selectLanguageLocally)
        }
    }

    fun selectLanguage(code: String) {
        if (mutableUiState.value.languages.none { it.code == code }) return
        val previous = mutableUiState.value.selectedLanguageCode
        selectLanguageLocally(code)
        viewModelScope.launch {
            try {
                dataSource.writePreference(LANGUAGE_KEY, code)
                toastSink.success("Lingua aggiornata.")
            } catch (error: CancellationException) {
                throw error
            } catch (_: Throwable) {
                selectLanguageLocally(previous)
                toastSink.error("Impossibile salvare la lingua.")
            }
        }
    }

    private fun selectLanguageLocally(code: String) {
        if (mutableUiState.value.languages.none { it.code == code }) return
        mutableUiState.value = mutableUiState.value.copy(selectedLanguageCode = code)
    }

    private companion object { const val LANGUAGE_KEY = "settings.language" }
}
