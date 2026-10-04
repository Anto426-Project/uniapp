package com.anto426.uniapp.notifications.platform
import androidx.compose.runtime.Composable
import com.anto426.uniapp.notifications.model.NotificationAuthorizationStatus
import kotlinx.coroutines.flow.MutableStateFlow
private object DesktopNotificationPermissions : NotificationPermissionController {
    override val authorizationStatus = MutableStateFlow(NotificationAuthorizationStatus.Unsupported)
    override fun refresh() = Unit
    override fun requestAuthorization() = Unit
    override fun setRegistrationEnabled(enabled: Boolean) = Unit
}
@Composable
internal actual fun rememberPlatformNotificationPermissionController(): NotificationPermissionController = DesktopNotificationPermissions
