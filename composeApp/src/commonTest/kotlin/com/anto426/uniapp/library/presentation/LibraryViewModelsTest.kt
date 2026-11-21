package com.anto426.uniapp.library.presentation

import com.anto426.uniapp.data.FakeUniAppDataSource
import com.anto426.uniapp.data.asUniRepository
import com.anto426.uniapp.feedback.runtime.AppToastKind
import com.anto426.uniapp.feedback.runtime.AppToastManager
import com.anto426.uniapp.library.model.*
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.testing.ResourceTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class LibraryViewModelsTest : ResourceTest() {
    @Test
    fun catalogSearchPopulatesResultsAndSupportsPagination() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val pages = mutableListOf<Int>()
            val source = object : FakeUniAppDataSource() {
                override suspend fun searchLibraryBooks(query: String, page: Int): LibraryCatalogPage {
                    pages += page
                    return LibraryCatalogPage(
                        query = query,
                        books = listOf(LibraryCatalogBook("MOL001", "Informatica $page", "Autore", "2024")),
                        page = page,
                        total = 25,
                    )
                }
            }
            val viewModel = LibraryViewModel(dataSource = source)
            viewModel.search("informatica", 1)
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.hasSearched)
            assertEquals(25, viewModel.uiState.value.totalResults)
            assertEquals(1, viewModel.uiState.value.currentPage)
            assertEquals("Informatica 1", viewModel.uiState.value.books.single().title)

            viewModel.search(page = 2)
            advanceUntilIdle()

            assertEquals(listOf(1, 2), pages)
            assertEquals("Informatica 2", viewModel.uiState.value.books.single().title)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun movementsTabLazyLoadsAndHandlesCancelConfirmation() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            var canceledToken: String? = null
            var movements = listOf(
                LibraryMovementItem(
                    id = "m1",
                    title = "Libro test",
                    detail = "Dettaglio",
                    canCancel = true,
                    actionToken = "tok-m1",
                ),
            )
            val source = object : FakeUniAppDataSource() {
                override suspend fun loadLibraryMovements(forceRefresh: Boolean) = movements
                override suspend fun cancelLibraryMovement(movementToken: String) {
                    canceledToken = movementToken
                    movements = emptyList()
                }
            }
            val toast = AppToastManager()
            val viewModel = LibraryViewModel(dataSource = source, toastSink = toast)
            advanceUntilIdle()

            // Initial tab 0 does not eagerly open a CAS library session.
            assertFalse(viewModel.uiState.value.hasLoadedMovements)

            viewModel.selectTab(1)
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.hasLoadedMovements)
            assertEquals(FeatureLoadState.Content, viewModel.uiState.value.movementsLoadState)
            assertEquals(1, viewModel.uiState.value.movements.size)

            viewModel.requestCancelMovement(movements.single())
            assertEquals("tok-m1", viewModel.uiState.value.pendingCancelMovement?.actionToken)

            viewModel.confirmCancelMovement()
            val toastMessage = toast.messages.first()
            advanceUntilIdle()

            assertEquals("tok-m1", canceledToken)
            assertEquals(FeatureLoadState.Empty, viewModel.uiState.value.movementsLoadState)
            assertEquals(AppToastKind.Success, toastMessage.kind)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun bookDetailPreparesAndConfirmsLoanDraft() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            var confirmedDraftId: String? = null
            val action = LibraryBookCopyAction("act-1", "Prestito", "MOLBU", "Campobasso", "RPE")
            val source = object : FakeUniAppDataSource() {
                override suspend fun loadLibraryBook(id: String) =
                    LibraryBookDetail(id, "Algoritmi", actions = listOf(action))
                override suspend fun prepareLibraryBookRequest(bookId: String, actionId: String) =
                    LibraryBookLoanDraft("draft-1", "Prestito presso Campobasso")
                override suspend fun confirmLibraryBookRequest(draftId: String): LibraryLoanSubmissionResult {
                    confirmedDraftId = draftId
                    return LibraryLoanSubmissionResult("Richiesta registrata", requiresVerification = true)
                }
            }
            val viewModel = LibraryBookDetailViewModel("MOL1", source)
            advanceUntilIdle()

            assertEquals("Algoritmi", viewModel.uiState.value.book?.title)
            viewModel.prepareRequest(action)
            advanceUntilIdle()
            assertEquals("draft-1", viewModel.uiState.value.pendingDraft?.draftId)

            viewModel.confirmDraft()
            advanceUntilIdle()
            assertEquals("draft-1", confirmedDraftId)
            assertNull(viewModel.uiState.value.pendingDraft)
            assertEquals("Richiesta registrata", viewModel.uiState.value.submissionResult?.description)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun rejectedSpaceBookingReloadsCalendarAndConfirmedMarksCompleted() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            var calendarLoads = 0
            var outcomeToReturn = LibrarySpaceBookingOutcome(confirmed = false, serverCode = 2)
            val source = object : FakeUniAppDataSource() {
                override suspend fun loadLibraryBookingOverview() = LibraryBookingOverview(
                    contextId = "ctx-1",
                    types = listOf(LibraryBookingOption("study", "Sala studio")),
                    bookings = emptyList(),
                )
                override suspend fun loadLibraryResourceSelection(contextId: String, typeId: String) =
                    LibrarySpaceSelection(
                        owners = listOf(LibraryBookingOption("MOLBU", "Campobasso")),
                        resources = listOf(LibraryBookingOption("res-1", "Sala 1")),
                    )
                override suspend fun loadLibraryCalendar(
                    contextId: String,
                    resourceId: String,
                    fromDate: LocalDate?,
                    toDate: LocalDate?,
                ): LibrarySpaceCalendar {
                    calendarLoads += 1
                    return LibrarySpaceCalendar(
                        calendarId = "cal-$calendarLoads",
                        resourceId = resourceId,
                        slots = listOf(LibrarySpaceSlot("s-$calendarLoads", "1", "09:00 – 13:00", "Mattina", "20/11/2026", "5 posti", false)),
                    )
                }
                override suspend fun bookLibrarySpace(
                    calendarId: String,
                    slotIds: List<String>,
                    note: String,
                    deviceAddresses: Map<String, String>,
                ) = outcomeToReturn
            }
            val toast = AppToastManager()
            val viewModel = LibrarySpaceBookingViewModel(source, toast)
            advanceUntilIdle()

            assertEquals(1, calendarLoads)
            viewModel.toggleSlot("s-1")
            viewModel.book()
            val warnToast = toast.messages.first()
            advanceUntilIdle()

            // Rejection reloads the calendar cleanly.
            assertEquals(AppToastKind.Warning, warnToast.kind)
            assertFalse(viewModel.uiState.value.bookingCompleted)
            assertEquals(2, calendarLoads)
            assertEquals("cal-2", viewModel.uiState.value.calendar?.calendarId)

            outcomeToReturn = LibrarySpaceBookingOutcome(confirmed = true, serverCode = 1)
            viewModel.toggleSlot("s-2")
            viewModel.book()
            val okToast = toast.messages.first()
            advanceUntilIdle()
            assertEquals(AppToastKind.Success, okToast.kind)
            assertTrue(viewModel.uiState.value.bookingCompleted)
        } finally {
            Dispatchers.resetMain()
        }
    }
}
