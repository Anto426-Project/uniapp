package com.anto426.uniapp.model.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

data class QuickActionItem(
    val id: String,
    val titleRes: StringResource? = null,
    val subtitleRes: StringResource? = null,
    val rawTitle: String = "",
    val rawSubtitle: String = "",
    val icon: ImageVector,
) {
    constructor(
        id: String,
        title: String,
        subtitle: String,
        icon: ImageVector,
    ) : this(id, null, null, title, subtitle, icon)

    constructor(
        id: String,
        titleRes: StringResource,
        subtitleRes: StringResource,
        icon: ImageVector,
    ) : this(id, titleRes, subtitleRes, "", "", icon)

    val title: String
        @Composable get() = titleRes?.let { stringResource(it) } ?: rawTitle

    val subtitle: String
        @Composable get() = subtitleRes?.let { stringResource(it) } ?: rawSubtitle
}

