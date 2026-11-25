package com.anto426.uniapp.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.staticCompositionLocalOf

/** Current offline status across the app shell (OS network availability + live request health). */
internal val LocalAppIsOffline = staticCompositionLocalOf { false }

/** Observes platform network connectivity and reports `true` when the device has no active internet connection. */
@Composable
internal expect fun rememberIsDeviceOffline(): State<Boolean>
