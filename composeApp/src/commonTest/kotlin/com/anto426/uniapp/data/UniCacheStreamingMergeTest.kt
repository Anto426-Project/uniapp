package com.anto426.uniapp.data

import com.anto426.unisdk.errors.UniSdkErrorCode
import com.anto426.unisdk.errors.UniSdkException
import com.anto426.unisdk.model.AttendanceRecord
import com.anto426.unisdk.model.ExamRound
import com.anto426.unisdk.model.ProfessorContentItem
import com.anto426.unisdk.model.ProfessorDashboard
import com.anto426.unisdk.model.ProfessorSection
import com.anto426.unisdk.model.UniversityContact
import com.anto426.unisdk.model.UniversityNews
import com.anto426.unisdk.transport.TransportBooking
import com.anto426.unisdk.transport.TransportData
import com.anto426.unisdk.transport.TransportRoute
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UniCacheStreamingMergeTest {

    @Test
    fun mergeUniversityNewsAddsNewItemsEnrichesDuplicatesAndNeverDropsCachedNews() {
        val cached = listOf(
            UniversityNews(
                id = "n-old",
                title = "Vecchio avviso",
                summary = "Storico",
                content = "Contenuto storico",
                publishedAt = "2026-10-01",
                category = "Ateneo",
                sourceUrl = null,
            ),
            UniversityNews(
                id = "n-1",
                title = "Avviso 1",
                summary = "Sintesi breve",
                content = "Breve",
                publishedAt = "2026-11-24",
                category = "Ateneo",
                sourceUrl = null,
            ),
        )
        val incoming = listOf(
            UniversityNews(
                id = "n-new",
                title = "Nuovo avviso",
                summary = "Nuova sintesi",
                content = "Nuovo testo",
                publishedAt = "2026-11-25",
                category = "Dipartimento",
                sourceUrl = null,
            ),
            UniversityNews(
                id = "n-1",
                title = "Avviso 1",
                summary = null,
                content = "Contenuto completo e molto più ricco",
                publishedAt = "2026-11-24",
                category = null,
                sourceUrl = null,
            ),
        )

        val merged = mergeUniversityNews(cached, incoming)

        assertEquals(listOf("n-new", "n-1", "n-old"), merged.map { it.id })
        val enriched = merged.first { it.id == "n-1" }
        assertEquals("Contenuto completo e molto più ricco", enriched.content)
        assertEquals("Sintesi breve", enriched.summary)
        assertEquals("Ateneo", enriched.category)

        // Empty partial response must never clear cached news
        assertEquals(merged, mergeUniversityNews(merged, emptyList()))
    }

    @Test
    fun mergeAttendanceAndContactsPreserveCachedHistoryAcrossPartialUpdates() {
        val cachedAttendance = listOf(
            AttendanceRecord(
                id = "att-1",
                teachingActivityId = "ad-1",
                courseName = "Algoritmi",
                status = "validata",
                occurredAt = "2026-11-10",
                startTime = "09:00",
                endTime = "11:00",
                teacherName = "Prof. Rossi",
                room = "Aula A",
                note = null,
                durationHours = 2.0,
                courseTotalHours = 48.0,
                locationVerified = true,
                locationCode = "1",
            ),
        )
        val incomingAttendance = listOf(
            AttendanceRecord(
                id = "att-2",
                teachingActivityId = "ad-1",
                courseName = "Algoritmi",
                status = "validata",
                occurredAt = "2026-11-25",
                startTime = "09:00",
                endTime = "11:00",
                teacherName = "Prof. Rossi",
                room = "Aula A",
                note = null,
                durationHours = 2.0,
                courseTotalHours = 48.0,
                locationVerified = true,
                locationCode = "1",
            ),
        )
        val mergedAttendance = mergeAttendanceHistory(cachedAttendance, incomingAttendance)
        assertEquals(setOf("att-1", "att-2"), mergedAttendance.map { it.id }.toSet())
        assertEquals(mergedAttendance, mergeAttendanceHistory(mergedAttendance, emptyList()))

        val cachedContacts = listOf(
            UniversityContact(
                id = "c-1",
                firstName = "Ada",
                lastName = "Lovelace",
                displayName = "Ada Lovelace",
                email = "ada@unimol.it",
                phones = listOf("+39 111"),
                organization = "Informatica",
                address = null,
                building = null,
                city = "Pesche",
            ),
            UniversityContact(
                id = "c-2",
                firstName = "Grace",
                lastName = "Hopper",
                displayName = "Grace Hopper",
                email = "grace@unimol.it",
                phones = listOf("+39 222"),
                organization = "Informatica",
                address = null,
                building = null,
                city = "Pesche",
            ),
        )
        val incomingContacts = listOf(
            UniversityContact(
                id = "c-1",
                firstName = "Ada",
                lastName = "Lovelace",
                displayName = "Ada Lovelace",
                email = "ada@unimol.it",
                phones = listOf("+39 333"),
                organization = null,
                address = null,
                building = "Edificio II",
                city = null,
            ),
        )
        val mergedContacts = mergeUniversityContacts(cachedContacts, incomingContacts)
        assertEquals(setOf("c-1", "c-2"), mergedContacts.map { it.id }.toSet())
        val ada = mergedContacts.first { it.id == "c-1" }
        assertEquals(listOf("+39 333", "+39 111"), ada.phones)
        assertEquals("Informatica", ada.organization)
        assertEquals("Edificio II", ada.building)
        assertEquals("Pesche", ada.city)
    }

    @Test
    fun mergeProfessorDashboardPreservesCachedSectionsWhileStreamingOrUnavailable() {
        val cached = ProfessorDashboard(
            teachings = listOf(ProfessorContentItem(id = "t-1", title = "Algoritmi")),
            theses = listOf(ProfessorContentItem(id = "th-1", title = "Tesi AI")),
        )
        val intermediate = ProfessorDashboard(
            teachings = listOf(ProfessorContentItem(id = "t-2", title = "Sistemi Operativi")),
            theses = emptyList(),
        )

        val duringStream = mergeProfessorDashboard(cached, intermediate, isFinal = false)
        assertEquals(listOf("t-2"), duringStream.teachings.map { it.id })
        assertEquals(listOf("th-1"), duringStream.theses.map { it.id })

        val finalWithUnavailableTheses = ProfessorDashboard(
            teachings = listOf(ProfessorContentItem(id = "t-2", title = "Sistemi Operativi")),
            theses = emptyList(),
            unavailableSections = setOf(ProfessorSection.THESES),
        )
        val mergedFinal = mergeProfessorDashboard(cached, finalWithUnavailableTheses, isFinal = true)
        assertEquals(listOf("th-1"), mergedFinal.theses.map { it.id })
    }

    @Test
    fun mergeTransportAndExamProgressKeepCachedBookedItemsUntilFinalReconciliation() {
        val cachedTransport = TransportData(
            routeLabel = "Campobasso - Pesche",
            routeCode = "CB-PE",
            availableRoutes = listOf(TransportRoute("CB-PE", "Campobasso - Pesche")),
            bookings = listOf(
                TransportBooking(
                    id = "b-old",
                    number = "1",
                    routeCode = "CB-PE",
                    date = "2026-11-26",
                    direction = "Andata",
                    isReturn = false,
                    ticketUrl = "https://example.test/t/1",
                ),
            ),
            totalCount = 1,
        )
        val firstRouteEmission = TransportData(
            routeLabel = "Isernia - Pesche",
            routeCode = "IS-PE",
            availableRoutes = listOf(TransportRoute("IS-PE", "Isernia - Pesche")),
            bookings = listOf(
                TransportBooking(
                    id = "b-new",
                    number = "2",
                    routeCode = "IS-PE",
                    date = "2026-11-27",
                    direction = "Andata",
                    isReturn = false,
                    ticketUrl = "https://example.test/t/2",
                ),
            ),
            totalCount = 1,
        )
        val mergedTransport = mergeTransportProgress(cachedTransport, firstRouteEmission)
        assertEquals(setOf("b-new", "b-old"), mergedTransport.bookings.map { it.id }.toSet())
        assertEquals(2, mergedTransport.availableRoutes.size)

        val cachedExams = listOf(
            ExamRound(
                courseName = "Analisi",
                dateTime = "10/12/2026 09:00",
                room = "Aula 1",
                availableSlots = 10,
                open = false,
                booked = true,
                cdsId = "1",
                adId = "2",
                appId = "3",
            ),
        )
        val availableOnly = listOf(
            ExamRound(
                courseName = "Fisica",
                dateTime = "12/12/2026 09:00",
                room = "Aula 2",
                availableSlots = 20,
                open = true,
                booked = false,
                cdsId = "1",
                adId = "4",
                appId = "5",
            ),
        )
        val mergedExams = mergeExamRoundsProgress(cachedExams, availableOnly)
        assertEquals(setOf("Fisica", "Analisi"), mergedExams.map { it.courseName }.toSet())
    }

    @Test
    fun cacheEngineEmitsNewsStreamBatchesIncrementallyWithoutBlocking() = runTest {
        val releaseSecondBatch = CompletableDeferred<Unit>()
        val firstBatchObserved = CompletableDeferred<List<UniversityNews>>()
        val finalBatchObserved = CompletableDeferred<List<UniversityNews>>()

        val streamingGateway = object : FakeUniAppDataSource() {
            override fun streamUniversityNews(forceRefresh: Boolean): Flow<List<UniversityNews>> = flow {
                val batch1 = listOf(
                    UniversityNews(
                        id = "n-1",
                        title = "Primo avviso",
                        summary = "Breve",
                        content = "Testo 1",
                        publishedAt = "2026-11-25",
                        category = "Ateneo",
                        sourceUrl = null,
                    ),
                )
                emit(batch1)
                releaseSecondBatch.await()
                emit(
                    batch1 + UniversityNews(
                        id = "n-2",
                        title = "Secondo avviso archivio",
                        summary = "Archivio",
                        content = "Testo 2",
                        publishedAt = "2026-11-20",
                        category = "Dipartimento",
                        sourceUrl = null,
                    ),
                )
            }
        }

        val engineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val engine = UniCacheEngine(source = streamingGateway, parentScope = engineScope)
        try {
            withContext(Dispatchers.Default) {
                val observerJob = launch {
                    engine.observe(listOf(UniDatasets.News)).collect { snapshot ->
                        val state = snapshot.state(UniDatasets.News)
                        val items = state.value ?: return@collect
                        if (items.size == 1 && !firstBatchObserved.isCompleted) {
                            firstBatchObserved.complete(items)
                            releaseSecondBatch.complete(Unit)
                        }
                        if (items.size == 2 && !state.refreshing && !finalBatchObserved.isCompleted) {
                            finalBatchObserved.complete(items)
                        }
                    }
                }

                val first = withTimeout(5_000L) { firstBatchObserved.await() }
                assertEquals(listOf("n-1"), first.map { it.id })

                val finalItems = withTimeout(5_000L) { finalBatchObserved.await() }
                assertEquals(listOf("n-1", "n-2"), finalItems.map { it.id })
                observerJob.cancel()
            }
        } finally {
            engine.close()
            engineScope.cancel()
        }
    }

    @Test
    fun cacheEngineTracksOfflineStateAcrossNetworkFailuresAndRecovery() = runTest {
        var shouldFail = true
        val gateway = object : FakeUniAppDataSource() {
            override suspend fun loadTaxes(forceRefresh: Boolean) =
                if (shouldFail) throw UniSdkException(UniSdkErrorCode.NETWORK_REQUEST_FAILED)
                else com.anto426.unisdk.model.Taxes("0,00 €", emptyList())
        }
        val engineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val engine = UniCacheEngine(source = gateway, parentScope = engineScope)
        try {
            withContext(Dispatchers.Default) {
                assertFalse(engine.isOffline.value)
                runCatching { engine.loadTaxes(forceRefresh = true) }
                assertTrue(engine.isOffline.value)

                shouldFail = false
                engine.loadTaxes(forceRefresh = true)
                assertFalse(engine.isOffline.value)
            }
        } finally {
            engine.close()
            engineScope.cancel()
        }
    }
}
