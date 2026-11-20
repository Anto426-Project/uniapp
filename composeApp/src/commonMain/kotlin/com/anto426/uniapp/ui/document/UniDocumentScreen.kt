package com.anto426.uniapp.ui.document

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.model.legal.LegalSectionData
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.updates.presentation.ChangelogUiState
import com.kyant.shapes.RoundedRectangle

@Composable
private fun DocumentHeaderContent(
    title: String?,
    subtitle: String?,
    headerBadge: String?,
    intro: String?,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    colorizeTitle: Boolean = false,
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        val eyebrowParts = listOfNotNull(
            headerBadge?.trim()?.takeIf(String::isNotBlank)?.uppercase(),
            subtitle?.trim()?.takeIf(String::isNotBlank),
        )
        if (eyebrowParts.isNotEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (!headerBadge.isNullOrBlank()) {
                    Text(
                        text = headerBadge.trim().uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        maxLines = 1,
                    )
                }
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = if (!headerBadge.isNullOrBlank()) "• ${subtitle.trim()}" else subtitle.trim(),
                        style = MaterialTheme.typography.labelMedium,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        if (!title.isNullOrBlank()) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                lineHeight = 33.sp,
                color = if (colorizeTitle) accentColor else colorScheme.onSurface,
            )
        }

        if (!intro.isNullOrBlank()) {
            Text(
                text = parseInlineMarkdown(intro.trim(), accentColor),
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 15.sp,
                lineHeight = 23.sp,
                color = colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DocumentSectionBlock(
    title: String?,
    body: String,
    indexLabel: String? = null,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    colorizeTitle: Boolean = false,
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (!title.isNullOrBlank()) {
            val formattedTitle = if (indexLabel.isNullOrBlank()) {
                parseInlineMarkdown(title, accentColor)
            } else {
                buildAnnotatedString {
                    withStyle(SpanStyle(color = accentColor, fontWeight = FontWeight.Bold)) {
                        append("$indexLabel. ")
                    }
                    append(parseInlineMarkdown(title, accentColor))
                }
            }
            Text(
                text = formattedTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                lineHeight = 24.sp,
                color = if (colorizeTitle) accentColor else colorScheme.onSurface,
            )
        }
        if (body.isNotBlank()) {
            RichMarkdownDocument(
                markdown = body,
                accentColor = accentColor,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

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
                DocumentHeaderContent(
                    title = title,
                    subtitle = subtitle,
                    headerBadge = headerBadge,
                    intro = intro,
                )
            }

            visibleSections.forEachIndexed { index, (sectionTitle, sectionBody) ->
                if (hasHeader || index > 0) {
                    LiquidHorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.25f))
                }
                DocumentSectionBlock(
                    title = sectionTitle,
                    body = sectionBody,
                    indexLabel = "${index + 1}",
                )
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
                DocumentHeaderContent(
                    title = title,
                    subtitle = subtitle,
                    headerBadge = headerBadge,
                    intro = leadingIntro,
                    accentColor = defaultAccent,
                    colorizeTitle = headerAccentColor != null,
                )
            }

            if (hasExplicitSections) {
                bodySections.forEachIndexed { index, section ->
                    if (hasHeader || index > 0) {
                        LiquidHorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.25f))
                    }
                    val sectionAccent = sectionAccentResolver?.invoke(section.title, index) ?: defaultAccent
                    DocumentSectionBlock(
                        title = section.title,
                        body = section.body,
                        accentColor = sectionAccent,
                        colorizeTitle = sectionAccentResolver != null,
                    )
                }
            } else {
                if (hasHeader) {
                    LiquidHorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.25f))
                }
                RichMarkdownDocument(
                    markdown = content,
                    accentColor = defaultAccent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                )
            }

            if (bottomContent != null) {
                bottomContent()
            }
        }
    }
}

