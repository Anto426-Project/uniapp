package com.anto426.uniapp.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.buttons.LiquidButton
import com.anto426.liquidmonet.components.buttons.LiquidButtonVariant
import com.anto426.liquidmonet.components.buttons.LiquidFloatingActionButton
import com.anto426.liquidmonet.components.feedback.LiquidDialog
import com.anto426.liquidmonet.components.navigation.LiquidNavigationBar
import com.anto426.liquidmonet.components.navigation.LiquidNavigationItem
import com.anto426.liquidmonet.components.navigation.LiquidTopBar
import com.anto426.liquidmonet.components.navigation.LiquidTopBarAction
import com.anto426.liquidmonet.glass.LiquidBackground
import com.anto426.liquidmonet.glass.LiquidBackgroundEffect
import com.anto426.liquidmonet.glass.LiquidGlassScene
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.motion.LiquidAnimatedNavContent
import com.anto426.liquidmonet.motion.LiquidNavTransition
import com.anto426.liquidmonet.theme.LiquidMonetTheme
import com.anto426.liquidmonet.theme.monet.LiquidMonetPresets
import com.anto426.liquidmonet.theme.monet.LiquidMonetSeed
import com.anto426.uniapp.android.ui.components.layout.LocalUniScreenPadding
import com.anto426.uniapp.android.ui.models.ContactData
import com.anto426.uniapp.android.ui.models.StudyCourse
import com.anto426.uniapp.android.ui.models.TransportReservation
import com.anto426.uniapp.android.ui.models.TransportTicket
import com.anto426.uniapp.android.ui.screens.AppInfoScreen
import com.anto426.uniapp.android.ui.screens.AttendanceScreen
import com.anto426.uniapp.android.ui.screens.AuthorScreen
import com.anto426.uniapp.android.ui.screens.ChangelogScreen
import com.anto426.uniapp.android.ui.screens.ColorLabScreen
import com.anto426.uniapp.android.ui.screens.ConnectedDevicesScreen
import com.anto426.uniapp.android.ui.screens.ContactDetailScreen
import com.anto426.uniapp.android.ui.screens.ContactsScreen
import com.anto426.uniapp.android.ui.screens.CookiesScreen
import com.anto426.uniapp.android.ui.screens.CourseDetailScreen
import com.anto426.uniapp.android.ui.screens.DidacticsScreen
import com.anto426.uniapp.android.ui.screens.ExamsHistoryScreen
import com.anto426.uniapp.android.ui.screens.ExamsScreen
import com.anto426.uniapp.android.ui.screens.GradesScreen
import com.anto426.uniapp.android.ui.screens.HomeScreen
import com.anto426.uniapp.android.ui.screens.LanguageScreen
import com.anto426.uniapp.android.ui.screens.NewsScreen
import com.anto426.uniapp.android.ui.screens.PrivacyScreen
import com.anto426.uniapp.android.ui.screens.QuestionnairesScreen
import com.anto426.uniapp.android.ui.screens.ReservationDetailScreen
import com.anto426.uniapp.android.ui.screens.RgbScreen
import com.anto426.uniapp.android.ui.screens.ServicesScreen
import com.anto426.uniapp.android.ui.screens.SettingsScreen
import com.anto426.uniapp.android.ui.screens.StudentIdScreen
import com.anto426.uniapp.android.ui.screens.StudyPlanScreen
import com.anto426.uniapp.android.ui.screens.TaxesScreen
import com.anto426.uniapp.android.ui.screens.TermsScreen
import com.anto426.uniapp.android.ui.screens.ThemeScreen
import com.anto426.uniapp.android.ui.screens.TicketDetailScreen
import com.anto426.uniapp.android.ui.screens.TranscriptsScreen
import com.anto426.uniapp.android.ui.screens.TransportScreen
import com.anto426.uniapp.android.ui.screens.UpdatesScreen
import com.anto426.uniapp.android.ui.screens.TransportBookingScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { UniAppRoot() }
    }
}

