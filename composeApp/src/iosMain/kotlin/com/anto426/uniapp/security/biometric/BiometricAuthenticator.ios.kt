@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.anto426.uniapp.security.biometric

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlin.coroutines.resume
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSError
import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAErrorAppCancel
import platform.LocalAuthentication.LAErrorBiometryNotEnrolled
import platform.LocalAuthentication.LAErrorPasscodeNotSet
import platform.LocalAuthentication.LAErrorSystemCancel
import platform.LocalAuthentication.LAErrorUserCancel
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthentication

@Composable
internal actual fun rememberPlatformBiometricAuthenticator(): BiometricAuthenticator =
    remember { IosBiometricAuthenticator() }

private class IosBiometricAuthenticator : BiometricAuthenticator {
    override fun availability(): BiometricAvailability = memScoped {
        val error = alloc<ObjCObjectVar<NSError?>>()
        if (LAContext().canEvaluatePolicy(LAPolicyDeviceOwnerAuthentication, error.ptr)) {
            BiometricAvailability.Available
        } else {
            when (error.value?.code) {
                LAErrorPasscodeNotSet, LAErrorBiometryNotEnrolled -> BiometricAvailability.NotEnrolled
                else -> BiometricAvailability.Unavailable
            }
        }
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
                    when {
                        success -> BiometricAuthenticationResult.Authenticated
                        error?.code == LAErrorUserCancel ||
                            error?.code == LAErrorSystemCancel ||
                            error?.code == LAErrorAppCancel -> BiometricAuthenticationResult.Cancelled
                        else -> BiometricAuthenticationResult.Failed(
                            error?.localizedDescription ?: "Autenticazione annullata.",
                        )
                    },
                )
            }
        }
}
