package com.anto426.uniapp.data

import com.anto426.uniapp.localization.readAppString
import com.anto426.uniapp.presentation.userMessage
import com.anto426.uniapp.updates.model.AppUpdatePhase
import com.anto426.uniapp.updates.model.AppUpdateState
import com.anto426.uniapp.updates.model.InstalledAppBuild
import com.anto426.uniapp.updates.model.UniAppUpdateManifest
import com.anto426.uniapp.updates.platform.PlatformUpdateLaunchResult
import com.anto426.uniapp.updates.platform.PlatformUpdateLauncher
import com.anto426.unisdk.backend.UniBackendService
import com.anto426.unisdk.platform.APP_CHANNEL_STABLE
import com.anto426.unisdk.platform.currentEpochMillis
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import uniapp.composeapp.generated.resources.*

internal fun interface UniUpdateManifestLoader {
    suspend fun load(installedBuild: InstalledAppBuild): UniAppUpdateManifest?
}

internal class SdkUniUpdateManifestLoader(
    private val backend: UniBackendService,
) : UniUpdateManifestLoader {
    override suspend fun load(installedBuild: InstalledAppBuild): UniAppUpdateManifest? =
        backend.loadAppUpdateInfo(
            currentVersion = installedBuild.versionName,
            currentVersionCode = installedBuild.versionCode,
            preferredUpdateChannel = APP_CHANNEL_STABLE,
        )?.toUniAppUpdateManifest()
}

internal class UniUpdateManager(
    private val source: UniUpdateManifestLoader,
    installedBuild: InstalledAppBuild,
    private val launcher: PlatformUpdateLauncher,
    private val policy: UniCachePolicy = UniCachePolicies.AppUpdate,
    private val nowMillis: () -> Long = ::currentEpochMillis,
) {
    private val operationLock = Mutex()
    private var lastCheckedAtMillis: Long? = null
    private val mutableState = MutableStateFlow(AppUpdateState(installedBuild = installedBuild))
    val state: StateFlow<AppUpdateState> = mutableState.asStateFlow()

    suspend fun observeInstallation() {
        launcher.updates.collect { progress ->
            mutableState.update {
                it.copy(
                    phase = progress.phase,
                    message = progress.message,
                    installedBuild = progress.installedBuild ?: it.installedBuild,
                    updateInfo = if (progress.phase == AppUpdatePhase.UpToDate)
                        it.updateInfo?.copy(isUpdateAvailable = false, isMandatory = false) else it.updateInfo,
                    downloadedBytes = progress.downloadedBytes,
                    totalBytes = progress.totalBytes,
                )
            }
        }
    }

    suspend fun refresh(force: Boolean = true) {
        if (!operationLock.tryLock()) return
        val previous = mutableState.value
        try {
            val now = nowMillis()
            val lastChecked = lastCheckedAtMillis
            if (!force && lastChecked != null && now - lastChecked in 0 until policy.maxAgeMillis &&
                (previous.phase == AppUpdatePhase.UpToDate || previous.phase == AppUpdatePhase.Available)
            ) {
                return
            }
            val restoringInstall = previous.phase == AppUpdatePhase.Installing && previous.updateInfo == null
            if (previous.isBusy && !restoringInstall) return
            if (!restoringInstall) mutableState.value = previous.copy(phase = AppUpdatePhase.Checking, message = null)
            val info = source.load(previous.installedBuild)
                ?: error(readAppString(Res.string.msg_informazioni_di_aggiornamento_non_disponibili_per_questo_canale))
            val installedCode = previous.installedBuild.versionCode
            val latestCode = info.latestVersionCode
            val newer = if (installedCode != null && latestCode != null && installedCode > 0 && latestCode > 0) {
                latestCode > installedCode
            } else info.isUpdateAvailable
            val available = newer
            lastCheckedAtMillis = nowMillis()
            mutableState.value = previous.copy(
                phase = if (restoringInstall) mutableState.value.phase else if (available) AppUpdatePhase.Available else AppUpdatePhase.UpToDate,
                updateInfo = info.copy(
                    isUpdateAvailable = available,
                    isMandatory = available && info.isMandatory && !previous.installedBuild.isDebuggable,
                ),
                message = null,
                downloadedBytes = 0,
                totalBytes = null,
            )
        } catch (error: CancellationException) {
            mutableState.value = previous
            throw error
        } catch (error: Throwable) {
            mutableState.value = previous.copy(
                phase = if (previous.updateInfo?.isUpdateAvailable == true) AppUpdatePhase.Available else AppUpdatePhase.Failed,
                message = error.userMessage { readAppString(Res.string.msg_impossibile_controllare_gli_aggiornamenti) },
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
                if (!current.isBusy) {
                    mutableState.value = current.copy(message = readAppString(Res.string.msg_nessun_aggiornamento_installabile_disponibile))
                }
                return false
            }
            mutableState.value = current.copy(
                phase = AppUpdatePhase.Downloading,
                message = null,
                downloadedBytes = 0,
                totalBytes = null,
            )
            return when (val result = launcher.start(current.downloadUrl!!, current.updateInfo?.latestVersionCode)) {
                PlatformUpdateLaunchResult.Started -> {
                    mutableState.update {
                        if (it.phase == AppUpdatePhase.Downloading || it.phase == AppUpdatePhase.Verifying)
                            it.copy(phase = AppUpdatePhase.Installing) else it
                    }
                    true
                }
                PlatformUpdateLaunchResult.OpenedExternalStore -> {
                    val message = readAppString(Res.string.msg_update_finish_in_app_store)
                    mutableState.update { it.copy(phase = AppUpdatePhase.Available, message = message) }
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
            mutableState.update {
                it.copy(
                    phase = AppUpdatePhase.Available,
                    message = error.userMessage { readAppString(Res.string.msg_impossibile_avviare_laggiornamento) },
                )
            }
            return false
        } finally {
            operationLock.unlock()
        }
    }
}
