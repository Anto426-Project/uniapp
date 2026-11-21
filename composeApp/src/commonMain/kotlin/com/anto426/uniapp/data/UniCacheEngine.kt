package com.anto426.uniapp.data

import com.anto426.uniapp.account.model.UniAccountSummary
import com.anto426.uniapp.library.model.LibraryBookDetail
import com.anto426.uniapp.library.model.LibraryBookingOverview
import com.anto426.uniapp.library.model.LibraryCatalogPage
import com.anto426.uniapp.library.model.LibraryMovementItem
import com.anto426.uniapp.model.news.NewsFeed
import com.anto426.uniapp.project.model.GitHubProjectSnapshot
import com.anto426.uniapp.updates.model.InstalledAppBuild
import com.anto426.uniapp.updates.model.UniAppUpdateManifest
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
import com.anto426.unisdk.transport.TransportActionResult
import com.anto426.unisdk.transport.TransportBookingRequest
import com.anto426.unisdk.transport.TransportData
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer

// ============================================================================
// 1. Cryptographic Cache Identity & Owner Verification
// ============================================================================

/** Versioned, domain-separated cache addresses; previous hashCode keys are intentionally ignored. */
internal fun profileCacheKey(profileId: String?, key: String): String {
    val owner = profileId?.let { "value:${it.length}:$it" } ?: "absent"
    return "profile-v2-${cacheIdentityDigest(owner)}-${cacheIdentityDigest(key)}"
}

internal fun datasetCacheKey(kind: String, id: String): String =
    "$kind-v2-${cacheIdentityDigest(id)}"

internal expect fun cacheIdentityDigest(value: String): String

/** Fixed sources belong to exactly one career, including an explicitly absent profile id. */
internal fun requireDataRequestOwner(
    activeAccountId: String,
    activeProfileId: String?,
    fixedAccountId: String?,
    fixedProfileId: String?,
) {
    if (fixedAccountId != null &&
        (fixedAccountId != activeAccountId || fixedProfileId != activeProfileId)
    ) {
        throw CancellationException("The data source belongs to a previous account or profile")
    }
}

// ============================================================================
// 2. Unified Cache Policies
// ============================================================================

/** One freshness policy per dataset, shared by memory and encrypted disk storage. */
data class UniCachePolicy(
    val maxAgeMillis: Long,
    val retryDelayMillis: Long = 60_000L,
) {
    init {
        require(maxAgeMillis >= 0L)
        require(retryDelayMillis > 0L)
    }
}

object UniCachePolicies {
    val Transport = UniCachePolicy(minutes(2))
    val ConnectedDevices = UniCachePolicy(minutes(5))
    val LibraryMovements = UniCachePolicy(minutes(5))
    val LibrarySpaces = UniCachePolicy(minutes(5))
    val LibraryBookDetail = UniCachePolicy(minutes(10))
    val LibraryCatalog = UniCachePolicy(minutes(5))
    val ExamRounds = UniCachePolicy(minutes(15))
    val ProfessorDashboard = UniCachePolicy(minutes(15))
    val AppUpdate = UniCachePolicy(minutes(15))
    val SurveyCourses = UniCachePolicy(minutes(30))
    val Attendance = UniCachePolicy(minutes(30))
    val SurveyPage = UniCachePolicy(minutes(30))
    val SurveyStatus = UniCachePolicy(minutes(30))
    val News = UniCachePolicy(hours(1))
    val Taxes = UniCachePolicy(hours(2))
    val StudyPlan = UniCachePolicy(hours(6))
    val Career = UniCachePolicy(hours(6))
    val StudentDetails = UniCachePolicy(hours(6))
    val GitHubProject = UniCachePolicy(hours(6))
    val Contacts = UniCachePolicy(hours(24))
    val ProfileImage = UniCachePolicy(hours(24 * 7))
    val CourseSyllabus = UniCachePolicy(hours(24 * 7))
    val TicketImage = UniCachePolicy(hours(24 * 30))

