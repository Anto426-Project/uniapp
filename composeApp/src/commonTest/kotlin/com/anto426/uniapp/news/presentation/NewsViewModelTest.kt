package com.anto426.uniapp.news.presentation

import com.anto426.uniapp.data.FakeUniAppDataSource
import com.anto426.uniapp.data.toNewsItems
import com.anto426.uniapp.data.runtime.UniAppDataCoordinator
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.unisdk.backend.model.UniversityNews
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class NewsViewModelTest : com.anto426.uniapp.testing.ResourceTest() {
    @Test
    fun filtersMatchPopulatedPortalScopesAndDoNotInventEvents() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val items = listOf(
                news("a", "Ateneo", "2026-09-23"),
                news("b", "Dipartimento", "24/09/2026"),
                news("c", "Corso di studi", "2026-09-25"),
            )
            val source = object : FakeUniAppDataSource() {
                override suspend fun loadUniversityNews(forceRefresh: Boolean) = items
            }
            val viewModel = NewsViewModel(source, preparationDispatcher = Dispatchers.Main)
            advanceUntilIdle()

            assertEquals(
                listOf(NewsFilter.All, NewsFilter.University, NewsFilter.Department, NewsFilter.Course),
                viewModel.uiState.value.filters,
            )
            assertEquals(listOf("c", "b", "a"), viewModel.uiState.value.visibleNews.map { it.title })
            viewModel.selectTab(2)
            assertEquals(listOf("b"), viewModel.uiState.value.visibleNews.map { it.title })
            viewModel.selectTab(3)
            assertEquals(listOf("c"), viewModel.uiState.value.visibleNews.map { it.title })
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun filtersWithNoRealNewsAreHidden() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val source = object : FakeUniAppDataSource() {
                override suspend fun loadUniversityNews(forceRefresh: Boolean) = listOf(news("a", "Ateneo", "2026-09-25"))
            }
            val viewModel = NewsViewModel(source, preparationDispatcher = Dispatchers.Main)
            advanceUntilIdle()
            assertEquals(listOf(NewsFilter.All, NewsFilter.University), viewModel.uiState.value.filters)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun mapperKeepsRealCategoryAndDateAcrossDateFormats() {
        val items = listOf(news("old", "Ateneo", "23/09/2026"), news("new", "Dipartimento", "2026-09-25"))
            .toNewsItems()
        assertEquals(listOf("new", "old"), items.map { it.title })
        assertEquals(listOf("Dipartimento", "Ateneo"), items.map { it.category })
        assertEquals("2026-09-25", items.first().publishedAt)
    }

    @Test
    fun openingUsesPreparedAccountFeedImmediatelyAndRefreshPreservesTheSelectedScope() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        var input = listOf(news("a", "Ateneo", "2026-09-25"), news("b", "Dipartimento", "2026-09-24"))
        val source = object : FakeUniAppDataSource() {
            override suspend fun loadUniversityNews(forceRefresh: Boolean) = input
        }
        val data = UniAppDataCoordinator(source, backgroundScope)
        try {
            val prepared = data.prepareNews(input, StandardTestDispatcher(testScheduler))
            val viewModel = NewsViewModel(data, preparationDispatcher = Dispatchers.Main)
            // No coroutine or loading frame is needed to expose the feed already used by Home.
            assertEquals(FeatureLoadState.Content, viewModel.uiState.value.loadState)
            assertSame(prepared.items, viewModel.uiState.value.visibleNews)
            advanceUntilIdle()
            viewModel.selectTab(2)
            val department = prepared.byCategory.getValue("dipartimento")
            assertSame(department, viewModel.uiState.value.visibleNews)
            viewModel.showNews(department.single())
            viewModel.dismissNews()
            assertSame(department, viewModel.uiState.value.visibleNews)
            input = input + news("c", "Ateneo", "2026-09-26")
            viewModel.refresh(force = true)
            runCurrent()
            advanceUntilIdle()
            assertEquals(NewsFilter.Department, viewModel.uiState.value.filters[viewModel.uiState.value.selectedTab])
            input = input.filter { it.category != "Dipartimento" }
            viewModel.refresh(force = true)
            runCurrent()
            advanceUntilIdle()
            assertEquals(0, viewModel.uiState.value.selectedTab)
            assertEquals(listOf("c", "a"), viewModel.uiState.value.visibleNews.map { it.title })
        } finally {
            data.close()
            Dispatchers.resetMain()
        }
    }

    private fun news(title: String, category: String, date: String) = UniversityNews(
        id = title,
        title = title,
        summary = "Riassunto $title",
        content = "Contenuto $title",
        publishedAt = date,
        category = category,
        sourceUrl = null,
    )
}
