package com.anto426.uniapp.navigation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.anto426.liquidmonet.components.buttons.LiquidButton
import com.anto426.liquidmonet.components.buttons.LiquidButtonVariant
import com.anto426.liquidmonet.components.buttons.LiquidFloatingActionButton
import com.anto426.liquidmonet.components.feedback.LiquidDialog
import com.anto426.liquidmonet.components.navigation.LiquidNavigationBar
import com.anto426.liquidmonet.components.navigation.LiquidNavigationItem
import com.anto426.liquidmonet.components.navigation.LiquidTopBar
import com.anto426.liquidmonet.components.navigation.LiquidTopBarAction
import com.anto426.liquidmonet.glass.LiquidBackground
import com.anto426.liquidmonet.glass.LiquidBackgroundEffect
import com.anto426.liquidmonet.glass.LiquidGlassScene
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.monet.LiquidMonetPresets
import com.anto426.uniapp.account.model.UniAccountSummary
import com.anto426.uniapp.account.presentation.AccountSwitcherViewModel
import com.anto426.uniapp.app.runtime.UniAppRuntime
import com.anto426.uniapp.feedback.runtime.AppToastHost
import com.anto426.uniapp.feedback.runtime.AppToastManager
import com.anto426.uniapp.feedback.runtime.info
import com.anto426.uniapp.navigation.model.AppRoute
import com.anto426.uniapp.navigation.model.appTopLevelRoutes
import com.anto426.uniapp.navigation.model.isTopLevel
import com.anto426.uniapp.navigation.model.topLevelParent
import com.anto426.uniapp.navigation.presentation.AppShellViewModel
import com.anto426.uniapp.navigation.runtime.AppNavigator
import com.anto426.uniapp.navigation.runtime.rememberAppNavigationState
import com.anto426.uniapp.session.model.AppSessionState
import com.anto426.uniapp.session.presentation.AppSessionViewModel
import com.anto426.uniapp.security.biometric.BiometricAuthenticator
import com.anto426.uniapp.settings.presentation.DeviceSessionsActionUiState
import com.anto426.uniapp.settings.presentation.DeviceSessionsActionViewModel
import com.anto426.uniapp.ui.components.layout.LocalNavigationBarVisible
import com.anto426.uniapp.ui.components.layout.LocalUniScreenPadding
import com.anto426.uniapp.ui.updates.UpdatesScreen
import com.anto426.uniapp.updates.presentation.AppUpdateViewModel
import com.anto426.unisdk.backend.model.BackendCareerType
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.Res
import uniapp.composeapp.generated.resources.ui_home_switch_career
import uniapp.composeapp.generated.resources.ui_professor_role
import uniapp.composeapp.generated.resources.ui_student_role

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AppNavigationHost(
    runtime: UniAppRuntime,
    sessionViewModel: AppSessionViewModel,
    sessionState: AppSessionState,
    biometricAuthenticator: BiometricAuthenticator,
) {
    val navigationState = rememberAppNavigationState(sessionState)
    val navigator = navigationState.navigator
    val toastManager = remember { AppToastManager() }
    val topBarAccountSwitcherViewModel =
        viewModel(key = "top-bar-profile-switcher") {
            AccountSwitcherViewModel(runtime.sessionController, toastManager)
        }
    val unlockUiState by sessionViewModel.unlockUiState.collectAsStateWithLifecycle()
    val shellViewModel = viewModel { AppShellViewModel() }
    val shellUiState by shellViewModel.uiState.collectAsStateWithLifecycle()
    val authenticatedAccount = (sessionState as? AppSessionState.Authenticated)?.account
    val accountId = authenticatedAccount?.accountId.orEmpty()
    val profileId = authenticatedAccount?.activeProfileId
    val accountDataSource =
        remember(runtime, accountId, profileId) {
            accountId.takeIf(String::isNotBlank)?.let { runtime.dataSourceFor(it, profileId) }
                ?: runtime.dataSource
        }
    val deviceSessionsViewModel =
        viewModel(key = "device-sessions-actions|$accountId") {
            DeviceSessionsActionViewModel(accountDataSource, toastManager)
        }
    val deviceSessionsUiState by deviceSessionsViewModel.uiState.collectAsStateWithLifecycle()
    val updateViewModel = viewModel { AppUpdateViewModel(runtime.updateController, toastManager) }
    val updateUiState by updateViewModel.uiState.collectAsStateWithLifecycle()
    val navigationRoute = navigator.currentRoute
    val isMandatoryUpdate = updateUiState.isMandatory
    val route = if (isMandatoryUpdate) AppRoute.Updates else navigationRoute
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val isAuthenticated = sessionState is AppSessionState.Authenticated
    val isProfessor = authenticatedAccount?.isProfessor == true
    val showTopBar = route != AppRoute.Bootstrap && route != AppRoute.Login
    val showBottomBar =
        isAuthenticated && !isMandatoryUpdate && route != AppRoute.Bootstrap && route != AppRoute.Login
    var topBarHeight by remember { mutableStateOf(152.dp) }
    val topLevelRoutes = appTopLevelRoutes
    val topLevelNavigationItems = topLevelRoutes.map { item ->
        val presentation = item.presentation(isProfessor)
        LiquidNavigationItem(label = presentation.title, icon = presentation.icon)
    }

    LaunchedEffect(sessionState) { navigator.reconcile() }
    LaunchedEffect((sessionState as? AppSessionState.UnlockRequired)?.account?.accountId) {
        if (sessionState is AppSessionState.UnlockRequired) {
            sessionViewModel.requestUnlock(biometricAuthenticator)
        }
    }
    LaunchedEffect(route) {
        shellViewModel.routeChanged(route)
        scrollBehavior.state.heightOffset = 0f
        scrollBehavior.state.contentOffset = 0f
    }

    val navigationBarScrollConnection =
        remember(shellViewModel) {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    when {
                        available.y < -2f -> shellViewModel.updateNavigationBarVisibility(false)
                        available.y > 2f -> shellViewModel.updateNavigationBarVisibility(true)
                    }
                    return Offset.Zero
                }
            }
        }

    LiquidGlassScene(
        modifier = Modifier.fillMaxSize(),
        scrollBehavior = scrollBehavior,
        background = {
            LiquidBackground(
                effect = LiquidBackgroundEffect.Aurora,
                monetSeed = LiquidMonetPresets.Sapphire,
                intensity = 1f,
                speedFactor = .28f,
            )
        },
        topBar = { backdrop ->
            if (showTopBar) {
                val presentation = route.presentation(isProfessor)
                LiquidTopBar(
                    title = presentation.title,
                    subtitle = presentation.subtitle,
                    backdropState = backdrop,
                    scrollBehavior = scrollBehavior,
                    showNavigationIcon = navigator.canNavigateUp,
                    onNavigationClick = navigator::goBack,
                    isSearchActive = shellUiState.isSearchActive,
                    searchQuery = shellUiState.searchQuery,
                    onQueryChange = shellViewModel::updateSearchQuery,
                    onSearchActiveChange = shellViewModel::setSearchActive,
                    searchPlaceholder = "Cerca...",
                    onHeightChanged = { topBarHeight = it },
                    actionItems = topBarActions(
                        route = route,
                        isProfessor = isProfessor,
                        navigator = navigator,
                        shellViewModel = shellViewModel,
                        onRefreshUpdate = updateViewModel::refresh,
                        onRequestDisconnectAll = deviceSessionsViewModel::requestDisconnectAll,
                        account = authenticatedAccount,
                        onSelectProfile = topBarAccountSwitcherViewModel::selectProfile,
                    ),
                )
            }
        },
        bottomBar = { backdrop ->
            if (showBottomBar) {
                Box(Modifier.fillMaxWidth().align(Alignment.BottomCenter)) {
                    val selectedRoot = navigator.currentTopLevelRoute ?: route.topLevelParent()
                    val selectedIndex = topLevelRoutes.indexOf(selectedRoot).coerceAtLeast(0)
                    LiquidNavigationBar(
                        selectedIndex = selectedIndex,
                        onItemSelected = { navigator.selectTopLevel(topLevelRoutes[it]) },
                        items = topLevelNavigationItems,
                        visible = shellUiState.isNavigationBarVisible,
                        backdropState = backdrop,
                    )
                }
            }
        },
        overlay = { backdrop ->
            when (route) {
                AppRoute.Transport ->
                    LiquidFloatingActionButton(
                        onClick = { navigator.navigate(AppRoute.TransportBooking) },
                        modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 112.dp),
                        visible = shellUiState.isNavigationBarVisible,
                        backdropState = backdrop,
                    ) {
                        Icon(LiquidIcons.Add, contentDescription = null, tint = Color.White)
                    }

                else -> Unit
            }
            AppToastHost(
                manager = toastManager,
                modifier = Modifier.fillMaxSize(),
                backdropState = backdrop,
            )
        },
    ) { backdrop ->
        Scaffold(
            modifier =
                Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .nestedScroll(navigationBarScrollConnection),
            containerColor = Color.Transparent,
            topBar = {
                if (showTopBar) {
                    Spacer(Modifier.height(topBarHeight))
                }
            },
        ) { innerPadding ->
            val screenPadding =
                PaddingValues(
                    top = innerPadding.calculateTopPadding(),
                    bottom = if (showBottomBar) 110.dp else 24.dp,
                )
            CompositionLocalProvider(
                LocalUniScreenPadding provides screenPadding,
                LocalNavigationBarVisible provides shellUiState.isNavigationBarVisible,
            ) {
                if (isMandatoryUpdate) {
                    UpdatesScreen(
                        backdropState = backdrop,
                        uiState = updateUiState,
                        onRetry = updateViewModel::refresh,
                        onOpenUpdate = updateViewModel::openUpdate,
                        onOpenChangelog = {},
                    )
                } else {
                    val entries =
                        navigationState.rememberDecoratedEntries { key ->
                            val entryRoute = key as AppRoute
                            NavEntry(key = key) {
                                Box(Modifier.fillMaxSize().graphicsLayer(clip = false)) {
                                    if (navigator.canRender(entryRoute)) {
                                        AppRouteContent(
                                            route = entryRoute,
                                            backdropState = backdrop,
                                            navigator = navigator,
                                            sessionController = runtime.sessionController,
                                            dataSource = accountDataSource,
                                            accountId = accountId,
                                            searchQuery = shellUiState.searchQuery,
                                            isSearchActive = shellUiState.isSearchActive,
                                            updateUiState = updateUiState,
                                            notificationController = runtime.notificationManager,
                                            toastSink = toastManager,
                                            biometricAuthenticator = biometricAuthenticator,
                                            sessionState = sessionState,
                                            unlockUiState = unlockUiState,
                                            onRequestUnlock = { sessionViewModel.requestUnlock(biometricAuthenticator) },
                                            onCancelUnlock = sessionViewModel::cancelUnlock,
                                            devicesRefreshRevision = deviceSessionsUiState.refreshRevision,
                                            onRetryUpdate = updateViewModel::refresh,
                                            onOpenUpdate = updateViewModel::openUpdate,
                                            onSignOut = {
                                                toastManager.info("Disconnessione in corso…")
                                                sessionViewModel.signOut()
                                            },
                                        )
                                    } else {
                                        LaunchedEffect(entryRoute, sessionState) { navigator.reconcile() }
                                    }
                                }
                            }
                        }
                    NavDisplay(
                        modifier = Modifier.fillMaxSize(),
                        entries = entries,
                        onBack = { navigator.goBack() },
                        transitionSpec = {
                            AppScreenTransitions.forward(
                                from = initialState.key as? AppRoute,
                                to = targetState.key as? AppRoute,
                            )
                        },
                        popTransitionSpec = {
                            AppScreenTransitions.backward(
                                from = initialState.key as? AppRoute,
                                to = targetState.key as? AppRoute,
                            )
                        },
                        predictivePopTransitionSpec = {
                            AppScreenTransitions.predictiveBack()
                        },
                    )
                }
            }
        }

        if (deviceSessionsUiState.isConfirmationVisible) {
            DisconnectAllDialog(
                backdrop = backdrop,
                state = deviceSessionsUiState,
                onDismiss = deviceSessionsViewModel::dismissConfirmation,
                onConfirm = deviceSessionsViewModel::confirmDisconnectAll,
            )
        }
    }
}

