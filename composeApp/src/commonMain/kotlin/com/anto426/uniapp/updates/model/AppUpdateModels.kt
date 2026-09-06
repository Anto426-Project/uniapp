package com.anto426.uniapp.updates.model

import com.anto426.unisdk.backend.model.AppUpdateInfo

typealias InstalledAppBuild = com.anto426.unisdk.platform.AppInfo

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
    val updateInfo: AppUpdateInfo? = null,
    val message: String? = null,
    val selectedChannel: String = installedBuild.releaseChannel,
    val downloadedBytes: Long = 0,
    val totalBytes: Long? = null,
) {
    val isBusy: Boolean
        get() = phase in setOf(AppUpdatePhase.Checking, AppUpdatePhase.Downloading,
            AppUpdatePhase.Verifying, AppUpdatePhase.Installing)

    val canStartUpdate: Boolean
        get() = phase == AppUpdatePhase.Available && updateInfo?.isUpdateAvailable == true && downloadUrl != null
    val isMandatory: Boolean
        get() =
            updateInfo?.isMandatory == true &&
                phase != AppUpdatePhase.UpToDate

    val downloadUrl: String?
        get() = updateInfo?.downloadUrl?.trim()?.takeIf {
            it.startsWith("https://", ignoreCase = true) && it.length > 8 && it.none(Char::isWhitespace)
        }
}
