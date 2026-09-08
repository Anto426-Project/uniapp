package com.anto426.uniapp.updates.runtime

import org.jetbrains.compose.resources.getString
import uniapp.composeapp.generated.resources.*

import com.anto426.uniapp.updates.data.AppUpdateSource
import com.anto426.uniapp.updates.model.AppUpdatePhase
import com.anto426.uniapp.updates.model.AppUpdateState
import com.anto426.uniapp.updates.model.InstalledAppBuild
import com.anto426.uniapp.updates.platform.PlatformUpdateLauncher
import com.anto426.uniapp.updates.platform.PlatformUpdateLaunchResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex

internal class AppUpdateController(
    private val source: AppUpdateSource,
    installedBuild: InstalledAppBuild,
    private val launcher: PlatformUpdateLauncher,
) {
    private val operationLock = Mutex()
    private val mutableState = MutableStateFlow(AppUpdateState(installedBuild = installedBuild))
    val state: StateFlow<AppUpdateState> = mutableState.asStateFlow()

    suspend fun observeInstallation() {
        launcher.updates.collect { progress ->
            mutableState.update {
                it.copy(phase = progress.phase, message = progress.message,
                    installedBuild = progress.installedBuild ?: it.installedBuild,
                    updateInfo = if (progress.phase == AppUpdatePhase.UpToDate)
                        it.updateInfo?.copy(isUpdateAvailable = false, isMandatory = false) else it.updateInfo,
                    downloadedBytes = progress.downloadedBytes, totalBytes = progress.totalBytes)
            }
        }
    }

    suspend fun refresh() {
        if (!operationLock.tryLock()) return
        val previous = mutableState.value
        try {
            val restoringInstall = previous.phase == AppUpdatePhase.Installing && previous.updateInfo == null
            if (previous.isBusy && !restoringInstall) return
            if (!restoringInstall) mutableState.value = previous.copy(phase = AppUpdatePhase.Checking, message = null)
            val info = source.load(previous.installedBuild)
                ?: error(getString(Res.string.msg_informazioni_di_aggiornamento_non_disponibili_per_questo_canale))
            // Numeric build codes are authoritative when both ends supply them.
            val installedCode = previous.installedBuild.versionCode
            val latestCode = info.latestVersionCode
            val newer = if (installedCode != null && latestCode != null && installedCode > 0 && latestCode > 0) {
                latestCode > installedCode
            } else info.isUpdateAvailable
            val available = newer
            mutableState.value = previous.copy(
                phase = if (restoringInstall) mutableState.value.phase else if (available) AppUpdatePhase.Available else AppUpdatePhase.UpToDate,
                updateInfo = info.copy(isUpdateAvailable = available, isMandatory = available && info.isMandatory),
                message = null, downloadedBytes = 0, totalBytes = null,
            )
        } catch (error: CancellationException) {
            mutableState.value = previous
            throw error
        } catch (error: Throwable) {
            mutableState.value = previous.copy(
                phase = if (previous.updateInfo?.isUpdateAvailable == true) AppUpdatePhase.Available else AppUpdatePhase.Failed,
                message = error.message ?: getString(Res.string.msg_impossibile_controllare_gli_aggiornamenti),
            )
        } finally {
            operationLock.unlock()
        }
    }

    suspend fun startUpdate(): Boolean {
        if (!operationLock.tryLock()) return false
        val current = mutableState.value
        try {
            if (!current.canStartUpdate) {
                if (!current.isBusy) mutableState.value = current.copy(message = getString(Res.string.msg_nessun_aggiornamento_installabile_disponibile))
                return false
            }
            mutableState.value = current.copy(phase = AppUpdatePhase.Downloading, message = null,
                downloadedBytes = 0, totalBytes = null)
            return when (val result = launcher.start(current.downloadUrl!!, current.updateInfo?.latestVersionCode)) {
                PlatformUpdateLaunchResult.Started -> {
                    // A callback may already have delivered failure or a confirmation request.
                    mutableState.update {
                        if (it.phase == AppUpdatePhase.Downloading || it.phase == AppUpdatePhase.Verifying)
                            it.copy(phase = AppUpdatePhase.Installing) else it
                    }
                    true
                }
                PlatformUpdateLaunchResult.OpenedExternalStore -> {
                    mutableState.update { it.copy(phase = AppUpdatePhase.Available,
                        message = "Completa l’aggiornamento dall’App Store.") }
                    true
                }
                is PlatformUpdateLaunchResult.Failed -> {
                    mutableState.update { it.copy(phase = AppUpdatePhase.Available, message = result.message) }
                    false
                }
            }
        } catch (error: CancellationException) {
            mutableState.update { if (it.phase == AppUpdatePhase.Installing) it else current }
            throw error
        } catch (error: Throwable) {
            mutableState.update { it.copy(phase = AppUpdatePhase.Available,
                message = error.message ?: getString(Res.string.msg_impossibile_avviare_laggiornamento)) }
            return false
        } finally {
            operationLock.unlock()
        }
    }
}
