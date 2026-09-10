package com.anto426.uniapp.updates.presentation

import androidx.lifecycle.ViewModelStore
import com.anto426.uniapp.updates.data.AppUpdateSource
import com.anto426.uniapp.updates.model.InstalledAppBuild
import com.anto426.uniapp.updates.platform.PlatformUpdateLaunchResult
import com.anto426.uniapp.updates.platform.PlatformUpdateLauncher
import com.anto426.uniapp.updates.runtime.AppUpdateController
import com.anto426.unisdk.backend.model.AppUpdateInfo
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class AppUpdateSheetTest {
    @Test
    fun anOpenSheetStaysOpenDuringRechecksForBothOptionalAndMandatoryUpdates() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            for (mandatory in listOf(false, true)) {
                var requests = 0
                val finish = CompletableDeferred<Unit>()
                val controller = AppUpdateController(AppUpdateSource {
                    requests++
                    if (requests > 1) finish.await()
                    AppUpdateInfo(currentVersion = "2.0.3", channel = "stable", track = "release",
                        latestVersion = "2.0.4", latestVersionCode = 1147,
                        downloadUrl = "https://example.invalid/app.apk", isUpdateAvailable = true, isMandatory = mandatory)
                }, InstalledAppBuild("2.0.3", 1146), PlatformUpdateLauncher { _, _ -> PlatformUpdateLaunchResult.Started })
                val store = ViewModelStore()
                try {
                    val viewModel = AppUpdateViewModel(controller)
                    store.put("updates", viewModel)
                    runCurrent()
                    assertEquals(0, requests, "The lifecycle owns the initial check")
                    controller.refresh()
                    runCurrent()
                    assertTrue(viewModel.uiState.value.showUpdateSheet)
                    val refresh = launch { controller.refresh() }
                    runCurrent()
                    assertTrue(viewModel.uiState.value.showUpdateSheet, "A recheck must not recreate the sheet")
                    finish.complete(Unit)
                    refresh.join()
                    runCurrent()
                    assertTrue(viewModel.uiState.value.showUpdateSheet)
                    viewModel.dismissUpdateSheet()
                    runCurrent()
                    controller.refresh()
                    runCurrent()
                    assertFalse(viewModel.uiState.value.showUpdateSheet, "A dismissed release stays dismissed")
                } finally { store.clear() }
            }
        } finally { Dispatchers.resetMain() }
    }
}
