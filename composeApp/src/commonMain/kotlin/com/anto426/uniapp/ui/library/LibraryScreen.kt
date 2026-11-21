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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
import com.anto426.liquidmonet.components.feedback.loading.LiquidLoading
import com.anto426.liquidmonet.components.feedback.loading.LiquidLoadingSize
import com.anto426.liquidmonet.components.feedback.loading.LiquidLoadingStyle
import com.anto426.liquidmonet.components.inputs.textfield.LiquidTextField
import com.anto426.liquidmonet.components.inputs.textfield.LiquidTextFieldType
import com.anto426.liquidmonet.components.navigation.navigationbar.LiquidNavigationItem
import com.anto426.liquidmonet.components.navigation.tabbar.LiquidTabBar
import com.anto426.liquidmonet.components.selection.chip.LiquidChip
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.library.model.LibraryCatalogBook
import com.anto426.uniapp.library.model.LibraryCatalogPage
import com.anto426.uniapp.library.model.LibraryMovementItem
import com.anto426.uniapp.library.model.LibrarySpaceBooking
import com.anto426.uniapp.library.presentation.LibraryUiState
import com.anto426.uniapp.ui.components.display.UniInsetGroup
import com.anto426.uniapp.ui.components.display.UniInsetRow
import com.anto426.uniapp.ui.components.display.UniSecurityFooterNote
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.components.state.FeatureStateContent
import com.anto426.uniapp.ui.didactics.components.GradesMetricTile
import com.kyant.shapes.RoundedRectangle
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

private val quickTopics = listOf(
    "Informatica",
    "Diritto",
    "Economia",
    "Medicina",
    "Agraria",
)

