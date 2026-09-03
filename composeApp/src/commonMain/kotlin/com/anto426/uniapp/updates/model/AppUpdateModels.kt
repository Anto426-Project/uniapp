package com.anto426.uniapp.updates.model

import com.anto426.unisdk.backend.model.AppUpdateInfo

data class InstalledAppBuild(
    val versionName: String,
    val versionCode: Int?,
    val isDebuggable: Boolean = false,
)

enum class AppUpdatePhase {
    Idle,
    Checking,
    UpToDate,
    Available,
    Downloading,
    Installing,
    Failed,
}

data class AppUpdateState(
    val installedBuild: InstalledAppBuild,
    val phase: AppUpdatePhase = AppUpdatePhase.Idle,
    val updateInfo: AppUpdateInfo? = null,
    val message: String? = null,
) {
    val isMandatory: Boolean
        get() =
            !installedBuild.isDebuggable &&
                updateInfo?.isMandatory == true &&
                phase != AppUpdatePhase.UpToDate

    val downloadUrl: String?
        get() = updateInfo?.downloadUrl?.takeIf { it.startsWith("https://") }
}
