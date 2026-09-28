package com.anto426.uniapp.navigation.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

internal class ReservationDeleteAction(
    val reservationId: String,
    val enabled: Boolean,
    val onClick: () -> Unit,
)

internal class ReservationDeleteActionHost {
    var action by mutableStateOf<ReservationDeleteAction?>(null)
        private set

    fun register(action: ReservationDeleteAction) {
        this.action = action
    }

    fun unregister(action: ReservationDeleteAction) {
        if (this.action === action) this.action = null
    }
}
