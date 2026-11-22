package com.anto426.uniapp.ui.components.layout

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.anto426.liquidmonet.components.layout.animatedswitcher.LiquidAnimatedSwitcher
import com.anto426.liquidmonet.components.layout.animatedswitcher.LiquidSwitcherMode
import com.anto426.liquidmonet.components.layout.animatedswitcher.LiquidSwitcherTransition
import com.anto426.liquidmonet.components.layout.screenentrance.LiquidScreenEntranceAnimation
import com.anto426.uniapp.ui.motion.LocalUniElementEntrance
import com.anto426.uniapp.ui.motion.UniMotion

/**
 * Shared host for inline content swaps (filters, loading/content states, segmented controls).
 * Uses its own dedicated [LiquidSwitcherMode.Inline] switcher motion instead of full-screen page
 * navigation transitions.
 *
 * When [suppressElementEntrance] is true, element entrance animations still play on initial render
 * (so opening a screen reveals its content naturally) and are suppressed only after the user
 * switches state inside the host, avoiding double animation during tab/filter transitions.
 */
@Composable
internal fun <T> UniContentHost(
    targetState: T,
    modifier: Modifier = Modifier,
    transition: LiquidSwitcherTransition = UniMotion.inlineContentTransition,
    mode: LiquidSwitcherMode = LiquidSwitcherMode.Inline,
    isForward: ((from: T, to: T) -> Boolean)? = null,
    onSwipeForward: (() -> Unit)? = null,
    onSwipeBackward: (() -> Unit)? = null,
    suppressElementEntrance: Boolean = true,
    contentAlignment: Alignment = Alignment.TopStart,
    label: String = "UniContentHost",
    content: @Composable (T) -> Unit,
) {
    val initialState = remember { targetState }
    var hasSwitched by remember { mutableStateOf(false) }
    if (!hasSwitched && targetState != initialState) {
        hasSwitched = true
    }

    LiquidAnimatedSwitcher(
        targetState = targetState,
        modifier = modifier,
        transition = transition,
        mode = mode,
        isForward = isForward,
        onSwipeForward = onSwipeForward,
        onSwipeBackward = onSwipeBackward,
        contentAlignment = contentAlignment,
        label = label,
    ) { state ->
        if (suppressElementEntrance && hasSwitched) {
            CompositionLocalProvider(
                LocalUniElementEntrance provides LiquidScreenEntranceAnimation.None,
            ) {
                content(state)
            }
        } else {
            content(state)
        }
    }
}

/**
 * Directional switcher host paired with [com.anto426.liquidmonet.components.navigation.tabbar.LiquidTabBar].
 * Uses dedicated [LiquidSwitcherMode.Tab] motion (independent from full-screen page transitions)
 * and infers forward/backward tab direction automatically without horizontal swipe gestures.
 */
@Composable
internal fun UniTabContentHost(
    selectedTab: Int,
    tabCount: Int,
    @Suppress("UNUSED_PARAMETER")
    onTabSelected: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier,
    @Suppress("UNUSED_PARAMETER")
    swipeEnabled: Boolean = false,
    label: String = "UniTabContentHost",
    content: @Composable (Int) -> Unit,
) {
    val maxIndex = (tabCount - 1).coerceAtLeast(0)
    val clampedTab = selectedTab.coerceIn(0, maxIndex)

    UniContentHost(
        targetState = clampedTab,
        modifier = modifier.fillMaxWidth(),
        transition = UniMotion.contentTransition,
        mode = LiquidSwitcherMode.Tab,
        isForward = { from, to -> to >= from },
        contentAlignment = Alignment.TopStart,
        label = label,
        content = content,
    )
}
