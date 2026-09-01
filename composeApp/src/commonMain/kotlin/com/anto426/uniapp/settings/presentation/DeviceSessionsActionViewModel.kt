package com.anto426.uniapp.settings.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.UniAppDataSource
import com.anto426.uniapp.feedback.runtime.AppToastSink
import com.anto426.uniapp.feedback.runtime.error
import com.anto426.uniapp.feedback.runtime.success
import com.anto426.uniapp.presentation.userMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class DeviceSessionsActionUiState(
    val isConfirmationVisible: Boolean = false,
    val isDisconnecting: Boolean = false,
    val refreshRevision: Int = 0,
)

class DeviceSessionsActionViewModel(
    private val dataSource: UniAppDataSource,
    private val toastSink: AppToastSink = AppToastSink.None,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(DeviceSessionsActionUiState())
    val uiState: StateFlow<DeviceSessionsActionUiState> = mutableUiState.asStateFlow()

    fun requestDisconnectAll() {
        mutableUiState.update { it.copy(isConfirmationVisible = true) }
    }

    fun dismissConfirmation() {
        if (!mutableUiState.value.isDisconnecting) {
            mutableUiState.update { it.copy(isConfirmationVisible = false) }
        }
    }

    fun confirmDisconnectAll() {
        if (mutableUiState.value.isDisconnecting) return
        viewModelScope.launch {
            mutableUiState.update { it.copy(isDisconnecting = true) }
            try {
                val message = dataSource.disconnectAllOtherDevices()
                mutableUiState.update {
                    it.copy(
                        isConfirmationVisible = false,
                        isDisconnecting = false,
                        refreshRevision = it.refreshRevision + 1,
                    )
                }
                toastSink.success(message)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                mutableUiState.update { it.copy(isConfirmationVisible = false, isDisconnecting = false) }
                toastSink.error(error.userMessage("Impossibile chiudere le altre sessioni."))
            }
        }
    }
}
