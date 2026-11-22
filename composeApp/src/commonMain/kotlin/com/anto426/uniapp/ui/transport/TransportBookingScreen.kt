package com.anto426.uniapp.ui.transport

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.buttons.button.LiquidButton
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonSize
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonVariant
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
import com.anto426.uniapp.ui.components.layout.UniAnimatedEntrance
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.transport.components.TransportBookingSummaryCard
import com.anto426.uniapp.ui.transport.components.TransportRouteStopsPreviewCard
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun TransportBookingScreen(
    uiState: TransportBookingUiState,
    onRouteSelected: (String) -> Unit,
    onBook: (List<LocalDate>, TripBookingDirection) -> Unit,
) {
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

                TransportRouteStopsPreviewCard(
                    selectedRoute = uiState.selectedRoute,
                    selectedDirection = selectedDirection,
                )
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
            TransportBookingSummaryCard(
                selectedRoute = uiState.selectedRoute,
                selectedDirection = selectedDirection,
                availableSelectedDates = availableSelectedDates,
                monthNamesShort = monthNamesShort,
            )
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
