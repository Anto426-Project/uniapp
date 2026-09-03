package com.anto426.uniapp.ui.transport

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.display.LiquidEmptyState
import com.anto426.liquidmonet.components.display.LiquidSectionHeader
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.model.transport.TransportReservation
import com.anto426.uniapp.transport.presentation.TransportUiState
import com.anto426.uniapp.ui.components.items.TransportReservationItem
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.kyant.backdrop.Backdrop

import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun TransportScreen(
    backdropState: Backdrop,
    uiState: TransportUiState,
    onReservationClick: (TransportReservation) -> Unit = {}
) {
    UniScreenColumn {
        if (uiState.days.isEmpty()) {
            LiquidEmptyState(
                title = stringResource(Res.string.ui_transport_empty_reservations),
                description = stringResource(Res.string.ui_state_no_data),
                icon = LiquidIcons.DirectionsBus,
                backdropState = backdropState,
            )
        } else {
            uiState.days.forEach { day ->
                LiquidSectionHeader(
                    title = day.date,
                    subtitle = day.directionSummary,
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer(clip = false),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    day.reservations.forEach { reservation ->
                        TransportReservationItem(
                            reservation = reservation,
                            backdropState = backdropState,
                            onClick = { onReservationClick(reservation) }
                        )
                    }
                }
            }
        }
    }
}
