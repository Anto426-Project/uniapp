package com.anto426.uniapp.ui.components.items

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
    val primaryTitle = stopsLabel ?: reservation.route

    val scheduleMeta = buildList {
        reservation.time.takeIf(String::isNotBlank)?.let(::add)
        reservation.busNumber.takeIf(String::isNotBlank)?.let { bus ->
            add(stringResource(Res.string.ui_transport_navetta_number, bus))
        }
    }.joinToString(" · ")
    val secondarySubtitle = scheduleMeta.ifBlank {
        reservation.date.ifBlank { stringResource(Res.string.ui_transport_date_unavailable) }
    }

    UniInsetRow(
        title = primaryTitle,
        icon = if (isAndata) LiquidIcons.ArrowForward else LiquidIcons.ArrowBack,
        iconTint = accentColor,
        onClick = onClick,
        showDivider = showDivider,
        showTrailingChevron = onClick != null,
        subtitleContent = {
            Text(
                text = secondarySubtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (scheduleMeta.isNotBlank()) FontWeight.SemiBold else FontWeight.Medium,
                    fontFeatureSettings = "tnum",
                ),
                color = if (scheduleMeta.isNotBlank()) accentColor else colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        trailingContent = {
            LiquidBadge(
                text = stringResource(
                    if (isAndata) Res.string.ui_transport_outbound_short
                    else Res.string.ui_transport_return_short,
                ),
                containerColor = accentColor.copy(alpha = 0.14f),
                contentColor = accentColor,
            )
        },
    )
}
