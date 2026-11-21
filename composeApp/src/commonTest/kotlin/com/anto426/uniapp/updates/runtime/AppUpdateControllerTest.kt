package com.anto426.uniapp.updates.runtime

import com.anto426.uniapp.data.UniUpdateManager
import com.anto426.uniapp.data.UniUpdateManifestLoader
import com.anto426.uniapp.updates.model.AppUpdatePhase
import com.anto426.uniapp.updates.model.InstalledAppBuild
import com.anto426.uniapp.updates.model.UniAppUpdateManifest
import com.anto426.uniapp.updates.platform.PlatformUpdateLaunchResult
import com.anto426.uniapp.updates.platform.PlatformUpdateLauncher
import com.anto426.uniapp.updates.presentation.toUiState
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppUpdateControllerTest {
    @Test
    fun aHigherBuildCodeOffersAnUpdateEvenWhenTheVersionNameAndRemoteFlagAreUnchanged() = runTest {
        val controller = UniUpdateManager(
            UniUpdateManifestLoader { _ -> updateInfo(false).copy(latestVersion = "2.0.3", latestVersionCode = 1146) },
            InstalledAppBuild("2.0.3", 1145), successfulLauncher(),
        )
        controller.refresh()
        assertEquals(AppUpdatePhase.Available, controller.state.value.phase)
    }

    @Test
    fun equalBuildCodesNeverOfferAnUpdateBasedOnlyOnTheVersionName() = runTest {
        val controller = UniUpdateManager(
            UniUpdateManifestLoader { _ -> updateInfo(true, true).copy(latestVersion = "9.0", latestVersionCode = 1145) },
            InstalledAppBuild("2.0.3", 1145), successfulLauncher(),
        )
        controller.refresh()
        assertEquals(AppUpdatePhase.UpToDate, controller.state.value.phase)
        assertFalse(controller.state.value.isMandatory)
    }

    @Test
    fun availableUpdateIsPublishedAndCanOpenHttpsDownload() = runTest {
        var openedUrl: String? = null
        val controller =
            controller(
                source = UniUpdateManifestLoader { _ -> updateInfo(isAvailable = true) },
                launcher = PlatformUpdateLauncher { url, _ ->
                    openedUrl = url
                    PlatformUpdateLaunchResult.Started
                },
            )

        controller.refresh()

        assertEquals(AppUpdatePhase.Available, controller.state.value.phase)
        assertFalse(controller.state.value.isMandatory)
        assertTrue(controller.startUpdate())
        assertEquals(DOWNLOAD_URL, openedUrl)
        assertEquals(AppUpdatePhase.Installing, controller.state.value.phase)
    }

    @Test
    fun mandatoryDecisionSurvivesTransientRefreshFailure() = runTest {
        var attempt = 0
        val controller =
            controller(
                source =
                    UniUpdateManifestLoader { _ ->
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
                    UniUpdateManifestLoader { _ ->
                        updateInfo(isAvailable = true).copy(downloadUrl = "http://example.invalid/app.apk")
                    },
                launcher = PlatformUpdateLauncher { _, _ ->
                    launchCount++
                    PlatformUpdateLaunchResult.Started
                },
            )

        controller.refresh()

        assertFalse(controller.startUpdate())
        assertEquals(0, launchCount)
    }

    @Test
    fun currentBuildIsPublishedAsUpToDate() = runTest {
        val controller = controller(UniUpdateManifestLoader { _ -> updateInfo(isAvailable = false) })

        controller.refresh()

        assertEquals(AppUpdatePhase.UpToDate, controller.state.value.phase)
    }

    @Test
    fun debugBuildDoesNotEnforceMandatoryGate() = runTest {
        val controller =
            UniUpdateManager(
                source = UniUpdateManifestLoader { _ -> updateInfo(isAvailable = true, isMandatory = true) },
                installedBuild = InstalledAppBuild("1.0", 1, isDebuggable = true),
                launcher = successfulLauncher(),
            )

        controller.refresh()

        assertEquals(AppUpdatePhase.Available, controller.state.value.phase)
        assertFalse(controller.state.value.isMandatory)
        assertFalse(controller.state.value.updateInfo?.isMandatory == true)
    }

    @Test
    fun newerInstalledBuildNeverOffersRemoteDowngradeEvenIfMarkedMandatory() = runTest {
        val controller = UniUpdateManager(
            UniUpdateManifestLoader { _ -> updateInfo(true, true).copy(latestVersion = "1.8.9-beta", latestVersionCode = 199) },
            InstalledAppBuild("2.0", 200), successfulLauncher(),
        )
        controller.refresh()
        assertEquals(AppUpdatePhase.UpToDate, controller.state.value.phase)
        assertFalse(controller.state.value.isMandatory)
        assertFalse(controller.startUpdate())
        assertEquals("2.0", controller.state.value.toUiState().displayedVersion)
    }

    @Test
    fun installCannotBeStartedTwiceOrOverwrittenByRefresh() = runTest {
        var launches = 0
        var checks = 0
        val controller = controller(UniUpdateManifestLoader { _ -> checks++; updateInfo(true) },
            PlatformUpdateLauncher { _, _ -> launches++; PlatformUpdateLaunchResult.Started })
        controller.refresh()
        assertTrue(controller.startUpdate())
        assertFalse(controller.startUpdate())
        controller.refresh()
        assertEquals(1, launches)
        assertEquals(1, checks)
        assertEquals(AppUpdatePhase.Installing, controller.state.value.phase)
    }

    @Test
    fun cancelledCheckRestoresPreviousState() = runTest {
        val controller = controller(UniUpdateManifestLoader { _ -> throw kotlinx.coroutines.CancellationException() })
        kotlin.test.assertFailsWith<kotlinx.coroutines.CancellationException> { controller.refresh() }
        assertEquals(AppUpdatePhase.Idle, controller.state.value.phase)
    }

    @Test
    fun thrownLauncherFailureCanBeRetried() = runTest {
        val controller = controller(UniUpdateManifestLoader { _ -> updateInfo(true) },
            PlatformUpdateLauncher { _, _ -> error("Installer failed") })
        controller.refresh()
        assertFalse(controller.startUpdate())
        assertEquals(AppUpdatePhase.Available, controller.state.value.phase)
        assertTrue(controller.state.value.canStartUpdate)
        assertEquals("Installer failed", controller.state.value.message)
    }

    @Test
    fun mandatoryUpdateRemainsRequiredAfterRefresh() = runTest {
        var checks = 0
        val controller = controller(UniUpdateManifestLoader { _ -> checks++; updateInfo(true, true) })
        controller.refresh()
        controller.refresh()
        assertEquals(2, checks)
        assertTrue(controller.state.value.isMandatory)
    }

    private fun controller(
        source: UniUpdateManifestLoader,
        launcher: PlatformUpdateLauncher = successfulLauncher(),
    ) = UniUpdateManager(
        source = source,
        installedBuild = InstalledAppBuild("1.0", 1),
        launcher = launcher,
    )

    private fun successfulLauncher() =
        PlatformUpdateLauncher { _, _ -> PlatformUpdateLaunchResult.Started }

    private fun updateInfo(
        isAvailable: Boolean,
        isMandatory: Boolean = false,
    ) = UniAppUpdateManifest(
        channel = "stable",
        latestVersion = "2.0",
        latestVersionCode = if (isAvailable) 2 else 1,
        isUpdateAvailable = isAvailable,
        isMandatory = isMandatory,
        downloadUrl = DOWNLOAD_URL,
    )

    private companion object {
        const val DOWNLOAD_URL = "https://example.invalid/app.apk"
    }
}
