package com.anto426.uniapp.model.transport

import androidx.compose.ui.graphics.vector.ImageVector

data class TransportRoute(val route: String, val time: String, val countdown: String)

data class TransportTicket(
    val id: String,
    val title: String,
    val price: String,
    val validity: String,
    val type: String,
    val icon: ImageVector,
)

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
    val busNumber: String = ""
)
