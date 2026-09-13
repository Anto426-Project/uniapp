package com.anto426.uniapp.transport.presentation

import com.anto426.uniapp.codes.*
import com.anto426.uniapp.data.FakeUniAppDataSource
import com.anto426.unisdk.transport.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import kotlinx.coroutines.flow.first
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class ReservationTicketTest : com.anto426.uniapp.testing.ResourceTest() {
    private val original = byteArrayOf(1, 4, 7, 9)
    private val booking = TransportBooking("0012", "0012", "route", "13/09/2026", "A -> B", false, "https://trasporti.unimol.it/vis_prenot.php?id=0012")
    private fun source(download: suspend () -> ByteArray = { original }) = object : FakeUniAppDataSource() {
        override suspend fun loadTransportData(forceRefresh: Boolean) = TransportData("A - B", bookings = listOf(booking), totalCount = 1)
        override suspend fun loadTransportTicketImage(bookingId: String, source: String, forceRefresh: Boolean): ByteArray {
            assertEquals(booking.id, bookingId)
            assertEquals(booking.ticketUrl, source)
            return download()
        }
    }

    @Test fun unreadableImageKeepsOriginalAndNeverInventsAReservationCode() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val model = ReservationDetailViewModel(booking.id, source(), codeReader = CodeReader { emptyList() })
            advanceUntilIdle()
            assertContentEquals(original, model.uiState.value.image.original)
            assertTrue(model.uiState.value.image.codes.isEmpty())
            assertEquals("", model.uiState.value.reservation!!.qrCodeData)
        } finally { Dispatchers.resetMain() }
    }

    @Test fun decoderFailureKeepsSavedOriginalAvailable() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val model = ReservationDetailViewModel(booking.id, source(), codeReader = CodeReader { error("Unrecognized image") })
            advanceUntilIdle()
            model.uiState.first { it.image.original != null && !it.image.isReading }
            assertContentEquals(original, model.uiState.value.image.original)
            assertNotNull(model.uiState.value.image.errorMessage)
            assertFalse(model.uiState.value.image.isReading)
        } finally { Dispatchers.resetMain() }
    }

    @Test fun failedRefreshPreservesPreviousOriginalAndCodes() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            var offline = false
            val code = DecodedCode("000042", CodeFormat.Code128)
            val model = ReservationDetailViewModel(booking.id, source { if (offline) error("Offline") else original }, codeReader = CodeReader { listOf(code) })
            advanceUntilIdle()
            offline = true
            model.loadTicketImage(forceRefresh = true)
            advanceUntilIdle()
            assertContentEquals(original, model.uiState.value.image.original)
            assertEquals(listOf(code), model.uiState.value.image.codes)
            assertFalse(model.uiState.value.image.isLoading)
        } finally { Dispatchers.resetMain() }
    }

    @Test fun deletionCancelsAnInFlightDecodeAndClearsTheImage() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val decode = CompletableDeferred<List<DecodedCode>>()
            val model = ReservationDetailViewModel(booking.id, source(), codeReader = CodeReader { decode.await() })
            advanceUntilIdle()
            model.delete()
            advanceUntilIdle()
            decode.complete(listOf(DecodedCode("must-not-reappear", CodeFormat.Qr)))
            advanceUntilIdle()
            assertTrue(model.uiState.value.deleted)
            assertNull(model.uiState.value.image.original)
            assertTrue(model.uiState.value.image.codes.isEmpty())
        } finally { Dispatchers.resetMain() }
    }
}
