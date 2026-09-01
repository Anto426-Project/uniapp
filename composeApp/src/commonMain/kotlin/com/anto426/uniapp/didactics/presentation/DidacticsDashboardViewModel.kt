package com.anto426.uniapp.didactics.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.account.model.UniAccountSummary
import com.anto426.uniapp.data.UniAppDataSource
import com.anto426.uniapp.model.didactics.firstAcademicIntegerOrNull
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.userMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class DidacticsDashboardUiState(
    val isProfessor: Boolean = false,
    val degreeName: String = "",
    val degreeDetails: String = "",
    val completedExams: Int = 0,
    val plannedActivities: Int = 0,
    val currentYear: Int = 1,
    val totalYears: Int = 1,
    val average: String = "—",
    val acquiredCfu: Int = 0,
    val targetCfu: Int = 0,
    val progress: Float = 0f,
    val openExamRounds: Int = 0,
    val pendingQuestionnaires: Int = 0,
    val teachingCount: Int = 0,
    val thesisCount: Int = 0,
    val reportCount: Int = 0,
    val loadState: FeatureLoadState = FeatureLoadState.Loading,
    val errorMessage: String? = null,
)

class DidacticsDashboardViewModel(
    private val dataSource: UniAppDataSource,
    private val account: UniAccountSummary? = null,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(DidacticsDashboardUiState())
    val uiState: StateFlow<DidacticsDashboardUiState> = mutableUiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh(force: Boolean = false) {
        viewModelScope.launch {
            mutableUiState.update { it.copy(loadState = FeatureLoadState.Loading, errorMessage = null) }
            try {
                if (account?.isProfessor == true) {
                    val professor = checkNotNull(account)
                    val dashboard = dataSource.loadProfessorDashboard(force)
                    val profile =
                        professor.profiles.firstOrNull { it.profileId == professor.activeProfileId }
                            ?: professor.profiles.firstOrNull {
                                it.type == com.anto426.unisdk.backend.model.BackendCareerType.PROFESSOR
                            }
                    mutableUiState.value =
                        DidacticsDashboardUiState(
                            isProfessor = true,
                            degreeName = professor.displayName,
                            degreeDetails = profile?.departmentName.orEmpty(),
                            openExamRounds = dashboard.examRounds.size,
                            teachingCount = dashboard.teachings.size,
                            thesisCount = dashboard.theses.size,
                            reportCount = dashboard.reports.size,
                            loadState = FeatureLoadState.Content,
                            errorMessage =
                                dashboard.unavailableSections.takeIf { it.isNotEmpty() }
                                    ?.let { "Alcune sezioni docente non sono momentaneamente disponibili." },
                        )
                    return@launch
                }
                val snapshot = coroutineScope {
                    val student = async { dataSource.loadStudentDetails(force) }
                    val career = async { dataSource.loadCareer(force) }
                    val plan = async { dataSource.loadStudyPlan(force) }
                    val rounds = async { runCatching { dataSource.loadExamRounds(force) }.getOrDefault(emptyList()) }
                    val surveys = async { runCatching { dataSource.loadSurveyCourses(force) }.getOrDefault(emptyList()) }
                    DidacticsSnapshot(
                        student = student.await(),
                        career = career.await(),
                        plan = plan.await(),
                        rounds = rounds.await(),
                        surveys = surveys.await(),
                    )
                }

                val acquiredCfu = snapshot.career.cfu.firstAcademicIntegerOrNull() ?: 0
                val targetCfu = snapshot.career.cfuTarget ?: snapshot.plan.courses.sumOf { it.cfu ?: 0 }
                val completedExams = maxOf(
                    snapshot.career.exams.size,
                    snapshot.plan.courses.count { it.completed },
                )
                val totalActivities = snapshot.plan.courses.size.coerceAtLeast(completedExams)
                val currentYear = snapshot.career.year.firstAcademicIntegerOrNull()?.coerceAtLeast(1) ?: 1
                val totalYears = snapshot.plan.courses.mapNotNull { it.year }.maxOrNull()?.coerceAtLeast(currentYear)
                    ?: currentYear
                val details = listOfNotNull(
                    snapshot.student.departmentName?.takeIf(String::isNotBlank),
                    snapshot.student.matricola?.takeIf(String::isNotBlank)?.let { "Matricola $it" },
                ).joinToString(" • ")

                mutableUiState.value = DidacticsDashboardUiState(
                    degreeName = snapshot.student.degreeName ?: snapshot.career.status,
                    degreeDetails = details,
                    completedExams = completedExams,
                    plannedActivities = totalActivities,
                    currentYear = currentYear,
                    totalYears = totalYears,
                    average = snapshot.career.average,
                    acquiredCfu = acquiredCfu,
                    targetCfu = targetCfu,
                    progress = if (targetCfu > 0) {
                        (acquiredCfu.toFloat() / targetCfu).coerceIn(0f, 1f)
                    } else {
                        0f
                    },
                    openExamRounds = snapshot.rounds.count { it.open && !it.booked },
                    pendingQuestionnaires = snapshot.surveys.count { it.enabled && !it.completed },
                    loadState = FeatureLoadState.Content,
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                mutableUiState.update {
                    it.copy(
                        loadState = if (it.degreeName.isBlank()) FeatureLoadState.Error else FeatureLoadState.Content,
                        errorMessage = error.userMessage("Impossibile caricare la panoramica didattica."),
                    )
                }
            }
        }
    }

    private data class DidacticsSnapshot(
        val student: com.anto426.unisdk.backend.model.StudentDetailsData,
        val career: com.anto426.unisdk.backend.model.CareerData,
        val plan: com.anto426.unisdk.backend.model.StudyPlanData,
        val rounds: List<com.anto426.unisdk.backend.model.ExamRoundData>,
        val surveys: List<com.anto426.unisdk.backend.model.SurveyCourseData>,
    )
}
