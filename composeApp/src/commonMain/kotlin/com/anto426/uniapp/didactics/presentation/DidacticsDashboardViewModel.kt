package com.anto426.uniapp.didactics.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.account.model.UniAccountSummary
import com.anto426.uniapp.data.UniDatasets
import com.anto426.uniapp.data.UniGateway
import com.anto426.uniapp.data.academicContract
import com.anto426.uniapp.data.asUniRepository
import com.anto426.uniapp.data.examsContract
import com.anto426.uniapp.data.identityContract
import com.anto426.uniapp.data.observeIn
import com.anto426.uniapp.data.professorContract
import com.anto426.uniapp.data.surveysContract
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.userMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.jetbrains.compose.resources.getString
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
    private val mutableUiState = MutableStateFlow(
        DidacticsDashboardUiState(
            isProfessor = account?.isProfessor == true,
            degreeName = if (account?.isProfessor == true) {
                account.displayName
            } else {
                account?.degreeName.orEmpty()
            },
        ),
    )
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
    private val dataObservation = sharedData.observeIn(viewModelScope, dataRequests, partial = true, subscriptions = mutableUiState.subscriptionCount) { snapshot ->
        val failure = snapshot.firstError?.userMessage { getString(Res.string.msg_impossibile_caricare_la_panoramica_didattica) }
        val professorWarning = if (account?.isProfessor == true && snapshot.value(UniDatasets.Professor) != null) {
            snapshot.professorContract().unavailableSections
                .takeIf { it.isNotEmpty() }
                ?.let { getString(Res.string.msg_alcune_sezioni_docente_non_sono_momentaneamente_disponibili) }
        } else {
            null
        }
        mutableUiState.update { current ->
            var next = current.copy(errorMessage = professorWarning ?: failure)
            if (account?.isProfessor == true) {
                val identity = snapshot.identityContract(account)
                next = next.copy(
                    isProfessor = true,
                    degreeName = identity.fullName,
                    degreeDetails = identity.departmentName,
                )
                if (snapshot.value(UniDatasets.Professor) != null) {
                    val professor = snapshot.professorContract()
                    next = next.copy(
                        openExamRounds = professor.openExamRoundsCount,
                        teachingCount = professor.teachingCount,
                        thesisCount = professor.thesisCount,
                        reportCount = professor.reportCount,
                        loadState = FeatureLoadState.Content,
                        errorMessage = professorWarning,
                    )
                }
                val hasProfessorContent = next.loadState == FeatureLoadState.Content
                next.copy(
                    loadState = when {
                        hasProfessorContent -> FeatureLoadState.Content
                        snapshot.resolved && failure != null -> FeatureLoadState.Error
                        snapshot.resolved -> FeatureLoadState.Content
                        else -> FeatureLoadState.Loading
                    },
                )
            } else {
                if (snapshot.value(UniDatasets.Student) != null || account != null) {
                    val identity = snapshot.identityContract(account)
                    next = next.copy(
                        degreeName = identity.degreeName.ifBlank { next.degreeName },
                        degreeDetails = identity.degreeSummary.ifBlank { next.degreeDetails },
                        loadState = if (snapshot.value(UniDatasets.Student) != null) FeatureLoadState.Content else next.loadState,
                    )
                }
                if (snapshot.value(UniDatasets.Career) != null || snapshot.value(UniDatasets.StudyPlan) != null) {
                    val academic = snapshot.academicContract(defaultTargetCfu = 180)
                    next = next.copy(
                        degreeName = next.degreeName.ifBlank { academic.statusLabel },
                        completedExams = academic.completedExamsCount,
                        plannedActivities = academic.plannedActivitiesCount,
                        currentYear = academic.currentYear,
                        totalYears = academic.totalYears,
                        average = academic.averageLabel,
                        acquiredCfu = academic.acquiredCfu,
                        targetCfu = academic.targetCfu,
                        progress = academic.progress,
                        loadState = FeatureLoadState.Content,
                    )
                }
                if (snapshot.value(UniDatasets.Exams) != null) {
                    val exams = snapshot.examsContract()
                    next = next.copy(openExamRounds = exams.openBookableCount)
                }
                if (snapshot.value(UniDatasets.Surveys) != null) {
                    val surveys = snapshot.surveysContract(requireSurveys = false)
                    next = next.copy(pendingQuestionnaires = surveys.pendingCount)
                }
                val hasContent = next.loadState == FeatureLoadState.Content
                next.copy(
                    loadState = when {
                        hasContent -> FeatureLoadState.Content
                        snapshot.resolved && failure != null -> FeatureLoadState.Error
                        snapshot.resolved -> FeatureLoadState.Content
                        else -> FeatureLoadState.Loading
                    },
                    errorMessage = if (hasContent) null else failure,
                )
            }
        }
    }

    fun refresh(force: Boolean = false) {
        mutableUiState.update { current ->
            if (current.loadState == FeatureLoadState.Error) {
                current.copy(loadState = FeatureLoadState.Loading, errorMessage = null)
            } else {
                current
            }
        }
        repository.didactics.refresh(dataRequests, force)
    }
}
