package com.anto426.uniapp.ui.document

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.model.legal.LegalSectionData
import com.anto426.uniapp.ui.components.layout.UniAnimatedEntrance
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.components.layout.uniListEntranceOrder
import com.anto426.uniapp.ui.document.components.DocumentHeaderContent
import com.anto426.uniapp.ui.document.components.DocumentSectionBlock
import com.anto426.uniapp.ui.document.components.RichMarkdownDocument
import com.kyant.shapes.RoundedRectangle

/**
 * Editorial containerless layout for sectioned documents (About, Privacy, Terms, Cookies).
 */
@Composable
fun UniSectionedDocumentScreen(
    sections: List<LegalSectionData>,
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    intro: String? = null,
    headerIcon: ImageVector? = null,
    headerBadge: String? = null,
    footerActionTitle: String? = null,
    footerActionSubtitle: String? = null,
    footerActionIcon: ImageVector? = null,
    onFooterActionClick: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    bottomContent: (@Composable () -> Unit)? = null,
) {
    val colorScheme = MaterialTheme.colorScheme
    val visibleSections = sections.mapNotNull { section ->
        val sectionTitle = section.title.trim()
        val sectionBody = section.content.trim()
        if (sectionTitle.isBlank() && sectionBody.isBlank()) null else sectionTitle to sectionBody
    }

    UniScreenColumn(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            val hasHeader = !title.isNullOrBlank() || !intro.isNullOrBlank()
            if (hasHeader) {
                UniAnimatedEntrance(order = 0) {
                    DocumentHeaderContent(
                        title = title,
                        subtitle = subtitle,
                        headerBadge = headerBadge,
                        intro = intro,
                    )
                }
            }

            visibleSections.forEachIndexed { index, (sectionTitle, sectionBody) ->
                if (hasHeader || index > 0) {
                    LiquidHorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.25f))
                }
                UniAnimatedEntrance(order = uniListEntranceOrder(index, offset = if (hasHeader) 1 else 0)) {
                    DocumentSectionBlock(
                        title = sectionTitle,
                        body = sectionBody,
                        indexLabel = "${index + 1}",
                    )
                }
            }

            if (!footerActionTitle.isNullOrBlank() && onFooterActionClick != null) {
                LiquidHorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.25f))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedRectangle(14.dp))
                        .background(colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .clickable(onClick = onFooterActionClick)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (footerActionIcon != null) {
                        Icon(
                            imageVector = footerActionIcon,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = footerActionTitle,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (!footerActionSubtitle.isNullOrBlank()) {
                            Text(
                                text = footerActionSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    Icon(
                        imageVector = LiquidIcons.ChevronRight,
                        contentDescription = null,
                        tint = colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            if (bottomContent != null) {
                bottomContent()
            }
        }
    }
}

/**
 * Editorial containerless layout for Markdown documents (`Changelog`, release notes, docs).
 */
@Composable
fun UniDocumentScreen(
    content: String,
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    headerIcon: ImageVector? = null,
    headerBadge: String? = null,
    headerAccentColor: Color? = null,
    sectionAccentResolver: ((sectionTitle: String?, index: Int) -> Color)? = null,
    onBack: (() -> Unit)? = null,
    bottomContent: (@Composable () -> Unit)? = null,
) {
    val colorScheme = MaterialTheme.colorScheme
    val defaultAccent = headerAccentColor ?: colorScheme.primary
    val parsedSections = remember(content) { DocumentMarkdownParser.splitMarkdownSections(content) }
    val hasExplicitSections = parsedSections.any { !it.title.isNullOrBlank() }
    val leadingIntro = if (hasExplicitSections && parsedSections.firstOrNull()?.title == null) {
        parsedSections.firstOrNull()?.body
    } else {
        null
    }
    val bodySections = if (hasExplicitSections) {
        parsedSections.filterNot { it.title == null && it.body == leadingIntro }
    } else {
        emptyList()
    }

    UniScreenColumn(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            val hasHeader = !title.isNullOrBlank() || !leadingIntro.isNullOrBlank()
            if (hasHeader) {
                UniAnimatedEntrance(order = 0) {
                    DocumentHeaderContent(
                        title = title,
                        subtitle = subtitle,
                        headerBadge = headerBadge,
                        intro = leadingIntro,
                        accentColor = defaultAccent,
                        colorizeTitle = headerAccentColor != null,
                    )
                }
            }

            if (hasExplicitSections) {
                bodySections.forEachIndexed { index, section ->
                    if (hasHeader || index > 0) {
                        LiquidHorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.25f))
                    }
                    val sectionAccent = sectionAccentResolver?.invoke(section.title, index) ?: defaultAccent
                    UniAnimatedEntrance(order = uniListEntranceOrder(index, offset = if (hasHeader) 1 else 0)) {
                        DocumentSectionBlock(
                            title = section.title,
                            body = section.body,
                            accentColor = sectionAccent,
                            colorizeTitle = sectionAccentResolver != null,
                        )
                    }
                }
            } else {
                if (hasHeader) {
                    LiquidHorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.25f))
                }
                UniAnimatedEntrance(order = if (hasHeader) 1 else 0) {
                    RichMarkdownDocument(
                        markdown = content,
                        accentColor = defaultAccent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                    )
                }
            }

            if (bottomContent != null) {
                bottomContent()
            }
        }
    }
}