@Composable
fun RichMarkdownDocument(
    markdown: String,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary,
) {
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    val linkColor = accentColor
    val blocks = remember(markdown) { DocumentMarkdownParser.parseMarkdownBlocks(markdown) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Heading -> {
                    Spacer(Modifier.height(if (block.level <= 2) 4.dp else 2.dp))
                    val (fontSize, lineHeight, color) = when (block.level) {
                        1 -> Triple(22.sp, 28.sp, colorScheme.onSurface)
                        2 -> Triple(18.sp, 24.sp, accentColor)
                        3 -> Triple(16.sp, 22.sp, colorScheme.onSurface)
                        else -> Triple(14.5.sp, 20.sp, accentColor)
                    }
                    Text(
                        text = parseInlineMarkdown(block.text, linkColor),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        fontSize = fontSize,
                        lineHeight = lineHeight,
                    )
                }

                is MarkdownBlock.Divider -> {
                    LiquidHorizontalDivider(
                        color = colorScheme.outlineVariant.copy(alpha = 0.25f),
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                }

                is MarkdownBlock.BlockQuote -> {
                    val accent = when (block.alertKind) {
                        MarkdownAlertKind.Tip -> glassColors.success
                        MarkdownAlertKind.Warning -> glassColors.warning
                        MarkdownAlertKind.Caution -> colorScheme.error
                        MarkdownAlertKind.Note, MarkdownAlertKind.Important, null -> accentColor
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min)
                            .clip(RoundedRectangle(12.dp))
                            .background(accent.copy(alpha = 0.08f))
                            .padding(end = 12.dp, top = 10.dp, bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .fillMaxHeight()
                                .background(accent),
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            if (block.alertKind != null) {
                                Text(
                                    text = block.alertKind.name.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = accent,
                                )
                            }
                            Text(
                                text = parseInlineMarkdown(block.text, linkColor),
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 14.sp,
                                lineHeight = 21.sp,
                                fontStyle = if (block.alertKind == null) FontStyle.Italic else FontStyle.Normal,
                                color = colorScheme.onSurface.copy(alpha = 0.90f),
                            )
                        }
                    }
                }

                is MarkdownBlock.ListItem -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = (block.indentLevel * 14).dp + 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Text(
                            text = block.marker,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            fontSize = 15.sp,
                        )
                        Text(
                            text = parseInlineMarkdown(block.text, linkColor),
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 14.5.sp,
                            lineHeight = 22.sp,
                            color = colorScheme.onSurface.copy(alpha = 0.90f),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                is MarkdownBlock.TaskItem -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = (block.indentLevel * 14).dp + 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Icon(
                            imageVector = if (block.checked) LiquidIcons.Check else LiquidIcons.Remove,
                            contentDescription = null,
                            tint = if (block.checked) glassColors.success else colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 3.dp).size(16.dp),
                        )
                        Text(
                            text = parseInlineMarkdown(block.text, linkColor),
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 14.5.sp,
                            lineHeight = 22.sp,
                            color = colorScheme.onSurface.copy(alpha = if (block.checked) 0.72f else 0.90f),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                is MarkdownBlock.CodeBlock -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedRectangle(12.dp))
                            .background(colorScheme.surfaceVariant.copy(alpha = 0.45f))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        if (!block.language.isNullOrBlank()) {
                            Text(
                                text = block.language.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = accentColor,
                            )
                        }
                        Text(
                            text = block.code,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.5.sp,
                            lineHeight = 18.sp,
                            color = colorScheme.onSurface,
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                        )
                    }
                }

                is MarkdownBlock.TableBlock -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedRectangle(12.dp))
                            .background(colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(accentColor.copy(alpha = 0.10f))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            block.headers.forEach { header ->
                                Text(
                                    text = parseInlineMarkdown(header, linkColor),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurface,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                        block.rows.forEach { row ->
                            LiquidHorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.22f))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                block.headers.indices.forEach { idx ->
                                    Text(
                                        text = parseInlineMarkdown(row.getOrElse(idx) { "" }, linkColor),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colorScheme.onSurface.copy(alpha = 0.88f),
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            }
                        }
                    }
                }

                is MarkdownBlock.Paragraph -> {
                    Text(
                        text = parseInlineMarkdown(block.text, linkColor),
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 14.5.sp,
                        lineHeight = 22.sp,
                        color = colorScheme.onSurface.copy(alpha = 0.90f),
                    )
                }
            }
        }
    }
}

private val inlineTokenRegex = Regex(
    """(\[(?<label>[^\]]+)\]\((?<url>(?:https?://|mailto:)[^\s)]+)\))|(<(?<autoUrl>(?:https?://|mailto:)[^\s>]+)>)|(\*\*(?<bold>[^*]+)\*\*)|(__(?<ubold>[^_]+)__)|(`(?<code>[^`]+)`)|(~~(?<strike>[^~]+)~~)|(\*(?<italic>[^*]+)\*)|((?<![A-Za-z0-9])_(?<uitalic>[^_\n]+)_(?![A-Za-z0-9]))|(?<rawUrl>https?://[^\s<>"{}|\\^`]+[^\s<>"{}|\\^`.,;:!?)\]])""",
)

