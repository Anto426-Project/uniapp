package com.anto426.uniapp.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.display.LiquidSectionTitle
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.android.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.android.ui.data.UiInitialData
import com.anto426.uniapp.android.ui.models.TransportTicket
import com.kyant.backdrop.Backdrop

@Composable
fun TransportCatalogScreen(
    backdropState: Backdrop,
    onTicketClick: (TransportTicket) -> Unit = {}
) {
    val tickets = UiInitialData.availableTickets

    UniScreenColumn {
        LiquidSectionTitle(
            title = "Biglietteria",
            subtitle = "Scegli il titolo di viaggio più adatto"
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            tickets.forEach { ticket ->
                TicketListItem(ticket, backdropState) { onTicketClick(ticket) }
            }
        }
    }
}

@Composable
private fun TicketListItem(
    ticket: TransportTicket,
    backdropState: Backdrop,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    LiquidCard(
        backdropState = backdropState,
        shape = RoundedCornerShape(24.dp),
        onClick = onClick,
        contentPadding = 16.dp,
        interactiveGelatin = true
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(ticket.icon, contentDescription = null, tint = colorScheme.primary, modifier = Modifier.size(24.dp))
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(ticket.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(ticket.validity, style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(ticket.price, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = colorScheme.primary)
                Text(ticket.type, style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            }
        }
    }
}
