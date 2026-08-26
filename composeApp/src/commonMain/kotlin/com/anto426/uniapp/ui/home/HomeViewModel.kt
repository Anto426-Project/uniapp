package com.anto426.uniapp.ui.home

import com.anto426.uniapp.backend.UniBackendService
import com.anto426.uniapp.backend.model.BackendUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val backendService: UniBackendService? = null,
    private val currentUser: BackendUser? = null,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun refresh() {
        _uiState.value = HomeUiState.Loading
        loadDashboardData()
    }

    fun loadDashboardData() {
        scope.launch {
            try {
                if (backendService != null && currentUser != null) {
                    val careerData = try { backendService.loadCareer(currentUser) } catch (_: Exception) { null }
                    val taxesData = try { backendService.loadTaxes(currentUser) } catch (_: Exception) { null }
                    val studentData = try { backendService.loadStudentDetails(currentUser) } catch (_: Exception) { null }
                    val homeData = try { backendService.loadHome(currentUser) } catch (_: Exception) { null }
                    val examRounds = try { backendService.loadExamRounds(currentUser) } catch (_: Exception) { emptyList() }

                    val fullName = studentData?.fullName ?: currentUser.displayName
                    val firstName = fullName.substringBefore(" ").ifEmpty { "Studente" }

                    val profileSummary = StudentProfileSummary(
                        fullName = fullName,
                        firstName = firstName,
                        studentNumber = studentData?.matricola ?: currentUser.matricola ?: "---",
                        degreeCourse = studentData?.degreeName ?: currentUser.degreeName,
                        academicYear = careerData?.year ?: "2025/2026",
                        status = careerData?.status ?: "In Corso"
                    )

                    val cfuParsed = careerData?.cfu?.filter { it.isDigit() }?.toIntOrNull() ?: 15
                    val cfuTarget = careerData?.cfuTarget ?: 180
                    val careerSummary = CareerOverviewSummary(
                        weightedAverage = careerData?.average ?: "---",
                        degreeBase110 = careerData?.degreeBase ?: "---/110",
                        cfuAcquired = cfuParsed,
                        cfuTarget = cfuTarget
                    )

                    val firstUnpaidTax = taxesData?.unpaidInstallments?.firstOrNull()
                    val nextDeadline = firstUnpaidTax?.let {
                        UpcomingDeadlineSummary(
                            title = it.title,
                            amountFormatted = it.amount,
                            deadlineFormatted = it.deadline,
                            isUrgent = false
                        )
                    }

                    val statsSummary = AcademicStatsSummary(
                        openExamRoundsCount = examRounds.size,
                        unpaidTaxesCount = taxesData?.unpaidInstallments?.size ?: 0,
                        activeCoursesCount = 5,
                        unreadNoticesCount = homeData?.newsCount ?: 0
                    )

                    _uiState.value = HomeUiState.Success(
                        HomeDashboardData(
                            profile = profileSummary,
                            career = careerSummary,
                            nextDeadline = nextDeadline,
                            stats = statsSummary,
                            quickActions = defaultQuickActions
                        )
                    )
                } else {
                    // Rich preview / mock state for design and decoupled rendering
                    _uiState.value = HomeUiState.Success(sampleDashboardData)
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(
                    message = e.message ?: "Errore durante il caricamento della dashboard",
                    canRetry = true
                )
            }
        }
    }

    companion object {
        val defaultQuickActions = listOf(
            QuickActionItem(
                id = "libretto",
                title = "Libretto",
                subtitle = "Esami e voti",
                iconKey = "📚",
                routeKey = "didattica/libretto"
            ),
            QuickActionItem(
                id = "appelli",
                title = "Appelli",
                subtitle = "Prenotazioni",
                iconKey = "📝",
                routeKey = "didattica/appelli"
            ),
            QuickActionItem(
                id = "tasse",
                title = "Tasse",
                subtitle = "Pagamenti",
                iconKey = "📄",
                routeKey = "servizi/tasse"
            ),
            QuickActionItem(
                id = "trasporti",
                title = "Trasporti",
                subtitle = "Navette e orari",
                iconKey = "🚌",
                routeKey = "servizi/trasporti"
            ),
            QuickActionItem(
                id = "moodle",
                title = "Moodle",
                subtitle = "Materiale didattico",
                iconKey = "🎓",
                routeKey = "servizi/moodle"
            ),
            QuickActionItem(
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
