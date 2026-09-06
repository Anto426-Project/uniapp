package com.anto426.uniapp.ui.news

import androidx.compose.runtime.Composable
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.ui.document.UniDocumentScreen

@Composable
fun NewsDetailScreen(
    title: String,
    description: String,
    fullContent: String,
    onBack: () -> Unit = {},
) {
    val body = buildString {
        if (description.isNotBlank() && description.trim() != fullContent.trim()) {
            append("**")
            append(description.trim())
            append("**")
            if (fullContent.isNotBlank()) {
                append("\n\n")
                append(fullContent.trim())
            }
        } else if (fullContent.isNotBlank()) {
            append(fullContent.trim())
        } else if (description.isNotBlank()) {
            append(description.trim())
        }
    }

    UniDocumentScreen(
        title = title.takeIf { it.isNotBlank() },
        headerIcon = LiquidIcons.Notifications,
        content = body,
        onBack = onBack,
    )
}

