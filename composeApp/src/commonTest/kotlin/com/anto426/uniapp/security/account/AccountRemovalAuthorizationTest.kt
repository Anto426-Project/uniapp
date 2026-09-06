package com.anto426.uniapp.security.account

import com.anto426.uniapp.security.biometric.BiometricAuthenticationResult
import com.anto426.uniapp.security.biometric.BiometricAuthenticator
import com.anto426.uniapp.security.biometric.BiometricAvailability
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class AccountRemovalAuthorizationTest {
    @Test
    fun unprotectedAccountDoesNotRequestBiometricAuthentication() = runTest {
        val authenticator = RemovalAuthenticator(BiometricAuthenticationResult.Cancelled)
        assertTrue(authorizeAccountRemoval(false, authenticator))
        assertEquals(0, authenticator.requests)
    }

    @Test
    fun protectedAccountRequiresSuccessfulAuthentication() = runTest {
        val accepted = RemovalAuthenticator(BiometricAuthenticationResult.Authenticated)
        assertTrue(authorizeAccountRemoval(true, accepted))
        assertEquals(1, accepted.requests)
        val cancelled = RemovalAuthenticator(BiometricAuthenticationResult.Cancelled)
        assertFalse(authorizeAccountRemoval(true, cancelled))
        assertEquals(1, cancelled.requests)
    }

    @Test
    fun failedAuthenticationOrCoroutineCancellationNeverAuthorizesRemoval() = runTest {
        assertFailsWith<IllegalStateException> {
            authorizeAccountRemoval(true, RemovalAuthenticator(BiometricAuthenticationResult.Failed("Unavailable")))
        }
        val cancelled = object : BiometricAuthenticator {
            override fun availability() = BiometricAvailability.Available
            override suspend fun authenticate(reason: String): BiometricAuthenticationResult =
                throw CancellationException("Dismissed")
        }
        assertFailsWith<CancellationException> { authorizeAccountRemoval(true, cancelled) }
    }
}

private class RemovalAuthenticator(private val result: BiometricAuthenticationResult) : BiometricAuthenticator {
    var requests = 0
    override fun availability() = BiometricAvailability.Available
    override suspend fun authenticate(reason: String): BiometricAuthenticationResult {
        requests++
        return result
    }
}
