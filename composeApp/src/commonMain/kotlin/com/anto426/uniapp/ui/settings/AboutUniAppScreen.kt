package com.anto426.uniapp.ui.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.model.legal.LegalSectionData
import com.anto426.uniapp.ui.components.display.UniInsetGroup
import com.anto426.uniapp.ui.components.display.UniInsetRow
import com.anto426.uniapp.ui.document.UniSectionedDocumentScreen
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun AboutUniAppScreen(
    sections: List<LegalSectionData>,
    modifier: Modifier = Modifier,
    onReportBug: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
) {
    UniSectionedDocumentScreen(
        sections = sections,
        modifier = modifier,
        title = stringResource(Res.string.ui_info_about_project),
        subtitle = stringResource(Res.string.ui_info_independent),
        intro = stringResource(Res.string.ui_project_summary_text),
        headerIcon = LiquidIcons.Info,
        headerBadge = stringResource(Res.string.ui_app_name),
        onBack = onBack,
        bottomContent = if (onReportBug != null) {
            {
                UniInsetGroup(
                    title = stringResource(Res.string.app_info_sec_6_title),
                    modifier = Modifier.padding(bottom = 16.dp),
                ) {
                    UniInsetRow(
                        title = stringResource(Res.string.ui_report_bug_github),
                        subtitle = stringResource(Res.string.ui_info_public_issue),
                        icon = LiquidIcons.Feedback,
                        iconTint = MaterialTheme.colorScheme.tertiary,
                        onClick = onReportBug,
                        showDivider = false,
                    )
                }
            }
        } else null,
    )
}
