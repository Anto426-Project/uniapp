package com.anto426.uniapp.updates.presentation

import org.jetbrains.compose.resources.getString
import uniapp.composeapp.generated.resources.*

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
    val statusText: org.jetbrains.compose.resources.StringResource? = null,
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
) {
    val isUpdateAvailable: Boolean
        get() = bannerState == UpdateState.AVAILABLE || availableUpdateKey != null
}

internal class AppUpdateViewModel(
    private val controller: AppUpdateController,
    private val toastSink: AppToastSink = AppToastSink.None,
) : ViewModel() {
    private val dismissedUpdate = MutableStateFlow<String?>(null)

    val uiState: StateFlow<AppUpdateUiState> =
        combine(controller.state, dismissedUpdate) { state, dismissed ->
            val ui = state.toUiState()
            // Rechecking a known update must not remove and recreate the modal.
            val canPresent = !state.isBusy || state.phase == AppUpdatePhase.Checking
            ui.copy(showUpdateSheet = ui.availableUpdateKey != null && ui.availableUpdateKey != dismissed && canPresent)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = controller.state.value.toUiState(),
        )

    init {
        viewModelScope.launch { controller.observeInstallation() }
    }

    fun dismissUpdateSheet() {
        dismissedUpdate.value = controller.state.value.toUiState().availableUpdateKey
    }

    fun refresh() {
        viewModelScope.launch {
            controller.refresh()
            val state = controller.state.value
            when (state.phase) {
                AppUpdatePhase.UpToDate -> toastSink.success(getString(Res.string.msg_lapp_e_aggiornata))
                AppUpdatePhase.Available -> toastSink.info(
                    if (state.isMandatory) getString(Res.string.msg_aggiornamento_obbligatorio_disponibile)
                    else getString(Res.string.msg_e_disponibile_un_nuovo_aggiornamento),
                )
                AppUpdatePhase.Failed -> toastSink.error(
                    state.message ?: getString(Res.string.msg_impossibile_controllare_gli_aggiornamenti),
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
                    controller.state.value.message ?: getString(Res.string.msg_impossibile_avviare_laggiornamento),
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
        statusText = when {
            phase == AppUpdatePhase.Verifying -> Res.string.ui_update_status_verifying
            phase == AppUpdatePhase.Downloading -> Res.string.ui_update_status_downloading
            phase == AppUpdatePhase.Installing -> Res.string.msg_attendi_la_conferma_dellinstallazione
            isMandatory -> Res.string.ui_update_status_mandatory
            phase == AppUpdatePhase.Available -> Res.string.ui_update_new_available
            phase == AppUpdatePhase.UpToDate -> Res.string.ui_updated_version
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
