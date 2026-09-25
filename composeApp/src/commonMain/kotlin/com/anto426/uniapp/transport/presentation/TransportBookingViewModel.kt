package com.anto426.uniapp.transport.presentation

import org.jetbrains.compose.resources.getString
import uniapp.composeapp.generated.resources.*

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.runtime.*
import com.anto426.uniapp.data.UniAppDataSource
import com.anto426.uniapp.feedback.runtime.AppToastSink
import com.anto426.uniapp.feedback.runtime.error
import com.anto426.uniapp.feedback.runtime.success
import com.anto426.uniapp.feedback.runtime.warning
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.onRefresh
import com.anto426.uniapp.presentation.onRefreshFailure
import com.anto426.uniapp.presentation.userMessage
import com.anto426.unisdk.transport.TransportActionResult
import com.anto426.unisdk.transport.TransportDirection
import com.anto426.unisdk.transport.TransportRouteData
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

data class TransportBookingUiState(
    val routes: List<String> = emptyList(),
    val selectedRoute: String = "",
    val loadState: FeatureLoadState = FeatureLoadState.Loading,
    val errorMessage: String? = null,
    val isSubmitting: Boolean = false,
    val bookedSuccessfully: Boolean = false,
)

class TransportBookingViewModel(
    private val dataSource: UniAppDataSource,
    private val toastSink: AppToastSink = AppToastSink.None,
    private val today: () -> LocalDate = { Clock.System.todayIn(TimeZone.currentSystemDefault()) },
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(TransportBookingUiState())
    val uiState: StateFlow<TransportBookingUiState> = mutableUiState.asStateFlow()
    private var routesByLabel: Map<String, TransportRouteData> = emptyMap()

    private val sharedData = dataSource.sharedData(viewModelScope)
    private val dataRequests = listOf(UniAppDataRequests.Transport)
    private val dataObservation = sharedData.observeIn(viewModelScope, dataRequests, subscriptions = mutableUiState.subscriptionCount) { snapshot ->
        mutableUiState.value = mutableUiState.value.copy(loadState = mutableUiState.value.loadState.onRefresh(), errorMessage = null)
        try {
            val routes = snapshot.require(UniAppDataRequests.Transport).availableRoutes
            routesByLabel = routes.associateBy(TransportRouteData::label)
            val selectedRoute = mutableUiState.value.selectedRoute.takeIf { it in routesByLabel }
                ?: routes.firstOrNull()?.label.orEmpty()
            mutableUiState.value = mutableUiState.value.copy(
                routes = routes.map(TransportRouteData::label),
                selectedRoute = selectedRoute,
                loadState = if (routes.isEmpty()) FeatureLoadState.Empty else FeatureLoadState.Content,
            )
            snapshot.throwIfFailed()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            mutableUiState.value = mutableUiState.value.copy(
                loadState = mutableUiState.value.loadState.onRefreshFailure(),
                errorMessage = error.userMessage(getString(Res.string.msg_impossibile_caricare_le_linee)),
            )
        }

    }

    fun refresh(force: Boolean = false) {
        sharedData.refresh(dataRequests, force)
    }

    fun selectRoute(route: String) {
        if (route !in mutableUiState.value.routes) return
        mutableUiState.value = mutableUiState.value.copy(selectedRoute = route)
    }

    fun book(date: LocalDate, direction: TransportDirection = TransportDirection.OUTBOUND) {
        book(listOf(date), direction)
    }

    fun book(dates: List<LocalDate>, direction: TransportDirection = TransportDirection.OUTBOUND) {
        val route = routesByLabel[mutableUiState.value.selectedRoute] ?: return
        if (dates.isEmpty() || mutableUiState.value.isSubmitting) return
        if (dates.any { !isTransportBookingDateAllowed(it, today()) }) {
            viewModelScope.launch {
                toastSink.warning(getString(Res.string.ui_transport_booking_date_policy))
            }
            return
        }
        val requests = transportBookingRequests(route.code, dates, direction)
        mutableUiState.value = mutableUiState.value.copy(isSubmitting = true)
        viewModelScope.launch {
            try {
                var completed = 0
                var existing = 0
                var failed = 0
                var firstFailure: Throwable? = null
                for (request in requests) {
                    try {
                        // Keep the portal's form sequence serial and refresh the shared snapshot once after the batch.
                        when (dataSource.bookTransport(request)) {
                            TransportActionResult.Completed -> completed++
                            TransportActionResult.AlreadyExists -> existing++
                        }
                    } catch (error: CancellationException) {
                        throw error
                    } catch (error: Throwable) {
                        failed++
                        if (firstFailure == null) firstFailure = error
                    }
                }
                mutableUiState.value = mutableUiState.value.copy(
                    isSubmitting = false,
                    bookedSuccessfully = completed > 0 && failed == 0,
                )
                if (completed > 0) sharedData.refresh(dataRequests, force = true)
                when {
                    failed > 0 && completed > 0 -> toastSink.warning(
                        getString(Res.string.ui_transport_booking_partial, completed, requests.size, existing, failed),
                    )
                    failed > 0 -> toastSink.error(
                        firstFailure?.userMessage(getString(Res.string.msg_prenotazione_non_riuscita))
                            ?: getString(Res.string.msg_prenotazione_non_riuscita),
                    )
                    completed > 0 -> toastSink.success(
                        getString(Res.string.ui_transport_booking_completed, completed),
                    )
                    else -> toastSink.warning(getString(Res.string.msg_corse_gia_prenotate_per_le_date_selezionate))
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                val message = error.userMessage(getString(Res.string.msg_prenotazione_non_riuscita))
                mutableUiState.value = mutableUiState.value.copy(isSubmitting = false)
                toastSink.error(message)
            }
        }
    }
}
