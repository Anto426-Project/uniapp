package com.anto426.uniapp.didactics.presentation

import androidx.compose.runtime.Immutable
import com.anto426.uniapp.presentation.FeatureLoadState

@Immutable
data class StatisticsChartPoint(
    val label: String,
    val value: Float,
    val secondaryValue: Float? = null,
)

@Immutable
data class StatisticsGradeTier(
    val label: String,
    val examCount: Float,
)

@Immutable
data class StatisticsUiState(
    val selectedTabIndex: Int = 0,
    val weightedAverage: Float = 0f,
    val arithmeticAverage: Float = 0f,
    val degreeBase: Float = 0f,
    val gradeEntries: List<StatisticsChartPoint> = emptyList(),
    val weightedAverageEntries: List<StatisticsChartPoint> = emptyList(),
    val arithmeticAverageEntries: List<StatisticsChartPoint> = emptyList(),
    val cfuEntries: List<StatisticsChartPoint> = emptyList(),
    val gradeTiers: List<StatisticsGradeTier> = emptyList(),
    val gradeMin: Float = 18f,
    val gradeMax: Float = 31f,
    val cfuMax: Float = 1f,
    val highestGradeLabel: String = "—",
    val highestGradeCourses: String = "Nessun esame",
    val recentTrend: Float = 0f,
    val totalCfu: Int = 0,
    val averageCfuPerExam: Float = 0f,
    val loadState: FeatureLoadState = FeatureLoadState.Loading,
    val errorMessage: String? = null,
) {
    val totalExams: Int get() = gradeTiers.sumOf { it.examCount.toInt() }
    val weightingSpread: Float get() = (weightedAverage - arithmeticAverage).let { kotlin.math.round(it * 100f) / 100f }
    val careerProgress: Float get() = (totalCfu / 180f).coerceIn(0f, 1f)
    val dominantTier: StatisticsGradeTier? get() = gradeTiers.maxByOrNull { it.examCount }
}
