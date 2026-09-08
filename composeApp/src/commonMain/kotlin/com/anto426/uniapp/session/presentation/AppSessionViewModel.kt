package com.anto426.uniapp.session.presentation

import org.jetbrains.compose.resources.getString
import uniapp.composeapp.generated.resources.*

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.session.AppSessionController
import com.anto426.uniapp.session.PasswordUnlockResult
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
    val passwordError: AppPasswordUnlockError? = null,
)

enum class AppPasswordUnlockError { Invalid, RetryLater, Failed }

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
                            AppUnlockUiState(errorMessage = getString(Res.string.msg_autenticazione_del_dispositivo_non_disponibile))
                        return@launch
                    }
                    when (
                        val result = authenticator.authenticate(
                            getString(Res.string.msg_conferma_la_tua_identita_per_aprire_laccount_uniapp),
                        )
                    ) {
                        BiometricAuthenticationResult.Authenticated -> {
                            sessionController.unlockRequiredAccount()
                            mutableUnlockUiState.value = AppUnlockUiState()
                        }

                        BiometricAuthenticationResult.Cancelled ->
                            mutableUnlockUiState.value = AppUnlockUiState(errorMessage = getString(Res.string.msg_accesso_annullato))

                        is BiometricAuthenticationResult.Failed ->
                            mutableUnlockUiState.value =
                                AppUnlockUiState(
                                    errorMessage = result.message.ifBlank { getString(Res.string.msg_autenticazione_non_riuscita) },
                                )
                    }
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Throwable) {
                    mutableUnlockUiState.value =
                        AppUnlockUiState(errorMessage = error.message ?: getString(Res.string.msg_impossibile_aprire_laccount))
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

    fun requestPasswordUnlock(password: String) {
        if (state.value !is AppSessionState.UnlockRequired || unlockJob?.isActive == true) return
        unlockJob = viewModelScope.launch {
            mutableUnlockUiState.value = AppUnlockUiState(isAuthenticating = true)
            try {
                val result = sessionController.unlockWithPassword(password)
                mutableUnlockUiState.value = AppUnlockUiState(
                    passwordError = when (result) {
                        PasswordUnlockResult.Unlocked -> null
                        PasswordUnlockResult.Invalid -> AppPasswordUnlockError.Invalid
                        PasswordUnlockResult.RetryLater -> AppPasswordUnlockError.RetryLater
                    },
                )
            } catch (error: CancellationException) {
                throw error
            } catch (_: Throwable) {
                mutableUnlockUiState.value = AppUnlockUiState(passwordError = AppPasswordUnlockError.Failed)
            } finally {
                mutableUnlockUiState.update { it.copy(isAuthenticating = false) }
            }
        }
    }
}
