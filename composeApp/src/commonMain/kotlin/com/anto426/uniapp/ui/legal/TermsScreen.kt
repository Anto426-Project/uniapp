package com.anto426.uniapp.ui.legal

import androidx.compose.runtime.Composable
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.model.legal.LegalSectionData
import com.anto426.uniapp.ui.document.UniSectionedDocumentScreen
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun TermsScreen(
    sections: List<LegalSectionData>,
    onBack: (() -> Unit)? = null,
) {
    UniSectionedDocumentScreen(
        sections = sections,
        title = stringResource(Res.string.ui_terms_title),
        subtitle = stringResource(Res.string.ui_terms_subtitle),
        intro = stringResource(Res.string.ui_terms_intro),
        headerIcon = LiquidIcons.Assignment,
        headerBadge = stringResource(Res.string.nav_route_terms_title),
        onBack = onBack,
    )
}
