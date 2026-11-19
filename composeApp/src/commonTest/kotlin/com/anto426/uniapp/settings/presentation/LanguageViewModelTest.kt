package com.anto426.uniapp.settings.presentation

import com.anto426.uniapp.data.UniAppInitialData
import com.anto426.uniapp.data.local.FakeUniLocalDataStore
import com.anto426.uniapp.data.local.LocalDataScope
import com.anto426.uniapp.data.local.UniAppDataKeys
import com.anto426.uniapp.feedback.runtime.AppToastKind
import com.anto426.uniapp.feedback.runtime.AppToastManager
import com.anto426.uniapp.testing.ResourceTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class LanguageViewModelTest : ResourceTest() {
    @Test
    fun bundledLanguagesIncludeEuropeanAndAsianLanguages() {
        assertEquals(
            listOf("it", "en", "fr", "es", "de", "pt", "nl", "pl", "zh", "ja"),
            UniAppInitialData.languages.map { it.code },
        )
    }

    @Test
    fun selectingEuropeanOrAsianLanguagePersistsCodeAndEmitsSuccessToast() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val dataStore = FakeUniLocalDataStore()
            val toastManager = AppToastManager()
            val viewModel = LanguageViewModel(
                languages = UniAppInitialData.languages,
                localDataStore = dataStore,
                toastSink = toastManager,
            )
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.isLoaded)
            assertEquals("it", viewModel.uiState.value.selectedLanguageCode)

            for (code in listOf("en", "fr", "es", "zh", "ja")) {
                viewModel.selectLanguage(code)
                advanceUntilIdle()

                assertEquals(code, viewModel.uiState.value.selectedLanguageCode)
                assertEquals(
                    code,
                    dataStore.read(LocalDataScope.Application, UniAppDataKeys.LanguageCode),
                )
            }
            val toast = toastManager.messages.first()
            assertEquals(AppToastKind.Success, toast.kind)
            assertTrue(toast.text.isNotBlank())
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun selectingUnsupportedLanguageCodeIsIgnored() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val dataStore = FakeUniLocalDataStore()
            val viewModel = LanguageViewModel(
                languages = UniAppInitialData.languages,
                localDataStore = dataStore,
            )
            advanceUntilIdle()

            viewModel.selectLanguage("xx")
            advanceUntilIdle()

            assertEquals("it", viewModel.uiState.value.selectedLanguageCode)
            assertEquals("", dataStore.read(LocalDataScope.Application, UniAppDataKeys.LanguageCode))
        } finally {
            Dispatchers.resetMain()
        }
    }
}
