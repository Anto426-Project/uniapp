package com.anto426.uniapp.data.local

import com.anto426.securestorage.SecureStorageManager
import com.anto426.uniapp.account.model.UniAccountCredentials
import com.anto426.uniapp.account.storage.UniAccountStore
import com.anto426.uniapp.testing.StorageTestFactory
import com.anto426.unisdk.session.UniSessionTicket
import com.anto426.unisdk.session.UniUserProfile
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class ApplicationStorageTest {
    @Test
    fun legacyApplicationValuesMigrateToTheCommonVaultAndSurviveAccountRemoval() = runTest {
        val factory = StorageTestFactory()
        val manager = SecureStorageManager(factory, "migration")
        val ids = listOf("installation", "account").iterator()
        val accounts = UniAccountStore(manager) { ids.next() }
        val account = accounts.persistAuthenticatedAccount(
            UniAccountCredentials("student", "secret"),
            UniUserProfile("server", "Student", "Course", null, null, null, false),
            UniSessionTicket.restore(byteArrayOf(1)),
        )
        val legacyKey = "local.v1.application.${UniAppDataKeys.ThemeSelection.name}"
        manager.registry().putBytes(legacyKey, "4".encodeToByteArray())
        val store = EncryptedUniLocalDataStore(accounts)
        assertEquals(4, store.read(LocalDataScope.Application, UniAppDataKeys.ThemeSelection))
        assertFalse(manager.registry().contains(legacyKey))
        assertTrue(manager.vault("application-data").contains(legacyKey))
        accounts.forgetAccount(account.accountId)
        store.invalidateAccount(account.accountId)
        // Reopen to verify disk persistence, independently of the in-memory cache.
        assertEquals(4, EncryptedUniLocalDataStore(UniAccountStore(manager)).read(LocalDataScope.Application, UniAppDataKeys.ThemeSelection))
    }

    @Test
    fun failedMigrationRetainsTheOriginalValueAndCanBeRetried() = runTest {
        val factory = StorageTestFactory()
        val manager = SecureStorageManager(factory, "migration")
        val key = "local.v1.application.${UniAppDataKeys.ThemeSelection.name}"
        manager.registry().putBytes(key, "3".encodeToByteArray())
        factory.failingScope = "migration.vault.application-data"
        val store = EncryptedUniLocalDataStore(UniAccountStore(manager))
        assertFailsWith<IllegalStateException> { store.read(LocalDataScope.Application, UniAppDataKeys.ThemeSelection) }
        assertContentEquals("3".encodeToByteArray(), manager.registry().getBytes(key))
        factory.failingScope = null
        assertEquals(3, store.read(LocalDataScope.Application, UniAppDataKeys.ThemeSelection))
        assertFalse(manager.registry().contains(key))
    }

    @Test
    fun repeatedReadsAndUnrelatedWritesDoNotDecryptTheSameValueAgain() = runTest {
        val factory = StorageTestFactory()
        val store = EncryptedUniLocalDataStore(UniAccountStore(SecureStorageManager(factory, "cache")))
        store.read(LocalDataScope.Application, UniAppDataKeys.ThemeSelection)
        val reads = factory.reads
        repeat(5) {
            store.read(LocalDataScope.Application, UniAppDataKeys.ThemeSelection)
            store.write(LocalDataScope.Application, UniAppDataKeys.ThemeCustomColor, "$it")
        }
        assertEquals(reads, factory.reads)
        store.write(LocalDataScope.Application, UniAppDataKeys.ThemeSelection, 4)
        assertEquals(4, store.read(LocalDataScope.Application, UniAppDataKeys.ThemeSelection))
        assertEquals(reads, factory.reads)
    }

    @Test
    fun accountAndCareerValuesRemainIsolatedAndMemoryIsClearedAfterRemoval() = runTest {
        val factory = StorageTestFactory()
        val manager = SecureStorageManager(factory, "isolation")
        val ids = listOf("installation", "account").iterator()
        val accounts = UniAccountStore(manager) { ids.next() }
        accounts.persistAuthenticatedAccount(UniAccountCredentials("student", "secret"),
            UniUserProfile("server", "Student", "Course", null, null, null, false), UniSessionTicket.restore(byteArrayOf(1)))
        val store = EncryptedUniLocalDataStore(accounts)
        val a = LocalDataScope.Profile("account", "A")
        val b = LocalDataScope.Profile("account", "B")
        store.write(a, UniAppDataKeys.ThemeSelection, 4)
        assertEquals(0, store.read(b, UniAppDataKeys.ThemeSelection))
        assertEquals(4, store.read(a, UniAppDataKeys.ThemeSelection))
        accounts.forgetAccount("account")
        store.invalidateAccount("account")
        assertFailsWith<IllegalArgumentException> { store.read(a, UniAppDataKeys.ThemeSelection) }
    }
}
