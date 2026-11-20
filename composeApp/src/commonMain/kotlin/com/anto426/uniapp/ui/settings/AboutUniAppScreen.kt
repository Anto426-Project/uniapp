package com.anto426.uniapp.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.model.legal.LegalSectionData
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
        footerActionTitle = if (onReportBug != null) stringResource(Res.string.ui_report_bug_github) else null,
        footerActionSubtitle = if (onReportBug != null) stringResource(Res.string.ui_info_public_issue) else null,
        footerActionIcon = if (onReportBug != null) LiquidIcons.Feedback else null,
        onFooterActionClick = onReportBug,
        onBack = onBack,
    )
}
