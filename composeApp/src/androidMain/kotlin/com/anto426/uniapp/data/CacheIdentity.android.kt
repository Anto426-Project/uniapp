package com.anto426.uniapp.data

import android.util.Log
import java.security.MessageDigest

internal actual fun cacheIdentityDigest(value: String): String =
    MessageDigest.getInstance("SHA-256").digest(value.encodeToByteArray())
        .joinToString("") { it.toUByte().toString(16).padStart(2, '0') }

internal actual fun logCacheDiagnostic(message: String) {
    try {
        Log.w("UniCache", message)
    } catch (_: Throwable) {
        // Ignore in JVM host unit tests where android.util.Log is unmocked.
    }
}
