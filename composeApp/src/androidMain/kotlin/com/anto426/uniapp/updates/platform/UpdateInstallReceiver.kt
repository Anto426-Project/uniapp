package com.anto426.uniapp.updates.platform


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build

class UpdateInstallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_INSTALL_STATUS) return
        val sessionId = intent.getIntExtra(PackageInstaller.EXTRA_SESSION_ID, -1)
        if (!UpdateInstallStatus.ownsSession(context, sessionId)) return
        when (intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                val confirmation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
                }
                if (confirmation == null) UpdateInstallStatus.failed(context, context.getString(com.anto426.uniapp.compose.R.string.android_conferma_di_installazione_non_disponibile))
                else UpdateInstallStatus.pending(context, confirmation)
            }
            PackageInstaller.STATUS_SUCCESS -> UpdateInstallStatus.succeeded(context)
            PackageInstaller.STATUS_FAILURE_ABORTED -> UpdateInstallStatus.failed(context, context.getString(com.anto426.uniapp.compose.R.string.android_installation_cancelled))
            else -> UpdateInstallStatus.failed(context,
                intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)?.takeIf { it.isNotBlank() }
                    ?: context.getString(com.anto426.uniapp.compose.R.string.android_android_non_ha_potuto_installare_laggiornamento))
        }
    }

    internal companion object {
        const val ACTION_INSTALL_STATUS = "com.anto426.uniapp.action.UPDATE_INSTALL_STATUS"
    }
}
