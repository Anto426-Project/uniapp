package com.anto426.uniapp.data

import com.anto426.unisdk.transport.TransportBooking
import com.anto426.unisdk.transport.TransportData
import com.anto426.unisdk.transport.TransportRouteData
import kotlin.test.Test
import kotlin.test.assertEquals

class TransportMappingTest {
    @Test fun reservationsUsePortalTicketFieldsAndChronologicalTravelDates() {
        val data = TransportData(
            routeLabel = "Campobasso/Pesche/A/R",
            availableRoutes = listOf(TransportRouteData("A01", "Campobasso/Pesche/A/R")),
            bookings = listOf(
                booking("later", "T-002", "lun 28/09/2026 14:05", isReturn = true),
                booking("earlier", "T-001", "dom 27/09/2026 08:30"),
            ),
            totalCount = 2,
        )

        val reservations = data.toReservations()

        assertEquals(listOf("earlier", "later"), reservations.map { it.id })
        assertEquals(listOf("27/09/2026", "28/09/2026"), reservations.map { it.date })
        assertEquals(listOf("08:30", "14:05"), reservations.map { it.time })
        assertEquals(listOf("T-001", "T-002"), reservations.map { it.ticketNumber })
        assertEquals("Campobasso", reservations.first().departureStop)
        assertEquals("Pesche", reservations.last().departureStop)
        assertEquals("Campobasso", reservations.last().arrivalStop)
        assertEquals(listOf("Campobasso/Pesche", "Pesche/Campobasso"), reservations.map { it.route })
    }

    @Test fun outboundIsShownBeforeReturnOnTheSameDayEvenWhenTheReturnTimeIsEarlier() {
        val data = TransportData(
            routeLabel = "Campobasso/Pesche/A/R",
            availableRoutes = listOf(TransportRouteData("A01", "Campobasso/Pesche/A/R")),
            bookings = listOf(
                booking("back", "T-004", "28/09/2026 08:00", isReturn = true),
                booking("out", "T-005", "28/09/2026 09:00"),
            ),
            totalCount = 2,
        )

        assertEquals(listOf("out", "back"), data.toReservations().map { it.id })
    }

    @Test fun weekdayOrDirectionAloneNeverBecomesTravelDateOrStop() {
        val data = TransportData(
            routeLabel = "Navetta Studenti",
            bookings = listOf(booking("one", "T-003", "lun")),
            totalCount = 1,
        )
        val reservation = data.toReservations().single()
        assertEquals("", reservation.date)
        assertEquals("", reservation.departureStop)
        assertEquals("", reservation.arrivalStop)
    }

    private fun booking(id: String, number: String, date: String, isReturn: Boolean = false) = TransportBooking(
        id = id,
        number = number,
        routeCode = "A01",
        date = date,
        direction = if (isReturn) "Ritorno" else "Andata",
        isReturn = isReturn,
        ticketUrl = "/vis_prenot.php?id=$id",
    )
}
