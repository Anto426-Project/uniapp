package com.anto426.uniapp.data.runtime

import com.anto426.uniapp.data.SessionUniAppDataSource
import com.anto426.uniapp.data.UniAppDataSource
import com.anto426.unisdk.backend.model.ExamRoundData
import com.anto426.unisdk.backend.model.SurveySaveRequest
import com.anto426.unisdk.transport.TransportBookingRequest
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit

/** One instance per authenticated account/profile. Only this class starts shared data loads. */
class UniAppDataCoordinator(
    private val source: UniAppDataSource,
    parentScope: CoroutineScope,
    val generation: Long = 0,
    concurrency: Int = 4,
    private val nowMillis: () -> Long = { com.anto426.unisdk.platform.currentEpochMillis() },
) : UniAppDataSource by source {
    private val scope = CoroutineScope(parentScope.coroutineContext + SupervisorJob(parentScope.coroutineContext[Job]))
    private val guard = Mutex()
    private val network = Semaphore(concurrency)
    private val entries = linkedMapOf<String, Entry<*>>()
    private var closed = false
    private val scheduleRevision = MutableStateFlow(0L)

    private class Entry<T : Any>(val request: UniAppDataRequest<T>) {
        val state = MutableStateFlow(UniAppDataValue<T>())
        var job: CompletableDeferred<T>? = null
        var leases = 0
        var visibleConsumers = 0
        var savedAt: Long? = null
        var retryAt: Long? = null
        var invalidated = false
        var forcePending = false
        var forcing = false
        fun clear() { state.value = UniAppDataValue(revision = state.value.revision + 1) }
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun <T : Any> acquire(request: UniAppDataRequest<T>): Entry<T> = guard.withLock {
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
        requests: List<UniAppDataRequest<*>>,
        visible: Flow<Boolean> = flowOf(true),
    ): Flow<UniAppDataSnapshot> = flow {
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
                    if (acquired.isEmpty()) emit(UniAppDataSnapshot(emptyMap()))
                    else emitAll(combine(acquired.map { it.state }) { values ->
                        UniAppDataSnapshot(acquired.mapIndexed { index, entry -> entry.request.id to values[index] }.toMap())
                    })
                } finally { visibility.cancel() }
            }
        } finally {
            withContext(NonCancellable) {
                guard.withLock { if (visibleNow) acquired.forEach { it.visibleConsumers -= 1 } }
                acquired.forEach { release(it) }
            }
        }
    }

    fun refresh(requests: List<UniAppDataRequest<*>>, force: Boolean = false) {
        scope.launch { refreshRequests(requests, force, invalidated = false) }
    }

    private suspend fun refreshRequests(requests: List<UniAppDataRequest<*>>, force: Boolean, invalidated: Boolean) {
        for (request in requests.distinctBy { it.id }) {
            val entry = acquire(request)
            try { start(entry, force, invalidated) }
            finally { release(entry) }
        }
    }

    // A mutation invalidates an already forced request too: its response may predate the change.
    private suspend fun invalidate(requests: List<UniAppDataRequest<*>>) {
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
        if (isProfessor) listOf(UniAppDataRequests.Professor, UniAppDataRequests.News, UniAppDataRequests.Contacts)
        else listOf(UniAppDataRequests.Student, UniAppDataRequests.Career, UniAppDataRequests.StudyPlan,
            UniAppDataRequests.Exams, UniAppDataRequests.Taxes, UniAppDataRequests.News,
            UniAppDataRequests.Surveys, UniAppDataRequests.Attendance, UniAppDataRequests.Contacts,
            UniAppDataRequests.Transport, UniAppDataRequests.Devices),
        force = false,
    )

    private val mutablePortrait = MutableStateFlow<ByteArray?>(null)
    val portrait = mutablePortrait.asStateFlow()
    private var portraitStarted = false
    private var portraitSource: String? = null

    fun startPortrait(account: com.anto426.uniapp.account.model.UniAccountSummary?) {
        scope.launch {
            guard.withLock {
                if (portraitStarted) return@launch
                portraitStarted = true
            }
            val sources = if (account?.isProfessor == true) flowOf(account.photoUrl)
            else observe(listOf(UniAppDataRequests.Student)).map { snapshot ->
                val student = snapshot.state(UniAppDataRequests.Student)
                student.value?.photoUrl?.takeIf(String::isNotBlank) ?: account?.photoUrl
            }
            sources.distinctUntilChanged().collectLatest { source ->
                portraitSource = source
                if (source.isNullOrBlank()) {
                    mutablePortrait.value = null
                } else {
                    observe(listOf(UniAppDataRequests.portrait(source))).collect { snapshot ->
                        snapshot.value(UniAppDataRequests.portrait(source))?.let { bytes ->
                            val shared = if (this@UniAppDataCoordinator.source is SessionUniAppDataSource)
                                this@UniAppDataCoordinator.source.sharePortrait(source, bytes) else bytes
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
        portraitSource?.takeIf(String::isNotBlank)?.let { refresh(listOf(UniAppDataRequests.portrait(it)), force = true) }
    }

    private fun dueIn(entry: Entry<*>): Long {
        val now = nowMillis()
        entry.retryAt?.let { return (it - now).coerceAtLeast(0) }
        if (entry.invalidated || !entry.state.value.resolved) return 0
        val savedAt = entry.savedAt ?: return 0
        val age = now - savedAt
        if (age < 0) return 0 // A clock correction must not make cached data immortal.
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
            awaitCancellation() // Completion/observation changes reschedule the next deadline.
        }
        awaitCancellation()
    }

    private suspend fun <T : Any> start(
        entry: Entry<T>, force: Boolean, invalidated: Boolean = false,
    ): Deferred<T>? = guard.withLock {
        if (closed) throw CancellationException("Data owner closed")
        entry.job?.let {
            if (invalidated || (force && !entry.forcing)) entry.forcePending = true
            return@withLock it
        }
        if (!force && !invalidated && dueIn(entry) > 0) return@withLock null
        val effectiveForce = force || entry.invalidated
        entry.forcing = effectiveForce
        entry.invalidated = false
        entry.forcePending = false
        entry.state.value = entry.state.value.copy(refreshing = true, error = null)
        val completion = CompletableDeferred<T>(scope.coroutineContext[Job])
        entry.job = completion
        scope.launch {
            try {
                if (entry.state.value.value == null && source is SessionUniAppDataSource) {
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
                    val savedAt = if (result.isSuccess && source is SessionUniAppDataSource) {
                        try { source.readCached(entry.request)?.savedAtMillis }
                        catch (error: CancellationException) { throw error }
                        catch (_: Exception) { null }
                    } else null
                    val again = guard.withLock {
                        currentCoroutineContext().ensureActive()
                        if (closed) throw CancellationException("Data owner closed")
                        if (entry.forcePending) {
                            // Do not publish a result known to predate a completed mutation.
                            mustRefresh = true
                            true
                        } else {
                            entry.state.value = entry.state.value.copy(
                                value = result.getOrNull() ?: entry.state.value.value,
                                error = result.exceptionOrNull(), refreshing = false,
                                revision = entry.state.value.revision + 1,
                            )
                            // Completion and removal are atomic with respect to a new refresh.
                            entry.job = null
                            entry.forcing = false
                            entry.invalidated = false
                            if (result.isSuccess) {
                                entry.savedAt = savedAt ?: nowMillis()
                                entry.retryAt = null
                            } else entry.retryAt = nowMillis() + (entry.request.policy?.retryDelayMillis ?: 60_000L)
                            scheduleRevision.value += 1
                            result.fold(completion::complete, completion::completeExceptionally)
                            trimIdleEntries()
                            false
                        }
                    }
                    if (!again) break
                }
            } catch (error: CancellationException) {
                completion.cancel(error)
            } catch (error: Throwable) {
                completion.completeExceptionally(error)
            } finally {
                withContext(NonCancellable) {
                    guard.withLock {
                        if (entry.job === completion) {
                            entry.job = null
                            entry.forcing = false
                            entry.state.value = entry.state.value.copy(refreshing = false)
                        }
                    }
                }
            }
        }
        completion
    }

    private suspend fun <T : Any> get(request: UniAppDataRequest<T>, force: Boolean): T {
        val entry = acquire(request)
        return try {
            val job = start(entry, force)
            job?.await() ?: entry.state.value.let { it.value ?: throw checkNotNull(it.error) }
        } finally { release(entry) }
    }

    suspend fun close() {
        guard.withLock {
            closed = true
            scope.cancel()
            entries.values.forEach { it.clear() }
            entries.clear()
            mutablePortrait.value = null
        }
    }

    override suspend fun loadCareer(forceRefresh: Boolean) = get(UniAppDataRequests.Career, forceRefresh)
    override suspend fun loadProfessorDashboard(forceRefresh: Boolean) = get(UniAppDataRequests.Professor, forceRefresh)
    override suspend fun loadStudyPlan(forceRefresh: Boolean) = get(UniAppDataRequests.StudyPlan, forceRefresh)
    override suspend fun loadExamRounds(forceRefresh: Boolean) = get(UniAppDataRequests.Exams, forceRefresh)
    override suspend fun loadTaxes(forceRefresh: Boolean) = get(UniAppDataRequests.Taxes, forceRefresh)
    override suspend fun loadStudentDetails(forceRefresh: Boolean) = get(UniAppDataRequests.Student, forceRefresh)
    override suspend fun loadConnectedDevices(forceRefresh: Boolean) = get(UniAppDataRequests.Devices, forceRefresh)
    override suspend fun loadAttendanceHistory(forceRefresh: Boolean) = get(UniAppDataRequests.Attendance, forceRefresh)
    override suspend fun loadUniversityNews(forceRefresh: Boolean) = get(UniAppDataRequests.News, forceRefresh)
    override suspend fun loadUniversityContacts(forceRefresh: Boolean) = get(UniAppDataRequests.Contacts, forceRefresh)
    override suspend fun loadSurveyCourses(forceRefresh: Boolean) = get(UniAppDataRequests.Surveys, forceRefresh)
    override suspend fun loadTransportData(forceRefresh: Boolean) = get(UniAppDataRequests.Transport, forceRefresh)
    override suspend fun loadCourseSyllabus(adsceId: String, forceRefresh: Boolean) = get(UniAppDataRequests.syllabus(adsceId), forceRefresh)
    override suspend fun loadProfileImage(source: String, forceRefresh: Boolean) = get(UniAppDataRequests.portrait(source), forceRefresh)
    override suspend fun loadSurveyCompilationStatus(adCod: String, forceRefresh: Boolean) = get(UniAppDataRequests.surveyStatus(adCod), forceRefresh)
    override suspend fun loadSurveyFirstPage(courseId: String, tagList: String) = get(UniAppDataRequests.surveyPage(courseId, tagList), false)

    override suspend fun bookExamRound(round: ExamRoundData) = source.bookExamRound(round).also { invalidate(listOf(UniAppDataRequests.Exams)) }
    override suspend fun cancelExamRound(round: ExamRoundData) = source.cancelExamRound(round).also { invalidate(listOf(UniAppDataRequests.Exams)) }
    override suspend fun disconnectDevice(targetToken: String) = source.disconnectDevice(targetToken).also { invalidate(listOf(UniAppDataRequests.Devices)) }
    override suspend fun disconnectAllOtherDevices() = source.disconnectAllOtherDevices().also { invalidate(listOf(UniAppDataRequests.Devices)) }
    override suspend fun registerAttendance(qrCode: String, deviceLatitude: Double?, deviceLongitude: Double?, deviceAccuracyMeters: Double?) =
        source.registerAttendance(qrCode, deviceLatitude, deviceLongitude, deviceAccuracyMeters).also { invalidate(listOf(UniAppDataRequests.Attendance)) }
    override suspend fun saveSurvey(courseId: String, request: SurveySaveRequest) = source.saveSurvey(courseId, request).also {
        val surveyDetails = guard.withLock {
            entries.values.map { it.request }.filter { it.id.startsWith("survey-status/") || it.id.startsWith("survey-page/") }
        }
        invalidate(listOf(UniAppDataRequests.Surveys, UniAppDataRequests.Exams) + surveyDetails)
    }
    override suspend fun bookTransport(request: TransportBookingRequest) = source.bookTransport(request).also { invalidate(listOf(UniAppDataRequests.Transport)) }
    override suspend fun deleteTransportBooking(bookingId: String) = source.deleteTransportBooking(bookingId).also { invalidate(listOf(UniAppDataRequests.Transport)) }
}
