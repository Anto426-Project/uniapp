package com.anto426.uniapp.notifications.platform

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.anto426.uniapp.notifications.model.NotificationAuthorizationStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Composable
internal actual fun rememberPlatformNotificationPermissionController(): NotificationPermissionController {
    val context = LocalContext.current
    val activity = context.findActivity()
    val controller = remember(context.applicationContext, activity) {
        AndroidNotificationPermissionController(context.applicationContext, activity)
    }
    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            controller.permissionRequestCompleted()
        }
    SideEffect {
        controller.permissionRequester = {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        controller.refresh()
    }
    return controller
}

private class AndroidNotificationPermissionController(
    private val context: Context,
    private val activity: Activity?,
) : NotificationPermissionController {
    private val preferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val mutableAuthorizationStatus =
        MutableStateFlow(resolveAuthorizationStatus())

    var permissionRequester: () -> Unit = {}

    override val authorizationStatus: StateFlow<NotificationAuthorizationStatus> =
        mutableAuthorizationStatus.asStateFlow()

    override fun refresh() {
        mutableAuthorizationStatus.value = resolveAuthorizationStatus()
    }

    override fun requestAuthorization() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            refresh()
            return
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            refresh()
            return
        }
        preferences.edit().putBoolean(KEY_PERMISSION_REQUESTED, true).apply()
        permissionRequester()
    }

    override fun setRegistrationEnabled(enabled: Boolean) {
        refresh()
    }

    fun permissionRequestCompleted() {
        refresh()
    }

    private fun resolveAuthorizationStatus(): NotificationAuthorizationStatus {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            return if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                !preferences.getBoolean(KEY_PERMISSION_REQUESTED, false)
            ) {
                NotificationAuthorizationStatus.NotDetermined
            } else {
                NotificationAuthorizationStatus.Denied
            }
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return NotificationAuthorizationStatus.Authorized
        }
        return if (
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            NotificationAuthorizationStatus.Authorized
        } else if (activity?.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) == true ||
            preferences.getBoolean(KEY_PERMISSION_REQUESTED, false)
        ) {
            NotificationAuthorizationStatus.Denied
        } else {
            NotificationAuthorizationStatus.NotDetermined
        }
    }

    private companion object {
        const val PREFERENCES_NAME = "notification-permission"
        const val KEY_PERMISSION_REQUESTED = "requested"
    }
}

private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
