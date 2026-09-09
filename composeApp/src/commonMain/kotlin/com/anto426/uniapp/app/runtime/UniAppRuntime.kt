package com.anto426.uniapp.app.runtime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.anto426.uniapp.account.platform.rememberPlatformUniAccountStore
import com.anto426.uniapp.account.session.UniSessionCoordinator
import com.anto426.uniapp.data.SessionUniAppDataSource
import com.anto426.uniapp.data.runtime.UniAppDataCoordinator
import com.anto426.uniapp.data.UniAppDataSource
import com.anto426.uniapp.data.local.EncryptedUniLocalDataStore
import com.anto426.uniapp.data.local.UniLocalDataStore
import com.anto426.uniapp.session.AppSessionController
import com.anto426.uniapp.notifications.platform.rememberPlatformNotificationPermissionController
import com.anto426.uniapp.notifications.runtime.AppNotificationManager
import com.anto426.uniapp.updates.data.UniSdkAppUpdateSource
import com.anto426.uniapp.updates.platform.rememberPlatformAppUpdateEnvironment
import com.anto426.uniapp.updates.runtime.AppUpdateController
import com.anto426.unisdk.backend.RemoteUniBackendService
import com.anto426.unisdk.platform.registerPushNotificationsTokenProvider
import com.anto426.firebase.createPushNotificationConnector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class UniAppRuntime internal constructor(
    val sessionController: AppSessionController,
    val dataSource: UniAppDataSource,
    val localDataStore: UniLocalDataStore,
    private val accountStore: com.anto426.uniapp.account.storage.UniAccountStore,
    internal val updateController: AppUpdateController,
    internal val notificationManager: AppNotificationManager,
    private val sessionCoordinator: UniSessionCoordinator,
    private val backend: RemoteUniBackendService,
    private val unregisterPushTokenProvider: () -> Unit,
) {
    val appInfo: com.anto426.unisdk.platform.AppInfo get() = com.anto426.unisdk.platform.AppInfoProvider.current

    private val cleanupScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val dataScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    internal val applicationImages = com.anto426.uniapp.data.images.ApplicationImageStore(localDataStore, dataScope)
    internal val projectData = com.anto426.uniapp.project.data.ProjectDataStore(dataScope, localDataStore)
    private val lifecycleScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val accountDataSources = mutableMapOf<Pair<String, String?>, UniAppDataCoordinator>()
    private var generation = 0L
    private var ownerState = sessionController.state.value

    init {
        lifecycleScope.launch {
            sessionController.state.collect { state ->
                updateOwner(state)
            }
        }
    }

    // Called only on Main, both by the state collector and before resolving a route.
    private fun updateOwner(state: com.anto426.uniapp.session.model.AppSessionState) {
        if (state === ownerState) return
        val previous = accountDataSources.values.toList()
        accountDataSources.clear()
        ownerState = state
        previous.forEach { old -> dataScope.launch { old.close() } }
    }

    internal fun dataSourceFor(accountId: String, profileId: String?): UniAppDataSource {
        require(accountId.isNotBlank()) { "Account id cannot be blank" }
        updateOwner(sessionController.state.value)
        val ownerKey = accountId to profileId
        return accountDataSources.getOrPut(ownerKey) {
            generation += 1
            val account = (sessionController.state.value as? com.anto426.uniapp.session.model.AppSessionState.Authenticated)?.account
            UniAppDataCoordinator(
                source = if (com.anto426.uniapp.demo.DemoAccount.isDemo(account)) com.anto426.uniapp.demo.DemoAppDataSource() else SessionUniAppDataSource(
                    sessions = sessionController,
                    accounts = accountStore,
                    fixedAccountId = accountId,
                    fixedProfileId = profileId,
                    fallbackToStaleCache = false,
                ),
                parentScope = dataScope,
                generation = generation,
            ).also {
                it.startPortrait(account)
                it.preload(account?.isProfessor == true)
            }
        }
    }

    internal fun close() {
        applicationImages.close()
        projectData.close()
        lifecycleScope.cancel()
        dataScope.cancel()
        unregisterPushTokenProvider()
        notificationManager.close()
        cleanupScope.launch {
            try {
                sessionCoordinator.shutdown()
                backend.close()
            } finally {
                cleanupScope.cancel()
            }
        }
    }
}

@Composable
internal fun rememberUniAppRuntime(): UniAppRuntime {
    val accountStore = rememberPlatformUniAccountStore()
    val updateEnvironment = rememberPlatformAppUpdateEnvironment()
    val notificationPermissions = rememberPlatformNotificationPermissionController()
    val pushConnector = remember { createPushNotificationConnector() }
    val runtime =
        remember(accountStore, updateEnvironment, notificationPermissions, pushConnector) {
            val backend = RemoteUniBackendService()
            val coordinator = UniSessionCoordinator(backend, accountStore)
            val localDataStore = EncryptedUniLocalDataStore(accountStore)
            val sessionController = AppSessionController(coordinator, accountStore, localDataStore)
            val unregisterPushTokenProvider =
                registerPushNotificationsTokenProvider { pushConnector.tokenFlow.value }
            UniAppRuntime(
                sessionController = sessionController,
                dataSource = SessionUniAppDataSource(sessionController, accountStore),
                localDataStore = localDataStore,
                accountStore = accountStore,
                updateController =
                    AppUpdateController(
                        source = UniSdkAppUpdateSource(backend),
                        installedBuild = updateEnvironment.installedBuild,
                        launcher = updateEnvironment.launcher,
                    ),
                notificationManager =
                    AppNotificationManager(
                        connector = pushConnector,
                        permissions = notificationPermissions,
                    ),
                sessionCoordinator = coordinator,
                backend = backend,
                unregisterPushTokenProvider = unregisterPushTokenProvider,
            )
        }
    DisposableEffect(runtime) {
        onDispose(runtime::close)
    }
    return runtime
}
