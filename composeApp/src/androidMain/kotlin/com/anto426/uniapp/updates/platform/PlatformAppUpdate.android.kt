package com.anto426.uniapp.updates.platform

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.net.Uri
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.anto426.uniapp.updates.model.InstalledAppBuild

@Composable
internal actual fun rememberPlatformAppUpdateEnvironment(): PlatformAppUpdateEnvironment {
    val context = LocalContext.current.applicationContext
    return remember(context) {
        val packageInfo = context.packageManager.getPackageInfoCompat(context.packageName)
        PlatformAppUpdateEnvironment(
            installedBuild =
                InstalledAppBuild(
                    versionName = packageInfo.versionName.orEmpty().ifBlank { "unknown" },
                    versionCode = packageInfo.compatVersionCode(),
                    isDebuggable =
                        context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0,
                ),
            launcher =
                PlatformUpdateLauncher { downloadUrl ->
                    runCatching {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl)).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            },
                        )
                    }.isSuccess
                },
        )
    }
}

@Suppress("DEPRECATION")
private fun android.content.pm.PackageManager.getPackageInfoCompat(packageName: String): PackageInfo =
    getPackageInfo(packageName, 0)

@Suppress("DEPRECATION")
private fun PackageInfo.compatVersionCode(): Int? {
    val raw = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) longVersionCode else versionCode.toLong()
    return raw.takeIf { it in 1..Int.MAX_VALUE }?.toInt()
}
