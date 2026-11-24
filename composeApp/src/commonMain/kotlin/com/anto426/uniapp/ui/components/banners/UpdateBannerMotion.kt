package com.anto426.uniapp.ui.components.banners

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import com.anto426.uniapp.model.updates.UpdateState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Dedicated ad-hoc motion system for [UniAppUpdateBanner], completely decoupled from
 * the Liquid Monet motion engine.
 *
 * Curves, timings, spinner trim-path choreography, dual-line indeterminate sweeps, and
 * staggered rising light waves are derived from OxygenOS / COUI OTA updater motion specs.
 */
internal object UpdateBannerMotionTokens {
    /** Signature OxygenOS movement curve (fast launch, long silky deceleration). */
    val MoveEase: Easing = CubicBezierEasing(0.30f, 0.0f, 0.10f, 1.0f)

    /** Swift decelerating panel/halo curve. */
    val OutEase: Easing = CubicBezierEasing(0.0f, 0.0f, 0.10f, 1.0f)

    /** Symmetric OxygenOS opacity curve (coui_curve_opacity_inout). */
    val OpacityEase: Easing = CubicBezierEasing(0.33f, 0.0f, 0.67f, 1.0f)

    /** Crisp accelerate-out exit curve. */
    val InEase: Easing = CubicBezierEasing(0.30f, 0.0f, 1.0f, 1.0f)

    /** OxygenOS OTA "new_version" rising wave curve (54 frames @ 60fps = 900ms). */
    val NewVersionRiseEase: Easing = CubicBezierEasing(0.001f, 0.0f, 0.05f, 1.0f)

    const val StatusEnterMs = 280
    const val StatusExitMs = 150
    const val FooterEnterMs = 300
    const val FooterEnterDelayMs = 45
    const val FooterExitMs = 170
    const val FooterSizeMorphMs = 320
    const val VersionEnterMs = 440
    const val VersionExitMs = 190
    const val NewVersionWaveMs = 900
    const val NewVersionStaggerMs = 83
}

private fun UpdateState.motionStage(): Int = when (this) {
    UpdateState.ERROR -> -1
    UpdateState.UP_TO_DATE -> 0
    UpdateState.CHECKING -> 1
    UpdateState.AVAILABLE -> 2
    UpdateState.DOWNLOADING -> 3
    UpdateState.VERIFYING -> 4
    UpdateState.INSTALLING -> 5
    UpdateState.RESTART_REQUIRED -> 6
}

/**
 * Directional top-status switcher for [UniAppUpdateBanner].
 */
@Composable
internal fun UpdateBannerStatusSwitcher(
    targetState: UpdateState,
    modifier: Modifier = Modifier,
    content: @Composable (UpdateState) -> Unit,
) {
    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        contentAlignment = Alignment.Center,
        transitionSpec = {
            val forward = targetState.motionStage() >= initialState.motionStage()
            val enter = fadeIn(
                animationSpec = tween(
                    durationMillis = UpdateBannerMotionTokens.StatusEnterMs,
                    delayMillis = 30,
                    easing = UpdateBannerMotionTokens.OpacityEase,
                ),
            ) + slideInVertically(
                animationSpec = tween(
                    durationMillis = UpdateBannerMotionTokens.StatusEnterMs,
                    delayMillis = 30,
                    easing = UpdateBannerMotionTokens.MoveEase,
                ),
                initialOffsetY = { fullHeight ->
                    val delta = (fullHeight * 0.45f).toInt().coerceAtLeast(10)
                    if (forward) delta else -delta
                },
            )
            val exit = fadeOut(
                animationSpec = tween(
                    durationMillis = UpdateBannerMotionTokens.StatusExitMs,
                    easing = UpdateBannerMotionTokens.InEase,
                ),
            ) + slideOutVertically(
                animationSpec = tween(
                    durationMillis = UpdateBannerMotionTokens.StatusExitMs,
                    easing = UpdateBannerMotionTokens.InEase,
                ),
                targetOffsetY = { fullHeight ->
                    val delta = (fullHeight * 0.35f).toInt().coerceAtLeast(8)
                    if (forward) -delta else delta
                },
            )
            ContentTransform(
                targetContentEnter = enter,
                initialContentExit = exit,
                sizeTransform = SizeTransform(clip = false) { _, _ ->
                    tween(
                        durationMillis = UpdateBannerMotionTokens.StatusEnterMs,
                        easing = UpdateBannerMotionTokens.MoveEase,
                    )
                },
            )
        },
        label = "UpdateBannerStatusSwitcher",
        content = { state -> content(state) },
    )
}

/**
 * Hero version number reveal switcher inspired by OxygenOS OTA shared-element version transitions.
 */
