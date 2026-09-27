package com.anto426.uniapp.data

import java.security.MessageDigest

internal actual fun cacheIdentityDigest(value: String): String =
    MessageDigest.getInstance("SHA-256").digest(value.encodeToByteArray())
        .joinToString("") { it.toUByte().toString(16).padStart(2, '0') }
