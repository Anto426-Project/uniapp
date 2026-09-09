package com.anto426.uniapp.demo

import com.anto426.securestorage.SecureStorageManager
import com.anto426.uniapp.account.model.UniAccountCredentials
import com.anto426.uniapp.account.session.UniSessionCoordinator
import com.anto426.uniapp.account.storage.UniAccountStore
import com.anto426.uniapp.data.local.FakeUniLocalDataStore
import com.anto426.uniapp.session.AppSessionController
import com.anto426.uniapp.session.model.AppSessionState
import com.anto426.uniapp.testing.ResourceTest
import com.anto426.uniapp.testing.StorageTestFactory
import com.anto426.unisdk.backend.RemoteUniBackendService
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class DemoAccountTest : ResourceTest() {
    @Test
    fun demoLoginRestoreAndSwitchNeverUseTheUniversityBackend() = runTest {
        var requests = 0
        val client = HttpClient(MockEngine { requests++; error("A demo account must never contact the university") })
        val backend = RemoteUniBackendService(client)
        val accounts = UniAccountStore(SecureStorageManager(StorageTestFactory(), "demo"))
        val coordinator = UniSessionCoordinator(backend, accounts)
        val local = FakeUniLocalDataStore()
        try {
            val controller = AppSessionController(coordinator, accounts, local)
            controller.authenticate(UniAccountCredentials(DemoAccount.USERNAME, DemoAccount.PASSWORD))
            val account = assertIs<AppSessionState.Authenticated>(controller.state.value).account
            assertTrue(DemoAccount.isDemo(account))
            assertNull(controller.currentAccountClient())
            val restored = AppSessionController(coordinator, accounts, local)
            restored.initialize()
            assertEquals(account, assertIs<AppSessionState.Authenticated>(restored.state.value).account)
            controller.signOut()
            controller.activate(account.accountId)
            assertIs<AppSessionState.Authenticated>(controller.state.value)
            assertEquals(0, requests)
        } finally { backend.close(); client.close() }
    }

    @Test
    fun incorrectDemoPasswordDoesNotFallThroughToRealAuthentication() = runTest {
        var requests = 0
        val client = HttpClient(MockEngine { requests++; error("Unexpected network") })
        val backend = RemoteUniBackendService(client)
        val accounts = UniAccountStore(SecureStorageManager(StorageTestFactory(), "demo"))
        try {
            val controller = AppSessionController(UniSessionCoordinator(backend, accounts), accounts, FakeUniLocalDataStore())
            controller.authenticate(UniAccountCredentials(DemoAccount.USERNAME, "incorrect"))
            assertIs<AppSessionState.SignedOut>(controller.state.value)
            assertTrue(accounts.snapshot().accounts.isEmpty())
            assertEquals(0, requests)
        } finally { backend.close(); client.close() }
    }

    @Test
    fun demoDatasetIsCoherentAndSimulatedChangesStayWithinThatSession() = runTest {
        val source = DemoAppDataSource()
        val career = source.loadCareer()
        val plan = source.loadStudyPlan()
        assertEquals(career.exams.sumOf { it.cfu ?: 0 }.toString(), career.cfu)
        assertEquals(career.cfuTarget, plan.courses.sumOf { it.cfu ?: 0 })
        assertEquals(career.exams.size, plan.courses.count { it.completed })
        assertEquals(setOf(1, 2, 3), plan.courses.mapNotNull { it.year }.toSet())
        val round = source.loadExamRounds().first { !it.booked }
        source.bookExamRound(round)
        assertTrue(source.loadExamRounds().first { it.appId == round.appId }.booked)
        assertFalse(DemoAppDataSource().loadExamRounds().first { it.appId == round.appId }.booked)
        assertTrue(source.loadStudentDetails().badgeQrValue!!.contains("NOT-A-VALID"))
    }
}
