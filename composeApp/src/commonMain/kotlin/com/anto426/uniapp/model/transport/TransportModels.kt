package com.anto426.uniapp.model.transport

import kotlinx.datetime.LocalDate

enum class TripDirection {
    ANDATA,
    RITORNO,
}

enum class TripBookingDirection {
    OUTBOUND,
    RETURN,
    ROUND_TRIP,
}

enum class TripBookingOutcome {
    COMPLETED,
    ALREADY_EXISTS,
}

data class TripBookingRequest(
    val routeCode: String,
    val date: LocalDate,
    val direction: TripBookingDirection = TripBookingDirection.OUTBOUND,
)

data class TransportRouteStops(
    val label: String,
    val departureStop: String,
    val arrivalStop: String,
)

data class TransportRouteOption(
    val code: String,
    val label: String,
    val outboundStops: TransportRouteStops,
    val returnStops: TransportRouteStops,
)

data class TransportReservation(
    val id: String,
    val route: String,
    val date: String,
    val time: String,
    val direction: TripDirection = TripDirection.ANDATA,
    val qrCodeData: String = "",
    val departureStop: String = "",
    val arrivalStop: String = "",
    val busNumber: String = "",
    val ticketUrl: String = "",
    val ticketNumber: String = "",
)

data class TransportOverviewData(
    val reservations: List<TransportReservation> = emptyList(),
    val routes: List<TransportRouteOption> = emptyList(),
    val bookedRequests: Set<TripBookingRequest> = emptySet(),
    val totalCount: Int = 0,
)
