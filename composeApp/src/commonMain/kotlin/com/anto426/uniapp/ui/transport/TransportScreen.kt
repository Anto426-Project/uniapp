package com.anto426.uniapp.ui.transport

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.display.emptystate.LiquidEmptyState
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.liquidmonet.components.selection.chip.LiquidChip
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.data.contract.TransportReservation
import com.anto426.uniapp.data.contract.TripDirection
import com.anto426.uniapp.transport.presentation.TransportUiState
import com.anto426.uniapp.ui.components.display.UniInsetGroup
import com.anto426.uniapp.ui.components.items.TransportReservationItem
import com.anto426.uniapp.ui.components.layout.UniContentHost
import com.anto426.uniapp.ui.components.layout.UniHorizontalChipRow
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.components.layout.uniListEntranceOrder
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

private enum class TransportDirectionFilter {
    ALL,
    OUTBOUND,
    RETURN,
}

@Composable
fun TransportScreen(
    uiState: TransportUiState,
    onReservationClick: ((TransportReservation) -> Unit)? = null,
    onBookClick: (() -> Unit)? = null,
) {
    var directionFilter by rememberSaveable { mutableStateOf(TransportDirectionFilter.ALL) }

    val allReservations = uiState.days.flatMap { it.reservations }
    val outboundCount = allReservations.count { it.direction == TripDirection.ANDATA }
    val returnCount = allReservations.count { it.direction == TripDirection.RITORNO }

    UniScreenColumn {
        if (allReservations.isEmpty()) {
            LiquidEmptyState(
                title = stringResource(Res.string.ui_transport_empty_reservations),
                description = stringResource(Res.string.ui_state_no_data),
                icon = LiquidIcons.Calendar,
                actionButtonText = onBookClick?.let { stringResource(Res.string.ui_transport_book_ride) },
                onActionClick = onBookClick,
            )
        } else {
            if (outboundCount > 0 && returnCount > 0) {
                val filterOptions = listOf(
                    Triple(
                        TransportDirectionFilter.ALL,
                        stringResource(Res.string.ui_transport_all_filter),
                        allReservations.size,
                    ),
                    Triple(
                        TransportDirectionFilter.OUTBOUND,
                        stringResource(Res.string.ui_transport_outbound_short),
                        outboundCount,
                    ),
                    Triple(
                        TransportDirectionFilter.RETURN,
                        stringResource(Res.string.ui_transport_return_short),
                        returnCount,
                    ),
                )
                UniHorizontalChipRow {
                    filterOptions.forEach { (option, label, count) ->
                        key(option) {
                            val selected = directionFilter == option
                            LiquidChip(
                                label = "$label ($count)",
                                selected = selected,
                                leadingIcon = if (selected) LiquidIcons.Check else null,
                                onClick = { directionFilter = option },
                            )
                        }
                    }
                }
            }

            UniContentHost(
                targetState = directionFilter,
                modifier = Modifier.fillMaxWidth(),
                isForward = { from, to -> to.ordinal >= from.ordinal },
                label = "TransportFilterSwitcher",
            ) { activeFilter ->
                val activeFilteredDays = uiState.days.mapNotNull { day ->
                    val visibleReservations = when (activeFilter) {
                        TransportDirectionFilter.ALL -> day.reservations
                        TransportDirectionFilter.OUTBOUND -> day.reservations.filter { it.direction == TripDirection.ANDATA }
                        TransportDirectionFilter.RETURN -> day.reservations.filter { it.direction == TripDirection.RITORNO }
                    }
                    if (visibleReservations.isEmpty()) null else day.copy(reservations = visibleReservations)
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    if (activeFilteredDays.isEmpty()) {
                        LiquidEmptyState(
                            title = stringResource(Res.string.ui_transport_filter_empty),
                            description = stringResource(Res.string.ui_transport_filter_empty_desc),
                        )
                    } else {
                        activeFilteredDays.forEachIndexed { dayIndex, day ->
                            key(day.date) {
                                val outgoing = day.reservations.count { it.direction == TripDirection.ANDATA }
                                val returning = day.reservations.count { it.direction == TripDirection.RITORNO }
                                val filteredSummary = buildList {
                                    if (outgoing > 0) add("$outgoing ${stringResource(Res.string.ui_transport_outbound_short)}")
                                    if (returning > 0) add("$returning ${stringResource(Res.string.ui_transport_return_short)}")
                                }.joinToString(" • ").ifBlank { day.directionSummary }

                                LiquidSectionHeader(
                                    title = day.date.ifBlank { stringResource(Res.string.ui_transport_date_unavailable) },
                                    subtitle = filteredSummary,
                                )

                                UniInsetGroup(entranceOrder = uniListEntranceOrder(dayIndex)) {
                                    day.reservations.forEachIndexed { index, reservation ->
                                        key(reservation.id, reservation.direction, reservation.time, reservation.route) {
                                            TransportReservationItem(
                                                reservation = reservation,
                                                onClick = onReservationClick?.let { open -> { open(reservation) } },
                                                showDivider = index < day.reservations.lastIndex,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
