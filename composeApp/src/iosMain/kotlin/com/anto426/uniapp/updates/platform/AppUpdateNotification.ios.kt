@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.anto426.uniapp.updates.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.anto426.uniapp.updates.presentation.AppUpdateUiState
import org.jetbrains.compose.resources.stringResource
import platform.Foundation.NSUserDefaults
import platform.UserNotifications.*
import uniapp.composeapp.generated.resources.*

@Composable
internal actual fun NotifyAvailableAppUpdate(state: AppUpdateUiState, enabled: Boolean) {
    val title = stringResource(if (state.isMandatory) Res.string.ui_update_required_title else Res.string.ui_update_new_available)
    val body = stringResource(Res.string.ui_update_notification_body, state.displayedVersion)
    LaunchedEffect(state.availableUpdateKey, enabled) {
        val key = state.availableUpdateKey ?: return@LaunchedEffect
        if (!enabled) return@LaunchedEffect
        val defaults = NSUserDefaults.standardUserDefaults
        if (defaults.stringForKey("app-update-last-notification") == key) return@LaunchedEffect
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.getNotificationSettingsWithCompletionHandler { settings ->
            if (settings?.authorizationStatus == UNAuthorizationStatusAuthorized ||
                settings?.authorizationStatus == UNAuthorizationStatusProvisional) {
                val content = UNMutableNotificationContent().apply {
                    setTitle(title)
                    setBody(body)
                    setSound(UNNotificationSound.defaultSound)
                }
                val request = UNNotificationRequest.requestWithIdentifier("uniapp-update", content, null)
                center.addNotificationRequest(request) { error ->
                    if (error == null) defaults.setObject(key, forKey = "app-update-last-notification")
                }
            }
        }
    }
}
