package com.anto426.uniapp.model.settings

import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.StringResource

data class LanguageInfo(
    val nameRes: StringResource,
    val regionRes: StringResource,
    val code: String,
)

data class ThemeOption(
    val nameRes: StringResource,
    val color: Color?,
    val isCustom: Boolean = false,
)
