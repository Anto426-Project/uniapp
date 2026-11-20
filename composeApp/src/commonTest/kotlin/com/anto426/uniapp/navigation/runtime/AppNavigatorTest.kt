package com.anto426.uniapp.navigation.runtime

import androidx.compose.runtime.mutableStateOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.anto426.uniapp.account.model.UniAccountSummary
import com.anto426.uniapp.navigation.model.AppRoute
import com.anto426.uniapp.navigation.model.appTopLevelRoutes
import com.anto426.uniapp.navigation.policy.AppRouteGuard
import com.anto426.uniapp.session.model.AppSessionState
import com.anto426.unisdk.model.CareerType
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

        fixture.navigator.navigate(AppRoute.Exams())

        assertEquals(listOf(AppRoute.Home, AppRoute.Exams()), fixture.stack(AppRoute.Home))
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
    fun lockedAccountCanRenderBootstrapAndReleaseNotes() {
        val locked =
            AppSessionState.UnlockRequired(authenticatedSession.account)
        val guard = AppRouteGuard()

        assertEquals(AppRoute.Bootstrap, guard.resolve(AppRoute.Home, locked))
        assertEquals(AppRoute.Bootstrap, guard.resolve(AppRoute.Login, locked))
        assertEquals(AppRoute.Changelog, guard.resolve(AppRoute.Changelog, locked))
    }

    @Test
    fun releaseNotesAreIndependentOfTheMandatoryUpdateAndSessionStacks() {
        val locked = AppSessionState.UnlockRequired(authenticatedSession.account)
        val fixture = Fixture(activeRoot = AppRoute.Bootstrap, sessionState = locked)
        val navigator = fixture.navigator

        assertEquals(AppRoute.Updates, navigator.visibleRoute(isMandatoryUpdate = true))
        assertEquals(NavigationResult.Navigated(AppRoute.Changelog), navigator.navigate(AppRoute.Changelog))
        assertEquals(AppRoute.Changelog, navigator.visibleRoute(isMandatoryUpdate = true))
        assertEquals(AppRoute.Changelog, navigator.visibleRoute(isMandatoryUpdate = false))
        assertEquals(listOf<NavKey>(AppRoute.Bootstrap), fixture.authStack.toList())

        assertEquals(true, navigator.goBack())
        assertEquals(AppRoute.Updates, navigator.visibleRoute(isMandatoryUpdate = true))
        assertEquals(AppRoute.Bootstrap, navigator.visibleRoute(isMandatoryUpdate = false))
    }

    @Test
    fun lockingTheSessionKeepsReleaseNotesOpenAndBackReturnsToUnlock() {
        val fixture = Fixture(activeRoot = AppRoute.Home)
        val navigator = fixture.navigator
        navigator.reconcile()
        navigator.navigate(AppRoute.Taxes)
        navigator.navigate(AppRoute.Changelog)

        fixture.currentSession = AppSessionState.UnlockRequired(authenticatedSession.account)
        assertEquals(AppRoute.Changelog, navigator.reconcile())
        assertEquals(AppRoute.Bootstrap, navigator.sessionRoute)
        assertEquals(false, navigator.canRender(AppRoute.Taxes))
        assertEquals(null, navigator.currentTopLevelRoute)
        assertEquals(true, navigator.canNavigateUp)

        assertEquals(true, navigator.goBack())
        assertEquals(AppRoute.Bootstrap, navigator.currentRoute)
    }

    @Test
    fun restoredReleaseNotesMoveOutOfTheLoginStack() {
        val fixture = Fixture(activeRoot = AppRoute.Login, sessionState = AppSessionState.SignedOut())
        fixture.authStack.clear()
        fixture.authStack.addAll(listOf(AppRoute.Login, AppRoute.Changelog))

        assertEquals(AppRoute.Changelog, fixture.navigator.reconcile())
        assertEquals(listOf<NavKey>(AppRoute.Login), fixture.authStack.toList())
        assertEquals(true, fixture.navigator.goBack())
        assertEquals(AppRoute.Login, fixture.navigator.currentRoute)
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
    fun libraryDestinationsRedirectToServicesForStudentsAndProfessors() {
        val guard = AppRouteGuard()
        val professor = AppSessionState.Authenticated(
            authenticatedSession.account.copy(activeProfileType = CareerType.PROFESSOR),
        )

        for (route in listOf(AppRoute.Library(), AppRoute.LibraryBookDetail("book"), AppRoute.LibraryBooking)) {
            assertEquals(AppRoute.Services, guard.resolve(route, authenticatedSession))
            assertEquals(AppRoute.Services, guard.resolve(route, professor))
            assertEquals(AppRoute.Login, guard.resolve(route, AppSessionState.SignedOut()))
        }
    }

    @Test
    fun restoredLibraryDestinationReturnsToTheServicesRoot() {
        val fixture = Fixture(activeRoot = AppRoute.Services)
        fixture.navigator.reconcile()
        fixture.navigator.selectTopLevel(AppRoute.Services)
        fixture.restore(AppRoute.Services, AppRoute.Library(), AppRoute.LibraryBookDetail("book"))

        assertEquals(false, fixture.navigator.canRender(fixture.navigator.currentRoute))
        assertEquals(AppRoute.Services, fixture.navigator.reconcile())
        assertEquals(AppRoute.Services, fixture.navigator.currentRoute)
        assertEquals(listOf(AppRoute.Services), fixture.stack(AppRoute.Services))
    }

    @Test
    fun professorCannotOpenStudentOnlyDestinations() {
        val professorSession =
            AppSessionState.Authenticated(
                authenticatedSession.account.copy(activeProfileType = CareerType.PROFESSOR),
            )
        val guard = AppRouteGuard()

        assertEquals(AppRoute.Didactics, guard.resolve(AppRoute.Grades, professorSession))
        assertEquals(AppRoute.Exams(), guard.resolve(AppRoute.Exams(), professorSession))
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
                changelogOpen = mutableStateOf(false),
                sessionState = { currentSession },
                routeGuard = AppRouteGuard(),
            )

        fun stack(route: AppRoute): List<NavKey> = stacks.getValue(route).toList()

        fun restore(root: AppRoute, vararg routes: AppRoute) {
            stacks.getValue(root).addAll(routes.toList())
        }
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
