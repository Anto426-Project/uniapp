package com.anto426.uniapp.library.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.uniapp.data.UniGateway
import com.anto426.uniapp.data.asUniRepository
import com.anto426.uniapp.feedback.runtime.AppToastSink
import com.anto426.uniapp.feedback.runtime.error
import com.anto426.uniapp.feedback.runtime.success
import com.anto426.uniapp.feedback.runtime.warning
import com.anto426.uniapp.data.contract.LibraryBookingOption
import com.anto426.uniapp.data.contract.LibraryBookingOverview
import com.anto426.uniapp.data.contract.LibrarySpaceCalendar
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.presentation.userMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.anto426.uniapp.localization.readAppString
import uniapp.composeapp.generated.resources.*

data class LibrarySpaceBookingUiState(
    val loadState: FeatureLoadState = FeatureLoadState.Loading,
    val overview: LibraryBookingOverview? = null,
    val selectedTypeId: String? = null,
    val owners: List<LibraryBookingOption> = emptyList(),
    val selectedOwnerId: String? = null,
    val resources: List<LibraryBookingOption> = emptyList(),
    val selectedResourceId: String? = null,
    val isLoadingSelection: Boolean = false,
    val isLoadingCalendar: Boolean = false,
    val calendar: LibrarySpaceCalendar? = null,
    val selectedSlotIds: Set<String> = emptySet(),
    val note: String = "",
    val deviceAddresses: Map<String, String> = emptyMap(),
    val isSubmitting: Boolean = false,
    val bookingCompleted: Boolean = false,
    val errorMessage: String? = null,
)

