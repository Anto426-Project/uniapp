package com.anto426.uniapp.testing

import com.anto426.securestorage.SecureStorage
import com.anto426.securestorage.SecureStorageFactory
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class StorageTestFactory : SecureStorageFactory {
    private val stores = mutableMapOf<String, SecureStorage>()
    var failingScope: String? = null
    var reads = 0
        private set
    private val lock = Mutex()
    override fun open(scope: String): SecureStorage = stores.getOrPut(scope) {
        object : SecureStorage {
            private val values = mutableMapOf<String, ByteArray>()
            override suspend fun putBytes(key: String, value: ByteArray) = lock.withLock {
                check(scope != failingScope) { "Storage unavailable" }
                values[key] = value.copyOf()
            }
            override suspend fun getBytes(key: String): ByteArray? = lock.withLock { reads++; values[key]?.copyOf() }
            override suspend fun contains(key: String): Boolean = lock.withLock { key in values }
            override suspend fun remove(key: String) { lock.withLock { values.remove(key)?.fill(0) } }
            override suspend fun destroy() { lock.withLock { values.values.forEach { it.fill(0) }; values.clear() } }
        }
    }
}
