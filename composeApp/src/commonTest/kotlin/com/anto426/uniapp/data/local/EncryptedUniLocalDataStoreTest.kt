package com.anto426.uniapp.data.local

import com.anto426.securestorage.SecureStorage
import com.anto426.securestorage.SecureStorageFactory
import com.anto426.securestorage.SecureStorageManager
import com.anto426.uniapp.account.storage.UniAccountStore
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EncryptedUniLocalDataStoreTest {
    @Test
    fun applicationValuesAreTypedAndCanBeRemoved() = runTest {
        val store = createStore()

        assertEquals(0, store.read(LocalDataScope.Application, UniAppDataKeys.ThemeSelection))
        assertNull(store.read(LocalDataScope.Application, UniAppDataKeys.ThemeCustomColor))

        store.write(LocalDataScope.Application, UniAppDataKeys.ThemeSelection, 4)
        store.write(LocalDataScope.Application, UniAppDataKeys.ThemeCustomColor, "4281558681")

        assertEquals(4, store.read(LocalDataScope.Application, UniAppDataKeys.ThemeSelection))
        assertEquals(
            "4281558681",
            store.read(LocalDataScope.Application, UniAppDataKeys.ThemeCustomColor),
        )

        store.remove(LocalDataScope.Application, UniAppDataKeys.ThemeCustomColor)
        assertNull(store.read(LocalDataScope.Application, UniAppDataKeys.ThemeCustomColor))
    }

    private fun createStore(): UniLocalDataStore {
        val manager = SecureStorageManager(MemorySecureStorageFactory(), "uniapp-test")
        return EncryptedUniLocalDataStore(UniAccountStore(manager))
    }
}

private class MemorySecureStorageFactory : SecureStorageFactory {
    private val storages = mutableMapOf<String, MemorySecureStorage>()

    override fun open(scope: String): SecureStorage =
        storages.getOrPut(scope, ::MemorySecureStorage)
}

private class MemorySecureStorage : SecureStorage {
    private val values = mutableMapOf<String, ByteArray>()

    override suspend fun putBytes(key: String, value: ByteArray) {
        values[key] = value.copyOf()
    }

    override suspend fun getBytes(key: String): ByteArray? = values[key]?.copyOf()

    override suspend fun remove(key: String) {
        values.remove(key)?.fill(0)
    }

    override suspend fun contains(key: String): Boolean = key in values

    override suspend fun destroy() {
        values.values.forEach { it.fill(0) }
        values.clear()
    }
}
