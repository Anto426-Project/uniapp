package com.anto426.uniapp.ui.components.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.iconcontainer.liquidIconContainer
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.model.transport.TransportReservation
import com.anto426.uniapp.model.transport.TripDirection
import com.kyant.shapes.RoundedRectangle
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
internal fun TransportReservationContent(reservation: TransportReservation) {
    val colorScheme = MaterialTheme.colorScheme
    val isAndata = reservation.direction == TripDirection.ANDATA
    val accentColor = if (isAndata) colorScheme.primary else colorScheme.secondary

    val stopsLabel = when {
        reservation.departureStop.isNotBlank() && reservation.arrivalStop.isNotBlank() ->
            "${reservation.departureStop} → ${reservation.arrivalStop}"
        reservation.departureStop.isNotBlank() -> reservation.departureStop
        else -> null
    }

    val metaText = buildList {
        reservation.time.takeIf(String::isNotBlank)?.let(::add)
        stopsLabel?.let(::add)
        reservation.busNumber.takeIf(String::isNotBlank)?.let { bus ->
            add(stringResource(Res.string.ui_transport_navetta_number, bus))
        }
        if (isEmpty()) {
            add(reservation.date.ifBlank { stringResource(Res.string.ui_transport_date_unavailable) })
        }
    }.joinToString(" • ")

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Icon(
            imageVector = if (isAndata) LiquidIcons.ArrowForward else LiquidIcons.ArrowBack,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.liquidIconContainer(
                containerSize = 40.dp,
                iconSize = 20.dp,
                containerColor = accentColor.copy(alpha = 0.12f),
                shape = RoundedRectangle(12.dp),
            ),
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = reservation.route,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = metaText,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            LiquidBadge(
                text = stringResource(
                    if (isAndata) Res.string.ui_transport_outbound_short
                    else Res.string.ui_transport_return_short,
                ),
                containerColor = accentColor.copy(alpha = 0.14f),
                contentColor = accentColor,
            )
            Icon(
                imageVector = LiquidIcons.ChevronRight,
                contentDescription = null,
                tint = colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
