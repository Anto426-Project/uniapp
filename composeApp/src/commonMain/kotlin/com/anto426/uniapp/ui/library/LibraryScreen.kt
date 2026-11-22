package com.anto426.uniapp.ui.library

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.anto426.liquidmonet.components.navigation.navigationbar.LiquidNavigationItem
import com.anto426.liquidmonet.components.navigation.tabbar.LiquidTabBar
import com.anto426.uniapp.data.contract.LibraryCatalogBook
import com.anto426.uniapp.data.contract.LibraryMovementItem
import com.anto426.uniapp.data.contract.LibrarySpaceBooking
import com.anto426.uniapp.library.presentation.LibraryUiState
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.components.layout.UniTabContentHost
import com.anto426.uniapp.ui.library.components.LibraryCatalogTab
import com.anto426.uniapp.ui.library.components.LibraryLoansTab
import com.anto426.uniapp.ui.library.components.LibrarySpacesTab
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

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

        UniTabContentHost(
            selectedTab = uiState.selectedTab,
            tabCount = tabs.size,
            onTabSelected = onTabSelected,
            label = "LibraryTabs",
        ) { tab ->
            when (tab) {
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
}
