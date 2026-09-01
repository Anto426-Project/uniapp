@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.anto426.uniapp.security.biometric

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthentication

@Composable
internal actual fun rememberPlatformBiometricAuthenticator(): BiometricAuthenticator =
    remember { IosBiometricAuthenticator() }

private class IosBiometricAuthenticator : BiometricAuthenticator {
    override fun availability(): BiometricAvailability =
        if (LAContext().canEvaluatePolicy(LAPolicyDeviceOwnerAuthentication, null)) {
            BiometricAvailability.Available
        } else {
            BiometricAvailability.Unavailable
        }

    override suspend fun authenticate(reason: String): BiometricAuthenticationResult =
        suspendCancellableCoroutine { continuation ->
            val context = LAContext()
            continuation.invokeOnCancellation { context.invalidate() }
            context.evaluatePolicy(
                policy = LAPolicyDeviceOwnerAuthentication,
                localizedReason = reason,
            ) { success, error ->
                if (!continuation.isActive) return@evaluatePolicy
                continuation.resume(
                    if (success) {
                        BiometricAuthenticationResult.Authenticated
                    } else {
                        BiometricAuthenticationResult.Failed(
                            error?.localizedDescription ?: "Autenticazione annullata.",
                        )
                    },
                )
            }
        }
}
