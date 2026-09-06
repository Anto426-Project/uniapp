package com.anto426.uniapp.security.account

import com.anto426.uniapp.security.biometric.BiometricAuthenticator
import com.anto426.uniapp.security.biometric.BiometricAuthenticationResult

internal suspend fun authorizeAccountRemoval(
    isProtected: Boolean,
    authenticator: BiometricAuthenticator,
): Boolean {
    if (!isProtected) return true
    return when (val result = authenticator.authenticate("Conferma la tua identità per rimuovere questo account da UniApp.")) {
        BiometricAuthenticationResult.Authenticated -> true
        BiometricAuthenticationResult.Cancelled -> false
        is BiometricAuthenticationResult.Failed -> error(result.message.ifBlank { "Autenticazione non riuscita." })
    }
}
