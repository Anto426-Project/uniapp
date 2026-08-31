package com.anto426.uniapp.account.platform

internal const val UNIAPP_STORAGE_SCOPE = "com.anto426.uniapp"

/** Cryptographically random, non-PII identifier suitable for encrypted storage scopes. */
expect fun generateAccountStorageIdentifier(): String
