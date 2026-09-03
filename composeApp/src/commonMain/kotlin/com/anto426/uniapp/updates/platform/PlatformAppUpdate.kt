package com.anto426.uniapp.updates.platform

import androidx.compose.runtime.Composable
import com.anto426.uniapp.updates.model.InstalledAppBuild

internal sealed interface PlatformUpdateLaunchResult {
    data object Started : PlatformUpdateLaunchResult
    data object OpenedExternalStore : PlatformUpdateLaunchResult
    data class Failed(val message: String) : PlatformUpdateLaunchResult
}

internal fun interface PlatformUpdateLauncher {
    suspend fun start(
        downloadUrl: String,
        expectedVersionCode: Int?,
    ): PlatformUpdateLaunchResult
}

internal data class PlatformAppUpdateEnvironment(
    val installedBuild: InstalledAppBuild,
    val launcher: PlatformUpdateLauncher,
)

@Composable
internal expect fun rememberPlatformAppUpdateEnvironment(): PlatformAppUpdateEnvironment
