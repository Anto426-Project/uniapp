package com.anto426.uniapp.model.settings

import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.StringResource

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

data class LanguageInfo(
    val nameRes: org.jetbrains.compose.resources.StringResource,
    val regionRes: org.jetbrains.compose.resources.StringResource,
    val code: String,
)

data class ThemeOption(
    val nameRes: StringResource,
    val color: Color?,
    val isCustom: Boolean = false,
)

