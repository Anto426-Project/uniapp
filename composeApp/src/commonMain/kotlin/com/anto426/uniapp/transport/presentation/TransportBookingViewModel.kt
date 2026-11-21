package com.anto426.uniapp.transport.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.UniDatasets
import com.anto426.uniapp.data.UniGateway
import com.anto426.uniapp.data.asUniRepository
import com.anto426.uniapp.data.observeIn
import com.anto426.uniapp.data.resolveTransportRouteStops
import com.anto426.uniapp.data.toSdkDirection
import com.anto426.uniapp.data.toSdkRequest
import com.anto426.uniapp.data.toTransportOverview
import com.anto426.uniapp.data.toTripBookingRequest
import com.anto426.uniapp.feedback.runtime.AppToastSink
import com.anto426.uniapp.feedback.runtime.error
import com.anto426.uniapp.feedback.runtime.success
import com.anto426.uniapp.feedback.runtime.warning
import com.anto426.uniapp.model.transport.TransportRouteOption
import com.anto426.uniapp.model.transport.TransportRouteStops
import com.anto426.uniapp.model.transport.TripBookingDirection
import com.anto426.uniapp.model.transport.TripBookingOutcome
import com.anto426.uniapp.model.transport.TripBookingRequest
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.onRefresh
import com.anto426.uniapp.presentation.onRefreshFailure
import com.anto426.uniapp.presentation.userMessage
import com.anto426.unisdk.transport.TRANSPORT_BOOKING_DAYS_AHEAD as SDK_TRANSPORT_BOOKING_DAYS_AHEAD
import com.anto426.unisdk.transport.isTransportBookingDateAllowed as sdkIsTransportBookingDateAllowed
import com.anto426.unisdk.transport.isTransportDateBooked as sdkIsTransportDateBooked
import com.anto426.unisdk.transport.transportBookingRequests as sdkTransportBookingRequests
import com.anto426.unisdk.transport.transportBookingWindow as sdkTransportBookingWindow
import kotlin.time.Clock
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import com.anto426.uniapp.localization.readAppString
import uniapp.composeapp.generated.resources.*

data class TransportBookingUiState(
    val routes: List<String> = emptyList(),
    val selectedRoute: String = "",
    val selectedRouteCode: String = "",
    val bookedRequests: Set<TripBookingRequest> = emptySet(),
    val loadState: FeatureLoadState = FeatureLoadState.Loading,
    val errorMessage: String? = null,
    val isSubmitting: Boolean = false,
    val bookedSuccessfully: Boolean = false,
)