@Composable
internal fun UpdateBannerVersionSwitcher(
    version: String,
    modifier: Modifier = Modifier,
    content: @Composable (String) -> Unit,
) {
    AnimatedContent(
        targetState = version,
        modifier = modifier,
        contentAlignment = Alignment.Center,
        transitionSpec = {
            val enter = fadeIn(
                animationSpec = tween(
                    durationMillis = 260,
                    delayMillis = 40,
                    easing = UpdateBannerMotionTokens.OpacityEase,
                ),
            ) + slideInVertically(
                animationSpec = tween(
                    durationMillis = UpdateBannerMotionTokens.VersionEnterMs,
                    easing = UpdateBannerMotionTokens.MoveEase,
                ),
                initialOffsetY = { (it * 0.22f).toInt() },
            ) + scaleIn(
                animationSpec = tween(
                    durationMillis = UpdateBannerMotionTokens.VersionEnterMs,
                    easing = UpdateBannerMotionTokens.MoveEase,
                ),
                initialScale = 0.93f,
            )
            val exit = fadeOut(
                animationSpec = tween(
                    durationMillis = UpdateBannerMotionTokens.VersionExitMs,
                    easing = UpdateBannerMotionTokens.InEase,
                ),
            ) + slideOutVertically(
                animationSpec = tween(
                    durationMillis = UpdateBannerMotionTokens.VersionExitMs,
                    easing = UpdateBannerMotionTokens.InEase,
                ),
                targetOffsetY = { -(it * 0.14f).toInt() },
            ) + scaleOut(
                animationSpec = tween(
                    durationMillis = UpdateBannerMotionTokens.VersionExitMs,
                    easing = UpdateBannerMotionTokens.InEase,
                ),
                targetScale = 0.96f,
            )
            (enter togetherWith exit).using(
                SizeTransform(clip = false) { _, _ ->
                    tween(
                        durationMillis = UpdateBannerMotionTokens.VersionEnterMs,
                        easing = UpdateBannerMotionTokens.MoveEase,
                    )
                },
            )
        },
        label = "UpdateBannerVersionSwitcher",
        content = { value -> content(value) },
    )
}

/**
 * Directional footer switcher for action buttons, checking status, and progress indicators.
 */
@Composable
internal fun UpdateBannerFooterSwitcher(
    targetState: UpdateState,
    modifier: Modifier = Modifier,
    content: @Composable (UpdateState) -> Unit,
) {
    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        contentAlignment = Alignment.Center,
        transitionSpec = {
            val forward = targetState.motionStage() >= initialState.motionStage()
            val enter = fadeIn(
                animationSpec = tween(
                    durationMillis = 250,
                    delayMillis = UpdateBannerMotionTokens.FooterEnterDelayMs,
                    easing = UpdateBannerMotionTokens.OpacityEase,
                ),
            ) + slideInVertically(
                animationSpec = tween(
                    durationMillis = UpdateBannerMotionTokens.FooterEnterMs,
                    delayMillis = UpdateBannerMotionTokens.FooterEnterDelayMs,
                    easing = UpdateBannerMotionTokens.MoveEase,
                ),
                initialOffsetY = { fullHeight ->
                    val delta = (fullHeight * 0.28f).toInt().coerceIn(14, 34)
                    if (forward) delta else -delta
                },
            ) + scaleIn(
                animationSpec = tween(
                    durationMillis = UpdateBannerMotionTokens.FooterEnterMs,
                    delayMillis = UpdateBannerMotionTokens.FooterEnterDelayMs,
                    easing = UpdateBannerMotionTokens.MoveEase,
                ),
                initialScale = 0.96f,
            )
            val exit = fadeOut(
                animationSpec = tween(
                    durationMillis = UpdateBannerMotionTokens.FooterExitMs,
                    easing = UpdateBannerMotionTokens.InEase,
                ),
            ) + slideOutVertically(
                animationSpec = tween(
                    durationMillis = UpdateBannerMotionTokens.FooterExitMs,
                    easing = UpdateBannerMotionTokens.InEase,
                ),
                targetOffsetY = { fullHeight ->
                    val delta = (fullHeight * 0.18f).toInt().coerceIn(8, 22)
                    if (forward) -delta else delta
                },
            )
            ContentTransform(
                targetContentEnter = enter,
                initialContentExit = exit,
                sizeTransform = SizeTransform(clip = false) { _, _ ->
                    tween(
                        durationMillis = UpdateBannerMotionTokens.FooterSizeMorphMs,
                        easing = UpdateBannerMotionTokens.MoveEase,
                    )
                },
            )
        },
        label = "UpdateBannerFooterSwitcher",
        content = { state -> content(state) },
    )
}

