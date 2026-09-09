package com.anto426.uniapp.account.data

import com.anto426.uniapp.account.model.UniAccountSummary
import com.anto426.uniapp.account.storage.UniAccountStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** The same byte array and decode-cache identity are shared by every account avatar. */
data class AccountAvatar(val bytes: ByteArray, val cacheKey: String, val source: String)

class AccountAvatarStore internal constructor(private val accounts: UniAccountStore) {
    private val lock = Mutex()
    private val mutableImages = MutableStateFlow<Map<String, AccountAvatar>>(emptyMap())
    val images = mutableImages.asStateFlow()
    private var revision = 0L
    private val identity = com.anto426.uniapp.account.platform.generateAccountStorageIdentifier()
    private val restoreLock = Mutex()

    suspend fun publish(accountId: String, source: String, bytes: ByteArray, ensureCurrent: () -> Unit = {}): ByteArray = lock.withLock {
        ensureCurrent()
        val previous = mutableImages.value[accountId]
        if (previous?.source == source && previous.bytes.contentEquals(bytes)) return@withLock previous.bytes
        revision += 1
        mutableImages.value = mutableImages.value + (accountId to AccountAvatar(bytes, "account-avatar:$identity:$accountId:$revision", source))
        bytes
    }

    suspend fun restore(accountsToLoad: List<UniAccountSummary>) = restoreLock.withLock {
        for (account in accountsToLoad) {
            if (images.value.containsKey(account.accountId)) continue
            val cached = try { accounts.readProfileImage(account.accountId) }
                catch (error: kotlinx.coroutines.CancellationException) { throw error }
                catch (_: Exception) { null } ?: continue
            val source = cached.source
            lock.withLock {
                // A download may have completed while the disk was read; never replace it.
                if (!mutableImages.value.containsKey(account.accountId)) {
                    revision += 1
                    mutableImages.value = mutableImages.value + (account.accountId to AccountAvatar(cached.bytes, "account-avatar:$identity:${account.accountId}:$revision", source))
                }
            }
        }
    }

    suspend fun remove(accountId: String) = lock.withLock {
        mutableImages.value = mutableImages.value - accountId
    }
}
