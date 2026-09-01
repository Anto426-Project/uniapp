package com.anto426.uniapp.feedback.runtime

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

enum class AppToastKind { Success, Info, Warning, Error }

data class AppToastMessage(
    val text: String,
    val kind: AppToastKind = AppToastKind.Info,
    val subtitle: String? = null,
)

/** Boundary used by ViewModels to publish ephemeral feedback without putting it in screen state. */
fun interface AppToastSink {
    fun show(message: AppToastMessage)

    companion object {
        val None = AppToastSink { }
    }
}

class AppToastManager : AppToastSink {
    private val messageQueue = Channel<AppToastMessage>(capacity = Channel.UNLIMITED)

    val messages: Flow<AppToastMessage> = messageQueue.receiveAsFlow()

    override fun show(message: AppToastMessage) {
        messageQueue.trySend(message)
    }
}

fun AppToastSink.success(text: String, subtitle: String? = null) =
    show(AppToastMessage(text, AppToastKind.Success, subtitle))

fun AppToastSink.info(text: String, subtitle: String? = null) =
    show(AppToastMessage(text, AppToastKind.Info, subtitle))

fun AppToastSink.warning(text: String, subtitle: String? = null) =
    show(AppToastMessage(text, AppToastKind.Warning, subtitle))

fun AppToastSink.error(text: String, subtitle: String? = null) =
    show(AppToastMessage(text, AppToastKind.Error, subtitle))
