package com.anto426.uniapp.settings.model

import kotlinx.serialization.Serializable

/** Persisted SDK preset. The legacy enabled field remains readable for existing records. */
@Serializable
data class PageMotionPreferences(
    // Compatibility only: reduced motion is controlled by the global theme preference.
    val enabled: Boolean = true,
    val transitionName: String = "DirectionalHorizontal",
)
