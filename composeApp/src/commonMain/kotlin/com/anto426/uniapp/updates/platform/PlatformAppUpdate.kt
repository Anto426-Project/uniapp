package com.anto426.uniapp.updates.platform

import androidx.compose.runtime.Composable
import com.anto426.uniapp.updates.model.InstalledAppBuild
import com.anto426.uniapp.updates.model.AppUpdatePhase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

internal sealed interface PlatformUpdateLaunchResult {
    data object Started : PlatformUpdateLaunchResult
    data object OpenedExternalStore : PlatformUpdateLaunchResult
    data class Failed(val message: String) : PlatformUpdateLaunchResult
}

internal fun interface PlatformUpdateLauncher {
    val updates: Flow<PlatformUpdateProgress> get() = emptyFlow()

    suspend fun start(
        downloadUrl: String,
        expectedVersionCode: Int?,
    ): PlatformUpdateLaunchResult
}

internal data class PlatformUpdateProgress(
    val phase: AppUpdatePhase,
    val downloadedBytes: Long = 0,
    val totalBytes: Long? = null,
    val installedBuild: InstalledAppBuild? = null,
    val message: String? = null,
)

internal data class PlatformAppUpdateEnvironment(
    val installedBuild: InstalledAppBuild,
    val launcher: PlatformUpdateLauncher,
)

@Composable
internal expect fun rememberPlatformAppUpdateEnvironment(): PlatformAppUpdateEnvironment
