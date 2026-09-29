package com.anto426.uniapp.account.storage

import com.anto426.securestorage.SecureStorage
import com.anto426.securestorage.SecureStorageFactory
import com.anto426.securestorage.SecureStorageManager
import com.anto426.securestorage.getString
import com.anto426.securestorage.putString
import com.anto426.uniapp.account.model.UniAccountCredentials
import com.anto426.uniapp.account.model.UniAccountSummary
import com.anto426.uniapp.data.profileCacheKey
import com.anto426.unisdk.session.UniSessionTicket
import com.anto426.unisdk.session.UniUserProfile
import com.anto426.unisdk.session.UniCareerProfile
import com.anto426.unisdk.backend.model.BackendCareerType
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

class UniAccountStoreTest {
    @Test
    fun twoAccountsAndTwoCareersKeepIndependentCachedValues() = runTest {
        val store = createStore(listOf("installation-id", "account-a", "account-b"))
        val firstCareer = UniCareerProfile(
            profileId = "001234|10", displayName = "Student", degreeName = "Informatica",
            matricola = "001234", cdsId = "10",
        )
        val secondCareer = firstCareer.copy(profileId = "001234|11", degreeName = "Matematica", cdsId = "11")
        val first = store.persistAuthenticatedAccount(
            UniAccountCredentials("first", "secret"),
            profile("first-user").copy(activeProfileId = firstCareer.profileId, profiles = listOf(firstCareer, secondCareer)),
            UniSessionTicket.restore(byteArrayOf(1)),
        )
        val second = store.persistAuthenticatedAccount(
            UniAccountCredentials("second", "secret"),
            profile("second-user").copy(activeProfileId = firstCareer.profileId, profiles = listOf(firstCareer)),
            UniSessionTicket.restore(byteArrayOf(2)),
        )
        val firstKey = profileCacheKey(firstCareer.profileId, "taxes")
        val secondKey = profileCacheKey(secondCareer.profileId, "taxes")
        store.writeCachedData(first.accountId, firstKey, byteArrayOf(10))
        store.writeCachedData(first.accountId, secondKey, byteArrayOf(11))
        store.writeCachedData(second.accountId, firstKey, byteArrayOf(20))

        assertEquals(setOf(firstCareer.profileId, secondCareer.profileId),
            store.snapshot().accounts.first { it.accountId == first.accountId }.profiles.map { it.profileId }.toSet())
        assertContentEquals(byteArrayOf(10), store.readCachedData(first.accountId, firstKey))
        assertContentEquals(byteArrayOf(11), store.readCachedData(first.accountId, secondKey))
        assertContentEquals(byteArrayOf(20), store.readCachedData(second.accountId, firstKey))
        assertNull(store.readCachedData(second.accountId, secondKey))
    }

    @Test
    fun multipleCareersRemainProfilesOfOneServerIdentity() = runTest {
        val store = createStore(listOf("installation-id", "account-id"))
        val first =
            profile("same-user").copy(
                matricola = "student-a",
                activeProfileId = "career-a",
                profiles =
                    listOf(
                        UniCareerProfile(
                            profileId = "career-a",
                            displayName = "Student same-user",
                            degreeName = "Informatica",
                            matricola = "student-a",
                        ),
                    ),
            )
        val second =
            profile("same-user").copy(
                degreeName = "Docenza",
                matricola = null,
                activeProfileId = "professor-a",
                activeProfileType = BackendCareerType.PROFESSOR,
                profiles =
                    listOf(
                        UniCareerProfile(
                            profileId = "professor-a",
                            displayName = "Professor same-user",
                            degreeName = "Docenza",
                            teacherId = "teacher-a",
                            type = BackendCareerType.PROFESSOR,
                        ),
                    ),
            )

        val firstAccount =
            store.persistAuthenticatedAccount(
                UniAccountCredentials("identity", "secret"),
                first,
                UniSessionTicket.restore(byteArrayOf(1)),
            )
        val secondAccount =
            store.persistAuthenticatedAccount(
                UniAccountCredentials("identity", "secret"),
                second,
                UniSessionTicket.restore(byteArrayOf(2)),
            )

        val accounts = store.snapshot().accounts
        assertEquals(firstAccount.accountId, secondAccount.accountId)
        assertEquals(1, accounts.size)
        assertEquals(setOf("career-a", "professor-a"), accounts.single().profiles.map { it.profileId }.toSet())
        assertEquals(BackendCareerType.PROFESSOR, accounts.single().activeProfileType)
    }

