package com.anto426.uniapp.transport.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.codes.CodeReader
import com.anto426.uniapp.codes.DecodedCode
import com.anto426.uniapp.codes.createCodeReader
import com.anto426.uniapp.data.UniDatasets
import com.anto426.uniapp.data.UniGateway
import com.anto426.uniapp.data.asUniRepository
import com.anto426.uniapp.data.observeIn
import com.anto426.uniapp.data.toReservations
import com.anto426.uniapp.feedback.runtime.AppToastSink
import com.anto426.uniapp.feedback.runtime.error
import com.anto426.uniapp.feedback.runtime.success
import com.anto426.uniapp.model.transport.TransportReservation
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.onRefreshFailure
import com.anto426.uniapp.presentation.userMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.anto426.uniapp.localization.readAppString
import uniapp.composeapp.generated.resources.*

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
    private val dataSource: UniGateway,
    private val toastSink: AppToastSink = AppToastSink.None,
    private val codeReader: CodeReader = createCodeReader(),
) : ViewModel() {
    private var imageJob: Job? = null
    private var imageSource: String? = null
    private val mutableUiState = MutableStateFlow(ReservationDetailUiState())
    val uiState: StateFlow<ReservationDetailUiState> = mutableUiState.asStateFlow()

    private val repository = dataSource.asUniRepository(viewModelScope)
    private val sharedData = repository.engine
    private val dataRequests = listOf(UniDatasets.Transport)
    private val dataObservation = sharedData.observeIn(viewModelScope, dataRequests, subscriptions = mutableUiState.subscriptionCount) { snapshot ->
        try {
            val reservation = snapshot.require(UniDatasets.Transport).toReservations().firstOrNull { it.id == reservationId }
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
                errorMessage = error.userMessage { readAppString(Res.string.msg_impossibile_caricare_la_prenotazione) },
            )
        }
    }

    fun refresh(force: Boolean = false) { repository.transport.refresh(force) }

    fun loadTicketImage(forceRefresh: Boolean = false) {
        val reservation = mutableUiState.value.reservation ?: return
        imageJob?.cancel()
        imageJob = viewModelScope.launch {
            val previous = mutableUiState.value.image
            mutableUiState.value = mutableUiState.value.copy(image = previous.copy(isLoading = true, errorMessage = null))
            try {
                val original = repository.transport.loadTicketImage(reservation.id, reservation.ticketUrl, forceRefresh)
                ensureActive()
                mutableUiState.value = mutableUiState.value.copy(image = TicketImageUiState(original = original, isReading = true))
                val codes = codeReader.read(original)
                ensureActive()
                mutableUiState.value = mutableUiState.value.copy(image = TicketImageUiState(original = original, codes = codes))
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                val current = mutableUiState.value.image
                val message = readAppString(if (current.original == null) Res.string.ui_ticket_download_failed else Res.string.ui_ticket_use_original)
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
                repository.transport.deleteBooking(reservationId)
                mutableUiState.value = mutableUiState.value.copy(isDeleting = false, deleted = true, image = TicketImageUiState())
                toastSink.success(Res.string.msg_transport_booking_cancelled)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                val message = error.userMessage { readAppString(Res.string.msg_impossibile_annullare_la_prenotazione) }
                mutableUiState.value = mutableUiState.value.copy(isDeleting = false)
                toastSink.error(message)
            }
        }
    }
}
