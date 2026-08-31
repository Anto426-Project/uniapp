package com.anto426.uniapp.updates.runtime

import com.anto426.uniapp.updates.data.AppUpdateSource
import com.anto426.uniapp.updates.model.AppUpdatePhase
import com.anto426.uniapp.updates.model.InstalledAppBuild
import com.anto426.uniapp.updates.platform.PlatformUpdateLauncher
import com.anto426.unisdk.backend.model.AppUpdateInfo
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppUpdateControllerTest {
    @Test
    fun availableUpdateIsPublishedAndCanOpenHttpsDownload() = runTest {
        var openedUrl: String? = null
        val controller =
            controller(
                source = AppUpdateSource { updateInfo(isAvailable = true) },
                launcher = PlatformUpdateLauncher { url -> openedUrl = url; true },
            )

        controller.refresh()

        assertEquals(AppUpdatePhase.Available, controller.state.value.phase)
        assertFalse(controller.state.value.isMandatory)
        assertTrue(controller.openUpdate())
        assertEquals(DOWNLOAD_URL, openedUrl)
    }

    @Test
    fun mandatoryDecisionSurvivesTransientRefreshFailure() = runTest {
        var attempt = 0
        val controller =
            controller(
                source =
                    AppUpdateSource {
                        attempt++
                        if (attempt == 1) updateInfo(isAvailable = true, isMandatory = true)
                        else error("offline")
                    },
            )

        controller.refresh()
        controller.refresh()

        assertEquals(AppUpdatePhase.Available, controller.state.value.phase)
        assertTrue(controller.state.value.isMandatory)
        assertEquals("offline", controller.state.value.message)
    }

    @Test
    fun insecureDownloadUrlIsRejected() = runTest {
        var launchCount = 0
        val controller =
            controller(
                source =
                    AppUpdateSource {
                        updateInfo(isAvailable = true).copy(downloadUrl = "http://example.invalid/app.apk")
                    },
                launcher = PlatformUpdateLauncher { launchCount++; true },
            )

        controller.refresh()

        assertFalse(controller.openUpdate())
        assertEquals(0, launchCount)
    }

    @Test
    fun currentBuildIsPublishedAsUpToDate() = runTest {
        val controller = controller(AppUpdateSource { updateInfo(isAvailable = false) })

        controller.refresh()

        assertEquals(AppUpdatePhase.UpToDate, controller.state.value.phase)
    }

    @Test
    fun debugBuildReportsUpdateWithoutActivatingMandatoryGate() = runTest {
        val controller =
            AppUpdateController(
                source = AppUpdateSource { updateInfo(isAvailable = true, isMandatory = true) },
                installedBuild = InstalledAppBuild("1.0", 1, isDebuggable = true),
                launcher = PlatformUpdateLauncher { true },
            )

        controller.refresh()

        assertEquals(AppUpdatePhase.Available, controller.state.value.phase)
        assertFalse(controller.state.value.isMandatory)
    }

    private fun controller(
        source: AppUpdateSource,
        launcher: PlatformUpdateLauncher = PlatformUpdateLauncher { true },
    ) = AppUpdateController(
        source = source,
        installedBuild = InstalledAppBuild("1.0", 1),
        launcher = launcher,
    )

    private fun updateInfo(
        isAvailable: Boolean,
        isMandatory: Boolean = false,
    ) = AppUpdateInfo(
        currentVersion = "1.0",
        currentVersionCode = 1,
        channel = "stable",
        track = "release",
        latestVersion = "2.0",
        latestVersionCode = 2,
        minSupportedVersion = if (isMandatory) "2.0" else null,
        minSupportedVersionCode = if (isMandatory) 2 else null,
        isUpdateAvailable = isAvailable,
        isMandatory = isMandatory,
        downloadUrl = DOWNLOAD_URL,
    )

    private companion object {
        const val DOWNLOAD_URL = "https://example.invalid/app.apk"
    }
}
