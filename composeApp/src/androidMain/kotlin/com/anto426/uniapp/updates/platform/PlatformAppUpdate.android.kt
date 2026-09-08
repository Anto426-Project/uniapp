package com.anto426.uniapp.updates.platform

import org.jetbrains.compose.resources.getString
import uniapp.composeapp.generated.resources.*

import android.app.PendingIntent
import android.provider.Settings
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.anto426.uniapp.app.info.AppBuildMetadata
import com.anto426.unisdk.platform.AppInfo
import com.anto426.unisdk.platform.AppInfoProvider
import com.anto426.uniapp.updates.model.AppUpdatePhase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.filterNotNull
import androidx.compose.ui.platform.LocalContext
import com.anto426.uniapp.updates.model.InstalledAppBuild
import java.io.File
import java.io.FileInputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal actual fun rememberPlatformAppUpdateEnvironment(): PlatformAppUpdateEnvironment {
    val activityContext = LocalContext.current
    val context = activityContext.applicationContext
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(context, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            UpdateInstallStatus.confirmation.filterNotNull().collect { confirmation ->
                try {
                    activityContext.startActivity(confirmation.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    UpdateInstallStatus.confirmationLaunched()
                } catch (error: Exception) {
                    UpdateInstallStatus.failed(context, "Impossibile aprire la conferma di installazione: ${error.message.orEmpty()}")
                }
            }
        }
    }
    return remember(context) {
        UpdateInstallStatus.restore(context)
        val packageInfo = context.packageManager.installedPackageInfo(context.packageName)
        val info = AppInfo(
                    versionName = packageInfo.versionName.orEmpty().ifBlank { "unknown" },
                    versionCode = packageInfo.compatVersionCode(),
                    isDebuggable =
                        context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0,
                    applicationId = context.packageName,
                    platform = "android",
                    osVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
                    deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}",
                    sourceRevision = AppBuildMetadata.sourceRevision,
                    modules = AppBuildMetadata.modules,
                )
        AppInfoProvider.initialize(info)
        PlatformAppUpdateEnvironment(
            installedBuild = info,
            launcher = AndroidDirectUpdateLauncher(context),
        )
    }
}

