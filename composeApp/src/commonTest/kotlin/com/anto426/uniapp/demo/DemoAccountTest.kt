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
import kotlinx.coroutines.flow.first
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
    fun demoUsesTheSharedDeveloperProfileAndPersistsUpdatesWithoutChangingUniversityData() = runTest {
        val client = HttpClient(MockEngine { error("Unexpected university request") })
        val backend = RemoteUniBackendService(client)
        val accounts = UniAccountStore(SecureStorageManager(StorageTestFactory(), "demo-author"))
        val local = FakeUniLocalDataStore()
        val author = com.anto426.uniapp.project.model.GitHubAuthor(
            login = com.anto426.unisdk.platform.ProjectInfo.authorLogin, name = "Nome dello sviluppatore",
            avatarUrl = "https://avatars.githubusercontent.com/u/123", url = "https://github.com/fixture")
        local.write(com.anto426.uniapp.data.local.LocalDataScope.Application,
            com.anto426.uniapp.data.local.UniAppDataKeys.GitHubProject,
            com.anto426.uniapp.project.model.GitHubProjectSnapshot(author = author))
        try {
            val controller = AppSessionController(UniSessionCoordinator(backend, accounts), accounts, local)
            controller.authenticate(UniAccountCredentials(DemoAccount.USERNAME, DemoAccount.PASSWORD))
            val original = assertIs<AppSessionState.Authenticated>(controller.state.value).account
            assertEquals(author.name, original.displayName)
            assertEquals(author.avatarUrl, original.photoUrl)
            assertEquals(DemoCatalog.load().student.matricola, original.matricola)
            assertEquals(DemoCatalog.load().student.email, original.email)
            val identity = { assertIs<AppSessionState.Authenticated>(controller.state.value).account.let { it.displayName to it.photoUrl } }
            val source = DemoAppDataSource(identity = identity, imageLoader = { byteArrayOf(1, 2, 3) })
            val round = source.loadExamRounds().first { !it.booked }
            source.bookExamRound(round)
            controller.updateDemoAuthor(author.copy(name = "Nome aggiornato", avatarUrl = "https://avatars.githubusercontent.com/u/456"))
            val updated = assertIs<AppSessionState.Authenticated>(controller.state.value).account
            assertEquals(original.accountId, updated.accountId)
            assertEquals("Nome aggiornato", updated.displayName)
            assertEquals(updated, accounts.snapshot().accounts.single())
            assertEquals(updated.displayName, source.loadStudentDetails().fullName)
            assertEquals(updated.photoUrl, source.loadStudentDetails().photoUrl)
            assertContentEquals(byteArrayOf(1, 2, 3), source.loadProfileImage(updated.photoUrl!!))
            assertTrue(source.loadExamRounds().first { it.appId == round.appId }.booked)
            controller.updateDemoAuthor(author.copy(login = "another-user", name = "Wrong"))
            assertEquals(updated, assertIs<AppSessionState.Authenticated>(controller.state.value).account)
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

    @Test
    fun demoSharesPortraitWithAccountAvatarStoreAndCommonComponents() = runTest {
        val client = HttpClient(MockEngine { error("Unexpected network") })
        val backend = RemoteUniBackendService(client)
        val accounts = UniAccountStore(SecureStorageManager(StorageTestFactory(), "demo-avatars"))
        try {
            val controller = AppSessionController(UniSessionCoordinator(backend, accounts), accounts, FakeUniLocalDataStore())
            controller.authenticate(UniAccountCredentials(DemoAccount.USERNAME, DemoAccount.PASSWORD))
            val account = assertIs<AppSessionState.Authenticated>(controller.state.value).account
            val testBytes = byteArrayOf(9, 8, 7)
            val source = DemoAppDataSource(
                identity = { account.displayName to account.photoUrl },
                imageLoader = { testBytes },
                portraitSharer = { src, bytes -> controller.avatars.publish(account.accountId, src, bytes) },
            )
            val coordinator = com.anto426.uniapp.data.runtime.UniAppDataCoordinator(source, backgroundScope)
            coordinator.startPortrait(account)
            controller.avatars.images.first { it.containsKey(account.accountId) }
            val published = controller.avatars.images.value[account.accountId]
            assertNotNull(published)
            assertContentEquals(testBytes, published.bytes)
            assertEquals(account.photoUrl, published.source)
        } finally { backend.close(); client.close() }
    }
}
