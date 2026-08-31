package com.anto426.uniapp.navigation.runtime

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.Snapshot
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.anto426.uniapp.navigation.model.AppRoute
import com.anto426.uniapp.navigation.model.isTopLevel
import com.anto426.uniapp.navigation.model.topLevelParent
import com.anto426.uniapp.navigation.policy.AppRouteGuard
import com.anto426.uniapp.session.model.AppSessionState

sealed interface NavigationResult {
    data class Navigated(val route: AppRoute) : NavigationResult

    data class Redirected(
        val requested: AppRoute,
        val destination: AppRoute,
    ) : NavigationResult

    data object Ignored : NavigationResult
}

/** The only mutator of UniApp's authenticated, signed-out and top-level back stacks. */
class AppNavigator internal constructor(
    private val authBackStack: NavBackStack<NavKey>,
    private val topLevelBackStacks: Map<AppRoute, NavBackStack<NavKey>>,
    private val activeRootState: MutableState<AppRoute>,
    private val sessionState: () -> AppSessionState,
    private val routeGuard: AppRouteGuard,
) {
    private val activeRoot: AppRoute
        get() = activeRootState.value

    private val activeBackStack: NavBackStack<NavKey>
        get() =
            if (activeRoot.isTopLevel) {
                topLevelBackStacks.getValue(activeRoot)
            } else {
                authBackStack
            }

    val currentRoute: AppRoute
        get() = activeBackStack.lastOrNull() as? AppRoute ?: activeRoot

    /** System Back can also return a secondary top-level flow to Home. */
    val canGoBack: Boolean
        get() = activeBackStack.size > 1 || (activeRoot.isTopLevel && activeRoot != AppRoute.Home)

    /** Up is hierarchical and therefore is not shown on a top-level navigation-bar destination. */
    val canNavigateUp: Boolean
        get() = activeBackStack.size > 1

    fun canRender(route: AppRoute): Boolean = routeGuard.canRender(route, sessionState())

    fun navigate(requested: AppRoute): NavigationResult {
        val destination = routeGuard.resolve(requested, sessionState())
        if (destination != requested) {
            activate(destination, clearAuthenticatedState = destination == AppRoute.Login)
            return NavigationResult.Redirected(requested, destination)
        }
        if (currentRoute == destination) return NavigationResult.Ignored
        if (destination.isTopLevel) return selectTopLevel(destination)

        if (activeRoot.isTopLevel) {
            // Authenticated destinations always belong to their declared top-level flow. Keeping
            // them in the previously selected stack makes the navbar highlight one tab while Back
            // mutates another one.
            activate(destination)
        } else {
            // Public legal pages opened from Login remain in the signed-out flow so Back returns
            // to Login instead of exposing an authenticated top-level stack.
            activeBackStack.add(destination)
        }
        return NavigationResult.Navigated(destination)
    }

    fun selectTopLevel(requested: AppRoute): NavigationResult {
        require(requested.isTopLevel) { "Only top-level routes can be selected by the navigation bar" }
        val destination = routeGuard.resolve(requested, sessionState())
        if (destination == activeRoot) {
            if (activeBackStack.size == 1) return NavigationResult.Ignored
            Snapshot.withMutableSnapshot { resetStack(activeBackStack, destination) }
            return NavigationResult.Navigated(destination)
        }

        activate(destination)
        return if (destination == requested) {
            NavigationResult.Navigated(destination)
        } else {
            NavigationResult.Redirected(requested, destination)
        }
    }

    fun goBack(): Boolean {
        val stack = activeBackStack
        if (stack.size > 1) {
            stack.removeAt(stack.lastIndex)
            reconcile()
            return true
        }
        if (activeRoot.isTopLevel && activeRoot != AppRoute.Home) {
            activeRootState.value = AppRoute.Home
            return true
        }
        return false
    }

    /** Revalidates restored destinations and reacts atomically to authentication changes. */
    fun reconcile(): AppRoute {
        val current = currentRoute
        val allowed = routeGuard.resolve(current, sessionState())
        if (allowed != current) {
            activate(
                route = allowed,
                clearAuthenticatedState = allowed == AppRoute.Login || allowed == AppRoute.Home,
            )
        }
        return allowed
    }

    private fun activate(
        route: AppRoute,
        clearAuthenticatedState: Boolean = false,
    ) {
        Snapshot.withMutableSnapshot {
            when {
                route.isTopLevel -> {
                    if (clearAuthenticatedState) resetAuthenticatedStacks()
                    activeRootState.value = route
                }

                route == AppRoute.Bootstrap || route == AppRoute.Login -> {
                    if (clearAuthenticatedState) resetAuthenticatedStacks()
                    resetStack(authBackStack, route)
                    activeRootState.value = route
                }

                else -> {
                    val parent = route.topLevelParent()
                    val destinationStack = topLevelBackStacks.getValue(parent)
                    if (destinationStack.lastOrNull() != route) destinationStack.add(route)
                    activeRootState.value = parent
                }
            }
        }
    }

    private fun resetAuthenticatedStacks() {
        topLevelBackStacks.forEach { (root, stack) -> resetStack(stack, root) }
    }

    private fun resetStack(
        stack: NavBackStack<NavKey>,
        root: AppRoute,
    ) {
        stack.clear()
        stack.add(root)
    }
}