private enum class Destination(val title: String, val subtitle: String, val icon: ImageVector, val topLevel: Boolean = false) {
    HOME("UniApp", "Liquidnio • Ingegneria Informatica", LiquidIcons.Home, true),
    SERVICES("Servizi", "Tasse, Trasporti e Servizi Studente", LiquidIcons.Star, true),
    DIDACTICS("Didattica", "Appelli, Libretto e Valutazioni", LiquidIcons.Calendar, true),
    SETTINGS("Impostazioni", "Preferenze e Configurazione", LiquidIcons.Settings, true),
    CAREER("Carriera", "Riepilogo Esami e CFU", LiquidIcons.Calendar),
    INFO("Informazioni", "Versione e Note di Rilascio", LiquidIcons.Info),
    THEME("Tema", "Personalizzazione Palette Monet", LiquidIcons.Star),
    COLORS("Laboratorio Colori", "Sperimentazione Palette", LiquidIcons.Star),
    TAXES("Tasse", "Gestione Pagamenti e Fatture", LiquidIcons.Warning),
    GRADES("Media e Voti", "Calcolo Media e Simulazione", LiquidIcons.Star),
    CONTACTS("Rubrica", "Contatti Docenti e Uffici", LiquidIcons.AccountCircle),
    TRANSPORT("Trasporti", "Navette e Orari", LiquidIcons.Time),
    TRANSCRIPTS("Libretto", "Voti e CFU Registrati", LiquidIcons.Calendar),
    EXAMS("Appelli", "Prenotazione Esami", LiquidIcons.Calendar),
    STUDY_PLAN("Piano di Studio", "Pianificazione esami e CFU", LiquidIcons.Edit),
    QUESTIONNAIRES("Questionari", "Valutazione Didattica", LiquidIcons.Edit),
    EXAMS_HISTORY("Storico Appelli", "Esami passati e prenotazioni vecchie", LiquidIcons.Time),
    BADGE("Badge", "Identità Digitale", LiquidIcons.AccountCircle),
    ATTENDANCE("Presenze", "Rilevazione Aula", LiquidIcons.Check),
    PRIVACY("Privacy", "Informativa sulla Privacy", LiquidIcons.Lock),
    TERMS("Termini", "Termini di Utilizzo", LiquidIcons.Info),
    COOKIES("Cookie", "Cookie Policy", LiquidIcons.Search),
    UPDATES("Aggiornamenti", "Stato Sistema e Software", LiquidIcons.Refresh),
    CHANGELOG("Note di Rilascio", "Cronologia Versioni", LiquidIcons.Star),
    NEWS("Notizie", "Comunicazioni e Avvisi", LiquidIcons.Notifications),
    DEVICES("Dispositivi", "Gestione Sessioni Attive", LiquidIcons.Lock),
    LANGUAGE("Lingua", "Seleziona Lingua App", LiquidIcons.Info),
    AUTHOR("Autore", "Sviluppatore e Designer", LiquidIcons.AccountCircle),
    CONTACT_DETAIL("Dettaglio Contatto", "Informazioni docente", LiquidIcons.AccountCircle),
    TICKET_DETAIL("Dettaglio Biglietto", "Info titolo di viaggio", LiquidIcons.Star),
    RESERVATION_DETAIL("Dettaglio Prenotazione", "Info posto a bordo", LiquidIcons.Calendar),
    COURSE_DETAIL("Dettaglio Corso", "Informazioni esame", LiquidIcons.Star),
    TRANSPORT_BOOKING("Prenotazione Posto", "Seleziona data e ora", LiquidIcons.Calendar)
}

