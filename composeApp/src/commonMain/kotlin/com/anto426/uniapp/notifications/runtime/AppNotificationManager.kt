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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface AppNotificationController {
    val state: StateFlow<AppNotificationState>
    val messages: Flow<RemotePushMessage>

    fun restoreEnabled(accountId: String, enabled: Boolean)
    fun setEnabled(accountId: String, enabled: Boolean)
    fun refresh()
}

internal class AppNotificationManager(
    private val connector: PushNotificationConnector,
    private val permissions: NotificationPermissionController,
) : AppNotificationController {
    private data class Binding(val accountId: String?, val enabled: Boolean)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val tokenMutation = Mutex()
    private val binding = MutableStateFlow(Binding(null, false))
    private val lastError = MutableStateFlow<String?>(null)
    private val mutableState = MutableStateFlow(AppNotificationState())

    override val state: StateFlow<AppNotificationState> = mutableState.asStateFlow()
    override val messages: Flow<RemotePushMessage> = connector.messageFlow

    init {
        scope.launch {
            combine(
                binding,
                permissions.authorizationStatus,
                connector.tokenFlow,
                lastError,
            ) { current, authorization, token, error ->
                AppNotificationState(
                    enabled = current.enabled,
                    authorizationStatus = authorization,
                    hasRegistrationToken = !token.isNullOrBlank(),
                    errorMessage = error,
                )
            }.collect(mutableState::emit)
        }
        permissions.refresh()
    }

    internal val boundOwnerId: String? get() = binding.value.accountId

    /** The SDK may read this during login; a token is visible only for the consenting owner. */
    internal fun backendToken(): String? {
        val current = binding.value
        if (current.accountId == null || !current.enabled) return null
        return when (permissions.authorizationStatus.value) {
            NotificationAuthorizationStatus.Authorized,
            NotificationAuthorizationStatus.Provisional -> connector.tokenFlow.value
            else -> null
        }
    }

    internal fun bindOwner(accountId: String?, consent: Boolean) {
        binding.value = Binding(accountId, accountId != null && consent)
        permissions.refresh()
        permissions.setRegistrationEnabled(binding.value.enabled)
        if (binding.value.enabled) refreshToken() else deleteToken()
    }

    override fun restoreEnabled(accountId: String, enabled: Boolean) {
        if (binding.value.accountId == accountId) bindOwner(accountId, enabled)
    }

    override fun setEnabled(accountId: String, enabled: Boolean) {
        if (binding.value.accountId != accountId) return
        binding.value = Binding(accountId, enabled)
        lastError.value = null
        if (enabled) {
            permissions.requestAuthorization()
            refreshToken()
        } else {
            permissions.setRegistrationEnabled(false)
            deleteToken()
        }
    }

    override fun refresh() {
        permissions.refresh()
        if (binding.value.enabled) refreshToken()
    }

    internal fun close() {
        scope.cancel()
    }

    private fun refreshToken() {
        scope.launch {
            try {
                tokenMutation.withLock {
                    if (binding.value.enabled) connector.getDeviceToken()
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                recordError(error)
            }
        }
    }

    private fun deleteToken() {
        scope.launch {
            tokenMutation.withLock {
                if (!binding.value.enabled) connector.deleteToken().onFailure(::recordError)
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

    override fun restoreEnabled(accountId: String, enabled: Boolean) = Unit
    override fun setEnabled(accountId: String, enabled: Boolean) = Unit
    override fun refresh() = Unit
}