fun parseInlineMarkdown(text: String, linkColor: Color): AnnotatedString {
    return buildAnnotatedString {
        var currentIndex = 0
        inlineTokenRegex.findAll(text).forEach { matchResult ->
            val range = matchResult.range
            if (range.first > currentIndex) {
                append(text.substring(currentIndex, range.first))
            }

            val linkLabel = matchResult.groups["label"]?.value
            val linkUrl = matchResult.groups["url"]?.value
            val autoUrl = matchResult.groups["autoUrl"]?.value
            val boldText = matchResult.groups["bold"]?.value ?: matchResult.groups["ubold"]?.value
            val italicText = matchResult.groups["italic"]?.value ?: matchResult.groups["uitalic"]?.value
            val codeText = matchResult.groups["code"]?.value
            val strikeText = matchResult.groups["strike"]?.value
            val rawUrl = matchResult.groups["rawUrl"]?.value

            when {
                linkLabel != null && linkUrl != null -> {
                    val cleanLabel = stripOuterEmphasis(linkLabel)
                    val extraBold = linkLabel.trim().startsWith("**") && linkLabel.trim().endsWith("**")
                    withLink(
                        LinkAnnotation.Url(
                            url = linkUrl,
                            styles = TextLinkStyles(
                                style = SpanStyle(
                                    color = linkColor,
                                    textDecoration = TextDecoration.Underline,
                                    fontWeight = if (extraBold) FontWeight.Bold else FontWeight.SemiBold,
                                ),
                            ),
                        ),
                    ) {
                        append(cleanLabel)
                    }
                }

                autoUrl != null || rawUrl != null -> {
                    val targetUrl = autoUrl ?: rawUrl.orEmpty()
                    withLink(
                        LinkAnnotation.Url(
                            url = targetUrl,
                            styles = TextLinkStyles(
                                style = SpanStyle(
                                    color = linkColor,
                                    textDecoration = TextDecoration.Underline,
                                ),
                            ),
                        ),
                    ) {
                        append(targetUrl)
                    }
                }

                boldText != null -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(parseInlineMarkdown(boldText, linkColor))
                    }
                }

                italicText != null -> {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(parseInlineMarkdown(italicText, linkColor))
                    }
                }

                strikeText != null -> {
                    withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                        append(parseInlineMarkdown(strikeText, linkColor))
                    }
                }

                codeText != null -> {
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = linkColor.copy(alpha = 0.12f),
                            color = linkColor,
                        ),
                    ) {
                        append(" $codeText ")
                    }
                }

                else -> append(matchResult.value)
            }

            currentIndex = range.last + 1
        }

        if (currentIndex < text.length) {
            append(text.substring(currentIndex))
        }
    }
}

private fun stripOuterEmphasis(raw: String): String {
    val trimmed = raw.trim()
    return if (trimmed.length >= 4 && ((trimmed.startsWith("**") && trimmed.endsWith("**")) || (trimmed.startsWith("__") && trimmed.endsWith("__")))) {
        trimmed.drop(2).dropLast(2).trim()
    } else {
        trimmed
    }
}

/**
 * Extension to convert a list of LegalSectionData into a clean Markdown document string.
 */
@Composable
fun List<LegalSectionData>.toMarkdownString(): String {
    val sb = StringBuilder()
    for (section in this) {
        val sectionTitle = section.title
        val sectionContent = section.content
        if (sb.isNotEmpty()) {
            sb.append("\n\n")
        }
        sb.append("## ").append(sectionTitle).append("\n\n").append(sectionContent)
    }
    return sb.toString()
}

/**
 * Extension to convert ChangelogUiState into a clean Markdown document string.
 */
@Composable
fun ChangelogUiState.toMarkdownString(): String {
    val version = versions.firstOrNull() ?: return ""
    val rawDesc = version.items.firstOrNull()?.rawDescription?.trim().orEmpty()
    if (rawDesc.isNotBlank()) {
        return DocumentMarkdownParser.normalizeDocumentMarkdown(rawDesc)
    }

    val sb = StringBuilder()
    for (item in version.items) {
        val title = item.title.trim()
        val desc = item.description.trim()
        if (title.isNotBlank() && desc.isNotBlank()) {
            sb.append("- **$title**: $desc\n")
        } else if (desc.isNotBlank()) {
            sb.append("- $desc\n")
        } else if (title.isNotBlank()) {
            sb.append("- **$title**\n")
        }
    }

    return sb.toString().trim()
}
