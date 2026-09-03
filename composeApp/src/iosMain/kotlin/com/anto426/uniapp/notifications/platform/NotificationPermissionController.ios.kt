@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.anto426.uniapp.notifications.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.anto426.uniapp.notifications.model.NotificationAuthorizationStatus
import kotlinx.cinterop.BetaInteropApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.UIKit.*
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusDenied
import platform.UserNotifications.UNAuthorizationStatusEphemeral
import platform.UserNotifications.UNAuthorizationStatusNotDetermined
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNUserNotificationCenter
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@Composable
internal actual fun rememberPlatformNotificationPermissionController(): NotificationPermissionController =
    remember { IosNotificationPermissionController() }

private class IosNotificationPermissionController : NotificationPermissionController {
    private val mutableAuthorizationStatus =
        MutableStateFlow(NotificationAuthorizationStatus.NotDetermined)

    override val authorizationStatus: StateFlow<NotificationAuthorizationStatus> =
        mutableAuthorizationStatus.asStateFlow()

    init {
        refresh()
    }

    override fun refresh() {
        UNUserNotificationCenter.currentNotificationCenter()
            .getNotificationSettingsWithCompletionHandler { settings ->
                mutableAuthorizationStatus.value =
                    when (settings?.authorizationStatus) {
                        UNAuthorizationStatusAuthorized,
                        UNAuthorizationStatusEphemeral,
                        -> NotificationAuthorizationStatus.Authorized

                        UNAuthorizationStatusProvisional -> NotificationAuthorizationStatus.Provisional
                        UNAuthorizationStatusDenied -> NotificationAuthorizationStatus.Denied
                        UNAuthorizationStatusNotDetermined -> NotificationAuthorizationStatus.NotDetermined
                        else -> NotificationAuthorizationStatus.Unsupported
                    }
            }
    }

    override fun requestAuthorization() {
        val options =
            UNAuthorizationOptionAlert or
                UNAuthorizationOptionBadge or
                UNAuthorizationOptionSound
        UNUserNotificationCenter.currentNotificationCenter()
            .requestAuthorizationWithOptions(options) { granted, _ ->
                refresh()
                if (granted) registerForRemoteNotifications()
            }
    }

    override fun setRegistrationEnabled(enabled: Boolean) {
        if (enabled) {
            UNUserNotificationCenter.currentNotificationCenter()
                .getNotificationSettingsWithCompletionHandler { settings ->
                    when (settings?.authorizationStatus) {
                        UNAuthorizationStatusAuthorized,
                        UNAuthorizationStatusProvisional,
                        UNAuthorizationStatusEphemeral,
                        -> registerForRemoteNotifications()
                    }
                    refresh()
                }
        } else {
            dispatch_async(dispatch_get_main_queue()) {
                UIApplication.sharedApplication.unregisterForRemoteNotifications()
            }
        }
    }

    private fun registerForRemoteNotifications() {
        dispatch_async(dispatch_get_main_queue()) {
            UIApplication.sharedApplication.registerForRemoteNotifications()
        }
    }
}
