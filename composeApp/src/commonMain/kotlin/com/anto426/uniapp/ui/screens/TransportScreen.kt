package com.anto426.uniapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.liquidmonet.components.display.LiquidSectionTitle
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.data.UiInitialData
import com.anto426.uniapp.ui.models.TransportReservation
import com.kyant.backdrop.Backdrop

@Composable
fun TransportScreen(
    backdropState: Backdrop,
    onReservationClick: (TransportReservation) -> Unit = {}
) {
    val reservations = UiInitialData.myTransportReservations
    val colorScheme = MaterialTheme.colorScheme

    UniScreenColumn {
        LiquidSectionTitle(
            title = "I miei Biglietti",
            subtitle = "Viaggi prenotati e pronti all'uso"
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (reservations.isEmpty()) {
                Text(
                    text = "Non hai viaggi prenotati per i prossimi giorni.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                reservations.forEach { reservation ->
                    ReservationListItem(reservation, backdropState) { onReservationClick(reservation) }
                }
            }
        }

        // Space for global FAB
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun ReservationListItem(
    reservation: TransportReservation,
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
            Column(modifier = Modifier.weight(1f)) {
                Text(reservation.route, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${reservation.date} • ${reservation.time}", style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
            }

            LiquidBadge(
                text = reservation.status.uppercase(),
                containerColor = if (reservation.status == "Confermato") colorScheme.primary.copy(alpha = 0.2f) else colorScheme.secondary.copy(alpha = 0.2f),
                contentColor = if (reservation.status == "Confermato") colorScheme.primary else colorScheme.secondary,
                backdropState = backdropState
            )
        }
    }
}
