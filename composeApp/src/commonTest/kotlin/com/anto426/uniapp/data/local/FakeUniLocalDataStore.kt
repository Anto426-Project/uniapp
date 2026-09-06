package com.anto426.uniapp.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

internal class FakeUniLocalDataStore : UniLocalDataStore {
    private val values = mutableMapOf<Pair<LocalDataScope, String>, Any?>()
    private val revision = MutableStateFlow(0L)

    @Suppress("UNCHECKED_CAST")
    override suspend fun <T> read(scope: LocalDataScope, key: LocalDataKey<T>): T =
        if (contains(scope, key)) {
            values[scope to key.name] as T
        } else {
            key.defaultValue
        }

    override suspend fun <T> write(scope: LocalDataScope, key: LocalDataKey<T>, value: T) {
        values[scope to key.name] = value
        revision.value += 1
    }

    override suspend fun remove(scope: LocalDataScope, key: LocalDataKey<*>) {
        values.remove(scope to key.name)
        revision.value += 1
    }

    override fun <T> observe(scope: LocalDataScope, key: LocalDataKey<T>): Flow<T> =
        revision.map { read(scope, key) }.distinctUntilChanged()

    fun contains(scope: LocalDataScope, key: LocalDataKey<*>): Boolean =
        values.containsKey(scope to key.name)
}
