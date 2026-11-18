package com.anto426.uniapp.model.transport


enum class TripDirection {
    ANDATA,
    RITORNO
}

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
