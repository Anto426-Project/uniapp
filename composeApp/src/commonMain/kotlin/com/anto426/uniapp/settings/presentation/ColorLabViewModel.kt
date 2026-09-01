package com.anto426.uniapp.settings.presentation

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.UniAppDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ColorLabUiState(val selectedColor: Color = Color(0xFF2979FF))

class ColorLabViewModel(private val dataSource: UniAppDataSource) : ViewModel() {
    private val mutableUiState = MutableStateFlow(ColorLabUiState())
    val uiState: StateFlow<ColorLabUiState> = mutableUiState.asStateFlow()

    init {
        viewModelScope.launch {
            dataSource.readPreference(COLOR_KEY)?.toULongOrNull()?.let { value ->
                mutableUiState.value = ColorLabUiState(Color(value))
            }
        }
    }

    fun selectColor(color: Color) {
        mutableUiState.value = ColorLabUiState(color)
        viewModelScope.launch { dataSource.writePreference(COLOR_KEY, color.value.toString()) }
    }

    private companion object { const val COLOR_KEY = "theme.custom-color" }
}
