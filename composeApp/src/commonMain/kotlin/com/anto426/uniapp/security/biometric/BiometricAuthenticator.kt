package com.anto426.uniapp.security.biometric

import org.jetbrains.compose.resources.getString
import uniapp.composeapp.generated.resources.*

import androidx.compose.runtime.Composable

enum class BiometricAvailability {
    Available,
    NotEnrolled,
    Unavailable,
}

sealed interface BiometricAuthenticationResult {
    data object Authenticated : BiometricAuthenticationResult
    data object Cancelled : BiometricAuthenticationResult
    data class Failed(val message: String) : BiometricAuthenticationResult
}

interface BiometricAuthenticator {
    fun availability(): BiometricAvailability

    suspend fun authenticate(reason: String): BiometricAuthenticationResult
}

object UnavailableBiometricAuthenticator : BiometricAuthenticator {
    override fun availability(): BiometricAvailability = BiometricAvailability.Unavailable

    override suspend fun authenticate(reason: String): BiometricAuthenticationResult =
        BiometricAuthenticationResult.Failed(getString(Res.string.msg_autenticazione_del_dispositivo_non_disponibile))
}

@Composable
internal expect fun rememberPlatformBiometricAuthenticator(): BiometricAuthenticator
