package com.anto426.uniapp.navigation.ui

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import com.anto426.uniapp.navigation.model.AppRoute
import com.anto426.uniapp.navigation.model.appTopLevelRoutes

/**
 * Screen transition policy refined for smooth, fluid motion without stutter.
 *
 * Uses:
 * - Fluid decelerated cubic-bezier curves (0.16, 1.0, 0.3, 1.0)
 * - Parallax partial offsets to prevent high-velocity snapping
 * - Balanced duration (380-420ms) for elegant and responsive page flow
 * - SizeTransform with clip=false to prevent bounding-box pop
 */
internal object AppScreenTransitions {

    private const val TAB_DURATION_MS = 380
    private const val SCREEN_DURATION_MS = 400
    private const val ONBOARDING_DURATION_MS = 500

    /** Smooth natural deceleration curve */
    private val SmoothEasing = CubicBezierEasing(0.16f, 1.0f, 0.3f, 1.0f)

    private enum class NavDirection {
        Start,
        End,
    }

    fun forward(
        from: AppRoute? = null,
        to: AppRoute? = null,
    ): ContentTransform {
        // 1. Onboarding / Auth transition
        if (from == AppRoute.Bootstrap || from == AppRoute.Login) {
            val enter = slideInHorizontally(
                initialOffsetX = { (it * 0.35f).toInt() },
                animationSpec = tween(ONBOARDING_DURATION_MS, easing = SmoothEasing),
            ) + fadeIn(animationSpec = tween(ONBOARDING_DURATION_MS, easing = SmoothEasing))

            val exit = slideOutHorizontally(
                targetOffsetX = { -(it * 0.35f).toInt() },
                animationSpec = tween(ONBOARDING_DURATION_MS, easing = SmoothEasing),
            ) + fadeOut(animationSpec = tween(ONBOARDING_DURATION_MS, easing = SmoothEasing))

            return enter togetherWith exit
        }

        // 2. Tab-to-Tab directional slide transition
        val tabDirection = resolveDirection(from, to)
        if (tabDirection != null) {
            return slideInFrom(tabDirection) togetherWith slideOutTo(tabDirection)
        }

        // 3. Standard Forward Navigation (Push)
        val enter = slideInHorizontally(
            initialOffsetX = { (it * 0.18f).toInt() },
            animationSpec = tween(SCREEN_DURATION_MS, easing = SmoothEasing),
        ) + scaleIn(
            initialScale = 0.95f,
            animationSpec = tween(SCREEN_DURATION_MS, easing = SmoothEasing),
        ) + fadeIn(animationSpec = tween(SCREEN_DURATION_MS, easing = SmoothEasing))

        val exit = slideOutHorizontally(
            targetOffsetX = { -(it * 0.12f).toInt() },
            animationSpec = tween(SCREEN_DURATION_MS, easing = SmoothEasing),
        ) + scaleOut(
            targetScale = 0.97f,
            animationSpec = tween(SCREEN_DURATION_MS, easing = SmoothEasing),
        ) + fadeOut(animationSpec = tween(SCREEN_DURATION_MS - 50, easing = SmoothEasing))

        return enter togetherWith exit
    }

    fun backward(
        from: AppRoute? = null,
        to: AppRoute? = null,
    ): ContentTransform {
        // 1. Tab-to-Tab directional slide transition
        val tabDirection = resolveDirection(from, to)
        if (tabDirection != null) {
            return slideInFrom(tabDirection) togetherWith slideOutTo(tabDirection)
        }

        // 2. Standard Backward Navigation (Pop)
        val enter = slideInHorizontally(
            initialOffsetX = { -(it * 0.12f).toInt() },
            animationSpec = tween(SCREEN_DURATION_MS, easing = SmoothEasing),
        ) + scaleIn(
            initialScale = 0.97f,
            animationSpec = tween(SCREEN_DURATION_MS, easing = SmoothEasing),
        ) + fadeIn(animationSpec = tween(SCREEN_DURATION_MS, easing = SmoothEasing))

        val exit = slideOutHorizontally(
            targetOffsetX = { (it * 0.18f).toInt() },
            animationSpec = tween(SCREEN_DURATION_MS, easing = SmoothEasing),
        ) + scaleOut(
            targetScale = 0.95f,
            animationSpec = tween(SCREEN_DURATION_MS, easing = SmoothEasing),
        ) + fadeOut(animationSpec = tween(SCREEN_DURATION_MS - 50, easing = SmoothEasing))

        return enter togetherWith exit
    }

    fun predictiveBack(): ContentTransform = backward()

    private fun resolveDirection(from: AppRoute?, to: AppRoute?): NavDirection? {
        if (from == null || to == null) return null
        val fromIndex = appTopLevelRoutes.indexOf(from)
        val toIndex = appTopLevelRoutes.indexOf(to)

        if (fromIndex == -1 || toIndex == -1 || fromIndex == toIndex) return null

        return if (toIndex > fromIndex) {
            NavDirection.End
        } else {
            NavDirection.Start
        }
    }

    private fun slideInFrom(direction: NavDirection): EnterTransition {
        val sign = when (direction) {
            NavDirection.Start -> -1
            NavDirection.End -> 1
        }
        return slideInHorizontally(
            initialOffsetX = { (it * 0.25f).toInt() * sign },
            animationSpec = tween(TAB_DURATION_MS, easing = SmoothEasing),
        ) + fadeIn(animationSpec = tween(TAB_DURATION_MS, easing = SmoothEasing))
    }

    private fun slideOutTo(direction: NavDirection): ExitTransition {
        val sign = when (direction) {
            NavDirection.Start -> -1
            NavDirection.End -> 1
        }
        return slideOutHorizontally(
            targetOffsetX = { -(it * 0.25f).toInt() * sign },
            animationSpec = tween(TAB_DURATION_MS, easing = SmoothEasing),
        ) + fadeOut(animationSpec = tween(TAB_DURATION_MS - 50, easing = SmoothEasing))
    }
}