@Composable
fun LibraryScreen(
    uiState: LibraryUiState,
    onTabSelected: (Int) -> Unit,
    onQueryChange: (String) -> Unit,
    onSearch: (String?, Int) -> Unit,
    onBookClick: (LibraryCatalogBook) -> Unit,
    onRefreshMovements: () -> Unit,
    onRequestCancelMovement: (LibraryMovementItem) -> Unit,
    onConfirmCancelMovement: () -> Unit,
    onDismissCancelMovement: () -> Unit,
    onRenewMovement: (LibraryMovementItem) -> Unit,
    onRefreshSpaces: () -> Unit,
    onOpenSpaceBooking: () -> Unit,
    onRequestCancelBooking: (LibrarySpaceBooking) -> Unit,
    onConfirmCancelBooking: () -> Unit,
    onDismissCancelBooking: () -> Unit,
) {
    LaunchedEffect(Unit) {
        when (uiState.selectedTab) {
            1 -> if (uiState.hasLoadedMovements) onRefreshMovements()
            2 -> if (uiState.hasLoadedSpaces) onRefreshSpaces()
        }
    }

    val tabs = listOf(
        LiquidNavigationItem(label = stringResource(Res.string.ui_library_tab_catalog)),
        LiquidNavigationItem(label = stringResource(Res.string.ui_library_tab_loans)),
        LiquidNavigationItem(label = stringResource(Res.string.ui_library_tab_spaces)),
    )

    UniScreenColumn {
        LiquidTabBar(
            items = tabs,
            selectedIndex = uiState.selectedTab,
            onTabSelected = onTabSelected,
        )

        when (uiState.selectedTab) {
            0 -> LibraryCatalogTab(
                uiState = uiState,
                onQueryChange = onQueryChange,
                onSearch = onSearch,
                onBookClick = onBookClick,
            )

            1 -> LibraryLoansTab(
                uiState = uiState,
                onRefresh = onRefreshMovements,
                onRequestCancel = onRequestCancelMovement,
                onConfirmCancel = onConfirmCancelMovement,
                onDismissCancel = onDismissCancelMovement,
                onRenew = onRenewMovement,
            )

            else -> LibrarySpacesTab(
                uiState = uiState,
                onRefresh = onRefreshSpaces,
                onOpenSpaceBooking = onOpenSpaceBooking,
                onRequestCancel = onRequestCancelBooking,
                onConfirmCancel = onConfirmCancelBooking,
                onDismissCancel = onDismissCancelBooking,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LibraryCatalogTab(
    uiState: LibraryUiState,
    onQueryChange: (String) -> Unit,
    onSearch: (String?, Int) -> Unit,
    onBookClick: (LibraryCatalogBook) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val fieldShape = RoundedRectangle(16.dp)

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        LiquidCard(
            shape = RoundedRectangle(24.dp),
            contentPadding = PaddingValues(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = LiquidIcons.MenuBook,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.liquidIconContainer(
                            containerSize = 40.dp,
                            iconSize = 20.dp,
                            containerColor = colorScheme.primary.copy(alpha = 0.14f),
                            shape = RoundedRectangle(12.dp),
                        ),
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = stringResource(Res.string.ui_library_hero_eyebrow),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.primary,
                        )
                        Text(
                            text = stringResource(Res.string.ui_library_hero_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface,
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                width = 1.dp,
                                color = colorScheme.outlineVariant.copy(alpha = 0.34f),
                                shape = fieldShape,
                            ),
                    ) {
                        LiquidTextField(
                            value = uiState.query,
                            onValueChange = onQueryChange,
                            type = LiquidTextFieldType.Text,
                            label = stringResource(Res.string.ui_library_search_label),
                            placeholder = stringResource(Res.string.ui_library_search_placeholder),
                            leadingIcon = LiquidIcons.Search,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { onSearch(null, 1) }),
                            shape = fieldShape,
                        )
                    }
                    LiquidButton(
                        text = stringResource(Res.string.ui_library_search_action),
                        onClick = { onSearch(null, 1) },
                        enabled = uiState.query.isNotBlank() && !uiState.isSearching,
                        isLoading = uiState.isSearching,
                        variant = LiquidButtonVariant.Primary,
                        size = LiquidButtonSize.Medium,
                    )
                }

                LiquidHorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.25f))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(Res.string.ui_library_quick_topics),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurfaceVariant,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        quickTopics.forEach { topic ->
                            val selected = uiState.submittedQuery.equals(topic, ignoreCase = true)
                            LiquidChip(
                                label = topic,
                                selected = selected,
                                leadingIcon = if (selected) LiquidIcons.Check else null,
                                onClick = { onSearch(topic, 1) },
                            )
                        }
                    }
                }
            }
        }

        when {
            uiState.isSearching -> {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    LiquidLoading(
                        style = LiquidLoadingStyle.Dots,
                        size = LiquidLoadingSize.Large,
                        message = stringResource(Res.string.ui_loading),
                    )
                }
            }

            uiState.catalogError != null -> {
                LiquidEmptyState(
                    title = stringResource(Res.string.ui_state_error_title),
                    description = uiState.catalogError,
                    icon = LiquidIcons.Warning,
                    actionButtonText = stringResource(Res.string.ui_retry),
                    onActionClick = {
                        onSearch(uiState.submittedQuery.ifBlank { uiState.query }, uiState.currentPage)
                    },
                )
            }

            !uiState.hasSearched -> {
                LiquidSectionHeader(
                    title = stringResource(Res.string.ui_library_branches_title),
                    subtitle = stringResource(Res.string.ui_library_branches_subtitle),
                )
                UniInsetGroup(cornerRadius = 22.dp) {
                    UniInsetRow(
                        title = stringResource(Res.string.ui_library_branch_campobasso),
                        subtitle = stringResource(Res.string.ui_library_branch_campobasso_sub),
                        icon = LiquidIcons.Home,
                        iconTint = colorScheme.primary,
                        trailingContent = {
                            LiquidBadge(
                                text = "MOLBU",
                                containerColor = colorScheme.primary.copy(alpha = 0.12f),
                                contentColor = colorScheme.primary,
                            )
                        },
                        showDivider = true,
                    )
                    UniInsetRow(
                        title = stringResource(Res.string.ui_library_branch_pesche),
                        subtitle = stringResource(Res.string.ui_library_branch_pesche_sub),
                        icon = LiquidIcons.MenuBook,
                        iconTint = colorScheme.secondary,
                        trailingContent = {
                            LiquidBadge(
                                text = "MOLBA",
                                containerColor = colorScheme.secondary.copy(alpha = 0.12f),
                                contentColor = colorScheme.secondary,
                            )
                        },
                        showDivider = true,
                    )
                    UniInsetRow(
                        title = stringResource(Res.string.ui_library_branch_termoli),
                        subtitle = stringResource(Res.string.ui_library_branch_termoli_sub),
                        icon = LiquidIcons.MenuBook,
                        iconTint = colorScheme.tertiary,
                        trailingContent = {
                            LiquidBadge(
                                text = "MOLBT",
                                containerColor = colorScheme.tertiary.copy(alpha = 0.12f),
                                contentColor = colorScheme.tertiary,
                            )
                        },
                        showDivider = false,
                    )
                }
            }

            uiState.books.isEmpty() -> {
                LiquidEmptyState(
                    title = stringResource(Res.string.ui_library_results_empty),
                    description = stringResource(Res.string.ui_library_results_empty_desc),
                    icon = LiquidIcons.Search,
                )
            }

            else -> {
                LiquidSectionHeader(
                    title = uiState.submittedQuery.ifBlank { stringResource(Res.string.ui_library_results_title) },
                    subtitle = "${stringResource(Res.string.ui_library_results_count, uiState.totalResults)} · ${
                        stringResource(Res.string.ui_library_page_label, uiState.currentPage)
                    }",
                )

                UniInsetGroup(cornerRadius = 22.dp) {
                    uiState.books.forEachIndexed { index, book ->
                        LibraryBookListRow(
                            book = book,
                            onClick = { onBookClick(book) },
                        )
                        if (index < uiState.books.lastIndex) {
                            LiquidHorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.25f))
                        }
                    }
                }

                if (uiState.currentPage > 1 || uiState.totalResults > LibraryCatalogPage.PAGE_SIZE) {
                    val hasNext = uiState.currentPage * LibraryCatalogPage.PAGE_SIZE < uiState.totalResults
                    LiquidCard(
                        shape = RoundedRectangle(20.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            LiquidButton(
                                text = stringResource(Res.string.ui_library_prev_page),
                                onClick = { onSearch(uiState.submittedQuery, uiState.currentPage - 1) },
                                enabled = uiState.currentPage > 1 && !uiState.isSearching,
                                variant = LiquidButtonVariant.Secondary,
                                size = LiquidButtonSize.Small,
                                modifier = Modifier.weight(1f),
                            )
                            LiquidBadge(
                                text = stringResource(Res.string.ui_library_page_label, uiState.currentPage),
                                containerColor = colorScheme.primary.copy(alpha = 0.14f),
                                contentColor = colorScheme.primary,
                            )
                            LiquidButton(
                                text = stringResource(Res.string.ui_library_next_page),
                                onClick = { onSearch(uiState.submittedQuery, uiState.currentPage + 1) },
                                enabled = hasNext && !uiState.isSearching,
                                variant = LiquidButtonVariant.Secondary,
                                size = LiquidButtonSize.Small,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }

        UniSecurityFooterNote(text = stringResource(Res.string.ui_library_open_opac_note))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LibraryBookListRow(
    book: LibraryCatalogBook,
    onClick: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
        Spacer(Modifier.width(14.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = book.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            val line = listOf(book.subtitle, book.metadata).filter { it.isNotBlank() }.joinToString(" · ")
            if (line.isNotBlank()) {
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (book.branches.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    book.branches.forEach { branch ->
                        LiquidBadge(
                            text = branch,
                            containerColor = colorScheme.tertiary.copy(alpha = 0.14f),
                            contentColor = colorScheme.tertiary,
                        )
                    }
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        Icon(
            imageVector = LiquidIcons.ChevronRight,
            contentDescription = null,
            tint = colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun LibraryLoansTab(
    uiState: LibraryUiState,
    onRefresh: () -> Unit,
    onRequestCancel: (LibraryMovementItem) -> Unit,
    onConfirmCancel: () -> Unit,
    onDismissCancel: () -> Unit,
    onRenew: (LibraryMovementItem) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors

    if (uiState.pendingCancelMovement != null) {
        LiquidDialog(
            onDismissRequest = onDismissCancel,
            title = stringResource(Res.string.ui_library_cancel_movement_title),
            text = stringResource(Res.string.ui_library_cancel_movement_confirm),
            confirmButton = {
                LiquidButton(
                    text = stringResource(Res.string.ui_library_cancel_movement),
                    onClick = onConfirmCancel,
                    variant = LiquidButtonVariant.Primary,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            dismissButton = {
                LiquidButton(
                    text = stringResource(Res.string.ui_cancel),
                    onClick = onDismissCancel,
                    variant = LiquidButtonVariant.Text,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
        )
    }

    FeatureStateContent(
        state = uiState.movementsLoadState,
        errorMessage = uiState.movementsError,
        onRetry = onRefresh,
        emptyMessage = stringResource(Res.string.ui_library_movements_empty),
        inline = true,
    ) {
        val activeLoansCount = uiState.movements.count { it.canRenew }
        val activeRequestsCount = uiState.movements.count { it.canCancel }

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            LiquidCard(
                shape = RoundedRectangle(24.dp),
                contentPadding = PaddingValues(18.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(
                            imageVector = LiquidIcons.MenuBook,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.liquidIconContainer(
                                containerSize = 36.dp,
                                iconSize = 18.dp,
                                containerColor = colorScheme.primary.copy(alpha = 0.14f),
                                shape = RoundedRectangle(11.dp),
                            ),
                        )
                        Text(
                            text = stringResource(Res.string.ui_library_movements_header),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface,
                        )
                    }

                    LiquidHorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.25f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        GradesMetricTile(
                            label = stringResource(Res.string.ui_library_loans_in_progress),
                            value = "$activeLoansCount",
                            valueColor = glassColors.success,
                            modifier = Modifier.weight(1f),
                        )
                        GradesMetricTile(
                            label = stringResource(Res.string.ui_library_loans_requests),
                            value = "$activeRequestsCount",
                            valueColor = colorScheme.primary,
                            modifier = Modifier.weight(1f),
                        )
                        GradesMetricTile(
                            label = stringResource(Res.string.ui_library_loans_total),
                            value = "${uiState.movements.size}",
                            valueColor = colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            LiquidSectionHeader(
                title = stringResource(Res.string.ui_library_movements_header),
                subtitle = stringResource(Res.string.ui_library_movements_count, uiState.movements.size),
            )

            uiState.movements.forEach { movement ->
                val isBusy = uiState.busyMovementToken == movement.actionToken && movement.actionToken != null
                LiquidCard(
                    shape = RoundedRectangle(22.dp),
                    contentPadding = PaddingValues(16.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                movement.branchName?.takeIf(String::isNotBlank)?.let { branchName ->
                                    LiquidBadge(
                                        text = branchName,
                                        containerColor = colorScheme.primary.copy(alpha = 0.14f),
                                        contentColor = colorScheme.primary,
                                    )
                                }
                                movement.dueDateLabel?.takeIf(String::isNotBlank)?.let { dueDateLabel ->
                                    LiquidBadge(
                                        text = dueDateLabel,
                                        containerColor = colorScheme.secondary.copy(alpha = 0.14f),
                                        contentColor = colorScheme.secondary,
                                    )
                                }
                            }
                            if (movement.canRenew || movement.canCancel) {
                                val statusResource = if (movement.canRenew) Res.string.ui_library_loans_in_progress else Res.string.ui_library_active_request
                                val badgeColor = if (movement.canRenew) glassColors.success else colorScheme.tertiary
                                LiquidBadge(
                                    text = stringResource(statusResource),
                                    containerColor = badgeColor.copy(alpha = 0.14f),
                                    contentColor = badgeColor,
                                )
                            }
                        }

                        Text(
                            text = movement.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onSurface,
                        )

                        if (movement.detail.isNotBlank() && movement.detail != movement.title) {
                            Text(
                                text = movement.detail,
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.onSurfaceVariant,
                            )
                        }

                        if (movement.canCancel || movement.canRenew) {
                            LiquidHorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.25f))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                if (movement.canRenew) {
                                    LiquidButton(
                                        text = stringResource(Res.string.ui_library_renew_loan),
                                        onClick = { onRenew(movement) },
                                        enabled = uiState.busyMovementToken == null,
                                        isLoading = isBusy,
                                        variant = LiquidButtonVariant.Primary,
                                        size = LiquidButtonSize.Small,
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                                if (movement.canCancel) {
                                    LiquidButton(
                                        text = stringResource(Res.string.ui_library_cancel_movement),
                                        onClick = { onRequestCancel(movement) },
                                        enabled = uiState.busyMovementToken == null,
                                        isLoading = isBusy,
                                        variant = LiquidButtonVariant.Secondary,
                                        size = LiquidButtonSize.Small,
                                        modifier = Modifier.weight(1f),
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LibrarySpacesTab(
    uiState: LibraryUiState,
    onRefresh: () -> Unit,
    onOpenSpaceBooking: () -> Unit,
    onRequestCancel: (LibrarySpaceBooking) -> Unit,
    onConfirmCancel: () -> Unit,
    onDismissCancel: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme

    if (uiState.pendingCancelBooking != null) {
        LiquidDialog(
            onDismissRequest = onDismissCancel,
            title = stringResource(Res.string.ui_library_cancel_space),
            text = stringResource(Res.string.ui_library_cancel_space_confirm),
            confirmButton = {
                LiquidButton(
                    text = stringResource(Res.string.ui_library_cancel_space),
                    onClick = onConfirmCancel,
                    variant = LiquidButtonVariant.Primary,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            dismissButton = {
                LiquidButton(
                    text = stringResource(Res.string.ui_cancel),
                    onClick = onDismissCancel,
                    variant = LiquidButtonVariant.Text,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        LiquidCard(
            shape = RoundedRectangle(24.dp),
            contentPadding = PaddingValues(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = LiquidIcons.Calendar,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.liquidIconContainer(
                            containerSize = 36.dp,
                            iconSize = 18.dp,
                            containerColor = colorScheme.primary.copy(alpha = 0.14f),
                            shape = RoundedRectangle(11.dp),
                        ),
                    )
                    Text(
                        text = stringResource(Res.string.ui_library_spaces_active_count),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurfaceVariant,
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(Res.string.ui_library_book_space_cta),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(Res.string.ui_library_book_space_cta_sub),
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                    )
                }

                LiquidButton(
                    text = stringResource(Res.string.nav_route_library_booking_title),
                    onClick = onOpenSpaceBooking,
                    variant = LiquidButtonVariant.Primary,
                    size = LiquidButtonSize.Medium,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        FeatureStateContent(
            state = uiState.spacesLoadState,
            errorMessage = uiState.spacesError,
            onRetry = onRefresh,
            emptyMessage = stringResource(Res.string.ui_library_spaces_empty),
            inline = true,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                LiquidSectionHeader(
                    title = stringResource(Res.string.ui_library_spaces_header),
                    subtitle = "${uiState.spaceBookings.size}",
                )

                uiState.spaceBookings.forEach { booking ->
                    val isBusy = uiState.busyBookingToken == booking.bookingToken && booking.bookingToken != null
                    LiquidCard(
                        shape = RoundedRectangle(22.dp),
                        contentPadding = PaddingValues(16.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (!booking.dateLabel.isNullOrBlank() || !booking.timeLabel.isNullOrBlank()) {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    booking.dateLabel?.takeIf(String::isNotBlank)?.let { dateLabel ->
                                        LiquidBadge(
                                            text = dateLabel,
                                            containerColor = colorScheme.primary.copy(alpha = 0.14f),
                                            contentColor = colorScheme.primary,
                                        )
                                    }
                                    booking.timeLabel?.takeIf(String::isNotBlank)?.let { timeLabel ->
                                        LiquidBadge(
                                            text = timeLabel,
                                            containerColor = colorScheme.secondary.copy(alpha = 0.14f),
                                            contentColor = colorScheme.secondary,
                                        )
                                    }
                                }
                            }

                            Text(
                                text = booking.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.onSurface,
                            )

                            if (booking.description.isNotBlank() && booking.description != booking.title) {
                                Text(
                                    text = booking.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colorScheme.onSurfaceVariant,
                                )
                            }

                            if (booking.canCancel) {
                                LiquidHorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.25f))
                                LiquidButton(
                                    text = stringResource(Res.string.ui_library_cancel_space),
                                    onClick = { onRequestCancel(booking) },
                                    enabled = uiState.busyBookingToken == null,
                                    isLoading = isBusy,
                                    variant = LiquidButtonVariant.Secondary,
                                    size = LiquidButtonSize.Small,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
