package com.anto426.uniapp.ui.transport

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.DpOffset
import com.anto426.liquidmonet.components.menu.LiquidDropdownMenu
import com.anto426.liquidmonet.components.menu.LiquidMenuItem
import com.anto426.liquidmonet.glass.overlay.LiquidGlassDropdownPlacement
import com.anto426.liquidmonet.glass.overlay.liquidGlassOverlayAnchor
import com.anto426.liquidmonet.glass.overlay.rememberLiquidGlassOverlayAnchorState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.buttons.LiquidFloatingActionButton
import com.anto426.liquidmonet.components.cards.LiquidPreferenceGroup
import com.anto426.liquidmonet.components.cards.LiquidPreferenceItem
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.liquidmonet.components.display.LiquidHorizontalDivider
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.model.transport.TransportReservation
import com.anto426.uniapp.model.transport.TripDirection
import com.anto426.uniapp.ui.components.cards.UniHeroFlipTrigger
import com.anto426.uniapp.ui.components.cards.UniHeroGlassCard
import com.anto426.uniapp.ui.components.layout.LocalNavigationBarVisible
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.transport.components.ReservationHeroBackFace
import com.anto426.uniapp.ui.transport.components.ReservationHeroFrontFace
import com.kyant.shapes.Capsule
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun ReservationDetailScreen(
    reservation: TransportReservation,
    isDeleting: Boolean,
    onDelete: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val officialUrl = reservation.ticketUrl.ifBlank { "https://unimol.esse3.cineca.it" }
    val rawCode = if (reservation.qrCodeData.startsWith("http")) {
        "TKT-${reservation.id.uppercase()}"
    } else {
        reservation.qrCodeData.ifBlank { "TKT-${reservation.id.uppercase()}" }
    }
    var menuExpanded by remember { mutableStateOf(false) }
    val isFabVisible = LocalNavigationBarVisible.current
    val anchorState = rememberLiquidGlassOverlayAnchorState()

    LaunchedEffect(isFabVisible) {
        if (!isFabVisible) menuExpanded = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        UniScreenColumn {
            // 1. Hero Ticket Card (Student Banner Structure with 3D Flip)
            UniHeroGlassCard(
                height = 370.dp,
                flipTrigger = UniHeroFlipTrigger.CLICK,
                frontContent = {
                    ReservationHeroFrontFace(
                        reservation = reservation
                    )
                },
                backContent = {
                    ReservationHeroBackFace(
                        reservation = reservation,
                        rawCode = rawCode,
                    )
                },
            )

            // 2. Info Group (Dettagli Biglietto)
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
                    subtitle = reservation.date,
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
                if (reservation.busNumber.isNotBlank()) {
                    LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
                    LiquidPreferenceItem(
                        title = stringResource(Res.string.ui_trip_bus_assigned),
                        subtitle = stringResource(Res.string.ui_transport_navetta_number, reservation.busNumber),
                        icon = LiquidIcons.Info,
                    )
                }
            }
        }

        // 2. Floating Action Button with Liquid Glass Dropdown Menu
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 112.dp)
                .liquidGlassOverlayAnchor(anchorState),
        ) {
            LiquidFloatingActionButton(
                onClick = { menuExpanded = !menuExpanded },
                visible = isFabVisible,
            ) {
                Icon(
                    imageVector = if (menuExpanded) LiquidIcons.Close else LiquidIcons.Settings,
                    contentDescription = stringResource(Res.string.ui_transport_ticket_options),
                    tint = Color.White,
                )
            }

            LiquidDropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                anchorState = anchorState,
                placement = LiquidGlassDropdownPlacement.AboveEnd,
                offset = DpOffset(0.dp, (-8).dp),
            ) {
                LiquidMenuItem(
                    text = stringResource(Res.string.ui_transport_show_official_ticket),
                    icon = LiquidIcons.Info,
                    onClick = {
                        menuExpanded = false
                        uriHandler.openUri(officialUrl)
                    },
                )
                LiquidMenuItem(
                    text = if (isDeleting) stringResource(Res.string.ui_transport_canceling) else stringResource(Res.string.ui_cancel_booking),
                    icon = LiquidIcons.Close,
                    destructive = true,
                    enabled = !isDeleting,
                    onClick = {
                        menuExpanded = false
                        onDelete()
                    },
                )
            }
        }
    }
}
