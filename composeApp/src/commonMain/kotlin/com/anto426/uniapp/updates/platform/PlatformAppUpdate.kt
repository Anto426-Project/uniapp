package com.anto426.uniapp.updates.platform

import androidx.compose.runtime.Composable
import com.anto426.uniapp.updates.model.InstalledAppBuild

internal fun interface PlatformUpdateLauncher {
    fun open(downloadUrl: String): Boolean
}

internal data class PlatformAppUpdateEnvironment(
    val installedBuild: InstalledAppBuild,
    val launcher: PlatformUpdateLauncher,
)

@Composable
internal expect fun rememberPlatformAppUpdateEnvironment(): PlatformAppUpdateEnvironment
