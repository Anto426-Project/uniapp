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
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.buttons.LiquidButton
import com.anto426.liquidmonet.components.buttons.LiquidButtonVariant
import com.anto426.liquidmonet.components.buttons.LiquidIconButton
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.liquidmonet.components.display.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.LiquidSectionHeader
import com.anto426.liquidmonet.components.selection.LiquidChip
import com.anto426.liquidmonet.components.selection.LiquidSelect
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.transport.presentation.TransportBookingUiState
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.unisdk.transport.TransportDirection
import com.kyant.backdrop.Backdrop
import com.kyant.shapes.Capsule
import kotlin.time.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransportBookingScreen(
    backdropState: Backdrop,
    uiState: TransportBookingUiState,
    onRouteSelected: (String) -> Unit,
    onBook: (List<LocalDate>, TransportDirection) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    var selectedDirection by remember { mutableStateOf(TransportDirection.OUTBOUND) }

    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    var displayedYear by remember { mutableIntStateOf(today.year) }
    var displayedMonth by remember { mutableIntStateOf(today.month.number) }
    var selectedDates by remember { mutableStateOf(setOf(today)) }

    val monthNames = listOf(
        "Gennaio", "Febbraio", "Marzo", "Aprile", "Maggio", "Giugno",
        "Luglio", "Agosto", "Settembre", "Ottobre", "Novembre", "Dicembre"
    )
    val monthNamesShort = listOf(
        "Gen", "Feb", "Mar", "Apr", "Mag", "Giu",
        "Lug", "Ago", "Set", "Ott", "Nov", "Dic"
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
            subtitle = stringResource(Res.string.ui_transport_route_subtitle),
        )

        LiquidSelect(
            items = uiState.routes,
            selectedItem = uiState.selectedRoute,
            onItemSelected = onRouteSelected,
            label = stringResource(Res.string.ui_trip_route),
            backdropState = backdropState,
            modifier = Modifier.fillMaxWidth(),
        )

        // 2. Selezione Direzione (Andata, Ritorno o Entrambi)
        LiquidSectionHeader(
            title = stringResource(Res.string.ui_transport_direction_title),
            subtitle = stringResource(Res.string.ui_transport_direction_subtitle),
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            LiquidChip(
                label = stringResource(Res.string.ui_trip_outbound),
                selected = selectedDirection == TransportDirection.OUTBOUND,
                onClick = { selectedDirection = TransportDirection.OUTBOUND },
                leadingIcon = LiquidIcons.ArrowForward,
                backdropState = backdropState,
            )
            LiquidChip(
                label = stringResource(Res.string.ui_trip_return),
                selected = selectedDirection == TransportDirection.RETURN,
                onClick = { selectedDirection = TransportDirection.RETURN },
                leadingIcon = LiquidIcons.ArrowBack,
                backdropState = backdropState,
            )
            LiquidChip(
                label = stringResource(Res.string.ui_transport_round_trip),
                selected = selectedDirection == TransportDirection.ROUND_TRIP,
                onClick = { selectedDirection = TransportDirection.ROUND_TRIP },
                leadingIcon = LiquidIcons.Refresh,
                backdropState = backdropState,
            )
        }

        // 3. Selezione Date Multi-Giorno
        LiquidSectionHeader(
            title = stringResource(Res.string.ui_transport_dates_title),
            subtitle = stringResource(Res.string.ui_transport_dates_subtitle),
        )

        // Tasti di selezione rapida (Preset)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            LiquidChip(
                label = stringResource(Res.string.ui_transport_preset_today),
                selected = selectedDates == setOf(today),
                onClick = { selectedDates = setOf(today) },
                backdropState = backdropState,
                modifier = Modifier.weight(1f),
            )
            LiquidChip(
                label = stringResource(Res.string.ui_transport_preset_today_tomorrow),
                selected = selectedDates == setOf(today, today.plus(DatePeriod(days = 1))),
                onClick = { selectedDates = setOf(today, today.plus(DatePeriod(days = 1))) },
                backdropState = backdropState,
                modifier = Modifier.weight(1.3f),
            )
            LiquidChip(
                label = stringResource(Res.string.ui_transport_preset_week),
                selected = selectedDates.size == 5 && (0..4).all { today.plus(DatePeriod(days = it)) in selectedDates },
                onClick = { selectedDates = (0..4).map { today.plus(DatePeriod(days = it)) }.toSet() },
                backdropState = backdropState,
                modifier = Modifier.weight(1.2f),
            )
        }

        // Calendario Multi-Selezione in Vetro Liquido
        LiquidCard(
            backdropState = backdropState,
            shape = RoundedCornerShape(24.dp),
            contentPadding = 16.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // Mese / Anno Header
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
                            onClick = {
                                if (displayedMonth == 1) {
                                    displayedMonth = 12
                                    displayedYear -= 1
                                } else {
                                    displayedMonth -= 1
                                }
                            },
                            backdropState = backdropState,
                        )
                        LiquidIconButton(
                            icon = LiquidIcons.ChevronRight,
                            onClick = {
                                if (displayedMonth == 12) {
                                    displayedMonth = 1
                                    displayedYear += 1
                                } else {
                                    displayedMonth += 1
                                }
                            },
                            backdropState = backdropState,
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
                                    val isSelected = cellDate in selectedDates
                                    val isToday = cellDate == today
                                    val isPast = cellDate < today

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
                                                alpha = if (isPast) 0.35f else 1f
                                            }
                                            .liquidGlass(
                                                backdrop = backdropState,
                                                shape = Capsule(),
                                                role = LiquidGlassRole.Control,
                                                containerColor = if (isSelected) {
                                                    LiquidGlassTheme.colors.selectedContainer
                                                } else if (isToday) {
                                                    colorScheme.primary.copy(alpha = 0.12f)
                                                } else null,
                                            )
                                            .border(
                                                width = if (isSelected) 1.5.dp else if (isToday) 1.dp else 0.dp,
                                                color = if (isSelected) colorScheme.primary else if (isToday) colorScheme.primary.copy(alpha = 0.45f) else Color.Transparent,
                                                shape = Capsule(),
                                            )
                                            .clickable(enabled = !isPast) {
                                                selectedDates = if (isSelected) {
                                                    if (selectedDates.size > 1) selectedDates - cellDate else selectedDates
                                                } else {
                                                    selectedDates + cellDate
                                                }
                                            },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = dayNumber.toString(),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else if (isToday) FontWeight.SemiBold else FontWeight.Normal,
                                            color = when {
                                                isSelected -> colorScheme.onSurface
                                                isToday -> colorScheme.primary
                                                else -> colorScheme.onSurface
                                            },
                                        )
                                    }
                                } else {
                                    Spacer(modifier = Modifier.size(38.dp))
                                }
                            }
                        }
                    }
                }

                // Riepilogo giorni selezionati con possibilità di rimuoverli
                if (selectedDates.isNotEmpty()) {
                    LiquidHorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        selectedDates.sorted().forEach { d ->
                            LiquidChip(
                                label = "${d.day} ${monthNamesShort[d.month.number - 1]}",
                                trailingIcon = if (selectedDates.size > 1) LiquidIcons.Close else null,
                                onClick = {
                                    if (selectedDates.size > 1) {
                                        selectedDates = selectedDates - d
                                    }
                                },
                                selected = true,
                                backdropState = backdropState,
                            )
                        }
                    }
                }
            }
        }

        // 4. Riepilogo Selezione (High-Fidelity Liquid Glass Ticket Pass)
        AnimatedVisibility(
            visible = uiState.selectedRoute.isNotBlank() && selectedDates.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            val totalRides = selectedDates.size * (if (selectedDirection == TransportDirection.ROUND_TRIP) 2 else 1)

            LiquidCard(
                backdropState = backdropState,
                shape = RoundedCornerShape(24.dp),
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
                            backdropState = backdropState,
                        )
                    }

                    // Route Segment Display (Origin -> Destination visualizer)
                    val routeParts = remember(uiState.selectedRoute) {
                        val parts = uiState.selectedRoute.split(Regex("[-–—>]"))
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                        if (parts.size >= 2) parts[0] to parts[1] else uiState.selectedRoute to ""
                    }

                    if (routeParts.second.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            val (origin, destination) = when (selectedDirection) {
                                TransportDirection.RETURN -> routeParts.second to routeParts.first
                                else -> routeParts.first to routeParts.second
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (selectedDirection == TransportDirection.ROUND_TRIP) stringResource(Res.string.ui_transport_terminus_a) else stringResource(Res.string.ui_transport_departure),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = origin,
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
                                    .background(colorScheme.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = if (selectedDirection == TransportDirection.ROUND_TRIP) LiquidIcons.Refresh else LiquidIcons.ArrowForward,
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
                                    text = if (selectedDirection == TransportDirection.ROUND_TRIP) stringResource(Res.string.ui_transport_terminus_b) else stringResource(Res.string.ui_transport_arrival),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = destination,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurface,
                                    maxLines = 1,
                                )
                            }
                        }
                    } else {
                        Text(
                            text = uiState.selectedRoute,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.primary,
                        )
                    }

                    LiquidHorizontalDivider()

                    // Details Grid: Date Selezionate, Totale Corse
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
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
                            Column {
                                Text(
                                    text = if (selectedDates.size > 1) stringResource(Res.string.ui_transport_dates_count, selectedDates.size) else stringResource(Res.string.ui_transport_ride_date),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colorScheme.onSurfaceVariant,
                                )
                                val formattedDateSummary = remember(selectedDates) {
                                    if (selectedDates.size == 1) {
                                        val single = selectedDates.first()
                                        "${single.day} ${monthNamesShort[single.month.number - 1]} ${single.year}"
                                    } else {
                                        selectedDates.sorted().joinToString(", ") { "${it.day} ${monthNamesShort[it.month.number - 1]}" }
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
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = stringResource(Res.string.ui_transport_total_rides),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = stringResource(Res.string.ui_transport_rides_guaranteed_format, totalRides, if (totalRides > 1) stringResource(Res.string.ui_transport_rides_plural) else stringResource(Res.string.ui_transport_ride_singular)),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colorScheme.primary,
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Bottone di Conferma Prenotazione
        val totalRides = selectedDates.size * (if (selectedDirection == TransportDirection.ROUND_TRIP) 2 else 1)
        val buttonText = when {
            selectedDirection == TransportDirection.ROUND_TRIP && selectedDates.size > 1 -> stringResource(Res.string.ui_transport_confirm_rides_roundtrip, totalRides)
            selectedDirection == TransportDirection.ROUND_TRIP -> stringResource(Res.string.ui_transport_confirm_two_rides_roundtrip)
            totalRides > 1 -> stringResource(Res.string.ui_transport_confirm_rides, totalRides)
            selectedDirection == TransportDirection.RETURN -> stringResource(Res.string.ui_transport_confirm_return_ride)
            else -> stringResource(Res.string.ui_transport_confirm_outbound_ride)
        }

        LiquidButton(
            text = buttonText,
            onClick = {
                onBook(selectedDates.toList().sorted(), selectedDirection)
            },
            enabled = selectedDates.isNotEmpty() && uiState.selectedRoute.isNotBlank(),
            isLoading = uiState.isSubmitting,
            modifier = Modifier.fillMaxWidth(),
            variant = LiquidButtonVariant.Primary,
            backdropState = backdropState,
        )

        Spacer(Modifier.height(32.dp))
    }
}
