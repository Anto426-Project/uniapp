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

    private val sharedData = dataSource.sharedData(viewModelScope)
    private val dataRequests = listOf(UniAppDataRequests.Transport)
    private val dataObservation = sharedData.observeIn(viewModelScope, dataRequests, subscriptions = mutableUiState.subscriptionCount) { snapshot ->
        try {
            val reservation = snapshot.require(UniAppDataRequests.Transport).toReservations().firstOrNull { it.id == reservationId }
            mutableUiState.value = mutableUiState.value.copy(
                reservation = reservation,
                loadState = if (reservation == null) FeatureLoadState.Empty else FeatureLoadState.Content,
            )
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

    fun delete() {
        if (mutableUiState.value.reservation == null) return
        viewModelScope.launch {
            mutableUiState.value = mutableUiState.value.copy(isDeleting = true, errorMessage = null)
            try {
                sharedData.deleteTransportBooking(reservationId)
                mutableUiState.value = mutableUiState.value.copy(isDeleting = false, deleted = true)
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
