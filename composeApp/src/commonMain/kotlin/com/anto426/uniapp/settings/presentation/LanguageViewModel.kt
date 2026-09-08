package com.anto426.uniapp.settings.presentation

import org.jetbrains.compose.resources.getString
import uniapp.composeapp.generated.resources.*

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.local.LocalDataScope
import com.anto426.uniapp.data.local.UniAppDataKeys
import com.anto426.uniapp.data.local.UniLocalDataStore
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
    val isLoaded: Boolean = false,
)

class LanguageViewModel(
    languages: List<LanguageInfo>,
    private val localDataStore: UniLocalDataStore,
    private val toastSink: AppToastSink = AppToastSink.None,
) : ViewModel() {
    private val mutableUiState =
        MutableStateFlow(
            LanguageUiState(
                languages = languages,
            ),
        )
    val uiState: StateFlow<LanguageUiState> = mutableUiState.asStateFlow()

    init {
        viewModelScope.launch {
            val selectedCode = try {
                localDataStore.read(LocalDataScope.Application, UniAppDataKeys.LanguageCode)
                    .takeIf { code -> mutableUiState.value.languages.any { it.code == code } }
                    ?: mutableUiState.value.languages.firstOrNull()?.code.orEmpty()
            } catch (error: CancellationException) {
                throw error
            } catch (_: Throwable) {
                // Keep the bundled language if encrypted local storage is temporarily unavailable.
                mutableUiState.value.languages.firstOrNull()?.code.orEmpty()
            }
            mutableUiState.value = mutableUiState.value.copy(
                selectedLanguageCode = selectedCode,
                isLoaded = true,
            )
        }
    }

    fun selectLanguage(code: String) {
        if (mutableUiState.value.languages.none { it.code == code }) return
        val previous = mutableUiState.value.selectedLanguageCode
        selectLanguageLocally(code)
        viewModelScope.launch {
            try {
                localDataStore.write(LocalDataScope.Application, UniAppDataKeys.LanguageCode, code)
                toastSink.success("Lingua aggiornata.")
            } catch (error: CancellationException) {
                throw error
            } catch (_: Throwable) {
                selectLanguageLocally(previous)
                toastSink.error(getString(Res.string.msg_impossibile_salvare_la_lingua))
            }
        }
    }

    private fun selectLanguageLocally(code: String) {
        if (mutableUiState.value.languages.none { it.code == code }) return
        mutableUiState.value = mutableUiState.value.copy(selectedLanguageCode = code)
    }
}
