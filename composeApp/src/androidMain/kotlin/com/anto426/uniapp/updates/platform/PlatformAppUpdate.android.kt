package com.anto426.uniapp.updates.platform

import android.app.PendingIntent
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
    val context = LocalContext.current.applicationContext
    return remember(context) {
        val packageInfo = context.packageManager.installedPackageInfo(context.packageName)
        PlatformAppUpdateEnvironment(
            installedBuild =
                InstalledAppBuild(
                    versionName = packageInfo.versionName.orEmpty().ifBlank { "unknown" },
                    versionCode = packageInfo.compatVersionCode(),
                    isDebuggable =
                        context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0,
                ),
            launcher = AndroidDirectUpdateLauncher(context),
        )
    }
}

private class AndroidDirectUpdateLauncher(
    private val context: Context,
) : PlatformUpdateLauncher {
    override suspend fun start(
        downloadUrl: String,
        expectedVersionCode: Int?,
    ): PlatformUpdateLaunchResult =
        withContext(Dispatchers.IO) {
            runCatching {
                val apkFile = downloadApk(downloadUrl)
                try {
                    validateApk(apkFile, expectedVersionCode)
                    enqueueInstall(apkFile, downloadUrl)
                    PlatformUpdateLaunchResult.Started
                } finally {
                    apkFile.delete()
                }
            }.getOrElse { error ->
                PlatformUpdateLaunchResult.Failed(
                    error.message?.takeIf(String::isNotBlank)
                        ?: "Impossibile scaricare o installare l’aggiornamento.",
                )
            }
        }

    private fun downloadApk(downloadUrl: String): File {
        val sourceUrl = URL(downloadUrl)
        require(sourceUrl.protocol.equals("https", ignoreCase = true)) {
            "Il download dell’aggiornamento deve usare HTTPS."
        }

        val updateDirectory = File(context.cacheDir, "app-updates").apply { mkdirs() }
        val destination = File(updateDirectory, "pending-update.apk")
        destination.delete()

        val connection = sourceUrl.openConnection() as HttpURLConnection
        connection.instanceFollowRedirects = true
        connection.connectTimeout = CONNECT_TIMEOUT_MILLIS
        connection.readTimeout = READ_TIMEOUT_MILLIS
        connection.setRequestProperty("Accept", "application/vnd.android.package-archive")
        connection.setRequestProperty("User-Agent", "UniApp-Updater")

        try {
            val status = connection.responseCode
            require(status in 200..299) { "Download aggiornamento non riuscito (HTTP $status)." }
            require(connection.url.protocol.equals("https", ignoreCase = true)) {
                "Il server ha reindirizzato il download verso una connessione non sicura."
            }
            val declaredLength = connection.contentLengthLong
            require(declaredLength <= MAX_APK_BYTES || declaredLength < 0L) {
                "Il pacchetto di aggiornamento supera il limite consentito."
            }

            connection.inputStream.use { input ->
                destination.outputStream().buffered().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var total = 0L
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        total += count
                        require(total <= MAX_APK_BYTES) {
                            "Il pacchetto di aggiornamento supera il limite consentito."
                        }
                        output.write(buffer, 0, count)
                    }
                    require(total > 0L) { "Il pacchetto di aggiornamento è vuoto." }
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

    private fun validateApk(
        apkFile: File,
        expectedVersionCode: Int?,
    ) {
        val packageManager = context.packageManager
        val candidate = packageManager.archivePackageInfo(apkFile)
            ?: error("Il file scaricato non è un APK Android valido.")
        require(candidate.packageName == context.packageName) {
            "Il pacchetto scaricato non appartiene a UniApp."
        }

        val installed = packageManager.installedPackageInfo(context.packageName, includeSigningInfo = true)
        val candidateCode = candidate.compatVersionCode()
            ?: error("L’APK non dichiara un versionCode valido.")
        val installedCode = installed.compatVersionCode() ?: 0
        require(candidateCode > installedCode) {
            "L’APK scaricato non è più recente della versione installata."
        }
        require(expectedVersionCode == null || candidateCode == expectedVersionCode) {
            "La versione dell’APK non corrisponde al manifest di aggiornamento."
        }
        require(installed.hasSignerInCommonWith(candidate)) {
            "La firma dell’APK non corrisponde alla firma di UniApp."
        }
    }

    private fun enqueueInstall(
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
    val installedSigners = signingInfo?.signingCertificateHistory.orEmpty()
    val candidateSigners = other.signingInfo?.signingCertificateHistory.orEmpty()
    return installedSigners.any { installed ->
        candidateSigners.any { candidate -> installed.toByteArray().contentEquals(candidate.toByteArray()) }
    }
}

@Suppress("DEPRECATION")
private fun PackageInfo.compatVersionCode(): Int? {
    val raw = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) longVersionCode else versionCode.toLong()
    return raw.takeIf { it in 1..Int.MAX_VALUE }?.toInt()
}
