@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.anto426.uniapp.data

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.CoreCrypto.CC_SHA256

internal actual fun cacheIdentityDigest(value: String): String {
    val bytes = value.encodeToByteArray()
    val input = if (bytes.isEmpty()) byteArrayOf(0) else bytes
    val digest = ByteArray(32)
    input.usePinned { source ->
        digest.usePinned { target ->
            CC_SHA256(source.addressOf(0), bytes.size.toUInt(), target.addressOf(0).reinterpret())
        }
    }
    return digest.joinToString("") { it.toUByte().toString(16).padStart(2, '0') }
}
