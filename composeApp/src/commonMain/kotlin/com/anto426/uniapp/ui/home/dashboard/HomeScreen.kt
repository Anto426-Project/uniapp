package com.anto426.uniapp.ui.home.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.anto426.uniapp.data.contract.NewsItem
import com.anto426.uniapp.home.presentation.HomeDashboardUiState
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.ui.components.layout.LocalUniScreenPadding
import com.anto426.uniapp.ui.components.layout.UniAnimatedEntrance
import com.anto426.uniapp.ui.home.dashboard.components.HomeErrorRetryCard
import com.anto426.uniapp.ui.home.dashboard.components.HomeNewsSection
import com.anto426.uniapp.ui.home.dashboard.components.HomeQuickAccessGroup
import com.anto426.uniapp.ui.home.dashboard.components.HomeSpotlightRow
import com.anto426.uniapp.ui.home.dashboard.components.HomeSummarySection

@Composable
fun HomeScreen(
    uiState: HomeDashboardUiState,
    onOpenStatistics: () -> Unit = {},
    onOpenTaxes: () -> Unit = {},
    onOpenExams: () -> Unit = {},
    onOpenNews: () -> Unit = {},
    onOpenBadge: () -> Unit = {},
    onShowNews: (NewsItem) -> Unit,
    onNextNews: () -> Unit = {},
    onPreviousNews: () -> Unit = {},
    onSelectNews: (Int) -> Unit = {},
    onRefresh: () -> Unit = {},
    onToggleCustomization: () -> Unit,
    onFinishCustomization: () -> Unit,
    onToggleQuickAction: (String) -> Unit,
    onQuickActionClick: (String) -> Unit,
) {
    val screenPadding = LocalUniScreenPadding.current
    val horizontalInset = 16.dp

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(clip = false),
        contentPadding = PaddingValues(
            top = screenPadding.calculateTopPadding() + 4.dp,
            bottom = screenPadding.calculateBottomPadding() + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        if (uiState.loadState == FeatureLoadState.Error && !uiState.errorMessage.isNullOrBlank()) {
            item(key = "home-error", contentType = "home-error") {
                HomeErrorRetryCard(
                    message = uiState.errorMessage,
                    onRetry = onRefresh,
                    modifier = Modifier.padding(horizontal = horizontalInset),
                )
            }
        }

        // 1. Riepilogo stile Apple Fitness / Salute (Header identità + unica card sintetica)
        item(key = "home-summary", contentType = "home-summary") {
            UniAnimatedEntrance(order = 0) {
                HomeSummarySection(
                    uiState = uiState,
                    onOpenBadge = onOpenBadge,
                    onOpenStatistics = onOpenStatistics,
                    modifier = Modifier.padding(horizontal = horizontalInset),
                )
            }
        }

        // 2. Stato immediato stile Apple Casa (Appelli e Tasse / Tesi)
        item(key = "home-spotlight", contentType = "home-spotlight") {
            UniAnimatedEntrance(order = 1) {
                HomeSpotlightRow(
                    uiState = uiState,
                    onOpenExams = onOpenExams,
                    onOpenTaxes = onOpenTaxes,
                    onOpenTheses = { onQuickActionClick("tesi") },
                    modifier = Modifier.padding(horizontal = horizontalInset),
                )
            }
        }

        // 3. Apple Control Center / Shortcuts 2-Column Bento Quick Access
        item(key = "home-quick-access", contentType = "home-quick-access") {
            UniAnimatedEntrance(order = 2) {
                HomeQuickAccessGroup(
                    uiState = uiState,
                    onToggleCustomization = onToggleCustomization,
                    onFinishCustomization = onFinishCustomization,
                    onToggleQuickAction = onToggleQuickAction,
                    onQuickActionClick = onQuickActionClick,
                )
            }
        }

        // 4. Apple News / App Store Edge-to-Edge Peek Pager
        item(key = "home-news", contentType = "home-news") {
            UniAnimatedEntrance(order = 3) {
                HomeNewsSection(
                    homeNews = uiState.news,
                    activeNewsIndex = uiState.activeNewsIndex,
                    onOpenNews = onOpenNews,
                    onShowNews = onShowNews,
                    onSelectNews = onSelectNews,
                )
            }
        }
    }
}
