package com.anto426.uniapp.notifications.runtime

import com.anto426.firebase.PushNotificationConnector
import com.anto426.firebase.RemotePushMessage
import com.anto426.uniapp.notifications.model.AppNotificationState
import com.anto426.uniapp.notifications.model.NotificationAuthorizationStatus
import com.anto426.uniapp.notifications.platform.NotificationPermissionController
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

interface AppNotificationController {
    val state: StateFlow<AppNotificationState>
    val messages: Flow<RemotePushMessage>

    fun restoreEnabled(enabled: Boolean)
    fun setEnabled(enabled: Boolean)
    fun refresh()
}

internal class AppNotificationManager(
    private val connector: PushNotificationConnector,
    private val permissions: NotificationPermissionController,
) : AppNotificationController {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val enabled = MutableStateFlow(false)
    private val lastError = MutableStateFlow<String?>(null)
    private val mutableState = MutableStateFlow(AppNotificationState())

    override val state: StateFlow<AppNotificationState> = mutableState.asStateFlow()
    override val messages: Flow<RemotePushMessage> = connector.messageFlow

    init {
        scope.launch {
            combine(
                enabled,
                permissions.authorizationStatus,
                connector.tokenFlow,
                lastError,
            ) { isEnabled, authorization, token, error ->
                AppNotificationState(
                    enabled = isEnabled,
                    authorizationStatus = authorization,
                    hasRegistrationToken = !token.isNullOrBlank(),
                    errorMessage = error,
                )
            }.collect(mutableState::emit)
        }
        permissions.refresh()
    }

    override fun restoreEnabled(enabled: Boolean) {
        this.enabled.value = enabled
        permissions.refresh()
        permissions.setRegistrationEnabled(enabled)
        if (enabled) refreshToken()
    }

    override fun setEnabled(enabled: Boolean) {
        this.enabled.value = enabled
        lastError.value = null
        if (enabled) {
            permissions.requestAuthorization()
            refreshToken()
        } else {
            permissions.setRegistrationEnabled(false)
            scope.launch {
                connector.deleteToken().onFailure(::recordError)
            }
        }
    }

    override fun refresh() {
        permissions.refresh()
        if (enabled.value) refreshToken()
    }

    internal fun close() {
        scope.cancel()
    }

    private fun refreshToken() {
        scope.launch {
            try {
                connector.getDeviceToken()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                recordError(error)
            }
        }
    }

    private fun recordError(error: Throwable) {
        lastError.value =
            error.message?.takeIf(String::isNotBlank)
                ?: "Impossibile registrare il dispositivo per le notifiche."
    }
}

object UnavailableAppNotificationController : AppNotificationController {
    private val unavailableState =
        MutableStateFlow(
            AppNotificationState(
                authorizationStatus = NotificationAuthorizationStatus.Unsupported,
            ),
        )

    override val state: StateFlow<AppNotificationState> = unavailableState.asStateFlow()
    override val messages: Flow<RemotePushMessage> = kotlinx.coroutines.flow.emptyFlow()

    override fun restoreEnabled(enabled: Boolean) = Unit
    override fun setEnabled(enabled: Boolean) = Unit
    override fun refresh() = Unit
}
