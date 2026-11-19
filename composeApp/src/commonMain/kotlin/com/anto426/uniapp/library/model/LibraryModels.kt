package com.anto426.uniapp.library.model

data class LibraryCatalogBook(
    val id: String,
    val title: String,
    val subtitle: String,
    val metadata: String = "",
    val branches: List<String> = emptyList(),
)

data class LibraryCatalogPage(
    val query: String,
    val books: List<LibraryCatalogBook>,
    val page: Int,
    val total: Int,
) {
    val hasNextPage: Boolean get() = page * PAGE_SIZE < total

    companion object {
        const val PAGE_SIZE = 10
    }
}

data class LibraryBookCopyAction(
    val actionId: String,
    val label: String,
    val libraryCode: String,
    val branchName: String,
    val movementType: String,
)

data class LibraryBookDetail(
    val id: String,
    val title: String,
    val author: String? = null,
    val year: String? = null,
    val availabilitySummary: String? = null,
    val isAvailable: Boolean = false,
    val branches: List<String> = emptyList(),
    val inventory: String? = null,
    val shelfmark: String? = null,
    val notes: String? = null,
    val description: String = "",
    val actions: List<LibraryBookCopyAction> = emptyList(),
)

data class LibraryBookLoanDraft(
    val draftId: String,
    val description: String,
)

data class LibraryLoanSubmissionResult(
    val description: String,
    val requiresVerification: Boolean = true,
)

data class LibraryMovementItem(
    val id: String? = null,
    val title: String,
    val detail: String,
    val branchName: String? = null,
    val statusBadge: String? = null,
    val dueDateLabel: String? = null,
    val canCancel: Boolean = false,
    val canRenew: Boolean = false,
    val actionToken: String? = null,
)

data class LibraryBookingOption(
    val id: String,
    val label: String,
)

data class LibrarySpaceBooking(
    val id: String? = null,
    val title: String,
    val description: String,
    val dateLabel: String? = null,
    val timeLabel: String? = null,
    val canCancel: Boolean = false,
    val bookingToken: String? = null,
)

data class LibraryBookingOverview(
    val contextId: String,
    val types: List<LibraryBookingOption>,
    val bookings: List<LibrarySpaceBooking>,
)

data class LibrarySpaceSelection(
    val owners: List<LibraryBookingOption>,
    val resources: List<LibraryBookingOption>,
)

data class LibrarySpaceSlot(
    val slotId: String,
    val label: String,
    val timeLabel: String,
    val description: String,
    val dateLabel: String? = null,
    val seatsLabel: String? = null,
    val requiresDeviceAddress: Boolean = false,
)

data class LibrarySpaceCalendar(
    val calendarId: String,
    val resourceId: String,
    val page: Int = 1,
    val description: String = "",
    val slots: List<LibrarySpaceSlot> = emptyList(),
)

data class LibrarySpaceBookingOutcome(
    val confirmed: Boolean,
    val serverCode: Int,
)
