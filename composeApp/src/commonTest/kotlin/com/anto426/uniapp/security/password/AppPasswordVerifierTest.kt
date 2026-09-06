package com.anto426.uniapp.security.password

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertNotEquals

class AppPasswordVerifierTest {
    @Test
    fun nativeImplementationsMatchTheSameUtf8ReferenceVector() {
        // Independently calculated with hashlib.pbkdf2_hmac, SHA-256, 600000 rounds.
        val reference = AppPasswordVerifier(
            saltHex = "000102030405060708090a0b0c0d0e0f",
            derivedKeyHex = "d4bed96057634a6676ad596b743711ef82af2b5d85f015d4aed179d1a98e0478",
            iterations = 600_000,
        )
        assertTrue(verifyAppPassword("Password-à-🔐-123", reference))
    }

    @Test
    fun verifierAcceptsOnlyTheOriginalPasswordAndUsesRandomSalt() {
        val password = "Password-à-🔐-123"
        val first = createAppPasswordVerifier(password)
        val second = createAppPasswordVerifier(password)
        assertNotEquals(first.saltHex, second.saltHex)
        assertTrue(verifyAppPassword(password, first))
        assertFalse(verifyAppPassword("wrong-password", first))
        assertFalse(verifyAppPassword("", first))
    }

    @Test
    fun malformedVerifierIsRejectedBeforeNativeDerivation() {
        val invalid = AppPasswordVerifier("", "", 600_000)
        assertFalse(verifyAppPassword("test-password", invalid))
        assertFalse(verifyAppPassword("test-password", invalid.copy(iterations = Int.MAX_VALUE)))
        assertFalse(verifyAppPassword("test-password", invalid.copy(saltHex = "zz".repeat(16))))
    }
}
