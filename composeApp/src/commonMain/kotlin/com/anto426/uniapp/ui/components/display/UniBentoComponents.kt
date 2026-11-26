package com.anto426.uniapp.ui.components.display

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.badge.LiquidBadgedBox
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.iconcontainer.liquidIconContainer
import com.anto426.liquidmonet.components.feedback.progressbar.LiquidCircularProgressIndicator
import com.anto426.liquidmonet.components.interaction.liquidClickable
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.ui.components.layout.UniAnimatedEntrance
import com.kyant.shapes.RoundedRectangle

/**
 * Circular progress ring backed by Liquid Monet's [LiquidCircularProgressIndicator]
 * with centered tabular metric copy.
 */
@Composable
fun UniCircularProgressRing(
    progress: Float,
    centerText: String,
    modifier: Modifier = Modifier,
    subtitleText: String? = null,
    size: Dp = 68.dp,
    strokeWidth: Dp = 6.dp,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.tertiary,
) {
    val colorScheme = MaterialTheme.colorScheme
    @Suppress("UNUSED_EXPRESSION") secondaryColor

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        LiquidCircularProgressIndicator(
            modifier = Modifier.size(size),
            progress = progress.coerceIn(0f, 1f),
            indicatorSize = size,
            strokeWidth = strokeWidth,
            progressColor = if (primaryColor == colorScheme.primary) null else primaryColor,
        )

        val (adaptiveFontSize, adaptiveLineHeight) = when {
            centerText.length >= 6 -> 12.sp to 14.sp
            centerText.length == 5 -> 13.sp to 15.sp
            centerText.length == 4 -> 14.sp to 16.sp
            else -> 15.sp to 17.sp
        }

        Column(
            modifier = Modifier.padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = centerText,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontFeatureSettings = "tnum",
                    letterSpacing = (-0.4).sp,
                ),
                fontWeight = FontWeight.ExtraBold,
                fontSize = adaptiveFontSize,
                lineHeight = adaptiveLineHeight,
                color = colorScheme.onSurface,
                maxLines = 1,
                softWrap = false,
            )
            if (!subtitleText.isNullOrBlank()) {
                Text(
                    text = subtitleText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFeatureSettings = "tnum",
                    ),
                    fontSize = 9.sp,
                    lineHeight = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }
    }
}

/**
 * Frosted sub-surface metric tile for bento hero cards across Home and Didactics.
 */
@Composable
fun UniBentoStatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    suffix: String? = null,
    icon: ImageVector? = null,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    cornerRadius: Dp = 18.dp,
) {
    val colorScheme = MaterialTheme.colorScheme
    val shape = RoundedRectangle(cornerRadius)
    Column(
        modifier = modifier
            .clip(shape)
            .border(
                width = 1.dp,
                color = colorScheme.onSurface.copy(alpha = 0.08f),
                shape = shape,
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(13.dp),
                )
            }
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp,
                color = colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!suffix.isNullOrBlank()) {
                Text(
                    text = suffix,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 3.dp),
                    maxLines = 1,
                )
            }
        }
    }
}

/** Shared geometry defaults for every card, group, tile, and row across UniApp. */
object UniBlockDefaults {
    val CornerRadius: Dp = 24.dp
    val HeroCornerRadius: Dp = 28.dp
    val ContentPadding: PaddingValues = PaddingValues(18.dp)
    val RowMinHeight: Dp = 64.dp
    val RowHorizontalPadding: Dp = 16.dp
    val RowVerticalPadding: Dp = 12.dp
    val TileHeight: Dp = 132.dp
    val MetricTileHeight: Dp = 148.dp
    val TileMinHeight: Dp = 96.dp
    val TileGap: Dp = 12.dp
    val IconContainerSize: Dp = 36.dp
    val IconSize: Dp = 18.dp
    val IconCornerRadius: Dp = 11.dp
    val DividerStartInset: Dp = 66.dp
    val DividerEndInset: Dp = 16.dp
}

/**
 * Unified Apple Inset Grouped block container with optional section header, consistent 24.dp
 * corner radius, entrance motion, border, and configurable inner padding.
 */
@Composable
fun UniInsetGroup(
    modifier: Modifier = Modifier,
    title: String? = null,
    cornerRadius: Dp = UniBlockDefaults.CornerRadius,
    border: BorderStroke? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onClick: (() -> Unit)? = null,
    animateEntrance: Boolean = true,
    entranceOrder: Int = 0,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val groupBody: @Composable () -> Unit = {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .graphicsLayer(clip = false),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (!title.isNullOrBlank()) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.7.sp,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 10.dp),
                )
            }

            LiquidCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer(clip = false),
                shape = RoundedRectangle(cornerRadius),
                border = border,
                contentPadding = contentPadding,
                clipContent = false,
                onClick = onClick,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().graphicsLayer(clip = false),
                    content = content,
                )
            }
        }
    }
    if (animateEntrance) {
        UniAnimatedEntrance(order = entranceOrder) { groupBody() }
    } else {
        groupBody()
    }
}

/**
 * Unified Apple Inset Grouped row with consistent 64.dp minimum height, 36.dp squircle icon or
 * custom leading slot, optional rich subtitle slot, trailing slot and/or chevron, and inset divider.
 */
