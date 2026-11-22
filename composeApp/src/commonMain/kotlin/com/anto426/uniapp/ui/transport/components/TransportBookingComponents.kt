package com.anto426.uniapp.ui.transport.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.iconcontainer.liquidIconContainer
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.data.contract.TripBookingDirection
import com.anto426.uniapp.transport.presentation.transportRouteStopsFor
import com.kyant.shapes.RoundedRectangle
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
internal fun TransportRouteStopsPreviewCard(
    selectedRoute: String,
    selectedDirection: TripBookingDirection,
) {
    if (selectedRoute.isBlank()) return
    val outbound = transportRouteStopsFor(selectedRoute, TripBookingDirection.OUTBOUND)
    val returning = transportRouteStopsFor(selectedRoute, TripBookingDirection.RETURN)
    if (outbound.departureStop.isBlank() || outbound.arrivalStop.isBlank()) return

    val colorScheme = MaterialTheme.colorScheme
    LiquidCard(
        shape = RoundedRectangle(18.dp),
        contentPadding = 14.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = LiquidIcons.Time,
                contentDescription = null,
                tint = colorScheme.primary,
                modifier = Modifier.liquidIconContainer(
                    containerSize = 36.dp,
                    iconSize = 18.dp,
                    containerColor = colorScheme.primary.copy(alpha = 0.14f),
                    shape = RoundedRectangle(11.dp),
                ),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when (selectedDirection) {
                        TripBookingDirection.OUTBOUND -> "${outbound.departureStop} → ${outbound.arrivalStop}"
                        TripBookingDirection.RETURN -> "${returning.departureStop} → ${returning.arrivalStop}"
                        TripBookingDirection.ROUND_TRIP -> "${outbound.departureStop} ⇄ ${outbound.arrivalStop}"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface,
                )
                Text(
                    text = selectedRoute,
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
internal fun TransportBookingSummaryCard(
    selectedRoute: String,
    selectedDirection: TripBookingDirection,
    availableSelectedDates: Set<LocalDate>,
    monthNamesShort: List<String>,
) {
    val colorScheme = MaterialTheme.colorScheme
    val totalRides = availableSelectedDates.size * (if (selectedDirection == TripBookingDirection.ROUND_TRIP) 2 else 1)

    LiquidCard(
        shape = RoundedRectangle(24.dp),
        contentPadding = 18.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        imageVector = LiquidIcons.Badge,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.liquidIconContainer(
                            containerSize = 34.dp,
                            iconSize = 18.dp,
                            containerColor = colorScheme.primary.copy(alpha = 0.14f),
                            shape = RoundedRectangle(10.dp),
                        ),
                    )
                    Text(
                        text = stringResource(Res.string.ui_transport_summary_ride),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                    )
                }

                LiquidBadge(
                    text = when (selectedDirection) {
                        TripBookingDirection.ROUND_TRIP -> stringResource(Res.string.ui_transport_round_trip_caps)
                        TripBookingDirection.RETURN -> stringResource(Res.string.ui_transport_return_only_caps)
                        else -> stringResource(Res.string.ui_transport_outbound_only_caps)
                    },
                    containerColor = colorScheme.primary.copy(alpha = 0.14f),
                    contentColor = colorScheme.primary,
                )
            }

            val directions = if (selectedDirection == TripBookingDirection.ROUND_TRIP) {
                listOf(TripBookingDirection.OUTBOUND, TripBookingDirection.RETURN)
            } else {
                listOf(selectedDirection)
            }

            directions.forEach { direction ->
                val display = transportRouteStopsFor(selectedRoute, direction)
                if (directions.size > 1) {
                    Text(
                        text = stringResource(
                            if (direction == TripBookingDirection.OUTBOUND) Res.string.ui_trip_outbound
                            else Res.string.ui_trip_return,
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary,
                    )
                }

                if (display.arrivalStop.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = display.departureStop,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }

                        Icon(
                            imageVector = LiquidIcons.ArrowForward,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier
                                .padding(horizontal = 10.dp)
                                .liquidIconContainer(
                                    containerSize = 30.dp,
                                    iconSize = 16.dp,
                                    containerColor = colorScheme.primary.copy(alpha = 0.14f),
                                    shape = CircleShape,
                                ),
                        )

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.End,
                        ) {
                            Text(
                                text = display.arrivalStop,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                } else {
                    Text(
                        text = display.label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary,
                    )
                }
            }

            LiquidHorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = LiquidIcons.Check,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.liquidIconContainer(
                        containerSize = 34.dp,
                        iconSize = 18.dp,
                        containerColor = colorScheme.primary.copy(alpha = 0.14f),
                        shape = CircleShape,
                    ),
                )

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(Res.string.ui_transport_seat_reserved_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(Res.string.ui_transport_seat_reserved_desc),
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant,
                    )
                }
            }

            LiquidHorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = LiquidIcons.Calendar,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                    val formattedDateSummary = remember(availableSelectedDates, monthNamesShort) {
                        if (availableSelectedDates.size == 1) {
                            val single = availableSelectedDates.first()
                            "${single.day} ${monthNamesShort[single.month.number - 1]} ${single.year}"
                        } else {
                            availableSelectedDates.sorted().joinToString(", ") {
                                "${it.day} ${monthNamesShort[it.month.number - 1]}"
                            }
                        }
                    }
                    Text(
                        text = formattedDateSummary,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurface,
                        maxLines = 2,
                    )
                }

                Spacer(Modifier.width(12.dp))

                LiquidBadge(
                    text = stringResource(
                        Res.string.ui_transport_rides_guaranteed_format,
                        totalRides,
                        if (totalRides > 1) stringResource(Res.string.ui_transport_rides_plural) else stringResource(Res.string.ui_transport_ride_singular),
                    ),
                    containerColor = colorScheme.primary.copy(alpha = 0.14f),
                    contentColor = colorScheme.primary,
                )
            }
        }
    }
}
