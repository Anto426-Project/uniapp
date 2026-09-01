package com.anto426.uniapp.transport.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.UniAppDataSource
import com.anto426.uniapp.feedback.runtime.AppToastSink
import com.anto426.uniapp.feedback.runtime.error
import com.anto426.uniapp.feedback.runtime.success
import com.anto426.uniapp.feedback.runtime.warning
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.userMessage
import com.anto426.unisdk.transport.TransportActionResult
import com.anto426.unisdk.transport.TransportBookingRequest
import com.anto426.unisdk.transport.TransportDirection
import com.anto426.unisdk.transport.TransportRouteData
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

data class TransportBookingUiState(
    val routes: List<String> = emptyList(),
    val selectedRoute: String = "",
    val loadState: FeatureLoadState = FeatureLoadState.Loading,
    val errorMessage: String? = null,
    val isSubmitting: Boolean = false,
)

class TransportBookingViewModel(
    private val dataSource: UniAppDataSource,
    private val toastSink: AppToastSink = AppToastSink.None,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(TransportBookingUiState())
    val uiState: StateFlow<TransportBookingUiState> = mutableUiState.asStateFlow()
    private var routesByLabel: Map<String, TransportRouteData> = emptyMap()

    init { refresh() }

    fun refresh(force: Boolean = false) {
        viewModelScope.launch {
            mutableUiState.value = mutableUiState.value.copy(loadState = FeatureLoadState.Loading, errorMessage = null)
            try {
                val routes = dataSource.loadTransportData(force).availableRoutes
                routesByLabel = routes.associateBy(TransportRouteData::label)
                mutableUiState.value = mutableUiState.value.copy(
                    routes = routes.map(TransportRouteData::label),
                    selectedRoute = routes.firstOrNull()?.label.orEmpty(),
                    loadState = if (routes.isEmpty()) FeatureLoadState.Empty else FeatureLoadState.Content,
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                mutableUiState.value = mutableUiState.value.copy(
                    loadState = FeatureLoadState.Error,
                    errorMessage = error.userMessage("Impossibile caricare le linee."),
                )
            }
        }
    }

    fun selectRoute(route: String) {
        if (route !in mutableUiState.value.routes) return
        mutableUiState.value = mutableUiState.value.copy(selectedRoute = route)
    }

    fun book(date: LocalDate, direction: TransportDirection = TransportDirection.OUTBOUND) {
        val route = routesByLabel[mutableUiState.value.selectedRoute] ?: return
        viewModelScope.launch {
            mutableUiState.value = mutableUiState.value.copy(isSubmitting = true)
            try {
                val result = dataSource.bookTransport(TransportBookingRequest(route.code, date, direction))
                mutableUiState.value = mutableUiState.value.copy(isSubmitting = false)
                if (result == TransportActionResult.AlreadyExists) {
                    toastSink.warning("Corsa già prenotata.")
                } else {
                    toastSink.success("Prenotazione completata.")
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                val message = error.userMessage("Prenotazione non riuscita.")
                mutableUiState.value = mutableUiState.value.copy(isSubmitting = false)
                toastSink.error(message)
            }
        }
    }
}
