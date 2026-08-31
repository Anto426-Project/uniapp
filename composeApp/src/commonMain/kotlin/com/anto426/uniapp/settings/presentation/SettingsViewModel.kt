package com.anto426.uniapp.settings.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SettingsUiState(
    val notificationsEnabled: Boolean = true,
    val biometricEnabled: Boolean = false,
    val isSignOutConfirmationVisible: Boolean = false,
)

class SettingsViewModel : ViewModel() {
    private val mutableUiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = mutableUiState.asStateFlow()

    fun setNotificationsEnabled(enabled: Boolean) = update { copy(notificationsEnabled = enabled) }
    fun setBiometricEnabled(enabled: Boolean) = update { copy(biometricEnabled = enabled) }
    fun requestSignOut() = update { copy(isSignOutConfirmationVisible = true) }
    fun dismissSignOut() = update { copy(isSignOutConfirmationVisible = false) }

    private fun update(transform: SettingsUiState.() -> SettingsUiState) {
        mutableUiState.value = mutableUiState.value.transform()
    }
}
