package com.anto426.uniapp.didactics.presentation

import androidx.lifecycle.ViewModelStore
import com.anto426.uniapp.data.FakeUniAppDataSource
import com.anto426.uniapp.data.stableUiId
import com.anto426.uniapp.feedback.runtime.AppToastSink
import com.anto426.uniapp.testing.ResourceTest
import com.anto426.unisdk.backend.model.ExamRoundData
import io.ktor.http.Url
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import kotlinx.datetime.LocalDate
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class ExamActionsTest : ResourceTest() {
    private fun round(date: String = "20/09/2026 09:30", booked: Boolean = true) =
        ExamRoundData("Analisi & geometria", date, "Aula A & B", 20, true, booked,
            appId = "1", adId = "42", cdsId = "10", notes = "Portare il documento")

    @Test
    fun calendarDraftUsesUniversityTimeAndEncodesText() {
        val url = Url(assertNotNull(round().calendarUrlOrNull()))
        assertEquals("TEMPLATE", url.parameters["action"])
        assertEquals("20260920T073000Z/20260920T083000Z", url.parameters["dates"])
        assertEquals("Analisi & geometria", url.parameters["text"])
        assertEquals("Aula A & B", url.parameters["location"])
        assertEquals("Europe/Rome", url.parameters["ctz"])
        assertEquals("20260120T083000Z/20260120T093000Z", Url(round("20/01/2026 09:30").calendarUrlOrNull()!!).parameters["dates"])
        assertEquals("20260920T073000Z/20260920T083000Z", Url(round("2026-09-20T09:30:00+02:00").calendarUrlOrNull()!!).parameters["dates"])
    }

    @Test
    fun missingTimeCreatesAnAllDayDraftWhileInvalidDatesAndUnbookedRoundsAreRejected() {
        assertEquals("20261231/20270101", Url(round("31/12/2026").calendarUrlOrNull()!!).parameters["dates"])
        assertNull(round("data da definire").calendarUrlOrNull())
        assertNull(round("20/09/2026 99:99").calendarUrlOrNull())
        assertNull(round(booked = false).calendarUrlOrNull())
    }

    @Test
    fun bookingUsesSelectedCourseAndARefreshCannotUnlockAnOngoingMutation() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val store = ViewModelStore()
        try {
            val rounds = listOf(round(booked = false), round(booked = false).copy(adId = "43", courseName = "Fisica"))
            val finish = CompletableDeferred<Unit>()
            val booked = mutableListOf<ExamRoundData>()
            val messages = mutableListOf<String>()
            val source = object : FakeUniAppDataSource() {
                override suspend fun loadExamRounds(forceRefresh: Boolean) = rounds
                override suspend fun bookExamRound(round: ExamRoundData): String {
                    booked += round
                    finish.await()
                    error("appello ancora non prenotabile")
                }
            }
            val vm = ExamsViewModel(source, AppToastSink { messages += it.text }, today = { LocalDate(2026, 9, 10) })
            store.put("exams", vm)
            runCurrent()
            assertEquals(2, vm.uiState.value.exams.map { it.id }.distinct().size)
            val selected = rounds[1].stableUiId()
            vm.toggleBooking(selected)
            vm.toggleBooking(selected)
            runCurrent()
            assertEquals(listOf(rounds[1]), booked)
            vm.refresh(force = true)
            runCurrent()
            assertEquals(selected, vm.uiState.value.mutatingExamId)
            vm.toggleBooking(rounds[0].stableUiId())
            runCurrent()
            assertEquals(1, booked.size)
            finish.complete(Unit)
            runCurrent()
            assertNull(vm.uiState.value.mutatingExamId)
            assertEquals(listOf("appello ancora non prenotabile"), messages)
            assertFalse(vm.uiState.value.exams.any { it.isBooked })
        } finally { store.clear(); Dispatchers.resetMain() }
    }

    @Test
    fun calendarActionOpensSelectedBookedExamAndReportsMissingDate() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val store = ViewModelStore()
        try {
            val booked = round()
            val undated = round("data da definire").copy(adId = "43")
            val errors = mutableListOf<String>()
            val source = object : FakeUniAppDataSource() {
                override suspend fun loadExamRounds(forceRefresh: Boolean) = listOf(booked, undated)
            }
            val vm = ExamsViewModel(source, AppToastSink { errors += it.text }, today = { LocalDate(2026, 9, 10) })
            store.put("exams", vm)
            runCurrent()
            val opened = mutableListOf<String>()
            vm.openCalendar(booked.stableUiId()) { opened += it }
            vm.openCalendar(undated.stableUiId()) { opened += it }
            runCurrent()
            assertEquals(listOf(booked.calendarUrlOrNull()), opened)
            assertEquals(1, errors.size)
        } finally { store.clear(); Dispatchers.resetMain() }
    }
}
