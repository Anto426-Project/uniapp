package com.anto426.uniapp.data.runtime

import com.anto426.uniapp.data.FakeUniAppDataSource
import com.anto426.uniapp.data.UniCacheEngine
import com.anto426.uniapp.data.UniCachePolicies
import com.anto426.uniapp.data.UniCachePolicy
import com.anto426.uniapp.data.UniDataSnapshot
import com.anto426.uniapp.data.UniDatasetKey
import com.anto426.uniapp.data.UniDatasets
import com.anto426.uniapp.data.asUniRepository
import com.anto426.uniapp.data.contract.toNewsItems
import com.anto426.uniapp.didactics.presentation.AcademicIdentityViewModel
import com.anto426.uniapp.home.presentation.HomeDashboardViewModel
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.services.presentation.TaxesViewModel
import com.anto426.unisdk.model.StudentDetails
import com.anto426.unisdk.model.Taxes
import com.anto426.unisdk.model.UniversityNews
import com.anto426.unisdk.transport.TransportActionResult
import com.anto426.unisdk.transport.TransportBooking
import com.anto426.unisdk.transport.TransportData
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class ScopedDataRepositoryTest {
    @Test
    fun newlyListedTicketOriginalsAreArchivedWithoutOpeningTheDetailScreen() = runTest {
        val archived = mutableListOf<String>()
        val bookings = listOf("first", "second").map { id ->
            TransportBooking(id, id, "A01", "28/09/2026", "Andata", false, "/vis_prenot.php?id=$id")
        }
        val source = object : FakeUniAppDataSource() {
            override suspend fun loadTransportData(forceRefresh: Boolean) =
                TransportData("Route", bookings = bookings, totalCount = bookings.size)

            override suspend fun loadTransportTicketImage(bookingId: String, source: String, forceRefresh: Boolean): ByteArray {
                archived += bookingId
                return byteArrayOf(1, 2, 3)
            }
        }
        val data = UniCacheEngine(source, backgroundScope)
        data.loadTransportData()
        runCurrent()
        assertEquals(listOf("first", "second"), archived)
        data.close()
    }

    @Test
    fun freshDataSurvivesNavigationAndAutomaticRefreshUntilItsPolicyExpires() = runTest {
        var now = 1_000L
        var calls = 0
        val source = object : FakeUniAppDataSource() {
            override suspend fun loadTaxes(forceRefresh: Boolean): Taxes { calls++; return Taxes("$calls", emptyList()) }
        }
        val data = UniCacheEngine(source, backgroundScope, nowMillis = { now })
        data.loadTaxes()
        repeat(5) {
            data.observe(listOf(UniDatasets.Taxes)).first()
            data.refresh(listOf(UniDatasets.Taxes))
            data.loadTaxes()
        }
        runCurrent()
        assertEquals(1, calls)
        now += UniCachePolicies.Taxes.maxAgeMillis - 1
        data.loadTaxes()
        assertEquals(1, calls)
        now++
        assertEquals("2", data.loadTaxes().dueAmount)
        assertEquals(2, calls)
        data.close()
    }

    @Test
    fun failedAutomaticRefreshUsesTheRetryPolicyInsteadOfRetryingEveryVisit() = runTest {
        var now = 1_000L
        var calls = 0
        val source = object : FakeUniAppDataSource() {
            override suspend fun loadTaxes(forceRefresh: Boolean): Taxes { calls++; error("offline") }
        }
        val data = UniCacheEngine(source, backgroundScope, nowMillis = { now })
        assertFailsWith<IllegalStateException> { data.loadTaxes() }
        repeat(5) { data.refresh(listOf(UniDatasets.Taxes)) }
        runCurrent()
        assertEquals(1, calls)
        now += UniCachePolicies.Taxes.retryDelayMillis
        assertFailsWith<IllegalStateException> { data.loadTaxes() }
        assertEquals(2, calls)
        data.close()
    }

    @Test
    fun cancelledLoaderResolvesItsErrorAndRetriesWhileOwnerIsActive() = runTest {
        var calls = 0
        val request = UniDatasetKey(
            "cancelled",
            policy = UniCachePolicy(100, retryDelayMillis = 50),
        ) { _ ->
            calls++
            if (calls == 1) throw CancellationException("request cancelled")
            42
        }
        val data = UniCacheEngine(
            FakeUniAppDataSource(), backgroundScope, nowMillis = { testScheduler.currentTime },
        )
        val received = mutableListOf<UniDataSnapshot>()
        backgroundScope.launch { data.observe(listOf(request)).collect(received::add) }
        backgroundScope.launch { data.maintainFreshData() }

        runCurrent()
        assertEquals(1, calls)
        assertTrue(received.last().resolved)
        assertNotNull(received.last().firstError)
        advanceTimeBy(49)
        runCurrent()
        assertEquals(1, calls)
        advanceTimeBy(1)
        runCurrent()
        assertEquals(2, calls)
        assertEquals(42, received.last().require(request))
        data.close()
    }

    @Test
    fun foregroundSchedulerRefreshesOnlyObservedExpiredDataAndStopsInBackground() = runTest {
        var calls = 0
        val request = UniDatasetKey("scheduled", policy = UniCachePolicy(100)) { _ -> ++calls }
        val data = UniCacheEngine(FakeUniAppDataSource(), backgroundScope, nowMillis = { testScheduler.currentTime })
        val observer = backgroundScope.launch { data.observe(listOf(request)).collect() }
        val foreground = backgroundScope.launch { data.maintainFreshData() }
        runCurrent()
        assertEquals(1, calls)
        advanceTimeBy(99); runCurrent()
        assertEquals(1, calls)
        advanceTimeBy(1); runCurrent()
        assertEquals(2, calls)
        foreground.cancel()
        advanceTimeBy(1_000); runCurrent()
        assertEquals(2, calls)
        observer.cancel()
        backgroundScope.launch { data.maintainFreshData() }
        runCurrent()
        advanceTimeBy(1_000); runCurrent()
        assertEquals(2, calls)
        data.close()
    }

    @Test
    fun anEarlyWakeUpReschedulesTheVisibleRefresh() = runTest {
        var clockOffset = 0L
        var calls = 0
        val request = UniDatasetKey("scheduled", policy = UniCachePolicy(100)) { _ -> ++calls }
        val data = UniCacheEngine(
            FakeUniAppDataSource(), backgroundScope,
            nowMillis = { testScheduler.currentTime + clockOffset },
        )
        backgroundScope.launch { data.observe(listOf(request)).collect() }
        backgroundScope.launch { data.maintainFreshData() }
        runCurrent()
        assertEquals(1, calls)

        advanceTimeBy(90)
        clockOffset = -40
        advanceTimeBy(10)
        runCurrent()
        assertEquals(1, calls)
        advanceTimeBy(40)
        runCurrent()
        assertEquals(2, calls)
        data.close()
    }

    @Test
    fun mutationLeavesUnobservedDataStaleUntilItIsNeededAgain() = runTest {
        val calls = mutableListOf<Boolean>()
        val source = object : FakeUniAppDataSource() {
            override suspend fun loadTransportData(forceRefresh: Boolean): TransportData {
                calls += forceRefresh
                return super.loadTransportData(forceRefresh)
            }
        }
        val data = UniCacheEngine(source, backgroundScope)
        data.loadTransportData()
        data.deleteTransportBooking("booking")
        runCurrent()
        assertEquals(listOf(false), calls)
        data.loadTransportData()
        assertEquals(listOf(false, true), calls)
        data.close()
    }

    @Test
    fun concurrentConsumersShareOneRequestAndCancellingOneKeepsTheProducer() = runTest {
        val response = CompletableDeferred<Taxes>()
        var calls = 0
        val source = object : FakeUniAppDataSource() {
            override suspend fun loadTaxes(forceRefresh: Boolean): Taxes { calls++; return response.await() }
        }
        val data = UniCacheEngine(source, backgroundScope)
        val first = async { data.loadTaxes() }
        val second = async { data.loadTaxes() }
        runCurrent()
        assertEquals(1, calls)
        first.cancel()
        response.complete(Taxes("42", emptyList()))
        assertEquals("42", second.await().dueAmount)
        assertEquals("42", data.loadTaxes().dueAmount)
        assertEquals(1, calls)
        data.close()
    }

    @Test
    fun forcedRefreshDuringCachedLoadRunsOnceAfterItAndCoalescesFurtherRefreshes() = runTest {
        val initial = CompletableDeferred<Unit>()
        val forced = CompletableDeferred<Unit>()
        val calls = mutableListOf<Boolean>()
        val source = object : FakeUniAppDataSource() {
            override suspend fun loadTaxes(forceRefresh: Boolean): Taxes {
                calls += forceRefresh
                if (forceRefresh) forced.await() else initial.await()
                return Taxes(if (forceRefresh) "20" else "10", emptyList())
            }
        }
        val data = UniCacheEngine(source, backgroundScope)
        val waiting = async { data.loadTaxes() }
        runCurrent()
        repeat(3) { data.refresh(listOf(UniDatasets.Taxes), force = true) }
        runCurrent()
        initial.complete(Unit)
        runCurrent()
        repeat(3) { data.refresh(listOf(UniDatasets.Taxes), force = true) }
        runCurrent()
        assertEquals(listOf(false, true), calls)
        forced.complete(Unit)
        assertEquals("20", waiting.await().dueAmount)
        assertEquals(listOf(false, true), calls)
        data.close()
    }

    @Test
    fun aMutationInvalidatesEvenAForcedRequestAlreadyInFlight() = runTest {
        val initial = CompletableDeferred<Unit>()
        var calls = 0
        var updated = false
        val source = object : FakeUniAppDataSource() {
            override suspend fun loadTransportData(forceRefresh: Boolean): TransportData {
                calls++
                val value = if (updated) "new" else "old"
                if (calls == 1) initial.await()
                return TransportData(value, bookings = emptyList(), totalCount = 0)
            }
            override suspend fun deleteTransportBooking(bookingId: String): TransportActionResult.Completed {
                updated = true
                return TransportActionResult.Completed
            }
        }
        val data = UniCacheEngine(source, backgroundScope)
        val received = mutableListOf<TransportData>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            data.observe(listOf(UniDatasets.Transport)).mapNotNull { it.value(UniDatasets.Transport) }.collect(received::add)
        }
        val waiting = async { data.loadTransportData(forceRefresh = true) }
        runCurrent()
        data.deleteTransportBooking("booking")
        initial.complete(Unit)
        waiting.await()
        runCurrent()
        assertEquals(2, calls)
        assertTrue(received.isNotEmpty())
        assertEquals(source.loadTransportData(true), received.last())
        assertTrue(received.all { it == received.last() }, "A known stale response must not reach screens")
        data.close()
    }

    @Test
    fun refreshFailureKeepsTheLastValueAndReportsTheError() = runTest {
        var fail = false
        val source = object : FakeUniAppDataSource() {
            override suspend fun loadTaxes(forceRefresh: Boolean): Taxes {
                if (fail) error("offline")
                return Taxes("42", emptyList())
            }
        }
        val data = UniCacheEngine(source, backgroundScope)
        data.loadTaxes()
        fail = true
        data.refresh(listOf(UniDatasets.Taxes), force = true)
        runCurrent()
        val snapshot = data.observe(listOf(UniDatasets.Taxes)).first()
        assertEquals("42", snapshot.require(UniDatasets.Taxes).dueAmount)
        assertEquals("offline", snapshot.firstError?.message)
        fail = false
        data.refresh(listOf(UniDatasets.Taxes), force = true)
        runCurrent()
        assertNull(data.observe(listOf(UniDatasets.Taxes)).first().firstError)
        data.close()
    }

    @Test
    fun closeDropsAccountValuesAndRejectsAResponseThatIgnoresCancellation() = runTest {
        val response = CompletableDeferred<Unit>()
        val source = object : FakeUniAppDataSource() {
            override suspend fun loadTaxes(forceRefresh: Boolean): Taxes = withContext(NonCancellable) {
                response.await()
                Taxes("private", emptyList())
            }
        }
        val data = UniCacheEngine(source, backgroundScope)
        val received = mutableListOf<UniDataSnapshot>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            data.observe(listOf(UniDatasets.Taxes)).collect(received::add)
        }
        runCurrent()
        data.close()
        response.complete(Unit)
        runCurrent()
        assertTrue(received.all { it.value(UniDatasets.Taxes) == null })
        assertFailsWith<CancellationException> { data.loadTaxes() }
    }

    @Test
    fun separateOwnersNeverShareTheSameDatasetValue() = runTest {
        fun source(amount: String) = object : FakeUniAppDataSource() {
            override suspend fun loadTaxes(forceRefresh: Boolean) = Taxes(amount, emptyList())
        }
        val first = UniCacheEngine(source("A"), backgroundScope, generation = 1)
        val second = UniCacheEngine(source("B"), backgroundScope, generation = 2)
        assertEquals("A", first.loadTaxes().dueAmount)
        assertEquals("B", second.loadTaxes().dueAmount)
        first.close()
        assertEquals("B", second.loadTaxes().dueAmount)
        second.close()
    }

    @Test
    fun preloadLimitsConcurrentRequestsWithoutSerializingIndependentDatasets() = runTest {
        val gate = CompletableDeferred<Unit>()
        var active = 0
        var maximum = 0
        val requests = (1..10).map { index ->
            UniDatasetKey("test/$index") { _ ->
                active++
                maximum = maxOf(maximum, active)
                try { gate.await(); index } finally { active-- }
            }
        }
        val data = UniCacheEngine(FakeUniAppDataSource(), backgroundScope, concurrency = 3)
        data.refresh(requests)
        runCurrent()
        assertEquals(3, active)
        gate.complete(Unit)
        runCurrent()
        assertEquals(3, maximum)
        assertTrue(data.observe(requests).first().resolved)
        data.close()
    }

    @Test
    fun homeShowsAvailableDataWhileTaxesAreStillLoadingAndSharesUpdatesWithTaxesScreen() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val gate = CompletableDeferred<Unit>()
        var calls = 0
        var amount = "42"
        val source = object : FakeUniAppDataSource() {
            override suspend fun loadStudentDetails(forceRefresh: Boolean) = StudentDetails("Mario Rossi")
            override suspend fun loadTaxes(forceRefresh: Boolean): Taxes { calls++; gate.await(); return Taxes(amount, emptyList()) }
        }
        val data = UniCacheEngine(source, backgroundScope)
        val repo = data.asUniRepository(backgroundScope)
        try {
            val home = HomeDashboardViewModel(repo, emptyList(), preparationDispatcher = Dispatchers.Main)
            val taxes = TaxesViewModel(repo)
            runCurrent()
            assertEquals("Mario Rossi", home.uiState.value.profileName)
            assertEquals(FeatureLoadState.Content, home.uiState.value.loadState)
            assertEquals(FeatureLoadState.Loading, taxes.uiState.value.loadState)
            assertEquals(1, calls)
            gate.complete(Unit)
            runCurrent()
            assertEquals("42", home.uiState.value.dueAmount)
            amount = "84"
            taxes.refresh(force = true)
            runCurrent()
            assertEquals("84", home.uiState.value.dueAmount)
            assertEquals(2, calls)
        } finally { data.close(); Dispatchers.resetMain() }
    }

    @Test
    fun homeAndBadgeSharePortraitBytesAndRefreshTheImageWhenItsSourceChanges() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        var photo = "first"
        var calls = 0
        val source = object : FakeUniAppDataSource() {
            override suspend fun loadStudentDetails(forceRefresh: Boolean) = StudentDetails("Mario", photoUrl = photo)
            override suspend fun loadProfileImage(source: String, forceRefresh: Boolean): ByteArray { calls++; return source.encodeToByteArray() }
        }
        val data = UniCacheEngine(source, backgroundScope)
        val repo = data.asUniRepository(backgroundScope)
        try {
            val home = HomeDashboardViewModel(repo, emptyList(), preparationDispatcher = Dispatchers.Main)
            val badge = AcademicIdentityViewModel(repo)
            runCurrent()
            assertSame(data.portrait.value, home.uiState.value.profilePhotoData)
            assertEquals(1, calls)
            photo = "second"
            data.refresh(listOf(UniDatasets.Student), force = true)
            runCurrent()
            assertContentEquals("second".encodeToByteArray(), home.uiState.value.profilePhotoData)
            assertSame(home.uiState.value.profilePhotoData, badge.uiState.value.photoData)
            assertEquals(2, calls)
        } finally { data.close(); Dispatchers.resetMain() }
    }
    @Test
    fun concurrentConsumersAndEquivalentSnapshotsReuseThePreparedFeed() = runTest {
        val cache = UniCacheEngine(FakeUniAppDataSource(), backgroundScope)
        val dispatcher = StandardTestDispatcher(testScheduler)
        val input = listOf(news("a", "Ateneo"), news("b", "Dipartimento"))
        val home = async { cache.prepareNews(input, dispatcher) }
        val list = async { cache.prepareNews(input.toList(), dispatcher) }
        assertSame(home.await(), list.await())
        assertSame(home.await(), cache.newsFeed)
        assertSame(home.await().byCategory.getValue("ateneo"), cache.newsFeed!!.byCategory.getValue("ateneo"))
    }

    @Test
    fun refreshReplacesChangedContentAndKeepsStableKeysAndAccountIsolation() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val cache = UniCacheEngine(FakeUniAppDataSource(), backgroundScope)
        val first = cache.prepareNews(listOf(news("a", "Ateneo")), dispatcher)
        val update = cache.prepareNews(listOf(news("a", "Ateneo").copy(title = "Updated", publishedAt = "26/09/2026")), dispatcher)
        assertNotSame(first, update)
        assertEquals(first.items.single().key, update.items.single().key)
        assertEquals("Updated", cache.newsFeed!!.items.single().title)
        assertNull(UniCacheEngine(FakeUniAppDataSource(), backgroundScope).newsFeed)
        val empty = cache.prepareNews(emptyList(), dispatcher)
        assertEquals(emptyList(), empty.items)
        assertEquals(emptyMap(), empty.byCategory)
    }

    @Test
    fun keysStayUniqueForDuplicateBackendIdsAndStableWhenOtherItemsMove() {
        val first = news("same", "Ateneo")
        val department = news("same", "Dipartimento")
        val input = listOf(first, first, department, news("other", "Ateneo"))
        val items = input.toNewsItems()
        assertEquals(items.size, items.map { it.key }.distinct().size)
        val reversed = input.reversed().toNewsItems()
        assertEquals(items.map { it.key }.toSet(), reversed.map { it.key }.toSet())
    }

    @Test
    fun sortPreservesDatesTitlesAndMissingDateOrdering() {
        val items = listOf(
            news("z", "Ateneo").copy(publishedAt = ""),
            news("b", "Ateneo").copy(publishedAt = "25/09/2026"),
            news("a", "Ateneo").copy(publishedAt = "2026-09-25T12:30:00Z"),
            news("old", "Ateneo").copy(publishedAt = "2025-12-31"),
        ).toNewsItems()
        assertEquals(listOf("a", "b", "old", "z"), items.map { it.title })
    }

    private fun news(id: String, category: String) = UniversityNews(
        id = id, title = id, summary = "Summary", content = "Content",
        publishedAt = "2026-09-25", category = category, sourceUrl = null,
    )
}
