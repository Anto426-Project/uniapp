package com.anto426.uniapp.updates.platform

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.anto426.uniapp.updates.presentation.AppUpdateUiState
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
internal actual fun NotifyAvailableAppUpdate(state: AppUpdateUiState, enabled: Boolean) {
    val context = LocalContext.current.applicationContext
    val title = stringResource(if (state.isMandatory) Res.string.ui_update_required_title else Res.string.ui_update_new_available)
    val body = stringResource(Res.string.ui_update_notification_body, state.displayedVersion)
    val channelName = stringResource(Res.string.ui_update_notification_channel)
    LaunchedEffect(state.availableUpdateKey, enabled) {
        val key = state.availableUpdateKey ?: return@LaunchedEffect
        if (!enabled || !NotificationManagerCompat.from(context).areNotificationsEnabled()) return@LaunchedEffect
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context,
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return@LaunchedEffect
        val preferences = context.getSharedPreferences("app-update-notifications", Context.MODE_PRIVATE)
        if (preferences.getString("last-release", null) == key) return@LaunchedEffect
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel("app-updates", channelName, NotificationManager.IMPORTANCE_DEFAULT))
        val launch = context.packageManager.getLaunchIntentForPackage(context.packageName) ?: return@LaunchedEffect
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pending = PendingIntent.getActivity(context, 4206, launch, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(context, "app-updates")
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle(title).setContentText(body)
            .setContentIntent(pending).setAutoCancel(true).build()
        try {
            manager.notify(4206, notification)
            preferences.edit().putString("last-release", key).apply()
        } catch (_: SecurityException) {
            // Permission can be revoked between the check and notify; allow a later retry.
        }
    }
}
