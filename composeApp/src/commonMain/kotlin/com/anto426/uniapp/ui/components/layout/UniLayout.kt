package com.anto426.uniapp.ui.components.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.*
import com.kyant.shapes.RoundedRectangle
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.ScrollAxisRange
import androidx.compose.ui.semantics.horizontalScrollAxisRange
import androidx.compose.ui.semantics.scrollBy
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.LayoutDirection
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.cards.card.LiquidCardDefaults
import com.anto426.liquidmonet.icons.LiquidIcons
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

val LocalUniScreenPadding = compositionLocalOf { PaddingValues(0.dp) }
val LocalNavigationBarVisible = compositionLocalOf { true }

/**
 * Expands a horizontal child past the parent's horizontal content padding so scrollable chips
 * cross cleanly over the screen or card borders without being sliced mid-pill.
 */
fun Modifier.fullBleedHorizontal(bleed: Dp = 18.dp): Modifier = layout { measurable, constraints ->
    val bleedPx = bleed.roundToPx()
    val expandedMinWidth = (constraints.minWidth + bleedPx * 2).coerceAtLeast(0)
    val expandedMaxWidth = if (constraints.hasBoundedWidth) {
        (constraints.maxWidth + bleedPx * 2).coerceAtLeast(expandedMinWidth)
    } else {
        constraints.maxWidth
    }
    val placeable = measurable.measure(
        constraints.copy(
            minWidth = expandedMinWidth,
            maxWidth = expandedMaxWidth,
        ),
    )
    layout(constraints.maxWidth.coerceAtLeast(0), placeable.height) {
        placeable.place(-bleedPx, 0)
    }
}

/** Scrolls a row of chips. Set [clipToBounds] when the row is confined by a card or panel. */
@Composable
fun UniHorizontalChipRow(
    bleed: Dp = 18.dp,
    spacing: Dp = 8.dp,
    clipToBounds: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val ltr = LocalLayoutDirection.current == LayoutDirection.Ltr
    val scope = rememberCoroutineScope()
    var offset by rememberSaveable { mutableFloatStateOf(0f) }
    var maxOffset by remember { mutableIntStateOf(0) }
    val scrollState = rememberScrollableState { delta ->
        val before = offset.coerceIn(0f, maxOffset.toFloat())
        offset = (before + delta).coerceIn(0f, maxOffset.toFloat())
        offset - before
    }

    Layout(
        modifier = modifier
            .fillMaxWidth()
            .fullBleedHorizontal(bleed)
            .then(if (clipToBounds) Modifier.clipToBounds() else Modifier)
            .scrollable(scrollState, Orientation.Horizontal, reverseDirection = ltr)
            .semantics {
                horizontalScrollAxisRange = ScrollAxisRange(
                    value = { offset.coerceIn(0f, maxOffset.toFloat()) },
                    maxValue = { maxOffset.toFloat() },
                )
                scrollBy { x, _ ->
                    if (x == 0f || maxOffset == 0) false
                    else {
                        scope.launch { scrollState.animateScrollBy(x) }
                        true
                    }
                }
            }
            .padding(vertical = 8.dp),
        content = content,
    ) { measurables, constraints ->
        val inset = bleed.roundToPx()
        val gap = spacing.roundToPx()
        val chips = measurables.map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }
        val stripWidth = chips.sumOf { it.width } + gap * (chips.size - 1).coerceAtLeast(0)
        val width = constraints.constrainWidth(stripWidth + inset * 2)
        val height = constraints.constrainHeight(chips.maxOfOrNull { it.height } ?: 0)
        val range = (stripWidth + inset * 2 - width).coerceAtLeast(0)
        maxOffset = range
        layout(width, height) {
            var start = inset - offset.roundToInt().coerceIn(0, range)
            chips.forEach { chip ->
                // Keep the contour of any visible chip intact as it crosses the viewport edge.
                if (start < width && start + chip.width > 0) {
                    chip.placeRelative(start, (height - chip.height) / 2)
                }
                start += chip.width + gap
            }
        }
    }
}

@Composable
fun UniScreenColumn(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val padding = LocalUniScreenPadding.current
    LazyColumn(
        modifier = modifier.fillMaxSize().graphicsLayer(clip = false),
        contentPadding =
            PaddingValues(
                start = 18.dp,
                top = padding.calculateTopPadding(),
                end = 18.dp,
                bottom = padding.calculateBottomPadding() + 8.dp,
            ),
    ) {
        item(key = "screen-content") {
            Column(
                modifier = Modifier.fillMaxWidth().graphicsLayer(clip = false),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                content = content,
            )
        }
    }
}

@Composable
fun UniScreenLazyColumn(content: LazyListScope.() -> Unit) {
    val padding = LocalUniScreenPadding.current
    LazyColumn(
        modifier = Modifier.fillMaxSize().graphicsLayer(clip = false),
        contentPadding =
            PaddingValues(
                start = 18.dp,
                top = padding.calculateTopPadding(),
                end = 18.dp,
                bottom = padding.calculateBottomPadding() + 8.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content,
    )
}


/**
 * A decorative card inspired by a well-known slogan, adapted for UniApp.
 */
@Composable
fun UniNeverSettleCard(
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    LiquidCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedRectangle(32.dp),
        colors = LiquidCardDefaults.colors(
            containerColor = colorScheme.primaryContainer.copy(alpha = 0.15f)
        ),
        contentPadding = 24.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.ui_brand_experience),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    ),
                    color = colorScheme.primary.copy(alpha = 0.8f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.ui_brand_motto_first),
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 32.sp,
                            letterSpacing = (-1).sp
                        ),
                        color = colorScheme.onSurface
                    )

                    Surface(
                        color = colorScheme.primary,
                        shape = RoundedRectangle(12.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.ui_brand_motto_second),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            ),
                            color = colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(colorScheme.primary.copy(alpha = 0.1f), RoundedRectangle(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = LiquidIcons.Star,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