    @Test
    fun matchingNamesAndIncompleteIdsDoNotMergeDistinctCareers() = runTest {
        val store = createStore(listOf("installation-id", "account-id"))
        val first = UniCareerProfile(
            profileId = "career-10", displayName = "Mario Rossi", degreeName = "Informatica",
            matricola = "12345", cdsId = "10",
        )
        val second = first.copy(profileId = "career-11", cdsId = null)
        val account = store.persistAuthenticatedAccount(
            UniAccountCredentials("mario", "secret"),
            profile("same-user").copy(activeProfileId = first.profileId, profiles = listOf(first, second)),
            UniSessionTicket.restore(byteArrayOf(1)),
        )

        assertEquals(setOf(first.profileId, second.profileId), account.profiles.map { it.profileId }.toSet())
    }

    @Test
    fun professorProfilesInDifferentDepartmentsStaySeparate() = runTest {
        val store = createStore(listOf("installation-id", "account-id"))
        val first = UniCareerProfile(
            profileId = "professor|123|dip-1", displayName = "Mario Rossi", degreeName = "Docenza",
            teacherId = "123", dipId = "dip-1", departmentName = "Scienze",
            type = BackendCareerType.PROFESSOR,
        )
        val second = first.copy(profileId = "professor|123|dip-2", dipId = "dip-2", departmentName = "Medicina")
        val account = store.persistAuthenticatedAccount(
            UniAccountCredentials("mario", "secret"),
            profile("same-user").copy(activeProfileId = first.profileId, profiles = listOf(first, second)),
            UniSessionTicket.restore(byteArrayOf(1)),
        )

        assertEquals(setOf(first.profileId, second.profileId), account.profiles.map { it.profileId }.toSet())
    }

    @Test
    fun professorLabelChangeKeepsTheSameStableProfile() = runTest {
        val store = createStore(listOf("installation-id", "account-id"))
        val original = UniCareerProfile(
            profileId = "professor|123|dip-1",
            displayName = "Mario Rossi", degreeName = "Docenza",
            teacherId = "123", dipId = "dip-1", departmentName = "Scienze",
            type = BackendCareerType.PROFESSOR,
        )
        val credentials = UniAccountCredentials("mario", "secret")
        val saved = store.persistAuthenticatedAccount(
            credentials, profile("same-user").copy(
                activeProfileId = original.profileId, profiles = listOf(original),
            ), UniSessionTicket.restore(byteArrayOf(1)),
        )
        val current = original.copy(
            degreeName = "Insegnamento",
            departmentName = "Dipartimento rinominato",
        )
        val updated = store.updateSession(
            saved.accountId,
            profile("same-user").copy(activeProfileId = current.profileId, profiles = listOf(current)),
            UniSessionTicket.restore(byteArrayOf(2)),
        )

        assertEquals(current.profileId, updated.activeProfileId)
        assertEquals(listOf(current.profileId), updated.profiles.map { it.profileId })
        assertEquals("Insegnamento", updated.profiles.single().degreeName)
    }

    @Test
    fun registryOwnsStableInstallationAndActiveAccount() = runTest {
        val store = createStore(listOf("installation-id", "account-id"))

        val initial = store.snapshot()
        val saved = store.persistAuthenticatedAccount(
            credentials = UniAccountCredentials("student", "secret"),
            profile = profile("server-user"),
            ticket = UniSessionTicket.restore(byteArrayOf(1, 2, 3)),
        )
        val restored = store.snapshot()

        assertEquals("installation-id", initial.installationId)
        assertEquals("installation-id", restored.installationId)
        assertEquals("account-id", saved.accountId)
        assertEquals("account-id", restored.activeAccountId)
        assertEquals(listOf(saved), restored.accounts)
    }

    @Test
    fun credentialsAndTicketStayInPerAccountVault() = runTest {
        val factory = MemorySecureStorageFactory()
        val store = createStore(listOf("installation-id", "account-id"), factory)
        store.persistAuthenticatedAccount(
            credentials = UniAccountCredentials("student", "secret"),
            profile = profile("server-user"),
            ticket = UniSessionTicket.restore(byteArrayOf(4, 5, 6)),
        )

        store.withCredentials("account-id") { credentials ->
            assertEquals("UniCredentials([REDACTED])", credentials.toString())
        }
        val ticketBytes = store.loadSessionTicket("account-id")!!.export()
        assertContentEquals(byteArrayOf(4, 5, 6), ticketBytes)
        ticketBytes.fill(0)

        assertFalse(factory.open("test.registry").contains("credentials.username"))
        val accountVault = factory.open("test.vault.account-id")
        assertEquals("student", accountVault.getString("credentials.username"))
        assertEquals("secret", accountVault.getString("credentials.password"))
    }

