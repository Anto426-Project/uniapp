package com.anto426.uniapp.didactics.presentation

import com.anto426.uniapp.model.didactics.GradeExam
import com.anto426.uniapp.model.didactics.SimulationItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CareerAnalyticsTest {
    private val state = GradesUiState(
        currentExams = listOf(GradeExam("Recorded", 30, 12)),
        officialAverage = 28.5,
        officialDegreeBase = 104.5,
        degreeCfuTarget = 120,
        simulationItems = listOf(
            SimulationItem("active", "Future", 6, 24, isEnabled = true),
            SimulationItem("inactive", "Disabled", 9, 18),
        ),
    )

    @Test
    fun recordedChartsAreIndependentOfSimulationSelections() {
        val recorded = state.chartState(projected = false)
        assertEquals(1, recorded.singleGradeEntries.size)
        assertEquals(28.5, recorded.currentAverage)
        assertEquals(104.5, recorded.currentGraduationBase)
        assertEquals(28.5f, recorded.weightedAverageEntries.last().value)
        assertEquals(28.5f, recorded.singleGradeEntries.last().secondaryValue)
        assertTrue(recorded.gradeMin <= 28.5f)
        assertTrue(recorded.gradeMax >= 30f)
        assertEquals(state.currentExams, recorded.currentExams)
        assertEquals(2, state.simulationItems.size)
        assertTrue(recorded.simulationItems.isEmpty())
    }

    @Test
    fun projectionIncludesOnlyEnabledExamsAndKeepsTheOfficialBaseline() {
        val projected = state.chartState(projected = true)
        assertEquals(listOf("Recorded", "(Sim.) Future"), projected.currentExams.map { it.name })
        assertEquals(27.0, projected.currentAverage)
        assertEquals(28.5f, projected.weightedAverageEntries.first().value)
        assertEquals(27.0f, projected.weightedAverageEntries.last().value)
        assertEquals(27.0f, projected.singleGradeEntries.last().secondaryValue)
        assertEquals(99.0, projected.currentGraduationBase)
        assertEquals(18, projected.totalCurrentCfu)
        assertEquals(120, projected.degreeCfuTarget)
        assertEquals(28.5, state.currentAverage)
    }

    @Test
    fun missingOfficialValuesUseRecordedGradesWithoutInventingAnAverage() {
        assertEquals(30.0, state.copy(officialAverage = null).currentAverage)
        assertEquals(0.0, GradesUiState().currentAverage)
        assertEquals(0.0, GradesUiState(officialAverage = Double.NaN).currentAverage)
    }
}
