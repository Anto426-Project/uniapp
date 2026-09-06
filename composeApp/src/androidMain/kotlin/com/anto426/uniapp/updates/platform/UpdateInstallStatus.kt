package com.anto426.uniapp.updates.platform

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import com.anto426.uniapp.updates.model.AppUpdatePhase
import kotlinx.coroutines.flow.MutableStateFlow

/** The receiver persists the session outcome; foreground composition opens system confirmations. */
internal object UpdateInstallStatus {
    val progress = MutableStateFlow<PlatformUpdateProgress?>(null)
    val confirmation = MutableStateFlow<Intent?>(null)
    private fun preferences(context: Context) = context.getSharedPreferences("app-update-install", Context.MODE_PRIVATE)

    fun downloaded(bytes: Long, total: Long?) {
        progress.value = PlatformUpdateProgress(AppUpdatePhase.Downloading, bytes, total)
    }

    fun report(phase: AppUpdatePhase, message: String? = null) {
        progress.value = PlatformUpdateProgress(phase, message = message)
    }

    fun beginSession(context: Context, sessionId: Int) {
        preferences(context).edit().clear().putInt("session", sessionId).commit()
        report(AppUpdatePhase.Installing)
    }

    fun ownsSession(context: Context, sessionId: Int): Boolean =
        sessionId >= 0 && preferences(context).getInt("session", -1) == sessionId

    fun pending(context: Context, intent: Intent) {
        preferences(context).edit().putString("confirmation", intent.toUri(Intent.URI_INTENT_SCHEME)).commit()
        report(AppUpdatePhase.Installing, "Conferma l’aggiornamento nella schermata di Android.")
        confirmation.value = intent
    }

    fun confirmationLaunched() { confirmation.value = null }

    fun failed(context: Context, message: String) {
        confirmation.value = null
        preferences(context).edit().clear().putString("error", message).commit()
        report(AppUpdatePhase.Available, message)
    }

    fun succeeded(context: Context) {
        confirmation.value = null
        preferences(context).edit().clear().commit()
        @Suppress("DEPRECATION")
        val installed = context.packageManager.getPackageInfo(context.packageName, 0)
        val info = com.anto426.unisdk.platform.AppInfoProvider.current.copy(
            versionName = installed.versionName.orEmpty(),
            versionCode = installed.longVersionCode.takeIf { it in 1..Int.MAX_VALUE }?.toInt(),
        )
        com.anto426.unisdk.platform.AppInfoProvider.initialize(info)
        progress.value = PlatformUpdateProgress(AppUpdatePhase.UpToDate, installedBuild = info)
    }

    fun restore(context: Context) {
        val prefs = preferences(context)
        val id = prefs.getInt("session", -1)
        if (id >= 0) {
            val session = context.packageManager.packageInstaller.getSessionInfo(id)
            if (session == null) {
                failed(context, "La precedente installazione è terminata. Controlla nuovamente gli aggiornamenti.")
            } else {
                report(AppUpdatePhase.Installing, "Installazione in attesa di conferma.")
                prefs.getString("confirmation", null)?.let { encoded ->
                    runCatching { Intent.parseUri(encoded, Intent.URI_INTENT_SCHEME) }
                        .onSuccess { confirmation.value = it }
                        .onFailure { failed(context, "Impossibile riprendere l’installazione. Riprova.") }
                }
            }
        } else {
            prefs.getString("error", null)?.let { report(AppUpdatePhase.Available, it) }
        }
    }
}
