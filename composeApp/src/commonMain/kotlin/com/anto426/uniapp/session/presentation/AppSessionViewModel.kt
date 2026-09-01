package com.anto426.uniapp.session.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.session.AppSessionController
import com.anto426.uniapp.session.model.AppSessionState
import com.anto426.uniapp.security.biometric.BiometricAuthenticationResult
import com.anto426.uniapp.security.biometric.BiometricAuthenticator
import com.anto426.uniapp.security.biometric.BiometricAvailability
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppUnlockUiState(
    val isAuthenticating: Boolean = false,
    val errorMessage: String? = null,
)

class AppSessionViewModel(
    private val sessionController: AppSessionController,
) : ViewModel() {
    val state: StateFlow<AppSessionState> = sessionController.state
    private val mutableUnlockUiState = MutableStateFlow(AppUnlockUiState())
    val unlockUiState: StateFlow<AppUnlockUiState> = mutableUnlockUiState.asStateFlow()
    private var unlockJob: Job? = null

    init {
        viewModelScope.launch { sessionController.initialize() }
    }

    fun signOut() {
        viewModelScope.launch { sessionController.signOut() }
    }

    fun requestUnlock(authenticator: BiometricAuthenticator) {
        if (state.value !is AppSessionState.UnlockRequired || unlockJob?.isActive == true) return
        unlockJob =
            viewModelScope.launch {
                mutableUnlockUiState.value = AppUnlockUiState(isAuthenticating = true)
                try {
                    if (authenticator.availability() != BiometricAvailability.Available) {
                        mutableUnlockUiState.value =
                            AppUnlockUiState(errorMessage = "Autenticazione del dispositivo non disponibile.")
                        return@launch
                    }
                    when (
                        val result = authenticator.authenticate(
                            "Conferma la tua identità per aprire l’account UniApp.",
                        )
                    ) {
                        BiometricAuthenticationResult.Authenticated -> {
                            sessionController.unlockRequiredAccount()
                            mutableUnlockUiState.value = AppUnlockUiState()
                        }

                        BiometricAuthenticationResult.Cancelled ->
                            mutableUnlockUiState.value = AppUnlockUiState(errorMessage = "Accesso annullato.")

                        is BiometricAuthenticationResult.Failed ->
                            mutableUnlockUiState.value =
                                AppUnlockUiState(
                                    errorMessage = result.message.ifBlank { "Autenticazione non riuscita." },
                                )
                    }
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Throwable) {
                    mutableUnlockUiState.value =
                        AppUnlockUiState(errorMessage = error.message ?: "Impossibile aprire l’account.")
                } finally {
                    mutableUnlockUiState.update { it.copy(isAuthenticating = false) }
                }
            }
    }

    fun cancelUnlock() {
        unlockJob?.cancel()
        viewModelScope.launch {
            sessionController.cancelUnlock()
            mutableUnlockUiState.value = AppUnlockUiState()
        }
    }
}
