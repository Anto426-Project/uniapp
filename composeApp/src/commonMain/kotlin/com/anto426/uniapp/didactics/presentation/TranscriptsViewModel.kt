package com.anto426.uniapp.didactics.presentation

import androidx.lifecycle.ViewModel
import com.anto426.uniapp.model.didactics.ExamRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TranscriptsUiState(
    val selectedYear: Int = 0,
    val examsByYear: Map<Int, List<ExamRecord>> = emptyMap(),
) {
    val displayedYears: List<Int>
        get() = if (selectedYear == 0) examsByYear.keys.sorted() else listOf(selectedYear)
}

class TranscriptsViewModel(exams: List<ExamRecord>) : ViewModel() {
    private val mutableUiState = MutableStateFlow(TranscriptsUiState(examsByYear = exams.groupBy { it.year }))
    val uiState: StateFlow<TranscriptsUiState> = mutableUiState.asStateFlow()

    fun selectYear(year: Int) {
        mutableUiState.value = mutableUiState.value.copy(selectedYear = year.coerceIn(0, 3))
    }
}