class TransportBookingViewModel(
    private val dataSource: UniGateway,
    private val toastSink: AppToastSink = AppToastSink.None,
    private val today: () -> LocalDate = { Clock.System.todayIn(TimeZone.currentSystemDefault()) },
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(TransportBookingUiState())
    val uiState: StateFlow<TransportBookingUiState> = mutableUiState.asStateFlow()
    private var routesByLabel: Map<String, TransportRouteOption> = emptyMap()
    private val confirmedBookings = mutableSetOf<TripBookingRequest>()

    private val repository = dataSource.asUniRepository(viewModelScope)
    private val sharedData = repository.engine
    private val dataRequests = listOf(UniDatasets.Transport)
    private val dataObservation = sharedData.observeIn(viewModelScope, dataRequests, subscriptions = mutableUiState.subscriptionCount) { snapshot ->
        mutableUiState.value = mutableUiState.value.copy(loadState = mutableUiState.value.loadState.onRefresh(), errorMessage = null)
        try {
            val transport = snapshot.require(UniDatasets.Transport).toTransportOverview()
            val routes = transport.routes
            routesByLabel = routes.associateBy(TransportRouteOption::label)
            val selectedRoute = mutableUiState.value.selectedRoute.takeIf { it in routesByLabel }
                ?: routes.firstOrNull()?.label.orEmpty()
            mutableUiState.value = mutableUiState.value.copy(
                routes = routes.map(TransportRouteOption::label),
                selectedRoute = selectedRoute,
                selectedRouteCode = routesByLabel[selectedRoute]?.code.orEmpty(),
                bookedRequests = transport.bookedRequests + confirmedBookings,
                loadState = if (routes.isEmpty()) FeatureLoadState.Empty else FeatureLoadState.Content,
            )
            snapshot.throwIfFailed()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            mutableUiState.value = mutableUiState.value.copy(
                loadState = mutableUiState.value.loadState.onRefreshFailure(),
                errorMessage = error.userMessage { readAppString(Res.string.msg_impossibile_caricare_le_linee) },
            )
        }
    }

    fun refresh(force: Boolean = false) {
        repository.transport.refresh(force)
    }

    fun selectRoute(route: String) {
        if (route !in mutableUiState.value.routes) return
        mutableUiState.value = mutableUiState.value.copy(
            selectedRoute = route,
            selectedRouteCode = routesByLabel[route]?.code.orEmpty(),
        )
    }

    fun book(date: LocalDate, direction: TripBookingDirection = TripBookingDirection.OUTBOUND) {
        book(listOf(date), direction)
    }

    fun book(dates: List<LocalDate>, direction: TripBookingDirection = TripBookingDirection.OUTBOUND) {
        val route = routesByLabel[mutableUiState.value.selectedRoute] ?: return
        if (dates.isEmpty() || mutableUiState.value.isSubmitting) return
        val bookingToday = today()
        if (dates.any { !isTransportBookingDateAllowed(it, bookingToday) }) {
            viewModelScope.launch {
                toastSink.warning(readAppString(Res.string.ui_transport_booking_date_policy))
            }
            return
        }
        val requests = transportBookingRequests(route.code, dates, direction)
        if (dates.any { isTransportDateBooked(it, route.code, direction, mutableUiState.value.bookedRequests) }) {
            viewModelScope.launch {
                toastSink.warning(readAppString(Res.string.msg_corse_gia_prenotate_per_le_date_selezionate))
            }
            return
        }
        mutableUiState.value = mutableUiState.value.copy(isSubmitting = true)
        viewModelScope.launch {
            try {
                var completed = 0
                var existing = 0
                var failed = 0
                var firstFailure: Throwable? = null
                for (request in requests) {
                    try {
                        when (repository.transport.bookTransport(request)) {
                            TripBookingOutcome.COMPLETED -> completed++
                            TripBookingOutcome.ALREADY_EXISTS -> existing++
                        }
                        confirmedBookings += request.copy(routeCode = request.routeCode.trim().uppercase())
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
                    bookedRequests = mutableUiState.value.bookedRequests + confirmedBookings,
                )
                when {
                    failed > 0 && completed > 0 -> toastSink.warning(
                        readAppString(Res.string.ui_transport_booking_partial, completed, requests.size, existing, failed),
                    )
                    failed > 0 -> toastSink.error(
                        firstFailure?.userMessage { readAppString(Res.string.msg_prenotazione_non_riuscita) }
                            ?: readAppString(Res.string.msg_prenotazione_non_riuscita),
                    )
                    completed > 0 -> toastSink.success(
                        readAppString(Res.string.ui_transport_booking_completed, completed),
                    )
                    else -> toastSink.warning(readAppString(Res.string.msg_corse_gia_prenotate_per_le_date_selezionate))
                }
                if (completed > 0 || existing > 0) repository.transport.refresh(force = true)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                val message = error.userMessage { readAppString(Res.string.msg_prenotazione_non_riuscita) }
                mutableUiState.value = mutableUiState.value.copy(isSubmitting = false)
                toastSink.error(message)
            }
        }
    }
}

internal const val TRANSPORT_BOOKING_DAYS_AHEAD = SDK_TRANSPORT_BOOKING_DAYS_AHEAD

internal fun transportBookingWindow(today: LocalDate): List<LocalDate> =
    sdkTransportBookingWindow(today)

internal fun isTransportBookingDateAllowed(date: LocalDate, today: LocalDate): Boolean =
    sdkIsTransportBookingDateAllowed(date, today)

internal fun isTransportDateBooked(
    date: LocalDate,
    routeCode: String,
    direction: TripBookingDirection,
    bookedRequests: Set<TripBookingRequest>,
): Boolean = sdkIsTransportDateBooked(
    date = date,
    routeCode = routeCode,
    direction = direction.toSdkDirection(),
    bookedRequests = bookedRequests.map(TripBookingRequest::toSdkRequest).toSet(),
)

internal fun transportBookingRequests(
    routeCode: String,
    dates: List<LocalDate>,
    direction: TripBookingDirection,
): List<TripBookingRequest> =
    sdkTransportBookingRequests(routeCode, dates, direction.toSdkDirection())
        .map { it.toTripBookingRequest() }

internal fun transportRouteStopsFor(
    routeLabel: String,
    direction: TripBookingDirection,
): TransportRouteStops = resolveTransportRouteStops(routeLabel, direction)
