package com.anto426.uniapp.navigation.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

internal class ConnectedDevicesActionHost {
    var onDisconnectAll by mutableStateOf<(() -> Unit)?>(null)
        private set

    fun register(action: () -> Unit) {
        onDisconnectAll = action
    }

    fun unregister(action: () -> Unit) {
        if (onDisconnectAll === action) onDisconnectAll = null
    }
}
