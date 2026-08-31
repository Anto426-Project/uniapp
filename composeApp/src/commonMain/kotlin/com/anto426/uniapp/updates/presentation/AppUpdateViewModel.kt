package com.anto426.uniapp.updates.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.model.updates.UpdateState
import com.anto426.uniapp.updates.model.AppUpdatePhase
import com.anto426.uniapp.updates.model.AppUpdateState
import com.anto426.uniapp.updates.runtime.AppUpdateController
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AppUpdateUiState(
    val bannerState: UpdateState = UpdateState.CHECKING,
    val installedVersion: String = "",
    val displayedVersion: String = "",
    val channel: String = "stable",
    val statusText: String? = null,
    val releaseNotes: String? = null,
    val isMandatory: Boolean = false,
    val canOpenUpdate: Boolean = false,
    val errorMessage: String? = null,
)

internal class AppUpdateViewModel(
    private val controller: AppUpdateController,
) : ViewModel() {
    val uiState: StateFlow<AppUpdateUiState> =
        controller.state
            .map(AppUpdateState::toUiState)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = controller.state.value.toUiState(),
            )

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch { controller.refresh() }
    }

    fun openUpdate() {
        controller.openUpdate()
    }
}

private fun AppUpdateState.toUiState(): AppUpdateUiState {
    val info = updateInfo
    return AppUpdateUiState(
        bannerState =
            when (phase) {
                AppUpdatePhase.Idle,
                AppUpdatePhase.Checking,
                -> UpdateState.CHECKING

                AppUpdatePhase.UpToDate -> UpdateState.UP_TO_DATE
                AppUpdatePhase.Available -> UpdateState.AVAILABLE
                AppUpdatePhase.Failed -> UpdateState.ERROR
            },
        installedVersion = installedBuild.versionName,
        displayedVersion = info?.latestVersion ?: installedBuild.versionName,
        channel = info?.channel ?: "stable",
        statusText =
            when {
                isMandatory -> "Aggiornamento obbligatorio"
                phase == AppUpdatePhase.Available -> "Nuovo aggiornamento disponibile"
                phase == AppUpdatePhase.UpToDate -> "Versione aggiornata"
                else -> null
            },
        releaseNotes = info?.notes,
        isMandatory = isMandatory,
        canOpenUpdate = downloadUrl != null,
        errorMessage = message,
    )
}
