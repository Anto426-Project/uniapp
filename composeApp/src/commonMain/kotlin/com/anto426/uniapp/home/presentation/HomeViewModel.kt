package com.anto426.uniapp.home.presentation

import androidx.lifecycle.ViewModel
import com.anto426.uniapp.model.home.dashboard.AcademicStatsSummary
import com.anto426.uniapp.model.home.dashboard.CareerOverviewSummary
import com.anto426.uniapp.model.home.dashboard.DashboardQuickAction
import com.anto426.uniapp.model.home.dashboard.HomeDashboardData
import com.anto426.uniapp.model.home.dashboard.StudentProfileSummary
import com.anto426.uniapp.model.home.dashboard.UpcomingDeadlineSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Temporary UI-only state holder. The real ViewModel will be introduced with its use cases. */
class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(
        HomeUiState.Success(sampleDashboardData)
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun refresh() {
        _uiState.value = HomeUiState.Success(sampleDashboardData)
    }

    companion object {
        val defaultQuickActions = listOf(
            DashboardQuickAction(
                id = "libretto",
                title = "Libretto",
                subtitle = "Esami e voti",
                iconKey = "📚",
                routeKey = "didattica/libretto"
            ),
            DashboardQuickAction(
                id = "appelli",
                title = "Appelli",
                subtitle = "Prenotazioni",
                iconKey = "📝",
                routeKey = "didattica/appelli"
            ),
            DashboardQuickAction(
                id = "tasse",
                title = "Tasse",
                subtitle = "Pagamenti",
                iconKey = "📄",
                routeKey = "servizi/tasse"
            ),
            DashboardQuickAction(
                id = "trasporti",
                title = "Trasporti",
                subtitle = "Navette e orari",
                iconKey = "🚌",
                routeKey = "servizi/trasporti"
            ),
            DashboardQuickAction(
                id = "moodle",
                title = "Moodle",
                subtitle = "Materiale didattico",
                iconKey = "🎓",
                routeKey = "servizi/moodle"
            ),
            DashboardQuickAction(
                id = "rubrica",
                title = "Rubrica",
                subtitle = "Contatti docenti",
                iconKey = "👤",
                routeKey = "servizi/rubrica"
            )
        )

        val sampleDashboardData = HomeDashboardData(
            profile = StudentProfileSummary(
                fullName = "Antonio",
                firstName = "Antonio",
                studentNumber = "123456",
                degreeCourse = "Ingegneria Informatica",
                academicYear = "2025/2026",
                status = "In Corso"
            ),
            career = CareerOverviewSummary(
                weightedAverage = "27.8",
                degreeBase110 = "90/110",
                cfuAcquired = 15,
                cfuTarget = 168
            ),
            nextDeadline = UpcomingDeadlineSummary(
                title = "2ª Rata Contributo Onnicomprensivo",
                amountFormatted = "EUR 395.33",
                deadlineFormatted = "29/05/2026",
                isUrgent = false
            ),
            stats = AcademicStatsSummary(
                openExamRoundsCount = 0,
                unpaidTaxesCount = 1,
                activeCoursesCount = 5,
                unreadNoticesCount = 2
            ),
            quickActions = defaultQuickActions
        )
    }
}
