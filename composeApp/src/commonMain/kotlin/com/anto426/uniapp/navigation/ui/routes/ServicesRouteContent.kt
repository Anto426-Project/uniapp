package com.anto426.uniapp.navigation.ui.routes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anto426.uniapp.data.UniAppInitialData
import com.anto426.uniapp.navigation.model.AppRoute
import com.anto426.uniapp.navigation.ui.AppRouteContext
import com.anto426.uniapp.services.presentation.ContactDetailViewModel
import com.anto426.uniapp.services.presentation.ContactsViewModel
import com.anto426.uniapp.services.presentation.ServicesViewModel
import com.anto426.uniapp.services.presentation.TaxesViewModel
import com.anto426.uniapp.session.model.AppSessionState
import com.anto426.uniapp.ui.components.state.FeatureStateContent
import com.anto426.uniapp.ui.services.ContactDetailScreen
import com.anto426.uniapp.ui.services.ContactsScreen
import com.anto426.uniapp.ui.services.ServicesScreen
import com.anto426.uniapp.ui.services.TaxesScreen
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
internal fun ServicesRouteContent(context: AppRouteContext) {
    val isProfessor =
        (context.sessionState as? AppSessionState.Authenticated)?.account?.isProfessor == true
    val servicesViewModel = viewModel(key = context.viewModelKey) {
        ServicesViewModel(
            studentServices = if (isProfessor) UniAppInitialData.professorServices else UniAppInitialData.studentServices,
            universityPortals = if (isProfessor) UniAppInitialData.professorPortals else UniAppInitialData.universityPortals,
        )
    }
    val servicesUiState by servicesViewModel.uiState.collectAsStateWithLifecycle()
    ServicesScreen(uiState = servicesUiState) { service ->
        when (service) {
            "news" -> context.navigator.navigate(AppRoute.News)
            "transport" -> context.navigator.navigate(AppRoute.Transport)
            "taxes" -> context.navigator.navigate(AppRoute.Taxes)
            "statistics" -> context.navigator.navigate(AppRoute.Grades)
            "contacts" -> context.navigator.navigate(AppRoute.Contacts)
            "student-office" -> context.uriHandler.openUri("https://www3.unimol.it/servizi/segreteria_studenti")
            "esse3" -> context.uriHandler.openUri("https://unimol.esse3.cineca.it")
            "moodle" -> context.uriHandler.openUri("https://learn.unimol.it")
            "university-web" -> context.uriHandler.openUri("https://www.unimol.it")
            "library" -> context.navigator.navigate(AppRoute.Library())
            "email" -> context.uriHandler.openUri("https://outlook.office.com/mail/")
        }
    }
}

@Composable
internal fun TaxesRouteContent(context: AppRouteContext) {
    val taxesViewModel = viewModel(key = context.viewModelKey) { TaxesViewModel(context.dataSource) }
    val taxesUiState by taxesViewModel.uiState.collectAsStateWithLifecycle()
    FeatureStateContent(
        taxesUiState.loadState,
        taxesUiState.errorMessage,
        onRetry = { taxesViewModel.refresh(force = true) },
        emptyMessage = stringResource(Res.string.ui_state_empty_taxes),
    ) {
        TaxesScreen(
            uiState = taxesUiState,
            onFilterSelected = taxesViewModel::selectFilter,
        )
    }
}

@Composable
internal fun ContactsRouteContent(context: AppRouteContext) {
    val contactsViewModel = viewModel(key = context.viewModelKey) { ContactsViewModel(context.dataSource) }
    val contactsUiState by contactsViewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(context.searchQuery, context.isSearchActive) {
        contactsViewModel.updateSearchQuery(
            context.searchQuery.takeIf { context.isSearchActive }.orEmpty(),
        )
    }
    FeatureStateContent(
        contactsUiState.loadState,
        contactsUiState.errorMessage,
        onRetry = { contactsViewModel.refresh(force = true) },
    ) {
        ContactsScreen(
            uiState = contactsUiState,
            onCategorySelected = contactsViewModel::selectCategory,
            onContactClick = { contact ->
                context.navigator.navigate(AppRoute.ContactDetail(contact.detailKey))
            },
        )
    }
}

@Composable
internal fun ContactDetailRouteContent(
    route: AppRoute.ContactDetail,
    context: AppRouteContext,
) {
    val detailViewModel = viewModel(key = context.viewModelKey) { ContactDetailViewModel(route.contactId, context.dataSource) }
    val detailUiState by detailViewModel.uiState.collectAsStateWithLifecycle()
    FeatureStateContent(
        detailUiState.loadState,
        detailUiState.errorMessage,
        onRetry = { detailViewModel.refresh(force = true) },
    ) { detailUiState.contact?.let { contact -> ContactDetailScreen(contact) } }
}
