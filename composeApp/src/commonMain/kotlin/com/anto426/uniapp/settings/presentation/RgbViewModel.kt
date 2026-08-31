package com.anto426.uniapp.settings.presentation

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class RgbUiState(
    val red: Float = 0.16f,
    val green: Float = 0.47f,
    val blue: Float = 1f,
) {
    val color: Color get() = Color(red, green, blue)
    val hex: String
        get() = "#${red.toHexByte()}${green.toHexByte()}${blue.toHexByte()}"

    private fun Float.toHexByte(): String =
        (coerceIn(0f, 1f) * 255).toInt().toString(16).uppercase().padStart(2, '0')
}

class RgbViewModel : ViewModel() {
    private val mutableUiState = MutableStateFlow(RgbUiState())
    val uiState: StateFlow<RgbUiState> = mutableUiState.asStateFlow()

    fun setRed(value: Float) = update { copy(red = value.coerceIn(0f, 1f)) }
    fun setGreen(value: Float) = update { copy(green = value.coerceIn(0f, 1f)) }
    fun setBlue(value: Float) = update { copy(blue = value.coerceIn(0f, 1f)) }

    private fun update(transform: RgbUiState.() -> RgbUiState) {
        mutableUiState.value = mutableUiState.value.transform()
    }
}
