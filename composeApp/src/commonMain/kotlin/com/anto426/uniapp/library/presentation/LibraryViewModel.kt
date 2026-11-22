package com.anto426.uniapp.library.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.UniGateway
import com.anto426.uniapp.data.asUniRepository
import com.anto426.uniapp.feedback.runtime.AppToastSink
import com.anto426.uniapp.feedback.runtime.error
import com.anto426.uniapp.feedback.runtime.success
import com.anto426.uniapp.data.contract.LibraryBookingOption
import com.anto426.uniapp.data.contract.LibraryCatalogBook
import com.anto426.uniapp.data.contract.LibraryMovementItem
import com.anto426.uniapp.data.contract.LibrarySpaceBooking
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.userMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.anto426.uniapp.localization.readAppString
import uniapp.composeapp.generated.resources.*

data class LibraryUiState(
    val selectedTab: Int = 0,
    val query: String = "",
    val submittedQuery: String = "",
    val hasSearched: Boolean = false,
    val books: List<LibraryCatalogBook> = emptyList(),
    val currentPage: Int = 1,
    val totalResults: Int = 0,
    val isSearching: Boolean = false,
    val catalogError: String? = null,
    val movementsLoadState: FeatureLoadState = FeatureLoadState.Loading,
    val movements: List<LibraryMovementItem> = emptyList(),
    val movementsError: String? = null,
    val hasLoadedMovements: Boolean = false,
    val busyMovementToken: String? = null,
    val pendingCancelMovement: LibraryMovementItem? = null,
    val spacesLoadState: FeatureLoadState = FeatureLoadState.Loading,
    val spaceBookings: List<LibrarySpaceBooking> = emptyList(),
    val spaceTypes: List<LibraryBookingOption> = emptyList(),
    val spacesError: String? = null,
    val hasLoadedSpaces: Boolean = false,
    val busyBookingToken: String? = null,
    val pendingCancelBooking: LibrarySpaceBooking? = null,
)

