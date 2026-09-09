package com.anto426.uniapp.didactics.presentation

import org.jetbrains.compose.resources.getString
import uniapp.composeapp.generated.resources.*

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.account.model.UniAccountSummary
import com.anto426.uniapp.data.runtime.*
import com.anto426.uniapp.data.UniAppDataSource
import com.anto426.uniapp.model.didactics.firstAcademicIntegerOrNull
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.onRefresh
import com.anto426.uniapp.presentation.userMessage
import kotlinx.coroutines.CancellationException
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

    private val sharedData = dataSource.sharedData(viewModelScope)
    private val dataRequests = if (account?.isProfessor == true) listOf(UniAppDataRequests.Professor) else listOf(UniAppDataRequests.Student, UniAppDataRequests.Career, UniAppDataRequests.StudyPlan, UniAppDataRequests.Exams, UniAppDataRequests.Surveys)
    private val dataObservation = sharedData.observeIn(viewModelScope, dataRequests, subscriptions = mutableUiState.subscriptionCount) { snapshot ->
        mutableUiState.update { it.copy(loadState = mutableUiState.value.loadState.onRefresh(), errorMessage = null) }
        try {
            if (account?.isProfessor == true) {
                val professor = checkNotNull(account)
                val dashboard = snapshot.require(UniAppDataRequests.Professor)
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
                                ?.let { getString(Res.string.msg_alcune_sezioni_docente_non_sono_momentaneamente_disponibili) },
                    )
                snapshot.throwIfFailed()
                return@observeIn
            }
            val data = DidacticsSnapshot(
                student = snapshot.require(UniAppDataRequests.Student),
                career = snapshot.require(UniAppDataRequests.Career),
                plan = snapshot.require(UniAppDataRequests.StudyPlan),
                rounds = snapshot.value(UniAppDataRequests.Exams).orEmpty(),
                surveys = snapshot.value(UniAppDataRequests.Surveys).orEmpty(),
            )

            val acquiredCfu = data.career.cfu.firstAcademicIntegerOrNull() ?: 0
            val targetCfu = data.career.cfuTarget ?: data.plan.courses.sumOf { it.cfu ?: 0 }
            val completedExams = maxOf(
                data.career.exams.size,
                data.plan.courses.count { it.completed },
            )
            val totalActivities = data.plan.courses.size.coerceAtLeast(completedExams)
            val currentYear = data.career.year.firstAcademicIntegerOrNull()?.coerceAtLeast(1) ?: 1
            val totalYears = data.plan.courses.mapNotNull { it.year }.maxOrNull()?.coerceAtLeast(currentYear)
                ?: currentYear
            val details = listOfNotNull(
                data.student.departmentName?.takeIf(String::isNotBlank),
                data.student.matricola?.takeIf(String::isNotBlank)?.let { "Matricola $it" },
            ).joinToString(" • ")

            mutableUiState.value = DidacticsDashboardUiState(
                degreeName = data.student.degreeName ?: data.career.status,
                degreeDetails = details,
                completedExams = completedExams,
                plannedActivities = totalActivities,
                currentYear = currentYear,
                totalYears = totalYears,
                average = data.career.average,
                acquiredCfu = acquiredCfu,
                targetCfu = targetCfu,
                progress = if (targetCfu > 0) {
                    (acquiredCfu.toFloat() / targetCfu).coerceIn(0f, 1f)
                } else {
                    0f
                },
                openExamRounds = data.rounds.count { it.open && !it.booked },
                pendingQuestionnaires = data.surveys.count { it.enabled && !it.completed },
                loadState = FeatureLoadState.Content,
            )
            snapshot.throwIfFailed()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            mutableUiState.update {
                it.copy(
                    loadState = if (it.degreeName.isBlank()) FeatureLoadState.Error else FeatureLoadState.Content,
                    errorMessage = error.userMessage(getString(Res.string.msg_impossibile_caricare_la_panoramica_didattica)),
                )
            }
        }

    }

    fun refresh(force: Boolean = false) {
        sharedData.refresh(dataRequests, force)
    }

    private data class DidacticsSnapshot(
        val student: com.anto426.unisdk.backend.model.StudentDetailsData,
        val career: com.anto426.unisdk.backend.model.CareerData,
        val plan: com.anto426.unisdk.backend.model.StudyPlanData,
        val rounds: List<com.anto426.unisdk.backend.model.ExamRoundData>,
        val surveys: List<com.anto426.unisdk.backend.model.SurveyCourseData>,
    )
}
