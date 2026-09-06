package com.anto426.uniapp.data.local

import com.anto426.securestorage.SecureStorageJson
import com.anto426.uniapp.account.storage.UniAccountStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

/** Defines who owns a persisted value and therefore when that value is erased. */
sealed interface LocalDataScope {
    data object Application : LocalDataScope

    data class Account(val accountId: String) : LocalDataScope {
        init {
            require(accountId.isNotBlank()) { "Account id cannot be blank" }
        }
    }

    data class Profile(
        val accountId: String,
        val profileId: String,
    ) : LocalDataScope {
        init {
            require(accountId.isNotBlank()) { "Account id cannot be blank" }
            require(profileId.isNotBlank()) { "Profile id cannot be blank" }
        }
    }
}

/** A centrally declared, strongly typed persisted value. */
class LocalDataKey<T>(
    val name: String,
    internal val serializer: KSerializer<T>,
    val defaultValue: T,
) {
    init {
        require(name.isNotBlank()) { "Local data key cannot be blank" }
        require(name.length <= 120) { "Local data key is too long" }
        require(name.all { it.isLetterOrDigit() || it == '.' || it == '_' || it == '-' }) {
            "Local data key contains unsupported characters"
        }
    }
}

/** Single entry point for app preferences and other small device-local values. */
interface UniLocalDataStore {
    suspend fun <T> read(scope: LocalDataScope, key: LocalDataKey<T>): T

    suspend fun <T> write(scope: LocalDataScope, key: LocalDataKey<T>, value: T)

    suspend fun remove(scope: LocalDataScope, key: LocalDataKey<*>)

    fun <T> observe(scope: LocalDataScope, key: LocalDataKey<T>): Flow<T>
}

/**
 * Encrypted implementation backed by the existing application registry and isolated account
 * vaults. Profile values live in their account vault under a non-identifying namespace.
 */
internal class EncryptedUniLocalDataStore(
    private val accounts: UniAccountStore,
    private val json: Json = SecureStorageJson,
) : UniLocalDataStore {
    private val operationLock = Mutex()
    private val revision = MutableStateFlow(0L)

    override suspend fun <T> read(scope: LocalDataScope, key: LocalDataKey<T>): T =
        operationLock.withLock {
            readUnlocked(scope, key)
        }

    override suspend fun <T> write(scope: LocalDataScope, key: LocalDataKey<T>, value: T) {
        operationLock.withLock {
            val bytes = json.encodeToString(key.serializer, value).encodeToByteArray()
            try {
                writeBytes(scope, storageKey(scope, key.name), bytes)
                revision.value += 1
            } finally {
                bytes.fill(0)
            }
        }
    }

    override suspend fun remove(scope: LocalDataScope, key: LocalDataKey<*>) {
        operationLock.withLock {
            removeBytes(scope, storageKey(scope, key.name))
            revision.value += 1
        }
    }

    override fun <T> observe(scope: LocalDataScope, key: LocalDataKey<T>): Flow<T> =
        revision
            .map { read(scope, key) }
            .distinctUntilChanged()

    private suspend fun <T> readUnlocked(scope: LocalDataScope, key: LocalDataKey<T>): T {
        val bytes = readBytes(scope, storageKey(scope, key.name)) ?: return key.defaultValue
        return try {
            json.decodeFromString(key.serializer, bytes.decodeToString(throwOnInvalidSequence = true))
        } finally {
            bytes.fill(0)
        }
    }

    private suspend fun readBytes(scope: LocalDataScope, key: String): ByteArray? =
        when (scope) {
            LocalDataScope.Application -> accounts.readApplicationData(key)
            is LocalDataScope.Account -> accounts.readAccountData(scope.accountId, key)
            is LocalDataScope.Profile -> accounts.readAccountData(scope.accountId, key)
        }

    private suspend fun writeBytes(scope: LocalDataScope, key: String, value: ByteArray) {
        when (scope) {
            LocalDataScope.Application -> accounts.writeApplicationData(key, value)
            is LocalDataScope.Account -> accounts.writeAccountData(scope.accountId, key, value)
            is LocalDataScope.Profile -> accounts.writeAccountData(scope.accountId, key, value)
        }
    }

    private suspend fun removeBytes(scope: LocalDataScope, key: String) {
        when (scope) {
            LocalDataScope.Application -> accounts.removeApplicationData(key)
            is LocalDataScope.Account -> accounts.removeAccountData(scope.accountId, key)
            is LocalDataScope.Profile -> accounts.removeAccountData(scope.accountId, key)
        }
    }

    private fun storageKey(scope: LocalDataScope, key: String): String =
        when (scope) {
            LocalDataScope.Application -> "application.$key"
            is LocalDataScope.Account -> "account.$key"
            is LocalDataScope.Profile -> "profile.${scope.profileId.stableFingerprint()}.$key"
        }
}

private fun String.stableFingerprint(): String {
    var hash = 0xcbf29ce484222325uL
    encodeToByteArray().forEach { byte ->
        hash = (hash xor byte.toUByte().toULong()) * 0x100000001b3uL
    }
    return hash.toString(radix = 16)
}
