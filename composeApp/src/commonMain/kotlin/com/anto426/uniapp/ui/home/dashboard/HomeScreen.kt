package com.anto426.uniapp.ui.home.dashboard

import androidx.compose.runtime.Composable
import com.anto426.uniapp.home.presentation.HomeDashboardUiState
import com.anto426.uniapp.model.news.NewsItem
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.components.layout.UniAnimatedSection
import com.anto426.uniapp.ui.home.dashboard.components.HomeNewsSection
import com.anto426.uniapp.ui.home.dashboard.components.HomeQuickAccessSection
import com.anto426.uniapp.ui.home.dashboard.components.HomeQuickIndicatorsRow
import com.anto426.uniapp.ui.home.dashboard.components.HomeAcademicProfileHeroCard

@Composable
fun HomeScreen(
    uiState: HomeDashboardUiState,
    onOpenStatistics: () -> Unit = {},
    onOpenTaxes: () -> Unit = {},
    onOpenExams: () -> Unit = {},
    onOpenNews: () -> Unit = {},
    onOpenBadge: () -> Unit = {},
    onShowNews: (NewsItem) -> Unit,
    onNextNews: () -> Unit,
    onPreviousNews: () -> Unit,
    onToggleCustomization: () -> Unit,
    onFinishCustomization: () -> Unit,
    onToggleQuickAction: (String) -> Unit,
    onQuickActionClick: (String) -> Unit,
) {
    UniScreenColumn {
        // 1. Scheda Unificata Profilo Studente & Carriera Accademica
        UniAnimatedSection {
            HomeAcademicProfileHeroCard(
                uiState = uiState,
                onOpenBadge = onOpenBadge,
                onOpenStatistics = onOpenStatistics,
            )
        }

        // 2. Indicatori Rapidi - Appelli e Tasse
        UniAnimatedSection(order = 1) {
            HomeQuickIndicatorsRow(
                uiState = uiState,
                onOpenExams = onOpenExams,
                onOpenTaxes = onOpenTaxes,
                onOpenTheses = { onQuickActionClick("tesi") },
            )
        }

        // 3. Notizie Ateneo / Dipartimento
        UniAnimatedSection(order = 2) {
            HomeNewsSection(
                homeNews = uiState.news,
                activeNewsIndex = uiState.activeNewsIndex,
                onOpenNews = onOpenNews,
                onShowNews = onShowNews,
                onNextNews = onNextNews,
                onPreviousNews = onPreviousNews,
            )
        }

        // 4. Accesso Rapido Personalizzabile
        UniAnimatedSection(order = 3) {
            HomeQuickAccessSection(
                uiState = uiState,
                onToggleCustomization = onToggleCustomization,
                onFinishCustomization = onFinishCustomization,
                onToggleQuickAction = onToggleQuickAction,
                onQuickActionClick = onQuickActionClick,
            )
        }
    }
}
