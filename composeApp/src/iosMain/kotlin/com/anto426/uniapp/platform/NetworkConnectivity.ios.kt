package com.anto426.uniapp.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import platform.Network.nw_path_get_status
import platform.Network.nw_path_monitor_cancel
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_status_satisfied
import platform.darwin.dispatch_get_main_queue

@Composable
internal actual fun rememberIsDeviceOffline(): State<Boolean> {
    val offlineState = remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val monitor = nw_path_monitor_create()
        nw_path_monitor_set_update_handler(monitor) { path ->
            offlineState.value = nw_path_get_status(path) != nw_path_status_satisfied
        }
        nw_path_monitor_set_queue(monitor, dispatch_get_main_queue())
        nw_path_monitor_start(monitor)

        onDispose {
            nw_path_monitor_cancel(monitor)
        }
    }

    return offlineState
}
