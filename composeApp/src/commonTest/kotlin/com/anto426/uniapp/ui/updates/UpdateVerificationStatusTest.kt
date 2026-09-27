package com.anto426.uniapp.ui.updates

import com.anto426.uniapp.updates.model.AppUpdatePhase
import com.anto426.uniapp.updates.model.AppUpdateState
import com.anto426.uniapp.updates.presentation.toUiState
import com.anto426.unisdk.platform.AppInfo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UpdateVerificationStatusTest {
    @Test
    fun verificationFollowsTheActualAndroidUpdaterPhase() {
        val build = AppInfo(versionName = "1.0", platform = "android")
        fun status(phase: AppUpdatePhase) = AppUpdateState(build, phase = phase)
            .toUiState().bannerState.packageVerificationStatus()

        assertEquals(PackageVerificationStatus.Pending, status(AppUpdatePhase.UpToDate))
        assertEquals(PackageVerificationStatus.Downloading, status(AppUpdatePhase.Downloading))
        assertEquals(PackageVerificationStatus.Checking, status(AppUpdatePhase.Verifying))
        assertEquals(PackageVerificationStatus.Passed, status(AppUpdatePhase.Installing))
        assertTrue(AppUpdateState(build).toUiState().supportsPackageVerification)
        assertFalse(AppUpdateState(build.copy(platform = "ios")).toUiState().supportsPackageVerification)
    }
}
