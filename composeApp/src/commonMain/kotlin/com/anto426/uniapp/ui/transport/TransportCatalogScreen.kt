package com.anto426.uniapp.ui.transport

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.display.LiquidEmptyState
import com.anto426.liquidmonet.components.display.LiquidSectionHeader
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.model.transport.TransportTicket
import com.anto426.uniapp.transport.presentation.TransportCatalogUiState
import com.anto426.uniapp.ui.components.items.TransportTicketItem
import com.anto426.uniapp.ui.components.layout.UniScreenColumn

import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun TransportCatalogScreen(
    uiState: TransportCatalogUiState,
    onTicketClick: (TransportTicket) -> Unit = {}
) {
    UniScreenColumn {
        LiquidSectionHeader(
            title = stringResource(Res.string.ui_transport_catalog_title),
            subtitle = stringResource(Res.string.ui_transport_catalog_sub)
        )

        if (uiState.tickets.isEmpty()) {
            LiquidEmptyState(
                title = stringResource(Res.string.ui_transport_empty_reservations),
                description = stringResource(Res.string.ui_state_no_data),
                icon = LiquidIcons.CreditCard,
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer(clip = false),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                uiState.tickets.forEach { ticket ->
                    TransportTicketItem(
                        ticket = ticket,
                        onClick = { onTicketClick(ticket) }
                    )
                }
            }
        }
    }
}
