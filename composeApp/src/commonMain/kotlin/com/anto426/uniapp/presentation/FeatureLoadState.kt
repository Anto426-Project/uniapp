package com.anto426.uniapp.presentation

enum class FeatureLoadState {
    Loading,
    Content,
    Empty,
    Error,
}

internal fun Throwable.userMessage(fallback: String): String =
    message?.trim()?.takeIf(String::isNotEmpty) ?: fallback
