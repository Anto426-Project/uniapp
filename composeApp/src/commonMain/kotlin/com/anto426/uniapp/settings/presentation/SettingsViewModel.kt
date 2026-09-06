package com.anto426.uniapp.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.local.LocalDataKey
import com.anto426.uniapp.data.local.LocalDataScope
import com.anto426.uniapp.data.local.UniAppDataKeys
import com.anto426.uniapp.data.local.UniLocalDataStore
import com.anto426.uniapp.feedback.runtime.AppToastSink
import com.anto426.uniapp.feedback.runtime.error
import com.anto426.uniapp.feedback.runtime.success
import com.anto426.uniapp.security.biometric.BiometricAuthenticationResult
import com.anto426.uniapp.security.biometric.BiometricAuthenticator
import com.anto426.uniapp.security.biometric.BiometricAvailability
import com.anto426.uniapp.security.biometric.UnavailableBiometricAuthenticator
import com.anto426.uniapp.notifications.model.NotificationAuthorizationStatus
import com.anto426.uniapp.notifications.runtime.AppNotificationController
import com.anto426.uniapp.notifications.runtime.UnavailableAppNotificationController
import com.anto426.uniapp.security.password.createAppPasswordVerifier
import com.anto426.uniapp.security.password.AppPasswordVerifier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class AppPasswordSetupError {
    TooShort,
    TooLong,
    Mismatch,
}

data class SettingsUiState(
    val notificationsEnabled: Boolean = false,
    val notificationAuthorization: NotificationAuthorizationStatus = NotificationAuthorizationStatus.NotDetermined,
    val biometricEnabled: Boolean = false,
    val biometricAvailability: BiometricAvailability = BiometricAvailability.Unavailable,
    val isBiometricAuthenticating: Boolean = false,
    val isPasswordSetupVisible: Boolean = false,
    val passwordSetupError: AppPasswordSetupError? = null,
    val isSignOutConfirmationVisible: Boolean = false,
)

