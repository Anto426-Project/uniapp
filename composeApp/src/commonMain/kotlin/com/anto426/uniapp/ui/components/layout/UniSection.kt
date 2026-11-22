package com.anto426.uniapp.ui.components.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.layout.screenentrance.LiquidScreenEntranceAnimation
import com.anto426.liquidmonet.components.layout.sectionentrance.LiquidSectionEntrance
import com.anto426.uniapp.ui.motion.LocalUniElementEntrance
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

/**
 * Tracks whether newly composed items belong to a screen's initial opening cascade or were
 * composed later during scrolling (in which case they reveal immediately with 0ms delay).
 */
internal val LocalUniEntranceEpoch = compositionLocalOf<(() -> Boolean)?> { null }

@Composable
internal fun ProvideUniEntranceEpoch(content: @Composable () -> Unit) {
    val openedAt = remember { TimeSource.Monotonic.markNow() }
    val isInitialWindow = remember(openedAt) { { openedAt.elapsedNow() < 350.milliseconds } }
    CompositionLocalProvider(
        LocalUniEntranceEpoch provides isInitialWindow,
        content = content,
    )
}

/**
 * Monotonic stagger order for repeating list items (never resets to 0 on tall screens so lower
 * items never appear ahead of higher ones). Items composed later during scrolling get 0ms delay
 * automatically via [LocalUniEntranceEpoch].
 */
internal fun uniListEntranceOrder(index: Int, offset: Int = 0): Int =
    (index + offset).coerceIn(0, 6)

/**
 * Reveals a screen section using the user's configured [LocalUniElementEntrance] animation.
 * Skips animation wrappers completely when [LiquidScreenEntranceAnimation.None] is selected.
 */
@Composable
fun UniAnimatedEntrance(
    order: Int = 0,
    modifier: Modifier = Modifier,
    replayKey: Any? = Unit,
    content: @Composable () -> Unit,
) {
    val animation = LocalUniElementEntrance.current
    if (animation == LiquidScreenEntranceAnimation.None) {
        content()
    } else {
        val epoch = LocalUniEntranceEpoch.current
        val delayMillis = remember(order, replayKey) {
            if (epoch == null || epoch()) order.coerceIn(0, 6) * 45 else 0
        }
        LiquidSectionEntrance(
            modifier = modifier,
            animation = animation,
            delayMillis = delayMillis,
            replayKey = replayKey,
            content = content,
        )
    }
}

/** Section layout with optional staggered entrance driven by [LocalUniElementEntrance]. */
@Composable
fun UniSection(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(16.dp),
    entranceOrder: Int = 0,
    animateEntrance: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val body: @Composable () -> Unit = {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = verticalArrangement,
            content = content,
        )
    }
    if (animateEntrance) {
        UniAnimatedEntrance(order = entranceOrder) { body() }
    } else {
        body()
    }
}
