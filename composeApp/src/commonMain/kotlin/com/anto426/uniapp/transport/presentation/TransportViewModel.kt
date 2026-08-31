package com.anto426.uniapp.transport.presentation

import androidx.lifecycle.ViewModel
import com.anto426.uniapp.model.transport.TransportReservation
import com.anto426.uniapp.model.transport.TripDirection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TransportDayUiState(
    val date: String,
    val reservations: List<TransportReservation>,
    val directionSummary: String,
)

data class TransportUiState(val days: List<TransportDayUiState> = emptyList())

class TransportViewModel(reservations: List<TransportReservation>) : ViewModel() {
    private val mutableUiState = MutableStateFlow(TransportUiState(reservations.toDayStates()))
    val uiState: StateFlow<TransportUiState> = mutableUiState.asStateFlow()
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