class SettingsViewModel(
    private val localDataStore: UniLocalDataStore,
    accountId: String,
    private val toastSink: AppToastSink = AppToastSink.None,
    private val biometricAuthenticator: BiometricAuthenticator = UnavailableBiometricAuthenticator,
    private val notificationController: AppNotificationController = UnavailableAppNotificationController,
    private val passwordVerifierFactory: suspend (String) -> AppPasswordVerifier = { password ->
        withContext(Dispatchers.Default) { createAppPasswordVerifier(password) }
    },
) : ViewModel() {
    private val dataScope = LocalDataScope.Account(accountId)
    private val mutableUiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = mutableUiState.asStateFlow()

    init {
        mutableUiState.value =
            mutableUiState.value.copy(
                biometricAvailability = biometricAuthenticator.availability(),
            )
        viewModelScope.launch {
            notificationController.state.collect { notificationState ->
                update {
                    copy(notificationAuthorization = notificationState.authorizationStatus)
                }
            }
        }
        viewModelScope.launch {
            try {
                val notificationsEnabled =
                    localDataStore.read(dataScope, UniAppDataKeys.NotificationsEnabled)
                mutableUiState.value =
                    mutableUiState.value.copy(
                        notificationsEnabled = notificationsEnabled,
                        biometricEnabled = localDataStore.read(dataScope, UniAppDataKeys.BiometricUnlock),
                    )
                notificationController.restoreEnabled(notificationsEnabled)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Throwable) {
                toastSink.error("Impossibile leggere le preferenze protette.")
            }
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        update { copy(notificationsEnabled = enabled) }
        notificationController.setEnabled(enabled)
        persistToggle(
            key = UniAppDataKeys.NotificationsEnabled,
            enabled = enabled,
            successMessage = if (enabled) "Notifiche attivate." else "Notifiche disattivate.",
            rollback = {
                update { copy(notificationsEnabled = !enabled) }
                notificationController.setEnabled(!enabled)
            },
        )
    }

    fun setBiometricEnabled(enabled: Boolean) {
        if (enabled == mutableUiState.value.biometricEnabled || mutableUiState.value.isBiometricAuthenticating) return

        if (enabled) {
            update {
                copy(
                    isPasswordSetupVisible = true,
                    passwordSetupError = null,
                )
            }
            return
        }

        authenticateAndSetBiometric(enabled = false)
    }

    fun submitBiometricPassword(password: String, confirmation: String) {
        if (!mutableUiState.value.isPasswordSetupVisible || mutableUiState.value.isBiometricAuthenticating) return
        val validationError =
            when {
                password.isBlank() || password.length < MIN_APP_PASSWORD_LENGTH -> AppPasswordSetupError.TooShort
                password.length > 128 -> AppPasswordSetupError.TooLong
                password != confirmation -> AppPasswordSetupError.Mismatch
                else -> null
            }
        if (validationError != null) {
            update { copy(passwordSetupError = validationError) }
            return
        }
        authenticateAndSetBiometric(enabled = true, password = password)
    }

    fun dismissBiometricPasswordSetup() {
        if (mutableUiState.value.isBiometricAuthenticating) return
        update {
            copy(
                isPasswordSetupVisible = false,
                passwordSetupError = null,
            )
        }
    }

    private fun authenticateAndSetBiometric(enabled: Boolean, password: String? = null) {
        update { copy(isBiometricAuthenticating = true, passwordSetupError = null) }
        viewModelScope.launch {
            try {
                when (
                    val result = biometricAuthenticator.authenticate(
                        if (enabled) {
                            "Conferma la tua identità per attivare la protezione di UniApp."
                        } else {
                            "Conferma la tua identità per disattivare la protezione di UniApp."
                        },
                    )
                ) {
                    BiometricAuthenticationResult.Authenticated -> {
                        if (enabled) {
                            requireNotNull(password)
                            val verifier = passwordVerifierFactory(password)
                            // Commit the verifier first: a failed enable must never leave an
                            // enabled biometric gate without its fallback credential.
                            localDataStore.write(dataScope, UniAppDataKeys.PasswordVerifier, verifier)
                            localDataStore.write(dataScope, UniAppDataKeys.BiometricUnlock, true)
                        } else {
                            localDataStore.write(dataScope, UniAppDataKeys.BiometricUnlock, false)
                        }
                        update {
                            copy(
                                biometricEnabled = enabled,
                                isPasswordSetupVisible = false,
                                passwordSetupError = null,
                            )
                        }
                        if (!enabled) localDataStore.remove(dataScope, UniAppDataKeys.PasswordVerifier)
                        toastSink.success(
                            if (enabled) "Accesso biometrico attivato." else "Accesso biometrico disattivato.",
                        )
                    }

                    BiometricAuthenticationResult.Cancelled -> Unit
                    is BiometricAuthenticationResult.Failed ->
                        toastSink.error(result.message.ifBlank { "Autenticazione non riuscita." })
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                toastSink.error(error.message ?: "Impossibile aggiornare la protezione biometrica.")
            } finally {
                update {
                    copy(
                        isBiometricAuthenticating = false,
                        biometricAvailability = biometricAuthenticator.availability(),
                    )
                }
            }
        }
    }
    fun requestSignOut() = update { copy(isSignOutConfirmationVisible = true) }
    fun dismissSignOut() = update { copy(isSignOutConfirmationVisible = false) }

    private fun update(transform: SettingsUiState.() -> SettingsUiState) {
        mutableUiState.value = mutableUiState.value.transform()
    }

    private fun persistToggle(
        key: LocalDataKey<Boolean>,
        enabled: Boolean,
        successMessage: String,
        rollback: () -> Unit,
    ) {
        viewModelScope.launch {
            try {
                localDataStore.write(dataScope, key, enabled)
                toastSink.success(successMessage)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Throwable) {
                rollback()
                toastSink.error("Impossibile salvare la preferenza.")
            }
        }
    }

    private companion object {
        const val MIN_APP_PASSWORD_LENGTH = 8
    }
}
