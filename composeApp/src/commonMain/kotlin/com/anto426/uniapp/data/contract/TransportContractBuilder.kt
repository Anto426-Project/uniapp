package com.anto426.uniapp.data.contract

import com.anto426.unisdk.transport.TransportActionResult
import com.anto426.unisdk.transport.TransportBooking
import com.anto426.unisdk.transport.TransportBookingRequest
import com.anto426.unisdk.transport.TransportData
import com.anto426.unisdk.transport.TransportDirection
import com.anto426.unisdk.transport.TransportRoute
import com.anto426.unisdk.transport.displayFor
import com.anto426.unisdk.transport.parseTransportTravelDate

internal fun buildTransportContract(data: TransportData): UniTransportContract {
    val reservations = data.toReservations()
    val days = reservations.groupBy(TransportReservation::date).map { (date, list) ->
        val outgoing = list.count { it.direction == TripDirection.ANDATA }
        val returning = list.count { it.direction == TripDirection.RITORNO }
        val summary = buildList {
            if (outgoing > 0) add("$outgoing Andata")
            if (returning > 0) add("$returning Ritorno")
        }.joinToString(" • ")
        TransportDayGroup(
            date = date,
            reservations = list,
            directionSummary = summary.ifBlank { "${list.size} corse" },
        )
    }
    return UniTransportContract(
        reservations = reservations,
        days = days,
        routes = data.availableRoutes.map(TransportRoute::toTransportRouteOption),
        bookedRequests = bookedTransportRequests(data.bookings),
        totalCount = data.totalCount,
    )
}

internal fun TripBookingDirection.toSdkDirection(): TransportDirection =
    when (this) {
        TripBookingDirection.OUTBOUND -> TransportDirection.OUTBOUND
        TripBookingDirection.RETURN -> TransportDirection.RETURN
        TripBookingDirection.ROUND_TRIP -> TransportDirection.ROUND_TRIP
    }

internal fun TransportActionResult.toTripBookingOutcome(): TripBookingOutcome =
    when (this) {
        TransportActionResult.Completed -> TripBookingOutcome.COMPLETED
        TransportActionResult.AlreadyExists -> TripBookingOutcome.ALREADY_EXISTS
    }

internal fun TripBookingRequest.toSdkRequest(): TransportBookingRequest =
    TransportBookingRequest(
        routeCode = routeCode,
        date = date,
        direction = direction.toSdkDirection(),
    )

internal fun TransportRoute.toTransportRouteOption(): TransportRouteOption {
    val out = displayFor(TransportDirection.OUTBOUND)
    val ret = displayFor(TransportDirection.RETURN)
    return TransportRouteOption(
        code = code,
        label = label,
        outboundStops = TransportRouteStops(out.label, out.departureStop, out.arrivalStop),
        returnStops = TransportRouteStops(ret.label, ret.departureStop, ret.arrivalStop),
    )
}

internal fun resolveTransportRouteStops(
    routeLabel: String,
    direction: TripBookingDirection,
): TransportRouteStops {
    val sdkDisplay = TransportRoute(code = "", label = routeLabel).displayFor(direction.toSdkDirection())
    return TransportRouteStops(
        label = sdkDisplay.label,
        departureStop = sdkDisplay.departureStop,
        arrivalStop = sdkDisplay.arrivalStop,
    )
}

internal fun TransportData.toReservations(): List<TransportReservation> =
    bookings
        .sortedWith(
            compareBy<TransportBooking> { parseTransportTravelDate(it.date)?.sortKey?.div(10_000L) ?: Long.MAX_VALUE }
                .thenBy { it.isReturn }
                .thenBy { parseTransportTravelDate(it.date)?.sortKey ?: Long.MAX_VALUE }
                .thenBy { it.id },
        )
        .map { booking ->
            val travel = parseTransportTravelDate(booking.date)
            val route = displayFor(booking)
            TransportReservation(
                id = booking.id,
                route = route.label.ifBlank { booking.routeCode },
                date = travel?.displayDate.orEmpty(),
                time = travel?.time.orEmpty(),
                direction = if (booking.isReturn) TripDirection.RITORNO else TripDirection.ANDATA,
                qrCodeData = "",
                departureStop = route.departureStop,
                arrivalStop = route.arrivalStop,
                busNumber = "",
                ticketUrl = booking.ticketUrl,
                ticketNumber = booking.number,
            )
        }

internal fun bookedTransportRequests(bookings: List<TransportBooking>): Set<TripBookingRequest> =
    bookings.mapNotNull { booking ->
        val date = parseTransportTravelDate(booking.date)?.date ?: return@mapNotNull null
        val routeCode = booking.routeCode.trim().takeIf(String::isNotEmpty) ?: return@mapNotNull null
        TripBookingRequest(
            routeCode = routeCode.uppercase(),
            date = date,
            direction = if (booking.isReturn) TripBookingDirection.RETURN else TripBookingDirection.OUTBOUND,
        )
    }.toSet()
