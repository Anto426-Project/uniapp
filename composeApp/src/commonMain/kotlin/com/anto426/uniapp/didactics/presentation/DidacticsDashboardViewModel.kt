package com.anto426.uniapp.didactics.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.account.model.UniAccountSummary
import com.anto426.uniapp.account.model.UniProfileRole
import com.anto426.uniapp.data.UniDatasets
import com.anto426.uniapp.data.UniGateway
import com.anto426.uniapp.data.asUniRepository
import com.anto426.uniapp.data.observeIn
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.onRefresh
import com.anto426.uniapp.presentation.userMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.anto426.uniapp.localization.readAppString
import uniapp.composeapp.generated.resources.*

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
    private val dataSource: UniGateway,
    private val account: UniAccountSummary? = null,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(DidacticsDashboardUiState())
    val uiState: StateFlow<DidacticsDashboardUiState> = mutableUiState.asStateFlow()

    private val repository = dataSource.asUniRepository(viewModelScope)
    private val sharedData = repository.engine
    private val dataRequests = if (account?.isProfessor == true) {
        listOf(UniDatasets.Professor)
    } else {
        listOf(
            UniDatasets.Student,
            UniDatasets.Career,
            UniDatasets.StudyPlan,
            UniDatasets.Exams,
            UniDatasets.Surveys,
        )
    }
    private val dataObservation = sharedData.observeIn(viewModelScope, dataRequests, subscriptions = mutableUiState.subscriptionCount) { snapshot ->
        mutableUiState.update { it.copy(loadState = mutableUiState.value.loadState.onRefresh(), errorMessage = null) }
        try {
            if (account?.isProfessor == true) {
                val professor = checkNotNull(account)
                val dashboard = snapshot.require(UniDatasets.Professor)
                val profile =
                    professor.profiles.firstOrNull { it.profileId == professor.activeProfileId }
                        ?: professor.profiles.firstOrNull {
                            it.type == UniProfileRole.PROFESSOR
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
                                ?.let { readAppString(Res.string.msg_alcune_sezioni_docente_non_sono_momentaneamente_disponibili) },
                    )
                snapshot.throwIfFailed()
                return@observeIn
            }
            val student = snapshot.require(UniDatasets.Student)
            val career = snapshot.require(UniDatasets.Career)
            val plan = snapshot.require(UniDatasets.StudyPlan)
            val rounds = snapshot.value(UniDatasets.Exams).orEmpty()
            val surveys = snapshot.value(UniDatasets.Surveys).orEmpty()

            val acquiredCfu = career.resolveAcquiredCfu(plan)
            val targetCfu = career.resolveTargetCfu(plan, defaultTarget = 0)
            val completedExams = career.resolveCompletedExamsCount(plan)
            val totalActivities = plan.courses.size.coerceAtLeast(completedExams)
            val currentYear = career.currentYearValue
            val totalYears = career.resolveTotalYears(plan)

            mutableUiState.value = DidacticsDashboardUiState(
                degreeName = student.degreeName ?: career.status,
                degreeDetails = student.degreeSummaryDetails,
                completedExams = completedExams,
                plannedActivities = totalActivities,
                currentYear = currentYear,
                totalYears = totalYears,
                average = career.average,
                acquiredCfu = acquiredCfu,
                targetCfu = targetCfu,
                progress = career.resolveProgress(plan, defaultTarget = 0),
                openExamRounds = rounds.count { it.open && !it.booked },
                pendingQuestionnaires = surveys.count { it.enabled && !it.completed },
                loadState = FeatureLoadState.Content,
            )
            snapshot.throwIfFailed()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            mutableUiState.update {
                it.copy(
                    loadState = if (it.degreeName.isBlank()) FeatureLoadState.Error else FeatureLoadState.Content,
                    errorMessage = error.userMessage { readAppString(Res.string.msg_impossibile_caricare_la_panoramica_didattica) },
                )
            }
        }
    }

    fun refresh(force: Boolean = false) {
        repository.didactics.refresh(dataRequests, force)
    }
}