class LibraryViewModel(
    initialTab: Int = 0,
    private val dataSource: UniGateway,
    private val toastSink: AppToastSink = AppToastSink.None,
) : ViewModel() {
    private val repository = dataSource.asUniRepository(viewModelScope)
    private val mutableUiState = MutableStateFlow(LibraryUiState(selectedTab = initialTab.coerceIn(0, 2)))
    val uiState: StateFlow<LibraryUiState> = mutableUiState.asStateFlow()

    init {
        when (mutableUiState.value.selectedTab) {
            1 -> refreshMovements()
            2 -> refreshSpaces()
        }
    }

    fun selectTab(index: Int) {
        val clamped = index.coerceIn(0, 2)
        if (mutableUiState.value.selectedTab == clamped) return
        mutableUiState.value = mutableUiState.value.copy(selectedTab = clamped)
        when (clamped) {
            1 -> if (!mutableUiState.value.hasLoadedMovements) refreshMovements()
            2 -> if (!mutableUiState.value.hasLoadedSpaces) refreshSpaces()
        }
    }

    fun updateQuery(query: String) {
        mutableUiState.value = mutableUiState.value.copy(query = query)
    }

    fun search(queryOverride: String? = null, page: Int = 1) {
        val targetQuery = (queryOverride ?: mutableUiState.value.query).trim()
        if (targetQuery.isBlank() || mutableUiState.value.isSearching) return
        mutableUiState.value = mutableUiState.value.copy(
            query = targetQuery,
            isSearching = true,
            catalogError = null,
        )
        viewModelScope.launch {
            try {
                val result = repository.library.searchBooks(targetQuery, page.coerceAtLeast(1))
                mutableUiState.value = mutableUiState.value.copy(
                    submittedQuery = targetQuery,
                    hasSearched = true,
                    books = result.books,
                    currentPage = result.page,
                    totalResults = result.total,
                    isSearching = false,
                    catalogError = null,
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                mutableUiState.value = mutableUiState.value.copy(
                    hasSearched = true,
                    isSearching = false,
                    catalogError = error.userMessage { readAppString(Res.string.msg_impossibile_cercare_in_biblioteca) },
                )
            }
        }
    }

    fun refreshMovements() {
        mutableUiState.value = mutableUiState.value.copy(
            movementsLoadState = FeatureLoadState.Loading,
            movementsError = null,
        )
        viewModelScope.launch {
            try {
                val list = repository.library.loadMovements(forceRefresh = true)
                mutableUiState.value = mutableUiState.value.copy(
                    movementsLoadState = if (list.isEmpty()) FeatureLoadState.Empty else FeatureLoadState.Content,
                    movements = list,
                    movementsError = null,
                    hasLoadedMovements = true,
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                mutableUiState.value = mutableUiState.value.copy(
                    movementsLoadState = FeatureLoadState.Error,
                    movementsError = error.userMessage { readAppString(Res.string.msg_impossibile_caricare_i_prestiti) },
                    hasLoadedMovements = true,
                )
            }
        }
    }

    fun requestCancelMovement(item: LibraryMovementItem) {
        if (!item.canCancel || item.actionToken == null) return
        mutableUiState.value = mutableUiState.value.copy(pendingCancelMovement = item)
    }

    fun dismissCancelMovement() {
        mutableUiState.value = mutableUiState.value.copy(pendingCancelMovement = null)
    }

    fun confirmCancelMovement() {
        val target = mutableUiState.value.pendingCancelMovement ?: return
        val token = target.actionToken ?: return
        mutableUiState.value = mutableUiState.value.copy(
            pendingCancelMovement = null,
            busyMovementToken = token,
        )
        viewModelScope.launch {
            try {
                repository.library.cancelMovement(token)
                mutableUiState.value = mutableUiState.value.copy(busyMovementToken = null)
                refreshMovements()
                toastSink.success(readAppString(Res.string.ui_library_movement_canceled))
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                mutableUiState.value = mutableUiState.value.copy(busyMovementToken = null)
                toastSink.error(error.userMessage { readAppString(Res.string.msg_impossibile_annullare_la_richiesta_biblioteca) })
            }
        }
    }

    fun renewMovement(item: LibraryMovementItem) {
        val token = item.actionToken ?: return
        if (!item.canRenew || mutableUiState.value.busyMovementToken != null) return
        mutableUiState.value = mutableUiState.value.copy(busyMovementToken = token)
        viewModelScope.launch {
            try {
                val feedback = repository.library.renewMovement(token)
                mutableUiState.value = mutableUiState.value.copy(busyMovementToken = null)
                toastSink.success(feedback.ifBlank { readAppString(Res.string.ui_library_loan_renewed) })
                refreshMovements()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                mutableUiState.value = mutableUiState.value.copy(busyMovementToken = null)
                toastSink.error(error.userMessage { readAppString(Res.string.msg_impossibile_prorogare_il_prestito) })
            }
        }
    }

    fun refreshSpaces() {
        mutableUiState.value = mutableUiState.value.copy(
            spacesLoadState = FeatureLoadState.Loading,
            spacesError = null,
        )
        viewModelScope.launch {
            try {
                val overview = repository.library.loadBookingOverview()
                mutableUiState.value = mutableUiState.value.copy(
                    spacesLoadState = if (overview.bookings.isEmpty()) FeatureLoadState.Empty else FeatureLoadState.Content,
                    spaceBookings = overview.bookings,
                    spaceTypes = overview.types,
                    spacesError = null,
                    hasLoadedSpaces = true,
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                mutableUiState.value = mutableUiState.value.copy(
                    spacesLoadState = FeatureLoadState.Error,
                    spacesError = error.userMessage { readAppString(Res.string.msg_impossibile_caricare_le_prenotazioni_biblioteca) },
                    hasLoadedSpaces = true,
                )
            }
        }
    }

    fun requestCancelBooking(booking: LibrarySpaceBooking) {
        if (!booking.canCancel || booking.bookingToken == null) return
        mutableUiState.value = mutableUiState.value.copy(pendingCancelBooking = booking)
    }

    fun dismissCancelBooking() {
        mutableUiState.value = mutableUiState.value.copy(pendingCancelBooking = null)
    }

    fun confirmCancelBooking() {
        val target = mutableUiState.value.pendingCancelBooking ?: return
        val token = target.bookingToken ?: return
        mutableUiState.value = mutableUiState.value.copy(
            pendingCancelBooking = null,
            busyBookingToken = token,
        )
        viewModelScope.launch {
            try {
                repository.library.cancelSpaceBooking(token)
                mutableUiState.value = mutableUiState.value.copy(busyBookingToken = null)
                toastSink.success(readAppString(Res.string.ui_library_space_booking_canceled))
                refreshSpaces()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                mutableUiState.value = mutableUiState.value.copy(busyBookingToken = null)
                toastSink.error(error.userMessage { readAppString(Res.string.msg_impossibile_annullare_prenotazione_spazio) })
            }
        }
    }
}
