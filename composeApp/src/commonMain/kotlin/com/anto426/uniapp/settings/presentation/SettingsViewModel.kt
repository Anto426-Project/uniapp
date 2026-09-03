package com.anto426.uniapp.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.UniAppDataSource
import com.anto426.uniapp.feedback.runtime.AppToastSink
import com.anto426.uniapp.feedback.runtime.error
import com.anto426.uniapp.feedback.runtime.success
import com.anto426.uniapp.security.biometric.BiometricAuthenticationResult
import com.anto426.uniapp.security.biometric.BiometricAuthenticator
import com.anto426.uniapp.security.biometric.BiometricAvailability
import com.anto426.uniapp.security.biometric.UnavailableBiometricAuthenticator
import com.anto426.uniapp.security.account.AccountSecurityPreferences
import com.anto426.uniapp.notifications.model.NotificationAuthorizationStatus
import com.anto426.uniapp.notifications.runtime.AppNotificationController
import com.anto426.uniapp.notifications.runtime.UnavailableAppNotificationController
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val notificationsEnabled: Boolean = false,
    val notificationAuthorization: NotificationAuthorizationStatus = NotificationAuthorizationStatus.NotDetermined,
    val biometricEnabled: Boolean = false,
    val biometricAvailability: BiometricAvailability = BiometricAvailability.Unavailable,
    val isBiometricAuthenticating: Boolean = false,
    val isSignOutConfirmationVisible: Boolean = false,
)

class SettingsViewModel(
    private val dataSource: UniAppDataSource,
    private val toastSink: AppToastSink = AppToastSink.None,
    private val biometricAuthenticator: BiometricAuthenticator = UnavailableBiometricAuthenticator,
    private val notificationController: AppNotificationController = UnavailableAppNotificationController,
) : ViewModel() {
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
                    dataSource.readPreference(NOTIFICATIONS_KEY)?.toBooleanStrictOrNull() ?: false
                mutableUiState.value =
                    mutableUiState.value.copy(
                        notificationsEnabled = notificationsEnabled,
                        biometricEnabled =
                            dataSource.readPreference(AccountSecurityPreferences.BIOMETRIC_UNLOCK)
                                ?.toBooleanStrictOrNull() ?: false,
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
            key = NOTIFICATIONS_KEY,
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

        viewModelScope.launch {
            update { copy(isBiometricAuthenticating = true) }
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
                        dataSource.writePreference(AccountSecurityPreferences.BIOMETRIC_UNLOCK, enabled.toString())
                        update { copy(biometricEnabled = enabled) }
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
        key: String,
        enabled: Boolean,
        successMessage: String,
        rollback: () -> Unit,
    ) {
        viewModelScope.launch {
            try {
                dataSource.writePreference(key, enabled.toString())
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
        const val NOTIFICATIONS_KEY = "settings.notifications"
    }
}
