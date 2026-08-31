package com.anto426.uniapp.settings.presentation

import androidx.lifecycle.ViewModel
import com.anto426.uniapp.model.settings.DeviceInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ConnectedDevicesUiState(
    val devices: List<DeviceInfo> = emptyList(),
    val devicePendingRevocation: DeviceInfo? = null,
) {
    val currentDevice: DeviceInfo? get() = devices.firstOrNull { it.isCurrent }
    val otherDevices: List<DeviceInfo> get() = devices.filterNot { it.isCurrent }
}

class ConnectedDevicesViewModel(devices: List<DeviceInfo>) : ViewModel() {
    private val mutableUiState = MutableStateFlow(ConnectedDevicesUiState(devices = devices))
    val uiState: StateFlow<ConnectedDevicesUiState> = mutableUiState.asStateFlow()

    fun requestRevocation(device: DeviceInfo) {
        if (!device.isCurrent && device in mutableUiState.value.devices) {
            mutableUiState.value = mutableUiState.value.copy(devicePendingRevocation = device)
        }
    }

    fun dismissRevocation() {
        mutableUiState.value = mutableUiState.value.copy(devicePendingRevocation = null)
    }

    fun confirmRevocation() {
        val pending = mutableUiState.value.devicePendingRevocation ?: return
        mutableUiState.value =
            mutableUiState.value.copy(
                devices = mutableUiState.value.devices - pending,
                devicePendingRevocation = null,
            )
    }
}
