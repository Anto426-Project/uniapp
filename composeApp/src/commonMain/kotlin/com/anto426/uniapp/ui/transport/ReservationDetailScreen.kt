package com.anto426.uniapp.ui.transport

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.codes.CodeFormat
import com.anto426.uniapp.data.contract.TransportReservation
import com.anto426.uniapp.data.contract.TripDirection
import com.anto426.uniapp.transport.presentation.TicketImageUiState
import com.anto426.uniapp.ui.components.cards.UniHeroFlipTrigger
import com.anto426.uniapp.ui.components.cards.UniHeroGlassCard
import com.anto426.uniapp.ui.components.display.UniInsetGroup
import com.anto426.uniapp.ui.components.display.UniInsetRow
import com.anto426.uniapp.ui.components.layout.UniAnimatedEntrance
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.transport.components.ReservationHeroBackFace
import com.anto426.uniapp.ui.transport.components.ReservationHeroFrontFace
import com.anto426.uniapp.ui.transport.components.TransportTicketActions
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun ReservationDetailScreen(
    reservation: TransportReservation,
    imageState: TicketImageUiState,
    onReloadImage: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val isAndata = reservation.direction == TripDirection.ANDATA

    val detailRows = buildList {
        add(
            Triple(
                stringResource(Res.string.ui_trip_route),
                reservation.route,
                LiquidIcons.Star to colorScheme.primary,
            ),
        )
        add(
            Triple(
                stringResource(Res.string.ui_transport_trip_date),
                reservation.date.ifBlank { stringResource(Res.string.ui_transport_date_unavailable) } +
                    reservation.time.takeIf(String::isNotBlank)?.let { " • $it" }.orEmpty(),
                LiquidIcons.Calendar to colorScheme.primary,
            ),
        )
        add(
            Triple(
                stringResource(Res.string.ui_transport_direction_title),
                if (isAndata) {
                    stringResource(Res.string.ui_trip_outbound)
                } else {
                    stringResource(Res.string.ui_trip_return)
                },
                (if (isAndata) LiquidIcons.ArrowForward else LiquidIcons.ArrowBack) to colorScheme.secondary,
            ),
        )
        if (reservation.departureStop.isNotBlank()) {
            add(
                Triple(
                    stringResource(Res.string.ui_trip_departure_stop),
                    reservation.departureStop,
                    LiquidIcons.Home to colorScheme.tertiary,
                ),
            )
        }
        if (reservation.arrivalStop.isNotBlank()) {
            add(
                Triple(
                    stringResource(Res.string.ui_trip_arrival_stop),
                    reservation.arrivalStop,
                    LiquidIcons.Home to colorScheme.tertiary,
                ),
            )
        }
        if (reservation.busNumber.isNotBlank()) {
            add(
                Triple(
                    stringResource(Res.string.ui_trip_bus_assigned),
                    stringResource(Res.string.ui_transport_navetta_number, reservation.busNumber),
                    LiquidIcons.Info to colorScheme.primary,
                ),
            )
        }
        if (reservation.id.isNotBlank()) {
            add(
                Triple(
                    stringResource(Res.string.ui_transport_ticket_title_badge),
                    reservation.ticketNumber.ifBlank { reservation.id },
                    LiquidIcons.QrCode to colorScheme.secondary,
                ),
            )
        }
    }

    UniScreenColumn {
        // 1. Hero Ticket Card with 3D Flip
        UniAnimatedEntrance(order = 0) {
            UniHeroGlassCard(
                height = 370.dp,
                flipTrigger = UniHeroFlipTrigger.CLICK,
                frontContent = {
                    ReservationHeroFrontFace(
                        reservation = reservation,
                    )
                },
                backContent = {
                    ReservationHeroBackFace(
                        reservation = reservation,
                        code = imageState.codes.firstOrNull { it.format == CodeFormat.Qr && it.canRegenerate }
                            ?: imageState.codes.firstOrNull { it.canRegenerate && it.format != CodeFormat.Unsupported },
                    )
                },
            )
        }

        UniAnimatedEntrance(order = 1) {
            TransportTicketActions(imageState, onReloadImage)
        }

        // 2. Dettagli Biglietto e Viaggio
        UniAnimatedEntrance(order = 2) {
            LiquidSectionHeader(
                title = stringResource(Res.string.ui_trip_details),
            )
        }

        UniInsetGroup(entranceOrder = 2) {
            detailRows.forEachIndexed { index, (title, value, iconPair) ->
                UniInsetRow(
                    title = title,
                    subtitle = value,
                    icon = iconPair.first,
                    iconTint = iconPair.second,
                    showDivider = index < detailRows.lastIndex,
                )
            }
        }
    }
}
