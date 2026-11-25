package com.anto426.uniapp.data

import com.anto426.securestorage.SecureStorageCorruptedException
import com.anto426.securestorage.SecureStorageDecodingException
import com.anto426.uniapp.account.storage.UniAccountStore
import com.anto426.uniapp.data.contract.LibraryBookCopyAction
import com.anto426.uniapp.data.contract.LibraryBookDetail
import com.anto426.uniapp.data.contract.LibraryBookLoanDraft
import com.anto426.uniapp.data.contract.LibraryBookingOption
import com.anto426.uniapp.data.contract.LibraryBookingOverview
import com.anto426.uniapp.data.contract.LibraryCatalogPage
import com.anto426.uniapp.data.contract.LibraryLoanSubmissionResult
import com.anto426.uniapp.data.contract.LibraryMovementItem
import com.anto426.uniapp.data.contract.LibrarySpaceBookingOutcome
import com.anto426.uniapp.data.contract.LibrarySpaceCalendar
import com.anto426.uniapp.data.contract.LibrarySpaceSelection
import com.anto426.uniapp.data.contract.LibraryTextParser
import com.anto426.uniapp.session.model.AppSessionState
import com.anto426.unisdk.backend.UniBackendService
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
import com.anto426.unisdk.model.AttendanceRecord
import com.anto426.unisdk.model.Career
import com.anto426.unisdk.model.ConnectedDevice
import com.anto426.unisdk.model.CourseSyllabus
import com.anto426.unisdk.model.ExamRound
import com.anto426.unisdk.model.ProfessorDashboard
import com.anto426.unisdk.model.StudentDetails
import com.anto426.unisdk.model.StudyPlan
import com.anto426.unisdk.model.SurveyCourse
import com.anto426.unisdk.model.SurveyForm
import com.anto426.unisdk.model.SurveySaveRequest
import com.anto426.unisdk.model.Taxes
import com.anto426.unisdk.model.UniversityContact
import com.anto426.unisdk.model.UniversityNews
import com.anto426.unisdk.platform.currentEpochMillis
import com.anto426.unisdk.session.UniCredentials
import com.anto426.unisdk.session.UniSession
import com.anto426.unisdk.transport.TransportActionResult
import com.anto426.unisdk.transport.TransportBookingRequest
import com.anto426.unisdk.transport.TransportData
import com.anto426.unisdk.transport.TransportService
import com.anto426.unisdk.transport.TransportSession
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.LocalDate
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

