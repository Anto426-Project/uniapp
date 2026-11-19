package com.anto426.uniapp.library.data

import com.anto426.uniapp.library.model.LibraryBookCopyAction
import com.anto426.uniapp.library.model.LibraryBookDetail
import com.anto426.uniapp.library.model.LibraryBookLoanDraft
import com.anto426.uniapp.library.model.LibraryBookingOption
import com.anto426.uniapp.library.model.LibraryBookingOverview
import com.anto426.uniapp.library.model.LibraryCatalogPage
import com.anto426.uniapp.library.model.LibraryLoanSubmissionResult
import com.anto426.uniapp.library.model.LibraryMovementItem
import com.anto426.uniapp.library.model.LibrarySpaceBooking
import com.anto426.uniapp.library.model.LibrarySpaceBookingOutcome
import com.anto426.uniapp.library.model.LibrarySpaceCalendar
import com.anto426.uniapp.library.model.LibrarySpaceSelection
import com.anto426.uniapp.library.model.LibraryTextParser
import com.anto426.uniapp.session.SessionManager
import com.anto426.unisdk.errors.UniSdkErrorCode
import com.anto426.unisdk.errors.UniSdkException
import com.anto426.unisdk.library.LibraryBook
import com.anto426.unisdk.library.LibraryBookAction
import com.anto426.unisdk.library.LibraryBookMovement
import com.anto426.unisdk.library.LibraryBookRequestDraft
import com.anto426.unisdk.library.LibraryBookingContext
import com.anto426.unisdk.library.LibraryCalendar
import com.anto426.unisdk.library.LibraryResourceBooking
import com.anto426.unisdk.library.LibraryService
import com.anto426.unisdk.library.LibrarySession
import com.anto426.unisdk.library.LibrarySlot
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.LocalDate

/**
 * Bridges active user/guest Sebina library sessions to app-layer models and opaque handles.
 * Keeps SDK session-owned instances off the UI/navigation layer and rejects stale handles.
 */
