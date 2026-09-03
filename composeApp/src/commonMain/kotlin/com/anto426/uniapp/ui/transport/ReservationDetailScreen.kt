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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.anto426.uniapp.ui.didactics.components.QrCodeMatrixCanvas
import com.anto426.uniapp.ui.didactics.components.UniAppBrandLogo
import com.kyant.backdrop.Backdrop
import com.kyant.shapes.Capsule
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun ReservationDetailScreen(
    reservation: TransportReservation,
    backdropState: Backdrop,
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
                backdropState = backdropState,
                height = 370.dp,
                flipTrigger = UniHeroFlipTrigger.CLICK,
                frontContent = {
                    ReservationHeroFrontFace(
                        reservation = reservation,
                        backdropState = backdropState,
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
                backdropState = backdropState,
            ) {
                LiquidPreferenceItem(
                    title = stringResource(Res.string.ui_trip_route),
                    subtitle = reservation.route,
                    icon = LiquidIcons.Star,
                    backdropState = backdropState,
                )
                LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
                LiquidPreferenceItem(
                    title = stringResource(Res.string.ui_transport_trip_date),
                    subtitle = reservation.date,
                    icon = LiquidIcons.Calendar,
                    backdropState = backdropState,
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
                    backdropState = backdropState,
                )
                if (reservation.departureStop.isNotBlank()) {
                    LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
                    LiquidPreferenceItem(
                        title = stringResource(Res.string.ui_trip_departure_stop),
                        subtitle = reservation.departureStop,
                        icon = LiquidIcons.Home,
                        backdropState = backdropState,
                    )
                }
                if (reservation.busNumber.isNotBlank()) {
                    LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
                    LiquidPreferenceItem(
                        title = stringResource(Res.string.ui_trip_bus_assigned),
                        subtitle = stringResource(Res.string.ui_transport_navetta_number, reservation.busNumber),
                        icon = LiquidIcons.Info,
                        backdropState = backdropState,
                    )
                }
            }

            Spacer(Modifier.height(130.dp))
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
                backdropState = backdropState,
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
                backdropState = backdropState,
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

@Composable
private fun ReservationHeroFrontFace(
    reservation: TransportReservation,
    backdropState: Backdrop,
) {
    val colorScheme = MaterialTheme.colorScheme
    val isAndata = reservation.direction == TripDirection.ANDATA

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f, fill = false),
            ) {
                UniAppBrandLogo(modifier = Modifier.size(40.dp))

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = stringResource(Res.string.ui_transport_brand_name),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = colorScheme.onSurface,
                            letterSpacing = (-0.3).sp,
                        )
                        Text(
                            text = if (isAndata) stringResource(Res.string.ui_transport_direction_andata) else stringResource(Res.string.ui_transport_direction_ritorno),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.primary,
                        )
                    }
                    Text(
                        text = if (reservation.busNumber.isNotBlank()) stringResource(Res.string.ui_transport_shuttle_confirmed, reservation.busNumber) else stringResource(Res.string.ui_transport_ride_booked),
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.78f),
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                    )
                }
            }

            // Quick Flip Indicator Pill
            Box(
                modifier = Modifier
                    .background(
                        color = colorScheme.surface.copy(alpha = 0.22f),
                        shape = RoundedCornerShape(16.dp),
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = LiquidIcons.QrCode,
                        contentDescription = stringResource(Res.string.ui_qr_code),
                        tint = colorScheme.primary,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = stringResource(Res.string.ui_transport_quick_qr),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary,
                        fontSize = 11.sp,
                    )
                }
            }
        }

        // Center Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .liquidGlass(
                        backdrop = backdropState,
                        shape = Capsule(),
                        role = LiquidGlassRole.Control,
                        containerColor = colorScheme.primary.copy(alpha = 0.14f),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (isAndata) LiquidIcons.ArrowForward else LiquidIcons.ArrowBack,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(38.dp),
                )
            }

            Text(
                text = reservation.date,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = colorScheme.onSurface,
                letterSpacing = (-0.5).sp,
            )

            LiquidBadge(
                text = if (isAndata) stringResource(Res.string.ui_trip_outbound) else stringResource(Res.string.ui_trip_return),
                containerColor = colorScheme.primaryContainer,
                contentColor = colorScheme.primary,
                backdropState = backdropState,
            )

            if (reservation.departureStop.isNotBlank()) {
                Text(
                    text = reservation.departureStop,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                )
            }
        }

        // Bottom Tap to Flip Hint
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = LiquidIcons.Refresh,
                contentDescription = null,
                tint = colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                modifier = Modifier.size(13.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(Res.string.ui_transport_ticket_flip_hint),
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
            )
        }
    }
}

@Composable
private fun ReservationHeroBackFace(
    reservation: TransportReservation,
    rawCode: String,
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f, fill = false),
            ) {
                UniAppBrandLogo(modifier = Modifier.size(40.dp))

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = stringResource(Res.string.ui_transport_brand_name),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = colorScheme.onSurface,
                            letterSpacing = (-0.3).sp,
                        )
                        Text(
                            text = stringResource(Res.string.ui_transport_validation_badge),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.primary,
                        )
                    }
                    Text(
                        text = stringResource(Res.string.ui_transport_scan_turnstile_hint),
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.78f),
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                    )
                }
            }

            // Flip Back Pill
            Box(
                modifier = Modifier
                    .background(
                        color = colorScheme.surface.copy(alpha = 0.22f),
                        shape = RoundedCornerShape(16.dp),
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = LiquidIcons.Info,
                        contentDescription = stringResource(Res.string.ui_info),
                        tint = colorScheme.primary,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = stringResource(Res.string.ui_transport_quick_front),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary,
                        fontSize = 11.sp,
                    )
                }
            }
        }

        // Center: Scannable Vector QR Code Canvas in rounded container + Monospace Code
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = colorScheme.surface.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(20.dp),
                    )
                    .padding(14.dp),
                contentAlignment = Alignment.Center,
            ) {
                QrCodeMatrixCanvas(
                    codeValue = rawCode,
                    color = colorScheme.onSurface,
                    modifier = Modifier.size(136.dp),
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = rawCode,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = colorScheme.onSurface,
                    letterSpacing = 1.8.sp,
                )

                Text(
                    text = stringResource(Res.string.ui_transport_reservation_back_instruction),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                    maxLines = 1,
                )
            }
        }

        // Bottom: Flip back note
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = LiquidIcons.Check,
                contentDescription = null,
                tint = colorScheme.primary,
                modifier = Modifier.size(14.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(Res.string.ui_transport_tap_return_front),
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
            )
        }
    }
}
