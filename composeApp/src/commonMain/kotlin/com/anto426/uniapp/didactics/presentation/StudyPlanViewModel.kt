package com.anto426.uniapp.didactics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.UniAppDataSource
import com.anto426.uniapp.data.runtime.*
import com.anto426.uniapp.data.toStudyYears
import com.anto426.uniapp.model.didactics.CourseStatus
import com.anto426.uniapp.model.didactics.StudyYear
import com.anto426.uniapp.model.didactics.firstAcademicIntegerOrNull
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
    val selectedYearIndex: Int = 0,
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
        get() = listOfNotNull(years.getOrNull(selectedYearIndex))
}

class StudyPlanViewModel(private val dataSource: UniAppDataSource) : ViewModel() {
    private val mutableUiState = MutableStateFlow(StudyPlanUiState())
    val uiState: StateFlow<StudyPlanUiState> = mutableUiState.asStateFlow()

    private val sharedData = dataSource.sharedData(viewModelScope)
    private val dataRequests = listOf(UniAppDataRequests.StudyPlan, UniAppDataRequests.Career)
    private val dataObservation = sharedData.observeIn(viewModelScope, dataRequests, subscriptions = mutableUiState.subscriptionCount) { snapshot ->
        mutableUiState.value = mutableUiState.value.copy(loadState = mutableUiState.value.loadState.onRefresh(), errorMessage = null)
        try {
            val plan = snapshot.require(UniAppDataRequests.StudyPlan)
            val career = runCatching { snapshot.require(UniAppDataRequests.Career) }.getOrNull()
            val years = plan.toStudyYears(career)
            val allCourses = years.flatMap { it.courses }
            val completedCourses = allCourses.filter { it.status == CourseStatus.COMPLETED }
            val completedCfu = completedCourses.sumOf { it.cfu.firstAcademicIntegerOrNull() ?: 0 }
            val planTotalCfu = allCourses.sumOf { it.cfu.firstAcademicIntegerOrNull() ?: 0 }
            val effectiveAcquiredCfu = career?.cfu?.firstAcademicIntegerOrNull()?.takeIf { it > 0 } ?: completedCfu
            val effectiveTargetCfu = (career?.cfuTarget ?: planTotalCfu).coerceAtLeast(effectiveAcquiredCfu)

            val previous = mutableUiState.value
            val selectedYear = previous.years.getOrNull(previous.selectedYearIndex)?.yearNumber
            mutableUiState.value = mutableUiState.value.copy(
                years = years,
                selectedYearIndex = years.indexOfFirst { it.yearNumber == selectedYear }.coerceAtLeast(0),
                acquiredCfu = effectiveAcquiredCfu,
                totalCfu = effectiveTargetCfu,
                completedCount = completedCourses.size,
                totalCoursesCount = allCourses.size,
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
        sharedData.refresh(dataRequests, force)
    }

    fun selectYear(index: Int) {
        val maxIndex = (mutableUiState.value.years.size - 1).coerceAtLeast(0)
        mutableUiState.value = mutableUiState.value.copy(
            selectedYearIndex = index.coerceIn(0, maxIndex),
        )
    }

    fun selectStatusFilter(status: CourseStatus?) {
        mutableUiState.value = mutableUiState.value.copy(statusFilter = status)
    }
}
