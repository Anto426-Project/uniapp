package com.anto426.uniapp.ui.motion

import androidx.compose.runtime.compositionLocalOf
import com.anto426.liquidmonet.components.layout.animatedswitcher.LiquidSwitcherTransition
import com.anto426.liquidmonet.components.layout.screenentrance.LiquidScreenEntranceAnimation
import com.anto426.uniapp.settings.model.PageMotionPreferences
import org.jetbrains.compose.resources.StringResource
import uniapp.composeapp.generated.resources.*

/** One SDK preset for changing app content; the SDK owns timing and reduced motion. */
internal object UniMotion {
    val contentTransition = LiquidSwitcherTransition.SlideHorizontal
    val inlineContentTransition = LiquidSwitcherTransition.Crossfade
    val defaultElementEntrance = LiquidScreenEntranceAnimation.None

    val options = (listOf(contentTransition) + LiquidSwitcherTransition.entries.filter {
        it != contentTransition && it != LiquidSwitcherTransition.None
    })
        .map { transition ->
            val labels = when (transition) {
                LiquidSwitcherTransition.None -> error("None is not a selectable animation")
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

    val elementEntranceOptions = listOf(
        LiquidScreenEntranceAnimation.None,
        LiquidScreenEntranceAnimation.FadeUp,
        LiquidScreenEntranceAnimation.FadeDown,
        LiquidScreenEntranceAnimation.Fade,
        LiquidScreenEntranceAnimation.Scale,
    ).map { animation ->
        val labels = when (animation) {
            LiquidScreenEntranceAnimation.None -> Res.string.ui_entrance_none to Res.string.ui_entrance_none_sub
            LiquidScreenEntranceAnimation.Fade -> Res.string.ui_entrance_fade to Res.string.ui_entrance_fade_sub
            LiquidScreenEntranceAnimation.FadeUp -> Res.string.ui_entrance_fade_up to Res.string.ui_entrance_fade_up_sub
            LiquidScreenEntranceAnimation.FadeDown -> Res.string.ui_entrance_fade_down to Res.string.ui_entrance_fade_down_sub
            LiquidScreenEntranceAnimation.Scale -> Res.string.ui_entrance_scale to Res.string.ui_entrance_scale_sub
        }
        UniElementEntranceOption(animation, labels.first, labels.second)
    }

    fun selectedTransition(preferences: PageMotionPreferences): LiquidSwitcherTransition =
        options.firstOrNull { it.transition.name == preferences.transitionName }?.transition ?: contentTransition

    fun effectiveTransition(preferences: PageMotionPreferences): LiquidSwitcherTransition =
        selectedTransition(preferences)

    fun parseElementEntrance(name: String?): LiquidScreenEntranceAnimation =
        LiquidScreenEntranceAnimation.entries.firstOrNull { it.name.equals(name, ignoreCase = true) }
            ?: defaultElementEntrance

    fun normalized(preferences: PageMotionPreferences): PageMotionPreferences {
        val transition = selectedTransition(preferences)
        return preferences.copy(
            enabled = true,
            transitionName = transition.name,
        )
    }
}

internal data class UniMotionOption(
    val transition: LiquidSwitcherTransition,
    val title: StringResource,
    val description: StringResource,
)

internal data class UniElementEntranceOption(
    val animation: LiquidScreenEntranceAnimation,
    val title: StringResource,
    val description: StringResource,
)

internal val LocalUniContentTransition = compositionLocalOf { UniMotion.contentTransition }
internal val LocalUniElementEntrance = compositionLocalOf { UniMotion.defaultElementEntrance }
