package com.anto426.uniapp.ui.components.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.data.contract.TransportReservation
import com.anto426.uniapp.data.contract.TripDirection
import com.anto426.uniapp.ui.components.display.UniInsetRow
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
internal fun TransportReservationItem(
    reservation: TransportReservation,
    onClick: (() -> Unit)?,
    showDivider: Boolean,
) {
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

    UniInsetRow(
        title = reservation.route,
        subtitle = metaText,
        icon = if (isAndata) LiquidIcons.ArrowForward else LiquidIcons.ArrowBack,
        iconTint = accentColor,
        onClick = onClick,
        showDivider = showDivider,
        titleMaxLines = 2,
        trailingContent = {
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
                if (onClick != null) {
                    Icon(
                        imageVector = LiquidIcons.ChevronRight,
                        contentDescription = null,
                        tint = colorScheme.onSurfaceVariant.copy(alpha = 0.42f),
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        },
    )
}
