package com.anto426.uniapp.data

import com.anto426.uniapp.account.storage.UniAccountStore
import com.anto426.uniapp.account.session.UniAccountClient
import com.anto426.uniapp.session.AppSessionController
import com.anto426.uniapp.session.model.AppSessionState
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

/** Account-aware, encrypted and policy-driven data boundary used by presentation code. */
interface UniAppDataSource {
    suspend fun loadCareer(forceRefresh: Boolean = false): CareerData
    suspend fun loadProfessorDashboard(forceRefresh: Boolean = false): ProfessorDashboardData
    suspend fun loadStudyPlan(forceRefresh: Boolean = false): StudyPlanData
    suspend fun loadExamRounds(forceRefresh: Boolean = false): List<ExamRoundData>
    suspend fun loadCourseSyllabus(adsceId: String, forceRefresh: Boolean = false): CourseSyllabusData
    suspend fun bookExamRound(round: ExamRoundData): String
    suspend fun cancelExamRound(round: ExamRoundData): String
    suspend fun loadTaxes(forceRefresh: Boolean = false): TaxesData
    suspend fun loadStudentDetails(forceRefresh: Boolean = false): StudentDetailsData
    suspend fun loadProfileImage(source: String, forceRefresh: Boolean = false): ByteArray
    suspend fun loadConnectedDevices(forceRefresh: Boolean = false): List<ConnectedDeviceData>
    suspend fun disconnectDevice(targetToken: String): String
    suspend fun disconnectAllOtherDevices(): String
    suspend fun loadAttendanceHistory(forceRefresh: Boolean = false): List<AttendanceRecord>
    suspend fun loadUniversityNews(forceRefresh: Boolean = false): List<UniversityNews>
    suspend fun loadUniversityContacts(forceRefresh: Boolean = false): List<UniversityContact>
    suspend fun loadSurveyCourses(forceRefresh: Boolean = false): List<SurveyCourseData>
    suspend fun loadSurveyFirstPage(courseId: String, tagList: String): SurveyFirstPageData
    suspend fun saveSurvey(courseId: String, request: SurveySaveRequest): String
    suspend fun loadSurveyCompilationStatus(adCod: String, forceRefresh: Boolean = false): Boolean
    suspend fun loadTransportData(forceRefresh: Boolean = false): TransportData
    suspend fun bookTransport(request: TransportBookingRequest): TransportActionResult
    suspend fun deleteTransportBooking(bookingId: String): TransportActionResult
    suspend fun readPreference(key: String): String?
    suspend fun writePreference(key: String, value: String)
}

data class UniAppCachePolicy(val maxAgeMillis: Long) {
    init {
        require(maxAgeMillis >= 0L)
    }
}

object UniAppCachePolicies {
    val Transport = UniAppCachePolicy(minutes(2))
    val ConnectedDevices = UniAppCachePolicy(minutes(5))
    val ExamRounds = UniAppCachePolicy(minutes(15))
    val StudyPlan = UniAppCachePolicy(hours(6))
    val SurveyCourses = UniAppCachePolicy(minutes(30))
    val Attendance = UniAppCachePolicy(minutes(30))
    val SurveyStatus = UniAppCachePolicy(minutes(30))
    val News = UniAppCachePolicy(hours(1))
    val Taxes = UniAppCachePolicy(hours(2))
    val Career = UniAppCachePolicy(hours(6))
    val ProfessorDashboard = UniAppCachePolicy(minutes(15))
    val StudentDetails = UniAppCachePolicy(hours(6))
    val ProfileImage = UniAppCachePolicy(hours(24 * 7))
    val Contacts = UniAppCachePolicy(hours(24))
    val CourseSyllabus = UniAppCachePolicy(hours(24 * 7))

    private fun minutes(value: Long): Long = value * 60_000L
    private fun hours(value: Long): Long = value * 60L * 60_000L
}

/**
 * Cache entries live inside the active account's encrypted vault. Fresh entries avoid network
 * access; stale entries are used only as an offline fallback after a failed refresh.
 */
