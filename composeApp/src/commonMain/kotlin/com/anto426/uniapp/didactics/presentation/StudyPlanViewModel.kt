package com.anto426.uniapp.didactics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.UniDatasets
import com.anto426.uniapp.data.UniGateway
import com.anto426.uniapp.data.academicContract
import com.anto426.uniapp.data.asUniRepository
import com.anto426.uniapp.data.contract.CourseStatus
import com.anto426.uniapp.data.contract.StudyYear
import com.anto426.uniapp.data.observeIn
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.onRefresh
import com.anto426.uniapp.presentation.onRefreshFailure
import com.anto426.uniapp.presentation.userMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.jetbrains.compose.resources.getString
import uniapp.composeapp.generated.resources.*

data class StudyPlanUiState(
    val selectedYearIndex: Int = ALL_ACADEMIC_YEARS,
    val years: List<StudyYear> = emptyList(),
    val loadState: FeatureLoadState = FeatureLoadState.Loading,
    val errorMessage: String? = null,
    val acquiredCfu: Int = 0,
    val totalCfu: Int = 0,
    val completedCount: Int = 0,
    val totalCoursesCount: Int = 0,
    val statusFilter: CourseStatus? = null,
) {
    val displayedYears: List<StudyYear>
        get() = if (selectedYearIndex == ALL_ACADEMIC_YEARS) {
            years
        } else {
            listOfNotNull(years.getOrNull(selectedYearIndex))
        }
}

class StudyPlanViewModel(private val dataSource: UniGateway) : ViewModel() {
    private val mutableUiState = MutableStateFlow(StudyPlanUiState())
    val uiState: StateFlow<StudyPlanUiState> = mutableUiState.asStateFlow()

    private val repository = dataSource.asUniRepository(viewModelScope)
    private val sharedData = repository.engine
    private val dataRequests = listOf(UniDatasets.StudyPlan, UniDatasets.Career)
    private val dataObservation = sharedData.observeIn(viewModelScope, dataRequests, subscriptions = mutableUiState.subscriptionCount) { snapshot ->
        mutableUiState.value = mutableUiState.value.copy(loadState = mutableUiState.value.loadState.onRefresh(), errorMessage = null)
        try {
            val academic = snapshot.academicContract(requireStudyPlan = true, defaultTargetCfu = 180)
            val years = academic.studyYears
            val previous = mutableUiState.value
            val selectedYear = previous.years.getOrNull(previous.selectedYearIndex)?.yearNumber
            val restoredIndex = if (previous.selectedYearIndex == ALL_ACADEMIC_YEARS && years.size > 1) {
                ALL_ACADEMIC_YEARS
            } else if (previous.selectedYearIndex != ALL_ACADEMIC_YEARS && selectedYear != null) {
                years.indexOfFirst { it.yearNumber == selectedYear }.takeIf { it >= 0 }
                    ?: if (years.size > 1) ALL_ACADEMIC_YEARS else 0
            } else {
                if (years.size > 1) ALL_ACADEMIC_YEARS else 0
            }
            mutableUiState.value = mutableUiState.value.copy(
                years = years,
                selectedYearIndex = restoredIndex,
                acquiredCfu = academic.acquiredCfu,
                totalCfu = academic.targetCfu,
                completedCount = academic.completedStudyCoursesCount,
                totalCoursesCount = academic.totalStudyCoursesCount,
                loadState = if (years.isEmpty()) FeatureLoadState.Empty else FeatureLoadState.Content,
            )
            snapshot.throwIfFailed()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            mutableUiState.value = mutableUiState.value.copy(
                loadState = mutableUiState.value.loadState.onRefreshFailure(),
                errorMessage = error.userMessage { getString(Res.string.msg_impossibile_caricare_il_piano_di_studi) },
            )
        }
    }

    fun refresh(force: Boolean = false) {
        repository.didactics.refresh(dataRequests, force)
    }

    fun selectYear(index: Int) {
        val state = mutableUiState.value
        val maxIndex = (state.years.size - 1).coerceAtLeast(0)
        mutableUiState.value = state.copy(
            selectedYearIndex = if (index == ALL_ACADEMIC_YEARS && state.years.size > 1) {
                ALL_ACADEMIC_YEARS
            } else {
                index.coerceIn(0, maxIndex)
            },
        )
    }

    fun selectStatusFilter(status: CourseStatus?) {
        mutableUiState.value = mutableUiState.value.copy(statusFilter = status)
    }
}
