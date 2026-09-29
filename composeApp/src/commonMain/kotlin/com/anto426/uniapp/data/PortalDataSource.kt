package com.anto426.uniapp.data

import com.anto426.uniapp.account.storage.UniAccountStore
import com.anto426.uniapp.session.SessionManager
import com.anto426.uniapp.session.model.AppSessionState
import com.anto426.unisdk.backend.UniBackendService
import com.anto426.unisdk.session.UniCredentials
import com.anto426.unisdk.session.UniSession
import com.anto426.unisdk.transport.TransportService
import com.anto426.unisdk.transport.TransportSession
import com.anto426.unisdk.backend.model.AttendanceRecord
import com.anto426.unisdk.backend.model.CareerData
import com.anto426.unisdk.backend.model.ConnectedDeviceData
import com.anto426.unisdk.backend.model.CourseSyllabusData
import com.anto426.unisdk.backend.model.ExamRoundData
import com.anto426.unisdk.backend.model.StudyPlanData
import com.anto426.unisdk.backend.model.SurveyCourseData
import com.anto426.unisdk.backend.model.SurveyFirstPageData
import com.anto426.unisdk.backend.model.SurveySaveRequest
import com.anto426.unisdk.backend.model.StudentDetailsData
import com.anto426.unisdk.backend.model.TaxesData
import com.anto426.unisdk.backend.model.UniversityContact
import com.anto426.unisdk.backend.model.UniversityNews
import com.anto426.unisdk.backend.model.ProfessorDashboardData
import com.anto426.unisdk.platform.currentEpochMillis
import com.anto426.unisdk.transport.TransportActionResult
import com.anto426.unisdk.transport.TransportBookingRequest
import com.anto426.unisdk.transport.TransportData
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

/**
 * Cache entries live inside the active account's encrypted vault. Fresh entries avoid network
 * access; stale entries are used only as an offline fallback after a failed refresh.
 */
