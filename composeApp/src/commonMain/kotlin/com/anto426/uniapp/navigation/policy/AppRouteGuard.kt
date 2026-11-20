package com.anto426.uniapp.navigation.policy

import com.anto426.uniapp.library.LibraryAccessEnabled
import com.anto426.uniapp.navigation.model.AppRoute
import com.anto426.uniapp.navigation.model.AppRouteRequirement
import com.anto426.uniapp.navigation.model.requirement
import com.anto426.uniapp.session.model.AppSessionState

/** Pure access policy shared by taps, restored stacks and future deep-link handling. */
class AppRouteGuard {
    fun resolve(
        requested: AppRoute,
        sessionState: AppSessionState,
    ): AppRoute {
        // Release notes are application metadata and can be opened during session restoration or unlock.
        if (requested == AppRoute.Changelog) return requested
        return when (sessionState) {
            AppSessionState.Initializing,
            AppSessionState.Switching,
            is AppSessionState.UnlockRequired,
            -> AppRoute.Bootstrap

            is AppSessionState.Authenticated ->
                when {
                    !LibraryAccessEnabled && requested.isLibraryRoute() -> AppRoute.Services
                    requested is AppRoute.NewsDetail &&
                        (requested.accountId != sessionState.account.accountId ||
                            requested.profileId != sessionState.account.activeProfileId) -> AppRoute.Home
                    sessionState.account.isProfessor && requested.isStudentOnly() -> AppRoute.Didactics
                    !sessionState.account.isProfessor && requested.isProfessorOnly() -> AppRoute.Didactics
                    else -> when (requested.requirement) {
                    AppRouteRequirement.BootstrapOnly,
                    AppRouteRequirement.SignedOutOnly,
                    -> AppRoute.Home

                    AppRouteRequirement.Public,
                    AppRouteRequirement.Authenticated -> requested
                    }
                }

            is AppSessionState.SignedOut,
            AppSessionState.Authenticating,
            is AppSessionState.CareerSelectionRequired,
            is AppSessionState.ReauthenticationRequired,
            ->
                when (requested.requirement) {
                    AppRouteRequirement.Public,
                    AppRouteRequirement.SignedOutOnly,
                    -> requested.takeIf { it != AppRoute.Bootstrap } ?: AppRoute.Login

                    AppRouteRequirement.BootstrapOnly,
                    AppRouteRequirement.Authenticated,
                    -> AppRoute.Login
                }
        }
    }

    fun canRender(
        route: AppRoute,
        sessionState: AppSessionState,
    ): Boolean = resolve(route, sessionState) == route
}

private fun AppRoute.isLibraryRoute(): Boolean =
    when (this) {
        is AppRoute.Library,
        is AppRoute.LibraryBookDetail,
        AppRoute.LibraryBooking,
        -> true

        else -> false
    }

private fun AppRoute.isStudentOnly(): Boolean =
    when (this) {
        AppRoute.Taxes,
        AppRoute.Grades,
        is AppRoute.Transcripts,
        is AppRoute.CourseDetail,
        AppRoute.Questionnaires,
        is AppRoute.Questionnaire,
        AppRoute.Attendance,
        -> true

        else -> false
    }

private fun AppRoute.isProfessorOnly(): Boolean =
    this == AppRoute.Teachings ||
        this == AppRoute.Theses ||
        this == AppRoute.Reports ||
        this is AppRoute.TeachingDetail ||
        this is AppRoute.ProfessorExamDetail ||
        this is AppRoute.ThesisDetail ||
        this is AppRoute.ReportDetail