/**
 * Animated status indicator dot with smooth color transitions and an optional
 * OxygenOS breathing pulse ring for live update states.
 */
@Composable
internal fun UpdateBannerStatusDot(
    dotColor: Color,
    isPulsing: Boolean,
    modifier: Modifier = Modifier,
) {
    val animatedColor by animateColorAsState(
        targetValue = dotColor,
        animationSpec = tween(
            durationMillis = 260,
            easing = UpdateBannerMotionTokens.OpacityEase,
        ),
        label = "UpdateBannerStatusDotColor",
    )

    val pulseProgress = if (isPulsing) {
        val pulseTransition = rememberInfiniteTransition(label = "UpdateBannerDotPulse")
        val value by pulseTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 1800,
                    easing = UpdateBannerMotionTokens.OutEase,
                ),
                repeatMode = RepeatMode.Restart,
            ),
            label = "UpdateBannerDotPulseProgress",
        )
        value
    } else {
        0f
    }

    Canvas(modifier = modifier.size(12.dp)) {
        val coreRadius = 3.5.dp.toPx()
        if (isPulsing) {
            val haloRadius = lerpFloat(coreRadius, size.minDimension * 0.56f, pulseProgress)
            val haloAlpha = (1f - pulseProgress) * 0.45f
            drawCircle(
                color = animatedColor.copy(alpha = haloAlpha),
                radius = haloRadius,
                center = center,
            )
        }
        drawCircle(
            color = animatedColor,
            radius = coreRadius,
            center = center,
        )
    }
}

/**
 * Staggered translucent rising light waves inspired by OxygenOS OTA `raw/new_version.json`
 * (two ascending feathered light crests over 900ms with 83ms stagger) plus a subtle active breath
 * during checking, download, verification, and installation.
 */
@Composable
internal fun UpdateBannerAtmosphereOverlay(
    state: UpdateState,
    accent: Color,
    warmAccent: Color,
    modifier: Modifier = Modifier,
) {
    val isRevealState = state == UpdateState.AVAILABLE || state == UpdateState.RESTART_REQUIRED
    val isWorkingState = state == UpdateState.CHECKING ||
        state == UpdateState.DOWNLOADING ||
        state == UpdateState.VERIFYING ||
        state == UpdateState.INSTALLING

    val wave1Progress = remember { Animatable(if (isRevealState) 1f else 0f) }
    val wave2Progress = remember { Animatable(if (isRevealState) 1f else 0f) }
    val wave1Alpha = remember { Animatable(if (isRevealState) 1f else 0f) }
    val wave2Alpha = remember { Animatable(if (isRevealState) 1f else 0f) }

    LaunchedEffect(state) {
        if (isRevealState) {
            wave1Progress.snapTo(0f)
            wave2Progress.snapTo(0f)
            wave1Alpha.snapTo(0f)
            wave2Alpha.snapTo(0f)

            launch {
                wave1Progress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = UpdateBannerMotionTokens.NewVersionWaveMs,
                        easing = UpdateBannerMotionTokens.NewVersionRiseEase,
                    ),
                )
            }
            launch {
                wave1Alpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = UpdateBannerMotionTokens.NewVersionWaveMs,
                        easing = UpdateBannerMotionTokens.MoveEase,
                    ),
                )
            }
            launch {
                delay(UpdateBannerMotionTokens.NewVersionStaggerMs.toLong())
                launch {
                    wave2Progress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = UpdateBannerMotionTokens.NewVersionWaveMs,
                            easing = UpdateBannerMotionTokens.NewVersionRiseEase,
                        ),
                    )
                }
                launch {
                    wave2Alpha.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = UpdateBannerMotionTokens.NewVersionWaveMs,
                            easing = UpdateBannerMotionTokens.MoveEase,
                        ),
                    )
                }
            }
        } else {
            launch {
                wave1Alpha.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(
                        durationMillis = 250,
                        easing = UpdateBannerMotionTokens.OpacityEase,
                    ),
                )
            }
            launch {
                wave2Alpha.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(
                        durationMillis = 250,
                        easing = UpdateBannerMotionTokens.OpacityEase,
                    ),
                )
            }
        }
    }

    val workingAlpha by animateFloatAsState(
        targetValue = if (isWorkingState) 1f else 0f,
        animationSpec = tween(
            durationMillis = 320,
            easing = UpdateBannerMotionTokens.OpacityEase,
        ),
        label = "UpdateBannerWorkingAuraAlpha",
    )

    val workingBreath = if (isWorkingState || workingAlpha > 0.01f) {
        val infinite = rememberInfiniteTransition(label = "UpdateBannerWorkingAura")
        val value by infinite.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 2400,
                    easing = UpdateBannerMotionTokens.OpacityEase,
                ),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "UpdateBannerWorkingBreath",
        )
        value
    } else {
        0f
    }

    val waveTint = remember(state, accent, warmAccent) {
        if (state == UpdateState.AVAILABLE) warmAccent else accent
    }
    val wavePath1 = remember { Path() }
    val wavePath2 = remember { Path() }

    Canvas(modifier = modifier.fillMaxSize()) {
        if (wave1Alpha.value > 0.005f) {
            drawRisingLightCone(
                path = wavePath1,
                progress = wave1Progress.value,
                alpha = wave1Alpha.value * 0.14f,
                startApexY = size.height * 0.66f,
                endApexY = size.height * 0.24f,
                coneHeight = size.height * 0.72f,
                tint = waveTint,
            )
        }
        if (wave2Alpha.value > 0.005f) {
            drawRisingLightCone(
                path = wavePath2,
                progress = wave2Progress.value,
                alpha = wave2Alpha.value * 0.11f,
                startApexY = size.height * 0.84f,
                endApexY = size.height * 0.42f,
                coneHeight = size.height * 0.70f,
                tint = lerp(waveTint, Color.White, 0.35f),
            )
        }
        if (workingAlpha > 0.005f) {
            val centerY = lerpFloat(size.height * 0.56f, size.height * 0.36f, workingBreath)
            val glowAlpha = workingAlpha * lerpFloat(0.05f, 0.13f, workingBreath)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        accent.copy(alpha = glowAlpha),
                        accent.copy(alpha = glowAlpha * 0.35f),
                        Color.Transparent,
                    ),
                    center = Offset(size.width * 0.5f, centerY),
                    radius = size.maxDimension * 0.54f,
                ),
                center = Offset(size.width * 0.5f, centerY),
                radius = size.maxDimension * 0.54f,
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRisingLightCone(
    path: Path,
    progress: Float,
    alpha: Float,
    startApexY: Float,
    endApexY: Float,
    coneHeight: Float,
    tint: Color,
) {
    val apexY = lerpFloat(startApexY, endApexY, progress)
    val baseY = apexY + coneHeight
    val halfSpread = size.width * 0.92f

    path.reset()
    path.moveTo(size.width * 0.5f, apexY)
    path.lineTo(size.width * 0.5f + halfSpread, baseY)
    path.lineTo(size.width * 0.5f - halfSpread, baseY)
    path.close()

    drawPath(
        path = path,
        brush = Brush.verticalGradient(
            colorStops = arrayOf(
                0.0f to tint.copy(alpha = alpha),
                0.5f to tint.copy(alpha = alpha * 0.50f),
                1.0f to Color.Transparent,
            ),
            startY = apexY,
            endY = baseY,
        ),
    )
}

