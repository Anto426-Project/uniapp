package com.anto426.uniapp.navigation.ui

import androidx.compose.ui.graphics.vector.ImageVector
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.navigation.model.AppRoute

internal data class AppRoutePresentation(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
)

internal fun AppRoute.presentation(): AppRoutePresentation =
    when (this) {
        AppRoute.Bootstrap -> AppRoutePresentation("UniApp", "Avvio sicuro", LiquidIcons.Lock)
        AppRoute.Login -> AppRoutePresentation("Accedi", "Portale studenti", LiquidIcons.Lock)
        AppRoute.Home -> AppRoutePresentation("UniApp", "Liquidnio • Ingegneria Informatica", LiquidIcons.Home)
        AppRoute.Services -> AppRoutePresentation("Servizi", "Tasse, Trasporti e Servizi Studente", LiquidIcons.Star)
        AppRoute.Didactics -> AppRoutePresentation("Didattica", "Appelli, Libretto e Valutazioni", LiquidIcons.Calendar)
        AppRoute.Settings -> AppRoutePresentation("Impostazioni", "Preferenze e Configurazione", LiquidIcons.Settings)
        AppRoute.Accounts -> AppRoutePresentation("Account", "Gestione profili universitari", LiquidIcons.AccountCircle)
        AppRoute.Career -> AppRoutePresentation("Carriera", "Riepilogo Esami e CFU", LiquidIcons.Calendar)
        AppRoute.Info -> AppRoutePresentation("Informazioni", "Versione e Note di Rilascio", LiquidIcons.Info)
        AppRoute.Theme -> AppRoutePresentation("Tema", "Personalizzazione Palette Monet", LiquidIcons.Star)
        AppRoute.Colors -> AppRoutePresentation("Laboratorio Colori", "Sperimentazione Palette", LiquidIcons.Star)
        AppRoute.Taxes -> AppRoutePresentation("Tasse", "Gestione Pagamenti e Fatture", LiquidIcons.Warning)
        AppRoute.Grades -> AppRoutePresentation("Media e Voti", "Calcolo Media e Simulazione", LiquidIcons.Star)
        AppRoute.Statistics -> AppRoutePresentation("Statistiche", "Grafici e Andamento Carriera", LiquidIcons.Star)
        AppRoute.Contacts -> AppRoutePresentation("Rubrica", "Contatti Docenti e Uffici", LiquidIcons.AccountCircle)
        is AppRoute.ContactDetail -> AppRoutePresentation("Dettaglio Contatto", "Informazioni docente", LiquidIcons.AccountCircle)
        AppRoute.Transport -> AppRoutePresentation("Trasporti", "Navette e Orari", LiquidIcons.Time)
        AppRoute.TransportCatalog -> AppRoutePresentation("Biglietti", "Catalogo titoli di viaggio", LiquidIcons.Star)
        AppRoute.TransportBooking -> AppRoutePresentation("Prenotazione Posto", "Seleziona data e ora", LiquidIcons.Calendar)
        is AppRoute.TicketDetail -> AppRoutePresentation("Dettaglio Biglietto", "Info titolo di viaggio", LiquidIcons.Star)
        is AppRoute.ReservationDetail -> AppRoutePresentation("Dettaglio Prenotazione", "Info posto a bordo", LiquidIcons.Calendar)
        AppRoute.Transcripts -> AppRoutePresentation("Libretto", "Voti e CFU Registrati", LiquidIcons.Calendar)
        AppRoute.Exams -> AppRoutePresentation("Appelli", "Prenotazione Esami", LiquidIcons.Calendar)
        AppRoute.ExamsHistory -> AppRoutePresentation("Storico Appelli", "Esami e prenotazioni precedenti", LiquidIcons.Time)
        AppRoute.StudyPlan -> AppRoutePresentation("Piano di Studio", "Pianificazione esami e CFU", LiquidIcons.Edit)
        is AppRoute.CourseDetail -> AppRoutePresentation("Dettaglio Corso", "Informazioni esame", LiquidIcons.Star)
        AppRoute.Questionnaires -> AppRoutePresentation("Questionari", "Valutazione Didattica", LiquidIcons.Edit)
        AppRoute.Badge -> AppRoutePresentation("Badge", "Identità Digitale", LiquidIcons.AccountCircle)
        AppRoute.Attendance -> AppRoutePresentation("Presenze", "Rilevazione Aula", LiquidIcons.Check)
        AppRoute.Privacy -> AppRoutePresentation("Privacy", "Informativa sulla Privacy", LiquidIcons.Lock)
        AppRoute.Terms -> AppRoutePresentation("Termini", "Termini di Utilizzo", LiquidIcons.Info)
        AppRoute.Cookies -> AppRoutePresentation("Cookie", "Cookie Policy", LiquidIcons.Search)
        AppRoute.Updates -> AppRoutePresentation("Aggiornamenti", "Stato Sistema e Software", LiquidIcons.Refresh)
        AppRoute.Changelog -> AppRoutePresentation("Note di Rilascio", "Cronologia Versioni", LiquidIcons.Star)
        AppRoute.News -> AppRoutePresentation("Notizie", "Comunicazioni e Avvisi", LiquidIcons.Notifications)
        AppRoute.Devices -> AppRoutePresentation("Dispositivi", "Gestione Sessioni Attive", LiquidIcons.Lock)
        AppRoute.Language -> AppRoutePresentation("Lingua", "Seleziona Lingua App", LiquidIcons.Info)
        AppRoute.Author -> AppRoutePresentation("Autore", "Sviluppatore e Designer", LiquidIcons.AccountCircle)
    }
