package com.anto426.uniapp.navigation.ui.routes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anto426.uniapp.library.presentation.LibraryBookDetailViewModel
import com.anto426.uniapp.library.presentation.LibrarySpaceBookingViewModel
import com.anto426.uniapp.library.presentation.LibraryViewModel
import com.anto426.uniapp.navigation.model.AppRoute
import com.anto426.uniapp.navigation.ui.AppRouteContext
import com.anto426.uniapp.ui.components.state.FeatureStateContent
import com.anto426.uniapp.ui.library.LibraryBookDetailScreen
import com.anto426.uniapp.ui.library.LibraryScreen
import com.anto426.uniapp.ui.library.LibrarySpaceBookingScreen
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
internal fun LibraryRouteContent(
    route: AppRoute.Library,
    context: AppRouteContext,
) {
    val libraryViewModel = viewModel(key = context.viewModelKey) {
        LibraryViewModel(route.initialTab, context.dataSource, context.toastSink)
    }
    val libraryUiState by libraryViewModel.uiState.collectAsStateWithLifecycle()
    LibraryScreen(
        uiState = libraryUiState,
        onTabSelected = libraryViewModel::selectTab,
        onQueryChange = libraryViewModel::updateQuery,
        onSearch = libraryViewModel::search,
        onBookClick = { book ->
            context.navigator.navigate(AppRoute.LibraryBookDetail(bookId = book.id, title = book.title))
        },
        onRefreshMovements = libraryViewModel::refreshMovements,
        onRequestCancelMovement = libraryViewModel::requestCancelMovement,
        onConfirmCancelMovement = libraryViewModel::confirmCancelMovement,
        onDismissCancelMovement = libraryViewModel::dismissCancelMovement,
        onRenewMovement = libraryViewModel::renewMovement,
        onRefreshSpaces = libraryViewModel::refreshSpaces,
        onOpenSpaceBooking = { context.navigator.navigate(AppRoute.LibraryBooking) },
        onRequestCancelBooking = libraryViewModel::requestCancelBooking,
        onConfirmCancelBooking = libraryViewModel::confirmCancelBooking,
        onDismissCancelBooking = libraryViewModel::dismissCancelBooking,
    )
}

@Composable
internal fun LibraryBookDetailRouteContent(
    route: AppRoute.LibraryBookDetail,
    context: AppRouteContext,
) {
    val detailViewModel = viewModel(key = context.viewModelKey) {
        LibraryBookDetailViewModel(route.bookId, context.dataSource, context.toastSink)
    }
    val detailUiState by detailViewModel.uiState.collectAsStateWithLifecycle()
    FeatureStateContent(
        state = detailUiState.loadState,
        errorMessage = detailUiState.errorMessage,
        onRetry = detailViewModel::refresh,
    ) {
        LibraryBookDetailScreen(
            uiState = detailUiState,
            onPrepareAction = detailViewModel::prepareRequest,
            onConfirmDraft = detailViewModel::confirmDraft,
            onDismissDraft = detailViewModel::dismissDraft,
        )
    }
}

@Composable
internal fun LibraryBookingRouteContent(context: AppRouteContext) {
    val bookingViewModel = viewModel(key = context.viewModelKey) {
        LibrarySpaceBookingViewModel(context.dataSource, context.toastSink)
    }
    val bookingUiState by bookingViewModel.uiState.collectAsStateWithLifecycle()
    FeatureStateContent(
        state = bookingUiState.loadState,
        errorMessage = bookingUiState.errorMessage,
        onRetry = bookingViewModel::refresh,
        emptyMessage = stringResource(Res.string.ui_library_booking_slots_empty),
    ) {
        LibrarySpaceBookingScreen(
            uiState = bookingUiState,
            onSelectType = bookingViewModel::selectType,
            onSelectOwner = bookingViewModel::selectOwner,
            onSelectResource = bookingViewModel::selectResource,
            onLoadCalendarPage = bookingViewModel::loadCalendarPage,
            onToggleSlot = bookingViewModel::toggleSlot,
            onUpdateDeviceAddress = bookingViewModel::updateDeviceAddress,
            onUpdateNote = bookingViewModel::updateNote,
            onBook = bookingViewModel::book,
            onBookingCompleted = { context.navigator.goBack() },
        )
    }
}
