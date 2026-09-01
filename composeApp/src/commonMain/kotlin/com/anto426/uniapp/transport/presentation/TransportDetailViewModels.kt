package com.anto426.uniapp.transport.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.UniAppDataSource
import com.anto426.uniapp.data.toReservations
import com.anto426.uniapp.data.toTickets
import com.anto426.uniapp.feedback.runtime.AppToastSink
import com.anto426.uniapp.feedback.runtime.error
import com.anto426.uniapp.feedback.runtime.success
import com.anto426.uniapp.model.transport.TransportReservation
import com.anto426.uniapp.model.transport.TransportTicket
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.userMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReservationDetailUiState(
    val reservation: TransportReservation? = null,
    val loadState: FeatureLoadState = FeatureLoadState.Loading,
    val errorMessage: String? = null,
    val isDeleting: Boolean = false,
    val deleted: Boolean = false,
)

class ReservationDetailViewModel(
    private val reservationId: String,
    private val dataSource: UniAppDataSource,
    private val toastSink: AppToastSink = AppToastSink.None,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(ReservationDetailUiState())
    val uiState: StateFlow<ReservationDetailUiState> = mutableUiState.asStateFlow()

    init { refresh() }

    fun refresh(force: Boolean = false) {
        viewModelScope.launch {
            try {
                val reservation = dataSource.loadTransportData(force).toReservations().firstOrNull { it.id == reservationId }
                mutableUiState.value = ReservationDetailUiState(
                    reservation = reservation,
                    loadState = if (reservation == null) FeatureLoadState.Empty else FeatureLoadState.Content,
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                mutableUiState.value = ReservationDetailUiState(
                    loadState = FeatureLoadState.Error,
                    errorMessage = error.userMessage("Impossibile caricare la prenotazione."),
                )
            }
        }
    }

    fun delete() {
        if (mutableUiState.value.reservation == null) return
        viewModelScope.launch {
            mutableUiState.value = mutableUiState.value.copy(isDeleting = true, errorMessage = null)
            try {
                dataSource.deleteTransportBooking(reservationId)
                mutableUiState.value = mutableUiState.value.copy(isDeleting = false, deleted = true)
                toastSink.success("Prenotazione annullata.")
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                val message = error.userMessage("Impossibile annullare la prenotazione.")
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

    init { refresh() }

    fun refresh(force: Boolean = false) {
        viewModelScope.launch {
            try {
                val ticket = dataSource.loadTransportData(force).toTickets().firstOrNull { it.id == ticketId }
                mutableUiState.value = TicketDetailUiState(
                    ticket = ticket,
                    loadState = if (ticket == null) FeatureLoadState.Empty else FeatureLoadState.Content,
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                mutableUiState.value = TicketDetailUiState(
                    loadState = FeatureLoadState.Error,
                    errorMessage = error.userMessage("Impossibile caricare la linea."),
                )
            }
        }
    }
}
