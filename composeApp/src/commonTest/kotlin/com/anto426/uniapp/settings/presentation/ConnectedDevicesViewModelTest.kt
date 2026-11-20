package com.anto426.uniapp.settings.presentation

import com.anto426.uniapp.data.FakeUniAppDataSource
import com.anto426.uniapp.feedback.runtime.AppToastMessage
import com.anto426.uniapp.feedback.runtime.AppToastSink
import com.anto426.unisdk.model.ConnectedDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ConnectedDevicesViewModelTest {
    @Test
    fun currentSessionIsSeparatedFromRevocableSessions() = runViewModelTest {
        val source = devicesSource()
        val viewModel = ConnectedDevicesViewModel(source)
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.currentDevice)
        assertTrue(viewModel.uiState.value.currentDevice?.isCurrent == true)
        assertEquals("other-token", viewModel.uiState.value.otherDevices.single().revocationToken)
    }

    @Test
    fun disconnectAllRequiresASuccessfulListWithOtherSessions() = runViewModelTest {
        var connectionFailed = false
        val viewModel = ConnectedDevicesViewModel(devicesSource { connectionFailed })
        assertFalse(viewModel.uiState.value.canDisconnectAll)
        advanceUntilIdle()

        val loaded = viewModel.uiState.value
        assertTrue(loaded.canDisconnectAll)
        assertFalse(loaded.copy(devices = listOfNotNull(loaded.currentDevice)).canDisconnectAll)

        connectionFailed = true
        viewModel.refresh(force = true)
        advanceUntilIdle()

        val failed = viewModel.uiState.value
        assertEquals(loaded.devices, failed.devices)
        assertNotNull(failed.errorMessage)
        assertFalse(failed.canDisconnectAll)
    }

    @Test
    fun revocationKeepsContentAndPublishesToast() = runViewModelTest {
        val messages = mutableListOf<AppToastMessage>()
        val source = devicesSource()
        val viewModel = ConnectedDevicesViewModel(source, AppToastSink(messages::add))
        advanceUntilIdle()

        viewModel.requestRevocation(viewModel.uiState.value.otherDevices.single())
        viewModel.confirmRevocation()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isMutating)
        assertNull(viewModel.uiState.value.devicePendingRevocation)
        assertEquals("Sessione revocata", messages.single().resolveText())
    }

    private fun devicesSource(shouldFail: () -> Boolean = { false }) =
        object : FakeUniAppDataSource() {
            override suspend fun loadConnectedDevices(forceRefresh: Boolean): List<ConnectedDevice> {
                check(!shouldFail()) { "Test device list unavailable" }
                return listOf(
                    ConnectedDevice(
                        lastLogin = "adesso",
                        model = "OnePlus",
                        token = "current-token",
                        isCurrentDevice = true,
                    ),
                    ConnectedDevice(
                        lastLogin = "ieri",
                        model = "Browser",
                        token = "other-token",
                    ),
                )
            }

            override suspend fun disconnectDevice(targetToken: String): String {
                assertEquals("other-token", targetToken)
                return "Sessione revocata"
            }
        }

    private fun runViewModelTest(block: suspend kotlinx.coroutines.test.TestScope.() -> Unit) = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            block()
        } finally {
            Dispatchers.resetMain()
        }
    }
}
