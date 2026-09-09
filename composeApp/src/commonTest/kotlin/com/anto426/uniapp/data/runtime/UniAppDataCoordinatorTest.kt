package com.anto426.uniapp.data.runtime

import com.anto426.uniapp.data.FakeUniAppDataSource
import com.anto426.uniapp.didactics.presentation.AcademicIdentityViewModel
import com.anto426.uniapp.home.presentation.HomeDashboardViewModel
import com.anto426.uniapp.services.presentation.TaxesViewModel
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.unisdk.backend.model.StudentDetailsData
import com.anto426.unisdk.backend.model.TaxesData
import com.anto426.unisdk.transport.TransportActionResult
import com.anto426.unisdk.transport.TransportData
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class UniAppDataCoordinatorTest {
    @Test
    fun freshDataSurvivesNavigationAndAutomaticRefreshUntilItsPolicyExpires() = runTest {
        var now = 1_000L
        var calls = 0
        val source = object : FakeUniAppDataSource() {
            override suspend fun loadTaxes(forceRefresh: Boolean): TaxesData { calls++; return TaxesData("$calls", emptyList()) }
        }
        val data = UniAppDataCoordinator(source, backgroundScope, nowMillis = { now })
        data.loadTaxes()
        repeat(5) {
            data.observe(listOf(UniAppDataRequests.Taxes)).first()
            data.refresh(listOf(UniAppDataRequests.Taxes))
            data.loadTaxes()
        }
        runCurrent()
        assertEquals(1, calls)
        now += com.anto426.uniapp.data.UniAppCachePolicies.Taxes.maxAgeMillis - 1
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
            override suspend fun loadTaxes(forceRefresh: Boolean): TaxesData { calls++; error("offline") }
        }
        val data = UniAppDataCoordinator(source, backgroundScope, nowMillis = { now })
        assertFailsWith<IllegalStateException> { data.loadTaxes() }
        repeat(5) { data.refresh(listOf(UniAppDataRequests.Taxes)) }
        runCurrent()
        assertEquals(1, calls)
        now += com.anto426.uniapp.data.UniAppCachePolicies.Taxes.retryDelayMillis
        assertFailsWith<IllegalStateException> { data.loadTaxes() }
        assertEquals(2, calls)
        data.close()
    }

    @Test
    fun foregroundSchedulerRefreshesOnlyObservedExpiredDataAndStopsInBackground() = runTest {
        var calls = 0
        val request = UniAppDataRequest("scheduled", policy = com.anto426.uniapp.data.UniAppCachePolicy(100)) { _ -> ++calls }
        val data = UniAppDataCoordinator(FakeUniAppDataSource(), backgroundScope, nowMillis = { testScheduler.currentTime })
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
    fun mutationLeavesUnobservedDataStaleUntilItIsNeededAgain() = runTest {
        val calls = mutableListOf<Boolean>()
        val source = object : FakeUniAppDataSource() {
            override suspend fun loadTransportData(forceRefresh: Boolean): TransportData {
                calls += forceRefresh
                return super.loadTransportData(forceRefresh)
            }
        }
        val data = UniAppDataCoordinator(source, backgroundScope)
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
        val response = CompletableDeferred<TaxesData>()
        var calls = 0
        val source = object : FakeUniAppDataSource() {
            override suspend fun loadTaxes(forceRefresh: Boolean): TaxesData { calls++; return response.await() }
        }
        val data = UniAppDataCoordinator(source, backgroundScope)
        val first = async { data.loadTaxes() }
        val second = async { data.loadTaxes() }
        runCurrent()
        assertEquals(1, calls)
        first.cancel()
        response.complete(TaxesData("42", emptyList()))
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
            override suspend fun loadTaxes(forceRefresh: Boolean): TaxesData {
                calls += forceRefresh
                if (forceRefresh) forced.await() else initial.await()
                return TaxesData(if (forceRefresh) "20" else "10", emptyList())
            }
        }
        val data = UniAppDataCoordinator(source, backgroundScope)
        val waiting = async { data.loadTaxes() }
        runCurrent()
        repeat(3) { data.refresh(listOf(UniAppDataRequests.Taxes), force = true) }
        runCurrent()
        initial.complete(Unit)
        runCurrent()
        repeat(3) { data.refresh(listOf(UniAppDataRequests.Taxes), force = true) }
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
        val data = UniAppDataCoordinator(source, backgroundScope)
        val received = mutableListOf<TransportData>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            data.observe(listOf(UniAppDataRequests.Transport)).mapNotNull { it.value(UniAppDataRequests.Transport) }.collect(received::add)
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
            override suspend fun loadTaxes(forceRefresh: Boolean): TaxesData {
                if (fail) error("offline")
                return TaxesData("42", emptyList())
            }
        }
        val data = UniAppDataCoordinator(source, backgroundScope)
        data.loadTaxes()
        fail = true
        data.refresh(listOf(UniAppDataRequests.Taxes), force = true)
        runCurrent()
        val snapshot = data.observe(listOf(UniAppDataRequests.Taxes)).first()
        assertEquals("42", snapshot.require(UniAppDataRequests.Taxes).dueAmount)
        assertEquals("offline", snapshot.firstError?.message)
        fail = false
        data.refresh(listOf(UniAppDataRequests.Taxes), force = true)
        runCurrent()
        assertNull(data.observe(listOf(UniAppDataRequests.Taxes)).first().firstError)
        data.close()
    }

    @Test
    fun closeDropsAccountValuesAndRejectsAResponseThatIgnoresCancellation() = runTest {
        val response = CompletableDeferred<Unit>()
        val source = object : FakeUniAppDataSource() {
            override suspend fun loadTaxes(forceRefresh: Boolean): TaxesData = withContext(NonCancellable) {
                response.await()
                TaxesData("private", emptyList())
            }
        }
        val data = UniAppDataCoordinator(source, backgroundScope)
        val received = mutableListOf<UniAppDataSnapshot>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            data.observe(listOf(UniAppDataRequests.Taxes)).collect(received::add)
        }
        runCurrent()
        data.close()
        response.complete(Unit)
        runCurrent()
        assertTrue(received.all { it.value(UniAppDataRequests.Taxes) == null })
        assertFailsWith<CancellationException> { data.loadTaxes() }
    }

    @Test
    fun separateOwnersNeverShareTheSameDatasetValue() = runTest {
        fun source(amount: String) = object : FakeUniAppDataSource() {
            override suspend fun loadTaxes(forceRefresh: Boolean) = TaxesData(amount, emptyList())
        }
        val first = UniAppDataCoordinator(source("A"), backgroundScope, generation = 1)
        val second = UniAppDataCoordinator(source("B"), backgroundScope, generation = 2)
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
            UniAppDataRequest("test/$index") { _ ->
                active++
                maximum = maxOf(maximum, active)
                try { gate.await(); index } finally { active-- }
            }
        }
        val data = UniAppDataCoordinator(FakeUniAppDataSource(), backgroundScope, concurrency = 3)
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
            override suspend fun loadStudentDetails(forceRefresh: Boolean) = StudentDetailsData("Mario Rossi")
            override suspend fun loadTaxes(forceRefresh: Boolean): TaxesData { calls++; gate.await(); return TaxesData(amount, emptyList()) }
        }
        val data = UniAppDataCoordinator(source, backgroundScope)
        try {
            val home = HomeDashboardViewModel(data, emptyList())
            val taxes = TaxesViewModel(data)
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
            override suspend fun loadStudentDetails(forceRefresh: Boolean) = StudentDetailsData("Mario", photoUrl = photo)
            override suspend fun loadProfileImage(source: String, forceRefresh: Boolean): ByteArray { calls++; return source.encodeToByteArray() }
        }
        val data = UniAppDataCoordinator(source, backgroundScope)
        try {
            val home = HomeDashboardViewModel(data, emptyList())
            val badge = AcademicIdentityViewModel(data)
            runCurrent()
            assertSame(data.portrait.value, home.uiState.value.profilePhotoData)
            assertEquals(1, calls)
            photo = "second"
            data.refresh(listOf(UniAppDataRequests.Student), force = true)
            runCurrent()
            assertContentEquals("second".encodeToByteArray(), home.uiState.value.profilePhotoData)
            assertSame(home.uiState.value.profilePhotoData, badge.uiState.value.photoData)
            assertEquals(2, calls)
        } finally { data.close(); Dispatchers.resetMain() }
    }
}
