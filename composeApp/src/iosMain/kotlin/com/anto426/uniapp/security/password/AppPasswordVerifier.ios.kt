@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.anto426.uniapp.security.password

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.CoreCrypto.CCKeyDerivationPBKDF
import platform.CoreCrypto.kCCPBKDF2
import platform.CoreCrypto.kCCPRFHmacAlgSHA256
import platform.Security.SecRandomCopyBytes
import platform.Security.errSecSuccess
import platform.Security.kSecRandomDefault

internal actual fun createAppPasswordVerifier(password: String): AppPasswordVerifier {
    require(password.length in 8..128 && password.isNotBlank()) { "Invalid app password length" }
    val salt = ByteArray(SALT_SIZE_BYTES)
    val randomStatus = salt.usePinned { pinned ->
        SecRandomCopyBytes(kSecRandomDefault, salt.size.toULong(), pinned.addressOf(0))
    }
    check(randomStatus == errSecSuccess) { "Unable to create password salt (OSStatus=$randomStatus)" }
    return try {
        val derivedKey = derivePasswordKey(password, salt, DEFAULT_ITERATIONS)
        try {
            AppPasswordVerifier(
                saltHex = salt.toHexString(),
                derivedKeyHex = derivedKey.toHexString(),
                iterations = DEFAULT_ITERATIONS,
            )
        } finally {
            derivedKey.fill(0)
        }
    } finally {
        salt.fill(0)
    }
}

internal actual fun verifyAppPassword(password: String, verifier: AppPasswordVerifier): Boolean {
    if (password.isEmpty() || password.length > 128) return false
    if (verifier.saltHex.length != 32 || verifier.derivedKeyHex.length != 64) return false
    if (!(verifier.saltHex + verifier.derivedKeyHex).all { it in "0123456789abcdefABCDEF" }) return false
    if (verifier.iterations !in MIN_ACCEPTED_ITERATIONS..MAX_ACCEPTED_ITERATIONS) return false
    val salt = runCatching(verifier.saltHex::hexToByteArray).getOrNull() ?: return false
    val expected = runCatching(verifier.derivedKeyHex::hexToByteArray).getOrNull() ?: return false
    return try {
        val actual = derivePasswordKey(password, salt, verifier.iterations)
        try {
            actual.constantTimeEquals(expected)
        } finally {
            actual.fill(0)
        }
    } finally {
        salt.fill(0)
        expected.fill(0)
    }
}

private fun derivePasswordKey(password: String, salt: ByteArray, iterations: Int): ByteArray {
    val passwordBytes = password.encodeToByteArray()
    val output = ByteArray(DERIVED_KEY_SIZE_BYTES)
    try {
        val status =
            salt.usePinned { saltPinned ->
                output.usePinned { outputPinned ->
                    CCKeyDerivationPBKDF(
                        algorithm = kCCPBKDF2,
                        password = password,
                        passwordLen = passwordBytes.size.toULong(),
                        salt = saltPinned.addressOf(0).reinterpret(),
                        saltLen = salt.size.toULong(),
                        prf = kCCPRFHmacAlgSHA256,
                        rounds = iterations.toUInt(),
                        derivedKey = outputPinned.addressOf(0).reinterpret(),
                        derivedKeyLen = output.size.toULong(),
                    )
                }
            }
        check(status == 0) { "Unable to derive password verifier (status=$status)" }
        return output
    } catch (error: Throwable) {
        output.fill(0)
        throw error
    } finally {
        passwordBytes.fill(0)
    }
}

private const val SALT_SIZE_BYTES = 16
private const val DERIVED_KEY_SIZE_BYTES = 32
private const val DEFAULT_ITERATIONS = 600_000
private const val MIN_ACCEPTED_ITERATIONS = 600_000
private const val MAX_ACCEPTED_ITERATIONS = 1_000_000
