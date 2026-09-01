package com.anto426.uniapp.didactics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.UniAppDataSource
import com.anto426.uniapp.data.toGradeExams
import com.anto426.uniapp.data.toSimulationPresets
import com.anto426.liquidmonet.components.charts.LiquidChartEntry
import com.anto426.uniapp.model.didactics.GradeExam
import com.anto426.uniapp.model.didactics.GradeSimulationPreset
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.userMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GradesUiState(
    val selectedTab: Int = 0,
    val currentExams: List<GradeExam> = emptyList(),
    val simulationPresets: List<GradeSimulationPreset> = emptyList(),
    val simulatedGrades: List<Int> = emptyList(),
    val loadState: FeatureLoadState = FeatureLoadState.Loading,
    val errorMessage: String? = null,
) {
    val totalCurrentCfu: Int get() = currentExams.sumOf { it.cfu }
    private val currentWeightedSum: Int get() = currentExams.sumOf { it.grade * it.cfu }
    val currentAverage: Double
        get() = if (totalCurrentCfu == 0) 0.0 else currentWeightedSum.toDouble() / totalCurrentCfu

    val activeSimulatedCount: Int get() = simulatedGrades.count { it >= 18 }

    val activeSimulatedCfu: Int
        get() = simulationPresets.mapIndexedNotNull { index, preset ->
            if (simulatedGrades.getOrElse(index) { 0 } >= 18) preset.cfu else null
        }.sum()

    val averageSimulatedGrade: Double
        get() {
            val active = simulatedGrades.filter { it >= 18 }
            return if (active.isEmpty()) 0.0 else active.average()
        }

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

    val deltaAverage: Double get() = projectedAverage - currentAverage
    val currentGraduationBase: Double get() = (currentAverage * 110.0) / 30.0
    val projectedGraduationBase: Double get() = (projectedAverage * 110.0) / 30.0
    val totalProjectedCfu: Int get() = totalCurrentCfu + activeSimulatedCfu
    val careerProgress: Float get() = (totalProjectedCfu.toFloat() / 180f).coerceIn(0f, 1f)

    val gradeMin: Float
        get() {
            val allGrades = currentExams.map { it.grade.toFloat() } + simulatedGrades.filter { it >= 18 }.map { it.toFloat() }
            return ((allGrades.minOrNull() ?: 18f) - 1f).coerceAtLeast(17f)
        }

    val gradeMax: Float
        get() {
            val allGrades = currentExams.map { it.grade.toFloat() } + simulatedGrades.filter { it >= 18 }.map { it.toFloat() }
            return ((allGrades.maxOrNull() ?: 30f) + 1.2f).coerceAtMost(32f)
        }

    val cfuMax: Float
        get() {
            val allCfu = currentExams.map { it.cfu } + simulationPresets.map { it.cfu }
            return ((allCfu.maxOrNull() ?: 6) * 1.25f).coerceAtLeast(1f)
        }

    val singleGradeEntries: List<LiquidChartEntry>
        get() {
            val list = mutableListOf<LiquidChartEntry>()
            var runningWeightedSum = 0
            var runningCfu = 0
            currentExams.forEach { exam ->
                runningWeightedSum += exam.grade * exam.cfu
                runningCfu += exam.cfu
                val weighted = if (runningCfu > 0) runningWeightedSum.toFloat() / runningCfu else 0f
                list += LiquidChartEntry(
                    label = exam.name,
                    value = exam.grade.toFloat(),
                    secondaryValue = weighted,
                )
            }
            simulationPresets.forEachIndexed { index, preset ->
                val grade = simulatedGrades.getOrElse(index) { 0 }
                if (grade >= 18) {
                    runningWeightedSum += grade * preset.cfu
                    runningCfu += preset.cfu
                    val weighted = if (runningCfu > 0) runningWeightedSum.toFloat() / runningCfu else 0f
                    list += LiquidChartEntry(
                        label = "🔮 " + preset.name,
                        value = grade.toFloat(),
                        secondaryValue = weighted,
                    )
                }
            }
            return list
        }

    val weightedAverageEntries: List<LiquidChartEntry>
        get() {
            val list = mutableListOf<LiquidChartEntry>()
            var runningWeightedSum = 0
            var runningCfu = 0
            currentExams.forEach { exam ->
                runningWeightedSum += exam.grade * exam.cfu
                runningCfu += exam.cfu
                val avg = if (runningCfu > 0) runningWeightedSum.toFloat() / runningCfu else 0f
                list += LiquidChartEntry(
                    label = exam.name,
                    value = avg,
                )
            }
            simulationPresets.forEachIndexed { index, preset ->
                val grade = simulatedGrades.getOrElse(index) { 0 }
                if (grade >= 18) {
                    runningWeightedSum += grade * preset.cfu
                    runningCfu += preset.cfu
                    val avg = if (runningCfu > 0) runningWeightedSum.toFloat() / runningCfu else 0f
                    list += LiquidChartEntry(
                        label = "🔮 " + preset.name,
                        value = avg,
                    )
                }
            }
            return list
        }

    val arithmeticAverageEntries: List<LiquidChartEntry>
        get() {
            val list = mutableListOf<LiquidChartEntry>()
            var runningSum = 0
            var count = 0
            currentExams.forEach { exam ->
                runningSum += exam.grade
                count++
                val avg = runningSum.toFloat() / count
                list += LiquidChartEntry(
                    label = exam.name,
                    value = avg,
                )
            }
            simulationPresets.forEachIndexed { index, preset ->
                val grade = simulatedGrades.getOrElse(index) { 0 }
                if (grade >= 18) {
                    runningSum += grade
                    count++
                    val avg = runningSum.toFloat() / count
                    list += LiquidChartEntry(
                        label = "🔮 " + preset.name,
                        value = avg,
                    )
                }
            }
            return list
        }

    val cfuEntries: List<LiquidChartEntry>
        get() {
            val list = mutableListOf<LiquidChartEntry>()
            currentExams.forEach { exam ->
                list += LiquidChartEntry(
                    label = exam.name,
                    value = exam.cfu.toFloat(),
                )
            }
            simulationPresets.forEachIndexed { index, preset ->
                val grade = simulatedGrades.getOrElse(index) { 0 }
                if (grade >= 18) {
                    list += LiquidChartEntry(
                        label = "🔮 " + preset.name,
                        value = preset.cfu.toFloat(),
                    )
                }
            }
            return list
        }
}

