package com.anto426.uniapp.didactics.presentation

import androidx.lifecycle.ViewModel
import com.anto426.uniapp.model.didactics.ExamSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ExamsUiState(
    val selectedTab: Int = 0,
    val exams: List<ExamSession> = emptyList(),
) {
    val bookableCount: Int get() = exams.count { !it.isBooked }
    val bookedCount: Int get() = exams.count { it.isBooked }
    val visibleExams: List<ExamSession> get() = exams.filter { it.isBooked == (selectedTab == 1) }
}

class ExamsViewModel(exams: List<ExamSession>) : ViewModel() {
    private val mutableUiState = MutableStateFlow(ExamsUiState(exams = exams))
    val uiState: StateFlow<ExamsUiState> = mutableUiState.asStateFlow()

    fun selectTab(index: Int) {
        mutableUiState.value = mutableUiState.value.copy(selectedTab = index.coerceIn(0, 1))
    }
}