    @Test
    fun forgettingOneAccountCryptographicallySeparatesTheOther() = runTest {
        val factory = MemorySecureStorageFactory()
        val store = createStore(listOf("installation-id", "account-a", "account-b"), factory)
        store.persistAuthenticatedAccount(
            credentials = UniAccountCredentials("first", "first-secret"),
            profile = profile("first-user"),
            ticket = UniSessionTicket.restore(byteArrayOf(1)),
        )
        store.persistAuthenticatedAccount(
            credentials = UniAccountCredentials("second", "second-secret"),
            profile = profile("second-user"),
            ticket = UniSessionTicket.restore(byteArrayOf(2)),
        )

        store.forgetAccount("account-a")

        val snapshot = store.snapshot()
        assertEquals(listOf("account-b"), snapshot.accounts.map(UniAccountSummary::accountId))
        assertEquals("account-b", snapshot.activeAccountId)
        assertEquals(
            "second",
            factory.open("test.vault.account-b").getString("credentials.username"),
        )
    }

    @Test
    fun differentLoginCannotOverwriteAccountAwaitingReauthentication() = runTest {
        val factory = MemorySecureStorageFactory()
        val store = createStore(listOf("installation-id", "account-a", "account-b"), factory)
        val first = store.persistAuthenticatedAccount(
            UniAccountCredentials("first", "first-secret"), profile("first-user"),
            UniSessionTicket.restore(byteArrayOf(1)),
        )
        store.writeCachedData(first.accountId, "career", byteArrayOf(7))

        val second = store.persistAuthenticatedAccount(
            UniAccountCredentials("second", "second-secret"), profile("second-user"),
            UniSessionTicket.restore(byteArrayOf(2)), preferredAccountId = first.accountId,
        )

        assertEquals("account-b", second.accountId)
        assertEquals(listOf("first-user", "second-user"), store.snapshot().accounts.map { it.serverUserId })
        assertEquals(second.accountId, store.snapshot().activeAccountId)
        assertEquals("first", factory.open("test.vault.account-a").getString("credentials.username"))
        assertEquals("first-secret", factory.open("test.vault.account-a").getString("credentials.password"))
        assertContentEquals(byteArrayOf(7), store.readCachedData(first.accountId, "career"))
        assertNull(store.readCachedData(second.accountId, "career"))
    }

    @Test
    fun resumedSessionWithDifferentIdentityCannotReplaceAccount() = runTest {
        val store = createStore(listOf("installation-id", "account-a"))
        val first = store.persistAuthenticatedAccount(
            UniAccountCredentials("first", "secret"), profile("first-user"),
            UniSessionTicket.restore(byteArrayOf(1)),
        )

        assertFailsWith<IllegalArgumentException> {
            store.updateSession(first.accountId, profile("second-user"), UniSessionTicket.restore(byteArrayOf(2)))
        }
        assertEquals("first-user", store.snapshot().accounts.single().serverUserId)
        assertContentEquals(byteArrayOf(1), store.loadSessionTicket(first.accountId)!!.export())
    }

    @Test
    fun clearingExpiredTicketKeepsEncryptedAccountAndCredentials() = runTest {
        val store = createStore(listOf("installation-id", "account-id"))
        store.persistAuthenticatedAccount(
            credentials = UniAccountCredentials("student", "secret"),
            profile = profile("server-user"),
            ticket = UniSessionTicket.restore(byteArrayOf(9)),
        )

        store.clearSessionTicket("account-id")

        assertNull(store.loadSessionTicket("account-id"))
        assertEquals("account-id", store.snapshot().activeAccountId)
        store.withCredentials("account-id") {
            assertEquals("UniCredentials([REDACTED])", it.toString())
        }
    }

    @Test
    fun cachedTransportTicketIsIsolatedAndDestroyedWithItsAccount() = runTest {
        val factory = MemorySecureStorageFactory()
        val store = createStore(listOf("installation-id", "account-a", "account-b"), factory)
        store.persistAuthenticatedAccount(
            credentials = UniAccountCredentials("first", "first-secret"),
            profile = profile("first-user"),
            ticket = UniSessionTicket.restore(byteArrayOf(1)),
        )
        store.persistAuthenticatedAccount(
            credentials = UniAccountCredentials("second", "second-secret"),
            profile = profile("second-user"),
            ticket = UniSessionTicket.restore(byteArrayOf(2)),
        )
        val cachedTicket = "ticket-url-and-qr".encodeToByteArray()
        store.writeCachedData("account-a", "transport", cachedTicket)

        assertContentEquals(cachedTicket, store.readCachedData("account-a", "transport"))
        assertNull(store.readCachedData("account-b", "transport"))

        store.forgetAccount("account-a")

        assertFalse(factory.open("test.vault.account-a").contains("cache.v1.transport"))
        assertEquals("second", factory.open("test.vault.account-b").getString("credentials.username"))
    }