class SessionUniAppDataSource(
    private val sessions: AppSessionController,
    private val accounts: UniAccountStore,
    private val fixedAccountId: String? = null,
    private val fixedProfileId: String? = null,
    private val nowMillis: () -> Long = ::currentEpochMillis,
) : UniAppDataSource {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val requestLocksGuard = Mutex()
    private val requestLocks = mutableMapOf<String, Mutex>()
    private val memoryCacheGuard = Mutex()
    private val memoryCache = mutableMapOf<String, CachedValue<Any?>>()

    private fun activeAccountId(): String =
        fixedAccountId
            ?: (sessions.state.value as? AppSessionState.Authenticated)?.account?.accountId
            ?: error("Nessuna sessione universitaria attiva.")

    private fun activeProfileId(): String =
        fixedProfileId
            ?: (sessions.state.value as? AppSessionState.Authenticated)?.account?.activeProfileId
            ?: "default"

    private fun profileScopedKey(key: String): String =
        "profile-${activeProfileId().hashCode().toUInt().toString(16)}-$key"

    private fun activeContext(): ActiveAccountContext {
        val accountId = activeAccountId()
        val client = checkNotNull(sessions.accountClient(accountId)) { "La sessione dell'account non è più attiva." }
        return ActiveAccountContext(accountId, client)
    }

    override suspend fun loadCareer(forceRefresh: Boolean): CareerData =
        cached("career-v2", UniAppCachePolicies.Career, CareerData.serializer(), forceRefresh) { client ->
            client.loadCareer()
        }

    override suspend fun loadProfessorDashboard(forceRefresh: Boolean): ProfessorDashboardData =
        cached(
            "professor-dashboard",
            UniAppCachePolicies.ProfessorDashboard,
            ProfessorDashboardData.serializer(),
            forceRefresh,
        ) { client -> client.loadProfessorDashboard() }

    override suspend fun loadStudyPlan(forceRefresh: Boolean): StudyPlanData =
        cached("study-plan", UniAppCachePolicies.StudyPlan, StudyPlanData.serializer(), forceRefresh) { client ->
            client.loadStudyPlan()
        }

    override suspend fun loadExamRounds(forceRefresh: Boolean): List<ExamRoundData> =
        cached("exam-rounds", UniAppCachePolicies.ExamRounds, ListSerializer(ExamRoundData.serializer()), forceRefresh) { client ->
            client.loadExamRounds()
        }

    override suspend fun loadCourseSyllabus(adsceId: String, forceRefresh: Boolean): CourseSyllabusData =
        cached(
            "course-syllabus-${adsceId.hashCode()}",
            UniAppCachePolicies.CourseSyllabus,
            CourseSyllabusData.serializer(),
            forceRefresh,
        ) { client -> client.loadCourseSyllabus(adsceId) }

    override suspend fun bookExamRound(round: ExamRoundData): String {
        val context = activeContext()
        return context.client.bookExamRound(round).also {
            invalidate(context.accountId, profileScopedKey("exam-rounds"))
        }
    }

    override suspend fun cancelExamRound(round: ExamRoundData): String {
        val context = activeContext()
        return context.client.cancelExamRound(round).also {
            invalidate(context.accountId, profileScopedKey("exam-rounds"))
        }
    }

    override suspend fun loadTaxes(forceRefresh: Boolean): TaxesData =
        cached("taxes", UniAppCachePolicies.Taxes, TaxesData.serializer(), forceRefresh) { client ->
            client.loadTaxes()
        }

    override suspend fun loadStudentDetails(forceRefresh: Boolean): StudentDetailsData =
        cached("student-details", UniAppCachePolicies.StudentDetails, StudentDetailsData.serializer(), forceRefresh) { client ->
            client.loadStudentDetails()
        }

    override suspend fun loadProfileImage(source: String, forceRefresh: Boolean): ByteArray =
        activeContext().let { context ->
            requestLock(context.accountId, "profile-image").withLock {
                val cached = accounts.readProfileImage(context.accountId, source)
                if (!forceRefresh && cached != null && nowMillis() - cached.savedAtMillis <= UniAppCachePolicies.ProfileImage.maxAgeMillis) {
                    return@withLock cached.bytes
                }
                try {
                    context.client.loadProfileImage(source).also { bytes ->
                        accounts.writeProfileImage(context.accountId, source, nowMillis(), bytes)
                    }
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Throwable) {
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
        ) { client -> client.loadConnectedDevices() }

    override suspend fun disconnectDevice(targetToken: String): String {
        val context = activeContext()
        return context.client.disconnectDevice(targetToken).also {
            invalidate(context.accountId, profileScopedKey("connected-devices"))
        }
    }

    override suspend fun disconnectAllOtherDevices(): String {
        val context = activeContext()
        return context.client.disconnectAllOtherDevices().also {
            invalidate(context.accountId, profileScopedKey("connected-devices"))
        }
    }

    override suspend fun loadAttendanceHistory(forceRefresh: Boolean): List<AttendanceRecord> =
        cached(
            "attendance",
            UniAppCachePolicies.Attendance,
            ListSerializer(AttendanceRecord.serializer()),
            forceRefresh,
        ) { client -> client.loadAttendanceHistory() }

    override suspend fun loadUniversityNews(forceRefresh: Boolean): List<UniversityNews> =
        cached(
            "university-news",
            UniAppCachePolicies.News,
            ListSerializer(UniversityNews.serializer()),
            forceRefresh,
        ) { client -> client.loadUniversityNews() }

    override suspend fun loadUniversityContacts(forceRefresh: Boolean): List<UniversityContact> =
        cached(
            "university-contacts",
            UniAppCachePolicies.Contacts,
            ListSerializer(UniversityContact.serializer()),
            forceRefresh,
        ) { client -> client.loadUniversityContacts() }

    override suspend fun loadSurveyCourses(forceRefresh: Boolean): List<SurveyCourseData> =
        cached(
            "survey-courses",
            UniAppCachePolicies.SurveyCourses,
            ListSerializer(SurveyCourseData.serializer()),
            forceRefresh,
        ) { client -> client.loadSurveyCourses() }

    override suspend fun loadSurveyFirstPage(courseId: String, tagList: String): SurveyFirstPageData =
        activeContext().client.loadSurveyFirstPage(courseId, tagList)

    override suspend fun saveSurvey(courseId: String, request: SurveySaveRequest): String {
        val context = activeContext()
        return context.client.saveSurvey(courseId, request).also {
            invalidate(context.accountId, profileScopedKey("survey-courses"))
        }
    }

    override suspend fun loadSurveyCompilationStatus(adCod: String, forceRefresh: Boolean): Boolean =
        cached(
            "survey-status-${adCod.hashCode()}",
            UniAppCachePolicies.SurveyStatus,
            Boolean.serializer(),
            forceRefresh,
        ) { client -> client.loadSurveyCompilationStatus(adCod) }

    override suspend fun loadTransportData(forceRefresh: Boolean): TransportData =
        cached("transport", UniAppCachePolicies.Transport, TransportData.serializer(), forceRefresh) { client ->
            client.withTransportSession { session -> loadTransportData(session) }
        }

    override suspend fun bookTransport(request: TransportBookingRequest): TransportActionResult {
        val context = activeContext()
        return context.client.withTransportSession { session -> bookTransport(session, request) }
            .also { invalidate(context.accountId, profileScopedKey("transport")) }
    }

    override suspend fun deleteTransportBooking(bookingId: String): TransportActionResult {
        val context = activeContext()
        return context.client.withTransportSession { session -> deleteTransportBooking(session, bookingId) }
            .also { invalidate(context.accountId, profileScopedKey("transport")) }
    }

    override suspend fun readPreference(key: String): String? {
        return accounts.readPreference(activeAccountId(), key)
    }

    override suspend fun writePreference(key: String, value: String) {
        accounts.writePreference(activeAccountId(), key, value)
    }

    private suspend fun <T> cached(
        key: String,
        policy: UniAppCachePolicy,
        serializer: KSerializer<T>,
        forceRefresh: Boolean,
        fetch: suspend (UniAccountClient) -> T,
    ): T {
        val context = activeContext()
        val accountId = context.accountId
        val scopedKey = profileScopedKey(key)
        return requestLock(accountId, scopedKey).withLock {
            val cached = readEntry(accountId, scopedKey, serializer)
            if (!forceRefresh && cached != null && nowMillis() - cached.savedAtMillis <= policy.maxAgeMillis) {
                return@withLock cached.value
            }
            try {
                fetch(context.client).also { value -> writeEntry(accountId, scopedKey, serializer, value) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                cached?.value ?: throw error
            }
        }
    }

    private suspend fun requestLock(accountId: String, key: String): Mutex =
        requestLocksGuard.withLock { requestLocks.getOrPut("$accountId|$key") { Mutex() } }

    private suspend fun <T> readEntry(accountId: String, key: String, serializer: KSerializer<T>): CachedValue<T>? {
        @Suppress("UNCHECKED_CAST")
        memoryCacheGuard.withLock {
            memoryCache[cacheIdentity(accountId, key)]?.let { return it as CachedValue<T> }
        }
        val bytes = accounts.readCachedData(accountId, key) ?: return null
        return try {
            val envelope = json.decodeFromString<CacheEnvelope>(bytes.decodeToString())
            if (envelope.schemaVersion != CACHE_SCHEMA_VERSION) return null
            CachedValue(envelope.savedAtMillis, json.decodeFromString(serializer, envelope.payload)).also { entry ->
                memoryCacheGuard.withLock {
                    memoryCache[cacheIdentity(accountId, key)] =
                        CachedValue(entry.savedAtMillis, entry.value)
                }
            }
        } catch (_: Throwable) {
            accounts.removeCachedData(accountId, key)
            null
        } finally {
            bytes.fill(0)
        }
    }

    private suspend fun <T> writeEntry(accountId: String, key: String, serializer: KSerializer<T>, value: T) {
        val savedAtMillis = nowMillis()
        val envelope = CacheEnvelope(CACHE_SCHEMA_VERSION, savedAtMillis, json.encodeToString(serializer, value))
        val bytes = json.encodeToString(CacheEnvelope.serializer(), envelope).encodeToByteArray()
        try {
            accounts.writeCachedData(accountId, key, bytes)
            memoryCacheGuard.withLock {
                memoryCache[cacheIdentity(accountId, key)] = CachedValue(savedAtMillis, value)
            }
        } finally {
            bytes.fill(0)
        }
    }

    private suspend fun invalidate(accountId: String, key: String) {
        memoryCacheGuard.withLock { memoryCache.remove(cacheIdentity(accountId, key)) }
        accounts.removeCachedData(accountId, key)
    }

    private fun cacheIdentity(accountId: String, key: String): String = "$accountId|$key"

    private data class CachedValue<T>(val savedAtMillis: Long, val value: T)
    private data class ActiveAccountContext(val accountId: String, val client: UniAccountClient)

    @Serializable
    private data class CacheEnvelope(val schemaVersion: Int, val savedAtMillis: Long, val payload: String)

    private companion object {
        const val CACHE_SCHEMA_VERSION = 1
    }
}
