package com.anto426.uniapp.ui.motion

import com.anto426.liquidmonet.components.layout.animatedswitcher.LiquidSwitcherTransition

/** One SDK preset for changing app content; the SDK owns timing and reduced motion. */
internal object UniMotion {
    val contentTransition = LiquidSwitcherTransition.Crossfade
}
