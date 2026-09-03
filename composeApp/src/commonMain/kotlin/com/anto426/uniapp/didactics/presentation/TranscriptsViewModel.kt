package com.anto426.uniapp.didactics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.UniAppDataSource
import com.anto426.uniapp.data.toExamRecords
import com.anto426.uniapp.model.didactics.ExamRecord
import com.anto426.uniapp.presentation.FeatureLoadState
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
) {
    val availableYears: List<Int>
        get() {
            val maxYear = maxOf(3, examsByYear.keys.maxOrNull() ?: 3)
            return (1..maxYear).toList()
        }

    val displayedYears: List<Int>
        get() = listOf(selectedYear)
}

class TranscriptsViewModel(private val dataSource: UniAppDataSource) : ViewModel() {
    private val mutableUiState = MutableStateFlow(TranscriptsUiState())
    val uiState: StateFlow<TranscriptsUiState> = mutableUiState.asStateFlow()

    init { refresh() }

    fun refresh(force: Boolean = false) {
        viewModelScope.launch {
            mutableUiState.value = mutableUiState.value.copy(loadState = FeatureLoadState.Loading, errorMessage = null)
            try {
                val exams = dataSource.loadCareer(force).toExamRecords()
                val examsByYear = exams.groupBy { it.year }
                val maxYear = maxOf(3, examsByYear.keys.maxOrNull() ?: 3)
                val currentYear = mutableUiState.value.selectedYear
                val selectedYear = if (currentYear in 1..maxYear) currentYear else 1
                mutableUiState.value = mutableUiState.value.copy(
                    examsByYear = examsByYear,
                    selectedYear = selectedYear,
                    loadState = if (exams.isEmpty()) FeatureLoadState.Empty else FeatureLoadState.Content,
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                mutableUiState.value = mutableUiState.value.copy(
                    loadState = FeatureLoadState.Error,
                    errorMessage = error.userMessage("Impossibile caricare il libretto."),
                )
            }
        }
    }

    fun selectYear(year: Int) {
        val available = mutableUiState.value.availableYears
        mutableUiState.value = mutableUiState.value.copy(
            selectedYear = if (year in available) year else 1,
        )
    }
}
