package com.anto426.uniapp.settings.presentation

import androidx.lifecycle.ViewModel
import com.anto426.uniapp.model.settings.LanguageInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LanguageUiState(
    val languages: List<LanguageInfo> = emptyList(),
    val selectedLanguageCode: String = "",
)

class LanguageViewModel(languages: List<LanguageInfo>) : ViewModel() {
    private val mutableUiState =
        MutableStateFlow(
            LanguageUiState(
                languages = languages,
                selectedLanguageCode = languages.firstOrNull()?.code.orEmpty(),
            ),
        )
    val uiState: StateFlow<LanguageUiState> = mutableUiState.asStateFlow()

    fun selectLanguage(code: String) {
        if (mutableUiState.value.languages.none { it.code == code }) return
        mutableUiState.value = mutableUiState.value.copy(selectedLanguageCode = code)
    }
}
