package com.anto426.uniapp.ui.transport

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.LiquidPreferenceGroup
import com.anto426.liquidmonet.components.buttons.LiquidButton
import com.anto426.liquidmonet.components.pickers.LiquidDatePicker
import com.anto426.liquidmonet.components.pickers.rememberLiquidDatePickerState
import com.anto426.liquidmonet.components.selection.LiquidSelect
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.transport.presentation.TransportBookingUiState
import com.kyant.backdrop.Backdrop

import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun TransportBookingScreen(
    backdropState: Backdrop,
    uiState: TransportBookingUiState,
    onRouteSelected: (String) -> Unit,
    onBook: (kotlinx.datetime.LocalDate) -> Unit,
) {
    val datePickerState = rememberLiquidDatePickerState()

    UniScreenColumn {
        LiquidSelect(
            items = uiState.routes,
            selectedItem = uiState.selectedRoute,
            onItemSelected = onRouteSelected,
            label = stringResource(Res.string.ui_trip_route),
            backdropState = backdropState
        )

        LiquidPreferenceGroup(title = stringResource(Res.string.ui_transport_trip_date), backdropState = backdropState) {
            Box(modifier = Modifier.padding(12.dp)) {
                LiquidDatePicker(
                    state = datePickerState,
                    backdropState = backdropState
                )
            }
        }

        LiquidButton(
            text = stringResource(Res.string.ui_transport_book_ride),
            onClick = { datePickerState.selectedDate?.let(onBook) },
            enabled = datePickerState.selectedDate != null && uiState.selectedRoute.isNotBlank(),
            isLoading = uiState.isSubmitting,
            modifier = Modifier.fillMaxWidth(),
            backdropState = backdropState,
        )



        // Space for global FAB
        Spacer(Modifier.height(100.dp))
    }
}
