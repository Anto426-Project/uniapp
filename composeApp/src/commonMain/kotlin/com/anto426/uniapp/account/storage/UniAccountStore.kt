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
import com.anto426.uniapp.account.model.UniAccountProfileSummary
import com.anto426.unisdk.backend.model.BackendCareerType
import com.anto426.uniapp.account.platform.generateAccountStorageIdentifier
import com.anto426.unisdk.session.UniCredentials
import com.anto426.unisdk.session.UniSessionTicket
import com.anto426.unisdk.session.UniUserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
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
    private val resetStorageForRelease: Boolean = false,
    private val resetPlatformStorage: suspend () -> Unit = {},
    private val generateIdentifier: () -> String = ::generateAccountStorageIdentifier,
) {
    private val lock = Mutex()
    private val applicationLock = Mutex()
    private var cachedRegistry: StoredAccountRegistry? = null
    private val resetLock = Mutex()
    private val resetReady = MutableStateFlow(!resetStorageForRelease)

    private val mutableStorageResetNotice = MutableStateFlow(false)
    internal val storageResetNotice = mutableStorageResetNotice.asStateFlow()

    /** Clears the previous local format once; subsequent logins remain stored. */
    private suspend fun prepareStorage() {
        if (resetReady.value) return
        withContext(Dispatchers.Default) {
            resetLock.withLock {
                if (!resetReady.value) {
                    val maintenance = storageManager.vault("storage-reset")
                    when (val status = maintenance.getString(RELEASE_STORAGE_RESET_KEY)) {
                        "complete" -> Unit
                        "notice-pending" -> mutableStorageResetNotice.value = true
                        else -> {
                            val registryStorage = storageManager.registry()
                            val registry = registryStorage.getObject<StoredAccountRegistry>(REGISTRY_KEY)
                            val showNotice = status == "resetting-notice" || registry != null
                            maintenance.putString(RELEASE_STORAGE_RESET_KEY, if (showNotice) "resetting-notice" else "resetting")
                            storageManager.destroyAll(registry?.accounts.orEmpty().map(StoredAccount::accountId) + listOf("application-data", "storage-migrations"))
                            cachedRegistry = null
                            resetPlatformStorage()
                            // The reset marker survives cleanup and interruption retries.
                            maintenance.putString(RELEASE_STORAGE_RESET_KEY, if (showNotice) "notice-pending" else "complete")
                            mutableStorageResetNotice.value = showNotice
                        }
                    }
                    resetReady.value = true
                }
            }
        }
    }

    internal suspend fun acknowledgeStorageReset() {
        prepareStorage()
        resetLock.withLock {
            storageManager.vault("storage-reset").putString(RELEASE_STORAGE_RESET_KEY, "complete")
            mutableStorageResetNotice.value = false
        }
    }

    private suspend fun <T> withAccountStorage(block: suspend () -> T): T {
        prepareStorage()
        return lock.withLock { block() }
    }

    private suspend fun <T> withApplicationStorage(block: suspend () -> T): T {
        prepareStorage()
        return applicationLock.withLock { block() }
    }

    suspend fun snapshot(): UniAccountRegistrySnapshot =
        withAccountStorage { loadRegistry().toSnapshot() }

    internal suspend fun persistAuthenticatedAccount(
        credentials: UniAccountCredentials,
        profile: UniUserProfile,
        ticket: UniSessionTicket,
        preferredAccountId: String? = null,
    ): UniAccountSummary =
        withAccountStorage {
            val registry = loadRegistry()
            val identityAccounts = registry.accounts.filter { it.serverUserId == profile.id }
            val existing =
                if (preferredAccountId != null) {
                    registry.accounts.firstOrNull { it.accountId == preferredAccountId }
                        ?: throw IllegalArgumentException("Unknown preferred account")
                    identityAccounts.firstOrNull { it.accountId == preferredAccountId }
                        ?: identityAccounts.firstOrNull { it.accountId == registry.activeAccountId }
                        ?: identityAccounts.firstOrNull()
                } else {
                    identityAccounts.firstOrNull { it.accountId == registry.activeAccountId }
                        ?: identityAccounts.firstOrNull()
                }
            val accountId = existing?.accountId ?: newUniqueAccountId(registry)
            val account = profile.toStoredAccount(accountId, identityAccounts)
            val accountStorage = storageManager.vault(accountId)
            val exportedTicket = ticket.export()

            try {
                accountStorage.putString(USERNAME_KEY, credentials.username)
                accountStorage.putString(PASSWORD_KEY, credentials.password)
                accountStorage.putBytes(SESSION_TICKET_KEY, exportedTicket)
                persistRegistry(
                    registry.copy(
                        activeAccountId = accountId,
                        accounts =
                            registry.accounts
                                .filterNot { it.serverUserId == profile.id }
                                .upsert(account),
                    ),
                )
                identityAccounts
                    .asSequence()
                    .map(StoredAccount::accountId)
                    .filterNot { it == accountId }
                    .forEach { duplicateId -> runCatching { storageManager.destroyVault(duplicateId) } }
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
        withAccountStorage {
            val registry = loadRegistry()
            val existing = registry.accounts.firstOrNull { it.accountId == accountId }
                ?: throw IllegalArgumentException("Unknown account")
            require(existing.serverUserId == profile.id) { "Session identity does not match the selected account" }
            val updated = profile.toStoredAccount(accountId, listOf(existing))
            val exportedTicket = ticket.export()
            try {
                storageManager.vault(accountId).putBytes(SESSION_TICKET_KEY, exportedTicket)
                persistRegistry(
                    registry.copy(accounts = registry.accounts.upsert(updated)),
                )
            } finally {
                exportedTicket.fill(0)
            }
            updated.toSummary()
        }

    internal suspend fun loadSessionTicket(accountId: String): UniSessionTicket? =
        withAccountStorage {
            requireKnownAccount(loadRegistry(), accountId)
            val bytes = storageManager.vault(accountId).getBytes(SESSION_TICKET_KEY) ?: return@withAccountStorage null
            try {
                UniSessionTicket.restore(bytes)
            } finally {
                bytes.fill(0)
            }
        }

    internal suspend fun matchesStoredUsername(accountId: String, username: String): Boolean =
        withAccountStorage {
            requireKnownAccount(loadRegistry(), accountId)
            storageManager.vault(accountId).getString(USERNAME_KEY)
                ?.trim()?.equals(username.trim(), ignoreCase = true) == true
        }

    internal suspend fun clearSessionTicket(accountId: String) {
        withAccountStorage {
            requireKnownAccount(loadRegistry(), accountId)
            storageManager.vault(accountId).remove(SESSION_TICKET_KEY)
        }
    }

    /** A null account identifies application-wide data; profile keys remain account-owned. */
    internal suspend fun readCachedData(accountId: String?, key: String): ByteArray? =
        if (accountId == null) withApplicationStorage {
            storageManager.vault("application-data").getBytes(cacheKey(key))
        } else withAccountStorage {
            requireKnownAccount(loadRegistry(), accountId)
            storageManager.vault(accountId).getBytes(cacheKey(key))
        }

    internal suspend fun writeCachedData(accountId: String?, key: String, value: ByteArray) {
        if (accountId == null) withApplicationStorage {
            storageManager.vault("application-data").putBytes(cacheKey(key), value)
        } else withAccountStorage {
            requireKnownAccount(loadRegistry(), accountId)
            storageManager.vault(accountId).putBytes(cacheKey(key), value)
        }
    }

    internal suspend fun removeCachedData(accountId: String?, key: String) {
        if (accountId == null) withApplicationStorage {
            storageManager.vault("application-data").remove(cacheKey(key))
        } else withAccountStorage {
            requireKnownAccount(loadRegistry(), accountId)
            storageManager.vault(accountId).remove(cacheKey(key))
        }
    }

    /** App-wide content has its own vault, independent of registry and account removal. */
    internal suspend fun readApplicationData(key: String): ByteArray? = withApplicationStorage {
        storageManager.vault("application-data").getBytes(localDataKey(key))
    }

    internal suspend fun writeApplicationData(key: String, value: ByteArray) = withApplicationStorage {
        storageManager.vault("application-data").putBytes(localDataKey(key), value)
    }

    internal suspend fun removeApplicationData(key: String) = withApplicationStorage {
        storageManager.vault("application-data").remove(localDataKey(key))
    }

    /** Account-owned data kept separate from credentials, sessions and response caches. */
    internal suspend fun readAccountData(accountId: String, key: String): ByteArray? =
        withAccountStorage {
            requireKnownAccount(loadRegistry(), accountId)
            storageManager.vault(accountId).getBytes(localDataKey(key))
        }

    internal suspend fun writeAccountData(accountId: String, key: String, value: ByteArray) {
        withAccountStorage {
            requireKnownAccount(loadRegistry(), accountId)
            storageManager.vault(accountId).putBytes(localDataKey(key), value)
        }
    }

    internal suspend fun removeAccountData(accountId: String, key: String) {
        withAccountStorage {
            requireKnownAccount(loadRegistry(), accountId)
            storageManager.vault(accountId).remove(localDataKey(key))
        }
    }

    internal suspend fun readProfileImage(
        accountId: String,
        expectedSource: String? = null,
    ): CachedProfileImage? =
        withAccountStorage {
            requireKnownAccount(loadRegistry(), accountId)
            val vault = storageManager.vault(accountId)
            val storedSource = vault.getString(PROFILE_IMAGE_SOURCE_KEY) ?: return@withAccountStorage null
            if (expectedSource != null && storedSource != expectedSource) return@withAccountStorage null
            val savedAt = vault.getString(PROFILE_IMAGE_SAVED_AT_KEY)?.toLongOrNull() ?: return@withAccountStorage null
            val bytes = vault.getBytes(PROFILE_IMAGE_BYTES_KEY) ?: return@withAccountStorage null
            CachedProfileImage(savedAtMillis = savedAt, bytes = bytes, source = storedSource)
        }

    internal suspend fun writeProfileImage(
        accountId: String,
        source: String,
        savedAtMillis: Long,
        bytes: ByteArray,
    ) {
        withAccountStorage {
            requireKnownAccount(loadRegistry(), accountId)
            val vault = storageManager.vault(accountId)
            vault.putBytes(PROFILE_IMAGE_BYTES_KEY, bytes)
            vault.putString(PROFILE_IMAGE_SOURCE_KEY, source)
            vault.putString(PROFILE_IMAGE_SAVED_AT_KEY, savedAtMillis.toString())
        }
    }

    internal suspend fun <T> withCredentials(
        accountId: String,
        block: suspend (UniCredentials) -> T,
    ): T {
        val credentials =
            withAccountStorage {
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
        withAccountStorage {
            val registry = loadRegistry()
            if (accountId != null) requireKnownAccount(registry, accountId)
            persistRegistry(registry.copy(activeAccountId = accountId))
        }
    }

    suspend fun forgetAccount(accountId: String) {
        withAccountStorage {
            val registry = loadRegistry()
            requireKnownAccount(registry, accountId)

            // Destroy the per-account key before removing the registry reference. A failed registry
            // update can leave a visible signed-out account, never recoverable secret material.
            storageManager.destroyVault(accountId)
            persistRegistry(
                registry.copy(
                    activeAccountId = registry.activeAccountId.takeUnless { it == accountId },
                    accounts = registry.accounts.filterNot { it.accountId == accountId },
                ),
            )
        }
    }

    suspend fun destroyAll() {
        withAccountStorage {
            val registry = loadRegistry()
            storageManager.destroyAll(registry.accounts.map(StoredAccount::accountId) + "application-data")
            cachedRegistry = null
        }
    }

    private suspend fun loadRegistry(): StoredAccountRegistry {
        cachedRegistry?.let { return it }
        val storage = storageManager.registry()
        val existing = storage.getObject<StoredAccountRegistry>(REGISTRY_KEY)
        if (existing != null) {
            check(existing.schemaVersion == REGISTRY_SCHEMA_VERSION) {
                "Unsupported account registry version ${existing.schemaVersion}"
            }
            return existing.normalized().also { cachedRegistry = it }
        }

        val created =
            StoredAccountRegistry(
                schemaVersion = REGISTRY_SCHEMA_VERSION,
                installationId = newIdentifier("installation"),
        )
        storage.putObject(REGISTRY_KEY, created)
        cachedRegistry = created
        return created
    }

    private suspend fun persistRegistry(registry: StoredAccountRegistry) {
        val normalized = registry.normalized()
        storageManager.registry().putObject(REGISTRY_KEY, normalized)
        cachedRegistry = normalized
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

    private fun localDataKey(key: String): String {
        val normalized = key.trim()
        require(normalized.isNotEmpty()) { "Local data key cannot be blank" }
        require(normalized.length <= 180) { "Local data key is too long" }
        require(normalized.all { it.isLetterOrDigit() || it == '.' || it == '_' || it == '-' }) {
            "Local data key contains unsupported characters"
        }
        return "local.v1.$normalized"
    }

    private companion object {
        const val RELEASE_STORAGE_RESET_KEY = "storage-reset-20260929"
        const val REGISTRY_SCHEMA_VERSION = 1
        const val REGISTRY_KEY = "account-registry"
        const val USERNAME_KEY = "credentials.username"
        const val PASSWORD_KEY = "credentials.password"
        const val SESSION_TICKET_KEY = "session.primary.ticket"
        const val PROFILE_IMAGE_BYTES_KEY = "profile.image.bytes"
        const val PROFILE_IMAGE_SOURCE_KEY = "profile.image.source"
        const val PROFILE_IMAGE_SAVED_AT_KEY = "profile.image.saved-at"
        const val MAX_IDENTIFIER_ATTEMPTS = 8
    }
}

internal data class CachedProfileImage(
    val source: String,
    val savedAtMillis: Long,
    val bytes: ByteArray,
)

@Serializable
private data class StoredAccountRegistry(
    val schemaVersion: Int,
    val installationId: String,
    val activeAccountId: String? = null,
    val accounts: List<StoredAccount> = emptyList(),
) {
    fun normalized(): StoredAccountRegistry {
        val uniqueAccounts =
            accounts
                .distinctBy(StoredAccount::accountId)
                .map(StoredAccount::withNormalizedProfiles)
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
    val activeProfileId: String? = null,
    val profiles: List<StoredAccountProfile> = emptyList(),
    val activeProfileType: BackendCareerType = BackendCareerType.STUDENT,
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
            activeProfileId = activeProfileId,
            profiles = profiles.map(StoredAccountProfile::toSummary),
            activeProfileType = activeProfileType,
        )
}

@Serializable
private data class StoredAccountProfile(
    val profileId: String,
    val displayName: String,
    val degreeName: String,
    val matricola: String? = null,
    val matId: String? = null,
    val stuId: String? = null,
    val anaId: String? = null,
    val cdsId: String? = null,
    val dipId: String? = null,
    val departmentName: String? = null,
    val teacherId: String? = null,
    val type: BackendCareerType = BackendCareerType.STUDENT,
) {
    fun toSummary(): UniAccountProfileSummary =
        UniAccountProfileSummary(
            profileId = profileId,
            displayName = displayName,
            degreeName = degreeName,
            matricola = matricola,
            matId = matId,
            stuId = stuId,
            anaId = anaId,
            cdsId = cdsId,
            dipId = dipId,
            departmentName = departmentName,
            teacherId = teacherId,
            type = type,
        )
}

private fun UniUserProfile.toStoredAccount(
    accountId: String,
    previousAccounts: List<StoredAccount>,
): StoredAccount {
    val incomingProfiles =
        profiles.map { profile ->
            StoredAccountProfile(
                profileId = profile.profileId,
                displayName = profile.displayName,
                degreeName = profile.degreeName,
                matricola = profile.matricola,
                matId = profile.matId,
                stuId = profile.stuId,
                anaId = profile.anaId,
                cdsId = profile.cdsId,
                dipId = profile.dipId,
                departmentName = profile.departmentName,
                teacherId = profile.teacherId,
                type = profile.type,
            )
        }
    val mergedProfiles =
        coalesceStoredProfiles(
            profiles = incomingProfiles + previousAccounts.flatMap(StoredAccount::profiles),
            activeProfileId = activeProfileId,
        )
    return StoredAccount(
        accountId = accountId,
        serverUserId = id,
        displayName = displayName,
        degreeName = degreeName,
        matricola = matricola,
        email = email,
        photoUrl = photoUrl,
        isGuest = isGuest,
        activeProfileId = activeProfileId,
        profiles = mergedProfiles,
        activeProfileType = activeProfileType,
    )
}

private fun List<StoredAccount>.upsert(account: StoredAccount): List<StoredAccount> =
    filterNot { it.accountId == account.accountId } + account

private fun StoredAccount.withNormalizedProfiles(): StoredAccount =
    copy(
        profiles = coalesceStoredProfiles(profiles, activeProfileId),
    )

private fun coalesceStoredProfiles(
    profiles: List<StoredAccountProfile>,
    activeProfileId: String?,
): List<StoredAccountProfile> =
    profiles.distinctBy(StoredAccountProfile::profileId)
        .sortedByDescending { it.profileId == activeProfileId }
