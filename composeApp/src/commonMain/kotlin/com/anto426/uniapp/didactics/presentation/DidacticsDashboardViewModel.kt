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
import com.anto426.uniapp.presentation.onRefresh
import com.anto426.uniapp.presentation.userMessage
import kotlinx.coroutines.CancellationException
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
    private val dataObservation = sharedData.observeIn(viewModelScope, dataRequests, partial = true, subscriptions = mutableUiState.subscriptionCount) { snapshot ->
        mutableUiState.update { it.copy(loadState = mutableUiState.value.loadState.onRefresh(), errorMessage = null) }
        try {
            if (account?.isProfessor == true) {
                val professorState = snapshot.state(UniDatasets.Professor)
                if (!professorState.resolved) return@observeIn
                val identity = snapshot.identityContract(account)
                val professor = snapshot.professorContract(requireDashboard = true)
                mutableUiState.value = DidacticsDashboardUiState(
                    isProfessor = true,
                    degreeName = identity.fullName,
                    degreeDetails = identity.departmentName,
                    openExamRounds = professor.openExamRoundsCount,
                    teachingCount = professor.teachingCount,
                    thesisCount = professor.thesisCount,
                    reportCount = professor.reportCount,
                    loadState = FeatureLoadState.Content,
                    errorMessage = professor.unavailableSections.takeIf { it.isNotEmpty() }
                        ?.let { getString(Res.string.msg_alcune_sezioni_docente_non_sono_momentaneamente_disponibili) },
                )
                snapshot.throwIfFailed()
                return@observeIn
            }

            val coreReady =
                snapshot.state(UniDatasets.Student).resolved &&
                    snapshot.state(UniDatasets.Career).resolved &&
                    snapshot.state(UniDatasets.StudyPlan).resolved
            if (!coreReady) return@observeIn

            val identity = snapshot.identityContract(account, requireStudent = true)
            val academic = snapshot.academicContract(requireCareer = true, requireStudyPlan = true, defaultTargetCfu = 180)
            val exams = snapshot.examsContract()
            val surveys = snapshot.surveysContract(requireSurveys = false)

            mutableUiState.value = DidacticsDashboardUiState(
                degreeName = identity.degreeName.ifBlank { academic.statusLabel },
                degreeDetails = identity.degreeSummary,
                completedExams = academic.completedExamsCount,
                plannedActivities = academic.plannedActivitiesCount,
                currentYear = academic.currentYear,
                totalYears = academic.totalYears,
                average = academic.averageLabel,
                acquiredCfu = academic.acquiredCfu,
                targetCfu = academic.targetCfu,
                progress = academic.progress,
                openExamRounds = exams.openBookableCount,
                pendingQuestionnaires = surveys.pendingCount,
                loadState = FeatureLoadState.Content,
            )
            snapshot.throwIfFailed()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            mutableUiState.update {
                it.copy(
                    loadState = if (it.degreeName.isBlank()) FeatureLoadState.Error else FeatureLoadState.Content,
                    errorMessage = error.userMessage { getString(Res.string.msg_impossibile_caricare_la_panoramica_didattica) },
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