private class AndroidDirectUpdateLauncher(
    private val context: Context,
) : PlatformUpdateLauncher {
    override val updates = UpdateInstallStatus.progress.filterNotNull()

    override suspend fun start(
        downloadUrl: String,
        expectedVersionCode: Int?,
    ): PlatformUpdateLaunchResult =
        withContext(Dispatchers.IO) {
            try {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    withContext(Dispatchers.Main) {
                        context.startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                            Uri.parse("package:${context.packageName}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                    return@withContext PlatformUpdateLaunchResult.Failed(
                        getString(Res.string.msg_abilita_linstallazione_da_uniapp_torna_nellapp_e_premi))
                }
                val apkFile = downloadApk(downloadUrl)
                try {
                    currentCoroutineContext().ensureActive()
                    UpdateInstallStatus.report(AppUpdatePhase.Verifying)
                    validateApk(apkFile, expectedVersionCode)
                    currentCoroutineContext().ensureActive()
                    enqueueInstall(apkFile, downloadUrl)
                    PlatformUpdateLaunchResult.Started
                } finally {
                    apkFile.delete()
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                PlatformUpdateLaunchResult.Failed(
                    error.message?.takeIf(String::isNotBlank)
                        ?: getString(Res.string.msg_impossibile_scaricare_o_installare_laggiornamento),
                )
            }
        }

    private suspend fun downloadApk(downloadUrl: String): File {
        val sourceUrl = URL(downloadUrl)
        require(sourceUrl.protocol.equals("https", ignoreCase = true)) {
            getString(Res.string.msg_il_download_dellaggiornamento_deve_usare_https)
        }

        val updateDirectory = File(context.cacheDir, "app-updates").apply { mkdirs() }
        val connection = openDownloadConnection(sourceUrl)
        val destination = try {
            File.createTempFile("update-", ".apk", updateDirectory)
        } catch (error: Exception) {
            connection.disconnect()
            throw error
        }

        try {
            val status = connection.responseCode
            require(status in 200..299) { "Download aggiornamento non riuscito (HTTP $status)." }
            require(connection.url.protocol.equals("https", ignoreCase = true)) {
                getString(Res.string.msg_il_server_ha_reindirizzato_il_download_verso_una)
            }
            val declaredLength = connection.contentLengthLong
            require(declaredLength <= MAX_APK_BYTES || declaredLength < 0L) {
                getString(Res.string.msg_il_pacchetto_di_aggiornamento_supera_il_limite_consentito)
            }

            connection.inputStream.use { input ->
                destination.outputStream().buffered().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var total = 0L
                    var lastReported = 0L
                    while (true) {
                        currentCoroutineContext().ensureActive()
                        val count = input.read(buffer)
                        if (count < 0) break
                        total += count
                        require(total <= MAX_APK_BYTES) {
                            getString(Res.string.msg_il_pacchetto_di_aggiornamento_supera_il_limite_consentito)
                        }
                        output.write(buffer, 0, count)
                        if (total - lastReported >= 256 * 1024) {
                            UpdateInstallStatus.downloaded(total, declaredLength.takeIf { it > 0 })
                            lastReported = total
                        }
                    }
                    require(declaredLength < 0 || total == declaredLength) { "Download incompleto. Riprova." }
                    UpdateInstallStatus.downloaded(total, declaredLength.takeIf { it > 0 })
                    require(total > 0L) { getString(Res.string.msg_il_pacchetto_di_aggiornamento_e_vuoto) }
                }
            }
            return destination
        } catch (error: Throwable) {
            destination.delete()
            throw error
        } finally {
            connection.disconnect()
        }
    }

    private suspend fun openDownloadConnection(initial: URL): HttpURLConnection {
        var url = initial
        repeat(6) { attempt ->
            currentCoroutineContext().ensureActive()
            require(url.protocol.equals("https", true)) { getString(Res.string.msg_il_download_deve_usare_https_anche_dopo_i) }
            val connection = (url.openConnection() as HttpURLConnection).apply {
                instanceFollowRedirects = false
                connectTimeout = CONNECT_TIMEOUT_MILLIS
                readTimeout = READ_TIMEOUT_MILLIS
                setRequestProperty("Accept", "application/vnd.android.package-archive")
                setRequestProperty("User-Agent", "UniApp-Updater")
            }
            try {
                if (connection.responseCode !in listOf(301, 302, 303, 307, 308)) return connection
                require(attempt < 5) { getString(Res.string.msg_troppi_reindirizzamenti_durante_il_download) }
                val location = connection.getHeaderField("Location") ?: error("Reindirizzamento senza destinazione.")
                url = URL(url, location)
            } catch (error: Throwable) {
                connection.disconnect()
                throw error
            }
            connection.disconnect()
        }
        error(getString(Res.string.msg_download_non_disponibile))
    }

    private suspend fun validateApk(
        apkFile: File,
        expectedVersionCode: Int?,
    ) {
        val packageManager = context.packageManager
        val candidate = packageManager.archivePackageInfo(apkFile)
            ?: error(getString(Res.string.msg_il_file_scaricato_non_e_un_apk_android))
        require(candidate.packageName == context.packageName) {
            getString(Res.string.msg_il_pacchetto_scaricato_non_appartiene_a_uniapp)
        }

        val installed = packageManager.installedPackageInfo(context.packageName, includeSigningInfo = true)
        val candidateCode = candidate.compatVersionCode()
            ?: error(getString(Res.string.msg_lapk_non_dichiara_un_versioncode_valido))
        val installedCode = installed.compatVersionCode() ?: 0
        require(candidateCode > installedCode) {
            getString(Res.string.msg_lapk_scaricato_non_e_piu_recente_della_versione)
        }
        require(expectedVersionCode == null || candidateCode == expectedVersionCode) {
            getString(Res.string.msg_la_versione_dellapk_non_corrisponde_al_manifest_di)
        }
        require(installed.hasSignerInCommonWith(candidate)) {
            getString(Res.string.msg_la_firma_dellapk_non_corrisponde_alla_firma_di)
        }
    }

    private suspend fun enqueueInstall(
        apkFile: File,
        sourceUrl: String,
    ) {
        val installer = context.packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL).apply {
            setAppPackageName(context.packageName)
            setSize(apkFile.length())
            setOriginatingUri(Uri.parse(sourceUrl))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                setPackageSource(PackageInstaller.PACKAGE_SOURCE_DOWNLOADED_FILE)
            }
        }
        val sessionId = installer.createSession(params)
        UpdateInstallStatus.beginSession(context, sessionId)
        try {
            installer.openSession(sessionId).use { session ->
                FileInputStream(apkFile).use { input ->
                    session.openWrite("base.apk", 0L, apkFile.length()).use { output ->
                        input.copyTo(output)
                        session.fsync(output)
                    }
                }

                val callbackIntent =
                    Intent(context, UpdateInstallReceiver::class.java)
                        .setAction(UpdateInstallReceiver.ACTION_INSTALL_STATUS)
                        .putExtra(PackageInstaller.EXTRA_SESSION_ID, sessionId)
                val pendingIntent =
                    PendingIntent.getBroadcast(
                        context,
                        sessionId,
                        callbackIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                PendingIntent.FLAG_MUTABLE
                            } else {
                                0
                            },
                    )
                session.commit(pendingIntent.intentSender)
            }
        } catch (error: Throwable) {
            runCatching { installer.abandonSession(sessionId) }
            UpdateInstallStatus.failed(context, error.message ?: getString(Res.string.msg_installazione_non_riuscita))
            throw error
        }
    }

    private companion object {
        const val CONNECT_TIMEOUT_MILLIS = 20_000
        const val READ_TIMEOUT_MILLIS = 120_000
        const val MAX_APK_BYTES = 512L * 1024L * 1024L
    }
}

