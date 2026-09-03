package com.anto426.uniapp.updates.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.model.updates.UpdateState
import com.anto426.uniapp.feedback.runtime.AppToastSink
import com.anto426.uniapp.feedback.runtime.error
import com.anto426.uniapp.feedback.runtime.info
import com.anto426.uniapp.feedback.runtime.success
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
    val publishedAt: String? = null,
    val isMandatory: Boolean = false,
    val canOpenUpdate: Boolean = false,
    val errorMessage: String? = null,
)

internal class AppUpdateViewModel(
    private val controller: AppUpdateController,
    private val toastSink: AppToastSink = AppToastSink.None,
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
        viewModelScope.launch { controller.refresh() }
    }

    fun refresh() {
        viewModelScope.launch {
            controller.refresh()
            val state = controller.state.value
            when (state.phase) {
                AppUpdatePhase.UpToDate -> toastSink.success("L’app è aggiornata.")
                AppUpdatePhase.Available -> toastSink.info(
                    if (state.isMandatory) "Aggiornamento obbligatorio disponibile."
                    else "È disponibile un nuovo aggiornamento.",
                )
                AppUpdatePhase.Failed -> toastSink.error(
                    state.message ?: "Impossibile controllare gli aggiornamenti.",
                )
                AppUpdatePhase.Idle,
                AppUpdatePhase.Checking,
                AppUpdatePhase.Downloading,
                AppUpdatePhase.Installing,
                -> Unit
            }
        }
    }

    fun openUpdate() {
        viewModelScope.launch {
            if (controller.startUpdate()) {
                toastSink.info("Aggiornamento avviato…")
            } else {
                toastSink.error(
                    controller.state.value.message ?: "Impossibile avviare l’aggiornamento.",
                )
            }
        }
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
                AppUpdatePhase.Available,
                AppUpdatePhase.Downloading,
                AppUpdatePhase.Installing,
                -> UpdateState.AVAILABLE
                AppUpdatePhase.Failed -> UpdateState.ERROR
            },
        installedVersion = installedBuild.versionName,
        displayedVersion = info?.latestVersion ?: installedBuild.versionName,
        channel = info?.channel ?: "stable",
        statusText =
            when {
                isMandatory -> "Aggiornamento obbligatorio"
                phase == AppUpdatePhase.Downloading -> "Download dell’aggiornamento…"
                phase == AppUpdatePhase.Installing -> "Installazione e riavvio…"
                phase == AppUpdatePhase.Available -> "Nuovo aggiornamento disponibile"
                phase == AppUpdatePhase.UpToDate -> "Versione aggiornata"
                else -> null
            },
        releaseNotes = info?.notes,
        publishedAt = info?.publishedAt,
        isMandatory = isMandatory,
        canOpenUpdate =
            downloadUrl != null &&
                phase != AppUpdatePhase.Downloading &&
                phase != AppUpdatePhase.Installing,
        errorMessage = message,
    )
}
