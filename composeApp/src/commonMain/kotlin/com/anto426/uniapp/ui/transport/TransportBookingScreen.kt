package com.anto426.uniapp.ui.transport

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.LiquidPreferenceGroup
import com.anto426.liquidmonet.components.pickers.LiquidDatePicker
import com.anto426.liquidmonet.components.pickers.rememberLiquidDatePickerState
import com.anto426.liquidmonet.components.selection.LiquidSelect
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.transport.presentation.TransportBookingUiState
import com.kyant.backdrop.Backdrop

@Composable
fun TransportBookingScreen(
    backdropState: Backdrop,
    uiState: TransportBookingUiState,
    onRouteSelected: (String) -> Unit,
) {
    val datePickerState = rememberLiquidDatePickerState()


    UniScreenColumn {
        LiquidSelect(
            items = uiState.routes,
            selectedItem = uiState.selectedRoute,
            onItemSelected = onRouteSelected,
            label = "Seleziona Linea",
            backdropState = backdropState
        )

        // 2. Date Selection (Compon        // 1. Course Selection (Corsa)ente giusto: Calendario Liquid)
        LiquidPreferenceGroup(title = "Data del Viaggio", backdropState = backdropState) {
            Box(modifier = Modifier.padding(12.dp)) {
                LiquidDatePicker(
                    state = datePickerState,
                    backdropState = backdropState
                )
            }
        }



        // Space for global FAB
        Spacer(Modifier.height(100.dp))
    }
}
