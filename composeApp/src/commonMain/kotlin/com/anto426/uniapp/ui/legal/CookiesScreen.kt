package com.anto426.uniapp.ui.legal

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.model.legal.LegalSectionData
import com.anto426.uniapp.ui.document.UniSectionedDocumentScreen
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun CookiesScreen(
    sections: List<LegalSectionData>,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
) {
    UniSectionedDocumentScreen(
        sections = sections,
        modifier = modifier,
        title = stringResource(Res.string.ui_cookies_title),
        subtitle = stringResource(Res.string.ui_cookies_subtitle),
        intro = stringResource(Res.string.ui_cookies_intro),
        headerIcon = LiquidIcons.Settings,
        headerBadge = stringResource(Res.string.ui_app_info_tech_cookies),
        onBack = onBack,
    )
}
