package com.anto426.uniapp.navigation.runtime

import androidx.compose.runtime.mutableStateOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.anto426.uniapp.account.model.UniAccountSummary
import com.anto426.uniapp.navigation.model.AppRoute
import com.anto426.uniapp.navigation.model.appTopLevelRoutes
import com.anto426.uniapp.navigation.policy.AppRouteGuard
import com.anto426.uniapp.session.model.AppSessionState
import com.anto426.unisdk.backend.model.BackendCareerType
import kotlin.test.Test
import kotlin.test.assertEquals

class AppNavigatorTest {
    @Test
    fun destinationStaysInTheStackThatOpenedIt() {
        val fixture = Fixture(activeRoot = AppRoute.Didactics)

        fixture.navigator.navigate(AppRoute.Taxes)

        assertEquals(AppRoute.Didactics, fixture.activeRoot.value)
        assertEquals(listOf(AppRoute.Services), fixture.stack(AppRoute.Services))
        assertEquals(listOf(AppRoute.Didactics, AppRoute.Taxes), fixture.stack(AppRoute.Didactics))
        assertEquals(AppRoute.Didactics, fixture.navigator.currentTopLevelRoute)
    }

    @Test
    fun examOpenedFromHomeGoesDirectlyBackToHome() {
        val fixture = Fixture(activeRoot = AppRoute.Home)

        fixture.navigator.navigate(AppRoute.Exams)

        assertEquals(listOf(AppRoute.Home, AppRoute.Exams), fixture.stack(AppRoute.Home))
        assertEquals(AppRoute.Home, fixture.navigator.currentTopLevelRoute)
        assertEquals(true, fixture.navigator.goBack())
        assertEquals(AppRoute.Home, fixture.navigator.currentRoute)
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

    @Test
    fun lockedAccountCanOnlyRenderBootstrap() {
        val locked =
            AppSessionState.UnlockRequired(authenticatedSession.account)
        val guard = AppRouteGuard()

        assertEquals(AppRoute.Bootstrap, guard.resolve(AppRoute.Home, locked))
        assertEquals(AppRoute.Bootstrap, guard.resolve(AppRoute.Login, locked))
    }

    @Test
    fun initializationCannotRenderRestoredLoginOrPrivateRoute() {
        val guard = AppRouteGuard()

        assertEquals(false, guard.canRender(AppRoute.Login, AppSessionState.Initializing))
        assertEquals(false, guard.canRender(AppRoute.Home, AppSessionState.Initializing))
        assertEquals(AppRoute.Bootstrap, guard.resolve(AppRoute.Login, AppSessionState.Initializing))
    }

    @Test
    fun switchingCareerHidesEveryPrivateRouteUntilTheNewSessionIsVerified() {
        val guard = AppRouteGuard()

        assertEquals(AppRoute.Bootstrap, guard.resolve(AppRoute.Home, AppSessionState.Switching))
        assertEquals(AppRoute.Bootstrap, guard.resolve(AppRoute.Grades, AppSessionState.Switching))
        assertEquals(false, guard.canRender(AppRoute.Home, AppSessionState.Switching))
    }

    @Test
    fun restoredPrivateRouteIsRejectedUntilAccountIsAuthenticated() {
        val fixture = Fixture(activeRoot = AppRoute.Home, sessionState = AppSessionState.SignedOut())

        assertEquals(false, fixture.navigator.canRender(AppRoute.Home))
        assertEquals(AppRoute.Login, fixture.navigator.reconcile())
        assertEquals(AppRoute.Login, fixture.navigator.currentRoute)

        fixture.currentSession = authenticatedSession
        assertEquals(false, fixture.navigator.canRender(AppRoute.Login))
        assertEquals(AppRoute.Home, fixture.navigator.reconcile())
        assertEquals(AppRoute.Home, fixture.navigator.currentRoute)
    }

    @Test
    fun professorCannotOpenStudentOnlyDestinations() {
        val professorSession =
            AppSessionState.Authenticated(
                authenticatedSession.account.copy(activeProfileType = BackendCareerType.PROFESSOR),
            )
        val guard = AppRouteGuard()

        assertEquals(AppRoute.Didactics, guard.resolve(AppRoute.Grades, professorSession))
        assertEquals(AppRoute.Exams, guard.resolve(AppRoute.Exams, professorSession))
        assertEquals(AppRoute.Badge, guard.resolve(AppRoute.Badge, professorSession))
        assertEquals(AppRoute.Teachings, guard.resolve(AppRoute.Teachings, professorSession))
        assertEquals(AppRoute.Home, guard.resolve(AppRoute.Home, professorSession))
        assertEquals(AppRoute.Services, guard.resolve(AppRoute.Services, professorSession))
    }

    @Test
    fun changingAccountClearsSavedNewsAndOtherAuthenticatedStacks() {
        val fixture = Fixture(activeRoot = AppRoute.Home)
        fixture.navigator.reconcile()
        fixture.navigator.navigate(AppRoute.NewsDetail("news-1", "account", null))
        fixture.navigator.selectTopLevel(AppRoute.Services)
        fixture.navigator.navigate(AppRoute.Transport)

        fixture.currentSession = AppSessionState.Authenticated(
            authenticatedSession.account.copy(accountId = "another-account"),
        )
        fixture.navigator.reconcile()

        assertEquals(AppRoute.Home, fixture.navigator.currentRoute)
        assertEquals(listOf(AppRoute.Home), fixture.stack(AppRoute.Home))
        assertEquals(listOf(AppRoute.Services), fixture.stack(AppRoute.Services))
    }

    @Test
    fun newsDetailFromAnotherAccountIsRejectedBeforeRendering() {
        val guard = AppRouteGuard()
        assertEquals(
            AppRoute.Home,
            guard.resolve(AppRoute.NewsDetail("news-1", "another-account", null), authenticatedSession),
        )
    }

    private class Fixture(
        activeRoot: AppRoute,
        sessionState: AppSessionState = authenticatedSession,
    ) {
        val authStack = NavBackStack<NavKey>(AppRoute.Bootstrap)
        val stacks =
            appTopLevelRoutes.associateWith { route -> NavBackStack<NavKey>(route) }
        val activeRoot = mutableStateOf(activeRoot)
        var currentSession = sessionState
        private val authenticatedOwner = mutableStateOf<String?>(null)
        val navigator =
            AppNavigator(
                authBackStack = authStack,
                topLevelBackStacks = stacks,
                activeRootState = this.activeRoot,
                authenticatedOwnerState = authenticatedOwner,
                sessionState = { currentSession },
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
