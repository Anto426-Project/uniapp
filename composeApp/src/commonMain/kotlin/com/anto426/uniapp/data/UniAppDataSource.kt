package com.anto426.uniapp.data

import com.anto426.uniapp.account.storage.UniAccountStore
import com.anto426.uniapp.session.AppSessionController
import com.anto426.uniapp.session.model.AppSessionState
import com.anto426.unisdk.backend.model.AttendanceRecord
import com.anto426.unisdk.backend.model.CareerData
import com.anto426.unisdk.backend.model.ConnectedDeviceData
import com.anto426.unisdk.backend.model.CourseSyllabusData
import com.anto426.unisdk.backend.model.ExamRoundData
import com.anto426.unisdk.backend.model.MoodleOverview
import com.anto426.unisdk.backend.model.StudentDetailsData
import com.anto426.unisdk.backend.model.TaxesData
import com.anto426.unisdk.backend.model.UniversityContact
import com.anto426.unisdk.backend.model.UniversityNews
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
import kotlinx.serialization.json.Json

/** Account-aware, encrypted and policy-driven data boundary used by presentation code. */
interface UniAppDataSource {
    suspend fun loadCareer(forceRefresh: Boolean = false): CareerData
    suspend fun loadExamRounds(forceRefresh: Boolean = false): List<ExamRoundData>
    suspend fun loadCourseSyllabus(adsceId: String, forceRefresh: Boolean = false): CourseSyllabusData
    suspend fun bookExamRound(round: ExamRoundData): String
    suspend fun cancelExamRound(round: ExamRoundData): String
    suspend fun loadTaxes(forceRefresh: Boolean = false): TaxesData
    suspend fun loadStudentDetails(forceRefresh: Boolean = false): StudentDetailsData
    suspend fun loadConnectedDevices(forceRefresh: Boolean = false): List<ConnectedDeviceData>
    suspend fun disconnectDevice(targetToken: String): String
    suspend fun disconnectAllOtherDevices(): String
    suspend fun loadAttendanceHistory(forceRefresh: Boolean = false): List<AttendanceRecord>
    suspend fun loadUniversityNews(forceRefresh: Boolean = false): List<UniversityNews>
    suspend fun loadUniversityContacts(forceRefresh: Boolean = false): List<UniversityContact>
    suspend fun loadMoodleOverview(forceRefresh: Boolean = false): MoodleOverview
    suspend fun loadTransportData(forceRefresh: Boolean = false): TransportData
    suspend fun bookTransport(request: TransportBookingRequest): TransportActionResult
    suspend fun deleteTransportBooking(bookingId: String): TransportActionResult
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
    val Moodle = UniAppCachePolicy(minutes(15))
    val Attendance = UniAppCachePolicy(minutes(30))
    val News = UniAppCachePolicy(hours(1))
    val Taxes = UniAppCachePolicy(hours(2))
    val Career = UniAppCachePolicy(hours(6))
    val StudentDetails = UniAppCachePolicy(hours(6))
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
    private val nowMillis: () -> Long = ::currentEpochMillis,
) : UniAppDataSource {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val cacheLock = Mutex()

    private fun activeAccountId(): String =
        (sessions.state.value as? AppSessionState.Authenticated)?.account?.accountId
            ?: error("Nessuna sessione universitaria attiva.")

    private fun activeClient() =
        checkNotNull(sessions.currentAccountClient()) { "Nessuna sessione universitaria attiva." }

    override suspend fun loadCareer(forceRefresh: Boolean): CareerData =
        cached("career", UniAppCachePolicies.Career, CareerData.serializer(), forceRefresh) {
            activeClient().loadCareer()
        }

    override suspend fun loadExamRounds(forceRefresh: Boolean): List<ExamRoundData> =
        cached("exam-rounds", UniAppCachePolicies.ExamRounds, ListSerializer(ExamRoundData.serializer()), forceRefresh) {
            activeClient().loadExamRounds()
        }

    override suspend fun loadCourseSyllabus(adsceId: String, forceRefresh: Boolean): CourseSyllabusData =
        cached(
            "course-syllabus-${adsceId.hashCode()}",
            UniAppCachePolicies.CourseSyllabus,
            CourseSyllabusData.serializer(),
            forceRefresh,
        ) { activeClient().loadCourseSyllabus(adsceId) }

    override suspend fun bookExamRound(round: ExamRoundData): String =
        activeClient().bookExamRound(round).also { invalidate("exam-rounds") }

    override suspend fun cancelExamRound(round: ExamRoundData): String =
        activeClient().cancelExamRound(round).also { invalidate("exam-rounds") }

    override suspend fun loadTaxes(forceRefresh: Boolean): TaxesData =
        cached("taxes", UniAppCachePolicies.Taxes, TaxesData.serializer(), forceRefresh) {
            activeClient().loadTaxes()
        }

    override suspend fun loadStudentDetails(forceRefresh: Boolean): StudentDetailsData =
        cached("student-details", UniAppCachePolicies.StudentDetails, StudentDetailsData.serializer(), forceRefresh) {
            activeClient().loadStudentDetails()
        }

    override suspend fun loadConnectedDevices(forceRefresh: Boolean): List<ConnectedDeviceData> =
        cached(
            "connected-devices",
            UniAppCachePolicies.ConnectedDevices,
            ListSerializer(ConnectedDeviceData.serializer()),
            forceRefresh,
        ) { activeClient().loadConnectedDevices() }

    override suspend fun disconnectDevice(targetToken: String): String =
        activeClient().disconnectDevice(targetToken).also { invalidate("connected-devices") }

    override suspend fun disconnectAllOtherDevices(): String =
        activeClient().disconnectAllOtherDevices().also { invalidate("connected-devices") }

    override suspend fun loadAttendanceHistory(forceRefresh: Boolean): List<AttendanceRecord> =
        cached(
            "attendance",
            UniAppCachePolicies.Attendance,
            ListSerializer(AttendanceRecord.serializer()),
            forceRefresh,
        ) { activeClient().loadAttendanceHistory() }

    override suspend fun loadUniversityNews(forceRefresh: Boolean): List<UniversityNews> =
        cached(
            "university-news",
            UniAppCachePolicies.News,
            ListSerializer(UniversityNews.serializer()),
            forceRefresh,
        ) { activeClient().loadUniversityNews() }

    override suspend fun loadUniversityContacts(forceRefresh: Boolean): List<UniversityContact> =
        cached(
            "university-contacts",
            UniAppCachePolicies.Contacts,
            ListSerializer(UniversityContact.serializer()),
            forceRefresh,
        ) { activeClient().loadUniversityContacts() }

    override suspend fun loadMoodleOverview(forceRefresh: Boolean): MoodleOverview =
        cached("moodle", UniAppCachePolicies.Moodle, MoodleOverview.serializer(), forceRefresh) {
            activeClient().loadMoodleOverview()
        }

    override suspend fun loadTransportData(forceRefresh: Boolean): TransportData =
        cached("transport", UniAppCachePolicies.Transport, TransportData.serializer(), forceRefresh) {
            activeClient().withTransportSession { session -> loadTransportData(session) }
        }

    override suspend fun bookTransport(request: TransportBookingRequest): TransportActionResult =
        activeClient().withTransportSession { session -> bookTransport(session, request) }
            .also { invalidate("transport") }

    override suspend fun deleteTransportBooking(bookingId: String): TransportActionResult =
        activeClient().withTransportSession { session -> deleteTransportBooking(session, bookingId) }
            .also { invalidate("transport") }

    private suspend fun <T> cached(
        key: String,
        policy: UniAppCachePolicy,
        serializer: KSerializer<T>,
        forceRefresh: Boolean,
        fetch: suspend () -> T,
    ): T {
        val accountId = activeAccountId()
        val cached = readEntry(accountId, key, serializer)
        if (!forceRefresh && cached != null && nowMillis() - cached.savedAtMillis <= policy.maxAgeMillis) {
            return cached.value
        }
        return try {
            fetch().also { value -> writeEntry(accountId, key, serializer, value) }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            cached?.value ?: throw error
        }
    }

    private suspend fun <T> readEntry(accountId: String, key: String, serializer: KSerializer<T>): CachedValue<T>? =
        cacheLock.withLock {
            val bytes = accounts.readCachedData(accountId, key) ?: return@withLock null
            try {
                val envelope = json.decodeFromString<CacheEnvelope>(bytes.decodeToString())
                if (envelope.schemaVersion != CACHE_SCHEMA_VERSION) return@withLock null
                CachedValue(envelope.savedAtMillis, json.decodeFromString(serializer, envelope.payload))
            } catch (_: Throwable) {
                accounts.removeCachedData(accountId, key)
                null
            } finally {
                bytes.fill(0)
            }
        }

    private suspend fun <T> writeEntry(accountId: String, key: String, serializer: KSerializer<T>, value: T) {
        cacheLock.withLock {
            val envelope = CacheEnvelope(CACHE_SCHEMA_VERSION, nowMillis(), json.encodeToString(serializer, value))
            val bytes = json.encodeToString(CacheEnvelope.serializer(), envelope).encodeToByteArray()
            try {
                accounts.writeCachedData(accountId, key, bytes)
            } finally {
                bytes.fill(0)
            }
        }
    }

    private suspend fun invalidate(key: String) {
        accounts.removeCachedData(activeAccountId(), key)
    }

    private data class CachedValue<T>(val savedAtMillis: Long, val value: T)

    @Serializable
    private data class CacheEnvelope(val schemaVersion: Int, val savedAtMillis: Long, val payload: String)

    private companion object {
        const val CACHE_SCHEMA_VERSION = 1
    }
}
