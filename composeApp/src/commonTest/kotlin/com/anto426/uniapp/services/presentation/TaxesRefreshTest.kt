package com.anto426.uniapp.services.presentation

import com.anto426.uniapp.data.FakeUniAppDataSource
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.unisdk.backend.model.TaxesData
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class TaxesRefreshTest : com.anto426.uniapp.testing.ResourceTest() {
    @Test
    fun openingUsesCachePolicyAndFailedRefreshKeepsTheLoadedScreen() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val finishRefresh = CompletableDeferred<Unit>()
            val flags = mutableListOf<Boolean>()
            val source = object : FakeUniAppDataSource() {
                override suspend fun loadTaxes(forceRefresh: Boolean): TaxesData {
                    flags += forceRefresh
                    if (forceRefresh) {
                        finishRefresh.await()
                        error("Network unavailable")
                    }
                    return TaxesData("0", emptyList())
                }
            }
            val viewModel = TaxesViewModel(source)
            advanceUntilIdle()
            assertEquals(listOf(false), flags)
            assertEquals(FeatureLoadState.Empty, viewModel.uiState.value.loadState)

            viewModel.refresh(force = true)
            runCurrent()
            assertEquals(FeatureLoadState.Empty, viewModel.uiState.value.loadState)
            finishRefresh.complete(Unit)
            advanceUntilIdle()
            assertEquals(listOf(false, true), flags)
            assertEquals(FeatureLoadState.Empty, viewModel.uiState.value.loadState)
            viewModel.uiState.first { it.errorMessage != null }
            assertEquals("Network unavailable", viewModel.uiState.value.errorMessage)
        } finally {
            Dispatchers.resetMain()
        }
    }
}
