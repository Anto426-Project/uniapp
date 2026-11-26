package com.anto426.uniapp.ui.transport.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.codes.CodeFormat
import com.anto426.uniapp.codes.DecodedCode
import com.anto426.uniapp.data.contract.TransportReservation
import com.anto426.uniapp.data.contract.TripDirection
import com.anto426.uniapp.ui.didactics.components.UniAppBrandLogo
import com.kyant.shapes.Capsule
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun ReservationHeroFrontFace(
    reservation: TransportReservation,
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
        ReservationHeroHeader(
            badgeText = if (isAndata) {
                stringResource(Res.string.ui_transport_direction_andata)
            } else {
                stringResource(Res.string.ui_transport_direction_ritorno)
            },
            subtitle = if (reservation.busNumber.isNotBlank()) {
                stringResource(Res.string.ui_transport_shuttle_confirmed, reservation.busNumber)
            } else {
                stringResource(Res.string.ui_transport_ride_booked)
            },
            pillIcon = LiquidIcons.QrCode,
            pillIconDescription = stringResource(Res.string.ui_qr_code),
            pillText = stringResource(Res.string.ui_transport_quick_qr),
        )

        // Center Content (flat tonal emblem — zero nested .liquidGlass inside UniHeroGlassCard)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(colorScheme.primary.copy(alpha = 0.14f), CircleShape)
                    .border(1.dp, colorScheme.primary.copy(alpha = 0.24f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (isAndata) LiquidIcons.ArrowForward else LiquidIcons.ArrowBack,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(34.dp),
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = reservation.date.ifBlank { stringResource(Res.string.ui_transport_date_unavailable) },
                    style = MaterialTheme.typography.headlineMedium.copy(fontFeatureSettings = "tnum"),
                    fontWeight = FontWeight.Black,
                    color = colorScheme.onSurface,
                    letterSpacing = (-0.6).sp,
                )
                if (reservation.time.isNotBlank()) {
                    Text(
                        text = reservation.time,
                        style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary,
                    )
                }
            }

            LiquidBadge(
                text = if (isAndata) stringResource(Res.string.ui_trip_outbound) else stringResource(Res.string.ui_trip_return),
                containerColor = colorScheme.primary.copy(alpha = 0.14f),
                contentColor = colorScheme.primary,
            )

            if (reservation.departureStop.isNotBlank() || reservation.arrivalStop.isNotBlank()) {
                val stopText = when {
                    reservation.departureStop.isNotBlank() && reservation.arrivalStop.isNotBlank() ->
                        "${reservation.departureStop} → ${reservation.arrivalStop}"
                    else -> reservation.departureStop.ifBlank { reservation.arrivalStop }
                }
                Text(
                    text = stopText,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
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
fun ReservationHeroBackFace(
    reservation: TransportReservation,
    code: DecodedCode?,
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // Top Header
        ReservationHeroHeader(
            badgeText = stringResource(Res.string.ui_transport_validation_badge),
            subtitle = stringResource(Res.string.ui_transport_scan_turnstile_hint),
            pillIcon = LiquidIcons.Info,
            pillIconDescription = stringResource(Res.string.ui_info),
            pillText = stringResource(Res.string.ui_transport_quick_front),
        )

        // Center: Scannable Theme-Tinted Ticket Code + Ticket Number
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TicketCodeImage(
                code = code,
                modifier = if (code?.format == CodeFormat.Qr) {
                    Modifier.size(156.dp)
                } else {
                    Modifier.fillMaxWidth().height(108.dp)
                },
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = reservation.ticketNumber.ifBlank { reservation.id },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = colorScheme.onSurface,
                    letterSpacing = 1.6.sp,
                )

                Text(
                    text = stringResource(Res.string.ui_transport_reservation_back_instruction),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
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
                color = colorScheme.onSurface.copy(alpha = 0.78f),
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
            )
        }
    }
}

@Composable
private fun ReservationHeroHeader(
    badgeText: String,
    subtitle: String,
    pillIcon: ImageVector,
    pillIconDescription: String,
    pillText: String,
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f).padding(end = 8.dp),
        ) {
            UniAppBrandLogo(modifier = Modifier.size(34.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.ui_transport_brand_name),
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurface,
                        letterSpacing = (-0.2).sp,
                        maxLines = 1,
                        softWrap = false,
                    )
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.80f),
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(Capsule())
                .background(color = colorScheme.primary.copy(alpha = 0.12f))
                .border(
                    width = 1.dp,
                    color = colorScheme.primary.copy(alpha = 0.22f),
                    shape = Capsule(),
                )
                .padding(horizontal = 10.dp, vertical = 5.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = pillIcon,
                    contentDescription = pillIconDescription,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = pillText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.primary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }
    }
}