private fun Destination.defaultTopLevel(): Destination = when (this) {
    Destination.HOME, Destination.NEWS -> Destination.HOME
    Destination.SERVICES, Destination.TAXES, Destination.TRANSPORT, Destination.CONTACTS,
    Destination.CONTACT_DETAIL, Destination.TICKET_DETAIL, Destination.RESERVATION_DETAIL,
    Destination.TRANSPORT_BOOKING -> Destination.SERVICES
    Destination.DIDACTICS, Destination.CAREER, Destination.GRADES, Destination.TRANSCRIPTS,
    Destination.EXAMS, Destination.EXAMS_HISTORY, Destination.STUDY_PLAN,
    Destination.QUESTIONNAIRES, Destination.BADGE, Destination.ATTENDANCE, Destination.COURSE_DETAIL -> Destination.DIDACTICS
    Destination.SETTINGS, Destination.INFO, Destination.THEME, Destination.COLORS,
    Destination.PRIVACY, Destination.TERMS, Destination.COOKIES, Destination.UPDATES,
    Destination.CHANGELOG, Destination.DEVICES, Destination.LANGUAGE, Destination.AUTHOR -> Destination.SETTINGS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UniAppRoot() {
    val topTabs = remember {
        listOf(Destination.HOME, Destination.SERVICES, Destination.DIDACTICS, Destination.SETTINGS)
    }
    var destination by remember { mutableStateOf(Destination.HOME) }
    var selectedContact by remember { mutableStateOf<ContactData?>(null) }
    var selectedTicket by remember { mutableStateOf<TransportTicket?>(null) }
    var selectedReservation by remember { mutableStateOf<TransportReservation?>(null) }
    var selectedCourse by remember { mutableStateOf<StudyCourse?>(null) }
    var navigationHistory by remember { mutableStateOf(emptyList<Destination>()) }
    var isNavBarVisible by remember { mutableStateOf(true) }
    var selectedChannel by remember { mutableStateOf("Beta") }
    var showDisconnectAllDialog by remember { mutableStateOf(false) }

    // Search State
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    val seed: LiquidMonetSeed = LiquidMonetPresets.Sapphire

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val navigateTo: (Destination) -> Unit = { target ->
        if (target != destination) {
            navigationHistory = navigationHistory + destination
            destination = target
            // Reset search when navigating
            searchQuery = ""
            isSearchActive = false
        }
    }
    val navigateBack: () -> Unit = {
        navigationHistory.lastOrNull()?.let { previous ->
            navigationHistory = navigationHistory.dropLast(1)
            destination = previous
            // Reset search when navigating back
            searchQuery = ""
            isSearchActive = false
        }
    }

    BackHandler(enabled = navigationHistory.isNotEmpty(), onBack = navigateBack)

    val navBarSelectedIndex = remember(destination, navigationHistory) {
        val rootDestination = if (destination.topLevel) {
            destination
        } else {
            navigationHistory.lastOrNull { it.topLevel } ?: destination.defaultTopLevel()
        }
        topTabs.indexOf(rootDestination).coerceAtLeast(0)
    }

    LaunchedEffect(destination) {
        scrollBehavior.state.heightOffset = 0f
        scrollBehavior.state.contentOffset = 0f
        isNavBarVisible = true
    }

    val navBarScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < -2f) isNavBarVisible = false
                else if (available.y > 2f) isNavBarVisible = true
                return Offset.Zero
            }
        }
    }

    LiquidMonetTheme(useMonetEngine = true, liquidIntensity = .82f) {
        LiquidGlassScene(
            modifier = Modifier.fillMaxSize(),
            scrollBehavior = scrollBehavior,
            background = {
                LiquidBackground(
                    effect = LiquidBackgroundEffect.Aurora,
                    monetSeed = seed,
                    intensity = 1f,
                    speedFactor = .28f
                )
            },
            topBar = { backdrop ->
                val topBarTitle = when (destination) {
                    Destination.CONTACT_DETAIL -> selectedContact?.name ?: destination.title
                    Destination.TICKET_DETAIL -> selectedTicket?.title ?: destination.title
                    Destination.RESERVATION_DETAIL -> selectedReservation?.route ?: destination.title
                    Destination.COURSE_DETAIL -> selectedCourse?.name ?: destination.title
                    else -> destination.title
                }

                val topBarSubtitle = when (destination) {
                    Destination.CONTACT_DETAIL -> selectedContact?.role ?: destination.subtitle
                    Destination.TICKET_DETAIL -> selectedTicket?.validity ?: destination.subtitle
                    Destination.RESERVATION_DETAIL -> selectedReservation?.let { "${it.date} • ${it.time}" } ?: destination.subtitle
                    Destination.COURSE_DETAIL -> selectedCourse?.let { "${it.cfu} • ${it.semester}" } ?: destination.subtitle
                    else -> destination.subtitle
                }

                LiquidTopBar(
                    title = topBarTitle,
                    subtitle = topBarSubtitle,
                    backdropState = backdrop,
                    showNavigationIcon = navigationHistory.isNotEmpty(),
                    onNavigationClick = navigateBack,
                    isSearchActive = isSearchActive,
                    searchQuery = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearchActiveChange = { isSearchActive = it },
                    searchPlaceholder = if (destination == Destination.CONTACTS) stringResource(R.string.ui_contact_placeholder) else "Cerca...",
                    actionItems = when {
                        destination == Destination.CONTACTS && !isSearchActive -> listOf(
                            LiquidTopBarAction(
                                icon = LiquidIcons.Search,
                                label = "Cerca",
                                onClick = { isSearchActive = true }
                            )
                        )
                        destination == Destination.UPDATES -> listOf(
                            LiquidTopBarAction(
                                icon = LiquidIcons.MoreVert,
                                label = "Canale Rilascio",
                                subItems = listOf("Stabile", "Beta", "Developer").map { channel ->
                                    LiquidTopBarAction(
                                        icon = if (selectedChannel == channel) LiquidIcons.Check else LiquidIcons.Star,
                                        label = channel,
                                        onClick = { selectedChannel = channel }
                                    )
                                }
                            )
                        )
                        destination == Destination.EXAMS -> listOf(
                            LiquidTopBarAction(
                                icon = LiquidIcons.Time,
                                label = "Storico",
                                onClick = { navigateTo(Destination.EXAMS_HISTORY) }
                            )
                        )
                        destination == Destination.DEVICES -> listOf(
                            LiquidTopBarAction(
                                icon = LiquidIcons.Close,
                                label = "Disconnetti Tutti",
                                onClick = { showDisconnectAllDialog = true }
                            )
                        )
                        destination.topLevel -> listOf(
                            LiquidTopBarAction(
                                icon = LiquidIcons.Notifications,
                                label = "Avvisi",
                                onClick = { navigateTo(Destination.NEWS) }
                            ),
                            LiquidTopBarAction(
                                icon = LiquidIcons.Settings,
                                label = "Impostazioni",
                                onClick = { navigateTo(Destination.SETTINGS) }
                            )
                        )
                        else -> emptyList()
                    }
                )
            },
            bottomBar = { backdrop ->
                Box(Modifier.fillMaxWidth().align(Alignment.BottomCenter)) {
                    LiquidNavigationBar(
                        selectedIndex = navBarSelectedIndex,
                        onItemSelected = { index ->
                            val target = topTabs[index]
                            if (destination != target || navigationHistory.isNotEmpty()) {
                                navigationHistory = emptyList()
                                destination = target
                            }
                        },
                        items = topTabs.map { LiquidNavigationItem(label = it.title, icon = it.icon) },
                        visible = isNavBarVisible,
                        backdropState = backdrop
                    )
                }
            },
            overlay = { backdrop ->
                when (destination) {
                    Destination.TRANSPORT -> {
                        LiquidFloatingActionButton(
                            onClick = { navigateTo(Destination.TRANSPORT_BOOKING) },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 20.dp, bottom = 112.dp),
                            visible = isNavBarVisible,
                            backdropState = backdrop
                        ) {
                            Icon(LiquidIcons.Add, contentDescription = null, tint = Color.White)
                        }
                    }
                    Destination.TRANSPORT_BOOKING -> {
                        LiquidFloatingActionButton(
                            onClick = {
                                // Reset to transport after confirmation
                                navigateTo(Destination.TRANSPORT)
                            },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 20.dp, bottom = 112.dp),
                            visible = isNavBarVisible,
                            backdropState = backdrop
                        ) {
                            Icon(LiquidIcons.Check, contentDescription = null, tint = Color.White)
                        }
                    }
                    else -> {}
                }
            }
        ) { backdrop ->
            androidx.compose.material3.Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .nestedScroll(navBarScrollConnection),
                containerColor = Color.Transparent,
                topBar = {
                    androidx.compose.material3.LargeTopAppBar(
                        title = {},
                        colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            scrolledContainerColor = Color.Transparent
                        ),
                        scrollBehavior = scrollBehavior
                    )
                }
            ) { innerPadding ->
                val screenPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding(),
                    bottom = 110.dp
                )
                CompositionLocalProvider(LocalUniScreenPadding provides screenPadding) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(clip = false)
                    ) {
                        LiquidAnimatedNavContent(
                            targetState = destination,
                            transition = LiquidNavTransition.AutoDirectional,
                            label = "mainNavContent"
                        ) { currentDestination ->
                            when (currentDestination) {
                                Destination.HOME -> HomeScreen(
                                    backdropState = backdrop,
                                    onOpenCareer = { navigateTo(Destination.CAREER) },
                                    onOpenServices = { navigateTo(Destination.SERVICES) },
                                    onOpenDidactics = { navigateTo(Destination.DIDACTICS) },
                                    onOpenTaxes = { navigateTo(Destination.TAXES) },
                                    onOpenGrades = { navigateTo(Destination.GRADES) },
                                    onOpenExams = { navigateTo(Destination.EXAMS) },
                                    onOpenTranscripts = { navigateTo(Destination.TRANSCRIPTS) },
                                    onOpenContacts = { navigateTo(Destination.CONTACTS) },
                                    onOpenTransport = { navigateTo(Destination.TRANSPORT) },
                                    onOpenSettings = { navigateTo(Destination.SETTINGS) },
                                    onOpenNews = { navigateTo(Destination.NEWS) },
                                    onOpenDevices = { navigateTo(Destination.DEVICES) }
                                )
                                Destination.SERVICES -> ServicesScreen(backdrop) {
                                    when (it) {
                                        "Trasporti" -> navigateTo(Destination.TRANSPORT)
                                        "Tasse" -> navigateTo(Destination.TAXES)
                                        "Rubrica" -> navigateTo(Destination.CONTACTS)
                                        "Esse3" -> navigateTo(Destination.CAREER)
                                    }
                                }
                                Destination.DIDACTICS -> DidacticsScreen(
                                    backdrop,
                                    onOpenCareer = { navigateTo(Destination.CAREER) },
                                    onOpenTaxes = { navigateTo(Destination.TAXES) },
                                    onOpenGrades = { navigateTo(Destination.GRADES) },
                                    onOpenTranscripts = { navigateTo(Destination.TRANSCRIPTS) },
                                    onOpenExams = { navigateTo(Destination.EXAMS) },
                                    onOpenQuestionnaires = { navigateTo(Destination.QUESTIONNAIRES) },
                                    onOpenBadge = { navigateTo(Destination.BADGE) },
                                    onOpenAttendance = { navigateTo(Destination.ATTENDANCE) },
                                    onOpenStudyPlan = { navigateTo(Destination.STUDY_PLAN) }
                                )
                                Destination.SETTINGS -> SettingsScreen(
                                    backdrop,
                                    onOpenInfo = { navigateTo(Destination.INFO) },
                                    onOpenTheme = { navigateTo(Destination.THEME) },
                                    onOpenUpdates = { navigateTo(Destination.UPDATES) },
                                    onOpenDevices = { navigateTo(Destination.DEVICES) },
                                    onOpenLanguage = { navigateTo(Destination.LANGUAGE) }
                                )
                                Destination.CAREER -> TranscriptsScreen(backdrop)
                                Destination.INFO -> AppInfoScreen(
                                    backdrop,
                                    onOpenPrivacy = { navigateTo(Destination.PRIVACY) },
                                    onOpenTerms = { navigateTo(Destination.TERMS) },
                                    onOpenCookies = { navigateTo(Destination.COOKIES) },
                                    onOpenAuthor = { navigateTo(Destination.AUTHOR) }
                                )
                                Destination.THEME -> ThemeScreen(backdrop) { navigateTo(Destination.COLORS) }
                                Destination.COLORS -> ColorLabScreen(backdrop)
                                Destination.TAXES -> TaxesScreen(backdrop)
                                Destination.GRADES -> GradesScreen(backdrop)
                                Destination.CONTACTS -> ContactsScreen(
                                    backdropState = backdrop,
                                    searchQuery = if (isSearchActive) searchQuery else "",
                                    onContactClick = { contact ->
                                        selectedContact = contact
                                        navigateTo(Destination.CONTACT_DETAIL)
                                    }
                                )
                                Destination.CONTACT_DETAIL -> selectedContact?.let { contact ->
                                    ContactDetailScreen(contact, backdrop)
                                }
                                Destination.TRANSPORT -> TransportScreen(
                                    backdropState = backdrop,
                                    onReservationClick = { res ->
                                        selectedReservation = res
                                        navigateTo(Destination.RESERVATION_DETAIL)
                                    }
                                )
                                Destination.TRANSPORT_BOOKING -> TransportBookingScreen(backdrop)
                                Destination.TICKET_DETAIL -> selectedTicket?.let { ticket ->
                                    TicketDetailScreen(ticket, backdrop)
                                }
                                Destination.RESERVATION_DETAIL -> selectedReservation?.let { res ->
                                    ReservationDetailScreen(res, backdrop)
                                }
                                Destination.TRANSCRIPTS -> TranscriptsScreen(backdrop)
                                Destination.EXAMS -> ExamsScreen(backdrop)
                                Destination.EXAMS_HISTORY -> ExamsHistoryScreen(backdrop)
                                Destination.STUDY_PLAN -> StudyPlanScreen(
                                    backdropState = backdrop,
                                    onCourseClick = { course ->
                                        selectedCourse = course
                                        navigateTo(Destination.COURSE_DETAIL)
                                    }
                                )
                                Destination.COURSE_DETAIL -> selectedCourse?.let { course ->
                                    CourseDetailScreen(course, backdrop)
                                }
                                Destination.QUESTIONNAIRES -> QuestionnairesScreen(backdrop)
                                Destination.BADGE -> StudentIdScreen(backdrop)
                                Destination.ATTENDANCE -> AttendanceScreen(backdrop)
                                Destination.PRIVACY -> PrivacyScreen(backdrop)
                                Destination.TERMS -> TermsScreen(backdrop)
                                Destination.COOKIES -> CookiesScreen(backdrop)
                                Destination.UPDATES -> UpdatesScreen(
                                    backdropState = backdrop,
                                    currentChannel = selectedChannel,
                                    onOpenChangelog = { navigateTo(Destination.CHANGELOG) }
                                )
                                Destination.CHANGELOG -> ChangelogScreen(backdrop)
                                Destination.NEWS -> NewsScreen(backdrop)
                                Destination.DEVICES -> ConnectedDevicesScreen(backdrop)
                                Destination.LANGUAGE -> LanguageScreen(backdrop)
                                Destination.AUTHOR -> AuthorScreen(backdrop)
                            }
                        }

                        if (showDisconnectAllDialog) {
                            LiquidDialog(
                                onDismissRequest = { showDisconnectAllDialog = false },
                                title = "Disconnetti Tutti",
                                text = "Sei sicuro di voler chiudere tutte le sessioni attive tranne quella attuale? Dovrai rientrare su ogni altro dispositivo.",
                                backdropState = backdrop,
                                confirmButton = {
                                    LiquidButton(
                                        text = "Disconnetti",
                                        onClick = { showDisconnectAllDialog = false },
                                        variant = LiquidButtonVariant.Primary,
                                        backdropState = backdrop,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                },
                                dismissButton = {
                                    LiquidButton(
                                        text = "Annulla",
                                        onClick = { showDisconnectAllDialog = false },
                                        variant = LiquidButtonVariant.Text,
                                        backdropState = backdrop,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
