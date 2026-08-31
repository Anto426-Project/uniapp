package com.anto426.uniapp.didactics.presentation

import androidx.lifecycle.ViewModel
import com.anto426.uniapp.model.didactics.StudyYear
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class StudyPlanUiState(
    val selectedYear: Int = 0,
    val years: List<StudyYear> = emptyList(),
) {
    val displayedYears: List<StudyYear>
        get() = if (selectedYear == 0) years else years.filter { it.yearNumber == selectedYear }
}

class StudyPlanViewModel(years: List<StudyYear>) : ViewModel() {
    private val mutableUiState = MutableStateFlow(StudyPlanUiState(years = years))
    val uiState: StateFlow<StudyPlanUiState> = mutableUiState.asStateFlow()

    fun selectYear(year: Int) {
        mutableUiState.value = mutableUiState.value.copy(selectedYear = year.coerceIn(0, 3))
    }
}
