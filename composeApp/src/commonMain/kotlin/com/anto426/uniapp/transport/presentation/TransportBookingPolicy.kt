package com.anto426.uniapp.transport.presentation

import com.anto426.unisdk.transport.TransportBooking
import com.anto426.unisdk.transport.TransportBookingRequest
import com.anto426.unisdk.transport.TransportDirection
import com.anto426.unisdk.transport.parseTransportTravelDate
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

internal const val TRANSPORT_BOOKING_DAYS_AHEAD = 15

internal fun transportBookingWindow(today: LocalDate): List<LocalDate> =
    (1..TRANSPORT_BOOKING_DAYS_AHEAD).map { today.plus(it, DateTimeUnit.DAY) }

internal fun isTransportBookingDateAllowed(date: LocalDate, today: LocalDate): Boolean =
    date > today && date <= today.plus(TRANSPORT_BOOKING_DAYS_AHEAD, DateTimeUnit.DAY) && date.dayOfWeek.ordinal < 5

internal fun bookedTransportRequests(bookings: List<TransportBooking>): Set<TransportBookingRequest> =
    bookings.mapNotNull { booking ->
        val date = parseTransportTravelDate(booking.date)?.date ?: return@mapNotNull null
        val routeCode = booking.routeCode.trim().takeIf(String::isNotEmpty) ?: return@mapNotNull null
        TransportBookingRequest(
            routeCode.uppercase(),
            date,
            if (booking.isReturn) TransportDirection.RETURN else TransportDirection.OUTBOUND,
        )
    }.toSet()

internal fun isTransportDateBooked(
    date: LocalDate,
    routeCode: String,
    direction: TransportDirection,
    bookedRequests: Set<TransportBookingRequest>,
): Boolean = routeCode.isNotBlank() && transportBookingRequests(routeCode.trim().uppercase(), listOf(date), direction)
    .any(bookedRequests::contains)

internal fun transportBookingRequests(
    routeCode: String,
    dates: List<LocalDate>,
    direction: TransportDirection,
): List<TransportBookingRequest> = dates.distinct().sorted().flatMap { date ->
    val directions = if (direction == TransportDirection.ROUND_TRIP) {
        listOf(TransportDirection.OUTBOUND, TransportDirection.RETURN)
    } else {
        listOf(direction)
    }
    directions.map { trip -> TransportBookingRequest(routeCode, date, trip) }
}
