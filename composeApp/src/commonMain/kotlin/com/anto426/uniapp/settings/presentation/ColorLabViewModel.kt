package com.anto426.uniapp.settings.presentation

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ColorLabUiState(val selectedColor: Color = Color(0xFF2979FF))

class ColorLabViewModel : ViewModel() {
    private val mutableUiState = MutableStateFlow(ColorLabUiState())
    val uiState: StateFlow<ColorLabUiState> = mutableUiState.asStateFlow()

    fun selectColor(color: Color) {
        mutableUiState.value = ColorLabUiState(color)
    }
}