    private fun minutes(value: Long): Long = value * 60_000L
    private fun hours(value: Long): Long = value * 60L * 60_000L
}

// ============================================================================
// 3. Reactive Dataset State & Snapshot
// ============================================================================

/** Values remain visible during refresh; errors belong to their own dataset. */
data class UniDataValue<T : Any>(
    val value: T? = null,
    val refreshing: Boolean = false,
    val error: Throwable? = null,
    val revision: Long = 0,
) {
    val resolved: Boolean get() = value != null || error != null
}

class UniDataSnapshot internal constructor(private val values: Map<String, UniDataValue<*>>) {
    val resolved: Boolean get() = values.values.all { it.resolved }
    val refreshing: Boolean get() = values.values.any { it.refreshing }
    val firstError: Throwable? get() = values.values.firstNotNullOfOrNull { it.error }
    fun throwIfFailed() { firstError?.let { throw it } }
    internal val version get() = values.mapValues { (_, state) -> Triple(state.revision, state.value != null, state.error != null) }
    fun <T : Any> value(request: UniDatasetKey<T>): T? = state(request).value
    fun <T : Any> require(request: UniDatasetKey<T>): T =
        state(request).let { it.value ?: throw (it.error ?: IllegalStateException("Dataset ${request.id} is not ready")) }
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> state(request: UniDatasetKey<T>): UniDataValue<T> =
        values[request.id] as? UniDataValue<T> ?: UniDataValue()
}

// ============================================================================
// 4. Declarative Dataset Keys
// ============================================================================

/** A dataset has one identity, cache policy and loader, independently of its consumers. */
class UniDatasetKey<T : Any> internal constructor(
    val id: String,
    internal val cacheKey: String? = null,
    internal val serializer: KSerializer<T>? = null,
    internal val policy: UniCachePolicy? = null,
    internal val load: suspend UniGateway.(Boolean) -> T,
)

object UniDatasets {
    val Career: UniDatasetKey<com.anto426.unisdk.model.Career> =
        UniDatasetKey("career", "career-v2", com.anto426.unisdk.model.Career.serializer(), UniCachePolicies.Career, UniGateway::loadCareer)
    val Professor: UniDatasetKey<ProfessorDashboard> =
        UniDatasetKey("professor", "professor-dashboard", ProfessorDashboard.serializer(), UniCachePolicies.ProfessorDashboard, UniGateway::loadProfessorDashboard)
    val StudyPlan: UniDatasetKey<com.anto426.unisdk.model.StudyPlan> =
        UniDatasetKey("study-plan", "study-plan", com.anto426.unisdk.model.StudyPlan.serializer(), UniCachePolicies.StudyPlan, UniGateway::loadStudyPlan)
    val Exams: UniDatasetKey<List<ExamRound>> =
        UniDatasetKey("exam-rounds", "exam-rounds", ListSerializer(ExamRound.serializer()), UniCachePolicies.ExamRounds, UniGateway::loadExamRounds)
    val Taxes: UniDatasetKey<com.anto426.unisdk.model.Taxes> =
        UniDatasetKey("taxes", "taxes", com.anto426.unisdk.model.Taxes.serializer(), UniCachePolicies.Taxes, UniGateway::loadTaxes)
    val Student: UniDatasetKey<StudentDetails> =
        UniDatasetKey("student", "student-details-v2", StudentDetails.serializer(), UniCachePolicies.StudentDetails, UniGateway::loadStudentDetails)
    val Devices: UniDatasetKey<List<ConnectedDevice>> =
        UniDatasetKey("devices", "connected-devices", ListSerializer(ConnectedDevice.serializer()), UniCachePolicies.ConnectedDevices, UniGateway::loadConnectedDevices)
    val Attendance: UniDatasetKey<List<AttendanceRecord>> =
        UniDatasetKey("attendance", "attendance", ListSerializer(AttendanceRecord.serializer()), UniCachePolicies.Attendance, UniGateway::loadAttendanceHistory)
    val News: UniDatasetKey<List<UniversityNews>> =
        UniDatasetKey("news", "university-news-v3", ListSerializer(UniversityNews.serializer()), UniCachePolicies.News, UniGateway::loadUniversityNews)
    val Contacts: UniDatasetKey<List<UniversityContact>> =
        UniDatasetKey("contacts", "university-contacts-v2", ListSerializer(UniversityContact.serializer()), UniCachePolicies.Contacts, UniGateway::loadUniversityContacts)
    val Surveys: UniDatasetKey<List<SurveyCourse>> =
        UniDatasetKey("surveys", "survey-courses", ListSerializer(SurveyCourse.serializer()), UniCachePolicies.SurveyCourses, UniGateway::loadSurveyCourses)
    val Transport: UniDatasetKey<TransportData> =
        UniDatasetKey("transport", "transport-data", TransportData.serializer(), UniCachePolicies.Transport, UniGateway::loadTransportData)
    val LibraryMovements: UniDatasetKey<List<LibraryMovementItem>> =
        UniDatasetKey("library-movements", policy = UniCachePolicies.LibraryMovements) { force -> loadLibraryMovements(force) }
    val LibrarySpaces: UniDatasetKey<LibraryBookingOverview> =
        UniDatasetKey("library-spaces", policy = UniCachePolicies.LibrarySpaces) { loadLibraryBookingOverview() }

