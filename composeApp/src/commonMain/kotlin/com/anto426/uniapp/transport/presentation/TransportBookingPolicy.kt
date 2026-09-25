package com.anto426.uniapp.transport.presentation

import com.anto426.unisdk.transport.TransportBookingRequest
import com.anto426.unisdk.transport.TransportDirection
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

internal fun isTransportBookingDateAllowed(date: LocalDate, today: LocalDate): Boolean =
    date > today && date <= today.plus(15, DateTimeUnit.DAY) && date.dayOfWeek.ordinal < 5

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
