package com.anto426.uniapp.updates.model

import kotlinx.serialization.Serializable

@Serializable
data class UniAppModuleBuildInfo(
    val name: String,
    val version: String,
    val revision: String = "",
    val hasLocalChanges: Boolean = false,
)

@Serializable
data class InstalledAppBuild(
    val versionName: String = "unknown",
    val versionCode: Int? = null,
    val isDebuggable: Boolean = false,
    val applicationId: String = "",
    val name: String = "UniApp",
    val platform: String = "unknown",
    val osVersion: String = "",
    val deviceModel: String = "",
    val sourceRevision: String = "unknown",
    val appStoreUrl: String? = null,
    val modules: List<UniAppModuleBuildInfo> = emptyList(),
)

@Serializable
data class UniAppUpdateManifest(
    val latestVersion: String,
    val latestVersionCode: Int? = null,
    val isUpdateAvailable: Boolean = false,
    val isMandatory: Boolean = false,
    val downloadUrl: String? = null,
    val notes: String? = null,
    val publishedAt: String? = null,
    val channel: String? = null,
)

enum class AppUpdatePhase {
    Idle,
    Checking,
    UpToDate,
    Available,
    Downloading,
    Verifying,
    Installing,
    Failed,
}

data class AppUpdateState(
    val installedBuild: InstalledAppBuild,
    val phase: AppUpdatePhase = AppUpdatePhase.Idle,
    val updateInfo: UniAppUpdateManifest? = null,
    val message: String? = null,
    val downloadedBytes: Long = 0,
    val totalBytes: Long? = null,
) {
    val isBusy: Boolean
        get() = phase in setOf(
            AppUpdatePhase.Checking,
            AppUpdatePhase.Downloading,
            AppUpdatePhase.Verifying,
            AppUpdatePhase.Installing,
        )

    val canStartUpdate: Boolean
        get() = phase == AppUpdatePhase.Available && updateInfo?.isUpdateAvailable == true && downloadUrl != null

    val isMandatory: Boolean
        get() =
            !installedBuild.isDebuggable &&
                updateInfo?.isMandatory == true &&
                phase != AppUpdatePhase.UpToDate

    val downloadUrl: String?
        get() = updateInfo?.downloadUrl?.trim()?.takeIf {
            it.startsWith("https://", ignoreCase = true) && it.length > 8 && it.none(Char::isWhitespace)
        }
}
