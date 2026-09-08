package com.anto426.uniapp.didactics.presentation

import com.anto426.uniapp.data.FakeUniAppDataSource
import com.anto426.uniapp.data.toExamRecords
import com.anto426.uniapp.data.toStudyYears
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.unisdk.backend.model.CareerData
import com.anto426.unisdk.backend.model.CareerExamData
import com.anto426.unisdk.backend.model.StudyPlanCourseData
import com.anto426.unisdk.backend.model.StudyPlanData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AcademicYearsTest {
    @Test
    fun eachAccountUsesItsOwnPlanYearsIncludingYearsWithoutPassedExams() = runViewModelTest {
        for (years in listOf(listOf(1, 2), (1..6).toList(), listOf(2, 5))) {
            val source = AcademicSource(years)
            val transcripts = TranscriptsViewModel(source)
            val studyPlan = StudyPlanViewModel(source)
            advanceUntilIdle()

            assertEquals(years, transcripts.uiState.value.availableYears)
            assertEquals(years, studyPlan.uiState.value.years.map { it.yearNumber })
            transcripts.selectYear(years.last())
            assertEquals(listOf(years.last()), transcripts.uiState.value.displayedYears)
        }
    }

    @Test
    fun emptyAccountDoesNotInventYearTabs() = runViewModelTest {
        val source = AcademicSource(emptyList()).apply { career = career(emptyList()) }
        val transcripts = TranscriptsViewModel(source)
        val studyPlan = StudyPlanViewModel(source)
        advanceUntilIdle()

        assertTrue(transcripts.uiState.value.availableYears.isEmpty())
        assertTrue(transcripts.uiState.value.displayedYears.isEmpty())
        assertTrue(studyPlan.uiState.value.years.isEmpty())
        assertEquals(FeatureLoadState.Empty, transcripts.uiState.value.loadState)
    }

    @Test
    fun courseIdDeterminesYearEvenWhenExamIsPassedLateOrTitlesAreIdentical() {
        val plan = StudyPlanData(
            listOf(
                StudyPlanCourseData("Analisi", year = 1, adsceId = "A"),
                StudyPlanCourseData("Analisi", year = 4, adsceId = "B"),
            ),
        )
        val records = career(
            listOf(
                CareerExamData("Analisi", "30", "01/02/2026", 9, " A "),
                CareerExamData("Analisi", "28", "01/02/2023", 9, "B"),
            ),
        ).toExamRecords(plan)

        assertEquals(listOf(1, 4), records.map { it.year })
    }

    @Test
    fun missingIdsUseOnlyUnambiguousNamesAndConflictingIdsStayUnknown() {
        val plan = StudyPlanData(
            listOf(
                StudyPlanCourseData(" Analisi ", year = 2),
                StudyPlanCourseData("Fisica", year = 1),
                StudyPlanCourseData("Fisica", year = 3),
                StudyPlanCourseData("Chimica", year = 2, adsceId = "D"),
                StudyPlanCourseData("Laboratorio", year = 4, adsceId = "D"),
            ),
        )
        val records = career(
            listOf(
                CareerExamData("ANALISI", "30", "01/02/2026"),
                CareerExamData("Fisica", "28", "01/02/2026"),
                CareerExamData("Chimica", "27", "01/02/2026", adsceId = "D"),
                CareerExamData("Non presente", "26", "01/02/2026"),
            ),
        ).toExamRecords(plan)

        assertEquals(listOf(2, 0, 0, 0), records.map { it.year })
    }

    @Test
    fun invalidOrMissingCourseYearsArePreservedInAnUnknownGroupAfterKnownYears() {
        val plan = StudyPlanData(
            listOf(
                StudyPlanCourseData("Senza anno"),
                StudyPlanCourseData("Quinto", year = 5),
                StudyPlanCourseData("Zero", year = 0),
                StudyPlanCourseData("Secondo", year = 2),
                StudyPlanCourseData("Negativo", year = -1),
            ),
        )
        val years = plan.toStudyYears()

        assertEquals(listOf(2, 5, 0), years.map { it.yearNumber })
        assertEquals(5, years.sumOf { it.courses.size })
        assertEquals(listOf("Senza anno", "Zero", "Negativo"), years.last().courses.map { it.name })
    }

    @Test
    fun unavailableStudyPlanKeepsTranscriptVisibleWithoutInventingYears() = runViewModelTest {
        val source = object : FakeUniAppDataSource() {
            override suspend fun loadCareer(forceRefresh: Boolean) = career()
            override suspend fun loadStudyPlan(forceRefresh: Boolean): StudyPlanData = error("Offline")
        }
        val transcripts = TranscriptsViewModel(source)
        advanceUntilIdle()

        assertEquals(listOf(0), transcripts.uiState.value.availableYears)
        assertEquals(listOf(0), transcripts.uiState.value.displayedYears)
        assertEquals("Esame 2", transcripts.uiState.value.examsByYear.getValue(0).single().name)
        assertEquals(FeatureLoadState.Content, transcripts.uiState.value.loadState)
        assertNull(transcripts.uiState.value.errorMessage)
    }

    @Test
    fun studyPlanRefreshPreservesSelectedYearWhenEarlierYearsAreInserted() = runViewModelTest {
        val source = AcademicSource(listOf(2, 5))
        val viewModel = StudyPlanViewModel(source)
        advanceUntilIdle()
        viewModel.selectYear(1)

        source.plan = plan(listOf(1, 2, 5, 6))
        viewModel.refresh(force = true)
        advanceUntilIdle()
        assertEquals(5, viewModel.uiState.value.displayedYears.single().yearNumber)

        source.plan = plan(listOf(2))
        viewModel.refresh(force = true)
        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.displayedYears.single().yearNumber)
    }

    @Test
    fun transcriptRefreshPreservesAvailableSelectionAndResetsRemovedYears() = runViewModelTest {
        val source = AcademicSource(listOf(2, 5))
        val viewModel = TranscriptsViewModel(source)
        advanceUntilIdle()
        viewModel.selectYear(5)

        source.plan = plan(listOf(2, 5, 6))
        viewModel.refresh(force = true)
        advanceUntilIdle()
        assertEquals(5, viewModel.uiState.value.selectedYear)

        source.plan = plan(listOf(2, 6))
        viewModel.refresh(force = true)
        advanceUntilIdle()
        assertEquals(listOf(2, 6), viewModel.uiState.value.availableYears)
        assertEquals(2, viewModel.uiState.value.selectedYear)
        viewModel.selectYear(3)
        assertEquals(2, viewModel.uiState.value.selectedYear)
    }

    private class AcademicSource(years: List<Int>) : FakeUniAppDataSource() {
        var plan = plan(years)
        var career = career()
        override suspend fun loadCareer(forceRefresh: Boolean) = career
        override suspend fun loadStudyPlan(forceRefresh: Boolean) = plan
    }

    private fun runViewModelTest(block: suspend TestScope.() -> Unit) = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            block()
        } finally {
            Dispatchers.resetMain()
        }
    }

    private companion object {
        fun plan(years: List<Int>) = StudyPlanData(
            years.map { StudyPlanCourseData("Esame $it", year = it, adsceId = "course-$it") },
        )

        fun career(exams: List<CareerExamData> = listOf(CareerExamData("Esame 2", "30", "01/02/2026", 9, "course-2"))) =
            CareerData(average = "30", cfu = "9", year = "2", status = "Attivo", exams = exams)
    }
}
