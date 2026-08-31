package com.anto426.uniapp.navigation.model

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Complete set of navigation keys owned by UniApp.
 *
 * Detail routes carry only stable identifiers. Screen models and authenticated data are resolved
 * by the destination from the appropriate data source instead of being serialized in navigation.
 */
@Serializable
sealed interface AppRoute : NavKey {
    @Serializable
    data object Bootstrap : AppRoute

    @Serializable
    data object Login : AppRoute

    @Serializable
    data object Home : AppRoute

    @Serializable
    data object Services : AppRoute

    @Serializable
    data object Didactics : AppRoute

    @Serializable
    data object Settings : AppRoute

    @Serializable
    data object Accounts : AppRoute

    @Serializable
    data object Career : AppRoute

    @Serializable
    data object Info : AppRoute

    @Serializable
    data object Theme : AppRoute

    @Serializable
    data object Colors : AppRoute

    @Serializable
    data object Taxes : AppRoute

    @Serializable
    data object Grades : AppRoute

    @Serializable
    data object Statistics : AppRoute

    @Serializable
    data object Contacts : AppRoute

    @Serializable
    data class ContactDetail(val contactId: String) : AppRoute

    @Serializable
    data object Transport : AppRoute

    @Serializable
    data object TransportCatalog : AppRoute

    @Serializable
    data object TransportBooking : AppRoute

    @Serializable
    data class TicketDetail(val ticketId: String) : AppRoute

    @Serializable
    data class ReservationDetail(val reservationId: String) : AppRoute

    @Serializable
    data object Transcripts : AppRoute

    @Serializable
    data object Exams : AppRoute

    @Serializable
    data object ExamsHistory : AppRoute

    @Serializable
    data object StudyPlan : AppRoute

    @Serializable
    data class CourseDetail(val courseId: String) : AppRoute

    @Serializable
    data object Questionnaires : AppRoute

    @Serializable
    data object Badge : AppRoute

    @Serializable
    data object Attendance : AppRoute

    @Serializable
    data object Privacy : AppRoute

    @Serializable
    data object Terms : AppRoute

    @Serializable
    data object Cookies : AppRoute

    @Serializable
    data object Updates : AppRoute

    @Serializable
    data object Changelog : AppRoute

    @Serializable
    data object News : AppRoute

    @Serializable
    data object Devices : AppRoute

    @Serializable
    data object Language : AppRoute

    @Serializable
    data object Author : AppRoute
}
