package com.anto426.uniapp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
object DesktopBackDispatcher {
    internal val handlers = linkedMapOf<Any, () -> Unit>()
    fun handle(event: KeyEvent): Boolean {
        if (event.key != Key.Escape || event.type != KeyEventType.KeyDown) return false
        val handler = handlers.values.lastOrNull() ?: return false
        handler()
        return true
    }
}
@Composable
internal actual fun UniBackHandler(enabled: Boolean, onBack: () -> Unit) {
    val token = remember { Any() }
    val callback = rememberUpdatedState(onBack)
    DisposableEffect(enabled) {
        if (enabled) DesktopBackDispatcher.handlers[token] = { callback.value() }
        onDispose { DesktopBackDispatcher.handlers.remove(token) }
    }
}
