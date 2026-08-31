package com.anto426.uniapp.updates.data

import com.anto426.uniapp.updates.model.InstalledAppBuild
import com.anto426.unisdk.backend.UniBackendService
import com.anto426.unisdk.backend.model.AppUpdateInfo

internal fun interface AppUpdateSource {
    suspend fun load(installedBuild: InstalledAppBuild): AppUpdateInfo?
}

internal class UniSdkAppUpdateSource(
    private val backend: UniBackendService,
) : AppUpdateSource {
    override suspend fun load(installedBuild: InstalledAppBuild): AppUpdateInfo? =
        backend.loadAppUpdateInfo(
            currentVersion = installedBuild.versionName,
            currentVersionCode = installedBuild.versionCode,
            preferredUpdateChannel = STABLE_UPDATE_CHANNEL,
        )

    private companion object {
        const val STABLE_UPDATE_CHANNEL = "stable"
    }
}
