package com.anto426.uniapp.didactics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.UniAppDataSource
import com.anto426.uniapp.data.toExamRecords
import com.anto426.uniapp.model.didactics.ExamRecord
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.onRefresh
import com.anto426.uniapp.presentation.onRefreshFailure
import com.anto426.uniapp.presentation.userMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TranscriptsUiState(
    val selectedYear: Int = 1,
    val examsByYear: Map<Int, List<ExamRecord>> = emptyMap(),
    val loadState: FeatureLoadState = FeatureLoadState.Loading,
    val errorMessage: String? = null,
    val studyPlanYears: List<Int> = emptyList(),
) {
    val availableYears: List<Int>
        get() = (studyPlanYears + examsByYear.keys).distinct()
            .sortedWith(compareBy<Int> { it == 0 }.thenBy { it })

    val displayedYears: List<Int>
        get() = listOfNotNull(selectedYear.takeIf { it in availableYears })
}

class TranscriptsViewModel(private val dataSource: UniAppDataSource) : ViewModel() {
    private val mutableUiState = MutableStateFlow(TranscriptsUiState())
    val uiState: StateFlow<TranscriptsUiState> = mutableUiState.asStateFlow()

    init { refresh() }

    fun refresh(force: Boolean = false) {
        viewModelScope.launch {
            mutableUiState.value = mutableUiState.value.copy(loadState = mutableUiState.value.loadState.onRefresh(), errorMessage = null)
            try {
                val career = dataSource.loadCareer(force)
                val plan = try {
                    dataSource.loadStudyPlan(force)
                } catch (error: CancellationException) {
                    throw error
                } catch (_: Exception) {
                    null // The transcript remains available when its year metadata cannot be loaded.
                }
                val exams = career.toExamRecords(plan)
                val examsByYear = exams.groupBy { it.year }
                val planYears = plan?.courses.orEmpty().mapNotNull { it.year?.takeIf { year -> year > 0 } }.distinct()
                val availableYears = (planYears + examsByYear.keys).distinct()
                    .sortedWith(compareBy<Int> { it == 0 }.thenBy { it })
                val currentYear = mutableUiState.value.selectedYear
                val selectedYear = currentYear.takeIf { it in availableYears } ?: availableYears.firstOrNull() ?: 0
                mutableUiState.value = mutableUiState.value.copy(
                    examsByYear = examsByYear,
                    selectedYear = selectedYear,
                    studyPlanYears = planYears,
                    loadState = if (exams.isEmpty()) FeatureLoadState.Empty else FeatureLoadState.Content,
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                mutableUiState.value = mutableUiState.value.copy(
                    loadState = mutableUiState.value.loadState.onRefreshFailure(),
                    errorMessage = error.userMessage("Impossibile caricare il libretto."),
                )
            }
        }
    }

    fun selectYear(year: Int) {
        val available = mutableUiState.value.availableYears
        mutableUiState.value = mutableUiState.value.copy(
            selectedYear = if (year in available) year else available.firstOrNull() ?: 0,
        )
    }
}
