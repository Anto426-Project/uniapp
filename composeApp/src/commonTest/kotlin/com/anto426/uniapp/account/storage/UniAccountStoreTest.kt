package com.anto426.uniapp.account.storage

import com.anto426.securestorage.SecureStorage
import com.anto426.securestorage.SecureStorageFactory
import com.anto426.securestorage.SecureStorageManager
import com.anto426.securestorage.getString
import com.anto426.uniapp.account.model.UniAccountCredentials
import com.anto426.uniapp.account.model.UniAccountSummary
import com.anto426.unisdk.session.UniSessionTicket
import com.anto426.unisdk.session.UniUserProfile
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class UniAccountStoreTest {
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

    private fun createStore(
        identifiers: List<String>,
        factory: MemorySecureStorageFactory = MemorySecureStorageFactory(),
    ): UniAccountStore {
        val iterator = identifiers.iterator()
        return UniAccountStore(
            storageManager = SecureStorageManager(factory, rootScope = "test"),
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
