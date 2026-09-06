package com.anto426.uniapp.presentation

enum class FeatureLoadState {
    Loading,
    Content,
    Empty,
    Error,
}

/** Keep a populated screen visible during refreshes and recoverable refresh failures. */
internal fun FeatureLoadState.onRefresh(): FeatureLoadState = when (this) {
    FeatureLoadState.Content, FeatureLoadState.Empty -> this
    else -> FeatureLoadState.Loading
}

internal fun FeatureLoadState.onRefreshFailure(): FeatureLoadState = when (this) {
    FeatureLoadState.Content, FeatureLoadState.Empty -> this
    else -> FeatureLoadState.Error
}

internal fun Throwable.userMessage(fallback: String): String =
    message?.trim()?.takeIf(String::isNotEmpty) ?: fallback
