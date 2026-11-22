package com.anto426.uniapp.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.buttons.button.LiquidButton
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonSize
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonVariant
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.cards.preferenceitem.LiquidPreferenceItem
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.emptystate.LiquidEmptyState
import com.anto426.liquidmonet.components.feedback.loading.LiquidLoading
import com.anto426.liquidmonet.components.feedback.loading.LiquidLoadingSize
import com.anto426.liquidmonet.components.feedback.loading.LiquidLoadingStyle
import com.anto426.liquidmonet.components.inputs.textfield.LiquidTextField
import com.anto426.liquidmonet.components.inputs.textfield.LiquidTextFieldType
import com.anto426.liquidmonet.components.selection.checkbox.LiquidCheckbox
import com.anto426.liquidmonet.components.selection.select.LiquidSelect
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.library.presentation.LibrarySpaceBookingUiState
import com.anto426.uniapp.ui.components.layout.UniAnimatedEntrance
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.library.components.StepCardHeader
import com.kyant.shapes.RoundedRectangle
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

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
    val selectedType = overview.types.firstOrNull { it.id == uiState.selectedTypeId }
    val selectedOwner = uiState.owners.firstOrNull { it.id == uiState.selectedOwnerId }
    val selectedResource = uiState.resources.firstOrNull { it.id == uiState.selectedResourceId }

    LaunchedEffect(uiState.bookingCompleted) {
        if (uiState.bookingCompleted) {
            onBookingCompleted()
        }
    }

    UniScreenColumn {
        // Step 1: Tipologia, Sede e Risorsa (LiquidSelect)
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

                    LiquidSelect(
                        items = overview.types,
                        selectedItem = selectedType,
                        onItemSelected = { onSelectType(it.id) },
                        label = stringResource(Res.string.ui_library_booking_type_title),
                        itemLabel = { it.label },
                        leadingIcon = LiquidIcons.MenuBook,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    if (uiState.owners.isNotEmpty()) {
                        LiquidSelect(
                            items = uiState.owners,
                            selectedItem = selectedOwner,
                            onItemSelected = { onSelectOwner(it.id) },
                            label = stringResource(Res.string.ui_library_booking_owner_title),
                            itemLabel = { it.label },
                            leadingIcon = LiquidIcons.Home,
                            modifier = Modifier.fillMaxWidth(),
                        )
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
                        LiquidSelect(
                            items = uiState.resources,
                            selectedItem = selectedResource,
                            onItemSelected = { onSelectResource(it.id) },
                            label = stringResource(Res.string.ui_library_booking_resource_title),
                            itemLabel = { it.label },
                            leadingIcon = LiquidIcons.Star,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }

        // Step 2: Fasce orarie (LiquidPreferenceItem + LiquidCheckbox)
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
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                calendar.slots.forEachIndexed { index, slot ->
                                    val selected = slot.slotId in uiState.selectedSlotIds
                                    val subtitle = buildList {
                                        slot.dateLabel?.takeIf(String::isNotBlank)?.let(::add)
                                        (slot.seatsLabel ?: slot.description)
                                            .takeIf(String::isNotBlank)
                                            ?.let(::add)
                                    }.joinToString(" · ").takeIf(String::isNotBlank)

                                    LiquidPreferenceItem(
                                        title = slot.timeLabel,
                                        subtitle = subtitle,
                                        icon = LiquidIcons.Time,
                                        onClick = { onToggleSlot(slot.slotId) },
                                        trailingContent = {
                                            LiquidCheckbox(
                                                checked = selected,
                                                onCheckedChange = { onToggleSlot(slot.slotId) },
                                            )
                                        },
                                    )
                                    if (index < calendar.slots.lastIndex) {
                                        LiquidHorizontalDivider(
                                            color = colorScheme.outlineVariant.copy(alpha = 0.25f),
                                        )
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
                        LiquidTextField(
                            value = uiState.deviceAddresses[slot.slotId].orEmpty(),
                            onValueChange = { onUpdateDeviceAddress(slot.slotId, it) },
                            type = LiquidTextFieldType.Text,
                            label = "${stringResource(Res.string.ui_library_device_address_label)} (${slot.timeLabel})",
                            placeholder = stringResource(Res.string.ui_library_device_address_placeholder),
                            shape = fieldShape,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    LiquidTextField(
                        value = uiState.note,
                        onValueChange = onUpdateNote,
                        type = LiquidTextFieldType.Text,
                        label = stringResource(Res.string.ui_library_booking_note_label),
                        placeholder = stringResource(Res.string.ui_library_booking_note_placeholder),
                        shape = fieldShape,
                        modifier = Modifier.fillMaxWidth(),
                    )

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