@Composable
fun UniInsetRow(
    title: String,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: (() -> Unit)? = null,
    clickRole: Role? = null,
    showDivider: Boolean = true,
    showTrailingChevron: Boolean = false,
    titleMaxLines: Int = 1,
    subtitleMaxLines: Int = 1,
    minHeight: Dp = UniBlockDefaults.RowMinHeight,
    dividerStartPadding: Dp = UniBlockDefaults.DividerStartInset,
    leadingContent: (@Composable () -> Unit)? = null,
    subtitleContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    val colorScheme = MaterialTheme.colorScheme
    val rowShape = remember { RoundedRectangle(14.dp) }
    val iconShape = remember { RoundedRectangle(UniBlockDefaults.IconCornerRadius) }
    val hasLeading = leadingContent != null || icon != null
    val effectiveDividerStart = if (hasLeading) dividerStartPadding else UniBlockDefaults.RowHorizontalPadding

    Column(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer(clip = false)
            .liquidClickable(shape = rowShape, role = clickRole ?: Role.Button, onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer(clip = false)
                .defaultMinSize(minHeight = minHeight)
                .semantics(mergeDescendants = true) {}
                .padding(
                    horizontal = UniBlockDefaults.RowHorizontalPadding,
                    vertical = UniBlockDefaults.RowVerticalPadding,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            when {
                leadingContent != null -> leadingContent()
                icon != null -> Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.liquidIconContainer(
                        containerSize = UniBlockDefaults.IconContainerSize,
                        iconSize = UniBlockDefaults.IconSize,
                        containerColor = iconTint.copy(alpha = 0.14f),
                        shape = iconShape,
                    ),
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.15).sp,
                    color = titleColor,
                    maxLines = titleMaxLines,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 16.sp,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = subtitleMaxLines,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                subtitleContent?.invoke()
            }

            when {
                trailingContent != null && showTrailingChevron -> Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    trailingContent()
                    Icon(
                        imageVector = LiquidIcons.ChevronRight,
                        contentDescription = null,
                        tint = colorScheme.onSurfaceVariant.copy(alpha = 0.48f),
                        modifier = Modifier.size(16.dp),
                    )
                }
                trailingContent != null -> trailingContent()
                onClick != null -> Icon(
                    imageVector = LiquidIcons.ChevronRight,
                    contentDescription = null,
                    tint = colorScheme.onSurfaceVariant.copy(alpha = 0.48f),
                    modifier = Modifier.size(16.dp),
                )
            }
        }

        if (showDivider) {
            LiquidHorizontalDivider(
                modifier = Modifier.padding(
                    start = effectiveDividerStart,
                    end = UniBlockDefaults.DividerEndInset,
                ),
            )
        }
    }
}

/** Shared icon + metadata row used inside expanded exam, history, and detail blocks. */
@Composable
fun UniDetailMetaRow(
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.78f),
    maxLines: Int = 3,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * Standardized 2-column Bento tile used across Didactics, Services, and Home status widgets.
 */
@Composable
fun UniBentoTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    headlineValue: String? = null,
    headlineValueColor: Color = MaterialTheme.colorScheme.onSurface,
    badgeCount: Int? = null,
    badgeLabel: String? = null,
    subtitleMaxLines: Int = 2,
    tileHeight: Dp = if (headlineValue.isNullOrBlank()) UniBlockDefaults.TileHeight else UniBlockDefaults.MetricTileHeight,
    border: BorderStroke? = null,
    onClick: () -> Unit = {},
) {
    val colorScheme = MaterialTheme.colorScheme
    val tileShape = remember { RoundedRectangle(UniBlockDefaults.CornerRadius) }
    val iconShape = remember { RoundedRectangle(UniBlockDefaults.IconCornerRadius) }

    LiquidCard(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(tileHeight)
            .semantics(mergeDescendants = true) {}
            .graphicsLayer(clip = false),
        shape = tileShape,
        border = border,
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LiquidBadgedBox(badge = { badgeCount?.let { LiquidBadge(count = it) } }) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.liquidIconContainer(
                            containerSize = UniBlockDefaults.IconContainerSize,
                            iconSize = UniBlockDefaults.IconSize,
                            containerColor = iconTint.copy(alpha = 0.14f),
                            shape = iconShape,
                        ),
                    )
                }
                if (!badgeLabel.isNullOrBlank()) {
                    LiquidBadge(
                        text = badgeLabel,
                        containerColor = iconTint.copy(alpha = 0.16f),
                        contentColor = iconTint,
                    )
                } else {
                    Icon(
                        imageVector = LiquidIcons.ChevronRight,
                        contentDescription = null,
                        tint = colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                if (!headlineValue.isNullOrBlank()) {
                    Text(
                        text = headlineValue,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.6).sp,
                        color = headlineValueColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = title,
                    style = if (headlineValue.isNullOrBlank()) {
                        MaterialTheme.typography.titleSmall
                    } else {
                        MaterialTheme.typography.labelLarge
                    },
                    fontWeight = if (headlineValue.isNullOrBlank()) FontWeight.Bold else FontWeight.SemiBold,
                    color = colorScheme.onSurface,
                    letterSpacing = (-0.2).sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = subtitleMaxLines,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** Symmetrical 2-column row wrapper for [UniBentoTile] grids with matched height. */
@Composable
fun UniBentoTileRow(
    item1: @Composable () -> Unit,
    item2: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max)
            .graphicsLayer(clip = false),
        horizontalArrangement = Arrangement.spacedBy(UniBlockDefaults.TileGap),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .graphicsLayer(clip = false),
        ) {
            item1()
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .graphicsLayer(clip = false),
        ) {
            item2()
        }
    }
}
