package com.anto426.uniapp.updates.runtime

import com.anto426.uniapp.updates.data.AppUpdateSource
import com.anto426.uniapp.updates.model.AppUpdatePhase
import com.anto426.uniapp.updates.model.AppUpdateState
import com.anto426.uniapp.updates.model.InstalledAppBuild
import com.anto426.uniapp.updates.platform.PlatformUpdateLauncher
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class AppUpdateController(
    private val source: AppUpdateSource,
    installedBuild: InstalledAppBuild,
    private val launcher: PlatformUpdateLauncher,
) {
    private val checkLock = Mutex()
    private val mutableState = MutableStateFlow(AppUpdateState(installedBuild = installedBuild))
    val state: StateFlow<AppUpdateState> = mutableState.asStateFlow()

    suspend fun refresh() {
        checkLock.withLock {
            val previous = mutableState.value
            mutableState.value = previous.copy(phase = AppUpdatePhase.Checking, message = null)
            mutableState.value =
                try {
                    val info = source.load(previous.installedBuild)
                        ?: error("Il server non ha restituito informazioni sugli aggiornamenti.")
                    previous.copy(
                        phase =
                            if (info.isUpdateAvailable || info.isMandatory) {
                                AppUpdatePhase.Available
                            } else {
                                AppUpdatePhase.UpToDate
                            },
                        updateInfo = info,
                        message = null,
                    )
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Throwable) {
                    if (previous.updateInfo?.isMandatory == true) {
                        previous.copy(message = error.message ?: DEFAULT_CHECK_ERROR)
                    } else {
                        previous.copy(
                            phase = AppUpdatePhase.Failed,
                            message = error.message ?: DEFAULT_CHECK_ERROR,
                        )
                    }
                }
        }
    }

    fun openUpdate(): Boolean {
        val current = mutableState.value
        val downloadUrl = current.downloadUrl
        if (downloadUrl == null) {
            mutableState.value = current.copy(message = "Link di aggiornamento non disponibile o non sicuro.")
            return false
        }
        val opened = launcher.open(downloadUrl)
        if (!opened) {
            mutableState.value = current.copy(message = "Impossibile aprire il link di aggiornamento.")
        }
        return opened
    }

    private companion object {
        const val DEFAULT_CHECK_ERROR = "Impossibile controllare gli aggiornamenti."
    }
}
