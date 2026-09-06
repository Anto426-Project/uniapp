package com.anto426.uniapp.ui.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.LiquidPreferenceGroup
import com.anto426.liquidmonet.components.cards.LiquidPreferenceItem
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.model.legal.LegalSectionData
import com.anto426.uniapp.ui.document.UniDocumentScreen
import com.anto426.uniapp.ui.document.toMarkdownString
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun AboutUniAppScreen(
    sections: List<LegalSectionData>,
    modifier: Modifier = Modifier,
    onReportBug: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
) {
    val intro = stringResource(Res.string.ui_project_summary_text)
    val body = sections.toMarkdownString()
    val fullContent = if (intro.isNotBlank()) "$intro\n\n---\n\n$body" else body

    UniDocumentScreen(
        content = fullContent,
        modifier = modifier,
        onBack = onBack,
        bottomContent = if (onReportBug != null) {
            {
                LiquidPreferenceGroup(
                    title = stringResource(Res.string.app_info_sec_6_title),
                    modifier = Modifier.padding(bottom = 16.dp),
                ) {
                    LiquidPreferenceItem(
                        title = stringResource(Res.string.ui_report_bug_github),
                        subtitle = stringResource(Res.string.ui_info_public_issue),
                        icon = LiquidIcons.Info,
                        onClick = onReportBug,
                    )
                }
            }
        } else null,
    )
}
