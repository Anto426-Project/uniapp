package com.anto426.uniapp.navigation.model

val appTopLevelRoutes: List<AppRoute> =
    listOf(
        AppRoute.Home,
        AppRoute.Services,
        AppRoute.Didactics,
        AppRoute.Settings,
    )

val AppRoute.isTopLevel: Boolean
    get() = this in appTopLevelRoutes

fun AppRoute.topLevelParent(): AppRoute =
    when (this) {
        AppRoute.Home,
        AppRoute.News,
        -> AppRoute.Home

        AppRoute.Services,
        AppRoute.Taxes,
        AppRoute.Transport,
        AppRoute.TransportCatalog,
        AppRoute.TransportBooking,
        is AppRoute.TicketDetail,
        is AppRoute.ReservationDetail,
        AppRoute.Contacts,
        is AppRoute.ContactDetail,
        -> AppRoute.Services

        AppRoute.Didactics,
        AppRoute.Career,
        AppRoute.Grades,
        AppRoute.Statistics,
        AppRoute.Transcripts,
        AppRoute.Exams,
        AppRoute.ExamsHistory,
        AppRoute.StudyPlan,
        is AppRoute.CourseDetail,
        AppRoute.Questionnaires,
        AppRoute.Badge,
        AppRoute.Attendance,
        -> AppRoute.Didactics

        AppRoute.Settings,
        AppRoute.Accounts,
        AppRoute.Info,
        AppRoute.Theme,
        AppRoute.Colors,
        AppRoute.Privacy,
        AppRoute.Terms,
        AppRoute.Cookies,
        AppRoute.Updates,
        AppRoute.Changelog,
        AppRoute.Devices,
        AppRoute.Language,
        AppRoute.Author,
        -> AppRoute.Settings

        AppRoute.Bootstrap,
        AppRoute.Login,
        -> this
    }
