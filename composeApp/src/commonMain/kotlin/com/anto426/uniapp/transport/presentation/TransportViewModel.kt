package com.anto426.uniapp.transport.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.UniDatasets
import com.anto426.uniapp.data.UniGateway
import com.anto426.uniapp.data.asUniRepository
import com.anto426.uniapp.data.observeIn
import com.anto426.uniapp.data.toReservations
import com.anto426.uniapp.model.transport.TransportReservation
import com.anto426.uniapp.model.transport.TripDirection
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.onRefresh
import com.anto426.uniapp.presentation.onRefreshFailure
import com.anto426.uniapp.presentation.userMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.anto426.uniapp.localization.readAppString
import uniapp.composeapp.generated.resources.*

data class TransportDayUiState(
    val date: String,
    val reservations: List<TransportReservation>,
    val directionSummary: String,
)

data class TransportUiState(
    val days: List<TransportDayUiState> = emptyList(),
    val loadState: FeatureLoadState = FeatureLoadState.Loading,
    val errorMessage: String? = null,
)

class TransportViewModel(private val dataSource: UniGateway) : ViewModel() {
    private val mutableUiState = MutableStateFlow(TransportUiState())
    val uiState: StateFlow<TransportUiState> = mutableUiState.asStateFlow()

    private val repository = dataSource.asUniRepository(viewModelScope)
    private val sharedData = repository.engine
    private val dataRequests = listOf(UniDatasets.Transport)
    private val dataObservation = sharedData.observeIn(viewModelScope, dataRequests, subscriptions = mutableUiState.subscriptionCount) { snapshot ->
        mutableUiState.value = mutableUiState.value.copy(loadState = mutableUiState.value.loadState.onRefresh(), errorMessage = null)
        try {
            val reservations = snapshot.require(UniDatasets.Transport).toReservations()
            mutableUiState.value = TransportUiState(
                days = reservations.toDayStates(),
                loadState = if (reservations.isEmpty()) FeatureLoadState.Empty else FeatureLoadState.Content,
            )
            snapshot.throwIfFailed()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            mutableUiState.value = mutableUiState.value.copy(
                loadState = mutableUiState.value.loadState.onRefreshFailure(),
                errorMessage = error.userMessage { readAppString(Res.string.msg_impossibile_caricare_le_prenotazioni_trasporto) },
            )
        }
    }

    fun refresh(force: Boolean = false) {
        repository.transport.refresh(force)
    }
}

private fun List<TransportReservation>.toDayStates(): List<TransportDayUiState> =
    groupBy(TransportReservation::date).map { (date, reservations) ->
        val outgoing = reservations.count { it.direction == TripDirection.ANDATA }
        val returning = reservations.count { it.direction == TripDirection.RITORNO }
        val summary =
            buildList {
                if (outgoing > 0) add("$outgoing Andata")
                if (returning > 0) add("$returning Ritorno")
            }.joinToString(" • ")
        TransportDayUiState(
            date = date,
            reservations = reservations,
            directionSummary = summary.ifBlank { "${reservations.size} corse" },
        )
    }
