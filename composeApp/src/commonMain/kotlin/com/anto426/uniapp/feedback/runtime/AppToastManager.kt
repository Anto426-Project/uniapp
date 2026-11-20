package com.anto426.uniapp.feedback.runtime

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anto426.liquidmonet.components.feedback.toast.state.LiquidToastState
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.localization.readAppString
import com.anto426.uniapp.presentation.sdkFeedbackMessage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource

enum class AppToastKind { Success, Info, Warning, Error }

sealed interface AppToastText {
    data class Plain(val value: String) : AppToastText
    data class Localized(val resource: StringResource) : AppToastText
}

data class AppToastMessage(
    val text: AppToastText,
    val kind: AppToastKind = AppToastKind.Info,
    val subtitle: String? = null,
) {
    constructor(text: String, kind: AppToastKind = AppToastKind.Info, subtitle: String? = null) :
        this(AppToastText.Plain(text), kind, subtitle)

    internal suspend fun resolveText(): String = when (val content = text) {
        is AppToastText.Plain -> sdkFeedbackMessage(content.value)
        is AppToastText.Localized -> readAppString(content.resource)
    }
}

/** Boundary used by ViewModels to publish ephemeral feedback without putting it in screen state. */
fun interface AppToastSink {
    fun show(message: AppToastMessage)

    companion object {
        val None = AppToastSink { }
    }
}

internal val LocalAppToastSink = staticCompositionLocalOf<AppToastSink> { AppToastSink.None }

class AppToastManager : ViewModel(), AppToastSink {
    private val messageQueue = Channel<AppToastMessage>(capacity = Channel.UNLIMITED)

    val messages: Flow<AppToastMessage> = messageQueue.receiveAsFlow()

    // Keep the active toast across navigation and Android activity recreation.
    internal val liquidState: LiquidToastState by lazy { LiquidToastState(viewModelScope) }
    private var presenterStarted = false

    internal fun startPresentation() {
        if (presenterStarted) return
        presenterStarted = true
        viewModelScope.launch {
            messages.collect { message ->
                liquidState.show(
                    message = message.resolveText(),
                    subtitle = message.subtitle?.let { sdkFeedbackMessage(it) },
                    icon = message.kind.toIcon(),
                )
            }
        }
    }

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

fun AppToastSink.success(resource: StringResource) =
    show(AppToastMessage(AppToastText.Localized(resource), AppToastKind.Success))

fun AppToastSink.info(resource: StringResource) =
    show(AppToastMessage(AppToastText.Localized(resource), AppToastKind.Info))

fun AppToastSink.warning(resource: StringResource) =
    show(AppToastMessage(AppToastText.Localized(resource), AppToastKind.Warning))

fun AppToastSink.error(resource: StringResource) =
    show(AppToastMessage(AppToastText.Localized(resource), AppToastKind.Error))

private fun AppToastKind.toIcon(): ImageVector =
    when (this) {
        AppToastKind.Success -> LiquidIcons.Check
        AppToastKind.Info -> LiquidIcons.Info
        AppToastKind.Warning -> LiquidIcons.Warning
        AppToastKind.Error -> LiquidIcons.Close
    }
