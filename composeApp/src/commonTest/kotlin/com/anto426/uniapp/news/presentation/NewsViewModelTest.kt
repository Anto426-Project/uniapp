package com.anto426.uniapp.news.presentation

import com.anto426.uniapp.data.FakeUniAppDataSource
import com.anto426.uniapp.data.toNewsItems
import com.anto426.unisdk.backend.model.UniversityNews
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.Test
import kotlin.test.assertEquals

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
            val viewModel = NewsViewModel(source)
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
            val viewModel = NewsViewModel(source)
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
