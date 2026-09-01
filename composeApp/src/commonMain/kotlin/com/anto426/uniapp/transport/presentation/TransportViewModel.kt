package com.anto426.uniapp.transport.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.UniAppDataSource
import com.anto426.uniapp.data.toReservations
import com.anto426.uniapp.model.transport.TransportReservation
import com.anto426.uniapp.model.transport.TripDirection
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.userMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

class TransportViewModel(private val dataSource: UniAppDataSource) : ViewModel() {
    private val mutableUiState = MutableStateFlow(TransportUiState())
    val uiState: StateFlow<TransportUiState> = mutableUiState.asStateFlow()

    init { refresh() }

    fun refresh(force: Boolean = false) {
        viewModelScope.launch {
            mutableUiState.value = mutableUiState.value.copy(loadState = FeatureLoadState.Loading, errorMessage = null)
            try {
                val reservations = dataSource.loadTransportData(force).toReservations()
                mutableUiState.value = TransportUiState(
                    days = reservations.toDayStates(),
                    loadState = if (reservations.isEmpty()) FeatureLoadState.Empty else FeatureLoadState.Content,
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                mutableUiState.value = mutableUiState.value.copy(
                    loadState = FeatureLoadState.Error,
                    errorMessage = error.userMessage("Impossibile caricare le prenotazioni trasporto."),
                )
            }
        }
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
