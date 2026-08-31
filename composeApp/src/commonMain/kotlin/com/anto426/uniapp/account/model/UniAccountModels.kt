package com.anto426.uniapp.account.model

/** Credentials owned by UniApp. They are never included in the SDK session ticket. */
class UniAccountCredentials(
    username: String,
    password: String,
) {
    internal val username = username
    internal val password = password

    init {
        require(username.isNotBlank()) { "The username cannot be blank" }
        require(password.isNotEmpty()) { "The password cannot be empty" }
    }

    override fun toString(): String = "UniAccountCredentials([REDACTED])"
}

data class UniAccountSummary(
    val accountId: String,
    val serverUserId: String,
    val displayName: String,
    val degreeName: String,
    val matricola: String?,
    val email: String?,
    val photoUrl: String?,
    val isGuest: Boolean,
)

data class UniAccountRegistrySnapshot(
    val installationId: String,
    val activeAccountId: String?,
    val accounts: List<UniAccountSummary>,
)

class MissingAccountCredentialsException(accountId: String) :
    IllegalStateException("Encrypted credentials are missing for account '$accountId'")
