package com.anto426.uniapp.updates.platform
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.anto426.uniapp.app.info.AppBuildMetadata
import com.anto426.uniapp.app.info.DesktopBuildMetadata
import com.anto426.unisdk.platform.AppInfo
import com.anto426.unisdk.platform.AppInfoProvider
import java.awt.Desktop
import java.net.URI

@Composable
internal actual fun rememberPlatformAppUpdateEnvironment(): PlatformAppUpdateEnvironment = remember {
    val info = AppInfo(
        versionName = System.getProperty("uniapp.version", "2.0.15-desktop.1"),
        isDebuggable = true, applicationId = "com.anto426.uniapp.desktop", platform = "desktop",
        osVersion = "${System.getProperty("os.name")} ${System.getProperty("os.version")}",
        deviceModel = System.getProperty("os.arch"), sourceRevision = AppBuildMetadata.sourceRevision,
        modules = DesktopBuildMetadata.modules,
    )
    AppInfoProvider.initialize(info)
    PlatformAppUpdateEnvironment(info, PlatformUpdateLauncher { _, _ ->
        try {
            Desktop.getDesktop().browse(URI("https://github.com/Anto426-Project/uniapp/releases"))
            PlatformUpdateLaunchResult.OpenedExternalStore
        } catch (error: Exception) { PlatformUpdateLaunchResult.Failed(error.message ?: "Unable to open releases") }
    })
}
