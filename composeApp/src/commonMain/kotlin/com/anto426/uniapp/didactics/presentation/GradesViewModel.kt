package com.anto426.uniapp.didactics.presentation

import androidx.lifecycle.ViewModel
import com.anto426.uniapp.model.didactics.GradeExam
import com.anto426.uniapp.model.didactics.GradeSimulationPreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class GradesUiState(
    val selectedTab: Int = 0,
    val currentExams: List<GradeExam> = emptyList(),
    val simulationPresets: List<GradeSimulationPreset> = emptyList(),
    val simulatedGrades: List<Int> = emptyList(),
) {
    val totalCurrentCfu: Int get() = currentExams.sumOf { it.cfu }
    private val currentWeightedSum: Int get() = currentExams.sumOf { it.grade * it.cfu }
    val currentAverage: Double
        get() = if (totalCurrentCfu == 0) 0.0 else currentWeightedSum.toDouble() / totalCurrentCfu
    val projectedAverage: Double
        get() {
            val included =
                simulationPresets.mapIndexedNotNull { index, preset ->
                    simulatedGrades.getOrNull(index)?.takeIf { it >= 18 }?.let { it to preset.cfu }
                }
            val simulatedCfu = included.sumOf { it.second }
            val totalCfu = totalCurrentCfu + simulatedCfu
            return if (totalCfu == 0) currentAverage
            else (currentWeightedSum + included.sumOf { it.first * it.second }).toDouble() / totalCfu
        }
}

class GradesViewModel(
    currentExams: List<GradeExam>,
    simulationPresets: List<GradeSimulationPreset>,
) : ViewModel() {
    private val mutableUiState =
        MutableStateFlow(
            GradesUiState(
                currentExams = currentExams,
                simulationPresets = simulationPresets,
                simulatedGrades = simulationPresets.map { it.initialGrade.coerceIn(0, 30) },
            ),
        )
    val uiState: StateFlow<GradesUiState> = mutableUiState.asStateFlow()

    fun selectTab(index: Int) {
        mutableUiState.value = mutableUiState.value.copy(selectedTab = index.coerceIn(0, 1))
    }

    fun updateSimulatedGrade(index: Int, grade: Int) {
        val current = mutableUiState.value
        if (index !in current.simulatedGrades.indices) return
        mutableUiState.value =
            current.copy(
                simulatedGrades = current.simulatedGrades.toMutableList().apply { this[index] = grade.coerceIn(0, 30) },
            )
    }
}
