package com.anto426.uniapp.transport.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TransportBookingUiState(
    val routes: List<String> = emptyList(),
    val selectedRoute: String = "",
)

class TransportBookingViewModel(routes: List<String>) : ViewModel() {
    private val mutableUiState =
        MutableStateFlow(
            TransportBookingUiState(
                routes = routes.distinct(),
                selectedRoute = routes.firstOrNull().orEmpty(),
            ),
        )
    val uiState: StateFlow<TransportBookingUiState> = mutableUiState.asStateFlow()

    fun selectRoute(route: String) {
        if (route !in mutableUiState.value.routes) return
        mutableUiState.value = mutableUiState.value.copy(selectedRoute = route)
    }
}
