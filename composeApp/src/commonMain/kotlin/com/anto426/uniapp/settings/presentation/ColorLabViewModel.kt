package com.anto426.uniapp.settings.presentation

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.local.LocalDataScope
import com.anto426.uniapp.data.local.UniAppDataKeys
import com.anto426.uniapp.data.local.UniLocalDataStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ColorLabUiState(val selectedColor: Color = Color(0xFF2979FF))

class ColorLabViewModel(private val localDataStore: UniLocalDataStore) : ViewModel() {
    private val mutableUiState = MutableStateFlow(ColorLabUiState())
    val uiState: StateFlow<ColorLabUiState> = mutableUiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                localDataStore
                    .read(LocalDataScope.Application, UniAppDataKeys.ThemeCustomColor)
                    ?.toULongOrNull()
                    ?.let { value -> mutableUiState.value = ColorLabUiState(Color(value)) }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Throwable) {
                // The default color remains usable if local storage cannot be read.
            }
        }
    }

    fun selectColor(color: Color) {
        mutableUiState.value = ColorLabUiState(color)
        viewModelScope.launch {
            localDataStore.write(
                LocalDataScope.Application,
                UniAppDataKeys.ThemeCustomColor,
                color.value.toString(),
            )
        }
    }
}
