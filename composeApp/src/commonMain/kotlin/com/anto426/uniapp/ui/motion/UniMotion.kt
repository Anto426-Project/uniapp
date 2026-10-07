package com.anto426.uniapp.ui.motion

import com.anto426.liquidmonet.components.layout.animatedswitcher.LiquidSwitcherTransition
import androidx.compose.runtime.compositionLocalOf
import com.anto426.uniapp.settings.model.PageMotionPreferences
import org.jetbrains.compose.resources.StringResource
import uniapp.composeapp.generated.resources.*

/** One SDK preset for changing app content; the SDK owns timing and reduced motion. */
internal object UniMotion {
    val contentTransition = LiquidSwitcherTransition.Crossfade

    val options = (listOf(contentTransition) + LiquidSwitcherTransition.entries.filter { it != contentTransition })
        .map { transition ->
            val labels = when (transition) {
                LiquidSwitcherTransition.None -> Res.string.ui_motion_none to Res.string.ui_motion_none_sub
                LiquidSwitcherTransition.Crossfade -> Res.string.ui_motion_crossfade to Res.string.ui_motion_crossfade_sub
                LiquidSwitcherTransition.FadeThrough -> Res.string.ui_motion_fade_through to Res.string.ui_motion_fade_through_sub
                LiquidSwitcherTransition.LiquidMorph -> Res.string.ui_motion_morph to Res.string.ui_motion_morph_sub
                LiquidSwitcherTransition.DirectionalHorizontal -> Res.string.ui_motion_directional_horizontal to Res.string.ui_motion_directional_horizontal_sub
                LiquidSwitcherTransition.DirectionalVertical -> Res.string.ui_motion_directional_vertical to Res.string.ui_motion_directional_vertical_sub
                LiquidSwitcherTransition.SlideHorizontal -> Res.string.ui_motion_slide_horizontal to Res.string.ui_motion_slide_horizontal_sub
                LiquidSwitcherTransition.SlideVertical -> Res.string.ui_motion_slide_vertical to Res.string.ui_motion_slide_vertical_sub
                LiquidSwitcherTransition.SharedAxisDepth -> Res.string.ui_motion_depth to Res.string.ui_motion_depth_sub
            }
            UniMotionOption(transition, labels.first, labels.second)
        }

    fun selectedTransition(preferences: PageMotionPreferences): LiquidSwitcherTransition =
        LiquidSwitcherTransition.entries.firstOrNull { it.name == preferences.transitionName } ?: contentTransition

    fun effectiveTransition(preferences: PageMotionPreferences): LiquidSwitcherTransition =
        if (preferences.enabled) selectedTransition(preferences) else LiquidSwitcherTransition.None

    fun normalized(preferences: PageMotionPreferences): PageMotionPreferences {
        val transition = selectedTransition(preferences)
        return preferences.copy(
            enabled = preferences.enabled && transition != LiquidSwitcherTransition.None,
            transitionName = transition.name,
        )
    }
}

internal data class UniMotionOption(
    val transition: LiquidSwitcherTransition,
    val title: StringResource,
    val description: StringResource,
)

internal val LocalUniContentTransition = compositionLocalOf { UniMotion.contentTransition }
