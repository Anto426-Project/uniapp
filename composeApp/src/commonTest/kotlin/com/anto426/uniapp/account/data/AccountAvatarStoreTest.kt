package com.anto426.uniapp.account.data

import com.anto426.securestorage.SecureStorageManager
import com.anto426.uniapp.account.model.UniAccountCredentials
import com.anto426.uniapp.account.storage.UniAccountStore
import com.anto426.uniapp.testing.StorageTestFactory
import com.anto426.unisdk.session.UniSessionTicket
import com.anto426.unisdk.session.UniUserProfile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class AccountAvatarStoreTest {
    @Test
    fun unchangedPortraitReusesTheSameBytesWhileChangedPortraitInvalidatesDecoding() = runTest {
        val store = AccountAvatarStore(UniAccountStore(SecureStorageManager(StorageTestFactory(), "avatars")))
        val bytes = store.publish("account", "url", byteArrayOf(1))
        val key = store.images.value.getValue("account").cacheKey
        assertSame(bytes, store.publish("account", "url", byteArrayOf(1)))
        assertEquals(key, store.images.value.getValue("account").cacheKey)
        store.publish("account", "url", byteArrayOf(2))
        assertNotEquals(key, store.images.value.getValue("account").cacheKey)
        store.remove("account")
        assertTrue(store.images.value.isEmpty())
        assertFailsWith<CancellationException> {
            store.publish("account", "url", byteArrayOf(3)) { throw CancellationException("Owner changed") }
        }
        assertTrue(store.images.value.isEmpty())
    }

    @Test
    fun restoreUsesThePersistedPortraitSourceAndDoesNotOverwriteANewerDownload() = runTest {
        val ids = listOf("installation", "account").iterator()
        val accounts = UniAccountStore(SecureStorageManager(StorageTestFactory(), "avatars")) { ids.next() }
        val account = accounts.persistAuthenticatedAccount(UniAccountCredentials("student", "secret"),
            UniUserProfile("server", "Student", "Course", null, null, "old-url", false), UniSessionTicket.restore(byteArrayOf(1)))
        accounts.writeProfileImage(account.accountId, "current-url", 1_000, byteArrayOf(1))
        val store = AccountAvatarStore(accounts)
        store.restore(listOf(account))
        assertEquals("current-url", store.images.value.getValue(account.accountId).source)
        val downloaded = store.publish(account.accountId, "new-url", byteArrayOf(2))
        store.restore(listOf(account))
        assertSame(downloaded, store.images.value.getValue(account.accountId).bytes)
    }
}
