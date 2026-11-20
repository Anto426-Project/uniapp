package com.anto426.uniapp.ui.library

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.buttons.button.LiquidButton
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonSize
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonVariant
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.emptystate.LiquidEmptyState
import com.anto426.liquidmonet.components.display.iconcontainer.liquidIconContainer
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.liquidmonet.components.feedback.dialog.LiquidDialog
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.library.model.LibraryBookCopyAction
import com.anto426.uniapp.library.presentation.LibraryBookDetailUiState
import com.anto426.uniapp.ui.components.display.UniInsetGroup
import com.anto426.uniapp.ui.components.display.UniInsetRow
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.didactics.components.GradesMetricTile
import com.kyant.shapes.RoundedRectangle
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LibraryBookDetailScreen(
    uiState: LibraryBookDetailUiState,
    onPrepareAction: (LibraryBookCopyAction) -> Unit,
    onConfirmDraft: () -> Unit,
    onDismissDraft: () -> Unit,
) {
    val book = uiState.book ?: return
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors

    if (uiState.pendingDraft != null) {
        LiquidDialog(
            onDismissRequest = onDismissDraft,
            title = stringResource(Res.string.ui_library_confirm_dialog_title),
            text = uiState.pendingDraft.description,
            confirmButton = {
                LiquidButton(
                    text = stringResource(Res.string.ui_library_confirm_request),
                    onClick = onConfirmDraft,
                    enabled = !uiState.isSubmitting,
                    isLoading = uiState.isSubmitting,
                    variant = LiquidButtonVariant.Primary,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            dismissButton = {
                LiquidButton(
                    text = stringResource(Res.string.ui_cancel),
                    onClick = onDismissDraft,
                    enabled = !uiState.isSubmitting,
                    variant = LiquidButtonVariant.Text,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
        )
    }

    UniScreenColumn {
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
                    LiquidBadge(
                        text = book.id,
                        containerColor = colorScheme.primary.copy(alpha = 0.14f),
                        contentColor = colorScheme.primary,
                    )
                    if (book.isAvailable) {
                        val availabilityLabel = if (book.hasAvailableDocument) {
                            stringResource(Res.string.ui_library_document_available)
                        } else {
                            stringResource(Res.string.ui_library_requestable_copies, book.actions.size)
                        }
                        LiquidBadge(
                            text = availabilityLabel,
                            containerColor = glassColors.success.copy(alpha = 0.14f),
                            contentColor = glassColors.success,
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                        letterSpacing = (-0.3).sp,
                    )

                    if (!book.author.isNullOrBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = LiquidIcons.AccountCircle,
                                contentDescription = null,
                                tint = colorScheme.primary,
                                modifier = Modifier.size(15.dp),
                            )
                            Text(
                                text = book.author,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }

                LiquidHorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.25f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    GradesMetricTile(
                        label = stringResource(Res.string.ui_library_book_year),
                        value = book.year ?: "—",
                        valueColor = colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    GradesMetricTile(
                        label = stringResource(Res.string.ui_library_book_branches),
                        value = book.branches.size.takeIf { it > 0 }?.toString() ?: "—",
                        valueColor = colorScheme.primary,
                        modifier = Modifier.weight(1f),
                    )
                    GradesMetricTile(
                        label = stringResource(Res.string.ui_library_book_copies),
                        value = "${book.actions.size}",
                        valueColor = if (book.actions.isNotEmpty()) glassColors.success else colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                }

                if (book.branches.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        book.branches.forEach { branch ->
                            LiquidBadge(
                                text = branch,
                                containerColor = colorScheme.tertiary.copy(alpha = 0.12f),
                                contentColor = colorScheme.tertiary,
                            )
                        }
                    }
                }
            }
        }

        if (uiState.submissionResult != null) {
            LiquidCard(
                shape = RoundedRectangle(22.dp),
                contentPadding = PaddingValues(16.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        imageVector = LiquidIcons.Check,
                        contentDescription = null,
                        tint = glassColors.success,
                        modifier = Modifier.size(18.dp),
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = uiState.submissionResult.description,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = glassColors.success,
                        )
                        if (uiState.submissionResult.requiresVerification) {
                            Text(
                                text = stringResource(Res.string.ui_library_check_loans_note),
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }

        LiquidSectionHeader(
            title = stringResource(Res.string.ui_library_copies_title),
            subtitle = stringResource(Res.string.ui_library_copies_subtitle),
        )

        if (book.actions.isEmpty()) {
            LiquidEmptyState(
                title = stringResource(Res.string.ui_library_no_actions),
                description = stringResource(Res.string.ui_state_no_data),
                icon = LiquidIcons.MenuBook,
            )
        } else {
            UniInsetGroup(cornerRadius = 22.dp) {
                book.actions.forEachIndexed { index, action ->
                    val isPreparing = uiState.preparingActionId == action.actionId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = LiquidIcons.MenuBook,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.liquidIconContainer(
                                containerSize = 40.dp,
                                iconSize = 20.dp,
                                containerColor = colorScheme.primary.copy(alpha = 0.12f),
                                shape = RoundedRectangle(12.dp),
                            ),
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = action.branchName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.onSurface,
                            )
                            Text(
                                text = "${action.label} · ${action.movementType}",
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.onSurfaceVariant,
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        LiquidButton(
                            text = stringResource(Res.string.ui_library_request_action),
                            onClick = { onPrepareAction(action) },
                            enabled = uiState.preparingActionId == null && !uiState.isSubmitting,
                            isLoading = isPreparing,
                            variant = LiquidButtonVariant.Primary,
                            size = LiquidButtonSize.Small,
                        )
                    }
                    if (index < book.actions.lastIndex) {
                        LiquidHorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.25f))
                    }
                }
            }
        }

        val metadataRows = buildList {
            book.inventory?.let { add(Triple(stringResource(Res.string.ui_library_meta_inventory), it, LiquidIcons.Badge)) }
            book.shelfmark?.let { add(Triple(stringResource(Res.string.ui_library_meta_shelfmark), it, LiquidIcons.Search)) }
            book.notes?.let { add(Triple(stringResource(Res.string.ui_library_meta_notes), it, LiquidIcons.Info)) }
        }
        if (metadataRows.isNotEmpty()) {
            LiquidSectionHeader(title = stringResource(Res.string.ui_library_biblio_details_title))
            UniInsetGroup(cornerRadius = 22.dp) {
                metadataRows.forEachIndexed { index, (label, value, icon) ->
                    UniInsetRow(
                        title = label,
                        subtitle = value,
                        icon = icon,
                        iconTint = colorScheme.secondary,
                        showDivider = index < metadataRows.lastIndex,
                    )
                }
            }
        } else if (book.description.isNotBlank()) {
            LiquidSectionHeader(title = stringResource(Res.string.ui_library_biblio_details_title))
            LiquidCard(
                shape = RoundedRectangle(22.dp),
                contentPadding = PaddingValues(16.dp),
            ) {
                Text(
                    text = book.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
