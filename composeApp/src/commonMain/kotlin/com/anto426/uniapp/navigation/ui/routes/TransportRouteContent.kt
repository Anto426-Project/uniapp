package com.anto426.uniapp.navigation.ui.routes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anto426.uniapp.navigation.model.AppRoute
import com.anto426.uniapp.navigation.ui.AppRouteContext
import com.anto426.uniapp.navigation.ui.ReservationDeleteAction
import com.anto426.uniapp.navigation.ui.ReservationDeleteActionHost
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.transport.presentation.ReservationDetailViewModel
import com.anto426.uniapp.transport.presentation.TransportBookingViewModel
import com.anto426.uniapp.transport.presentation.TransportViewModel
import com.anto426.uniapp.ui.components.state.FeatureStateContent
import com.anto426.uniapp.ui.transport.ReservationDetailScreen
import com.anto426.uniapp.ui.transport.TransportBookingScreen
import com.anto426.uniapp.ui.transport.TransportScreen
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
internal fun TransportRouteContent(context: AppRouteContext) {
    val transportViewModel = viewModel(key = context.viewModelKey) { TransportViewModel(context.dataSource) }
    val transportUiState by transportViewModel.uiState.collectAsStateWithLifecycle()
    FeatureStateContent(
        transportUiState.loadState,
        transportUiState.errorMessage,
        onRetry = { transportViewModel.refresh(force = true) },
        emptyMessage = stringResource(Res.string.ui_state_empty_transport_reservations),
    ) {
        TransportScreen(
            uiState = transportUiState,
            onReservationClick = { reservation ->
                context.navigator.navigate(
                    AppRoute.ReservationDetail(
                        reservationId = reservation.id,
                        title = reservation.route,
                    ),
                )
            },
            onBookClick = { context.navigator.navigate(AppRoute.TransportBooking) },
        )
    }
}

@Composable
internal fun TransportBookingRouteContent(context: AppRouteContext) {
    val bookingViewModel = viewModel(key = context.viewModelKey) {
        TransportBookingViewModel(context.dataSource, context.toastSink)
    }
    val bookingUiState by bookingViewModel.uiState.collectAsStateWithLifecycle()
    FeatureStateContent(
        bookingUiState.loadState,
        bookingUiState.errorMessage,
        onRetry = { bookingViewModel.refresh(force = true) },
        emptyMessage = stringResource(Res.string.ui_state_empty_transport_lines),
    ) {
        TransportBookingScreen(
            uiState = bookingUiState,
            onRouteSelected = bookingViewModel::selectRoute,
            onBook = { dates, direction -> bookingViewModel.book(dates, direction) },
        )
    }
}

@Composable
internal fun ReservationDetailRouteContent(
    route: AppRoute.ReservationDetail,
    context: AppRouteContext,
    reservationDeleteActionHost: ReservationDeleteActionHost,
) {
    val detailViewModel = viewModel(key = context.viewModelKey) {
        ReservationDetailViewModel(route.reservationId, context.dataSource, context.toastSink)
    }
    val detailUiState by detailViewModel.uiState.collectAsStateWithLifecycle()
    val hasVisibleReservation = detailUiState.loadState == FeatureLoadState.Content &&
        detailUiState.reservation != null && !detailUiState.deleted
    val deleteAction = remember(
        route.reservationId,
        detailViewModel,
        hasVisibleReservation,
        detailUiState.isDeleting,
    ) {
        if (hasVisibleReservation) {
            ReservationDeleteAction(
                reservationId = route.reservationId,
                enabled = !detailUiState.isDeleting,
                onClick = detailViewModel::delete,
            )
        } else {
            null
        }
    }
    DisposableEffect(deleteAction, reservationDeleteActionHost) {
        if (deleteAction != null) reservationDeleteActionHost.register(deleteAction)
        onDispose {
            if (deleteAction != null) reservationDeleteActionHost.unregister(deleteAction)
        }
    }
    LaunchedEffect(detailUiState.deleted) {
        if (detailUiState.deleted) context.navigator.goBack()
    }
    FeatureStateContent(
        detailUiState.loadState,
        detailUiState.errorMessage,
        onRetry = { detailViewModel.refresh(force = true) },
    ) {
        detailUiState.reservation?.let { reservation ->
            ReservationDetailScreen(
                reservation = reservation,
                imageState = detailUiState.image,
                onReloadImage = { detailViewModel.loadTicketImage(forceRefresh = true) },
            )
        }
    }
}
