package com.anto426.uniapp.didactics.presentation

import com.anto426.uniapp.data.contract.GradeExam
import com.anto426.uniapp.data.contract.SimulationItem
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
        assertEquals(30f, recorded.singleGradeEntries.last().value)
        assertTrue(recorded.gradeMin <= 28.5f)
        assertTrue(recorded.gradeMax >= 30f)
        assertEquals(state.currentExams, recorded.currentExams)
        assertEquals(2, state.simulationItems.size)
        assertTrue(recorded.simulationItems.isEmpty())
    }

    @Test
    fun projectionIncludesOnlyEnabledExamsAndKeepsTheOfficialBaseline() {
        val projected = state.chartState(projected = true)
        assertEquals(listOf("Recorded", "Future"), projected.currentExams.map { it.name })
        assertEquals(27.0, projected.currentAverage)
        assertEquals(28.5f, projected.weightedAverageEntries.first().value)
        assertEquals(27.0f, projected.weightedAverageEntries.last().value)
        assertEquals(24f, projected.singleGradeEntries.last().value)
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

    @Test
    fun simulationImpactAndGraduationScenariosComputeConsistently() {
        val item = SimulationItem("future", "Future", cfu = 6, grade = 24, isEnabled = true)
        assertEquals(-1.5, state.simulationItemImpact(item), 0.001)

        val scenarios = state.graduationScenarios()
        assertEquals(4, scenarios.size)
        assertEquals(null, scenarios.first().averageGrade)
        assertEquals(listOf(24, 27, 30), scenarios.drop(1).map { it.averageGrade })
        assertTrue(scenarios.last().projectedScore >= scenarios[1].projectedScore)

        val progression = state.chartState(projected = true).examProgression
        assertEquals(2, progression.size)
        assertEquals(false, progression[0].isSimulated)
        assertEquals(true, progression[1].isSimulated)
        assertEquals(null, progression[0].deltaFromPrevious)
        assertEquals(-1.5, progression[1].deltaFromPrevious!!, 0.001)

        val impacts = state.chartState(projected = true).examWeightImpacts
        assertEquals(2, impacts.size)
        assertTrue(impacts.first().impactOnAverage > impacts.last().impactOnAverage)
        assertEquals(1, state.examsInTier("30 e 30L").size)
        assertEquals(0, state.examsInTier("18 - 23").size)
    }
}
