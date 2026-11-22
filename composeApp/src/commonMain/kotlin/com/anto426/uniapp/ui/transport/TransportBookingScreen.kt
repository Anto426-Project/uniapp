package com.anto426.uniapp.ui.transport

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.buttons.button.LiquidButton
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonSize
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonVariant
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.iconcontainer.liquidIconContainer
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeaderSize
import com.anto426.liquidmonet.components.navigation.navigationbar.LiquidNavigationItem
import com.anto426.liquidmonet.components.navigation.tabbar.LiquidTabBar
import com.anto426.liquidmonet.components.pickers.datepicker.LiquidDatePicker
import com.anto426.liquidmonet.components.pickers.datepicker.rememberLiquidDatePickerState
import com.anto426.liquidmonet.components.pickers.datepicker.state.LiquidDatePickerFilter
import com.anto426.liquidmonet.components.pickers.datepicker.state.LiquidDatePickerLabels
import com.anto426.liquidmonet.components.pickers.datepicker.state.LiquidDateSelectionMode
import com.anto426.liquidmonet.components.selection.select.LiquidSelect
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.data.contract.TripBookingDirection
import com.anto426.uniapp.transport.presentation.TransportBookingUiState
import com.anto426.uniapp.transport.presentation.isTransportBookingDateAllowed
import com.anto426.uniapp.transport.presentation.isTransportDateBooked
import com.anto426.uniapp.transport.presentation.transportBookingWindow
import com.anto426.uniapp.transport.presentation.transportRouteStopsFor
import com.anto426.uniapp.ui.components.layout.UniAnimatedEntrance
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.kyant.shapes.RoundedRectangle
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun TransportBookingScreen(
    uiState: TransportBookingUiState,
    onRouteSelected: (String) -> Unit,
    onBook: (List<LocalDate>, TripBookingDirection) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val haptics = LocalHapticFeedback.current
    var selectedDirection by remember { mutableStateOf(TripBookingDirection.OUTBOUND) }

    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val lastBookableDate = remember(today) { transportBookingWindow(today).last() }
    val bookedDescription = stringResource(Res.string.ui_transport_ride_booked)
    val dateFilter = remember(
        today,
        lastBookableDate,
        uiState.selectedRouteCode,
        selectedDirection,
        uiState.bookedRequests,
        bookedDescription,
    ) {
        LiquidDatePickerFilter.range(
            minDate = today,
            maxDate = lastBookableDate,
            isSelectable = { date ->
                isTransportBookingDateAllowed(date, today) &&
                    !isTransportDateBooked(
                        date,
                        uiState.selectedRouteCode,
                        selectedDirection,
                        uiState.bookedRequests,
                    )
            },
            isMarked = { date ->
                isTransportDateBooked(
                    date,
                    uiState.selectedRouteCode,
                    selectedDirection,
                    uiState.bookedRequests,
                )
            },
            stateDescription = { date ->
                if (
                    isTransportDateBooked(
                        date,
                        uiState.selectedRouteCode,
                        selectedDirection,
                        uiState.bookedRequests,
                    )
                ) {
                    bookedDescription
                } else {
                    null
                }
            },
        )
    }
    val datePickerState = rememberLiquidDatePickerState(
        initialDate = today,
        selectionMode = LiquidDateSelectionMode.Multiple,
        filter = dateFilter,
    )
    val availableSelectedDates = datePickerState.selectedDates
    val defaultDatePickerLabels = LiquidDatePickerLabels.Default
    val monthsFullRaw = stringResource(Res.string.ui_calendar_months_full)
    val monthsShortRaw = stringResource(Res.string.ui_calendar_months_short)
    val weekdaysShortRaw = stringResource(Res.string.ui_calendar_weekdays_short)
    val weekdaysInitialRaw = stringResource(Res.string.ui_calendar_weekdays_initial)
    val clearLabel = stringResource(Res.string.ui_transport_clear_selection)
    val emptySelectionLabel = stringResource(Res.string.ui_transport_no_date_selected)
    val todayLabel = stringResource(Res.string.ui_calendar_today)
    val selectedDatesCountPattern = stringResource(Res.string.ui_calendar_selected_dates_count, 0)
    val prevMonthLabel = stringResource(Res.string.ui_calendar_prev_month)
    val nextMonthLabel = stringResource(Res.string.ui_calendar_next_month)
    val datePickerLabels = remember(
        defaultDatePickerLabels,
        monthsFullRaw,
        monthsShortRaw,
        weekdaysShortRaw,
        weekdaysInitialRaw,
        clearLabel,
        emptySelectionLabel,
        todayLabel,
        selectedDatesCountPattern,
        prevMonthLabel,
        nextMonthLabel,
    ) {
        val parsedMonthsFull = monthsFullRaw.split(',').map(String::trim).filter(String::isNotEmpty)
        val parsedMonthsShort = monthsShortRaw.split(',').map(String::trim).filter(String::isNotEmpty)
        val parsedWeekdaysShort = weekdaysShortRaw.split(',').map(String::trim).filter(String::isNotEmpty)
        val parsedWeekdaysInitial = weekdaysInitialRaw.split(',').map(String::trim).filter(String::isNotEmpty)
        defaultDatePickerLabels.copy(
            monthNames = if (parsedMonthsFull.size == 12) parsedMonthsFull else defaultDatePickerLabels.monthNames,
            shortMonthNames = if (parsedMonthsShort.size == 12) parsedMonthsShort else defaultDatePickerLabels.shortMonthNames,
            shortWeekdayNames = if (parsedWeekdaysShort.size == 7) parsedWeekdaysShort else defaultDatePickerLabels.shortWeekdayNames,
            weekdayInitials = if (parsedWeekdaysInitial.size == 7) parsedWeekdaysInitial else defaultDatePickerLabels.weekdayInitials,
            emptySelection = emptySelectionLabel,
            todayAction = todayLabel,
            clearAction = clearLabel,
            selectedCountFormat = { count -> selectedDatesCountPattern.replace("0", count.toString()) },
            previousMonthDescription = prevMonthLabel,
            nextMonthDescription = nextMonthLabel,
        )
    }
    val monthNamesShort = datePickerLabels.shortMonthNames
    val directionTabs = listOf(
        LiquidNavigationItem(
            label = stringResource(Res.string.ui_transport_outbound_short),
            icon = LiquidIcons.ArrowForward,
        ),
        LiquidNavigationItem(
            label = stringResource(Res.string.ui_transport_return_short),
            icon = LiquidIcons.ArrowBack,
        ),
        LiquidNavigationItem(
            label = stringResource(Res.string.ui_transport_round_trip_short),
            icon = LiquidIcons.Refresh,
        ),
    )

    UniScreenColumn {
        // 1. Selezione Tratta
        UniAnimatedEntrance(order = 0) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LiquidSectionHeader(
                    title = stringResource(Res.string.ui_trip_route),
                    subtitle = stringResource(Res.string.ui_transport_route_subtitle),
                    size = LiquidSectionHeaderSize.Small,
                )

                LiquidSelect(
                    items = uiState.routes,
                    selectedItem = uiState.selectedRoute.takeIf(String::isNotBlank),
                    onItemSelected = onRouteSelected,
                    label = stringResource(Res.string.ui_trip_route),
                    leadingIcon = LiquidIcons.Star,
                    modifier = Modifier.fillMaxWidth(),
                )

                if (uiState.selectedRoute.isNotBlank()) {
                    val outbound = transportRouteStopsFor(uiState.selectedRoute, TripBookingDirection.OUTBOUND)
                    val returning = transportRouteStopsFor(uiState.selectedRoute, TripBookingDirection.RETURN)

                    if (outbound.departureStop.isNotBlank() && outbound.arrivalStop.isNotBlank()) {
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
                                        text = uiState.selectedRoute,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Selezione Direzione (LiquidTabBar)
        UniAnimatedEntrance(order = 1) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LiquidSectionHeader(
                    title = stringResource(Res.string.ui_transport_direction_title),
                    subtitle = stringResource(Res.string.ui_transport_direction_subtitle),
                    size = LiquidSectionHeaderSize.Small,
                )

                LiquidTabBar(
                    items = directionTabs,
                    selectedIndex = when (selectedDirection) {
                        TripBookingDirection.OUTBOUND -> 0
                        TripBookingDirection.RETURN -> 1
                        TripBookingDirection.ROUND_TRIP -> 2
                    },
                    onTabSelected = { index ->
                        haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        selectedDirection = when (index) {
                            0 -> TripBookingDirection.OUTBOUND
                            1 -> TripBookingDirection.RETURN
                            else -> TripBookingDirection.ROUND_TRIP
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        // 3. Calendario Multi-Selezione Date (LiquidDatePicker con filtri)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LiquidSectionHeader(
                title = stringResource(Res.string.ui_transport_dates_title),
                subtitle = stringResource(Res.string.ui_transport_booking_date_policy),
                size = LiquidSectionHeaderSize.Small,
            )

            LiquidDatePicker(
                state = datePickerState,
                labels = datePickerLabels,
                allowYearSelection = false,
                showTodayAction = false,
                showClearAction = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // 4. Riepilogo Selezione (Pass Digitale di Prenotazione)
        if (uiState.selectedRoute.isNotBlank() && availableSelectedDates.isNotEmpty()) {
            val totalRides = availableSelectedDates.size * (if (selectedDirection == TripBookingDirection.ROUND_TRIP) 2 else 1)

            LiquidCard(
                shape = RoundedRectangle(24.dp),
                contentPadding = 18.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Header Pass
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

                    // Fermate e Tragitto
                    val directions = if (selectedDirection == TripBookingDirection.ROUND_TRIP) {
                        listOf(TripBookingDirection.OUTBOUND, TripBookingDirection.RETURN)
                    } else {
                        listOf(selectedDirection)
                    }

                    directions.forEach { direction ->
                        val display = transportRouteStopsFor(uiState.selectedRoute, direction)
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

                    // Indicatore posto prenotato a bordo garantito
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

                    // Dettagli Date e Totale Corse
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
                            val formattedDateSummary = remember(availableSelectedDates) {
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

        // 5. Bottone di Conferma Prenotazione
        val totalRides = availableSelectedDates.size * (if (selectedDirection == TripBookingDirection.ROUND_TRIP) 2 else 1)
        val buttonText = when {
            selectedDirection == TripBookingDirection.ROUND_TRIP && availableSelectedDates.size > 1 ->
                stringResource(Res.string.ui_transport_confirm_rides_roundtrip, totalRides)
            selectedDirection == TripBookingDirection.ROUND_TRIP ->
                stringResource(Res.string.ui_transport_confirm_two_rides_roundtrip)
            totalRides > 1 ->
                stringResource(Res.string.ui_transport_confirm_rides, totalRides)
            selectedDirection == TripBookingDirection.RETURN ->
                stringResource(Res.string.ui_transport_confirm_return_ride)
            else ->
                stringResource(Res.string.ui_transport_confirm_outbound_ride)
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
            size = LiquidButtonSize.Medium,
        )
    }
}
