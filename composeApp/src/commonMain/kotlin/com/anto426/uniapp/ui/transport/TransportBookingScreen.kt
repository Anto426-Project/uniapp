package com.anto426.uniapp.ui.transport


import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.anto426.liquidmonet.components.cards.card.LiquidCardDefaults
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeaderSize
import com.anto426.liquidmonet.components.selection.radiobutton.LiquidRadioButton
import com.anto426.liquidmonet.components.selection.select.LiquidSelect
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.transport.presentation.TransportBookingUiState
import com.anto426.uniapp.transport.presentation.isTransportBookingDateAllowed
import com.anto426.uniapp.transport.presentation.isTransportDateBooked
import com.anto426.uniapp.transport.presentation.transportBookingWindow
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.unisdk.transport.TransportDirection
import com.anto426.unisdk.transport.TransportRouteData
import com.anto426.unisdk.transport.displayFor
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

    val monthNames = remember {
        listOf(
            "Gennaio", "Febbraio", "Marzo", "Aprile", "Maggio", "Giugno",
            "Luglio", "Agosto", "Settembre", "Ottobre", "Novembre", "Dicembre",
        )
    }
    val monthNamesShort = remember {
        listOf(
            "Gen", "Feb", "Mar", "Apr", "Mag", "Giu",
            "Lug", "Ago", "Set", "Ott", "Nov", "Dic",
        )
    }

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
        // 1. Selezione Tratta
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LiquidSectionHeader(
                title = stringResource(Res.string.ui_trip_route),
                subtitle = stringResource(Res.string.ui_transport_route_subtitle),
                size = LiquidSectionHeaderSize.Small,
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
                val outbound = route.displayFor(TransportDirection.OUTBOUND)
                val returning = route.displayFor(TransportDirection.RETURN)

                if (outbound.departureStop.isNotBlank() && outbound.arrivalStop.isNotBlank()) {
                    LiquidCard(
                        shape = RoundedRectangle(14.dp),
                        contentPadding = 12.dp,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(colorScheme.primary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = LiquidIcons.Time,
                                    contentDescription = null,
                                    tint = colorScheme.primary,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = when (selectedDirection) {
                                        TransportDirection.OUTBOUND -> "${outbound.departureStop} → ${outbound.arrivalStop}"
                                        TransportDirection.RETURN -> "${returning.departureStop} → ${returning.arrivalStop}"
                                        TransportDirection.ROUND_TRIP -> "${outbound.departureStop} ⇄ ${outbound.arrivalStop}"
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

        // 2. Selezione Direzione (Come Aspetto e Modalità in Temi con Radio Button)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            LiquidSectionHeader(
                title = stringResource(Res.string.ui_transport_direction_title),
                subtitle = stringResource(Res.string.ui_transport_direction_subtitle),
                size = LiquidSectionHeaderSize.Small,
            )

            BookingDirectionSelector(
                selectedDirection = selectedDirection,
                onDirectionSelected = { selectedDirection = it },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // 3. Calendario Multi-Selezione Date
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LiquidSectionHeader(
                title = stringResource(Res.string.ui_transport_dates_title),
                subtitle = stringResource(Res.string.ui_transport_booking_date_policy),
                size = LiquidSectionHeaderSize.Small,
            )

            LiquidCard(
                shape = RoundedRectangle(20.dp),
                contentPadding = 16.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    // Header Mese con Frecce
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = currentMonthTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface,
                        )

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

                    // Intestazione Giorni della Settimana
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                    ) {
                        listOf("L", "M", "M", "G", "V", "S", "D").forEach { d ->
                            Text(
                                text = d,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(36.dp),
                            )
                        }
                    }

                    // Griglia dei Giorni (senza bordi interni forzati)
                    val bookedDescription = stringResource(Res.string.ui_transport_ride_booked)
                    val totalCells = firstDayOffset + daysInCurrentMonth
                    val rows = (totalCells + 6) / 7

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
                                            cellDate,
                                            uiState.selectedRouteCode,
                                            selectedDirection,
                                            uiState.bookedRequests,
                                        )
                                        val isSelected = cellDate in availableSelectedDates
                                        val isAllowed = isTransportBookingDateAllowed(cellDate, today) && !isBooked

                                        val cellScale = if (isSelected) 1.06f else 1f

                                        val cellContainerColor = when {
                                            isSelected -> colorScheme.primary.copy(alpha = 0.18f)
                                            isBooked -> colorScheme.outlineVariant.copy(alpha = 0.15f)
                                            else -> Color.Transparent
                                        }

                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .graphicsLayer {
                                                    scaleX = cellScale
                                                    scaleY = cellScale
                                                    alpha = when {
                                                        isAllowed -> 1f
                                                        isBooked -> 0.65f
                                                        else -> 0.30f
                                                    }
                                                }
                                                .clip(RoundedRectangle(12.dp))
                                                .background(cellContainerColor)
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
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
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
        }

        // 4. Riepilogo Selezione (Pass Digitale di Prenotazione)
        if (uiState.selectedRoute.isNotBlank() && availableSelectedDates.isNotEmpty()) {
            val totalRides = availableSelectedDates.size * (if (selectedDirection == TransportDirection.ROUND_TRIP) 2 else 1)

            LiquidCard(
                shape = RoundedRectangle(20.dp),
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
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(colorScheme.primary.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = LiquidIcons.Badge,
                                    contentDescription = null,
                                    tint = colorScheme.primary,
                                    modifier = Modifier.size(18.dp),
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

                    // Fermate e Tragitto
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
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 10.dp)
                                        .size(30.dp)
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

                    // Indicatore posto prenotato a bordo garantito
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
                                text = stringResource(
                                    Res.string.ui_transport_rides_guaranteed_format,
                                    totalRides,
                                    if (totalRides > 1) stringResource(Res.string.ui_transport_rides_plural) else stringResource(Res.string.ui_transport_ride_singular),
                                ),
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
            selectedDirection == TransportDirection.ROUND_TRIP && availableSelectedDates.size > 1 ->
                stringResource(Res.string.ui_transport_confirm_rides_roundtrip, totalRides)
            selectedDirection == TransportDirection.ROUND_TRIP ->
                stringResource(Res.string.ui_transport_confirm_two_rides_roundtrip)
            totalRides > 1 ->
                stringResource(Res.string.ui_transport_confirm_rides, totalRides)
            selectedDirection == TransportDirection.RETURN ->
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
        )
    }
}

/**
 * Selettore Direzione Corsa a 3 Card Visive (Andata, Ritorno, A/R).
 * Strutturato identicamente a ThemeAppearanceSelector (aspetto e modalità in Temi):
 * - 3 card affiancate con peso uguale (weight 1f)
 * - Nessun bordo interno
 * - Mockup/icona visiva superiore in box dedicato
 * - Titolo e sottotitolo
 * - Indicatore di selezione LiquidRadioButton in basso
 */
@Composable
private fun BookingDirectionSelector(
    selectedDirection: TransportDirection,
    onDirectionSelected: (TransportDirection) -> Unit,
    modifier: Modifier = Modifier,
) {
    val directions = remember {
        listOf(
            Triple(TransportDirection.OUTBOUND, "Andata", LiquidIcons.ArrowForward),
            Triple(TransportDirection.RETURN, "Ritorno", LiquidIcons.ArrowBack),
            Triple(TransportDirection.ROUND_TRIP, "A/R", LiquidIcons.Refresh),
        )
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        directions.forEach { (direction, title, icon) ->
            val isSelected = selectedDirection == direction
            BookingDirectionCard(
                title = title,
                icon = icon,
                isSelected = isSelected,
                onClick = { onDirectionSelected(direction) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * Card singola di selezione direzione:
 * Colori allineati agli altri elementi della schermata (vetro ottico, accento Monet primary),
 * singolo titolo ("Andata", "Ritorno", "A/R") e indicatore LiquidRadioButton.
 */
@Composable
private fun BookingDirectionCard(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme

    LiquidCard(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedRectangle(16.dp),
        contentPadding = 12.dp,
        colors = if (isSelected) {
            LiquidCardDefaults.colors(
                containerColor = colorScheme.primary.copy(alpha = 0.12f),
            )
        } else {
            LiquidCardDefaults.colors()
        },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Contenitore icona circolare con colore allineato agli altri elementi della schermata
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        colorScheme.primary.copy(alpha = if (isSelected) 0.18f else 0.12f),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }

            // Singolo titolo pulito ("Andata", "Ritorno", "A/R")
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) colorScheme.primary else colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )

            // Indicatore di selezione radio
            LiquidRadioButton(
                selected = isSelected,
                onClick = onClick,
            )
        }
    }
}
