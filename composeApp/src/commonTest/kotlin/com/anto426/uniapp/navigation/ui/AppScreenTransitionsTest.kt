package com.anto426.uniapp.navigation.ui

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import com.anto426.liquidmonet.components.layout.animatedswitcher.LiquidSwitcherTransition
import com.anto426.uniapp.navigation.model.AppRoute
import com.anto426.uniapp.navigation.model.appTopLevelRoutes
import com.anto426.uniapp.ui.motion.UniMotion
import kotlin.test.Test
import kotlin.test.assertEquals

class AppScreenTransitionsTest {
    @Test
    fun tabsDetailsAndPublicPagesShareTheSameSdkPresetInBothDirections() {
        val pages = appTopLevelRoutes + listOf(
            AppRoute.Theme, AppRoute.Statistics, AppRoute.Grades, AppRoute.CourseDetail("course"),
            AppRoute.TransportBooking, AppRoute.TicketDetail("ticket"), AppRoute.ContactDetail("contact"),
            AppRoute.Privacy, AppRoute.Updates,
        )
        for (page in pages) {
            assertEquals(UniMotion.contentTransition, AppScreenTransitions.preset(AppRoute.Home, page))
            assertEquals(UniMotion.contentTransition, AppScreenTransitions.preset(page, AppRoute.Home))
        }
        assertEquals(UniMotion.contentTransition, AppScreenTransitions.preset(AppRoute.Login, AppRoute.Privacy))
    }

    @Test
    fun sessionChangesCannotAnimateAnOutgoingEntryThatHasBeenInvalidated() {
        val boundaries = listOf(
            AppRoute.Bootstrap to AppRoute.Home,
            AppRoute.Home to AppRoute.Bootstrap,
            AppRoute.Home to AppRoute.Login,
            AppRoute.Login to AppRoute.Home,
            AppRoute.Privacy to AppRoute.Login,
        )
        for ((from, to) in boundaries) {
            assertEquals(LiquidSwitcherTransition.None, AppScreenTransitions.preset(from, to))
        }
    }

    @Test
    fun sceneContentKeysAreOpaqueAndSessionPolicyUsesRouteMetadata() {
        assertEquals(LiquidSwitcherTransition.None, AppScreenTransitions.preset(scene(AppRoute.Home), scene(AppRoute.Login)))
        assertEquals(LiquidSwitcherTransition.None, AppScreenTransitions.preset(scene(AppRoute.Login), scene(AppRoute.Home)))
        assertEquals(UniMotion.contentTransition, AppScreenTransitions.preset(scene(AppRoute.Home), scene(AppRoute.Theme)))
    }

    private fun scene(route: AppRoute): Scene<NavKey> {
        val entry = NavEntry<NavKey>(
            key = route,
            metadata = mapOf(AppScreenTransitions.RouteMetadataKey to route),
        ) {}
        return object : Scene<NavKey> {
            override val key = entry.contentKey
            override val entries = listOf(entry)
            override val previousEntries = emptyList<NavEntry<NavKey>>()
            override val content: @Composable () -> Unit = {}
        }
    }
}
