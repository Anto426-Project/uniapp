package com.anto426.uniapp.security.account

import com.anto426.uniapp.localization.readAppString
import uniapp.composeapp.generated.resources.*

import com.anto426.uniapp.security.biometric.BiometricAuthenticator
import com.anto426.uniapp.security.biometric.BiometricAuthenticationResult

internal suspend fun authorizeAccountRemoval(
    isProtected: Boolean,
    authenticator: BiometricAuthenticator,
): Boolean {
    if (!isProtected) return true
    return when (val result = authenticator.authenticate(readAppString(Res.string.msg_conferma_la_tua_identita_per_rimuovere_questo_account))) {
        BiometricAuthenticationResult.Authenticated -> true
        BiometricAuthenticationResult.Cancelled -> false
        is BiometricAuthenticationResult.Failed -> error(result.message.ifBlank { readAppString(Res.string.msg_autenticazione_non_riuscita) })
    }
}
