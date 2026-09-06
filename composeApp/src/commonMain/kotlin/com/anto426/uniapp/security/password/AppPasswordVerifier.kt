package com.anto426.uniapp.security.password

import kotlinx.serialization.Serializable

@Serializable
data class AppPasswordVerifier(
    val saltHex: String,
    val derivedKeyHex: String,
    val iterations: Int,
)

internal expect fun createAppPasswordVerifier(password: String): AppPasswordVerifier

internal expect fun verifyAppPassword(password: String, verifier: AppPasswordVerifier): Boolean

internal fun ByteArray.toHexString(): String =
    joinToString(separator = "") { byte -> byte.toUByte().toString(radix = 16).padStart(2, '0') }

internal fun String.hexToByteArray(): ByteArray {
    require(length % 2 == 0) { "Hex value must have an even length" }
    return ByteArray(length / 2) { index ->
        substring(index * 2, index * 2 + 2).toInt(radix = 16).toByte()
    }
}

internal fun ByteArray.constantTimeEquals(other: ByteArray): Boolean {
    var difference = size xor other.size
    val maxSize = maxOf(size, other.size)
    repeat(maxSize) { index ->
        val left = if (index < size) this[index].toInt() else 0
        val right = if (index < other.size) other[index].toInt() else 0
        difference = difference or (left xor right)
    }
    return difference == 0
}