    @Test
    fun applicationCacheIsSharedWhileProfileDataRemainsIsolatedAndSurvivesAccountRemoval() = runTest {
        val factory = MemorySecureStorageFactory()
        val store = createStore(listOf("installation", "account-a", "account-b"), factory)
        val first = store.persistAuthenticatedAccount(UniAccountCredentials("first", "secret"), profile("first"), UniSessionTicket.restore(byteArrayOf(1)))
        val second = store.persistAuthenticatedAccount(UniAccountCredentials("second", "secret"), profile("second"), UniSessionTicket.restore(byteArrayOf(2)))
        store.writeCachedData(null, "public-project-data", byteArrayOf(7))
        store.writeCachedData(first.accountId, "profile-a-news", byteArrayOf(8))
        assertContentEquals(byteArrayOf(7), store.readCachedData(null, "public-project-data"))
        assertNull(store.readCachedData(second.accountId, "profile-a-news"))
        store.forgetAccount(first.accountId)
        val reopened = createStore(emptyList(), factory)
        assertContentEquals(byteArrayOf(7), reopened.readCachedData(null, "public-project-data"))
        assertEquals(listOf(second), reopened.snapshot().accounts)
    }

    @Test
    fun releaseResetRemovesOldSharedNewsCache() = runTest {
        val factory = MemorySecureStorageFactory()
        val old = createStore(listOf("installation"), factory)
        old.writeCachedData(null, "university-news-unimol-ateneo", byteArrayOf(7))

        val updated = createStore(emptyList(), factory, resetStorageForRelease = true)
        assertNull(updated.readCachedData(null, "university-news-unimol-ateneo"))
    }

    @Test
    fun releaseResetErasesAllManagedDataOnceAndPersistsTheNoticeUntilAcknowledgment() = runTest {
        val factory = MemorySecureStorageFactory()
        val old = createStore(listOf("old-installation", "old-account-a", "old-account-b"), factory)
        repeat(2) { index ->
            val account = old.persistAuthenticatedAccount(UniAccountCredentials("user-$index", "secret"), profile("user-$index"), UniSessionTicket.restore(byteArrayOf(1)))
            old.writeCachedData(account.accountId, "response", byteArrayOf(2))
            old.writeAccountData(account.accountId, "security.preference", byteArrayOf(3))
            old.writeProfileImage(account.accountId, "portrait", 100L, byteArrayOf(4))
        }
        old.writeCachedData(null, "general-news", byteArrayOf(5))
        old.writeApplicationData("application.theme.mode", "Dark".encodeToByteArray())
        var cleanups = 0
        val updated = createStore(listOf("new-installation", "new-account"), factory, true) { cleanups++ }
        // The preference path must await reset too, before session initialization runs.
        assertNull(updated.readApplicationData("application.theme.mode"))
        assertTrue(updated.storageResetNotice.value)
        assertTrue(updated.snapshot().accounts.isEmpty())
        assertNull(updated.readCachedData(null, "general-news"))
        for (accountId in listOf("old-account-a", "old-account-b")) {
            val vault = factory.open("test.vault.$accountId")
            assertFalse(vault.contains("credentials.username"))
            assertFalse(vault.contains("session.primary.ticket"))
            assertFalse(vault.contains("cache.v1.response"))
            assertFalse(vault.contains("local.v1.security.preference"))
            assertFalse(vault.contains("profile.image.bytes"))
        }
        val saved = updated.persistAuthenticatedAccount(UniAccountCredentials("new", "secret"), profile("new"), UniSessionTicket.restore(byteArrayOf(9)))
        updated.writeApplicationData("application.theme.mode", "Light".encodeToByteArray())
        updated.writeCachedData(saved.accountId, "response", byteArrayOf(8))
        val nextLaunch = createStore(emptyList(), factory, true) { cleanups++ }
        assertEquals(saved, nextLaunch.snapshot().accounts.single())
        assertTrue(nextLaunch.storageResetNotice.value)
        assertContentEquals("Light".encodeToByteArray(), nextLaunch.readApplicationData("application.theme.mode"))
        assertContentEquals(byteArrayOf(8), nextLaunch.readCachedData(saved.accountId, "response"))
        nextLaunch.acknowledgeStorageReset()
        val futureVersion = createStore(emptyList(), factory, true) { cleanups++ }
        assertEquals(saved, futureVersion.snapshot().accounts.single())
        assertFalse(futureVersion.storageResetNotice.value)
        assertContentEquals(byteArrayOf(9), futureVersion.loadSessionTicket(saved.accountId)!!.export())
        assertEquals(1, cleanups)
    }

