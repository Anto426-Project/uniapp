package com.anto426.uniapp.model.services

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

data class ServiceData(
    val titleRes: StringResource? = null,
    val subtitleRes: StringResource? = null,
    val rawTitle: String = "",
    val rawSubtitle: String = "",
    val icon: ImageVector,
    val badgeCount: Int? = null,
    val id: String = "",
    val badgeLabelRes: StringResource? = null,
) {
    constructor(
        title: String,
        subtitle: String,
        icon: ImageVector,
        badgeCount: Int? = null,
        id: String = "",
    ) : this(null, null, title, subtitle, icon, badgeCount, id, null)

    constructor(
        titleRes: StringResource,
        subtitleRes: StringResource,
        icon: ImageVector,
        badgeCount: Int? = null,
        id: String = "",
        badgeLabelRes: StringResource? = null,
    ) : this(titleRes, subtitleRes, "", "", icon, badgeCount, id, badgeLabelRes)

    val title: String
        @Composable get() = titleRes?.let { stringResource(it) } ?: rawTitle

    val subtitle: String
        @Composable get() = subtitleRes?.let { stringResource(it) } ?: rawSubtitle

    val badgeLabel: String?
        @Composable get() = badgeLabelRes?.let { stringResource(it) }
}
