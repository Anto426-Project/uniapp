package com.anto426.uniapp.notifications.platform

import androidx.compose.runtime.Composable
import com.anto426.uniapp.notifications.model.NotificationAuthorizationStatus
import kotlinx.coroutines.flow.StateFlow

internal interface NotificationPermissionController {
    val authorizationStatus: StateFlow<NotificationAuthorizationStatus>

    fun refresh()

    /** Requests user-facing authorization and registers with the platform push service. */
    fun requestAuthorization()

    /** Enables or disables remote registration without displaying a permission dialog. */
    fun setRegistrationEnabled(enabled: Boolean)
}

@Composable
internal expect fun rememberPlatformNotificationPermissionController(): NotificationPermissionController
