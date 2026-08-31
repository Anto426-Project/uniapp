package com.anto426.uniapp.navigation.runtime

import androidx.compose.runtime.mutableStateOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.anto426.uniapp.account.model.UniAccountSummary
import com.anto426.uniapp.navigation.model.AppRoute
import com.anto426.uniapp.navigation.model.appTopLevelRoutes
import com.anto426.uniapp.navigation.policy.AppRouteGuard
import com.anto426.uniapp.session.model.AppSessionState
import kotlin.test.Test
import kotlin.test.assertEquals

class AppNavigatorTest {
    @Test
    fun crossFeatureDestinationMovesToItsOwningStack() {
        val fixture = Fixture(activeRoot = AppRoute.Didactics)

        fixture.navigator.navigate(AppRoute.Taxes)

        assertEquals(AppRoute.Services, fixture.activeRoot.value)
        assertEquals(listOf(AppRoute.Services, AppRoute.Taxes), fixture.stack(AppRoute.Services))
        assertEquals(listOf(AppRoute.Didactics), fixture.stack(AppRoute.Didactics))
    }

    @Test
    fun reselectingCurrentTabReturnsToItsRoot() {
        val fixture = Fixture(activeRoot = AppRoute.Services)
        fixture.navigator.navigate(AppRoute.Transport)
        fixture.navigator.navigate(AppRoute.TransportBooking)

        fixture.navigator.selectTopLevel(AppRoute.Services)

        assertEquals(listOf(AppRoute.Services), fixture.stack(AppRoute.Services))
        assertEquals(AppRoute.Services, fixture.navigator.currentRoute)
    }

    @Test
    fun publicPageOpenedFromLoginStaysInSignedOutStack() {
        val fixture =
            Fixture(
                activeRoot = AppRoute.Login,
                sessionState = AppSessionState.SignedOut(),
            )
        fixture.authStack.clear()
        fixture.authStack.add(AppRoute.Login)

        fixture.navigator.navigate(AppRoute.Privacy)

        assertEquals(AppRoute.Login, fixture.activeRoot.value)
        assertEquals(listOf<NavKey>(AppRoute.Login, AppRoute.Privacy), fixture.authStack.toList())
    }

    private class Fixture(
        activeRoot: AppRoute,
        sessionState: AppSessionState = authenticatedSession,
    ) {
        val authStack = NavBackStack<NavKey>(AppRoute.Bootstrap)
        val stacks =
            appTopLevelRoutes.associateWith { route -> NavBackStack<NavKey>(route) }
        val activeRoot = mutableStateOf(activeRoot)
        val navigator =
            AppNavigator(
                authBackStack = authStack,
                topLevelBackStacks = stacks,
                activeRootState = this.activeRoot,
                sessionState = { sessionState },
                routeGuard = AppRouteGuard(),
            )

        fun stack(route: AppRoute): List<NavKey> = stacks.getValue(route).toList()
    }

    private companion object {
        val authenticatedSession =
            AppSessionState.Authenticated(
                UniAccountSummary(
                    accountId = "account",
                    serverUserId = "user",
                    displayName = "Student",
                    degreeName = "Degree",
                    matricola = null,
                    email = null,
                    photoUrl = null,
                    isGuest = false,
                ),
            )
    }
}
