package com.anto426.uniapp.model.updates

import androidx.compose.ui.graphics.Color

data class ChangelogItemData(
    val tag: String,
    val tagColor: Color,
    val title: String,
    val description: String,
)

data class ChangelogVersionData(
    val version: String,
    val date: String,
    val items: List<ChangelogItemData>,
)

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