/**
 * Subtle OxygenOS breathing pulse wrapper for the verification lock icon.
 */
@Composable
internal fun UpdateBannerPulseIconHost(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val transition = rememberInfiniteTransition(label = "UpdateBannerVerifyIconPulse")
    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1350,
                easing = UpdateBannerMotionTokens.OpacityEase,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "UpdateBannerVerifyIconPulseValue",
    )

    Box(
        modifier = modifier.graphicsLayer {
            val scale = lerpFloat(0.92f, 1.08f, pulse)
            scaleX = scale
            scaleY = scale
            alpha = lerpFloat(0.76f, 1.0f, pulse)
        },
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

/**
 * Computes a gentle ambient glow alpha behind [VersionText] when the update banner is active.
 */
@Composable
internal fun rememberUpdateBannerVersionGlowAlpha(state: UpdateState): Float {
    val isActive = state == UpdateState.CHECKING ||
        state == UpdateState.AVAILABLE ||
        state == UpdateState.DOWNLOADING ||
        state == UpdateState.VERIFYING ||
        state == UpdateState.INSTALLING

    val targetBase by animateFloatAsState(
        targetValue = if (isActive) 0.20f else 0.16f,
        animationSpec = tween(
            durationMillis = 320,
            easing = UpdateBannerMotionTokens.OpacityEase,
        ),
        label = "UpdateBannerVersionGlowBase",
    )

    if (!isActive) return targetBase

    val infinite = rememberInfiniteTransition(label = "UpdateBannerVersionGlowBreath")
    val delta by infinite.animateFloat(
        initialValue = -0.03f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2200,
                easing = UpdateBannerMotionTokens.OpacityEase,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "UpdateBannerVersionGlowDelta",
    )
    return (targetBase + delta).coerceIn(0.10f, 0.28f)
}

private fun lerpFloat(start: Float, stop: Float, fraction: Float): Float =
    start + (stop - start) * fraction