class LibrarySpaceBookingViewModel(
    private val dataSource: UniGateway,
    private val toastSink: AppToastSink = AppToastSink.None,
) : ViewModel() {
    private val repository = dataSource.asUniRepository(viewModelScope)
    private val mutableUiState = MutableStateFlow(LibrarySpaceBookingUiState())
    val uiState: StateFlow<LibrarySpaceBookingUiState> = mutableUiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        mutableUiState.value = LibrarySpaceBookingUiState(loadState = FeatureLoadState.Loading)
        viewModelScope.launch {
            try {
                val overview = repository.library.loadBookingOverview()
                val firstType = overview.types.firstOrNull()?.id
                mutableUiState.value = LibrarySpaceBookingUiState(
                    loadState = if (overview.types.isEmpty()) FeatureLoadState.Empty else FeatureLoadState.Content,
                    overview = overview,
                    selectedTypeId = firstType,
                )
                if (firstType != null) {
                    loadSelectionForType(overview.contextId, firstType)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                mutableUiState.value = LibrarySpaceBookingUiState(
                    loadState = FeatureLoadState.Error,
                    errorMessage = error.userMessage { readAppString(Res.string.msg_impossibile_caricare_le_prenotazioni_biblioteca) },
                )
            }
        }
    }

    fun selectType(typeId: String) {
        val overview = mutableUiState.value.overview ?: return
        if (mutableUiState.value.selectedTypeId == typeId || mutableUiState.value.isLoadingSelection) return
        mutableUiState.value = mutableUiState.value.copy(
            selectedTypeId = typeId,
            owners = emptyList(),
            selectedOwnerId = null,
            resources = emptyList(),
            selectedResourceId = null,
            calendar = null,
            selectedSlotIds = emptySet(),
            deviceAddresses = emptyMap(),
        )
        viewModelScope.launch {
            loadSelectionForType(overview.contextId, typeId)
        }
    }

    fun selectOwner(ownerId: String) {
        val overview = mutableUiState.value.overview ?: return
        if (mutableUiState.value.selectedOwnerId == ownerId || mutableUiState.value.isLoadingSelection) return
        mutableUiState.value = mutableUiState.value.copy(
            selectedOwnerId = ownerId,
            resources = emptyList(),
            selectedResourceId = null,
            calendar = null,
            selectedSlotIds = emptySet(),
            deviceAddresses = emptyMap(),
            isLoadingSelection = true,
        )
        viewModelScope.launch {
            try {
                val resources = repository.library.loadResourcesForOwner(ownerId)
                val firstResource = resources.firstOrNull()?.id
                mutableUiState.value = mutableUiState.value.copy(
                    resources = resources,
                    selectedResourceId = firstResource,
                    isLoadingSelection = false,
                )
                if (firstResource != null) {
                    loadCalendarForResource(overview.contextId, firstResource)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                mutableUiState.value = mutableUiState.value.copy(isLoadingSelection = false)
                toastSink.error(error.userMessage { readAppString(Res.string.msg_impossibile_caricare_spazi_biblioteca) })
            }
        }
    }

    fun selectResource(resourceId: String) {
        val overview = mutableUiState.value.overview ?: return
        if (mutableUiState.value.selectedResourceId == resourceId || mutableUiState.value.isLoadingCalendar) return
        mutableUiState.value = mutableUiState.value.copy(
            selectedResourceId = resourceId,
            calendar = null,
            selectedSlotIds = emptySet(),
            deviceAddresses = emptyMap(),
        )
        viewModelScope.launch {
            loadCalendarForResource(overview.contextId, resourceId)
        }
    }

    fun loadCalendarPage(page: Int) {
        val currentCalendar = mutableUiState.value.calendar ?: return
        if (mutableUiState.value.isLoadingCalendar || page < 1) return
        mutableUiState.value = mutableUiState.value.copy(
            isLoadingCalendar = true,
            selectedSlotIds = emptySet(),
            deviceAddresses = emptyMap(),
        )
        viewModelScope.launch {
            try {
                val next = repository.library.loadCalendarPage(currentCalendar.calendarId, page)
                mutableUiState.value = mutableUiState.value.copy(
                    isLoadingCalendar = false,
                    calendar = next,
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                mutableUiState.value = mutableUiState.value.copy(isLoadingCalendar = false)
                toastSink.error(error.userMessage { readAppString(Res.string.msg_impossibile_caricare_calendario_biblioteca) })
            }
        }
    }

    fun toggleSlot(slotId: String) {
        val current = mutableUiState.value.selectedSlotIds
        val updated = if (slotId in current) current - slotId else current + slotId
        mutableUiState.value = mutableUiState.value.copy(selectedSlotIds = updated)
    }

    fun updateDeviceAddress(slotId: String, value: String) {
        mutableUiState.value = mutableUiState.value.copy(
            deviceAddresses = mutableUiState.value.deviceAddresses + (slotId to value),
        )
    }

    fun updateNote(value: String) {
        mutableUiState.value = mutableUiState.value.copy(note = value)
    }

    fun book() {
        val state = mutableUiState.value
        val overview = state.overview ?: return
        val calendar = state.calendar ?: return
        val selectedSlots = calendar.slots.filter { it.slotId in state.selectedSlotIds }
        if (selectedSlots.isEmpty() || state.isSubmitting) return

        viewModelScope.launch {
            val missingAddress = selectedSlots.any {
                it.requiresDeviceAddress && state.deviceAddresses[it.slotId].isNullOrBlank()
            }
            if (missingAddress) {
                toastSink.error(readAppString(Res.string.ui_library_device_address_required))
                return@launch
            }
            mutableUiState.value = mutableUiState.value.copy(isSubmitting = true)
            try {
                val addresses = selectedSlots
                    .filter { it.requiresDeviceAddress }
                    .associate { it.slotId to state.deviceAddresses[it.slotId].orEmpty().trim() }
                val outcome = repository.library.bookSpace(
                    calendarId = calendar.calendarId,
                    slotIds = selectedSlots.map { it.slotId },
                    note = state.note.trim(),
                    deviceAddresses = addresses,
                )
                mutableUiState.value = mutableUiState.value.copy(isSubmitting = false)
                if (outcome.confirmed) {
                    mutableUiState.value = mutableUiState.value.copy(bookingCompleted = true)
                    toastSink.success(readAppString(Res.string.ui_library_booking_confirmed))
                } else {
                    toastSink.warning(
                        readAppString(Res.string.ui_library_booking_rejected_code, outcome.serverCode),
                    )
                    // SDK requires reloading calendar slots after a rejected booking attempt.
                    loadCalendarForResource(overview.contextId, calendar.resourceId)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                mutableUiState.value = mutableUiState.value.copy(isSubmitting = false)
                toastSink.error(error.userMessage { readAppString(Res.string.msg_impossibile_prenotare_spazio) })
                loadCalendarForResource(overview.contextId, calendar.resourceId)
            }
        }
    }

    private suspend fun loadSelectionForType(contextId: String, typeId: String) {
        mutableUiState.value = mutableUiState.value.copy(isLoadingSelection = true)
        try {
            val selection = repository.library.loadResourceSelection(contextId, typeId)
            val firstOwner = selection.owners.firstOrNull()?.id
            val firstResource = selection.resources.firstOrNull()?.id
            mutableUiState.value = mutableUiState.value.copy(
                owners = selection.owners,
                selectedOwnerId = firstOwner,
                resources = selection.resources,
                selectedResourceId = firstResource,
                isLoadingSelection = false,
            )
            if (firstResource != null) {
                loadCalendarForResource(contextId, firstResource)
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            mutableUiState.value = mutableUiState.value.copy(isLoadingSelection = false)
            toastSink.error(error.userMessage { readAppString(Res.string.msg_impossibile_caricare_spazi_biblioteca) })
        }
    }

    private suspend fun loadCalendarForResource(contextId: String, resourceId: String) {
        mutableUiState.value = mutableUiState.value.copy(
            isLoadingCalendar = true,
            selectedSlotIds = emptySet(),
            deviceAddresses = emptyMap(),
        )
        try {
            val calendar = repository.library.loadCalendar(contextId, resourceId, null, null)
            mutableUiState.value = mutableUiState.value.copy(
                isLoadingCalendar = false,
                calendar = calendar,
            )
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            mutableUiState.value = mutableUiState.value.copy(isLoadingCalendar = false)
            toastSink.error(error.userMessage { readAppString(Res.string.msg_impossibile_caricare_calendario_biblioteca) })
        }
    }
}