class PortalDataSource(
    private val sessions: SessionManager,
    private val accounts: UniAccountStore,
    private val fixedAccountId: String? = null,
    private val fixedProfileId: String? = null,
    private val nowMillis: () -> Long = ::currentEpochMillis,
    private val fallbackToStaleCache: Boolean = true,
) : UniAppDataSource, UniAppPortraitSharer {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val requestLocksGuard = Mutex()
    private val requestLocks = mutableMapOf<String, Mutex>()

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

    private fun ensureCurrent(context: ActiveAccountContext) {
        if (sessions.state.value !== context.sessionState) {
            throw CancellationException("The account or profile changed while loading data")
        }
    }

    @Suppress("UNCHECKED_CAST")
    internal suspend fun <T : Any> readCached(request: com.anto426.uniapp.data.runtime.UniAppDataRequest<T>): CachedValue<T>? {
        val context = activeContext()
        if (request.id.startsWith("portrait/")) {
            val cached = accounts.readProfileImage(context.accountId, request.id.removePrefix("portrait/"))
            ensureCurrent(context)
            return cached?.let { CachedValue(it.savedAtMillis, it.bytes as T) }
        }
        val key = request.cacheKey ?: return null
        val serializer = request.serializer ?: return null
        val value = try {
            readEntry(context.accountId, context.profileScopedKey(key), serializer)
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            null // Damaged or unavailable cache must not prevent a network refresh.
        }
        ensureCurrent(context)
        return value
    }

    override suspend fun loadCareer(forceRefresh: Boolean): CareerData =
        cached("career-v2", UniAppCachePolicies.Career, CareerData.serializer(), forceRefresh) { context ->
            context.backend { session, credentials -> loadCareer(session, credentials) }
        }

    override suspend fun loadProfessorDashboard(forceRefresh: Boolean): ProfessorDashboardData =
        cached(
            "professor-dashboard",
            UniAppCachePolicies.ProfessorDashboard,
            ProfessorDashboardData.serializer(),
            forceRefresh,
        ) { context -> context.backend { session, credentials -> loadProfessorDashboard(session, credentials) } }

    override suspend fun loadStudyPlan(forceRefresh: Boolean): StudyPlanData =
        cached("study-plan", UniAppCachePolicies.StudyPlan, StudyPlanData.serializer(), forceRefresh) { context ->
            context.backend { session, credentials -> loadStudyPlan(session, credentials) }
        }

    override suspend fun loadExamRounds(forceRefresh: Boolean): List<ExamRoundData> =
        cached("exam-rounds", UniAppCachePolicies.ExamRounds, ListSerializer(ExamRoundData.serializer()), forceRefresh) { context ->
            context.backend { session, credentials -> loadExamRounds(session, credentials) }
        }

    override suspend fun loadCourseSyllabus(adsceId: String, forceRefresh: Boolean): CourseSyllabusData =
        cached(
            datasetCacheKey("course-syllabus", adsceId),
            UniAppCachePolicies.CourseSyllabus,
            CourseSyllabusData.serializer(),
            forceRefresh,
        ) { context -> context.backend { session, credentials -> loadCourseSyllabus(session, credentials, adsceId) } }

    override suspend fun bookExamRound(round: ExamRoundData): String {
        val context = activeContext()
        return context.backend { session, credentials -> bookExamRound(session, credentials, round) }.also {
            invalidate(context.accountId, context.profileScopedKey("exam-rounds"))
        }
    }

    override suspend fun cancelExamRound(round: ExamRoundData): String {
        val context = activeContext()
        return context.backend { session, credentials -> cancelExamRound(session, credentials, round) }.also {
            invalidate(context.accountId, context.profileScopedKey("exam-rounds"))
        }
    }

    override suspend fun loadTaxes(forceRefresh: Boolean): TaxesData =
        cached("taxes", UniAppCachePolicies.Taxes, TaxesData.serializer(), forceRefresh) { context ->
            context.backend { session, credentials -> loadTaxes(session, credentials) }
        }

    override suspend fun loadStudentDetails(forceRefresh: Boolean): StudentDetailsData =
        cached("student-details-v2", UniAppCachePolicies.StudentDetails, StudentDetailsData.serializer(), forceRefresh) { context ->
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
                if (!forceRefresh && cached != null && nowMillis() - cached.savedAtMillis in 0 until UniAppCachePolicies.ProfileImage.maxAgeMillis) {
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
                            // Keep the downloaded image usable even when persistence fails.
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

    override suspend fun loadConnectedDevices(forceRefresh: Boolean): List<ConnectedDeviceData> =
        cached(
            "connected-devices",
            UniAppCachePolicies.ConnectedDevices,
            ListSerializer(ConnectedDeviceData.serializer()),
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
            UniAppCachePolicies.Attendance,
            ListSerializer(AttendanceRecord.serializer()),
            forceRefresh,
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
        cached(
            "university-news", UniAppCachePolicies.News,
            ListSerializer(UniversityNews.serializer()), forceRefresh,
        ) { context -> context.backend { session, credentials -> loadUniversityNews(session, credentials) } }

    override suspend fun loadUniversityContacts(forceRefresh: Boolean): List<UniversityContact> =
        cached(
            "university-contacts-v2",
            UniAppCachePolicies.Contacts,
            ListSerializer(UniversityContact.serializer()),
            forceRefresh,
        ) { context -> context.backend { session, credentials -> loadUniversityContacts(session, credentials) } }

    override suspend fun loadSurveyCourses(forceRefresh: Boolean): List<SurveyCourseData> =
        cached(
            "survey-courses",
            UniAppCachePolicies.SurveyCourses,
            ListSerializer(SurveyCourseData.serializer()),
            forceRefresh,
        ) { context -> context.backend { session, credentials -> loadSurveyCourses(session, credentials) } }

    override suspend fun loadSurveyFirstPage(courseId: String, tagList: String): SurveyFirstPageData =
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
            UniAppCachePolicies.SurveyStatus,
            Boolean.serializer(),
            forceRefresh,
        ) { context -> context.backend { session, credentials -> loadSurveyCompilationStatus(session, credentials, adCod) } }

    override suspend fun loadTransportData(forceRefresh: Boolean): TransportData =
        cached(
            "transport-data",
            UniAppCachePolicies.Transport,
            TransportData.serializer(),
            forceRefresh,
            requirePersistence = true,
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
        ) { context -> context.transport { session -> loadTransportData(session) } }

    override suspend fun loadTransportTicketImage(bookingId: String, source: String, forceRefresh: Boolean): ByteArray {
        val context = activeContext()
        val key = context.profileScopedKey(ticketImageCacheKey(bookingId))
        return requestLock(context.accountId, key).withLock {
            ensureCurrent(context)
            val original = accounts.readCachedData(context.accountId, key)
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

    private suspend fun safelyMarkTransportSnapshotStale(context: ActiveAccountContext, removedBookingId: String? = null) {
        try { markTransportSnapshotStale(context, removedBookingId) }
        catch (error: CancellationException) { throw error }
        catch (_: Exception) { /* The server mutation succeeded; the next refresh will retry. */ }
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
            // Expire the list without erasing the last known good encrypted snapshot.
            writeEntry(
                context.accountId, key, TransportData.serializer(), value,
                savedAtMillis = nowMillis() - UniAppCachePolicies.Transport.maxAgeMillis - 1L,
            )
        }
    }

    private suspend fun <T> cached(
        key: String,
        policy: UniAppCachePolicy,
        serializer: KSerializer<T>,
        forceRefresh: Boolean,
        requirePersistence: Boolean = false,
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
                val value = fetch(context)
                ensureCurrent(context)
                // A disk write failure must not replace a successful response with stale data.
                var saved = false
                try {
                    writeEntry(accountId, scopedKey, serializer, value)
                    saved = true
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    if (requirePersistence) throw error
                    // The active repository can still use the response for this session.
                }
                if (saved) {
                    try { onSaved(context, cached?.value, value) }
                    catch (error: CancellationException) { throw error }
                    catch (_: Exception) { /* A cleanup failure must not hide the saved list. */ }
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

    private suspend fun requestLock(accountId: String, key: String): Mutex =
        requestLocksGuard.withLock { requestLocks.getOrPut("$accountId|$key") { Mutex() } }

    private suspend fun <T> readEntry(accountId: String?, key: String, serializer: KSerializer<T>): CachedValue<T>? {
        val bytes = accounts.readCachedData(accountId, key) ?: return null
        return try {
            val envelope = json.decodeFromString<CacheEnvelope>(bytes.decodeToString())
            if (envelope.schemaVersion != CACHE_SCHEMA_VERSION) return null
            CachedValue(envelope.savedAtMillis, json.decodeFromString(serializer, envelope.payload))
        } catch (error: CancellationException) {
            throw error
        } catch (_: kotlinx.serialization.SerializationException) {
            accounts.removeCachedData(accountId, key)
            null
        } finally {
            bytes.fill(0)
        }
    }

    private suspend fun <T> writeEntry(accountId: String?, key: String, serializer: KSerializer<T>, value: T, savedAtMillis: Long = nowMillis()) {
        val envelope = CacheEnvelope(CACHE_SCHEMA_VERSION, savedAtMillis, json.encodeToString(serializer, value))
        val bytes = json.encodeToString(CacheEnvelope.serializer(), envelope).encodeToByteArray()
        try {
            accounts.writeCachedData(accountId, key, bytes)
        } finally {
            bytes.fill(0)
        }
    }

    private suspend fun invalidate(accountId: String, key: String) {
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
        const val CACHE_SCHEMA_VERSION = 1
    }
}
