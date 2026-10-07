package com.anto426.uniapp.navigation.ui

import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import com.anto426.liquidmonet.components.layout.animatedswitcher.LiquidSwitcherTransition
import com.anto426.uniapp.navigation.model.AppRoute
import com.anto426.uniapp.ui.motion.UniMotion

/** Only route eligibility lives here; the SDK owns every animation transform and timing. */
internal object AppScreenTransitions {
    const val RouteMetadataKey = "com.anto426.uniapp.route"

    fun preset(from: Scene<NavKey>, to: Scene<NavKey>): LiquidSwitcherTransition =
        preset(from.route(), to.route())

    private fun Scene<NavKey>.route(): AppRoute? =
        entries.lastOrNull()?.metadata?.get(RouteMetadataKey) as? AppRoute

    fun preset(from: AppRoute?, to: AppRoute?): LiquidSwitcherTransition =
        if (crossesSessionBoundary(from, to)) LiquidSwitcherTransition.None else UniMotion.contentTransition

    // Session changes invalidate the outgoing entry before an exit animation can finish.
    private fun crossesSessionBoundary(from: AppRoute?, to: AppRoute?): Boolean =
        from == AppRoute.Bootstrap || to == AppRoute.Bootstrap ||
            to == AppRoute.Login || (from == AppRoute.Login && to == AppRoute.Home)
}
