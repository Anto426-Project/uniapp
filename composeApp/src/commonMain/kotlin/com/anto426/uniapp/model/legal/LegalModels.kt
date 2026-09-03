package com.anto426.uniapp.model.legal

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

data class LegalSectionData(
    val titleRes: StringResource? = null,
    val contentRes: StringResource? = null,
    val rawTitle: String = "",
    val rawContent: String = "",
) {
    constructor(title: String, content: String) : this(null, null, title, content)
    constructor(titleRes: StringResource, contentRes: StringResource) : this(titleRes, contentRes, "", "")

    val title: String
        @Composable get() = titleRes?.let { stringResource(it) } ?: rawTitle

    val content: String
        @Composable get() = contentRes?.let { stringResource(it) } ?: rawContent
}