internal class PortalLibraryGateway(
    private val sessions: SessionManager,
    private val ownerProvider: () -> Pair<String, String?>,
) {
    private data class Owned<T>(
        val session: LibrarySession,
        val value: T,
    )

    private val handleLock = Mutex()
    private var nextToken = 0L
    private val bookActions = mutableMapOf<String, Owned<LibraryBookAction>>()
    private val bookDrafts = mutableMapOf<String, Owned<LibraryBookRequestDraft>>()
    private val movements = mutableMapOf<String, Owned<LibraryBookMovement>>()
    private val bookingContexts = mutableMapOf<String, Owned<LibraryBookingContext>>()
    private val spaceBookings = mutableMapOf<String, Owned<LibraryResourceBooking>>()
    private val calendars = mutableMapOf<String, Owned<Pair<LibraryCalendar, Map<String, LibrarySlot>>>>()

    private fun issueToken(prefix: String): String {
        nextToken += 1
        return "$prefix#$nextToken"
    }

    private suspend fun <T> withAuthenticated(
        retryOnExpiry: Boolean = true,
        block: suspend LibraryService.(LibrarySession) -> T,
    ): T {
        val (accountId, profileId) = ownerProvider()
        return sessions.withAuthenticatedLibrarySession(accountId, profileId, retryOnExpiry, block)
    }

    suspend fun searchBooks(query: String, page: Int): LibraryCatalogPage {
        val (accountId, profileId) = ownerProvider()
        val authSession = sessions.currentAuthenticatedLibrarySessionOrNull(accountId, profileId)
        val sdkPage = if (authSession != null) {
            withAuthenticated(retryOnExpiry = true) { session -> searchBooks(session, query, page) }
        } else {
            sessions.withLibraryGuestSession { session -> searchBooks(session, query, page) }
        }
        return LibraryCatalogPage(
            query = query,
            books = sdkPage.books.map { book ->
                LibraryTextParser.parseCatalogBook(book.id, book.title, book.description)
            },
            page = sdkPage.page,
            total = sdkPage.total,
        )
    }

    suspend fun loadBook(id: String): LibraryBookDetail {
        val (accountId, profileId) = ownerProvider()
        val existingAuth = sessions.currentAuthenticatedLibrarySessionOrNull(accountId, profileId)
        var capturedSession: LibrarySession? = null
        val sdkBook: LibraryBook = if (existingAuth != null) {
            withAuthenticated(retryOnExpiry = true) { session ->
                capturedSession = session
                loadBook(session, id)
            }
        } else {
            sessions.withLibraryGuestSession { session ->
                capturedSession = session
                loadBook(session, id)
            }
        }
        return mapAndRegisterBook(sdkBook, capturedSession!!)
    }

    suspend fun prepareBookRequest(bookId: String, actionId: String): LibraryBookLoanDraft {
        val existing = handleLock.withLock { bookActions[actionId]?.value }
            ?: throw UniSdkException(UniSdkErrorCode.LIBRARY_ACTION_INVALID)
        var createdDraft: LibraryBookRequestDraft? = null
        var draftSession: LibrarySession? = null
        withAuthenticated(retryOnExpiry = true) { session ->
            val currentStored = handleLock.withLock { bookActions[actionId] }
            val actionForSession = if (currentStored != null && currentStored.session === session) {
                currentStored.value
            } else {
                val reloaded = loadBook(session, bookId)
                mapAndRegisterBook(reloaded, session)
                reloaded.actions.firstOrNull {
                    it.libraryCode == existing.libraryCode &&
                        it.movementType == existing.movementType &&
                        it.label == existing.label
                } ?: reloaded.actions.firstOrNull {
                    it.libraryCode == existing.libraryCode && it.movementType == existing.movementType
                } ?: throw UniSdkException(UniSdkErrorCode.LIBRARY_ACTION_INVALID)
            }
            createdDraft = prepareBookRequest(session, actionForSession)
            draftSession = session
        }
        val token = handleLock.withLock {
            val id = issueToken("draft")
            bookDrafts[id] = Owned(draftSession!!, createdDraft!!)
            id
        }
        return LibraryBookLoanDraft(draftId = token, description = createdDraft!!.description)
    }

    suspend fun confirmBookRequest(draftId: String): LibraryLoanSubmissionResult {
        val stored = handleLock.withLock { bookDrafts.remove(draftId) }
            ?: throw UniSdkException(UniSdkErrorCode.LIBRARY_ACTION_INVALID)
        val submission = withAuthenticated(retryOnExpiry = false) { session ->
            if (stored.session !== session) throw UniSdkException(UniSdkErrorCode.LIBRARY_SESSION_EXPIRED)
            confirmBookRequest(session, stored.value, note = "")
        }
        return LibraryLoanSubmissionResult(
            description = submission.description,
            requiresVerification = submission.requiresVerification,
        )
    }

    suspend fun loadMovements(): List<LibraryMovementItem> {
        var owningSession: LibrarySession? = null
        val list = withAuthenticated(retryOnExpiry = true) { session ->
            owningSession = session
            loadBookMovements(session)
        }
        return handleLock.withLock {
            movements.clear()
            list.map { item ->
                val token = if (item.canCancel || item.canRenew) {
                    issueToken("mov").also { movements[it] = Owned(owningSession!!, item) }
                } else {
                    null
                }
                LibraryTextParser.parseMovement(
                    id = item.id,
                    rawDescription = item.description,
                    canCancel = item.canCancel,
                    canRenew = item.canRenew,
                    actionToken = token,
                )
            }
        }
    }

    suspend fun cancelMovement(movementToken: String) {
        val stored = handleLock.withLock { movements[movementToken] }
            ?: throw UniSdkException(UniSdkErrorCode.LIBRARY_ACTION_INVALID)
        withAuthenticated(retryOnExpiry = false) { session ->
            if (stored.session !== session) throw UniSdkException(UniSdkErrorCode.LIBRARY_SESSION_EXPIRED)
            cancelBookRequest(session, stored.value)
        }
        handleLock.withLock { movements.remove(movementToken) }
    }

    suspend fun renewMovement(movementToken: String): String {
        val stored = handleLock.withLock { movements[movementToken] }
            ?: throw UniSdkException(UniSdkErrorCode.LIBRARY_ACTION_INVALID)
        return withAuthenticated(retryOnExpiry = false) { session ->
            if (stored.session !== session) throw UniSdkException(UniSdkErrorCode.LIBRARY_SESSION_EXPIRED)
            renewBookLoan(session, stored.value)
        }
    }

    suspend fun loadBookingOverview(): LibraryBookingOverview {
        var owningSession: LibrarySession? = null
        val ctx = withAuthenticated(retryOnExpiry = true) { session ->
            owningSession = session
            loadBookingContext(session)
        }
        val contextId = handleLock.withLock {
            bookingContexts.clear()
            spaceBookings.clear()
            val id = issueToken("ctx")
            bookingContexts[id] = Owned(owningSession!!, ctx)
            id
        }
        val mappedBookings = handleLock.withLock {
            ctx.bookings.map { booking ->
                val token = if (booking.canCancel) {
                    issueToken("bkg").also { spaceBookings[it] = Owned(owningSession!!, booking) }
                } else {
                    null
                }
                LibraryTextParser.parseSpaceBooking(
                    id = booking.id,
                    rawDescription = booking.description,
                    canCancel = booking.canCancel,
                    bookingToken = token,
                )
            }
        }
        return LibraryBookingOverview(
            contextId = contextId,
            types = ctx.types.map { LibraryBookingOption(it.id, it.label) },
            bookings = mappedBookings,
        )
    }

    suspend fun loadResourceSelection(contextId: String, typeId: String): LibrarySpaceSelection {
        val stored = handleLock.withLock { bookingContexts[contextId] }
            ?: throw UniSdkException(UniSdkErrorCode.LIBRARY_SESSION_EXPIRED)
        val selection = withAuthenticated(retryOnExpiry = false) { session ->
            if (stored.session !== session) throw UniSdkException(UniSdkErrorCode.LIBRARY_SESSION_EXPIRED)
            loadResourceSelection(session, stored.value, typeId)
        }
        return LibrarySpaceSelection(
            owners = selection.owners.map { LibraryBookingOption(it.id, it.label) },
            resources = selection.resources.map { LibraryBookingOption(it.id, it.label) },
        )
    }

    suspend fun loadResourcesForOwner(ownerId: String): List<LibraryBookingOption> {
        val list = withAuthenticated(retryOnExpiry = false) { session ->
            loadResourcesForOwner(session, ownerId)
        }
        return list.map { LibraryBookingOption(it.id, it.label) }
    }

    suspend fun loadCalendar(
        contextId: String,
        resourceId: String,
        fromDate: LocalDate?,
        toDate: LocalDate?,
    ): LibrarySpaceCalendar {
        val stored = handleLock.withLock { bookingContexts[contextId] }
            ?: throw UniSdkException(UniSdkErrorCode.LIBRARY_SESSION_EXPIRED)
        var owningSession: LibrarySession? = null
        val calendar = withAuthenticated(retryOnExpiry = false) { session ->
            if (stored.session !== session) throw UniSdkException(UniSdkErrorCode.LIBRARY_SESSION_EXPIRED)
            owningSession = session
            loadCalendar(session, stored.value, resourceId, fromDate, toDate)
        }
        return registerCalendar(owningSession!!, calendar, page = 1)
    }

    suspend fun loadCalendarPage(calendarId: String, page: Int): LibrarySpaceCalendar {
        val stored = handleLock.withLock { calendars[calendarId] }
            ?: throw UniSdkException(UniSdkErrorCode.LIBRARY_ACTION_INVALID)
        var owningSession: LibrarySession? = null
        val nextCalendar = withAuthenticated(retryOnExpiry = false) { session ->
            if (stored.session !== session) throw UniSdkException(UniSdkErrorCode.LIBRARY_SESSION_EXPIRED)
            owningSession = session
            loadCalendarPage(session, stored.value.first, page)
        }
        return registerCalendar(owningSession!!, nextCalendar, page = page)
    }

    suspend fun bookSpace(
        calendarId: String,
        slotIds: List<String>,
        note: String,
        deviceAddresses: Map<String, String>,
    ): LibrarySpaceBookingOutcome {
        val stored = handleLock.withLock { calendars[calendarId] }
            ?: throw UniSdkException(UniSdkErrorCode.LIBRARY_ACTION_INVALID)
        val (sdkCalendar, slotMap) = stored.value
        val sdkSlots = slotIds.map { id ->
            slotMap[id] ?: throw UniSdkException(UniSdkErrorCode.LIBRARY_ACTION_INVALID)
        }
        val sdkDeviceMap = buildMap<LibrarySlot, String> {
            deviceAddresses.forEach { (slotId, addr) ->
                slotMap[slotId]?.let { put(it, addr) }
            }
        }
        val result = withAuthenticated(retryOnExpiry = false) { session ->
            if (stored.session !== session) throw UniSdkException(UniSdkErrorCode.LIBRARY_SESSION_EXPIRED)
            bookResource(session, sdkCalendar, sdkSlots, note, sdkDeviceMap)
        }
        handleLock.withLock { calendars.remove(calendarId) }
        return LibrarySpaceBookingOutcome(confirmed = result.confirmed, serverCode = result.serverCode)
    }

    suspend fun cancelSpaceBooking(bookingToken: String) {
        val stored = handleLock.withLock { spaceBookings[bookingToken] }
            ?: throw UniSdkException(UniSdkErrorCode.LIBRARY_ACTION_INVALID)
        withAuthenticated(retryOnExpiry = false) { session ->
            if (stored.session !== session) throw UniSdkException(UniSdkErrorCode.LIBRARY_SESSION_EXPIRED)
            cancelResourceBooking(session, stored.value)
        }
        handleLock.withLock { spaceBookings.remove(bookingToken) }
    }

    private suspend fun mapAndRegisterBook(sdkBook: LibraryBook, session: LibrarySession): LibraryBookDetail {
        val mappedActions = handleLock.withLock {
            if (bookActions.size > 120) bookActions.clear()
            sdkBook.actions.map { action ->
                val token = issueToken("act")
                bookActions[token] = Owned(session, action)
                LibraryBookCopyAction(
                    actionId = token,
                    label = action.label.ifBlank { action.movementType },
                    libraryCode = action.libraryCode,
                    branchName = LibraryTextParser.branchDisplayName(action.libraryCode),
                    movementType = action.movementType,
                )
            }
        }
        return LibraryTextParser.parseBookDetail(
            id = sdkBook.id,
            title = sdkBook.title,
            rawDescription = sdkBook.description,
            actions = mappedActions,
        )
    }

    private suspend fun registerCalendar(
        session: LibrarySession,
        calendar: LibraryCalendar,
        page: Int,
    ): LibrarySpaceCalendar {
        val calId = issueToken("cal")
        val slotMap = linkedMapOf<String, LibrarySlot>()
        val mappedSlots = handleLock.withLock {
            calendars.clear()
            val mapped = calendar.slots.mapIndexed { index, slot ->
                val slotId = "${calId}_s$index"
                slotMap[slotId] = slot
                LibraryTextParser.parseSlot(
                    slotId = slotId,
                    label = slot.label,
                    rawDescription = slot.description,
                    requiresDeviceAddress = slot.requiresDeviceAddress,
                )
            }
            calendars[calId] = Owned(session, calendar to slotMap)
            mapped
        }
        return LibrarySpaceCalendar(
            calendarId = calId,
            resourceId = calendar.resourceId,
            page = page,
            description = calendar.description,
            slots = mappedSlots,
        )
    }
}
