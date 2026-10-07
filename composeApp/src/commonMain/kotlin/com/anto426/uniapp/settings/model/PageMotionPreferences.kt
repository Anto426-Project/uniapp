package com.anto426.uniapp.settings.model

import kotlinx.serialization.Serializable

/** One stored record keeps the switch and selected SDK preset consistent. */
@Serializable
data class PageMotionPreferences(
    val enabled: Boolean = true,
    val transitionName: String = "DirectionalHorizontal",
)