    fun syllabus(id: String): UniDatasetKey<CourseSyllabus> =
        UniDatasetKey("syllabus/$id", datasetCacheKey("course-syllabus", id), CourseSyllabus.serializer(), UniCachePolicies.CourseSyllabus) { force ->
            loadCourseSyllabus(id, force)
        }

    fun surveyStatus(code: String): UniDatasetKey<Boolean> =
        UniDatasetKey("survey-status/$code", datasetCacheKey("survey-status", code), Boolean.serializer(), UniCachePolicies.SurveyStatus) { force ->
            loadSurveyCompilationStatus(code, force)
        }

    fun surveyPage(course: String, tags: String): UniDatasetKey<SurveyForm> =
        UniDatasetKey("survey-page/${course.length}:$course$tags", policy = UniCachePolicies.SurveyPage) {
            loadSurveyFirstPage(course, tags)
        }

    fun portrait(source: String): UniDatasetKey<ByteArray> =
        UniDatasetKey("portrait/$source", policy = UniCachePolicies.ProfileImage) { force ->
            loadProfileImage(source, force)
        }

    fun ticketImage(bookingId: String, source: String): UniDatasetKey<ByteArray> =
        UniDatasetKey("ticket-image/$bookingId", policy = UniCachePolicies.TicketImage) { force ->
            loadTransportTicketImage(bookingId, source, force)
        }

    fun libraryBook(bookId: String): UniDatasetKey<LibraryBookDetail> =
        UniDatasetKey("library-book/$bookId", policy = UniCachePolicies.LibraryBookDetail) {
            loadLibraryBook(bookId)
        }

    fun libraryCatalog(query: String, page: Int): UniDatasetKey<LibraryCatalogPage> =
        UniDatasetKey("library-search/$page:${query.trim().lowercase()}", policy = UniCachePolicies.LibraryCatalog) {
            searchLibraryBooks(query, page)
        }
}

// ============================================================================
// 5. UniCacheEngine: Single Reactive Cache, Policy & Invalidation Engine
// ============================================================================

