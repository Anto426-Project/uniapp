package com.anto426.uniapp.ui.legal

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.anto426.uniapp.model.legal.LegalSectionData
import com.anto426.uniapp.ui.document.UniDocumentScreen
import com.anto426.uniapp.ui.document.toMarkdownString
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun CookiesScreen(
    sections: List<LegalSectionData>,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
) {
    val intro = stringResource(Res.string.ui_cookies_intro)
    val body = sections.toMarkdownString()

    val fullContent = buildString {
        if (intro.isNotBlank()) {
            append(intro)
            append("\n\n")
        }
        if (body.isNotBlank()) {
            append("\n\n---\n\n")
            append(body)
        }
    }

    UniDocumentScreen(
        content = fullContent,
        modifier = modifier,
        onBack = onBack,
    )
}
