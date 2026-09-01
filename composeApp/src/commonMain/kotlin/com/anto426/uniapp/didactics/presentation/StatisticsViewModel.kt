package com.anto426.uniapp.didactics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.UniAppDataSource
import com.anto426.uniapp.data.numericGradeOrNull
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.userMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.round

class StatisticsViewModel(private val dataSource: UniAppDataSource) : ViewModel() {
    private val mutableUiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = mutableUiState.asStateFlow()

    init { refresh() }

    fun refresh(force: Boolean = false) {
        viewModelScope.launch {
            mutableUiState.value = mutableUiState.value.copy(loadState = FeatureLoadState.Loading, errorMessage = null)
            try {
                val career = dataSource.loadCareer(force)
                val graded = career.exams.mapNotNull { exam ->
                    exam.grade.numericGradeOrNull()?.let { grade -> Triple(exam.name, grade, exam.cfu ?: 0) }
                }
                var weightedSum = 0
                var cfuSum = 0
                var arithmeticSum = 0
                val weightedEntries = mutableListOf<StatisticsChartPoint>()
                val arithmeticEntries = mutableListOf<StatisticsChartPoint>()
                val gradeEntries = mutableListOf<StatisticsChartPoint>()
                val cfuEntries = mutableListOf<StatisticsChartPoint>()
                graded.forEachIndexed { index, (name, grade, cfu) ->
                    weightedSum += grade * cfu
                    cfuSum += cfu
                    arithmeticSum += grade
                    val weighted = if (cfuSum == 0) 0f else weightedSum.toFloat() / cfuSum
                    val arithmetic = arithmeticSum.toFloat() / (index + 1)
                    val label = name.take(16)
                    gradeEntries += StatisticsChartPoint(label, grade.toFloat(), weighted)
                    weightedEntries += StatisticsChartPoint(label, weighted)
                    arithmeticEntries += StatisticsChartPoint(label, arithmetic)
                    cfuEntries += StatisticsChartPoint(label, cfu.toFloat())
                }
                val grades = graded.map { it.second }
                val highestGrade = grades.maxOrNull()
                val highestCourses =
                    highestGrade?.let { highest ->
                        graded.filter { it.second == highest }.joinToString(limit = 2) { it.first }
                    }.orEmpty()
                val trendReference = weightedEntries.getOrNull((weightedEntries.lastIndex - 3).coerceAtLeast(0))?.value
                val recentTrend =
                    if (weightedEntries.size > 1 && trendReference != null) {
                        (weightedEntries.last().value - trendReference).rounded(2)
                    } else {
                        0f
                    }
                val gradeFloor = grades.minOrNull()?.minus(1)?.coerceAtLeast(17)?.toFloat() ?: 18f
                val gradeCeiling = grades.maxOrNull()?.plus(1)?.coerceAtMost(31)?.toFloat() ?: 31f
                val highestCfu = graded.maxOfOrNull { it.third } ?: 0
                val computedWeightedAvg = if (cfuSum > 0) (weightedSum.toFloat() / cfuSum).rounded(2) else 0f
                val computedArithmeticAvg = if (grades.isNotEmpty()) (arithmeticSum.toFloat() / graded.size).rounded(2) else 0f
                val rawAverage = career.average.decimalFloatOrZero()
                val finalWeightedAvg = if (rawAverage > 0f) rawAverage.rounded(2) else computedWeightedAvg
                val finalArithmeticAvg = computedArithmeticAvg
                val rawDegreeBase = career.degreeBase.decimalFloatOrZero()
                val finalDegreeBase = if (rawDegreeBase > 0f) rawDegreeBase.rounded(2) else if (finalWeightedAvg > 0f) ((finalWeightedAvg * 110f) / 30f).rounded(2) else 0f

                mutableUiState.value = mutableUiState.value.copy(
                    weightedAverage = finalWeightedAvg,
                    arithmeticAverage = finalArithmeticAvg,
                    degreeBase = finalDegreeBase,
                    gradeEntries = gradeEntries,
                    weightedAverageEntries = weightedEntries,
                    arithmeticAverageEntries = arithmeticEntries,
                    cfuEntries = cfuEntries,
                    gradeTiers = listOf(
                        StatisticsGradeTier("30 e 30L", grades.count { it == 30 }.toFloat()),
                        StatisticsGradeTier("27 - 29", grades.count { it in 27..29 }.toFloat()),
                        StatisticsGradeTier("24 - 26", grades.count { it in 24..26 }.toFloat()),
                        StatisticsGradeTier("18 - 23", grades.count { it in 18..23 }.toFloat()),
                    ),
                    gradeMin = gradeFloor,
                    gradeMax = gradeCeiling.coerceAtLeast(gradeFloor + 1f),
                    cfuMax = (highestCfu * 1.2f).coerceAtLeast(1f),
                    highestGradeLabel = highestGrade?.let { if (it == 30) "30" else it.toString() } ?: "—",
                    highestGradeCourses = highestCourses.ifBlank { "Nessun esame" },
                    recentTrend = recentTrend,
                    totalCfu = cfuSum,
                    averageCfuPerExam = if (graded.isEmpty()) 0f else (cfuSum.toFloat() / graded.size).rounded(1),
                    loadState = if (graded.isEmpty()) FeatureLoadState.Empty else FeatureLoadState.Content,
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                mutableUiState.value = mutableUiState.value.copy(
                    loadState = FeatureLoadState.Error,
                    errorMessage = error.userMessage("Impossibile caricare le statistiche."),
                )
            }
        }
    }

    fun selectTab(index: Int) {
        mutableUiState.value = mutableUiState.value.copy(selectedTabIndex = index.coerceIn(0, LAST_TAB_INDEX))
    }

    private companion object {
        const val LAST_TAB_INDEX = 2
    }
}

private fun String?.decimalFloatOrZero(): Float =
    this?.replace(',', '.')?.filter { it.isDigit() || it == '.' || it == '-' }?.toFloatOrNull() ?: 0f

private fun Float.rounded(decimals: Int): Float {
    val factor = when (decimals) {
        1 -> 10f
        2 -> 100f
        else -> 1f
    }
    return round(this * factor) / factor
}
