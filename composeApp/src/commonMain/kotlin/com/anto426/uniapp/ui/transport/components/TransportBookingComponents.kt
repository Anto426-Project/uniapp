package com.anto426.uniapp.ui.transport.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.iconcontainer.liquidIconContainer
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.data.contract.TripBookingDirection
import com.anto426.uniapp.transport.presentation.transportRouteStopsFor
import com.kyant.shapes.RoundedRectangle
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
internal fun TransportBookingSummaryCard(
    selectedRoute: String,
    selectedDirection: TripBookingDirection,
    availableSelectedDates: Set<LocalDate>,
    monthNamesShort: List<String>,
) {
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    val totalRides = availableSelectedDates.size * (if (selectedDirection == TripBookingDirection.ROUND_TRIP) 2 else 1)

    LiquidCard(
        shape = RoundedRectangle(28.dp),
        contentPadding = PaddingValues(20.dp),
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
                    modifier = Modifier.weight(1f),
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
                        letterSpacing = (-0.25).sp,
                        color = colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(Modifier.width(8.dp))

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

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                directions.forEach { direction ->
                    val display = transportRouteStopsFor(selectedRoute, direction)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (directions.size > 1) {
                            Text(
                                text = stringResource(
                                    if (direction == TripBookingDirection.OUTBOUND) Res.string.ui_trip_outbound
                                    else Res.string.ui_trip_return,
                                ).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                color = colorScheme.primary,
                            )
                        }

                        if (display.arrivalStop.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = display.departureStop,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f),
                                )

                                Icon(
                                    imageVector = LiquidIcons.ArrowForward,
                                    contentDescription = null,
                                    tint = colorScheme.primary,
                                    modifier = Modifier
                                        .padding(horizontal = 10.dp)
                                        .liquidIconContainer(
                                            containerSize = 28.dp,
                                            iconSize = 15.dp,
                                            containerColor = colorScheme.primary.copy(alpha = 0.14f),
                                            shape = CircleShape,
                                        ),
                                )

                                Text(
                                    text = display.arrivalStop,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.End,
                                )
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
                }
            }

            LiquidHorizontalDivider(color = colorScheme.onSurface.copy(alpha = 0.08f))

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
                        modifier = Modifier.size(16.dp),
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
                        style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
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

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = LiquidIcons.Check,
                    contentDescription = null,
                    tint = glassColors.success,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = stringResource(Res.string.ui_transport_seat_reserved_title),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = glassColors.success,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