class GradesViewModel(
    private val dataSource: UniAppDataSource,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(GradesUiState())
    val uiState: StateFlow<GradesUiState> = mutableUiState.asStateFlow()

    init { refresh() }

    fun refresh(force: Boolean = false) {
        viewModelScope.launch {
            mutableUiState.value = mutableUiState.value.copy(loadState = FeatureLoadState.Loading, errorMessage = null)
            try {
                val career = dataSource.loadCareer(force)
                val rounds = dataSource.loadExamRounds(force)
                val currentExams = career.toGradeExams()
                val presets = rounds.toSimulationPresets()
                mutableUiState.value = mutableUiState.value.copy(
                    currentExams = currentExams,
                    simulationPresets = presets,
                    simulatedGrades = presets.map { it.initialGrade.coerceIn(0, 30) },
                    loadState = if (currentExams.isEmpty() && presets.isEmpty()) FeatureLoadState.Empty else FeatureLoadState.Content,
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                mutableUiState.value = mutableUiState.value.copy(
                    loadState = FeatureLoadState.Error,
                    errorMessage = error.userMessage("Impossibile caricare voti e simulazione."),
                )
            }
        }
    }

    fun selectTab(index: Int) {
        mutableUiState.value = mutableUiState.value.copy(selectedTab = index.coerceIn(0, 2))
    }

    fun updateSimulatedGrade(index: Int, grade: Int) {
        val current = mutableUiState.value
        if (index !in current.simulatedGrades.indices) return
        mutableUiState.value =
            current.copy(
                simulatedGrades = current.simulatedGrades.toMutableList().apply { this[index] = grade.coerceIn(0, 30) },
            )
    }

    fun setAllSimulatedGrades(grade: Int) {
        val current = mutableUiState.value
        val safeGrade = grade.coerceIn(0, 30)
        mutableUiState.value = current.copy(
            simulatedGrades = List(current.simulationPresets.size) { safeGrade },
        )
    }

    fun resetSimulation() {
        setAllSimulatedGrades(0)
    }
}
