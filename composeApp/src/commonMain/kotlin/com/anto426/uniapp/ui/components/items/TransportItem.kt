package com.anto426.uniapp.ui.components.items

import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.liquidmonet.components.display.LiquidIconBox
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.model.transport.TransportReservation
import com.anto426.uniapp.model.transport.TransportRoute
import com.anto426.uniapp.model.transport.TransportTicket
import com.kyant.backdrop.Backdrop

@Composable
fun TransportReservationItem(
    reservation: TransportReservation,
    backdropState: Backdrop,
    onClick: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    val isAndata = reservation.direction == com.anto426.uniapp.model.transport.TripDirection.ANDATA

    LiquidCard(
        backdropState = backdropState,
        shape = RoundedCornerShape(20.dp),
        contentPadding = 16.dp,
        onClick = onClick,
        interactiveGelatin = true
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                LiquidIconBox(
                    icon = if (isAndata) LiquidIcons.ArrowForward else LiquidIcons.ArrowBack,
                    size = 40.dp,
                    iconSize = 20.dp,
                    containerColor = colorScheme.primary.copy(alpha = 0.12f),
                    iconTint = colorScheme.primary,
                    shape = RoundedCornerShape(12.dp),
                )

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = reservation.route,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                        letterSpacing = (-0.2).sp
                    )
                    Text(
                        text = "Ore ${reservation.time} • ${if (reservation.departureStop.isNotBlank()) reservation.departureStop else reservation.date}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(contentAlignment = Alignment.Center) {
                LiquidBadge(
                    text = if (isAndata) stringResource(Res.string.ui_trip_outbound) else stringResource(Res.string.ui_trip_return),
                    containerColor = colorScheme.primaryContainer,
                    contentColor = colorScheme.primary,
                    backdropState = backdropState,
                    modifier = Modifier.graphicsLayer {
                        scaleX = 1.05f
                        scaleY = 1.05f
                    }
                )
            }
        }
    }
}

@Composable
fun TransportTicketItem(
    ticket: TransportTicket,
    backdropState: Backdrop,
    onClick: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme

    LiquidCard(
        backdropState = backdropState,
        shape = RoundedCornerShape(20.dp),
        contentPadding = 16.dp,
        onClick = onClick,
        interactiveGelatin = true
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                LiquidIconBox(
                    icon = ticket.icon,
                    size = 40.dp,
                    iconSize = 20.dp,
                    containerColor = colorScheme.primary.copy(alpha = 0.12f),
                    iconTint = colorScheme.primary,
                    shape = RoundedCornerShape(12.dp),
                )

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = ticket.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                        letterSpacing = (-0.2).sp
                    )
                    Text(
                        text = "${ticket.validity} • ${ticket.type}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = ticket.price,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = colorScheme.primary
            )
        }
    }
}

@Composable
fun TransportRouteItem(
    route: TransportRoute,
    backdropState: Backdrop,
    onClick: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme

    LiquidCard(
        backdropState = backdropState,
        shape = RoundedCornerShape(20.dp),
        contentPadding = 16.dp,
        onClick = onClick,
        interactiveGelatin = true
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                LiquidIconBox(
                    icon = LiquidIcons.Time,
                    size = 40.dp,
                    iconSize = 20.dp,
                    containerColor = colorScheme.primary.copy(alpha = 0.12f),
                    iconTint = colorScheme.primary,
                    shape = RoundedCornerShape(12.dp),
                )

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = route.route,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                        letterSpacing = (-0.2).sp
                    )
                    Text(
                        text = route.time,
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(contentAlignment = Alignment.Center) {
                LiquidBadge(
                    text = route.countdown,
                    containerColor = colorScheme.primaryContainer,
                    contentColor = colorScheme.primary,
                    backdropState = backdropState,
                    modifier = Modifier.graphicsLayer {
                        scaleX = 1.05f
                        scaleY = 1.05f
                    }
                )
            }
        }
    }
}
