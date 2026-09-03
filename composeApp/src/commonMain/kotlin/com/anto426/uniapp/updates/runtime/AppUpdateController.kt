package com.anto426.uniapp.updates.runtime

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

    suspend fun startUpdate(): Boolean {
        val current = mutableState.value
        val downloadUrl = current.downloadUrl
        if (downloadUrl == null) {
            mutableState.value = current.copy(message = "Link di aggiornamento non disponibile o non sicuro.")
            return false
        }

        mutableState.value = current.copy(
            phase = AppUpdatePhase.Downloading,
            message = null,
        )
        return when (
            val result = launcher.start(
                downloadUrl = downloadUrl,
                expectedVersionCode = current.updateInfo?.latestVersionCode,
            )
        ) {
            PlatformUpdateLaunchResult.Started -> {
                mutableState.value = mutableState.value.copy(
                    phase = AppUpdatePhase.Installing,
                    message = "Installazione dell’aggiornamento in corso…",
                )
                true
            }

            PlatformUpdateLaunchResult.OpenedExternalStore -> {
                mutableState.value = mutableState.value.copy(
                    phase = AppUpdatePhase.Available,
                    message = "Completa l’aggiornamento dall’App Store.",
                )
                true
            }

            is PlatformUpdateLaunchResult.Failed -> {
                mutableState.value = mutableState.value.copy(
                    phase = AppUpdatePhase.Available,
                    message = result.message,
                )
                false
            }
        }
    }

    private companion object {
        const val DEFAULT_CHECK_ERROR = "Impossibile controllare gli aggiornamenti."
    }
}
