package com.anto426.uniapp.ui.library

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.buttons.button.LiquidButton
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonSize
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonVariant
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.emptystate.LiquidEmptyState
import com.anto426.liquidmonet.components.display.iconcontainer.liquidIconContainer
import com.anto426.liquidmonet.components.feedback.loading.LiquidLoading
import com.anto426.liquidmonet.components.feedback.loading.LiquidLoadingSize
import com.anto426.liquidmonet.components.feedback.loading.LiquidLoadingStyle
import com.anto426.liquidmonet.components.inputs.textfield.LiquidTextField
import com.anto426.liquidmonet.components.inputs.textfield.LiquidTextFieldType
import com.anto426.liquidmonet.components.selection.chip.LiquidChip
import com.anto426.liquidmonet.components.selection.radiobutton.LiquidRadioButton
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.library.presentation.LibrarySpaceBookingUiState
import com.anto426.uniapp.ui.components.layout.UniAnimatedEntrance
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.kyant.shapes.RoundedRectangle
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LibrarySpaceBookingScreen(
    uiState: LibrarySpaceBookingUiState,
    onSelectType: (String) -> Unit,
    onSelectOwner: (String) -> Unit,
    onSelectResource: (String) -> Unit,
    onLoadCalendarPage: (Int) -> Unit,
    onToggleSlot: (String) -> Unit,
    onUpdateDeviceAddress: (String, String) -> Unit,
    onUpdateNote: (String) -> Unit,
    onBook: () -> Unit,
    onBookingCompleted: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val overview = uiState.overview ?: return
    val fieldShape = RoundedRectangle(16.dp)

    LaunchedEffect(uiState.bookingCompleted) {
        if (uiState.bookingCompleted) {
            onBookingCompleted()
        }
    }

    UniScreenColumn {
        // Step 1: Tipologia, Sede e Risorsa
        UniAnimatedEntrance(order = 0) {
            LiquidCard(
                shape = RoundedRectangle(24.dp),
                contentPadding = PaddingValues(18.dp),
            ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                StepCardHeader(
                    title = stringResource(Res.string.ui_library_booking_step_1),
                    icon = LiquidIcons.Home,
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(Res.string.ui_library_booking_type_title),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurfaceVariant,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        overview.types.forEach { option ->
                            val selected = uiState.selectedTypeId == option.id
                            LiquidChip(
                                label = option.label,
                                selected = selected,
                                leadingIcon = if (selected) LiquidIcons.Check else null,
                                onClick = { onSelectType(option.id) },
                            )
                        }
                    }
                }

                if (uiState.owners.isNotEmpty()) {
                    LiquidHorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.25f))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(Res.string.ui_library_booking_owner_title),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onSurfaceVariant,
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            uiState.owners.forEach { owner ->
                                val selected = uiState.selectedOwnerId == owner.id
                                LiquidChip(
                                    label = owner.label,
                                    selected = selected,
                                    leadingIcon = if (selected) LiquidIcons.Check else null,
                                    onClick = { onSelectOwner(owner.id) },
                                )
                            }
                        }
                    }
                }

                if (uiState.isLoadingSelection) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        LiquidLoading(
                            style = LiquidLoadingStyle.Dots,
                            size = LiquidLoadingSize.Medium,
                            message = stringResource(Res.string.ui_loading),
                        )
                    }
                } else if (uiState.resources.isNotEmpty()) {
                    LiquidHorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.25f))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(Res.string.ui_library_booking_resource_title),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onSurfaceVariant,
                        )
                        uiState.resources.forEach { resource ->
                            val selected = uiState.selectedResourceId == resource.id
                            val itemShape = RoundedRectangle(14.dp)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(itemShape)
                                    .border(
                                        width = if (selected) 1.5.dp else 1.dp,
                                        color = if (selected) {
                                            colorScheme.primary
                                        } else {
                                            colorScheme.outlineVariant.copy(alpha = 0.30f)
                                        },
                                        shape = itemShape,
                                    )
                                    .clickable { onSelectResource(resource.id) }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = resource.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (selected) colorScheme.primary else colorScheme.onSurface,
                                    modifier = Modifier.weight(1f),
                                )
                                Spacer(Modifier.width(8.dp))
                                LiquidRadioButton(
                                    selected = selected,
                                    onClick = { onSelectResource(resource.id) },
                                )
                            }
                        }
                    }
                }
            }
        }
        }

        // Step 2: Fasce orarie
        val calendar = uiState.calendar
        if (uiState.isLoadingCalendar || calendar != null) {
            LiquidCard(
                shape = RoundedRectangle(24.dp),
                contentPadding = PaddingValues(18.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        StepCardHeader(
                            title = stringResource(Res.string.ui_library_booking_step_2),
                            icon = LiquidIcons.Calendar,
                            modifier = Modifier.weight(1f),
                        )
                        if (calendar != null) {
                            LiquidBadge(
                                text = stringResource(Res.string.ui_library_page_label, calendar.page),
                                containerColor = colorScheme.primary.copy(alpha = 0.14f),
                                contentColor = colorScheme.primary,
                            )
                        }
                    }

                    if (uiState.isLoadingCalendar) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            LiquidLoading(
                                style = LiquidLoadingStyle.Dots,
                                size = LiquidLoadingSize.Medium,
                                message = stringResource(Res.string.ui_loading),
                            )
                        }
                    } else if (calendar != null) {
                        if (calendar.slots.isEmpty()) {
                            LiquidEmptyState(
                                title = stringResource(Res.string.ui_library_booking_slots_empty),
                                description = stringResource(Res.string.ui_state_no_data),
                                icon = LiquidIcons.Calendar,
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                calendar.slots.forEach { slot ->
                                    val selected = slot.slotId in uiState.selectedSlotIds
                                    val slotShape = RoundedRectangle(16.dp)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(slotShape)
                                            .border(
                                                width = if (selected) 1.5.dp else 1.dp,
                                                color = if (selected) {
                                                    colorScheme.primary
                                                } else {
                                                    colorScheme.outlineVariant.copy(alpha = 0.30f)
                                                },
                                                shape = slotShape,
                                            )
                                            .clickable { onToggleSlot(slot.slotId) }
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(4.dp),
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            ) {
                                                Text(
                                                    text = slot.timeLabel,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                                                    color = if (selected) colorScheme.primary else colorScheme.onSurface,
                                                )
                                                slot.dateLabel?.takeIf(String::isNotBlank)?.let { dateLabel ->
                                                    LiquidBadge(
                                                        text = dateLabel,
                                                        containerColor = colorScheme.secondary.copy(alpha = 0.14f),
                                                        contentColor = colorScheme.secondary,
                                                    )
                                                }
                                            }
                                            val secondary = slot.seatsLabel ?: slot.description
                                            if (secondary.isNotBlank()) {
                                                Text(
                                                    text = secondary,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = colorScheme.onSurfaceVariant,
                                                )
                                            }
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        if (selected) {
                                            Icon(
                                                imageVector = LiquidIcons.Check,
                                                contentDescription = null,
                                                tint = colorScheme.primary,
                                                modifier = Modifier.size(18.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            LiquidButton(
                                text = stringResource(Res.string.ui_library_prev_page),
                                onClick = { onLoadCalendarPage(calendar.page - 1) },
                                enabled = calendar.page > 1 && !uiState.isLoadingCalendar,
                                variant = LiquidButtonVariant.Secondary,
                                size = LiquidButtonSize.Small,
                                modifier = Modifier.weight(1f),
                            )
                            LiquidButton(
                                text = stringResource(Res.string.ui_library_next_page),
                                onClick = { onLoadCalendarPage(calendar.page + 1) },
                                enabled = calendar.slots.isNotEmpty() && !uiState.isLoadingCalendar,
                                variant = LiquidButtonVariant.Secondary,
                                size = LiquidButtonSize.Small,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }

        // Step 3: Conferma e dettagli aggiuntivi
        if (calendar != null && uiState.selectedSlotIds.isNotEmpty()) {
            val selectedSlotsRequiringDevice = calendar.slots.filter {
                it.slotId in uiState.selectedSlotIds && it.requiresDeviceAddress
            }
            LiquidCard(
                shape = RoundedRectangle(24.dp),
                contentPadding = PaddingValues(18.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        StepCardHeader(
                            title = stringResource(Res.string.ui_library_booking_step_3),
                            icon = LiquidIcons.Check,
                            modifier = Modifier.weight(1f),
                        )
                        LiquidBadge(
                            text = stringResource(
                                Res.string.ui_library_booking_selected_slots,
                                uiState.selectedSlotIds.size,
                            ),
                            containerColor = colorScheme.primary.copy(alpha = 0.14f),
                            contentColor = colorScheme.primary,
                        )
                    }

                    selectedSlotsRequiringDevice.forEach { slot ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, colorScheme.outlineVariant.copy(alpha = 0.34f), fieldShape),
                        ) {
                            LiquidTextField(
                                value = uiState.deviceAddresses[slot.slotId].orEmpty(),
                                onValueChange = { onUpdateDeviceAddress(slot.slotId, it) },
                                type = LiquidTextFieldType.Text,
                                label = "${stringResource(Res.string.ui_library_device_address_label)} (${slot.timeLabel})",
                                placeholder = stringResource(Res.string.ui_library_device_address_placeholder),
                                shape = fieldShape,
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, colorScheme.outlineVariant.copy(alpha = 0.34f), fieldShape),
                    ) {
                        LiquidTextField(
                            value = uiState.note,
                            onValueChange = onUpdateNote,
                            type = LiquidTextFieldType.Text,
                            label = stringResource(Res.string.ui_library_booking_note_label),
                            placeholder = stringResource(Res.string.ui_library_booking_note_placeholder),
                            shape = fieldShape,
                        )
                    }

                    LiquidButton(
                        text = stringResource(
                            Res.string.ui_library_booking_confirm_button,
                            uiState.selectedSlotIds.size,
                        ),
                        onClick = onBook,
                        enabled = !uiState.isSubmitting,
                        isLoading = uiState.isSubmitting,
                        variant = LiquidButtonVariant.Primary,
                        size = LiquidButtonSize.Large,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun StepCardHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            imageVector = icon,
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
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onSurface,
        )
    }
}
