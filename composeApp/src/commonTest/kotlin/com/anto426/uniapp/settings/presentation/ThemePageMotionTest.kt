package com.anto426.uniapp.settings.presentation

import com.anto426.liquidmonet.components.layout.animatedswitcher.LiquidSwitcherTransition
import com.anto426.liquidmonet.glass.runtime.LiquidGlassQualityTier
import com.anto426.uniapp.data.local.FakeUniLocalDataStore
import com.anto426.uniapp.data.local.LocalDataScope
import com.anto426.uniapp.data.local.UniAppDataKeys
import com.anto426.uniapp.settings.model.PageMotionPreferences
import com.anto426.uniapp.ui.motion.UniMotion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ThemePageMotionTest : com.anto426.uniapp.testing.ResourceTest() {
    @Test
    fun pickerCoversEveryAnimatedSdkPresetAndDefaultsToHorizontalFade() {
        val animated = LiquidSwitcherTransition.entries.filter { it != LiquidSwitcherTransition.None }
        assertEquals(animated.toSet(), UniMotion.options.map { it.transition }.toSet())
        assertEquals(animated.size, UniMotion.options.size)
        assertEquals(LiquidSwitcherTransition.DirectionalHorizontal, UniMotion.effectiveTransition(PageMotionPreferences()))
        for (transition in animated) {
            val preference = PageMotionPreferences(true, transition.name)
            assertEquals(preference, Json.decodeFromString(
                PageMotionPreferences.serializer(), Json.encodeToString(PageMotionPreferences.serializer(), preference),
            ))
            assertEquals(transition, UniMotion.effectiveTransition(preference))
        }
    }

    @Test
    fun legacyDisabledFlagCannotSilentlyDisableTheSelectedStyle() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val store = FakeUniLocalDataStore()
            for (transition in LiquidSwitcherTransition.entries.filter { it != LiquidSwitcherTransition.None }) {
                store.write(LocalDataScope.Application, UniAppDataKeys.ThemePageMotion, PageMotionPreferences(false, transition.name))
                val restarted = ThemeViewModel(store)
                advanceUntilIdle()
                assertTrue(restarted.uiState.value.pageMotion.enabled)
                assertEquals(transition, UniMotion.selectedTransition(restarted.uiState.value.pageMotion))
                assertEquals(transition, UniMotion.effectiveTransition(restarted.uiState.value.pageMotion))
                restarted.selectPageTransition(transition)
                advanceUntilIdle()
                assertEquals(transition, UniMotion.effectiveTransition(restarted.uiState.value.pageMotion))
                assertEquals(restarted.uiState.value.pageMotion, store.read(LocalDataScope.Application, UniAppDataKeys.ThemePageMotion))
            }
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun legacyNoneFallsBackWhileGlobalReducedMotionSurvivesRestart() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val store = FakeUniLocalDataStore()
            store.write(LocalDataScope.Application, UniAppDataKeys.ThemePageMotion, PageMotionPreferences(true, "None"))
            store.write(LocalDataScope.Application, UniAppDataKeys.ThemeReducedMotion, true)
            val viewModel = ThemeViewModel(store)
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value.pageMotion.enabled)
            assertTrue(viewModel.uiState.value.reducedMotion)
            assertEquals(UniMotion.contentTransition, UniMotion.selectedTransition(viewModel.uiState.value.pageMotion))
            viewModel.selectPageTransition(UniMotion.contentTransition)
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value.pageMotion.enabled)
            assertEquals(UniMotion.contentTransition, UniMotion.effectiveTransition(viewModel.uiState.value.pageMotion))
            viewModel.selectPageTransition(LiquidSwitcherTransition.None)
            assertTrue(viewModel.uiState.value.pageMotion.enabled)
            assertTrue(viewModel.uiState.value.reducedMotion)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun unknownStoredPresetFallsBackAndResetPersistsDefaults() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val store = FakeUniLocalDataStore()
            store.write(LocalDataScope.Application, UniAppDataKeys.ThemePageMotion, PageMotionPreferences(true, "unknown-preset"))
            val viewModel = ThemeViewModel(store)
            advanceUntilIdle()
            assertEquals(UniMotion.contentTransition, UniMotion.effectiveTransition(viewModel.uiState.value.pageMotion))
            viewModel.selectPageTransition(LiquidSwitcherTransition.LiquidMorph)
            viewModel.setReducedMotion(true)
            viewModel.reset()
            advanceUntilIdle()
            assertEquals(PageMotionPreferences(), viewModel.uiState.value.pageMotion)
            assertEquals(PageMotionPreferences(), store.read(LocalDataScope.Application, UniAppDataKeys.ThemePageMotion))
            assertEquals(false, store.read(LocalDataScope.Application, UniAppDataKeys.ThemeReducedMotion))
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun liquidMonetOptionsPersistAndResetToDefaults() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val store = FakeUniLocalDataStore()
            val viewModel = ThemeViewModel(store)
            advanceUntilIdle()

            viewModel.setLiquidIntensity(0.45f)
            viewModel.selectGlassQuality(LiquidGlassQualityTier.HIGH)
            viewModel.setBackgroundIntensity(0.60f)
            viewModel.setBackgroundSpeedFactor(1.6f)
            viewModel.setBackgroundVignette(false)
            advanceUntilIdle()

            assertEquals(0.45f, viewModel.uiState.value.liquidIntensity)
            assertEquals(LiquidGlassQualityTier.HIGH, viewModel.uiState.value.glassQuality)
            assertEquals(0.60f, viewModel.uiState.value.backgroundIntensity)
            assertEquals(1.6f, viewModel.uiState.value.backgroundSpeedFactor)
            assertEquals(false, viewModel.uiState.value.showBackgroundVignette)

            val reloaded = ThemeViewModel(store)
            advanceUntilIdle()
            assertEquals(0.45f, reloaded.uiState.value.liquidIntensity)
            assertEquals(LiquidGlassQualityTier.HIGH, reloaded.uiState.value.glassQuality)
            assertEquals(0.60f, reloaded.uiState.value.backgroundIntensity)
            assertEquals(1.6f, reloaded.uiState.value.backgroundSpeedFactor)
            assertEquals(false, reloaded.uiState.value.showBackgroundVignette)

            reloaded.reset()
            advanceUntilIdle()
            assertEquals(ThemeUiState.DEFAULT_LIQUID_INTENSITY, reloaded.uiState.value.liquidIntensity)
            assertEquals(null, reloaded.uiState.value.glassQuality)
            assertEquals(ThemeUiState.DEFAULT_BACKGROUND_INTENSITY, reloaded.uiState.value.backgroundIntensity)
            assertEquals(ThemeUiState.DEFAULT_BACKGROUND_SPEED, reloaded.uiState.value.backgroundSpeedFactor)
            assertEquals(true, reloaded.uiState.value.showBackgroundVignette)
        } finally {
            Dispatchers.resetMain()
        }
    }
}
