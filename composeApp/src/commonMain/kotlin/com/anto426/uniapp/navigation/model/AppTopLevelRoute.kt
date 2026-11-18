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
        is AppRoute.NewsDetail,
        -> AppRoute.Home

        AppRoute.Services,
        AppRoute.Taxes,
        AppRoute.Transport,
        AppRoute.TransportBooking,
        is AppRoute.ReservationDetail,
        AppRoute.Contacts,
        is AppRoute.ContactDetail,
        -> AppRoute.Services

        AppRoute.Didactics,
        AppRoute.Teachings,
        AppRoute.Theses,
        AppRoute.Reports,
        is AppRoute.TeachingDetail,
        is AppRoute.ProfessorExamDetail,
        is AppRoute.ThesisDetail,
        is AppRoute.ReportDetail,
        AppRoute.Grades,
        is AppRoute.Transcripts,
        is AppRoute.Exams,
        is AppRoute.CourseDetail,
        AppRoute.Questionnaires,
        is AppRoute.Questionnaire,
        AppRoute.Badge,
        AppRoute.Attendance,
        -> AppRoute.Didactics

        AppRoute.Settings,
        AppRoute.Info,
        AppRoute.AboutUniApp,
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
