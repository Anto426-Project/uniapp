package com.anto426.uniapp.didactics.presentation

import androidx.compose.runtime.Immutable

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
    val weightedAverage: Float = 28.2f,
    val arithmeticAverage: Float = 28.3f,
    val degreeBase: Float = 103.4f,
    val gradeEntries: List<StatisticsChartPoint> = defaultGradeEntries,
    val weightedAverageEntries: List<StatisticsChartPoint> = defaultWeightedAverageEntries,
    val arithmeticAverageEntries: List<StatisticsChartPoint> = defaultArithmeticAverageEntries,
    val cfuEntries: List<StatisticsChartPoint> = defaultCfuEntries,
    val gradeTiers: List<StatisticsGradeTier> = defaultGradeTiers,
) {
    val totalExams: Int get() = gradeTiers.sumOf { it.examCount.toInt() }

    companion object {
        private val labels =
            listOf("Prog I", "Analisi I", "Architetture", "Fisica I", "Prog II", "Algoritmi", "Basi Dati", "Reti", "Sistemi Op.", "Ing. Software")
        private val grades = listOf(30f, 28f, 27f, 26f, 30f, 29f, 28f, 27f, 28f, 30f)
        private val weighted = listOf(30f, 28.85f, 28.30f, 27.76f, 28.20f, 28.33f, 28.28f, 28.12f, 28.11f, 28.20f)
        private val arithmetic = listOf(30f, 29f, 28.33f, 27.75f, 28.20f, 28.33f, 28.28f, 28.12f, 28.11f, 28.30f)

        val defaultGradeEntries = labels.indices.map { index ->
            StatisticsChartPoint(labels[index], grades[index], weighted[index])
        }
        val defaultWeightedAverageEntries = labels.indices.map { index ->
            StatisticsChartPoint(labels[index], weighted[index])
        }
        val defaultArithmeticAverageEntries = labels.indices.map { index ->
            StatisticsChartPoint(labels[index], arithmetic[index])
        }
        val defaultCfuEntries =
            listOf(
                StatisticsChartPoint("1°A - S1", 30f),
                StatisticsChartPoint("1°A - S2", 30f),
                StatisticsChartPoint("2°A - S1", 28f),
                StatisticsChartPoint("2°A - S2", 32f),
                StatisticsChartPoint("3°A - S1", 0f),
            )
        val defaultGradeTiers =
            listOf(
                StatisticsGradeTier("30 e 30L", 6f),
                StatisticsGradeTier("27 - 29", 5f),
                StatisticsGradeTier("24 - 26", 3f),
            )
    }
}
