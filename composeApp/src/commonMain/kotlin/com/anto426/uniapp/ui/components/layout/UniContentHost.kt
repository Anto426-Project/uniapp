package com.anto426.uniapp.ui.components.layout

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.anto426.liquidmonet.components.layout.animatedswitcher.LiquidAnimatedSwitcher
import com.anto426.uniapp.ui.motion.LocalUniContentTransition

/** Shared host for feature content changes; Liquid Monet owns timing and reduced motion. */
@Composable
internal fun <T> UniContentHost(
    targetState: T,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.Center,
    label: String = "UniContentHost",
    content: @Composable (T) -> Unit,
) {
    LiquidAnimatedSwitcher(
        targetState = targetState,
        modifier = modifier,
        transition = LocalUniContentTransition.current,
        contentAlignment = contentAlignment,
        label = label,
    ) { state -> content(state) }
}