    @Test
    fun newResetRunsEvenWhenThePreviousReleaseWasAlreadyReset() = runTest {
        val factory = MemorySecureStorageFactory()
        val old = createStore(listOf("old-installation", "old-account"), factory)
        old.persistAuthenticatedAccount(
            UniAccountCredentials("old", "secret"), profile("old-user"),
            UniSessionTicket.restore(byteArrayOf(1)),
        )
        factory.open("test.vault.storage-migrations")
            .putString("migration.storage-reset-20260927", "complete")

        val current = createStore(listOf("new-installation"), factory, resetStorageForRelease = true)
        assertTrue(current.snapshot().accounts.isEmpty())
        assertTrue(current.storageResetNotice.value)
    }

    @Test
    fun interruptedResetRetriesCleanupAndKeepsTheNoticeEvenAfterTheRegistryWasRemoved() = runTest {
        val factory = MemorySecureStorageFactory()
        createStore(listOf("old-installation"), factory).snapshot()
        var fail = true
        var attempts = 0
        val updated = createStore(listOf("new-installation"), factory, true) {
            attempts++
            if (fail) error("Cleanup interrupted")
        }
        assertFailsWith<IllegalStateException> { updated.writeApplicationData("application.theme.mode", "Dark".encodeToByteArray()) }
        fail = false
        assertTrue(updated.snapshot().accounts.isEmpty())
        assertTrue(updated.storageResetNotice.value)
        assertNull(updated.readApplicationData("application.theme.mode"))
        assertEquals(2, attempts)
    }

    @Test
    fun freshInstallHasNoResetNoticeAndConcurrentFirstReadsRunResetOnlyOnce() = runTest {
        val factory = MemorySecureStorageFactory()
        var cleanups = 0
        val fresh = createStore(listOf("installation"), factory, true) { cleanups++ }
        List(8) { async { fresh.readApplicationData("application.theme.mode") } }.awaitAll()
        assertFalse(fresh.storageResetNotice.value)
        fresh.writeApplicationData("application.theme.mode", "Dark".encodeToByteArray())
        val restarted = createStore(emptyList(), factory, true) { cleanups++ }
        assertContentEquals("Dark".encodeToByteArray(), restarted.readApplicationData("application.theme.mode"))
        assertFalse(restarted.storageResetNotice.value)
        assertEquals(1, cleanups)
    }

    private fun createStore(
        identifiers: List<String>,
        factory: MemorySecureStorageFactory = MemorySecureStorageFactory(),
        resetStorageForRelease: Boolean = false,
        resetPlatformStorage: suspend () -> Unit = {},
    ): UniAccountStore {
        val iterator = identifiers.iterator()
        return UniAccountStore(
            storageManager = SecureStorageManager(factory, rootScope = "test"),
            resetStorageForRelease = resetStorageForRelease,
            resetPlatformStorage = resetPlatformStorage,
            generateIdentifier = { iterator.next() },
        )
    }

    private fun profile(id: String) =
        UniUserProfile(
            id = id,
            displayName = "Student $id",
            degreeName = "Computer Science",
            matricola = id,
            email = "$id@example.invalid",
            photoUrl = null,
            isGuest = false,
        )
}

private class MemorySecureStorageFactory : SecureStorageFactory {
    private val storages = mutableMapOf<String, MemorySecureStorage>()

    override fun open(scope: String): SecureStorage = storages.getOrPut(scope, ::MemorySecureStorage)
}

private class MemorySecureStorage : SecureStorage {
    private val lock = Mutex()
    private val values = mutableMapOf<String, ByteArray>()

    override suspend fun putBytes(key: String, value: ByteArray) {
        lock.withLock { values[key] = value.copyOf() }
    }

    override suspend fun getBytes(key: String): ByteArray? =
        lock.withLock { values[key]?.copyOf() }

    override suspend fun remove(key: String) {
        lock.withLock { values.remove(key)?.fill(0) }
    }

    override suspend fun contains(key: String): Boolean = lock.withLock { key in values }

    override suspend fun destroy() {
        lock.withLock {
            values.values.forEach { it.fill(0) }
            values.clear()
        }
    }
}
