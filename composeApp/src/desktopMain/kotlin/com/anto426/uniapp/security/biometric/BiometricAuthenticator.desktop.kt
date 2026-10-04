package com.anto426.uniapp.security.biometric
import androidx.compose.runtime.Composable
@Composable
internal actual fun rememberPlatformBiometricAuthenticator(): BiometricAuthenticator = UnavailableBiometricAuthenticator
