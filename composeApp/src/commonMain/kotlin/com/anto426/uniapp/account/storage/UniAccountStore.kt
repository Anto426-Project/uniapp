package com.anto426.uniapp.account.storage

import com.anto426.securestorage.SecureStorageManager
import com.anto426.securestorage.getObject
import com.anto426.securestorage.getString
import com.anto426.securestorage.putObject
import com.anto426.securestorage.putString
import com.anto426.uniapp.account.model.MissingAccountCredentialsException
import com.anto426.uniapp.account.model.UniAccountCredentials
import com.anto426.uniapp.account.model.UniAccountRegistrySnapshot
import com.anto426.uniapp.account.model.UniAccountSummary
import com.anto426.uniapp.account.platform.generateAccountStorageIdentifier
import com.anto426.unisdk.session.UniCredentials
import com.anto426.unisdk.session.UniSessionTicket
import com.anto426.unisdk.session.UniUserProfile
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable

/**
 * UniApp-owned encrypted account registry.
 *
 * The registry contains only account metadata. Credentials and the opaque primary-session ticket
 * live in the account's independently erasable encrypted vault.
 */
class UniAccountStore(
    private val storageManager: SecureStorageManager,
    private val generateIdentifier: () -> String = ::generateAccountStorageIdentifier,
) {
    private val lock = Mutex()

    suspend fun snapshot(): UniAccountRegistrySnapshot =
        lock.withLock { loadRegistry().toSnapshot() }

    internal suspend fun persistAuthenticatedAccount(
        credentials: UniAccountCredentials,
        profile: UniUserProfile,
        ticket: UniSessionTicket,
        preferredAccountId: String? = null,
    ): UniAccountSummary =
        lock.withLock {
            val registry = loadRegistry()
            val existing =
                if (preferredAccountId != null) {
                    registry.accounts.firstOrNull { it.accountId == preferredAccountId }
                        ?: throw IllegalArgumentException("Unknown preferred account")
                } else {
                    registry.accounts.firstOrNull { account ->
                        account.serverUserId == profile.id && account.matricola == profile.matricola
                    }
                }
            val accountId = existing?.accountId ?: newUniqueAccountId(registry)
            val account = profile.toStoredAccount(accountId)
            val accountStorage = storageManager.vault(accountId)
            val exportedTicket = ticket.export()

            try {
                accountStorage.putString(USERNAME_KEY, credentials.username)
                accountStorage.putString(PASSWORD_KEY, credentials.password)
                accountStorage.putBytes(SESSION_TICKET_KEY, exportedTicket)
                storageManager.registry().putObject(
                    REGISTRY_KEY,
                    registry.copy(
                        activeAccountId = accountId,
                        accounts = registry.accounts.upsert(account),
                    ),
                )
            } finally {
                exportedTicket.fill(0)
            }

            account.toSummary()
        }

    internal suspend fun updateSession(
        accountId: String,
        profile: UniUserProfile,
        ticket: UniSessionTicket,
    ): UniAccountSummary =
        lock.withLock {
            val registry = loadRegistry()
            require(registry.accounts.any { it.accountId == accountId }) { "Unknown account" }
            val updated = profile.toStoredAccount(accountId)
            val exportedTicket = ticket.export()
            try {
                storageManager.vault(accountId).putBytes(SESSION_TICKET_KEY, exportedTicket)
                storageManager.registry().putObject(
                    REGISTRY_KEY,
                    registry.copy(accounts = registry.accounts.upsert(updated)),
                )
            } finally {
                exportedTicket.fill(0)
            }
            updated.toSummary()
        }

    internal suspend fun loadSessionTicket(accountId: String): UniSessionTicket? =
        lock.withLock {
            requireKnownAccount(loadRegistry(), accountId)
            val bytes = storageManager.vault(accountId).getBytes(SESSION_TICKET_KEY) ?: return@withLock null
            try {
                UniSessionTicket.restore(bytes)
            } finally {
                bytes.fill(0)
            }
        }

    internal suspend fun clearSessionTicket(accountId: String) {
        lock.withLock {
            requireKnownAccount(loadRegistry(), accountId)
            storageManager.vault(accountId).remove(SESSION_TICKET_KEY)
        }
    }

    internal suspend fun readCachedData(
        accountId: String,
        key: String,
    ): ByteArray? =
        lock.withLock {
            requireKnownAccount(loadRegistry(), accountId)
            storageManager.vault(accountId).getBytes(cacheKey(key))
        }

    internal suspend fun writeCachedData(
        accountId: String,
        key: String,
        value: ByteArray,
    ) {
        lock.withLock {
            requireKnownAccount(loadRegistry(), accountId)
            storageManager.vault(accountId).putBytes(cacheKey(key), value)
        }
    }

    internal suspend fun removeCachedData(
        accountId: String,
        key: String,
    ) {
        lock.withLock {
            requireKnownAccount(loadRegistry(), accountId)
            storageManager.vault(accountId).remove(cacheKey(key))
        }
    }

    internal suspend fun <T> withCredentials(
        accountId: String,
        block: suspend (UniCredentials) -> T,
    ): T {
        val credentials =
            lock.withLock {
                requireKnownAccount(loadRegistry(), accountId)
                val accountStorage = storageManager.vault(accountId)
                val username = accountStorage.getString(USERNAME_KEY)
                    ?: throw MissingAccountCredentialsException(accountId)
                val password = accountStorage.getString(PASSWORD_KEY)
                    ?: throw MissingAccountCredentialsException(accountId)
                UniCredentials(username, password)
            }
        return block(credentials)
    }

    suspend fun setActiveAccount(accountId: String?) {
        lock.withLock {
            val registry = loadRegistry()
            if (accountId != null) requireKnownAccount(registry, accountId)
            storageManager.registry().putObject(
                REGISTRY_KEY,
                registry.copy(activeAccountId = accountId),
            )
        }
    }

    suspend fun forgetAccount(accountId: String) {
        lock.withLock {
            val registry = loadRegistry()
            requireKnownAccount(registry, accountId)

            // Destroy the per-account key before removing the registry reference. A failed registry
            // update can leave a visible signed-out account, never recoverable secret material.
            storageManager.destroyVault(accountId)
            storageManager.registry().putObject(
                REGISTRY_KEY,
                registry.copy(
                    activeAccountId = registry.activeAccountId.takeUnless { it == accountId },
                    accounts = registry.accounts.filterNot { it.accountId == accountId },
                ),
            )
        }
    }

    suspend fun destroyAll() {
        lock.withLock {
            val registry = loadRegistry()
            storageManager.destroyAll(registry.accounts.map(StoredAccount::accountId))
        }
    }

    private suspend fun loadRegistry(): StoredAccountRegistry {
        val storage = storageManager.registry()
        val existing = storage.getObject<StoredAccountRegistry>(REGISTRY_KEY)
        if (existing != null) {
            check(existing.schemaVersion == REGISTRY_SCHEMA_VERSION) {
                "Unsupported account registry version ${existing.schemaVersion}"
            }
            return existing.normalized()
        }

        val created =
            StoredAccountRegistry(
                schemaVersion = REGISTRY_SCHEMA_VERSION,
                installationId = newIdentifier("installation"),
            )
        storage.putObject(REGISTRY_KEY, created)
        return created
    }

    private fun newIdentifier(label: String): String {
        val id = generateIdentifier().trim()
        require(id.isNotEmpty()) { "$label identifier cannot be blank" }
        require(id.length <= 160) { "$label identifier is too long" }
        require(id.all { it.isLetterOrDigit() || it == '.' || it == '_' || it == '-' }) {
            "$label identifier contains unsupported characters"
        }
        return id
    }

    private fun newUniqueAccountId(registry: StoredAccountRegistry): String {
        repeat(MAX_IDENTIFIER_ATTEMPTS) {
            val candidate = newIdentifier("account")
            if (registry.accounts.none { it.accountId == candidate }) return candidate
        }
        error("Unable to generate a unique account identifier")
    }

    private fun requireKnownAccount(registry: StoredAccountRegistry, accountId: String) {
        require(registry.accounts.any { it.accountId == accountId }) { "Unknown account" }
    }

    private fun cacheKey(key: String): String {
        val normalized = key.trim()
        require(normalized.isNotEmpty()) { "Cache key cannot be blank" }
        require(normalized.length <= 180) { "Cache key is too long" }
        require(normalized.all { it.isLetterOrDigit() || it == '.' || it == '_' || it == '-' }) {
            "Cache key contains unsupported characters"
        }
        return "cache.v1.$normalized"
    }

    private companion object {
        const val REGISTRY_SCHEMA_VERSION = 1
        const val REGISTRY_KEY = "account-registry"
        const val USERNAME_KEY = "credentials.username"
        const val PASSWORD_KEY = "credentials.password"
        const val SESSION_TICKET_KEY = "session.primary.ticket"
        const val MAX_IDENTIFIER_ATTEMPTS = 8
    }
}

