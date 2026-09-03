package com.anto426.uniapp.app.runtime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.anto426.uniapp.account.platform.rememberPlatformUniAccountStore
import com.anto426.uniapp.account.session.UniSessionCoordinator
import com.anto426.uniapp.data.SessionUniAppDataSource
import com.anto426.uniapp.data.UniAppDataSource
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
    private val accountStore: com.anto426.uniapp.account.storage.UniAccountStore,
    internal val updateController: AppUpdateController,
    internal val notificationManager: AppNotificationManager,
    private val sessionCoordinator: UniSessionCoordinator,
    private val backend: RemoteUniBackendService,
    private val unregisterPushTokenProvider: () -> Unit,
) {
    private val cleanupScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val accountDataSources = mutableMapOf<String, UniAppDataSource>()

    internal fun dataSourceFor(accountId: String, profileId: String?): UniAppDataSource {
        require(accountId.isNotBlank()) { "Account id cannot be blank" }
        val ownerKey = "$accountId|${profileId.orEmpty()}"
        return accountDataSources.getOrPut(ownerKey) {
            SessionUniAppDataSource(
                sessions = sessionController,
                accounts = accountStore,
                fixedAccountId = accountId,
                fixedProfileId = profileId,
            )
        }
    }

    internal fun close() {
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
            val sessionController = AppSessionController(coordinator, accountStore)
            val unregisterPushTokenProvider =
                registerPushNotificationsTokenProvider { pushConnector.tokenFlow.value }
            UniAppRuntime(
                sessionController = sessionController,
                dataSource = SessionUniAppDataSource(sessionController, accountStore),
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