/** One instance per authenticated account/profile generation. Only this class starts shared data loads. */
class UniCacheEngine(
    private val source: UniGateway,
    parentScope: CoroutineScope,
    val generation: Long = 0,
    concurrency: Int = 4,
    private val nowMillis: () -> Long = { currentEpochMillis() },
) : UniGateway by source {
    private val scope = CoroutineScope(parentScope.coroutineContext + SupervisorJob(parentScope.coroutineContext[Job]))
    private data class PreparedNews(val source: List<UniversityNews>, val feed: NewsFeed)
    private val preparedNews = MutableStateFlow<PreparedNews?>(null)
    private val newsPreparation = Mutex()
    internal val newsFeed: NewsFeed? get() = preparedNews.value?.feed

    internal suspend fun prepareNews(news: List<UniversityNews>, dispatcher: CoroutineDispatcher): NewsFeed {
        preparedNews.value?.takeIf { it.source === news }?.let { return it.feed }
        return withContext(dispatcher) {
            newsPreparation.withLock {
                preparedNews.value?.takeIf { it.source == news }?.let { previous ->
                    preparedNews.value = PreparedNews(news, previous.feed)
                    previous.feed
                } ?: run {
                    val items = news.toNewsItems()
                    NewsFeed(items, items.groupBy { it.category.lowercase() }).also {
                        preparedNews.value = PreparedNews(news, it)
                    }
                }
            }
        }
    }

    private val guard = Mutex()
    private val network = Semaphore(concurrency)
    private val entries = linkedMapOf<String, Entry<*>>()
    private var ticketArchiveJob: Job? = null
    private var closed = false
    private val scheduleRevision = MutableStateFlow(0L)

    private class Entry<T : Any>(val request: UniDatasetKey<T>) {
        val state = MutableStateFlow(UniDataValue<T>())
        var job: CompletableDeferred<T>? = null
        var leases = 0
        var visibleConsumers = 0
        var savedAt: Long? = null
        var retryAt: Long? = null
        var invalidated = false
        var forcePending = false
        var forcing = false
        fun clear() { state.value = UniDataValue(revision = state.value.revision + 1) }
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun <T : Any> acquire(request: UniDatasetKey<T>): Entry<T> = guard.withLock {
        if (closed) throw CancellationException("Data owner closed")
        val entry = (entries[request.id] as? Entry<T>) ?: Entry(request).also { entries[request.id] = it }
        entry.leases += 1
        scheduleRevision.value += 1
        entry
    }

    private suspend fun release(entry: Entry<*>) = withContext(NonCancellable) {
        guard.withLock {
            entry.leases -= 1
            scheduleRevision.value += 1
            trimIdleEntries()
        }
    }

    private fun trimIdleEntries() {
        while (entries.size > 64) {
            val idle = entries.entries.firstOrNull { it.value.leases == 0 && it.value.job == null } ?: break
            entries.remove(idle.key)
        }
    }

    fun observe(
        requests: List<UniDatasetKey<*>>,
        visible: Flow<Boolean> = flowOf(true),
    ): Flow<UniDataSnapshot> = flow {
        val acquired = mutableListOf<Entry<*>>()
        var visibleNow = false
        try {
            for (request in requests.distinctBy { it.id }) {
                val entry = acquire(request)
                acquired += entry
                start(entry, force = false)
            }
            coroutineScope {
                val visibility = launch {
                    visible.distinctUntilChanged().collect { active ->
                        guard.withLock {
                            if (active != visibleNow) {
                                acquired.forEach { it.visibleConsumers += if (active) 1 else -1 }
                                visibleNow = active
                                scheduleRevision.value += 1
                            }
                        }
                        if (active) acquired.forEach { start(it, force = false) }
                    }
                }
                try {
                    if (acquired.isEmpty()) emit(UniDataSnapshot(emptyMap()))
                    else emitAll(
                        combine(acquired.map { it.state }) { values ->
                            UniDataSnapshot(acquired.mapIndexed { index, entry -> entry.request.id to values[index] }.toMap())
                        },
                    )
                } finally { visibility.cancel() }
            }
        } finally {
            withContext(NonCancellable) {
                guard.withLock { if (visibleNow) acquired.forEach { it.visibleConsumers -= 1 } }
                acquired.forEach { release(it) }
            }
        }
    }

    fun refresh(requests: List<UniDatasetKey<*>>, force: Boolean = false) {
        scope.launch { refreshRequests(requests, force, invalidated = false) }
    }

    private suspend fun refreshRequests(requests: List<UniDatasetKey<*>>, force: Boolean, invalidated: Boolean) {
        for (request in requests.distinctBy { it.id }) {
            val entry = acquire(request)
            try { start(entry, force, invalidated) }
            finally { release(entry) }
        }
    }

    internal suspend fun invalidate(requests: List<UniDatasetKey<*>>) {
        for (request in requests.distinctBy { it.id }) {
            val entry = acquire(request)
            try {
                val needed = guard.withLock {
                    entry.invalidated = true
                    entry.retryAt = null
                    entry.leases > 1 || entry.job != null
                }
                if (needed) start(entry, force = true, invalidated = true)
            } finally { release(entry) }
        }
    }

    fun preload(isProfessor: Boolean) = refresh(
        if (isProfessor) listOf(UniDatasets.Professor, UniDatasets.News, UniDatasets.Contacts)
        else listOf(
            UniDatasets.Student,
            UniDatasets.Career,
            UniDatasets.StudyPlan,
            UniDatasets.Exams,
            UniDatasets.Taxes,
            UniDatasets.News,
            UniDatasets.Surveys,
            UniDatasets.Attendance,
            UniDatasets.Contacts,
            UniDatasets.Transport,
            UniDatasets.Devices,
        ),
        force = false,
    )

    private val mutablePortrait = MutableStateFlow<ByteArray?>(null)
    val portrait = mutablePortrait.asStateFlow()
    private var portraitStarted = false
    private var portraitSource: String? = null

    fun startPortrait(account: UniAccountSummary?) {
        scope.launch {
            guard.withLock {
                if (portraitStarted) return@launch
                portraitStarted = true
            }
            val sources = if (account?.isProfessor == true) flowOf(account.photoUrl)
            else observe(listOf(UniDatasets.Student)).map { snapshot ->
                val student = snapshot.state(UniDatasets.Student)
                student.value?.photoUrl?.takeIf(String::isNotBlank) ?: account?.photoUrl
            }
            sources.distinctUntilChanged().collectLatest { source ->
                portraitSource = source
                if (source.isNullOrBlank()) {
                    mutablePortrait.value = null
                } else {
                    observe(listOf(UniDatasets.portrait(source))).collect { snapshot ->
                        snapshot.value(UniDatasets.portrait(source))?.let { bytes ->
                            val shared = (this@UniCacheEngine.source as? UniPortraitSharer)
                                ?.sharePortrait(source, bytes) ?: bytes
                            guard.withLock {
                                currentCoroutineContext().ensureActive()
                                if (!closed) mutablePortrait.value = shared
                            }
                        }
                    }
                }
            }
        }
    }

    fun refreshPortrait() {
        portraitSource?.takeIf(String::isNotBlank)?.let { refresh(listOf(UniDatasets.portrait(it)), force = true) }
    }

    private fun dueIn(entry: Entry<*>): Long {
        val now = nowMillis()
        entry.retryAt?.let { return (it - now).coerceAtLeast(0) }
        if (entry.invalidated || !entry.state.value.resolved) return 0
        val savedAt = entry.savedAt ?: return 0
        val age = now - savedAt
        if (age < 0) return 0
        val lifetime = entry.request.policy?.maxAgeMillis ?: Long.MAX_VALUE
        return (lifetime - age).coerceAtLeast(0)
    }

    /** One scheduler while the app is visible. It sleeps until observed data actually expires. */
    suspend fun maintainFreshData(): Nothing {
        scheduleRevision.collectLatest {
            val wait = guard.withLock {
                entries.values.filter { it.visibleConsumers > 0 && it.job == null && it.request.policy != null }
                    .minOfOrNull(::dueIn)
            } ?: return@collectLatest awaitCancellation()
            delay(wait.coerceAtLeast(1))
            val due = guard.withLock {
                entries.values.filter { it.visibleConsumers > 0 && it.job == null && dueIn(it) == 0L }.map { it.request }
            }
            refreshRequests(due, force = false, invalidated = false)
            awaitCancellation()
        }
        awaitCancellation()
    }

    private suspend fun <T : Any> start(
        entry: Entry<T>,
        force: Boolean,
        invalidated: Boolean = false,
    ): Deferred<T>? = guard.withLock {
        if (closed) throw CancellationException("Data owner closed")
        entry.job?.let {
            if (invalidated || (force && !entry.forcing)) entry.forcePending = true
            return@withLock it
        }
        if (!force && !invalidated && dueIn(entry) > 0) return@withLock null
        val effectiveForce = force || entry.invalidated
        if (entry.request.id == UniDatasets.Transport.id) {
            ticketArchiveJob?.cancel()
            ticketArchiveJob = null
        }
        entry.forcing = effectiveForce
        entry.invalidated = false
        entry.forcePending = false
        entry.state.value = entry.state.value.copy(refreshing = true, error = null)
        val completion = CompletableDeferred<T>(scope.coroutineContext[Job])
        entry.job = completion
        scope.launch {
            try {
                if (entry.state.value.value == null && source is PortalUniGateway) {
                    source.readCached(entry.request)?.let { cached ->
                        guard.withLock {
                            currentCoroutineContext().ensureActive()
                            if (closed) throw CancellationException("Data owner closed")
                            entry.state.value = entry.state.value.copy(value = cached.value, revision = entry.state.value.revision + 1)
                            entry.savedAt = cached.savedAtMillis
                        }
                    }
                }
                var mustRefresh = effectiveForce
                while (true) {
                    guard.withLock {
                        mustRefresh = mustRefresh || entry.forcePending
                        entry.forcePending = false
                        entry.forcing = mustRefresh
                    }
                    val result = try {
                        Result.success(network.withPermit { entry.request.load(source, mustRefresh) })
                    } catch (error: CancellationException) { throw error }
                    catch (error: Throwable) { Result.failure(error) }
                    val savedAt = if (result.isSuccess && source is PortalUniGateway) {
                        try { source.readCached(entry.request)?.savedAtMillis }
                        catch (error: CancellationException) { throw error }
                        catch (_: Exception) { null }
                    } else null
                    val again = guard.withLock {
                        currentCoroutineContext().ensureActive()
                        if (closed) throw CancellationException("Data owner closed")
                        if (entry.forcePending) {
                            mustRefresh = true
                            true
                        } else {
                            entry.state.value = entry.state.value.copy(
                                value = result.getOrNull() ?: entry.state.value.value,
                                error = result.exceptionOrNull(),
                                refreshing = false,
                                revision = entry.state.value.revision + 1,
                            )
                            entry.job = null
                            entry.forcing = false
                            entry.invalidated = false
                            if (result.isSuccess) {
                                entry.savedAt = savedAt ?: nowMillis()
                                val age = nowMillis() - entry.savedAt!!
                                entry.retryAt = if (age >= (entry.request.policy?.maxAgeMillis ?: Long.MAX_VALUE)) {
                                    nowMillis() + (entry.request.policy?.retryDelayMillis ?: 60_000L)
                                } else null
                            } else entry.retryAt = nowMillis() + (entry.request.policy?.retryDelayMillis ?: 60_000L)
                            result.fold(completion::complete, completion::completeExceptionally)
                            trimIdleEntries()
                            false
                        }
                    }
                    if (!again) {
                        (result.getOrNull() as? TransportData)?.takeIf {
                            entry.request.id == UniDatasets.Transport.id
                        }?.let { data ->
                            guard.withLock {
                                ticketArchiveJob?.cancel()
                                ticketArchiveJob = scope.launch {
                                    for (booking in data.bookings) {
                                        ensureActive()
                                        if (booking.ticketUrl.isBlank()) continue
                                        try {
                                            network.withPermit {
                                                source.loadTransportTicketImage(booking.id, booking.ticketUrl)
                                            }
                                        } catch (error: CancellationException) {
                                            throw error
                                        } catch (_: Exception) {
                                            // Retried on next refresh or detail view.
                                        }
                                    }
                                }
                            }
                        }
                        break
                    }
                }
            } catch (error: CancellationException) {
                if (currentCoroutineContext().isActive) {
                    val failure = IllegalStateException()
                    guard.withLock {
                        if (!closed && entry.job === completion) {
                            entry.state.value = entry.state.value.copy(
                                error = failure,
                                refreshing = false,
                                revision = entry.state.value.revision + 1,
                            )
                            entry.retryAt = nowMillis() + (entry.request.policy?.retryDelayMillis ?: 60_000L)
                        }
                    }
                    completion.completeExceptionally(failure)
                } else completion.cancel(error)
            } catch (error: Throwable) {
                completion.completeExceptionally(error)
            } finally {
                withContext(NonCancellable) {
                    guard.withLock {
                        if (entry.job === completion) {
                            entry.job = null
                            entry.forcing = false
                            entry.state.value = entry.state.value.copy(refreshing = false)
                            scheduleRevision.value += 1
                        }
                    }
                }
            }
        }
        completion
    }

    internal suspend fun <T : Any> get(request: UniDatasetKey<T>, force: Boolean): T {
        val entry = acquire(request)
        return try {
            val job = start(entry, force)
            job?.await() ?: entry.state.value.let { it.value ?: throw checkNotNull(it.error) }
        } finally { release(entry) }
    }

    suspend fun close() {
        guard.withLock {
            closed = true
            ticketArchiveJob?.cancel()
            scope.cancel()
            entries.values.forEach { it.clear() }
            entries.clear()
            preparedNews.value = null
            mutablePortrait.value = null
        }
    }

    override suspend fun loadCareer(forceRefresh: Boolean) = get(UniDatasets.Career, forceRefresh)
    override suspend fun loadProfessorDashboard(forceRefresh: Boolean) = get(UniDatasets.Professor, forceRefresh)
    override suspend fun loadStudyPlan(forceRefresh: Boolean) = get(UniDatasets.StudyPlan, forceRefresh)
    override suspend fun loadExamRounds(forceRefresh: Boolean) = get(UniDatasets.Exams, forceRefresh)
    override suspend fun loadTaxes(forceRefresh: Boolean) = get(UniDatasets.Taxes, forceRefresh)
    override suspend fun loadStudentDetails(forceRefresh: Boolean) = get(UniDatasets.Student, forceRefresh)
    override suspend fun loadConnectedDevices(forceRefresh: Boolean) = get(UniDatasets.Devices, forceRefresh)
    override suspend fun loadAttendanceHistory(forceRefresh: Boolean) = get(UniDatasets.Attendance, forceRefresh)
    override suspend fun loadUniversityNews(forceRefresh: Boolean) = get(UniDatasets.News, forceRefresh)
    override suspend fun loadUniversityContacts(forceRefresh: Boolean) = get(UniDatasets.Contacts, forceRefresh)
    override suspend fun loadSurveyCourses(forceRefresh: Boolean) = get(UniDatasets.Surveys, forceRefresh)
    override suspend fun loadTransportTicketImage(bookingId: String, source: String, forceRefresh: Boolean) =
        get(UniDatasets.ticketImage(bookingId, source), forceRefresh)

    override suspend fun loadTransportData(forceRefresh: Boolean) = get(UniDatasets.Transport, forceRefresh)
    override suspend fun loadCourseSyllabus(adsceId: String, forceRefresh: Boolean) = get(UniDatasets.syllabus(adsceId), forceRefresh)
    override suspend fun loadProfileImage(source: String, forceRefresh: Boolean) = get(UniDatasets.portrait(source), forceRefresh)
    override suspend fun loadSurveyCompilationStatus(adCod: String, forceRefresh: Boolean) = get(UniDatasets.surveyStatus(adCod), forceRefresh)
    override suspend fun loadSurveyFirstPage(courseId: String, tagList: String) = get(UniDatasets.surveyPage(courseId, tagList), false)

    override suspend fun searchLibraryBooks(query: String, page: Int) = get(UniDatasets.libraryCatalog(query, page), false)
    override suspend fun loadLibraryBook(id: String) = get(UniDatasets.libraryBook(id), false)
    override suspend fun loadLibraryMovements(forceRefresh: Boolean) = get(UniDatasets.LibraryMovements, forceRefresh)
    override suspend fun loadLibraryBookingOverview() = get(UniDatasets.LibrarySpaces, false)

    override suspend fun bookExamRound(round: ExamRound) = source.bookExamRound(round).also { invalidate(listOf(UniDatasets.Exams)) }
    override suspend fun cancelExamRound(round: ExamRound) = source.cancelExamRound(round).also { invalidate(listOf(UniDatasets.Exams)) }
    override suspend fun disconnectDevice(targetToken: String) = source.disconnectDevice(targetToken).also { invalidate(listOf(UniDatasets.Devices)) }
    override suspend fun disconnectAllOtherDevices() = source.disconnectAllOtherDevices().also { invalidate(listOf(UniDatasets.Devices)) }
    override suspend fun registerAttendance(qrCode: String, deviceLatitude: Double?, deviceLongitude: Double?, deviceAccuracyMeters: Double?) =
        source.registerAttendance(qrCode, deviceLatitude, deviceLongitude, deviceAccuracyMeters).also { invalidate(listOf(UniDatasets.Attendance)) }
    override suspend fun saveSurvey(courseId: String, request: SurveySaveRequest) = source.saveSurvey(courseId, request).also {
        val surveyDetails = guard.withLock {
            entries.values.map { it.request }.filter { it.id.startsWith("survey-status/") || it.id.startsWith("survey-page/") }
        }
        invalidate(listOf(UniDatasets.Surveys, UniDatasets.Exams) + surveyDetails)
    }
    override suspend fun bookTransport(request: TransportBookingRequest) = source.bookTransport(request).also { invalidate(listOf(UniDatasets.Transport)) }
    override suspend fun deleteTransportBooking(bookingId: String) = source.deleteTransportBooking(bookingId).also {
        val ticketEntries = guard.withLock {
            entries.values.map { it.request }.filter { it.id == "ticket-image/$bookingId" }
        }
        invalidate(listOf(UniDatasets.Transport) + ticketEntries)
    }

    override suspend fun confirmLibraryBookRequest(draftId: String) = source.confirmLibraryBookRequest(draftId).also {
        val bookEntries = guard.withLock {
            entries.values.map { it.request }.filter { it.id.startsWith("library-book/") }
        }
        invalidate(listOf(UniDatasets.LibraryMovements) + bookEntries)
    }
    override suspend fun cancelLibraryMovement(movementToken: String) = source.cancelLibraryMovement(movementToken).also {
        invalidate(listOf(UniDatasets.LibraryMovements))
    }
    override suspend fun renewLibraryMovement(movementToken: String) = source.renewLibraryMovement(movementToken).also {
        invalidate(listOf(UniDatasets.LibraryMovements))
    }
    override suspend fun bookLibrarySpace(
        calendarId: String,
        slotIds: List<String>,
        note: String,
        deviceAddresses: Map<String, String>,
    ) = source.bookLibrarySpace(calendarId, slotIds, note, deviceAddresses).also { outcome ->
        if (outcome.confirmed) invalidate(listOf(UniDatasets.LibrarySpaces))
    }
    override suspend fun cancelLibrarySpaceBooking(bookingToken: String) = source.cancelLibrarySpaceBooking(bookingToken).also {
        invalidate(listOf(UniDatasets.LibrarySpaces))
    }
}

/** ViewModels share the active owner; isolated source fakes use the same observable contract. */
fun UniGateway.sharedEngine(scope: CoroutineScope): UniCacheEngine =
    when (this) {
        is UniCacheEngine -> this
        is UniRepository -> this.engine
        else -> UniCacheEngine(this, scope)
    }

fun UniCacheEngine.observeIn(
    scope: CoroutineScope,
    requests: List<UniDatasetKey<*>>,
    partial: Boolean = false,
    subscriptions: StateFlow<Int>? = null,
    render: suspend (UniDataSnapshot) -> Unit,
) = scope.launch {
    observe(requests, subscriptions?.map { it > 0 } ?: flowOf(true))
        .filter { partial || it.resolved }
        .distinctUntilChangedBy { it.version }
        .collect { render(it) }
}
