package com.anto426.uniapp.model.updates

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

data class ChangelogItemData(
    val tag: String,
    val tagColor: Color,
    val titleRes: StringResource? = null,
    val descriptionRes: StringResource? = null,
    val rawTitle: String = "",
    val rawDescription: String = "",
) {
    constructor(
        tag: String,
        tagColor: Color,
        title: String,
        description: String,
    ) : this(tag, tagColor, null, null, title, description)

    constructor(
        tag: String,
        tagColor: Color,
        titleRes: StringResource,
        descriptionRes: StringResource,
    ) : this(tag, tagColor, titleRes, descriptionRes, "", "")

    val title: String
        @Composable get() = titleRes?.let { stringResource(it) } ?: rawTitle

    val description: String
        @Composable get() = descriptionRes?.let { stringResource(it) } ?: rawDescription
}

data class ChangelogVersionData(
    val version: String,
    val dateRes: StringResource? = null,
    val rawDate: String = "",
    val items: List<ChangelogItemData>,
) {
    constructor(
        version: String,
        date: String,
        items: List<ChangelogItemData>,
    ) : this(version, null, date, items)

    constructor(
        version: String,
        dateRes: StringResource,
        items: List<ChangelogItemData>,
    ) : this(version, dateRes, "", items)

    val date: String
        @Composable get() = dateRes?.let { stringResource(it) } ?: rawDate
}


enum class UpdateState {
    CHECKING,
    UP_TO_DATE,
    AVAILABLE,
    DOWNLOADING,
    VERIFYING,
    INSTALLING,
    RESTART_REQUIRED,
    ERROR,
}
