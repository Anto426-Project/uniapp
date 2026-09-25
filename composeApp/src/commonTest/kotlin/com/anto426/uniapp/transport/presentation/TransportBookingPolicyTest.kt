package com.anto426.uniapp.transport.presentation

import com.anto426.uniapp.data.FakeUniAppDataSource
import com.anto426.uniapp.feedback.runtime.AppToastKind
import com.anto426.uniapp.feedback.runtime.AppToastMessage
import com.anto426.uniapp.feedback.runtime.AppToastSink
import com.anto426.unisdk.transport.TransportActionResult
import com.anto426.unisdk.transport.TransportBookingRequest
import com.anto426.unisdk.transport.TransportData
import com.anto426.unisdk.transport.TransportDirection
import com.anto426.unisdk.transport.TransportRouteData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class TransportBookingPolicyTest : com.anto426.uniapp.testing.ResourceTest() {
    private val friday = LocalDate(2026, 9, 25)

    @Test
    fun onlyWeekdaysFromTomorrowThroughDayFifteenCanBeBooked() {
        assertFalse(isTransportBookingDateAllowed(friday, friday))
        assertFalse(isTransportBookingDateAllowed(LocalDate(2026, 9, 26), friday))
        assertFalse(isTransportBookingDateAllowed(LocalDate(2026, 9, 27), friday))
        assertTrue(isTransportBookingDateAllowed(LocalDate(2026, 9, 28), friday))
        assertTrue(isTransportBookingDateAllowed(LocalDate(2026, 10, 9), friday))
        assertFalse(isTransportBookingDateAllowed(LocalDate(2026, 10, 10), friday))
        assertFalse(isTransportBookingDateAllowed(LocalDate(2026, 10, 12), friday))
        val tuesday = LocalDate(2026, 9, 22)
        assertTrue(isTransportBookingDateAllowed(LocalDate(2026, 10, 7), tuesday))
        assertFalse(isTransportBookingDateAllowed(LocalDate(2026, 10, 8), tuesday))
    }

    @Test
    fun roundTripsExpandIntoOneOutboundAndOneReturnPerDistinctDay() {
        val monday = LocalDate(2026, 9, 28)
        val tuesday = LocalDate(2026, 9, 29)
        assertEquals(
            listOf(
                TransportBookingRequest("A01", monday, TransportDirection.OUTBOUND),
                TransportBookingRequest("A01", monday, TransportDirection.RETURN),
                TransportBookingRequest("A01", tuesday, TransportDirection.OUTBOUND),
                TransportBookingRequest("A01", tuesday, TransportDirection.RETURN),
            ),
            transportBookingRequests("A01", listOf(tuesday, monday, monday), TransportDirection.ROUND_TRIP),
        )
    }

    @Test
    fun aFailedRideDoesNotAbortTheRemainingIndividualBookings() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val calls = mutableListOf<TransportBookingRequest>()
            val messages = mutableListOf<AppToastMessage>()
            val source = object : FakeUniAppDataSource() {
                override suspend fun loadTransportData(forceRefresh: Boolean) = TransportData(
                    routeLabel = "Campus A/R", routeCode = "A01", availableRoutes = listOf(TransportRouteData("A01", "Campus A/R")),
                    bookings = emptyList(), totalCount = 0,
                )
                override suspend fun bookTransport(request: TransportBookingRequest): TransportActionResult {
                    calls += request
                    if (request.date == LocalDate(2026, 9, 28) && request.direction == TransportDirection.RETURN) {
                        error("Portale temporaneamente non disponibile")
                    }
                    return TransportActionResult.Completed
                }
            }
            val model = TransportBookingViewModel(source, AppToastSink { message -> messages += message }, today = { friday })
            backgroundScope.launch { model.uiState.collect() }
            advanceUntilIdle()
            assertTrue(model.uiState.value.routes.isNotEmpty())
            model.book(listOf(LocalDate(2026, 9, 28), LocalDate(2026, 9, 29)), TransportDirection.ROUND_TRIP)
            advanceUntilIdle()

            assertEquals(
                transportBookingRequests(
                    "A01", listOf(LocalDate(2026, 9, 28), LocalDate(2026, 9, 29)), TransportDirection.ROUND_TRIP,
                ),
                calls,
            )
            assertFalse(model.uiState.value.bookedSuccessfully)
            assertEquals(AppToastKind.Warning, messages.last().kind)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun todayAndWeekendsNeverReachThePortalEvenFromAnOldScreen() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val calls = mutableListOf<TransportBookingRequest>()
            val source = object : FakeUniAppDataSource() {
                override suspend fun loadTransportData(forceRefresh: Boolean) = TransportData(
                    routeLabel = "Campus A/R", routeCode = "A01", availableRoutes = listOf(TransportRouteData("A01", "Campus A/R")),
                    bookings = emptyList(), totalCount = 0,
                )
                override suspend fun bookTransport(request: TransportBookingRequest): TransportActionResult {
                    calls += request
                    return TransportActionResult.Completed
                }
            }
            val model = TransportBookingViewModel(source, today = { friday })
            backgroundScope.launch { model.uiState.collect() }
            advanceUntilIdle()
            assertTrue(model.uiState.value.routes.isNotEmpty())
            assertEquals("Campus A/R", model.uiState.value.selectedRoute)
            model.book(listOf(friday, LocalDate(2026, 9, 28)))
            model.book(listOf(LocalDate(2026, 9, 26)))
            advanceUntilIdle()

            assertTrue(calls.isEmpty())
            assertFalse(model.uiState.value.bookedSuccessfully)
        } finally {
            Dispatchers.resetMain()
        }
    }
}
