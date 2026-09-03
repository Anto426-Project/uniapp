package com.anto426.uniapp.navigation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.navigation.model.AppRoute
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

internal data class AppRoutePresentation(
    val titleRes: StringResource? = null,
    val titleString: String? = null,
    val subtitleRes: StringResource? = null,
    val subtitleString: String? = null,
    val icon: ImageVector,
) {
    val title: String
        @Composable get() = titleRes?.let { stringResource(it) } ?: titleString.orEmpty()

    val subtitle: String
        @Composable get() = subtitleRes?.let { stringResource(it) } ?: subtitleString.orEmpty()
}

internal fun AppRoute.presentation(isProfessor: Boolean = false): AppRoutePresentation =
    when (this) {
        AppRoute.Bootstrap -> AppRoutePresentation(
            titleRes = Res.string.ui_app_name,
            subtitleRes = Res.string.nav_route_bootstrap_subtitle,
            icon = LiquidIcons.Lock,
        )
        AppRoute.Login -> AppRoutePresentation(
            titleRes = Res.string.nav_route_login_title,
            subtitleRes = Res.string.nav_route_login_subtitle,
            icon = LiquidIcons.Lock,
        )
        AppRoute.Home ->
            if (isProfessor) {
                AppRoutePresentation(
                    titleRes = Res.string.ui_app_name,
                    subtitleRes = Res.string.nav_route_home_professor_subtitle,
                    icon = LiquidIcons.Home,
                )
            } else {
                AppRoutePresentation(
                    titleRes = Res.string.ui_app_name,
                    subtitleRes = Res.string.nav_route_home_student_subtitle,
                    icon = LiquidIcons.Home,
                )
            }
        AppRoute.Services ->
            if (isProfessor) {
                AppRoutePresentation(
                    titleRes = Res.string.nav_route_services_title,
                    subtitleRes = Res.string.nav_route_services_professor_subtitle,
                    icon = LiquidIcons.Star,
                )
            } else {
                AppRoutePresentation(
                    titleRes = Res.string.nav_route_services_title,
                    subtitleRes = Res.string.nav_route_services_student_subtitle,
                    icon = LiquidIcons.Star,
                )
            }
        AppRoute.Didactics ->
            if (isProfessor) {
                AppRoutePresentation(
                    titleRes = Res.string.nav_route_didactics_title,
                    subtitleRes = Res.string.nav_route_didactics_professor_subtitle,
                    icon = LiquidIcons.MenuBook,
                )
            } else {
                AppRoutePresentation(
                    titleRes = Res.string.nav_route_didactics_title,
                    subtitleRes = Res.string.nav_route_didactics_student_subtitle,
                    icon = LiquidIcons.Calendar,
                )
            }
        AppRoute.Teachings -> AppRoutePresentation(
            titleRes = Res.string.nav_route_teachings_title,
            subtitleRes = Res.string.nav_route_teachings_subtitle,
            icon = LiquidIcons.MenuBook,
        )
        AppRoute.Theses -> AppRoutePresentation(
            titleRes = Res.string.nav_route_theses_title,
            subtitleRes = Res.string.nav_route_theses_subtitle,
            icon = LiquidIcons.Assignment,
        )
        AppRoute.Reports -> AppRoutePresentation(
            titleRes = Res.string.nav_route_reports_title,
            subtitleRes = Res.string.nav_route_reports_subtitle,
            icon = LiquidIcons.Edit,
        )
        is AppRoute.TeachingDetail -> AppRoutePresentation(
            titleString = title,
            subtitleRes = Res.string.nav_route_teaching_detail_subtitle,
            icon = LiquidIcons.MenuBook,
        )
        is AppRoute.ProfessorExamDetail -> AppRoutePresentation(
            titleRes = Res.string.nav_route_professor_exam_detail_title,
            subtitleString = title,
            icon = LiquidIcons.Calendar,
        )
        is AppRoute.ThesisDetail -> AppRoutePresentation(
            titleRes = Res.string.nav_route_thesis_detail_title,
            subtitleString = title,
            icon = LiquidIcons.Assignment,
        )
        is AppRoute.ReportDetail -> AppRoutePresentation(
            titleString = title,
            subtitleRes = Res.string.nav_route_report_detail_subtitle,
            icon = LiquidIcons.Edit,
        )
        AppRoute.Settings -> AppRoutePresentation(
            titleRes = Res.string.nav_route_settings_title,
            subtitleRes = Res.string.nav_route_settings_subtitle,
            icon = LiquidIcons.Settings,
        )
        AppRoute.Accounts -> AppRoutePresentation(
            titleRes = Res.string.nav_route_accounts_title,
            subtitleRes = Res.string.nav_route_accounts_subtitle,
            icon = LiquidIcons.AccountCircle,
        )
        AppRoute.Info -> AppRoutePresentation(
            titleRes = Res.string.nav_route_info_title,
            subtitleRes = Res.string.nav_route_info_subtitle,
            icon = LiquidIcons.Info,
        )
        AppRoute.Theme -> AppRoutePresentation(
            titleRes = Res.string.nav_route_theme_title,
            subtitleRes = Res.string.nav_route_theme_subtitle,
            icon = LiquidIcons.Star,
        )
        AppRoute.Colors -> AppRoutePresentation(
            titleRes = Res.string.nav_route_colors_title,
            subtitleRes = Res.string.nav_route_colors_subtitle,
            icon = LiquidIcons.Star,
        )
        AppRoute.Taxes -> AppRoutePresentation(
            titleRes = Res.string.nav_route_taxes_title,
            subtitleRes = Res.string.nav_route_taxes_subtitle,
            icon = LiquidIcons.Warning,
        )
        AppRoute.Grades -> AppRoutePresentation(
            titleRes = Res.string.nav_route_grades_title,
            subtitleRes = Res.string.nav_route_grades_subtitle,
            icon = LiquidIcons.Star,
        )
        AppRoute.Statistics -> AppRoutePresentation(
            titleRes = Res.string.nav_route_statistics_title,
            subtitleRes = Res.string.nav_route_statistics_subtitle,
            icon = LiquidIcons.Star,
        )
        AppRoute.Contacts -> AppRoutePresentation(
            titleRes = Res.string.nav_route_contacts_title,
            subtitleRes = Res.string.nav_route_contacts_subtitle,
            icon = LiquidIcons.AccountCircle,
        )
        is AppRoute.ContactDetail -> AppRoutePresentation(
            titleRes = Res.string.nav_route_contact_detail_title,
            subtitleRes = Res.string.nav_route_contact_detail_subtitle,
            icon = LiquidIcons.AccountCircle,
        )
        AppRoute.Transport -> AppRoutePresentation(
            titleRes = Res.string.nav_route_transport_title,
            subtitleRes = Res.string.nav_route_transport_subtitle,
            icon = LiquidIcons.Time,
        )
        AppRoute.TransportCatalog -> AppRoutePresentation(
            titleRes = Res.string.nav_route_transport_catalog_title,
            subtitleRes = Res.string.nav_route_transport_catalog_subtitle,
            icon = LiquidIcons.Star,
        )
        AppRoute.TransportBooking -> AppRoutePresentation(
            titleRes = Res.string.nav_route_transport_booking_title,
            subtitleRes = Res.string.nav_route_transport_booking_subtitle,
            icon = LiquidIcons.Calendar,
        )
        is AppRoute.TicketDetail -> AppRoutePresentation(
            titleString = title.ifBlank { null },
            titleRes = if (title.isBlank()) Res.string.nav_route_ticket_detail_default_title else null,
            subtitleRes = Res.string.nav_route_ticket_detail_subtitle,
            icon = LiquidIcons.Star,
        )
        is AppRoute.ReservationDetail -> AppRoutePresentation(
            titleString = title.ifBlank { null },
            titleRes = if (title.isBlank()) Res.string.nav_route_reservation_detail_default_title else null,
            subtitleRes = Res.string.nav_route_reservation_detail_subtitle,
            icon = LiquidIcons.Calendar,
        )
        AppRoute.Transcripts -> AppRoutePresentation(
            titleRes = Res.string.nav_route_transcripts_title,
            subtitleRes = Res.string.nav_route_transcripts_subtitle,
            icon = LiquidIcons.Calendar,
        )
        AppRoute.Exams ->
            if (isProfessor) {
                AppRoutePresentation(
                    titleRes = Res.string.nav_route_exams_title,
                    subtitleRes = Res.string.nav_route_exams_professor_subtitle,
                    icon = LiquidIcons.Calendar,
                )
            } else {
                AppRoutePresentation(
                    titleRes = Res.string.nav_route_exams_title,
                    subtitleRes = Res.string.nav_route_exams_student_subtitle,
                    icon = LiquidIcons.Calendar,
                )
            }
        AppRoute.ExamsHistory -> AppRoutePresentation(
            titleRes = Res.string.nav_route_exams_history_title,
            subtitleRes = Res.string.nav_route_exams_history_subtitle,
            icon = LiquidIcons.Time,
        )
        AppRoute.StudyPlan -> AppRoutePresentation(
            titleRes = Res.string.nav_route_study_plan_title,
            subtitleRes = Res.string.nav_route_study_plan_subtitle,
            icon = LiquidIcons.Edit,
        )
        is AppRoute.CourseDetail -> AppRoutePresentation(
            titleRes = Res.string.nav_route_course_detail_title,
            subtitleRes = Res.string.nav_route_course_detail_subtitle,
            icon = LiquidIcons.Star,
        )
        AppRoute.Questionnaires -> AppRoutePresentation(
            titleRes = Res.string.nav_route_questionnaires_title,
            subtitleRes = Res.string.nav_route_questionnaires_subtitle,
            icon = LiquidIcons.Edit,
        )
        is AppRoute.Questionnaire -> AppRoutePresentation(
            titleString = title,
            subtitleRes = Res.string.nav_route_questionnaire_subtitle,
            icon = LiquidIcons.Edit,
        )
        AppRoute.Badge ->
            AppRoutePresentation(
                titleRes = Res.string.nav_route_badge_title,
                subtitleRes = if (isProfessor) Res.string.nav_route_badge_professor_subtitle else Res.string.nav_route_badge_student_subtitle,
                icon = LiquidIcons.AccountCircle,
            )
        AppRoute.Attendance -> AppRoutePresentation(
            titleRes = Res.string.nav_route_attendance_title,
            subtitleRes = Res.string.nav_route_attendance_subtitle,
            icon = LiquidIcons.Check,
        )
        AppRoute.AboutUniApp -> AppRoutePresentation(
            titleRes = Res.string.nav_route_about_title,
            subtitleRes = Res.string.nav_route_about_subtitle,
            icon = LiquidIcons.Info,
        )
        AppRoute.Privacy -> AppRoutePresentation(
            titleRes = Res.string.nav_route_privacy_title,
            subtitleRes = Res.string.nav_route_privacy_subtitle,
            icon = LiquidIcons.Lock,
        )
        AppRoute.Terms -> AppRoutePresentation(
            titleRes = Res.string.nav_route_terms_title,
            subtitleRes = Res.string.nav_route_terms_subtitle,
            icon = LiquidIcons.Info,
        )
        AppRoute.Cookies -> AppRoutePresentation(
            titleRes = Res.string.nav_route_cookies_title,
            subtitleRes = Res.string.nav_route_cookies_subtitle,
            icon = LiquidIcons.Search,
        )
        AppRoute.Updates -> AppRoutePresentation(
            titleRes = Res.string.nav_route_updates_title,
            subtitleRes = Res.string.nav_route_updates_subtitle,
            icon = LiquidIcons.Refresh,
        )
        AppRoute.Changelog -> AppRoutePresentation(
            titleRes = Res.string.nav_route_changelog_title,
            subtitleRes = Res.string.nav_route_changelog_subtitle,
            icon = LiquidIcons.Star,
        )
        AppRoute.News -> AppRoutePresentation(
            titleRes = Res.string.nav_route_news_title,
            subtitleRes = Res.string.nav_route_news_subtitle,
            icon = LiquidIcons.Notifications,
        )
        is AppRoute.NewsDetail -> AppRoutePresentation(
            titleString = title.ifBlank { null },
            titleRes = if (title.isBlank()) Res.string.nav_route_news_detail_default_title else null,
            subtitleRes = Res.string.nav_route_news_detail_subtitle,
            icon = LiquidIcons.Notifications,
        )
        AppRoute.Devices -> AppRoutePresentation(
            titleRes = Res.string.nav_route_devices_title,
            subtitleRes = Res.string.nav_route_devices_subtitle,
            icon = LiquidIcons.Lock,
        )
        AppRoute.Language -> AppRoutePresentation(
            titleRes = Res.string.nav_route_language_title,
            subtitleRes = Res.string.nav_route_language_subtitle,
            icon = LiquidIcons.Info,
        )
        AppRoute.Author -> AppRoutePresentation(
            titleRes = Res.string.nav_route_author_title,
            subtitleRes = Res.string.nav_route_author_subtitle,
            icon = LiquidIcons.AccountCircle,
        )
    }