private fun PackageManager.installedPackageInfo(
    packageName: String,
    includeSigningInfo: Boolean = false,
): PackageInfo {
    val flags = if (includeSigningInfo) PackageManager.GET_SIGNING_CERTIFICATES else 0
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags.toLong()))
    } else {
        @Suppress("DEPRECATION")
        getPackageInfo(packageName, flags)
    }
}

private fun PackageManager.archivePackageInfo(apkFile: File): PackageInfo? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getPackageArchiveInfo(
            apkFile.absolutePath,
            PackageManager.PackageInfoFlags.of(PackageManager.GET_SIGNING_CERTIFICATES.toLong()),
        )
    } else {
        @Suppress("DEPRECATION")
        getPackageArchiveInfo(apkFile.absolutePath, PackageManager.GET_SIGNING_CERTIFICATES)
    }

private fun PackageInfo.hasSignerInCommonWith(other: PackageInfo): Boolean {
    val installed = signingInfo ?: return false
    val candidate = other.signingInfo ?: return false
    val current = installed.apkContentsSigners.orEmpty()
    if (current.isEmpty()) return false
    if (installed.hasMultipleSigners() || candidate.hasMultipleSigners()) {
        val target = candidate.apkContentsSigners.orEmpty()
        return current.size == target.size && current.all { it in target }
    }
    // Accept the installed signer or a forward signing-key rotation; PackageInstaller
    // remains responsible for verifying the cryptographic APK signing lineage.
    return current.all { it in candidate.signingCertificateHistory.orEmpty() }
}

@Suppress("DEPRECATION")
private fun PackageInfo.compatVersionCode(): Int? {
    val raw = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) longVersionCode else versionCode.toLong()
    return raw.takeIf { it in 1..Int.MAX_VALUE }?.toInt()
}
