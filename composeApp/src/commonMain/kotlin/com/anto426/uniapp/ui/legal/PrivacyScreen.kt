package com.anto426.uniapp.ui.legal

import androidx.compose.runtime.Composable
import com.anto426.uniapp.model.legal.LegalSectionData
import com.anto426.uniapp.ui.document.UniDocumentScreen
import com.anto426.uniapp.ui.document.toMarkdownString
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun PrivacyScreen(
    sections: List<LegalSectionData>,
    onBack: (() -> Unit)? = null,
) {
    val intro = stringResource(Res.string.ui_privacy_intro)
    val body = sections.toMarkdownString()
    val fullContent = if (intro.isNotBlank()) "$intro\n\n---\n\n$body" else body

    UniDocumentScreen(
        content = fullContent,
        onBack = onBack,
    )
}

