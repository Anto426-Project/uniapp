package com.anto426.uniapp.ui.transport

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.buttons.button.LiquidButton
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonVariant
import com.anto426.liquidmonet.components.buttons.iconbutton.LiquidIconButton
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeaderSize
import com.anto426.liquidmonet.components.navigation.navigationbar.LiquidNavigationItem
import com.anto426.liquidmonet.components.navigation.tabbar.LiquidTabBar
import com.anto426.liquidmonet.components.selection.select.LiquidSelect
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.transport.presentation.TransportBookingUiState
import com.anto426.uniapp.transport.presentation.isTransportDateBooked
import com.anto426.uniapp.transport.presentation.isTransportBookingDateAllowed
import com.anto426.uniapp.transport.presentation.transportBookingWindow
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.unisdk.transport.TransportDirection
import com.anto426.unisdk.transport.TransportRouteData
import com.anto426.unisdk.transport.displayFor
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransportBookingScreen(
    uiState: TransportBookingUiState,
    onRouteSelected: (String) -> Unit,
    onBook: (List<LocalDate>, TransportDirection) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    var selectedDirection by remember { mutableStateOf(TransportDirection.OUTBOUND) }

    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val lastBookableDate = remember(today) { transportBookingWindow(today).last() }
    var displayedYear by remember { mutableIntStateOf(today.year) }
    var displayedMonth by remember { mutableIntStateOf(today.month.number) }
    var selectedDates by remember { mutableStateOf(emptySet<LocalDate>()) }
    val availableSelectedDates = selectedDates.filterTo(mutableSetOf()) { date ->
        isTransportBookingDateAllowed(date, today) &&
            !isTransportDateBooked(date, uiState.selectedRouteCode, selectedDirection, uiState.bookedRequests)
    }
    LaunchedEffect(today, selectedDirection, uiState.selectedRouteCode, uiState.bookedRequests) {
        selectedDates = availableSelectedDates
    }
    val displayedMonthIndex = displayedYear * 12 + displayedMonth
    val firstMonthIndex = today.year * 12 + today.month.number
    val lastMonthIndex = lastBookableDate.year * 12 + lastBookableDate.month.number
    LaunchedEffect(today) {
        if (displayedMonthIndex !in firstMonthIndex..lastMonthIndex) {
            displayedYear = today.year
            displayedMonth = today.month.number
        }
    }
    val monthNames = listOf(
        "Gennaio", "Febbraio", "Marzo", "Aprile", "Maggio", "Giugno",
        "Luglio", "Agosto", "Settembre", "Ottobre", "Novembre", "Dicembre",
    )
    val monthNamesShort = listOf(
        "Gen", "Feb", "Mar", "Apr", "Mag", "Giu",
        "Lug", "Ago", "Set", "Ott", "Nov", "Dic",
    )

    val currentMonthTitle = "${monthNames[displayedMonth - 1]} $displayedYear"
    val firstDayOffset = remember(displayedYear, displayedMonth) {
        LocalDate(displayedYear, displayedMonth, 1).dayOfWeek.ordinal
    }
    val daysInCurrentMonth = remember(displayedYear, displayedMonth) {
        when (displayedMonth) {
            2 -> if (displayedYear % 400 == 0 || (displayedYear % 4 == 0 && displayedYear % 100 != 0)) 29 else 28
            4, 6, 9, 11 -> 30
            else -> 31
        }
    }

    UniScreenColumn {
        // 1. Header & Selezione Tratta
        LiquidSectionHeader(
            title = stringResource(Res.string.ui_trip_route),
        )

        LiquidSelect(
            items = uiState.routes,
            selectedItem = uiState.selectedRoute,
            onItemSelected = onRouteSelected,
            label = stringResource(Res.string.ui_trip_route),
            modifier = Modifier.fillMaxWidth(),
        )
        if (uiState.selectedRoute.isNotBlank()) {
            val route = TransportRouteData(code = "", label = uiState.selectedRoute)
            val outbound = route.displayFor(TransportDirection.OUTBOUND).label
            val returning = route.displayFor(TransportDirection.RETURN).label
            Text(
                text = when (selectedDirection) {
                    TransportDirection.OUTBOUND -> outbound
                    TransportDirection.RETURN -> returning
                    TransportDirection.ROUND_TRIP -> "$outbound • $returning"
                },
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant,
            )
        }

        // 2. Selezione Direzione (Andata, Ritorno, Andata e Ritorno) con LiquidTabBar
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LiquidSectionHeader(
                title = stringResource(Res.string.ui_transport_direction_title),
                size = LiquidSectionHeaderSize.Small,
            )

            val outboundLabel = stringResource(Res.string.ui_trip_outbound)
            val returnLabel = stringResource(Res.string.ui_trip_return)
            val roundTripLabel = stringResource(Res.string.ui_transport_round_trip)
            val directionItems = remember(outboundLabel, returnLabel, roundTripLabel) {
                listOf(
                    LiquidNavigationItem(
                        label = outboundLabel,
                        icon = LiquidIcons.ArrowForward,
                    ),
                    LiquidNavigationItem(
                        label = returnLabel,
                        icon = LiquidIcons.ArrowBack,
                    ),
                    LiquidNavigationItem(
                        label = roundTripLabel,
                        icon = LiquidIcons.Refresh,
                    ),
                )
            }
            val selectedDirectionIndex = when (selectedDirection) {
                TransportDirection.OUTBOUND -> 0
                TransportDirection.RETURN -> 1
                TransportDirection.ROUND_TRIP -> 2
            }
            LiquidTabBar(
                items = directionItems,
                selectedIndex = selectedDirectionIndex,
                onTabSelected = { index ->
                    selectedDirection = when (index) {
                        0 -> TransportDirection.OUTBOUND
                        1 -> TransportDirection.RETURN
                        else -> TransportDirection.ROUND_TRIP
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // 3. Selezione Date Multi-Giorno (Calendario)
        LiquidSectionHeader(
            title = stringResource(Res.string.ui_transport_dates_title),
        )
        Text(
            text = stringResource(Res.string.ui_transport_booking_date_policy),
            style = MaterialTheme.typography.bodySmall,
            color = colorScheme.onSurfaceVariant,
        )

        // Calendario Multi-Selezione in Vetro Liquido
        LiquidCard(
            shape = RoundedRectangle(24.dp),
            contentPadding = 16.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // Show whole months while allowing bookings only inside the rolling 15-day window.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AnimatedContent(
                        targetState = currentMonthTitle,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "monthTitleAnim",
                    ) { title ->
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface,
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        LiquidIconButton(
                            icon = LiquidIcons.ChevronLeft,
                            enabled = displayedMonthIndex > firstMonthIndex,
                            onClick = {
                                if (displayedMonth == 1) {
                                    displayedMonth = 12
                                    displayedYear -= 1
                                } else displayedMonth -= 1
                            },
                        )
                        LiquidIconButton(
                            icon = LiquidIcons.ChevronRight,
                            enabled = displayedMonthIndex < lastMonthIndex,
                            onClick = {
                                if (displayedMonth == 12) {
                                    displayedMonth = 1
                                    displayedYear += 1
                                } else displayedMonth += 1
                            },
                        )
                    }
                }

                // Giorni della settimana
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                ) {
                    listOf("L", "M", "M", "G", "V", "S", "D").forEach { d ->
                        Text(
                            text = d,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = LiquidGlassTheme.colors.secondaryContent,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(36.dp),
                        )
                    }
                }

                // Griglia dei Giorni (Multi-Select)
                val bookedDescription = stringResource(Res.string.ui_transport_ride_booked)
                val totalCells = firstDayOffset + daysInCurrentMonth
                val rows = (totalCells + 6) / 7

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (rowIndex in 0 until rows) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                        ) {
                            for (colIndex in 0..6) {
                                val cellIndex = rowIndex * 7 + colIndex
                                val dayNumber = cellIndex - firstDayOffset + 1

                                if (dayNumber in 1..daysInCurrentMonth) {
                                    val cellDate = LocalDate(displayedYear, displayedMonth, dayNumber)
                                    val isBooked = isTransportDateBooked(
                                        cellDate, uiState.selectedRouteCode, selectedDirection, uiState.bookedRequests,
                                    )
                                    val isSelected = cellDate in availableSelectedDates
                                    val isAllowed = isTransportBookingDateAllowed(cellDate, today) && !isBooked

                                    val cellScale by animateFloatAsState(
                                        targetValue = if (isSelected) 1.08f else 1f,
                                        animationSpec = tween(150),
                                        label = "cellScale",
                                    )

                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .graphicsLayer {
                                                scaleX = cellScale
                                                scaleY = cellScale
                                                alpha = when {
                                                    isAllowed -> 1f
                                                    isBooked -> 0.7f
                                                    else -> 0.35f
                                                }
                                            }
                                            .liquidGlass(
                                                shape = Capsule(),
                                                role = LiquidGlassRole.Control,
                                                containerColor = when {
                                                    isSelected -> LiquidGlassTheme.colors.selectedContainer
                                                    isBooked -> colorScheme.primary.copy(alpha = 0.1f)
                                                    else -> null
                                                },
                                            )
                                            .border(
                                                width = if (isSelected) 1.5.dp else 0.dp,
                                                color = if (isSelected) colorScheme.primary else Color.Transparent,
                                                shape = Capsule(),
                                            )
                                            .semantics {
                                                if (isBooked) stateDescription = bookedDescription
                                            }
                                            .clickable(enabled = isAllowed) {
                                                selectedDates = if (isSelected) {
                                                    availableSelectedDates - cellDate
                                                } else {
                                                    availableSelectedDates + cellDate
                                                }
                                            },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = cellDate.day.toString(),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                                            color = if (isSelected) colorScheme.primary else colorScheme.onSurface,
                                        )
                                        if (isBooked) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.BottomCenter)
                                                    .padding(bottom = 4.dp)
                                                    .size(4.dp)
                                                    .background(colorScheme.primary, CircleShape),
                                            )
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.size(38.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Riepilogo Selezione (High-Fidelity Liquid Glass Ticket Pass)
        AnimatedVisibility(
            visible = uiState.selectedRoute.isNotBlank() && availableSelectedDates.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            val totalRides = availableSelectedDates.size * (if (selectedDirection == TransportDirection.ROUND_TRIP) 2 else 1)

            LiquidCard(
                shape = RoundedRectangle(24.dp),
                contentPadding = 20.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Header: Icon + Title + Direction Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = LiquidIcons.Badge,
                                    contentDescription = null,
                                    tint = colorScheme.primary,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                            Text(
                                text = stringResource(Res.string.ui_transport_summary_ride),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface,
                            )
                        }

                        LiquidBadge(
                            text = when (selectedDirection) {
                                TransportDirection.ROUND_TRIP -> stringResource(Res.string.ui_transport_round_trip_caps)
                                TransportDirection.RETURN -> stringResource(Res.string.ui_transport_return_only_caps)
                                else -> stringResource(Res.string.ui_transport_outbound_only_caps)
                            },
                        )
                    }

                    // Use the SDK's direction-aware route names in every leg of the summary.
                    val route = TransportRouteData(code = "", label = uiState.selectedRoute)
                    val directions = if (selectedDirection == TransportDirection.ROUND_TRIP) {
                        listOf(TransportDirection.OUTBOUND, TransportDirection.RETURN)
                    } else listOf(selectedDirection)
                    directions.forEach { direction ->
                        val display = route.displayFor(direction)
                        if (directions.size > 1) {
                            Text(
                                text = stringResource(
                                    if (direction == TransportDirection.OUTBOUND) Res.string.ui_trip_outbound
                                    else Res.string.ui_trip_return,
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.onSurfaceVariant,
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
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 12.dp)
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(colorScheme.primary.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = LiquidIcons.ArrowForward,
                                        contentDescription = null,
                                        tint = colorScheme.primary,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }

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

                    // Indicatore posto prenotato a bordo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(colorScheme.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = LiquidIcons.Check,
                                contentDescription = null,
                                tint = colorScheme.primary,
                                modifier = Modifier.size(18.dp),
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Posto a sedere prenotato",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface,
                            )
                            Text(
                                text = "Posto garantito e riservato a bordo per le date selezionate",
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    LiquidHorizontalDivider()

                    // Details Grid: Date Selezionate, Totale Corse
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Date Corsa
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
                            val formattedDateSummary = remember(availableSelectedDates) {
                                if (availableSelectedDates.size == 1) {
                                    val single = availableSelectedDates.first()
                                    "${single.day} ${monthNamesShort[single.month.number - 1]} ${single.year}"
                                } else {
                                    availableSelectedDates.sorted().joinToString(", ") { "${it.day} ${monthNamesShort[it.month.number - 1]}" }
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

                        // Disponibilità & Totale Corse
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = LiquidIcons.Check,
                                contentDescription = null,
                                tint = colorScheme.primary,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = stringResource(Res.string.ui_transport_rides_guaranteed_format, totalRides, if (totalRides > 1) stringResource(Res.string.ui_transport_rides_plural) else stringResource(Res.string.ui_transport_ride_singular)),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.primary,
                            )
                        }
                    }
                }
            }
        }

        // 5. Bottone di Conferma Prenotazione
        val totalRides = availableSelectedDates.size * (if (selectedDirection == TransportDirection.ROUND_TRIP) 2 else 1)
        val buttonText = when {
            selectedDirection == TransportDirection.ROUND_TRIP && availableSelectedDates.size > 1 -> stringResource(Res.string.ui_transport_confirm_rides_roundtrip, totalRides)
            selectedDirection == TransportDirection.ROUND_TRIP -> stringResource(Res.string.ui_transport_confirm_two_rides_roundtrip)
            totalRides > 1 -> stringResource(Res.string.ui_transport_confirm_rides, totalRides)
            selectedDirection == TransportDirection.RETURN -> stringResource(Res.string.ui_transport_confirm_return_ride)
            else -> stringResource(Res.string.ui_transport_confirm_outbound_ride)
        }

        LiquidButton(
            text = buttonText,
            onClick = {
                onBook(availableSelectedDates.toList().sorted(), selectedDirection)
            },
            enabled = availableSelectedDates.isNotEmpty() && uiState.selectedRoute.isNotBlank(),
            isLoading = uiState.isSubmitting,
            modifier = Modifier.fillMaxWidth(),
            variant = LiquidButtonVariant.Primary,
        )
    }
}
