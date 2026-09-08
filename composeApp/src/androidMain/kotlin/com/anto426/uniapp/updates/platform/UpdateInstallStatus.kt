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
        report(AppUpdatePhase.Installing, context.getString(com.anto426.uniapp.compose.R.string.android_conferma_laggiornamento_nella_schermata_di_android))
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
                failed(context, context.getString(com.anto426.uniapp.compose.R.string.android_la_precedente_installazione_e_terminata_controlla_nuovamente_gli))
            } else {
                report(AppUpdatePhase.Installing, context.getString(com.anto426.uniapp.compose.R.string.android_installazione_in_attesa_di_conferma))
                prefs.getString("confirmation", null)?.let { encoded ->
                    runCatching { Intent.parseUri(encoded, Intent.URI_INTENT_SCHEME) }
                        .onSuccess { confirmation.value = it }
                        .onFailure { failed(context, context.getString(com.anto426.uniapp.compose.R.string.android_impossibile_riprendere_linstallazione_riprova)) }
                }
            }
        } else {
            prefs.getString("error", null)?.let { report(AppUpdatePhase.Available, it) }
        }
    }
}
