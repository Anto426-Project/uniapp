package com.anto426.uniapp.security.biometric

import android.app.Activity
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.app.KeyguardManager
import android.os.Build
import android.os.CancellationSignal
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

@Composable
internal actual fun rememberPlatformBiometricAuthenticator(): BiometricAuthenticator {
    val activity = LocalContext.current.findActivity()
    return remember(activity) { AndroidBiometricAuthenticator(activity) }
}

private class AndroidBiometricAuthenticator(
    private val activity: Activity?,
) : BiometricAuthenticator {
    override fun availability(): BiometricAvailability {
        val currentActivity = activity ?: return BiometricAvailability.Unavailable
        val manager = currentActivity.getSystemService(BiometricManager::class.java)
            ?: return BiometricAvailability.Unavailable
        val result =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                manager.canAuthenticate(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL,
                )
            } else {
                @Suppress("DEPRECATION")
                manager.canAuthenticate()
            }
        return when (result) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability.Available
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                val deviceSecure =
                    currentActivity.getSystemService(KeyguardManager::class.java)?.isDeviceSecure == true
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q && deviceSecure) {
                    BiometricAvailability.Available
                } else {
                    BiometricAvailability.NotEnrolled
                }
            }
            else -> BiometricAvailability.Unavailable
        }
    }

    override suspend fun authenticate(reason: String): BiometricAuthenticationResult {
        val currentActivity = activity
            ?: return BiometricAuthenticationResult.Failed("Attività Android non disponibile.")
        if (availability() != BiometricAvailability.Available) {
            return BiometricAuthenticationResult.Failed("Autenticazione del dispositivo non disponibile.")
        }

        return suspendCancellableCoroutine { continuation ->
            val cancellationSignal = CancellationSignal()
            val completed = AtomicBoolean(false)
            fun complete(result: BiometricAuthenticationResult) {
                if (completed.compareAndSet(false, true) && continuation.isActive) {
                    continuation.resume(result)
                }
            }

            val prompt =
                BiometricPrompt.Builder(currentActivity)
                    .setTitle("Proteggi UniApp")
                    .setSubtitle(reason)
                    .apply {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            setAllowedAuthenticators(
                                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                                    BiometricManager.Authenticators.DEVICE_CREDENTIAL,
                            )
                        } else {
                            @Suppress("DEPRECATION")
                            setDeviceCredentialAllowed(true)
                        }
                    }
                    .build()

            continuation.invokeOnCancellation { cancellationSignal.cancel() }
            prompt.authenticate(
                cancellationSignal,
                currentActivity.mainExecutor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        complete(BiometricAuthenticationResult.Authenticated)
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        val cancelled =
                            errorCode == BiometricPrompt.BIOMETRIC_ERROR_USER_CANCELED ||
                                errorCode == BiometricPrompt.BIOMETRIC_ERROR_CANCELED
                        complete(
                            if (cancelled) BiometricAuthenticationResult.Cancelled
                            else BiometricAuthenticationResult.Failed(errString.toString()),
                        )
                    }
                },
            )
        }
    }
}

private tailrec fun android.content.Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is android.content.ContextWrapper -> baseContext.findActivity()
        else -> null
    }
