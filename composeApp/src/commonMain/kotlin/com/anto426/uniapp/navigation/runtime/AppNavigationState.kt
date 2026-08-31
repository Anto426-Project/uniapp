package com.anto426.uniapp.navigation.runtime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.savedstate.compose.serialization.serializers.MutableStateSerializer
import com.anto426.uniapp.navigation.model.AppRoute
import com.anto426.uniapp.navigation.model.appTopLevelRoutes
import com.anto426.uniapp.navigation.model.isTopLevel
import com.anto426.uniapp.navigation.policy.AppRouteGuard
import com.anto426.uniapp.session.model.AppSessionState

/**
 * Navigation state for the signed-out flow and the four persistent top-level flows.
 *
 * Each navigation-bar destination owns its back stack. [stacksInUse] exposes Home followed by the
 * selected flow, matching Navigation 3's official "exit through Home" multiple-stack pattern and
 * giving predictive Back a real previous destination to preview.
 */
@Stable
internal class AppNavigationState(
    val authBackStack: NavBackStack<NavKey>,
    val topLevelBackStacks: Map<AppRoute, NavBackStack<NavKey>>,
    internal val activeRootState: MutableState<AppRoute>,
    val navigator: AppNavigator,
) {
    val activeRoot: AppRoute
        get() = activeRootState.value

    val activeBackStack: NavBackStack<NavKey>
        get() =
            if (activeRoot.isTopLevel) {
                topLevelBackStacks.getValue(activeRoot)
            } else {
                authBackStack
            }

    val allBackStacks: List<NavBackStack<NavKey>>
        get() = listOf(authBackStack) + appTopLevelRoutes.map(topLevelBackStacks::getValue)

    val stacksInUse: List<NavBackStack<NavKey>>
        get() =
            when (activeRoot) {
                AppRoute.Bootstrap,
                AppRoute.Login,
                -> listOf(authBackStack)

                AppRoute.Home -> listOf(topLevelBackStacks.getValue(AppRoute.Home))

                else ->
                    listOf(
                        topLevelBackStacks.getValue(AppRoute.Home),
                        topLevelBackStacks.getValue(activeRoot),
                    )
            }
}

@Composable
internal fun rememberAppNavigationState(sessionState: AppSessionState): AppNavigationState {
    val currentSessionState = rememberUpdatedState(sessionState)
    val authBackStack =
        rememberNavBackStack(
            AppNavigationSavedStateConfiguration,
            AppRoute.Bootstrap,
        )
    val topLevelBackStacks =
        appTopLevelRoutes.associateWith { route ->
            rememberNavBackStack(
                AppNavigationSavedStateConfiguration,
                route,
            )
        }
    val activeRootState =
        rememberSerializable(
            serializer = MutableStateSerializer(AppRoute.serializer()),
        ) {
            mutableStateOf<AppRoute>(AppRoute.Bootstrap)
        }
    val routeGuard = remember { AppRouteGuard() }
    val navigator =
        remember(authBackStack, topLevelBackStacks, activeRootState, routeGuard) {
            AppNavigator(
                authBackStack = authBackStack,
                topLevelBackStacks = topLevelBackStacks,
                activeRootState = activeRootState,
                sessionState = { currentSessionState.value },
                routeGuard = routeGuard,
            )
        }

    return remember(authBackStack, topLevelBackStacks, activeRootState, navigator) {
        AppNavigationState(
            authBackStack = authBackStack,
            topLevelBackStacks = topLevelBackStacks,
            activeRootState = activeRootState,
            navigator = navigator,
        )
    }
}
