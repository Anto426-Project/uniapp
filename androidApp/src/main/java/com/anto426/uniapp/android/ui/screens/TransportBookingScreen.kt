package com.anto426.uniapp.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.LiquidPreferenceGroup
import com.anto426.liquidmonet.components.pickers.LiquidDatePicker
import com.anto426.liquidmonet.components.pickers.rememberLiquidDatePickerState
import com.anto426.liquidmonet.components.selection.LiquidSelect
import com.anto426.uniapp.android.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.android.ui.data.UiInitialData
import com.kyant.backdrop.Backdrop

@Composable
fun TransportBookingScreen(backdropState: Backdrop) {
    val colorScheme = MaterialTheme.colorScheme

    val routes = UiInitialData.transportRoutes.map { it.route }
    var selectedRoute by rememberSaveable { mutableStateOf(routes[0]) }

    val datePickerState = rememberLiquidDatePickerState()


    UniScreenColumn {
        LiquidSelect(
            items = routes,
            selectedItem = selectedRoute,
            onItemSelected = { selectedRoute = it },
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
