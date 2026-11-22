package com.anto426.uniapp.navigation.ui.routes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.data.UniAppInitialData
import com.anto426.uniapp.home.presentation.HomeDashboardViewModel
import com.anto426.uniapp.navigation.model.AppRoute
import com.anto426.uniapp.navigation.runtime.AppNavigator
import com.anto426.uniapp.navigation.ui.AppRouteContext
import com.anto426.uniapp.navigation.ui.NewsDetailActionHost
import com.anto426.uniapp.news.presentation.NewsViewModel
import com.anto426.uniapp.presentation.FeatureLoadState
import com.anto426.uniapp.session.model.AppSessionState
import com.anto426.uniapp.ui.components.state.FeatureStateContent
import com.anto426.uniapp.ui.home.dashboard.HomeScreen
import com.anto426.uniapp.ui.news.NewsDetailScreen
import com.anto426.uniapp.ui.news.NewsScreen
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
internal fun HomeRouteContent(context: AppRouteContext) {
    val account = (context.sessionState as? AppSessionState.Authenticated)?.account
    val homeViewModel = viewModel(key = context.viewModelKey) {
        HomeDashboardViewModel(
            dataSource = context.dataSource,
            quickActions =
                if (account?.isProfessor == true) UniAppInitialData.professorQuickActions
                else UniAppInitialData.allQuickActions,
            account = account,
        )
    }
    val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        uiState = homeUiState,
        onOpenStatistics = { context.navigator.navigate(AppRoute.Grades) },
        onOpenTaxes = { context.navigator.navigate(AppRoute.Taxes) },
        onOpenExams = { context.navigator.navigate(AppRoute.Exams()) },
        onOpenNews = { context.navigator.navigate(AppRoute.News) },
        onOpenBadge = { context.navigator.navigate(AppRoute.Badge) },
        onShowNews = { news ->
            context.navigator.navigate(
                AppRoute.NewsDetail(
                    newsKey = news.key,
                    accountId = context.accountId,
                    profileId = context.activeProfileId,
                ),
            )
        },
        onNextNews = homeViewModel::showNextNews,
        onPreviousNews = homeViewModel::showPreviousNews,
        onSelectNews = homeViewModel::selectNews,
        onRefresh = { homeViewModel.refresh(force = true) },
        onToggleCustomization = homeViewModel::toggleCustomization,
        onFinishCustomization = homeViewModel::finishCustomization,
        onToggleQuickAction = homeViewModel::toggleQuickAction,
        onQuickActionClick = context.navigator::openHomeAction,
    )
}

@Composable
internal fun NewsRouteContent(context: AppRouteContext) {
    val newsViewModel = viewModel(key = context.viewModelKey) { NewsViewModel(context.dataSource) }
    val newsUiState by newsViewModel.uiState.collectAsStateWithLifecycle()
    FeatureStateContent(
        newsUiState.loadState,
        newsUiState.errorMessage,
        onRetry = { newsViewModel.refresh(force = true) },
        emptyTitle = stringResource(Res.string.ui_news_empty_title),
        emptyMessage = stringResource(Res.string.ui_news_empty_desc),
        emptyIcon = LiquidIcons.Notifications,
    ) {
        NewsScreen(
            uiState = newsUiState,
            searchQuery = context.searchQuery.takeIf { context.isSearchActive }.orEmpty(),
            onTabSelected = newsViewModel::selectTab,
            onNewsSelected = { news ->
                context.navigator.navigate(
                    AppRoute.NewsDetail(
                        newsKey = news.key,
                        accountId = context.accountId,
                        profileId = context.activeProfileId,
                    ),
                )
            },
        )
    }
}

@Composable
internal fun NewsDetailRouteContent(
    route: AppRoute.NewsDetail,
    context: AppRouteContext,
    newsDetailActionHost: NewsDetailActionHost?,
) {
    val newsViewModel = viewModel(key = context.viewModelKey) { NewsViewModel(context.dataSource) }
    val newsUiState by newsViewModel.uiState.collectAsStateWithLifecycle()
    val news = newsUiState.news.firstOrNull { it.key == route.newsKey }
    if (news != null) {
        NewsDetailScreen(
            title = news.title,
            description = news.description,
            fullContent = news.fullContent,
            category = news.category,
            publishedAt = news.publishedAt,
            sourceUrl = news.sourceUrl,
            type = news.type,
            onOpenSource = { url -> context.uriHandler.openUri(url) },
            onBack = { context.navigator.goBack() },
            newsDetailActionHost = newsDetailActionHost,
        )
    } else {
        FeatureStateContent(
            state = if (newsUiState.loadState == FeatureLoadState.Content) FeatureLoadState.Empty else newsUiState.loadState,
            errorMessage = newsUiState.errorMessage,
            onRetry = { newsViewModel.refresh(force = true) },
            emptyTitle = stringResource(Res.string.ui_news_empty_title),
            emptyMessage = stringResource(Res.string.ui_news_empty_desc),
            emptyIcon = LiquidIcons.Notifications,
        ) {}
    }
}

private fun AppNavigator.openHomeAction(actionId: String) {
    when (actionId) {
        "libretto" -> navigate(AppRoute.Transcripts())
        "statistiche" -> navigate(AppRoute.Grades)
        "media" -> navigate(AppRoute.Grades)
        "appelli" -> navigate(AppRoute.Exams())
        "insegnamenti" -> navigate(AppRoute.Teachings)
        "tesi" -> navigate(AppRoute.Theses)
        "verbali" -> navigate(AppRoute.Reports)
        "didattica", "condivisione" -> selectTopLevel(AppRoute.Didactics)
        "trasporti" -> navigate(AppRoute.Transport)
        "biblioteca", "library" -> navigate(AppRoute.Library())
        "tasse" -> navigate(AppRoute.Taxes)
        "rubrica" -> navigate(AppRoute.Contacts)
        "notifiche" -> navigate(AppRoute.News)
        "sicurezza" -> navigate(AppRoute.Devices)
        "impostazioni" -> selectTopLevel(AppRoute.Settings)
        else -> selectTopLevel(AppRoute.Services)
    }
}
