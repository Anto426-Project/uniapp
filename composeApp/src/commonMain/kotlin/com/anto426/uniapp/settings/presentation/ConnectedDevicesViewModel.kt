package com.anto426.uniapp.settings.presentation

import org.jetbrains.compose.resources.getString
import uniapp.composeapp.generated.resources.*

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.UniAppDataSource
import com.anto426.uniapp.data.toDeviceInfo
import com.anto426.uniapp.feedback.runtime.AppToastSink
import com.anto426.uniapp.feedback.runtime.error
import com.anto426.uniapp.feedback.runtime.success
import com.anto426.uniapp.feedback.runtime.warning
import com.anto426.uniapp.model.settings.DeviceInfo
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.userMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
}

class ConnectedDevicesViewModel(
    private val dataSource: UniAppDataSource,
    private val toastSink: AppToastSink = AppToastSink.None,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(ConnectedDevicesUiState())
    val uiState: StateFlow<ConnectedDevicesUiState> = mutableUiState.asStateFlow()

    init { refresh() }

    fun refresh(force: Boolean = false) {
        viewModelScope.launch {
            mutableUiState.value = mutableUiState.value.copy(loadState = FeatureLoadState.Loading, errorMessage = null)
            try {
                val devices = dataSource.loadConnectedDevices(force).toDeviceInfo()
                mutableUiState.value = ConnectedDevicesUiState(
                    devices = devices,
                    loadState = if (devices.isEmpty()) FeatureLoadState.Empty else FeatureLoadState.Content,
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                mutableUiState.value = mutableUiState.value.copy(
                    loadState = FeatureLoadState.Error,
                    errorMessage = error.userMessage(getString(Res.string.msg_impossibile_caricare_i_dispositivi_collegati)),
                )
            }
        }
    }

    fun requestRevocation(device: DeviceInfo) {
        if (!device.isCurrent && !device.revocationToken.isNullOrBlank() && device in mutableUiState.value.devices) {
            mutableUiState.value = mutableUiState.value.copy(devicePendingRevocation = device)
        }
    }

    fun dismissRevocation() {
        mutableUiState.value = mutableUiState.value.copy(devicePendingRevocation = null)
    }

    fun confirmRevocation() {
        val pending = mutableUiState.value.devicePendingRevocation ?: return
        val token = pending.revocationToken
        if (token.isNullOrBlank()) {
            viewModelScope.launch { toastSink.warning(getString(Res.string.msg_questa_sessione_non_espone_un_token_revocabile)) }
            dismissRevocation()
            return
        }
        viewModelScope.launch {
            mutableUiState.value = mutableUiState.value.copy(isMutating = true, errorMessage = null)
            try {
                val message = dataSource.disconnectDevice(token)
                mutableUiState.value = mutableUiState.value.copy(
                    devicePendingRevocation = null,
                    isMutating = false,
                )
                toastSink.success(message)
                refresh(force = true)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                val message = error.userMessage(getString(Res.string.msg_impossibile_revocare_il_dispositivo))
                mutableUiState.value = mutableUiState.value.copy(
                    isMutating = false,
                )
                toastSink.error(message)
            }
        }
    }
}
