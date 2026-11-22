package com.anto426.uniapp.settings.presentation

import com.anto426.uniapp.localization.readAppString
import uniapp.composeapp.generated.resources.*

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.UniDatasets
import com.anto426.uniapp.data.UniGateway
import com.anto426.uniapp.data.asUniRepository
import com.anto426.uniapp.data.contract.DeviceInfo
import com.anto426.uniapp.data.contract.toDeviceInfo
import com.anto426.uniapp.data.observeIn
import com.anto426.uniapp.feedback.runtime.AppToastSink
import com.anto426.uniapp.feedback.runtime.error
import com.anto426.uniapp.feedback.runtime.success
import com.anto426.uniapp.feedback.runtime.warning
import com.anto426.uniapp.presentation.onRefresh
import com.anto426.uniapp.presentation.onRefreshFailure
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.userMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ConnectedDevicesUiState(
    val devices: List<DeviceInfo> = emptyList(),
    val devicePendingRevocation: DeviceInfo? = null,
    val loadState: FeatureLoadState = FeatureLoadState.Loading,
    val errorMessage: String? = null,
    val isMutating: Boolean = false,
) {
    val currentDevice: DeviceInfo? get() = devices.firstOrNull { it.isCurrent }
    val otherDevices: List<DeviceInfo> get() = devices.filterNot { it.isCurrent }
    val canManageSessions: Boolean get() = loadState == FeatureLoadState.Content && errorMessage == null && !isMutating
    val canDisconnectAll: Boolean get() = canManageSessions && devices.any { !it.isCurrent }
}

class ConnectedDevicesViewModel(
    private val dataSource: UniGateway,
    private val toastSink: AppToastSink = AppToastSink.None,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(ConnectedDevicesUiState())
    val uiState: StateFlow<ConnectedDevicesUiState> = mutableUiState.asStateFlow()

    private val repository = dataSource.asUniRepository(viewModelScope)
    private val sharedData = repository.engine
    private val dataRequests = listOf(UniDatasets.Devices)
    private val dataObservation = sharedData.observeIn(viewModelScope, dataRequests, subscriptions = mutableUiState.subscriptionCount) { snapshot ->
        val result = snapshot.state(UniDatasets.Devices)
        val devices = result.value?.toDeviceInfo()
        val message = result.error?.userMessage {
            readAppString(Res.string.msg_impossibile_caricare_i_dispositivi_collegati)
        }
        mutableUiState.update { state ->
            state.copy(
                devices = devices ?: state.devices,
                loadState = when {
                    devices == null -> state.loadState.onRefreshFailure()
                    devices.isEmpty() -> FeatureLoadState.Empty
                    else -> FeatureLoadState.Content
                },
                errorMessage = message,
            )
        }
    }

    fun refresh(force: Boolean = false) {
        mutableUiState.update { it.copy(loadState = it.loadState.onRefresh()) }
        sharedData.refresh(dataRequests, force)
    }

    fun requestRevocation(device: DeviceInfo) {
        val state = mutableUiState.value
        if (!state.canManageSessions || device.isCurrent || device !in state.devices) return
        if (device.revocationToken.isNullOrBlank()) {
            viewModelScope.launch {
                toastSink.warning(readAppString(Res.string.msg_questa_sessione_non_espone_un_token_revocabile))
            }
            return
        }
        mutableUiState.value = mutableUiState.value.copy(devicePendingRevocation = device)
    }

    fun dismissRevocation() {
        mutableUiState.value = mutableUiState.value.copy(devicePendingRevocation = null)
    }

    fun confirmRevocation() {
        val state = mutableUiState.value
        val pending = state.devicePendingRevocation ?: return
        if (!state.canManageSessions || pending !in state.devices) {
            dismissRevocation()
            return
        }
        val token = pending.revocationToken
        if (token.isNullOrBlank()) {
            viewModelScope.launch { toastSink.warning(readAppString(Res.string.msg_questa_sessione_non_espone_un_token_revocabile)) }
            dismissRevocation()
            return
        }
        viewModelScope.launch {
            mutableUiState.value = mutableUiState.value.copy(isMutating = true, errorMessage = null)
            try {
                val message = sharedData.disconnectDevice(token)
                mutableUiState.value = mutableUiState.value.copy(
                    devicePendingRevocation = null,
                    isMutating = false,
                )
                toastSink.success(message)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                val message = error.userMessage { readAppString(Res.string.msg_impossibile_revocare_il_dispositivo) }
                mutableUiState.value = mutableUiState.value.copy(
                    isMutating = false,
                )
                toastSink.error(message)
            }
        }
    }
}
