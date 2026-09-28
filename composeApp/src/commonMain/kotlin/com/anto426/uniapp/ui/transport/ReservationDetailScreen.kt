package com.anto426.uniapp.ui.transport

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.preferenceitem.LiquidPreferenceGroup
import com.anto426.liquidmonet.components.cards.preferenceitem.LiquidPreferenceItem
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.codes.CodeFormat
import com.anto426.uniapp.model.transport.TransportReservation
import com.anto426.uniapp.model.transport.TripDirection
import com.anto426.uniapp.transport.presentation.TicketImageUiState
import com.anto426.uniapp.ui.components.cards.UniHeroFlipTrigger
import com.anto426.uniapp.ui.components.cards.UniHeroGlassCard
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
    UniScreenColumn {
        // 1. Hero Ticket Card (Student Banner Structure with 3D Flip)
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

        TransportTicketActions(imageState, onReloadImage)

        // 2. Info Group (Dettagli Biglietto e Viaggio)
        LiquidPreferenceGroup(
            title = stringResource(Res.string.ui_trip_details),
        ) {
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_trip_route),
                subtitle = reservation.route,
                icon = LiquidIcons.Star,
            )
            LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_transport_trip_date),
                subtitle = reservation.date.ifBlank { stringResource(Res.string.ui_transport_date_unavailable) } +
                    reservation.time.takeIf(String::isNotBlank)?.let { " • $it" }.orEmpty(),
                icon = LiquidIcons.Calendar,
            )
            LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
            LiquidPreferenceItem(
                title = stringResource(Res.string.ui_transport_direction_title),
                subtitle = if (reservation.direction == TripDirection.ANDATA) {
                    stringResource(Res.string.ui_trip_outbound)
                } else {
                    stringResource(Res.string.ui_trip_return)
                },
                icon = if (reservation.direction == TripDirection.ANDATA) LiquidIcons.ArrowForward else LiquidIcons.ArrowBack,
            )
            if (reservation.departureStop.isNotBlank()) {
                LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
                LiquidPreferenceItem(
                    title = stringResource(Res.string.ui_trip_departure_stop),
                    subtitle = reservation.departureStop,
                    icon = LiquidIcons.Home,
                )
            }
            if (reservation.arrivalStop.isNotBlank()) {
                LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
                LiquidPreferenceItem(
                    title = stringResource(Res.string.ui_trip_arrival_stop),
                    subtitle = reservation.arrivalStop,
                    icon = LiquidIcons.Home,
                )
            }
            if (reservation.busNumber.isNotBlank()) {
                LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
                LiquidPreferenceItem(
                    title = stringResource(Res.string.ui_trip_bus_assigned),
                    subtitle = stringResource(Res.string.ui_transport_navetta_number, reservation.busNumber),
                    icon = LiquidIcons.Info,
                )
            }
            if (reservation.id.isNotBlank()) {
                LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
                LiquidPreferenceItem(
                    title = stringResource(Res.string.ui_transport_ticket_title_badge),
                    subtitle = reservation.ticketNumber.ifBlank { reservation.id },
                    icon = LiquidIcons.QrCode,
                )
            }
        }
    }
}
