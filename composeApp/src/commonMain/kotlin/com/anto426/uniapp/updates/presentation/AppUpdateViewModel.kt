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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AppUpdateUiState(
    val bannerState: UpdateState = UpdateState.CHECKING,
    val installedVersion: String = "",
    val displayedVersion: String = "",
    val statusText: String? = null,
    val releaseNotes: String? = null,
    val publishedAt: String? = null,
    val isMandatory: Boolean = false,
    val canOpenUpdate: Boolean = false,
    val errorMessage: String? = null,
    val isBusy: Boolean = false,
    val progress: Float? = null,
    val downloadedMb: Float = 0f,
    val totalMb: Float = 0f,
    val availableUpdateKey: String? = null,
    val showUpdateSheet: Boolean = false,
)

internal class AppUpdateViewModel(
    private val controller: AppUpdateController,
    private val toastSink: AppToastSink = AppToastSink.None,
) : ViewModel() {
    private val dismissedUpdate = MutableStateFlow<String?>(null)

    val uiState: StateFlow<AppUpdateUiState> =
        combine(controller.state, dismissedUpdate) { state, dismissed ->
            val ui = state.toUiState()
            ui.copy(showUpdateSheet = ui.availableUpdateKey != null && ui.availableUpdateKey != dismissed && !state.isBusy)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = controller.state.value.toUiState(),
        )

    init {
        viewModelScope.launch { controller.observeInstallation() }
        viewModelScope.launch { controller.refresh() }
    }

    fun dismissUpdateSheet() {
        dismissedUpdate.value = controller.state.value.toUiState().availableUpdateKey
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
                AppUpdatePhase.Verifying,
                AppUpdatePhase.Downloading,
                AppUpdatePhase.Installing,
                -> Unit
            }
        }
    }

    fun openUpdate() {
        if (!controller.state.value.canStartUpdate) return
        dismissUpdateSheet()
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

internal fun AppUpdateState.toUiState(): AppUpdateUiState {
    val info = updateInfo
    return AppUpdateUiState(
        bannerState =
            when (phase) {
                AppUpdatePhase.Idle,
                AppUpdatePhase.Checking,
                -> UpdateState.CHECKING

                AppUpdatePhase.UpToDate -> UpdateState.UP_TO_DATE
                AppUpdatePhase.Available -> UpdateState.AVAILABLE
                AppUpdatePhase.Downloading -> UpdateState.DOWNLOADING
                AppUpdatePhase.Verifying -> UpdateState.VERIFYING
                AppUpdatePhase.Installing -> UpdateState.INSTALLING
                AppUpdatePhase.Failed -> UpdateState.ERROR
            },
        installedVersion = installedBuild.versionName,
        displayedVersion = if (info?.isUpdateAvailable == true) info.latestVersion else installedBuild.versionName,
        statusText =
            when {
                phase == AppUpdatePhase.Verifying -> "Verifica dell’aggiornamento…"
                phase == AppUpdatePhase.Downloading -> "Download dell’aggiornamento…"
                phase == AppUpdatePhase.Installing -> "Attendi la conferma dell’installazione…"
                isMandatory -> "Aggiornamento obbligatorio"
                phase == AppUpdatePhase.Available -> "Nuovo aggiornamento disponibile"
                phase == AppUpdatePhase.UpToDate -> "Versione aggiornata"
                else -> null
            },
        releaseNotes = info?.notes,
        publishedAt = info?.publishedAt,
        isMandatory = isMandatory,
        canOpenUpdate = canStartUpdate,
        isBusy = isBusy,
        progress = totalBytes?.takeIf { it > 0 }?.let { (downloadedBytes.toFloat() / it).coerceIn(0f, 1f) },
        downloadedMb = downloadedBytes / (1024f * 1024f),
        totalMb = (totalBytes ?: 0) / (1024f * 1024f),
        availableUpdateKey = info?.takeIf { it.isUpdateAvailable }?.let {
            "${it.channel}:${it.latestVersionCode}:${it.latestVersion}:${it.isMandatory}"
        },
        errorMessage = message,
    )
}
