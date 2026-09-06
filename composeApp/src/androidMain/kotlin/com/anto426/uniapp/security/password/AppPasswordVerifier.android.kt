package com.anto426.uniapp.security.password

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

internal actual fun createAppPasswordVerifier(password: String): AppPasswordVerifier {
    require(password.length in 8..128 && password.isNotBlank()) { "Invalid app password length" }
    val salt = ByteArray(SALT_SIZE_BYTES).also(SecureRandom()::nextBytes)
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
    val characters = password.toCharArray()
    val specification = PBEKeySpec(characters, salt, iterations, DERIVED_KEY_BITS)
    return try {
        SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(specification).encoded
    } finally {
        specification.clearPassword()
        characters.fill('\u0000')
    }
}

private const val SALT_SIZE_BYTES = 16
private const val DERIVED_KEY_BITS = 256
private const val DEFAULT_ITERATIONS = 600_000
private const val MIN_ACCEPTED_ITERATIONS = 600_000
private const val MAX_ACCEPTED_ITERATIONS = 1_000_000
