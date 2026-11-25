package com.anto426.uniapp.navigation.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

internal class NewsDetailActions(
    val onOpenOriginal: (() -> Unit)?,
    val onShare: () -> Unit,
)

internal class NewsDetailActionHost {
    var actions by mutableStateOf<NewsDetailActions?>(null)
        private set

    fun register(actions: NewsDetailActions) {
        this.actions = actions
    }

    fun unregister(actions: NewsDetailActions) {
        if (this.actions === actions) this.actions = null
    }
}
