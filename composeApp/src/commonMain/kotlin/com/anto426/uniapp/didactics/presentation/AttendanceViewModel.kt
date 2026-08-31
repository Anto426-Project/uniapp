package com.anto426.uniapp.didactics.presentation

import androidx.lifecycle.ViewModel
import com.anto426.uniapp.model.didactics.AttendanceData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AttendanceUiState(val records: List<AttendanceData> = emptyList())

class AttendanceViewModel(records: List<AttendanceData>) : ViewModel() {
    private val mutableUiState = MutableStateFlow(AttendanceUiState(records))
    val uiState: StateFlow<AttendanceUiState> = mutableUiState.asStateFlow()
}