class PortalUniGateway(
    private val sessions: UniSessionController,
    private val accounts: UniAccountStore,
    private val fixedAccountId: String? = null,
    private val fixedProfileId: String? = null,
    private val nowMillis: () -> Long = ::currentEpochMillis,
    private val fallbackToStaleCache: Boolean = true,
) : UniGateway, UniPortraitSharer {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val requestLocksGuard = Mutex()
    private val requestLocks = mutableMapOf<String, Mutex>()
    private val savedAtGuard = Mutex()
    private val savedAtByKey = mutableMapOf<String, Long>()
    private val newsArchiveSyncedAtByKey = mutableMapOf<String, Long>()

    // Opaque token vault for Sebina library session objects
    private data class OwnedLibraryHandle<T>(
        val session: LibrarySession,
        val value: T,
    )
    private val libraryHandleLock = Mutex()
    private var nextLibraryToken = 0L
    private val bookActions = mutableMapOf<String, OwnedLibraryHandle<LibraryBookAction>>()
    private val bookDrafts = mutableMapOf<String, OwnedLibraryHandle<LibraryBookRequestDraft>>()
    private val movements = mutableMapOf<String, OwnedLibraryHandle<LibraryBookMovement>>()
    private val bookingContexts = mutableMapOf<String, OwnedLibraryHandle<LibraryBookingContext>>()
    private val spaceBookings = mutableMapOf<String, OwnedLibraryHandle<LibraryResourceBooking>>()
    private val calendars = mutableMapOf<String, OwnedLibraryHandle<Pair<LibraryCalendar, Map<String, LibrarySlot>>>>()

    private fun issueLibraryToken(prefix: String): String {
        nextLibraryToken += 1
        return "$prefix#$nextLibraryToken"
    }

    private fun ActiveAccountContext.profileScopedKey(key: String): String =
        profileCacheKey(sessionState.account.activeProfileId, key)

    private fun activeContext(): ActiveAccountContext {
        val sessionState = sessions.state.value as? AppSessionState.Authenticated
            ?: throw CancellationException("The data request no longer has an authenticated owner")
        val account = sessionState.account
        requireDataRequestOwner(account.accountId, account.activeProfileId, fixedAccountId, fixedProfileId)
        return ActiveAccountContext(account.accountId, sessionState)
    }

    private suspend fun <T> ActiveAccountContext.backend(
        block: suspend UniBackendService.(UniSession, UniCredentials) -> T,
    ): T = sessions.callAuthenticated(accountId, sessionState.account.activeProfileId, block)

    private suspend fun <T> ActiveAccountContext.transport(
        block: suspend TransportService.(TransportSession) -> T,
    ): T = sessions.withTransportSession(accountId, sessionState.account.activeProfileId, block)

    private suspend fun <T> withAuthenticatedLibrary(
        retryOnExpiry: Boolean = true,
        block: suspend LibraryService.(LibrarySession) -> T,
    ): T {
        val context = activeContext()
        return sessions.withAuthenticatedLibrarySession(
            context.accountId,
            context.sessionState.account.activeProfileId,
            retryOnExpiry,
            block,
        )
    }

    private fun ensureCurrent(context: ActiveAccountContext) {
        if (sessions.state.value !== context.sessionState) {
            throw CancellationException("The account or profile changed while loading data")
        }
    }

    internal suspend fun readCachedSavedAtMillis(request: UniDatasetKey<*>): Long? {
        val context = activeContext()
        if (request.id.startsWith("portrait/")) {
            return accounts.readProfileImage(context.accountId, request.id.removePrefix("portrait/"))
                ?.savedAtMillis
                ?.also { ensureCurrent(context) }
        }
        val key = request.cacheKey ?: return null
        val cacheAddress = "${context.accountId}|${context.profileScopedKey(key)}"
        val savedAt = savedAtGuard.withLock { savedAtByKey[cacheAddress] }
        ensureCurrent(context)
        return savedAt
    }

    @Suppress("UNCHECKED_CAST")
    internal suspend fun <T : Any> readCached(request: UniDatasetKey<T>): CachedValue<T>? {
        val context = activeContext()
        if (request.id.startsWith("portrait/")) {
            val cached = accounts.readProfileImage(context.accountId, request.id.removePrefix("portrait/"))
            ensureCurrent(context)
            return cached?.let { CachedValue(it.savedAtMillis, it.bytes as T) }
        }
        if (request.id.startsWith("ticket-image/")) {
            val bookingId = request.id.removePrefix("ticket-image/")
            if (bookingId.isEmpty() || bookingId.length > 256) return null
            val key = context.profileScopedKey(ticketImageCacheKey(bookingId))
            val bytes = try {
                accounts.readCachedData(context.accountId, key)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                null
            }
            ensureCurrent(context)
            return bytes?.let { CachedValue(nowMillis(), it as T) }
        }
        val key = request.cacheKey ?: return null
        val serializer = request.serializer ?: return null
        val value = try {
            readEntry(context.accountId, context.profileScopedKey(key), serializer)
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            null
        }
        ensureCurrent(context)
        return value
    }

    override suspend fun loadCareer(forceRefresh: Boolean): Career =
        streamCareer(forceRefresh).last()

    override fun streamCareer(forceRefresh: Boolean): Flow<Career> =
        cachedStream(
            key = "career-v2",
            policy = UniCachePolicies.Career,
            serializer = Career.serializer(),
            forceRefresh = forceRefresh,
        ) { context, _, emitValue ->
            context.backend { session, credentials ->
                observeCareer(session, credentials).collect { emitValue(it) }
            }
        }

    override suspend fun loadProfessorDashboard(forceRefresh: Boolean): ProfessorDashboard =
        streamProfessorDashboard(forceRefresh).last()

    override fun streamProfessorDashboard(forceRefresh: Boolean): Flow<ProfessorDashboard> =
        cachedStream(
            key = "professor-dashboard",
            policy = UniCachePolicies.ProfessorDashboard,
            serializer = ProfessorDashboard.serializer(),
            forceRefresh = forceRefresh,
            mergeIntermediate = { existing, incoming -> mergeProfessorDashboard(existing, incoming, isFinal = false) },
            mergeFinal = { existing, incoming -> mergeProfessorDashboard(existing, incoming, isFinal = true) },
        ) { context, _, emitValue ->
            context.backend { session, credentials ->
                observeProfessorDashboard(session, credentials).collect { emitValue(it) }
            }
        }

    override suspend fun loadStudyPlan(forceRefresh: Boolean): StudyPlan =
        cached("study-plan", UniCachePolicies.StudyPlan, StudyPlan.serializer(), forceRefresh) { context ->
            context.backend { session, credentials -> loadStudyPlan(session, credentials) }
        }

    override suspend fun loadExamRounds(forceRefresh: Boolean): List<ExamRound> =
        streamExamRounds(forceRefresh).last()

    override fun streamExamRounds(forceRefresh: Boolean): Flow<List<ExamRound>> =
        cachedStream(
            key = "exam-rounds",
            policy = UniCachePolicies.ExamRounds,
            serializer = ListSerializer(ExamRound.serializer()),
            forceRefresh = forceRefresh,
            mergeIntermediate = { existing, incoming -> mergeExamRoundsProgress(existing, incoming) },
            mergeFinal = { _, incoming -> incoming },
        ) { context, _, emitValue ->
            context.backend { session, credentials ->
                observeExamRounds(session, credentials).collect { emitValue(it) }
            }
        }

    override suspend fun loadCourseSyllabus(adsceId: String, forceRefresh: Boolean): CourseSyllabus =
        cached(
            datasetCacheKey("course-syllabus", adsceId),
            UniCachePolicies.CourseSyllabus,
            CourseSyllabus.serializer(),
            forceRefresh,
        ) { context -> context.backend { session, credentials -> loadCourseSyllabus(session, credentials, adsceId) } }

    override suspend fun bookExamRound(round: ExamRound): String {
        val context = activeContext()
        return context.backend { session, credentials -> bookExamRound(session, credentials, round) }.also {
            invalidate(context.accountId, context.profileScopedKey("exam-rounds"))
        }
    }

    override suspend fun cancelExamRound(round: ExamRound): String {
        val context = activeContext()
        return context.backend { session, credentials -> cancelExamRound(session, credentials, round) }.also {
            invalidate(context.accountId, context.profileScopedKey("exam-rounds"))
        }
    }

    override suspend fun loadTaxes(forceRefresh: Boolean): Taxes =
        cached("taxes", UniCachePolicies.Taxes, Taxes.serializer(), forceRefresh) { context ->
            context.backend { session, credentials -> loadTaxes(session, credentials) }
        }

    override suspend fun loadStudentDetails(forceRefresh: Boolean): StudentDetails =
        cached("student-details-v2", UniCachePolicies.StudentDetails, StudentDetails.serializer(), forceRefresh) { context ->
            context.backend { session, credentials -> loadStudentDetails(session, credentials) }
        }

    override suspend fun sharePortrait(source: String, bytes: ByteArray): ByteArray {
        val context = activeContext()
        return sessions.avatars.publish(context.accountId, source, bytes) { ensureCurrent(context) }
    }

    override suspend fun loadProfileImage(source: String, forceRefresh: Boolean): ByteArray =
        activeContext().let { context ->
            requestLock(context.accountId, "profile-image").withLock {
                ensureCurrent(context)
                val cached = accounts.readProfileImage(context.accountId, source)
                ensureCurrent(context)
                if (!forceRefresh && cached != null && nowMillis() - cached.savedAtMillis in 0 until UniCachePolicies.ProfileImage.maxAgeMillis) {
                    return@withLock cached.bytes
                }
                try {
                    val downloaded = context.backend { session, credentials -> loadProfileImage(session, credentials, source) }.also { bytes ->
                        ensureCurrent(context)
                        try {
                            accounts.writeProfileImage(context.accountId, source, nowMillis(), bytes)
                        } catch (error: CancellationException) {
                            throw error
                        } catch (_: Exception) {
                        }
                        ensureCurrent(context)
                    }
                    downloaded
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Throwable) {
                    ensureCurrent(context)
                    cached?.bytes ?: throw error
                }
            }
        }

    override suspend fun loadConnectedDevices(forceRefresh: Boolean): List<ConnectedDevice> =
        cached(
            "connected-devices",
            UniCachePolicies.ConnectedDevices,
            ListSerializer(ConnectedDevice.serializer()),
            forceRefresh,
        ) { context -> context.backend { session, credentials -> loadConnectedDevices(session, credentials) } }

    override suspend fun disconnectDevice(targetToken: String): String {
        val context = activeContext()
        return context.backend { session, credentials -> disconnectDevice(session, credentials, targetToken) }.also {
            invalidate(context.accountId, context.profileScopedKey("connected-devices"))
        }
    }

    override suspend fun disconnectAllOtherDevices(): String {
        val context = activeContext()
        return context.backend { session, credentials -> disconnectAllOtherDevices(session, credentials) }.also {
            invalidate(context.accountId, context.profileScopedKey("connected-devices"))
        }
    }

    override suspend fun loadAttendanceHistory(forceRefresh: Boolean): List<AttendanceRecord> =
        cached(
            "attendance",
            UniCachePolicies.Attendance,
            ListSerializer(AttendanceRecord.serializer()),
            forceRefresh,
            mergeWithCached = { existing, incoming -> mergeAttendanceHistory(existing.orEmpty(), incoming) },
        ) { context -> context.backend { session, credentials -> loadAttendanceHistory(session, credentials) } }

    override suspend fun registerAttendance(
        qrCode: String,
        deviceLatitude: Double?,
        deviceLongitude: Double?,
        deviceAccuracyMeters: Double?,
    ): String {
        val context = activeContext()
        return context.backend { session, credentials ->
            registerAttendance(
                session = session,
                credentials = credentials,
                qrCode = qrCode,
                deviceLatitude = deviceLatitude,
                deviceLongitude = deviceLongitude,
                deviceAccuracyMeters = deviceAccuracyMeters,
            )
        }.also {
            invalidate(context.accountId, context.profileScopedKey("attendance"))
        }
    }

    override suspend fun loadUniversityNews(forceRefresh: Boolean): List<UniversityNews> =
        streamUniversityNews(forceRefresh).last()

    override fun streamUniversityNews(forceRefresh: Boolean): Flow<List<UniversityNews>> =
        cachedStream(
            key = "university-news-v3",
            policy = UniCachePolicies.News,
            serializer = ListSerializer(UniversityNews.serializer()),
            forceRefresh = forceRefresh,
            mergeIntermediate = { existing, incoming -> mergeUniversityNews(existing.orEmpty(), incoming) },
            mergeFinal = { existing, incoming -> mergeUniversityNews(existing.orEmpty(), incoming) },
        ) { context, cached, emitValue ->
            val archiveKey = "${context.accountId}|${context.profileScopedKey("university-news-v3")}"
            val lastArchiveSync = savedAtGuard.withLock { newsArchiveSyncedAtByKey[archiveKey] }
            val hasCachedHistory = !cached.isNullOrEmpty()
            val needsArchives = forceRefresh ||
                !hasCachedHistory ||
                lastArchiveSync == null ||
                (nowMillis() - lastArchiveSync) !in 0 until NEWS_ARCHIVE_SYNC_INTERVAL_MILLIS
            context.backend { session, credentials ->
                observeUniversityNews(
                    session = session,
                    credentials = credentials,
                    includeArchives = needsArchives,
                ).collect { emitValue(it) }
            }
            if (needsArchives) {
                savedAtGuard.withLock { newsArchiveSyncedAtByKey[archiveKey] = nowMillis() }
            }
        }

    override suspend fun loadUniversityContacts(forceRefresh: Boolean): List<UniversityContact> =
        cached(
            "university-contacts-v2",
            UniCachePolicies.Contacts,
            ListSerializer(UniversityContact.serializer()),
            forceRefresh,
            mergeWithCached = { existing, incoming -> mergeUniversityContacts(existing.orEmpty(), incoming) },
        ) { context -> context.backend { session, credentials -> loadUniversityContacts(session, credentials) } }

    override suspend fun loadSurveyCourses(forceRefresh: Boolean): List<SurveyCourse> =
        cached(
            "survey-courses",
            UniCachePolicies.SurveyCourses,
            ListSerializer(SurveyCourse.serializer()),
            forceRefresh,
        ) { context -> context.backend { session, credentials -> loadSurveyCourses(session, credentials) } }

    override suspend fun loadSurveyFirstPage(courseId: String, tagList: String): SurveyForm =
        activeContext().backend { session, credentials -> loadSurveyFirstPage(session, credentials, courseId, tagList) }

    override suspend fun saveSurvey(courseId: String, request: SurveySaveRequest): String {
        val context = activeContext()
        return context.backend { session, credentials -> saveSurvey(session, credentials, courseId, request) }.also {
            invalidate(context.accountId, context.profileScopedKey("survey-courses"))
        }
    }

    override suspend fun loadSurveyCompilationStatus(adCod: String, forceRefresh: Boolean): Boolean =
        cached(
            datasetCacheKey("survey-status", adCod),
            UniCachePolicies.SurveyStatus,
            Boolean.serializer(),
            forceRefresh,
        ) { context -> context.backend { session, credentials -> loadSurveyCompilationStatus(session, credentials, adCod) } }

    override suspend fun loadTransportData(forceRefresh: Boolean): TransportData =
        streamTransportData(forceRefresh).last()

    override fun streamTransportData(forceRefresh: Boolean): Flow<TransportData> =
        cachedStream(
            key = "transport-data",
            policy = UniCachePolicies.Transport,
            serializer = TransportData.serializer(),
            forceRefresh = forceRefresh,
            requirePersistence = true,
            mergeIntermediate = { existing, incoming -> mergeTransportProgress(existing, incoming) },
            mergeFinal = { _, incoming -> incoming },
            onSaved = { context, previous, current ->
                val activeIds = current.bookings.mapTo(mutableSetOf()) { it.id }
                previous?.bookings.orEmpty().asSequence()
                    .map { it.id }
                    .filterNot(activeIds::contains)
                    .forEach { removedId ->
                        val key = context.profileScopedKey(ticketImageCacheKey(removedId))
                        requestLock(context.accountId, key).withLock {
                            ensureCurrent(context)
                            accounts.removeCachedData(context.accountId, key)
                        }
                    }
            },
        ) { context, _, emitValue ->
            context.transport { session ->
                observeTransportData(session).collect { emitValue(it) }
            }
        }

    override suspend fun loadTransportTicketImage(bookingId: String, source: String, forceRefresh: Boolean): ByteArray {
        val context = activeContext()
        val key = context.profileScopedKey(ticketImageCacheKey(bookingId))
        return requestLock(context.accountId, key).withLock {
            ensureCurrent(context)
            val original = try {
                accounts.readCachedData(context.accountId, key)
            } catch (_: SecureStorageCorruptedException) {
                runCatching { accounts.removeCachedData(context.accountId, key) }
                null
            } catch (_: SecureStorageDecodingException) {
                runCatching { accounts.removeCachedData(context.accountId, key) }
                null
            }
            ensureCurrent(context)
            if (!forceRefresh && original != null) return@withLock original
            try {
                val downloaded = context.transport { session -> loadTransportTicketImage(session, source) }
                ensureCurrent(context)
                accounts.writeCachedData(context.accountId, key, downloaded)
                ensureCurrent(context)
                downloaded
            } catch (error: CancellationException) { throw error }
            catch (error: Exception) {
                ensureCurrent(context)
                original ?: throw error
            }
        }
    }

    private fun ticketImageCacheKey(bookingId: String): String {
        require(bookingId.isNotEmpty() && bookingId.length <= 256)
        return "ticket-image-v1-" + bookingId.encodeToByteArray().joinToString("") { it.toUByte().toString(16).padStart(2, '0') }
    }

    override suspend fun bookTransport(request: TransportBookingRequest): TransportActionResult {
        val context = activeContext()
        return context.transport { session -> bookTransport(session, request) }.also {
            safelyMarkTransportSnapshotStale(context)
        }
    }

    override suspend fun deleteTransportBooking(bookingId: String): TransportActionResult {
        val context = activeContext()
        return context.transport { session -> deleteTransportBooking(session, bookingId) }.also {
            val imageKey = context.profileScopedKey(ticketImageCacheKey(bookingId))
            val imageRemoved = try {
                requestLock(context.accountId, imageKey).withLock { accounts.removeCachedData(context.accountId, imageKey) }
                true
            } catch (error: CancellationException) { throw error }
            catch (_: Exception) { false }
            safelyMarkTransportSnapshotStale(context, removedBookingId = bookingId.takeIf { imageRemoved })
        }
    }

    // ------------------------------------------------------------------------
    // Sebina Library Operations (Integrated Opaque Handle Vault)
    // ------------------------------------------------------------------------

    override suspend fun searchLibraryBooks(query: String, page: Int): LibraryCatalogPage {
        val context = activeContext()
        val authSession = sessions.currentAuthenticatedLibrarySessionOrNull(
            context.accountId,
            context.sessionState.account.activeProfileId,
        )
        val sdkPage = if (authSession != null) {
            withAuthenticatedLibrary(retryOnExpiry = true) { session -> searchBooks(session, query, page) }
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

    override suspend fun loadLibraryBook(id: String): LibraryBookDetail {
        val context = activeContext()
        val existingAuth = sessions.currentAuthenticatedLibrarySessionOrNull(
            context.accountId,
            context.sessionState.account.activeProfileId,
        )
        var capturedSession: LibrarySession? = null
        val sdkBook: LibraryBook = if (existingAuth != null) {
            withAuthenticatedLibrary(retryOnExpiry = true) { session ->
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

    override suspend fun prepareLibraryBookRequest(bookId: String, actionId: String): LibraryBookLoanDraft {
        val existing = libraryHandleLock.withLock { bookActions[actionId]?.value }
            ?: throw UniSdkException(UniSdkErrorCode.LIBRARY_ACTION_INVALID)
        var createdDraft: LibraryBookRequestDraft? = null
        var draftSession: LibrarySession? = null
        withAuthenticatedLibrary(retryOnExpiry = true) { session ->
            val currentStored = libraryHandleLock.withLock { bookActions[actionId] }
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
        val token = libraryHandleLock.withLock {
            val id = issueLibraryToken("draft")
            bookDrafts[id] = OwnedLibraryHandle(draftSession!!, createdDraft!!)
            id
        }
        return LibraryBookLoanDraft(draftId = token, description = createdDraft!!.description)
    }

    override suspend fun confirmLibraryBookRequest(draftId: String): LibraryLoanSubmissionResult {
        val stored = libraryHandleLock.withLock { bookDrafts.remove(draftId) }
            ?: throw UniSdkException(UniSdkErrorCode.LIBRARY_ACTION_INVALID)
        val submission = withAuthenticatedLibrary(retryOnExpiry = false) { session ->
            if (stored.session !== session) throw UniSdkException(UniSdkErrorCode.LIBRARY_SESSION_EXPIRED)
            confirmBookRequest(session, stored.value, note = "")
        }
        return LibraryLoanSubmissionResult(
            description = submission.description,
            requiresVerification = submission.requiresVerification,
        )
    }

    override suspend fun loadLibraryMovements(forceRefresh: Boolean): List<LibraryMovementItem> {
        var owningSession: LibrarySession? = null
        val list = withAuthenticatedLibrary(retryOnExpiry = true) { session ->
            owningSession = session
            loadBookMovements(session)
        }
        return libraryHandleLock.withLock {
            movements.clear()
            list.map { item ->
                val token = if (item.canCancel || item.canRenew) {
                    issueLibraryToken("mov").also { movements[it] = OwnedLibraryHandle(owningSession!!, item) }
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

    override suspend fun cancelLibraryMovement(movementToken: String) {
        val stored = libraryHandleLock.withLock { movements[movementToken] }
            ?: throw UniSdkException(UniSdkErrorCode.LIBRARY_ACTION_INVALID)
        withAuthenticatedLibrary(retryOnExpiry = false) { session ->
            if (stored.session !== session) throw UniSdkException(UniSdkErrorCode.LIBRARY_SESSION_EXPIRED)
            cancelBookRequest(session, stored.value)
        }
        libraryHandleLock.withLock { movements.remove(movementToken) }
    }

    override suspend fun renewLibraryMovement(movementToken: String): String {
        val stored = libraryHandleLock.withLock { movements[movementToken] }
            ?: throw UniSdkException(UniSdkErrorCode.LIBRARY_ACTION_INVALID)
        return withAuthenticatedLibrary(retryOnExpiry = false) { session ->
            if (stored.session !== session) throw UniSdkException(UniSdkErrorCode.LIBRARY_SESSION_EXPIRED)
            renewBookLoan(session, stored.value)
        }
    }

    override suspend fun loadLibraryBookingOverview(): LibraryBookingOverview {
        var owningSession: LibrarySession? = null
        val ctx = withAuthenticatedLibrary(retryOnExpiry = true) { session ->
            owningSession = session
            loadBookingContext(session)
        }
        val contextId = libraryHandleLock.withLock {
            bookingContexts.clear()
            spaceBookings.clear()
            val id = issueLibraryToken("ctx")
            bookingContexts[id] = OwnedLibraryHandle(owningSession!!, ctx)
            id
        }
        val mappedBookings = libraryHandleLock.withLock {
            ctx.bookings.map { booking ->
                val token = if (booking.canCancel) {
                    issueLibraryToken("bkg").also { spaceBookings[it] = OwnedLibraryHandle(owningSession!!, booking) }
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

    override suspend fun loadLibraryResourceSelection(contextId: String, typeId: String): LibrarySpaceSelection {
        val stored = libraryHandleLock.withLock { bookingContexts[contextId] }
            ?: throw UniSdkException(UniSdkErrorCode.LIBRARY_SESSION_EXPIRED)
        val selection = withAuthenticatedLibrary(retryOnExpiry = false) { session ->
            if (stored.session !== session) throw UniSdkException(UniSdkErrorCode.LIBRARY_SESSION_EXPIRED)
            loadResourceSelection(session, stored.value, typeId)
        }
        return LibrarySpaceSelection(
            owners = selection.owners.map { LibraryBookingOption(it.id, it.label) },
            resources = selection.resources.map { LibraryBookingOption(it.id, it.label) },
        )
    }

    override suspend fun loadLibraryResourcesForOwner(ownerId: String): List<LibraryBookingOption> {
        val list = withAuthenticatedLibrary(retryOnExpiry = false) { session ->
            loadResourcesForOwner(session, ownerId)
        }
        return list.map { LibraryBookingOption(it.id, it.label) }
    }

    override suspend fun loadLibraryCalendar(
        contextId: String,
        resourceId: String,
        fromDate: LocalDate?,
        toDate: LocalDate?,
    ): LibrarySpaceCalendar {
        val stored = libraryHandleLock.withLock { bookingContexts[contextId] }
            ?: throw UniSdkException(UniSdkErrorCode.LIBRARY_SESSION_EXPIRED)
        var owningSession: LibrarySession? = null
        val calendar = withAuthenticatedLibrary(retryOnExpiry = false) { session ->
            if (stored.session !== session) throw UniSdkException(UniSdkErrorCode.LIBRARY_SESSION_EXPIRED)
            owningSession = session
            loadCalendar(session, stored.value, resourceId, fromDate, toDate)
        }
        return registerCalendar(owningSession!!, calendar, page = 1)
    }

    override suspend fun loadLibraryCalendarPage(calendarId: String, page: Int): LibrarySpaceCalendar {
        val stored = libraryHandleLock.withLock { calendars[calendarId] }
            ?: throw UniSdkException(UniSdkErrorCode.LIBRARY_ACTION_INVALID)
        var owningSession: LibrarySession? = null
        val nextCalendar = withAuthenticatedLibrary(retryOnExpiry = false) { session ->
            if (stored.session !== session) throw UniSdkException(UniSdkErrorCode.LIBRARY_SESSION_EXPIRED)
            owningSession = session
            loadCalendarPage(session, stored.value.first, page)
        }
        return registerCalendar(owningSession!!, nextCalendar, page = page)
    }

    override suspend fun bookLibrarySpace(
        calendarId: String,
        slotIds: List<String>,
        note: String,
        deviceAddresses: Map<String, String>,
    ): LibrarySpaceBookingOutcome {
        val stored = libraryHandleLock.withLock { calendars[calendarId] }
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
        val result = withAuthenticatedLibrary(retryOnExpiry = false) { session ->
            if (stored.session !== session) throw UniSdkException(UniSdkErrorCode.LIBRARY_SESSION_EXPIRED)
            bookResource(session, sdkCalendar, sdkSlots, note, sdkDeviceMap)
        }
        libraryHandleLock.withLock { calendars.remove(calendarId) }
        return LibrarySpaceBookingOutcome(confirmed = result.confirmed, serverCode = result.serverCode)
    }

    override suspend fun cancelLibrarySpaceBooking(bookingToken: String) {
        val stored = libraryHandleLock.withLock { spaceBookings[bookingToken] }
            ?: throw UniSdkException(UniSdkErrorCode.LIBRARY_ACTION_INVALID)
        withAuthenticatedLibrary(retryOnExpiry = false) { session ->
            if (stored.session !== session) throw UniSdkException(UniSdkErrorCode.LIBRARY_SESSION_EXPIRED)
            cancelResourceBooking(session, stored.value)
        }
        libraryHandleLock.withLock { spaceBookings.remove(bookingToken) }
    }

    private suspend fun mapAndRegisterBook(sdkBook: LibraryBook, session: LibrarySession): LibraryBookDetail {
        val mappedActions = libraryHandleLock.withLock {
            if (bookActions.size > 120) bookActions.clear()
            sdkBook.actions.map { action ->
                val token = issueLibraryToken("act")
                bookActions[token] = OwnedLibraryHandle(session, action)
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
        val calId = issueLibraryToken("cal")
        val slotMap = linkedMapOf<String, LibrarySlot>()
        val mappedSlots = libraryHandleLock.withLock {
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
            calendars[calId] = OwnedLibraryHandle(session, calendar to slotMap)
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

    // ------------------------------------------------------------------------
    // Encrypted Disk Cache Primitives (Zeroing ByteArray Buffers)
    // ------------------------------------------------------------------------

    private suspend fun safelyMarkTransportSnapshotStale(context: ActiveAccountContext, removedBookingId: String? = null) {
        try { markTransportSnapshotStale(context, removedBookingId) }
        catch (error: CancellationException) { throw error }
        catch (_: Exception) {}
    }

    private suspend fun markTransportSnapshotStale(context: ActiveAccountContext, removedBookingId: String? = null) {
        val key = context.profileScopedKey("transport-data")
        requestLock(context.accountId, key).withLock {
            ensureCurrent(context)
            val cached = readEntry(context.accountId, key, TransportData.serializer()) ?: return@withLock
            val value = if (removedBookingId == null) cached.value else cached.value.copy(
                bookings = cached.value.bookings.filterNot { it.id == removedBookingId },
                totalCount = (cached.value.totalCount - cached.value.bookings.count { it.id == removedBookingId }).coerceAtLeast(0),
            )
            writeEntry(
                context.accountId,
                key,
                TransportData.serializer(),
                value,
                savedAtMillis = nowMillis() - UniCachePolicies.Transport.maxAgeMillis - 1L,
            )
        }
    }

    private suspend fun <T> cached(
        key: String,
        policy: UniCachePolicy,
        serializer: KSerializer<T>,
        forceRefresh: Boolean,
        requirePersistence: Boolean = false,
        mergeWithCached: (existing: T?, incoming: T) -> T = { _, incoming -> incoming },
        onSaved: suspend (ActiveAccountContext, T?, T) -> Unit = { _, _, _ -> },
        fetch: suspend (ActiveAccountContext) -> T,
    ): T {
        val requestStartedAt = nowMillis()
        val context = activeContext()
        val accountId = context.accountId
        val scopedKey = context.profileScopedKey(key)
        return requestLock(accountId, scopedKey).withLock {
            ensureCurrent(context)
            val cached = try { readEntry(accountId, scopedKey, serializer) }
                catch (error: CancellationException) { throw error }
                catch (_: Exception) { null }
            ensureCurrent(context)
            if (cached != null && nowMillis() - cached.savedAtMillis in 0 until policy.maxAgeMillis &&
                (!forceRefresh || cached.savedAtMillis > requestStartedAt)
            ) {
                return@withLock cached.value
            }
            try {
                val rawValue = fetch(context)
                ensureCurrent(context)
                val value = mergeWithCached(cached?.value, rawValue)
                var saved = false
                try {
                    writeEntry(accountId, scopedKey, serializer, value)
                    saved = true
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    if (requirePersistence) throw error
                }
                if (saved) {
                    try { onSaved(context, cached?.value, value) }
                    catch (error: CancellationException) { throw error }
                    catch (_: Exception) {}
                }
                ensureCurrent(context)
                value
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                ensureCurrent(context)
                if (fallbackToStaleCache) cached?.value ?: throw error else throw error
            }
        }
    }

    private fun <T> cachedStream(
        key: String,
        policy: UniCachePolicy,
        serializer: KSerializer<T>,
        forceRefresh: Boolean,
        requirePersistence: Boolean = false,
        mergeIntermediate: (existing: T?, incoming: T) -> T = { _, incoming -> incoming },
        mergeFinal: (existing: T?, incoming: T) -> T = mergeIntermediate,
        onSaved: suspend (ActiveAccountContext, T?, T) -> Unit = { _, _, _ -> },
        stream: suspend (ActiveAccountContext, T?, suspend (T) -> Unit) -> Unit,
    ): Flow<T> = flow {
        val requestStartedAt = nowMillis()
        val context = activeContext()
        val accountId = context.accountId
        val scopedKey = context.profileScopedKey(key)
        requestLock(accountId, scopedKey).withLock {
            ensureCurrent(context)
            val cached = try { readEntry(accountId, scopedKey, serializer) }
                catch (error: CancellationException) { throw error }
                catch (_: Exception) { null }
            ensureCurrent(context)
            if (cached != null && nowMillis() - cached.savedAtMillis in 0 until policy.maxAgeMillis &&
                (!forceRefresh || cached.savedAtMillis > requestStartedAt)
            ) {
                emit(cached.value)
                return@withLock
            }
            var latestMerged: T? = cached?.value
            var lastRawNetwork: T? = null
            var emittedAnyFromNetwork = false
            try {
                stream(context, cached?.value) { incoming ->
                    ensureCurrent(context)
                    lastRawNetwork = incoming
                    val merged = mergeIntermediate(latestMerged, incoming)
                    latestMerged = merged
                    emittedAnyFromNetwork = true
                    try {
                        writeEntry(
                            accountId = accountId,
                            key = scopedKey,
                            serializer = serializer,
                            value = merged,
                            savedAtMillis = cached?.savedAtMillis ?: nowMillis(),
                        )
                    } catch (error: CancellationException) {
                        throw error
                    } catch (_: Exception) {
                        // Best-effort intermediate cache checkpoint.
                    }
                    emit(merged)
                }
                ensureCurrent(context)
                val finalRaw = lastRawNetwork
                if (finalRaw != null) {
                    val finalValue = mergeFinal(cached?.value, finalRaw)
                    var saved = false
                    try {
                        writeEntry(accountId, scopedKey, serializer, finalValue)
                        saved = true
                    } catch (error: CancellationException) {
                        throw error
                    } catch (error: Exception) {
                        if (requirePersistence) throw error
                    }
                    if (saved) {
                        try { onSaved(context, cached?.value, finalValue) }
                        catch (error: CancellationException) { throw error }
                        catch (_: Exception) {}
                    }
                    ensureCurrent(context)
                    if (finalValue != latestMerged) {
                        emit(finalValue)
                    }
                } else if (cached != null) {
                    emit(cached.value)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                ensureCurrent(context)
                if (emittedAnyFromNetwork && latestMerged != null) {
                    return@withLock
                }
                if (fallbackToStaleCache && cached != null) {
                    emit(cached.value)
                } else {
                    throw error
                }
            }
        }
    }

    private suspend fun requestLock(accountId: String, key: String): Mutex =
        requestLocksGuard.withLock { requestLocks.getOrPut("$accountId|$key") { Mutex() } }

    private suspend fun <T> readEntry(accountId: String?, key: String, serializer: KSerializer<T>): CachedValue<T>? {
        val bytes = try {
            accounts.readCachedData(accountId, key)
        } catch (error: CancellationException) {
            throw error
        } catch (_: SecureStorageCorruptedException) {
            runCatching { accounts.removeCachedData(accountId, key) }
            return null
        } catch (_: SecureStorageDecodingException) {
            runCatching { accounts.removeCachedData(accountId, key) }
            return null
        } ?: return null
        return try {
            val envelope = json.decodeFromString<CacheEnvelope>(bytes.decodeToString(throwOnInvalidSequence = true))
            if (envelope.schemaVersion != CACHE_SCHEMA_VERSION) {
                runCatching { accounts.removeCachedData(accountId, key) }
                savedAtGuard.withLock { savedAtByKey.remove("$accountId|$key") }
                return null
            }
            savedAtGuard.withLock { savedAtByKey["$accountId|$key"] = envelope.savedAtMillis }
            CachedValue(envelope.savedAtMillis, json.decodeFromString(serializer, envelope.payload))
        } catch (error: CancellationException) {
            throw error
        } catch (_: SerializationException) {
            runCatching { accounts.removeCachedData(accountId, key) }
            null
        } catch (_: CharacterCodingException) {
            runCatching { accounts.removeCachedData(accountId, key) }
            null
        } catch (_: IllegalArgumentException) {
            runCatching { accounts.removeCachedData(accountId, key) }
            null
        } finally {
            bytes.fill(0)
        }
    }

    private suspend fun <T> writeEntry(
        accountId: String?,
        key: String,
        serializer: KSerializer<T>,
        value: T,
        savedAtMillis: Long = nowMillis(),
    ) {
        val envelope = CacheEnvelope(CACHE_SCHEMA_VERSION, savedAtMillis, json.encodeToString(serializer, value))
        val bytes = json.encodeToString(CacheEnvelope.serializer(), envelope).encodeToByteArray()
        try {
            accounts.writeCachedData(accountId, key, bytes)
            savedAtGuard.withLock { savedAtByKey["$accountId|$key"] = savedAtMillis }
        } finally {
            bytes.fill(0)
        }
    }

    private suspend fun invalidate(accountId: String, key: String) {
        savedAtGuard.withLock { savedAtByKey.remove("$accountId|$key") }
        accounts.removeCachedData(accountId, key)
    }

    internal data class CachedValue<T>(val savedAtMillis: Long, val value: T)
    private data class ActiveAccountContext(
        val accountId: String,
        val sessionState: AppSessionState.Authenticated,
    )

    @Serializable
    private data class CacheEnvelope(val schemaVersion: Int, val savedAtMillis: Long, val payload: String)

    private companion object {
        const val CACHE_SCHEMA_VERSION = 3
        const val NEWS_ARCHIVE_SYNC_INTERVAL_MILLIS = 6L * 60L * 60_000L
    }
}
