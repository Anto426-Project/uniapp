package com.anto426.uniapp.presentation

import com.anto426.uniapp.localization.readAppString
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.ResourceEnvironment
import org.jetbrains.compose.resources.StringResource
import uniapp.composeapp.generated.resources.Res
import uniapp.composeapp.generated.resources.allStringResources
import uniapp.composeapp.generated.resources.sdk_error_remote_request_failed

/** Resolve SDK codes without coupling app builds to a newly published SDK binary. */
@OptIn(ExperimentalResourceApi::class)
internal fun sdkErrorResource(value: String?): StringResource? {
    val code = value?.trim() ?: return null
    if (!code.matches(Regex("UNI_[A-Z][A-Z0-9_]*"))) return null
    return Res.allStringResources["sdk_error_" + code.removePrefix("UNI_").lowercase()]
}

internal suspend fun sdkFailureMessage(value: String?, fallback: suspend () -> String): String =
    resolveSdkFailureMessage(value, environment = null, fallback = fallback)

private suspend fun resolveSdkFailureMessage(
    value: String?,
    environment: ResourceEnvironment?,
    fallback: suspend () -> String,
): String {
    val message = value?.trim()?.takeIf(String::isNotEmpty) ?: return fallback()
    sdkErrorResource(message)?.let { return readAppString(it, environment) }
    return if (message.startsWith("UNI_")) fallback() else message
}

internal suspend fun sdkFailureMessage(value: String?, fallback: String): String =
    sdkFailureMessage(value) { fallback }

internal suspend fun sdkFeedbackMessage(
    value: String,
    environment: ResourceEnvironment? = null,
): String = resolveSdkFailureMessage(value, environment) {
    readAppString(Res.string.sdk_error_remote_request_failed, environment)
}
