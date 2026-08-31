package com.anto426.uniapp.didactics.presentation

import androidx.lifecycle.ViewModel
import com.anto426.uniapp.model.didactics.PastExam
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ExamsHistoryUiState(val exams: List<PastExam> = emptyList())

class ExamsHistoryViewModel(exams: List<PastExam>) : ViewModel() {
    private val mutableUiState = MutableStateFlow(ExamsHistoryUiState(exams))
    val uiState: StateFlow<ExamsHistoryUiState> = mutableUiState.asStateFlow()
}
