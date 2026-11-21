package com.anto426.uniapp.app.runtime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.anto426.firebase.createPushNotificationConnector
import com.anto426.uniapp.account.platform.rememberPlatformUniAccountStore
import com.anto426.uniapp.account.storage.UniAccountStore
import com.anto426.uniapp.data.DemoUniGateway
import com.anto426.uniapp.data.PortalUniGateway
import com.anto426.uniapp.data.SdkUniUpdateManifestLoader
import com.anto426.uniapp.data.UniCacheEngine
import com.anto426.uniapp.data.UniDatasets
import com.anto426.uniapp.data.UniGateway
import com.anto426.uniapp.data.UniProjectManager
import com.anto426.uniapp.data.UniRepository
import com.anto426.uniapp.data.UniSessionController
import com.anto426.uniapp.data.UniUpdateManager
import com.anto426.uniapp.data.images.ApplicationImageStore
import com.anto426.uniapp.data.local.EncryptedUniLocalDataStore
import com.anto426.uniapp.data.local.LocalDataScope
import com.anto426.uniapp.data.local.UniAppDataKeys
import com.anto426.uniapp.data.local.UniLocalDataStore
import com.anto426.uniapp.demo.DemoAccount
import com.anto426.uniapp.didactics.data.GradeSimulationRepository
import com.anto426.uniapp.notifications.platform.rememberPlatformNotificationPermissionController
import com.anto426.uniapp.notifications.runtime.AppNotificationManager
import com.anto426.uniapp.session.model.AppSessionState
import com.anto426.uniapp.updates.model.InstalledAppBuild
import com.anto426.uniapp.updates.platform.rememberPlatformAppUpdateEnvironment
import com.anto426.unisdk.backend.RemoteUniBackendService
import com.anto426.unisdk.library.LibraryService
import com.anto426.unisdk.library.RemoteLibraryService
import com.anto426.unisdk.platform.currentEpochMillis
import com.anto426.unisdk.platform.registerPushNotificationsTokenProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class UniAppRuntime internal constructor(
    val sessionController: UniSessionController,
    val defaultGateway: UniGateway,
    val localDataStore: UniLocalDataStore,
    private val accountStore: UniAccountStore,
    internal val updateController: UniUpdateManager,
    internal val notificationManager: AppNotificationManager,
    private val backend: RemoteUniBackendService,
    private val libraryService: LibraryService,
    private val unregisterPushTokenProvider: () -> Unit,
) {
    private val cleanupScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val dataScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    internal val gradeSimulations = GradeSimulationRepository(localDataStore, dataScope)
    internal val applicationImages = ApplicationImageStore(localDataStore, dataScope)
    internal val projectData = UniProjectManager(dataScope, localDataStore)
    internal val defaultRepository = UniRepository(
        engine = UniCacheEngine(defaultGateway, dataScope),
        sessionController = sessionController,
        simulations = gradeSimulations,
        updateManager = updateController,
        projectManager = projectData,
    )
    val appInfo: InstalledAppBuild get() = defaultRepository.system.currentInstalledBuild()

    private val lifecycleScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var activeDataOwner: Pair<String, String?>? = null
    private var activeRepository: UniRepository? = null
    private var generation = 0L
    private var ownerState = sessionController.state.value

    init {
        lifecycleScope.launch {
            sessionController.state.collect { state ->
                updateOwner(state)
                if (state == AppSessionState.Initializing || state == AppSessionState.Authenticating) return@collect
                val accountId = (state as? AppSessionState.Authenticated)?.account
                    ?.takeUnless(DemoAccount::isDemo)?.accountId
                if (notificationManager.boundOwnerId != accountId) {
                    val enabled = accountId?.let { owner ->
                        try { localDataStore.read(LocalDataScope.Account(owner), UniAppDataKeys.NotificationsEnabled) }
                        catch (error: CancellationException) { throw error }
                        catch (_: Exception) { false }
                    } ?: false
                    notificationManager.bindOwner(accountId, enabled)
                }
            }
        }
        lifecycleScope.launch {
            projectData.uiState.map { it.data?.author }.distinctUntilChanged().collect { author ->
                if (author != null) sessionController.updateDemoAuthor(author)
            }
        }
    }

    private fun updateOwner(state: AppSessionState) {
        if (state === ownerState) return
        val before = (ownerState as? AppSessionState.Authenticated)?.account
        val after = (state as? AppSessionState.Authenticated)?.account
        val beforeOwner = before?.let { it.accountId to it.activeProfileId }
        val afterOwner = after?.let { it.accountId to it.activeProfileId }
        ownerState = state
        if (beforeOwner == afterOwner) {
            if (DemoAccount.isDemo(before) && before != after) {
                activeRepository?.engine?.refresh(listOf(UniDatasets.Student), force = true)
            }
            return
        }
        val previous = activeRepository
        activeRepository = null
        activeDataOwner = null
        if (previous != null) dataScope.launch { previous.close() }
    }

    internal fun repositoryFor(accountId: String, profileId: String?): UniRepository {
        require(accountId.isNotBlank()) { "Account id cannot be blank" }
        updateOwner(sessionController.state.value)
        val ownerKey = accountId to profileId
        activeRepository?.takeIf { activeDataOwner == ownerKey }?.let { return it }
        return run {
            generation += 1
            val account = (sessionController.state.value as? AppSessionState.Authenticated)?.account
            val engine = UniCacheEngine(
                source = if (DemoAccount.isDemo(account)) DemoUniGateway(
                    identity = {
                        val current = (sessionController.state.value as? AppSessionState.Authenticated)?.account
                            ?.takeIf { it.accountId == accountId } ?: account
                        (current?.displayName ?: DemoAccount.developerName(null)) to current?.photoUrl
                    },
                    imageLoader = { applicationImages.load(it)?.bytes ?: byteArrayOf() },
                    portraitSharer = { source, bytes ->
                        try {
                            accountStore.writeProfileImage(
                                accountId,
                                source,
                                currentEpochMillis(),
                                bytes,
                            )
                        } catch (error: CancellationException) {
                            throw error
                        } catch (_: Exception) {
                        }
                        sessionController.avatars.publish(accountId, source, bytes)
                    },
                ) else PortalUniGateway(
                    sessions = sessionController,
                    accounts = accountStore,
                    fixedAccountId = accountId,
                    fixedProfileId = profileId,
                    fallbackToStaleCache = false,
                ),
                parentScope = dataScope,
                generation = generation,
            )
            UniRepository(
                engine = engine,
                sessionController = sessionController,
                simulations = gradeSimulations,
                updateManager = updateController,
                projectManager = projectData,
            ).also { repo ->
                activeDataOwner = ownerKey
                activeRepository = repo
                engine.startPortrait(account)
                engine.preload(account?.isProfessor == true)
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
                sessionController.shutdown()
                backend.close()
                libraryService.close()
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
            val libraryService = RemoteLibraryService()
            val localDataStore = EncryptedUniLocalDataStore(accountStore)
            val notificationManager = AppNotificationManager(pushConnector, notificationPermissions)
            val sessionController = UniSessionController(backend, accountStore, localDataStore, libraryService = libraryService) { accountId ->
                val account = accountId?.let { id ->
                    accountStore.snapshot().accounts.firstOrNull { it.accountId == id }
                }
                if (account == null || DemoAccount.isDemo(account)) {
                    notificationManager.bindOwner(null, false)
                } else {
                    val consent = try {
                        localDataStore.read(LocalDataScope.Account(account.accountId), UniAppDataKeys.NotificationsEnabled)
                    } catch (error: CancellationException) { throw error }
                    catch (_: Exception) { false }
                    notificationManager.bindOwner(account.accountId, consent)
                }
            }
            val unregisterPushTokenProvider =
                registerPushNotificationsTokenProvider(notificationManager::backendToken)
            UniAppRuntime(
                sessionController = sessionController,
                defaultGateway = PortalUniGateway(sessionController, accountStore),
                localDataStore = localDataStore,
                accountStore = accountStore,
                updateController =
                    UniUpdateManager(
                        source = SdkUniUpdateManifestLoader(backend),
                        installedBuild = updateEnvironment.installedBuild,
                        launcher = updateEnvironment.launcher,
                    ),
                notificationManager = notificationManager,
                backend = backend,
                libraryService = libraryService,
                unregisterPushTokenProvider = unregisterPushTokenProvider,
            )
        }
    DisposableEffect(runtime) {
        onDispose(runtime::close)
    }
    return runtime
}