/**
 * Decorates every retained stack independently, as required by Navigation 3 for multiple stacks.
 * Only the active flow (plus Home for exit-through-Home) is exposed to [NavDisplay].
 */
@Composable
private fun com.anto426.uniapp.navigation.runtime.AppNavigationState.rememberDecoratedEntries(
    entryProvider: (NavKey) -> NavEntry<NavKey>,
): List<NavEntry<NavKey>> {
    val decoratedEntries =
        allBackStacks.associateWith { stack ->
            rememberDecoratedNavEntries(
                backStack = stack,
                entryDecorators =
                    listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator(),
                    ),
                entryProvider = entryProvider,
            )
        }

    return stacksInUse.flatMap { stack -> decoratedEntries.getValue(stack) }
}

@Composable
private fun DisconnectAllDialog(
    backdrop: com.kyant.backdrop.Backdrop,
    state: DeviceSessionsActionUiState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    LiquidDialog(
        onDismissRequest = onDismiss,
        title = "Disconnetti tutti",
        text = "Confermi la chiusura delle altre sessioni attive?",
        backdropState = backdrop,
        confirmButton = {
            LiquidButton(
                text = "Disconnetti",
                onClick = onConfirm,
                enabled = !state.isDisconnecting,
                variant = LiquidButtonVariant.Primary,
                backdropState = backdrop,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        dismissButton = {
            LiquidButton(
                text = "Annulla",
                onClick = onDismiss,
                enabled = !state.isDisconnecting,
                variant = LiquidButtonVariant.Text,
                backdropState = backdrop,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    )
}

@Composable
private fun topBarActions(
    route: AppRoute,
    isProfessor: Boolean,
    navigator: AppNavigator,
    shellViewModel: AppShellViewModel,
    onRefreshUpdate: () -> Unit,
    onRequestDisconnectAll: () -> Unit,
    account: UniAccountSummary?,
    onSelectProfile: (String) -> Unit,
): List<LiquidTopBarAction> =
    when (route) {
        AppRoute.Contacts,
        AppRoute.Teachings,
        AppRoute.Theses,
        AppRoute.Reports,
        ->
            listOf(
                LiquidTopBarAction(
                    icon = LiquidIcons.Search,
                    label = "Cerca",
                    onClick = { shellViewModel.setSearchActive(true) },
                ),
            )

        AppRoute.Updates ->
            listOf(
                LiquidTopBarAction(
                    icon = LiquidIcons.Refresh,
                    label = "Controlla aggiornamenti",
                    onClick = onRefreshUpdate,
                ),
            )

        AppRoute.Exams ->
            if (isProfessor) {
                emptyList()
            } else {
            listOf(
                LiquidTopBarAction(
                    icon = LiquidIcons.Time,
                    label = "Storico",
                    onClick = { navigator.navigate(AppRoute.ExamsHistory) },
                ),
            )
            }

        AppRoute.Devices ->
            listOf(
                LiquidTopBarAction(
                    icon = LiquidIcons.Close,
                    label = "Disconnetti tutti",
                    onClick = onRequestDisconnectAll,
                ),
            )

        AppRoute.Transport -> emptyList()

        AppRoute.Home ->
            account
                ?.profiles
                ?.distinctBy { it.profileId }
                ?.takeIf { it.size > 1 }
                ?.let { profiles ->
                    listOf(
                        LiquidTopBarAction(
                            icon = LiquidIcons.AccountCircle,
                            label = stringResource(Res.string.ui_home_switch_career),
                            subItems =
                                profiles.map { profile ->
                                    val role =
                                        stringResource(
                                            if (profile.type == BackendCareerType.PROFESSOR) {
                                                Res.string.ui_professor_role
                                            } else {
                                                Res.string.ui_student_role
                                            },
                                        )
                                    val profileName = profile.degreeName.ifBlank { profile.displayName }
                                    LiquidTopBarAction(
                                        icon = LiquidIcons.AccountCircle,
                                        label = "$role · $profileName",
                                        selected = profile.profileId == account.activeProfileId,
                                        onClick = { onSelectProfile(profile.profileId) },
                                    )
                                },
                        ),
                    )
                }
                .orEmpty()

        else -> emptyList()
    }
