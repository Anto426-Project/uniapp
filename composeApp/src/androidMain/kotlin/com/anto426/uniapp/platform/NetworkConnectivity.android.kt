package com.anto426.uniapp.platform

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
internal actual fun rememberIsDeviceOffline(): State<Boolean> {
    val context = LocalContext.current.applicationContext
    val offlineState = remember(context) {
        mutableStateOf(readCurrentOfflineState(context))
    }

    DisposableEffect(context) {
        val manager = runCatching {
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        }.getOrNull()
        if (manager == null) {
            return@DisposableEffect onDispose {}
        }

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                offlineState.value = readCurrentOfflineState(context)
            }

            override fun onLost(network: Network) {
                offlineState.value = readCurrentOfflineState(context)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities,
            ) {
                offlineState.value = !networkCapabilities.isValidatedInternet()
            }

            override fun onUnavailable() {
                offlineState.value = true
            }
        }

        val registered = runCatching {
            manager.registerDefaultNetworkCallback(callback)
            true
        }.getOrDefault(false)

        offlineState.value = readCurrentOfflineState(context)

        onDispose {
            if (registered) {
                runCatching { manager.unregisterNetworkCallback(callback) }
            }
        }
    }

    return offlineState
}

private fun readCurrentOfflineState(context: Context): Boolean = runCatching {
    val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        ?: return@runCatching false
    val activeNetwork = manager.activeNetwork ?: return@runCatching true
    val capabilities = manager.getNetworkCapabilities(activeNetwork) ?: return@runCatching true
    !capabilities.isValidatedInternet()
}.getOrDefault(false)

private fun NetworkCapabilities.isValidatedInternet(): Boolean =
    hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
        hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