@Serializable
private data class StoredAccountRegistry(
    val schemaVersion: Int,
    val installationId: String,
    val activeAccountId: String? = null,
    val accounts: List<StoredAccount> = emptyList(),
) {
    fun normalized(): StoredAccountRegistry {
        val uniqueAccounts = accounts.distinctBy(StoredAccount::accountId)
        return copy(
            activeAccountId = activeAccountId?.takeIf { active ->
                uniqueAccounts.any { it.accountId == active }
            },
            accounts = uniqueAccounts,
        )
    }

    fun toSnapshot(): UniAccountRegistrySnapshot =
        UniAccountRegistrySnapshot(
            installationId = installationId,
            activeAccountId = activeAccountId,
            accounts = accounts.map(StoredAccount::toSummary),
        )
}

@Serializable
private data class StoredAccount(
    val accountId: String,
    val serverUserId: String,
    val displayName: String,
    val degreeName: String,
    val matricola: String? = null,
    val email: String? = null,
    val photoUrl: String? = null,
    val isGuest: Boolean,
) {
    fun toSummary(): UniAccountSummary =
        UniAccountSummary(
            accountId = accountId,
            serverUserId = serverUserId,
            displayName = displayName,
            degreeName = degreeName,
            matricola = matricola,
            email = email,
            photoUrl = photoUrl,
            isGuest = isGuest,
        )
}

private fun UniUserProfile.toStoredAccount(accountId: String): StoredAccount =
    StoredAccount(
        accountId = accountId,
        serverUserId = id,
        displayName = displayName,
        degreeName = degreeName,
        matricola = matricola,
        email = email,
        photoUrl = photoUrl,
        isGuest = isGuest,
    )

private fun List<StoredAccount>.upsert(account: StoredAccount): List<StoredAccount> =
    filterNot { it.accountId == account.accountId } + account
