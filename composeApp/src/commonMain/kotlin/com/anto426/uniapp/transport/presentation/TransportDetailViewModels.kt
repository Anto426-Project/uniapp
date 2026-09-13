package com.anto426.uniapp.transport.presentation

import org.jetbrains.compose.resources.getString
import uniapp.composeapp.generated.resources.*

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.runtime.*
import com.anto426.uniapp.data.UniAppDataSource
import com.anto426.uniapp.data.toReservations
import com.anto426.uniapp.data.toTickets
import com.anto426.uniapp.feedback.runtime.AppToastSink
import com.anto426.uniapp.feedback.runtime.error
import com.anto426.uniapp.feedback.runtime.success
import com.anto426.uniapp.model.transport.TransportReservation
import com.anto426.uniapp.model.transport.TransportTicket
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.onRefreshFailure
import com.anto426.uniapp.presentation.userMessage
import com.anto426.uniapp.codes.CodeReader
import com.anto426.uniapp.codes.DecodedCode
import com.anto426.uniapp.codes.createCodeReader
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TicketImageUiState(
    val original: ByteArray? = null,
    val codes: List<DecodedCode> = emptyList(),
    val isLoading: Boolean = false,
    val isReading: Boolean = false,
    val errorMessage: String? = null,
)

data class ReservationDetailUiState(
    val reservation: TransportReservation? = null,
    val loadState: FeatureLoadState = FeatureLoadState.Loading,
    val errorMessage: String? = null,
    val isDeleting: Boolean = false,
    val deleted: Boolean = false,
    val image: TicketImageUiState = TicketImageUiState(),
)

class ReservationDetailViewModel(
    private val reservationId: String,
    private val dataSource: UniAppDataSource,
    private val toastSink: AppToastSink = AppToastSink.None,
    private val codeReader: CodeReader = createCodeReader(),
) : ViewModel() {
    private var imageJob: Job? = null
    private var imageSource: String? = null
    private val mutableUiState = MutableStateFlow(ReservationDetailUiState())
    val uiState: StateFlow<ReservationDetailUiState> = mutableUiState.asStateFlow()

    private val sharedData = dataSource.sharedData(viewModelScope)
    private val dataRequests = listOf(UniAppDataRequests.Transport)
    private val dataObservation = sharedData.observeIn(viewModelScope, dataRequests, subscriptions = mutableUiState.subscriptionCount) { snapshot ->
        try {
            val reservation = snapshot.require(UniAppDataRequests.Transport).toReservations().firstOrNull { it.id == reservationId }
            mutableUiState.value = mutableUiState.value.copy(
                reservation = reservation,
                loadState = if (reservation == null) FeatureLoadState.Empty else FeatureLoadState.Content,
            )
            if (reservation == null) {
                imageJob?.cancel()
                imageSource = null
                mutableUiState.value = mutableUiState.value.copy(image = TicketImageUiState())
            } else if (imageSource != reservation.ticketUrl) {
                val changed = imageSource != null
                imageSource = reservation.ticketUrl
                mutableUiState.value = mutableUiState.value.copy(image = TicketImageUiState())
                loadTicketImage(forceRefresh = changed)
            }
            snapshot.throwIfFailed()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            mutableUiState.value = mutableUiState.value.copy(
                loadState = mutableUiState.value.loadState.onRefreshFailure(),
                errorMessage = error.userMessage(getString(Res.string.msg_impossibile_caricare_la_prenotazione)),
            )
        }
    
    }

    fun refresh(force: Boolean = false) { sharedData.refresh(dataRequests, force) }

    fun loadTicketImage(forceRefresh: Boolean = false) {
        val reservation = mutableUiState.value.reservation ?: return
        imageJob?.cancel()
        imageJob = viewModelScope.launch {
            val previous = mutableUiState.value.image
            mutableUiState.value = mutableUiState.value.copy(image = previous.copy(isLoading = true, errorMessage = null))
            try {
                val original = sharedData.loadTransportTicketImage(reservation.id, reservation.ticketUrl, forceRefresh)
                ensureActive()
                // Publish the saved original before decoding: reading failure cannot hide it.
                mutableUiState.value = mutableUiState.value.copy(image = TicketImageUiState(original = original, isReading = true))
                val codes = codeReader.read(original)
                ensureActive()
                mutableUiState.value = mutableUiState.value.copy(image = TicketImageUiState(original = original, codes = codes))
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                val current = mutableUiState.value.image
                val message = getString(if (current.original == null) Res.string.ui_ticket_download_failed else Res.string.ui_ticket_use_original)
                mutableUiState.value = mutableUiState.value.copy(image = current.copy(isLoading = false, isReading = false, errorMessage = message))
            }
        }
    }

    fun delete() {
        if (mutableUiState.value.reservation == null) return
        viewModelScope.launch {
            mutableUiState.value = mutableUiState.value.copy(isDeleting = true, errorMessage = null)
            try {
                imageJob?.cancel()
                sharedData.deleteTransportBooking(reservationId)
                mutableUiState.value = mutableUiState.value.copy(isDeleting = false, deleted = true, image = TicketImageUiState())
                toastSink.success("Prenotazione annullata.")
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                val message = error.userMessage(getString(Res.string.msg_impossibile_annullare_la_prenotazione))
                mutableUiState.value = mutableUiState.value.copy(isDeleting = false)
                toastSink.error(message)
            }
        }
    }
}

data class TicketDetailUiState(
    val ticket: TransportTicket? = null,
    val loadState: FeatureLoadState = FeatureLoadState.Loading,
    val errorMessage: String? = null,
)

class TicketDetailViewModel(
    private val ticketId: String,
    private val dataSource: UniAppDataSource,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(TicketDetailUiState())
    val uiState: StateFlow<TicketDetailUiState> = mutableUiState.asStateFlow()

    private val sharedData = dataSource.sharedData(viewModelScope)
    private val dataRequests = listOf(UniAppDataRequests.Transport)
    private val dataObservation = sharedData.observeIn(viewModelScope, dataRequests, subscriptions = mutableUiState.subscriptionCount) { snapshot ->
        try {
            val ticket = snapshot.require(UniAppDataRequests.Transport).toTickets().firstOrNull { it.id == ticketId }
            mutableUiState.value = mutableUiState.value.copy(
                ticket = ticket,
                loadState = if (ticket == null) FeatureLoadState.Empty else FeatureLoadState.Content,
            )
            snapshot.throwIfFailed()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            mutableUiState.value = mutableUiState.value.copy(
                loadState = mutableUiState.value.loadState.onRefreshFailure(),
                errorMessage = error.userMessage(getString(Res.string.msg_impossibile_caricare_la_linea)),
            )
        }
    
    }

    fun refresh(force: Boolean = false) { sharedData.refresh(dataRequests, force) }
}
