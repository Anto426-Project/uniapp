package com.anto426.uniapp.transport.presentation

import androidx.lifecycle.ViewModel
import com.anto426.uniapp.model.transport.TransportTicket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TransportCatalogUiState(val tickets: List<TransportTicket> = emptyList())

class TransportCatalogViewModel(tickets: List<TransportTicket>) : ViewModel() {
    private val mutableUiState = MutableStateFlow(TransportCatalogUiState(tickets))
    val uiState: StateFlow<TransportCatalogUiState> = mutableUiState.asStateFlow()
}
