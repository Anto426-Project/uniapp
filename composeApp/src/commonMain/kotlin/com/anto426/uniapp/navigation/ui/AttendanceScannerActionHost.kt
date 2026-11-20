package com.anto426.uniapp.navigation.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

internal class AttendanceScannerActionHost {
    var onOpenScanner by mutableStateOf<(() -> Unit)?>(null)
        private set

    fun register(action: () -> Unit) {
        onOpenScanner = action
    }

    fun unregister(action: () -> Unit) {
        if (onOpenScanner === action) onOpenScanner = null
    }
}
