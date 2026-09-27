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
    val category: String = "",
) {
    constructor(
        tag: String,
        tagColor: Color,
        title: String,
        description: String,
    ) : this(tag, tagColor, null, null, title, description, "")

    constructor(
        tag: String,
        tagColor: Color,
        category: String,
        title: String,
        description: String,
    ) : this(tag, tagColor, null, null, title, description, category)

    constructor(
        tag: String,
        tagColor: Color,
        titleRes: StringResource,
        descriptionRes: StringResource,
    ) : this(tag, tagColor, titleRes, descriptionRes, "", "", "")

    constructor(
        tag: String,
        tagColor: Color,
        category: String,
        titleRes: StringResource,
        descriptionRes: StringResource,
    ) : this(tag, tagColor, titleRes, descriptionRes, "", "", category)

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
    val channel: String = "Canale Stabile",
    val summary: String = "",
) {
    constructor(
        version: String,
        date: String,
        items: List<ChangelogItemData>,
    ) : this(version, null, date, items, "Canale Stabile", "")

    constructor(
        version: String,
        date: String,
        items: List<ChangelogItemData>,
        channel: String,
        summary: String = "",
    ) : this(version, null, date, items, channel, summary)

    constructor(
        version: String,
        dateRes: StringResource,
        items: List<ChangelogItemData>,
    ) : this(version, dateRes, "", items, "Canale Stabile", "")

    constructor(
        version: String,
        dateRes: StringResource,
        items: List<ChangelogItemData>,
        channel: String,
        summary: String = "",
    ) : this(version, dateRes, "", items, channel, summary)

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
