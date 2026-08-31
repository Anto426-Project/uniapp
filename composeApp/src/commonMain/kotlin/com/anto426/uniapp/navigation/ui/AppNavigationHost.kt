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
import androidx.navigationevent.NavigationEvent.Companion.EDGE_RIGHT
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
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.motion.liquidFadeThrough
import com.anto426.liquidmonet.motion.liquidPredictiveBackHorizontal
import com.anto426.liquidmonet.motion.liquidSharedAxisHorizontal
import com.anto426.liquidmonet.theme.monet.LiquidMonetPresets
import com.anto426.uniapp.app.runtime.UniAppRuntime
import com.anto426.uniapp.navigation.model.AppRoute
import com.anto426.uniapp.navigation.model.appTopLevelRoutes
import com.anto426.uniapp.navigation.model.isTopLevel
import com.anto426.uniapp.navigation.model.topLevelParent
import com.anto426.uniapp.navigation.presentation.AppShellViewModel
import com.anto426.uniapp.navigation.runtime.AppNavigator
import com.anto426.uniapp.navigation.runtime.rememberAppNavigationState
import com.anto426.uniapp.session.model.AppSessionState
import com.anto426.uniapp.session.presentation.AppSessionViewModel
import com.anto426.uniapp.ui.components.layout.LocalUniScreenPadding
import com.anto426.uniapp.ui.updates.UpdatesScreen
import com.anto426.uniapp.updates.presentation.AppUpdateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AppNavigationHost(
    runtime: UniAppRuntime,
    sessionViewModel: AppSessionViewModel,
    sessionState: AppSessionState,
) {
    val navigationState = rememberAppNavigationState(sessionState)
    val navigator = navigationState.navigator
    val shellViewModel = viewModel { AppShellViewModel() }
    val shellUiState by shellViewModel.uiState.collectAsStateWithLifecycle()
    val updateViewModel = viewModel { AppUpdateViewModel(runtime.updateController) }
    val updateUiState by updateViewModel.uiState.collectAsStateWithLifecycle()
    val navigationRoute = navigator.currentRoute
    val isMandatoryUpdate = updateUiState.isMandatory
    val route = if (isMandatoryUpdate) AppRoute.Updates else navigationRoute
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val liquidPerformance = LocalLiquidGlassPerformance.current
    val isAuthenticated = sessionState is AppSessionState.Authenticated
    val showTopBar = route != AppRoute.Bootstrap && route != AppRoute.Login
    val showBottomBar =
        isAuthenticated && !isMandatoryUpdate && route != AppRoute.Bootstrap && route != AppRoute.Login
    var topBarHeight by remember { mutableStateOf(152.dp) }
    val topLevelRoutes = appTopLevelRoutes
    val topLevelNavigationItems =
        remember {
            topLevelRoutes.map { item ->
                val presentation = item.presentation()
                LiquidNavigationItem(label = presentation.title, icon = presentation.icon)
            }
        }

    LaunchedEffect(sessionState) { navigator.reconcile() }
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
                val presentation = route.presentation()
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
                    actionItems = topBarActions(route, navigator, shellViewModel, updateViewModel::refresh),
                )
            }
        },
        bottomBar = { backdrop ->
            if (showBottomBar) {
                Box(Modifier.fillMaxWidth().align(Alignment.BottomCenter)) {
                    val selectedIndex = topLevelRoutes.indexOf(route.topLevelParent()).coerceAtLeast(0)
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

                AppRoute.TransportBooking ->
                    LiquidFloatingActionButton(
                        onClick = { navigator.goBack() },
                        modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 112.dp),
                        visible = shellUiState.isNavigationBarVisible,
                        backdropState = backdrop,
                    ) {
                        Icon(LiquidIcons.Check, contentDescription = null, tint = Color.White)
                    }

                else -> Unit
            }
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
            CompositionLocalProvider(LocalUniScreenPadding provides screenPadding) {
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
                            val transitionMetadata =
                                if (entryRoute.isTopLevel) {
                                    NavDisplay.transitionSpec {
                                        liquidFadeThrough(liquidPerformance)
                                    }
                                } else {
                                    emptyMap()
                                }
                            NavEntry(key = key, metadata = transitionMetadata) {
                                Box(Modifier.fillMaxSize().graphicsLayer(clip = false)) {
                                    if (navigator.canRender(entryRoute)) {
                                        AppRouteContent(
                                            route = entryRoute,
                                            backdropState = backdrop,
                                            navigator = navigator,
                                            sessionController = runtime.sessionController,
                                            searchQuery = shellUiState.searchQuery,
                                            isSearchActive = shellUiState.isSearchActive,
                                            updateUiState = updateUiState,
                                            onRetryUpdate = updateViewModel::refresh,
                                            onOpenUpdate = updateViewModel::openUpdate,
                                            onSignOut = sessionViewModel::signOut,
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
                            liquidSharedAxisHorizontal(
                                forward = true,
                                performance = liquidPerformance,
                            )
                        },
                        popTransitionSpec = {
                            liquidSharedAxisHorizontal(
                                forward = false,
                                performance = liquidPerformance,
                            )
                        },
                        predictivePopTransitionSpec = { swipeEdge ->
                            liquidPredictiveBackHorizontal(
                                fromRightEdge = swipeEdge == EDGE_RIGHT,
                                performance = liquidPerformance,
                            )
                        },
                    )
                }
            }
        }

        if (shellUiState.isDisconnectAllDialogVisible) {
            DisconnectAllDialog(backdrop, shellViewModel)
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
    shellViewModel: AppShellViewModel,
) {
    LiquidDialog(
        onDismissRequest = shellViewModel::dismissDisconnectAllDialog,
        title = "Disconnetti tutti",
        text = "Confermi la chiusura delle altre sessioni attive?",
        backdropState = backdrop,
        confirmButton = {
            LiquidButton(
                text = "Disconnetti",
                onClick = shellViewModel::dismissDisconnectAllDialog,
                variant = LiquidButtonVariant.Primary,
                backdropState = backdrop,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        dismissButton = {
            LiquidButton(
                text = "Annulla",
                onClick = shellViewModel::dismissDisconnectAllDialog,
                variant = LiquidButtonVariant.Text,
                backdropState = backdrop,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    )
}

private fun topBarActions(
    route: AppRoute,
    navigator: AppNavigator,
    shellViewModel: AppShellViewModel,
    onRefreshUpdate: () -> Unit,
): List<LiquidTopBarAction> =
    when (route) {
        AppRoute.Contacts ->
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
            listOf(
                LiquidTopBarAction(
                    icon = LiquidIcons.Time,
                    label = "Storico",
                    onClick = { navigator.navigate(AppRoute.ExamsHistory) },
                ),
            )

        AppRoute.Devices ->
            listOf(
                LiquidTopBarAction(
                    icon = LiquidIcons.Close,
                    label = "Disconnetti tutti",
                    onClick = shellViewModel::showDisconnectAllDialog,
                ),
            )

        AppRoute.Transport -> emptyList()

        AppRoute.Home,
        AppRoute.Services,
        AppRoute.Didactics,
        AppRoute.Settings,
        ->
            listOf(
                LiquidTopBarAction(
                    icon = LiquidIcons.Notifications,
                    label = "Avvisi",
                    onClick = { navigator.navigate(AppRoute.News) },
                ),
                LiquidTopBarAction(
                    icon = LiquidIcons.Settings,
                    label = "Impostazioni",
                    onClick = { navigator.selectTopLevel(AppRoute.Settings) },
                ),
            )

        else -> emptyList()
    }
