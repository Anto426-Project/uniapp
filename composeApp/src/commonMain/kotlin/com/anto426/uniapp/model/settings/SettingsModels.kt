package com.anto426.uniapp.model.settings

import androidx.compose.ui.graphics.Color

enum class DeviceType { PHONE, PC, TABLET }

data class DeviceInfo(
    val name: String,
    val location: String,
    val lastSeen: String,
    val appVersion: String? = null,
    val type: DeviceType,
    val isCurrent: Boolean,
    val id: String = "",
    val revocationToken: String? = null,
)

data class LanguageInfo(val name: String, val region: String, val code: String)

data class ThemeOption(
    val name: String,
    val description: String,
    val color: Color?,
    val isCustom: Boolean = false,
)
