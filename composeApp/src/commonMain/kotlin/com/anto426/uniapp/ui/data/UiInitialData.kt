package com.anto426.uniapp.ui.data

import androidx.compose.ui.graphics.Color
import com.anto426.liquidmonet.components.pickers.LiquidPaletteOption
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.model.home.QuickActionItem
import com.anto426.uniapp.model.legal.LegalSectionData
import com.anto426.uniapp.model.services.ServiceData
import com.anto426.uniapp.model.settings.LanguageInfo
import com.anto426.uniapp.model.updates.ChangelogItemData
import com.anto426.uniapp.model.updates.ChangelogVersionData

/**
 * Static configuration metadata, legal disclosures, service directory links, and changelogs.
 */
object UiInitialData {
    val studentServices = listOf(
        ServiceData("Trasporti", "Navette e Orari", LiquidIcons.Time, id = "transport"),
        ServiceData("Tasse", "Pagamenti e Scadenze", LiquidIcons.CreditCard, id = "taxes"),
        ServiceData("Rubrica", "Docenti e Uffici", LiquidIcons.AccountCircle, id = "contacts"),
        ServiceData("Segreteria", "Ticket e Supporto", LiquidIcons.Phone, badgeCount = 1, id = "student-office")
    )

    val universityPortals = listOf(
        ServiceData("Esse3", "Esami e Carriera", LiquidIcons.Badge, id = "esse3"),
        ServiceData("Moodle", "Corsi e Materiale", LiquidIcons.MenuBook, id = "moodle"),
        ServiceData("Portale Web", "Sito Istituzionale", LiquidIcons.Home, id = "university-web"),
        ServiceData("Email", "Outlook Ateneo", LiquidIcons.Notifications, badgeCount = 3, id = "email")
    )

    val professorServices = listOf(
        ServiceData("Rubrica", "Contatti e uffici", LiquidIcons.AccountCircle, id = "contacts"),
        ServiceData("Docenti", "Profili e contatti accademici", LiquidIcons.AccountCircle, id = "professors"),
    )

    val professorPortals = listOf(
        ServiceData("Esse3", "Portale docenti", LiquidIcons.MenuBook, id = "esse3"),
        ServiceData("Moodle", "Corsi e materiale", LiquidIcons.MenuBook, id = "moodle"),
        ServiceData("Portale Web", "Sito istituzionale", LiquidIcons.Home, id = "university-web"),
        ServiceData("Biblioteca", "Cataloghi e servizi bibliotecari", LiquidIcons.MenuBook, id = "library"),
    )

    val allQuickActions = listOf(
        QuickActionItem("libretto", "Libretto", "Voti e CFU", LiquidIcons.MenuBook),
        QuickActionItem("statistiche", "Statistiche", "Grafici e andamento", LiquidIcons.Analytics),
        QuickActionItem("media", "Media", "Simulazione", LiquidIcons.Analytics),
        QuickActionItem("appelli", "Appelli", "Prenotazioni", LiquidIcons.Calendar),
        QuickActionItem("didattica", "Didattica", "Corsi e orari", LiquidIcons.Assignment),
        QuickActionItem("trasporti", "Trasporti", "Navette e orari", LiquidIcons.Time),
        QuickActionItem("tasse", "Tasse", "Bollettini", LiquidIcons.CreditCard),
        QuickActionItem("rubrica", "Rubrica", "Docenti", LiquidIcons.AccountCircle),
        QuickActionItem("notifiche", "Avvisi", "Comunicazioni", LiquidIcons.Notifications),
        QuickActionItem("condivisione", "Orario", "Calendario lezioni", LiquidIcons.Share),
        QuickActionItem("sicurezza", "Accessi", "Dispositivi", LiquidIcons.Lock),
        QuickActionItem("impostazioni", "Preferenze", "Personalizzazione", LiquidIcons.Settings)
    )

    val professorQuickActions = listOf(
        QuickActionItem("insegnamenti", "Insegnamenti", "Corsi assegnati", LiquidIcons.MenuBook),
        QuickActionItem("appelli", "Appelli", "Prenotazioni e commissioni", LiquidIcons.Calendar),
        QuickActionItem("tesi", "Tesi", "Tesisti e discussioni", LiquidIcons.Assignment),
        QuickActionItem("verbali", "Verbali", "Esiti e verbalizzazioni", LiquidIcons.Edit),
        QuickActionItem("rubrica", "Rubrica", "Docenti e uffici", LiquidIcons.AccountCircle),
        QuickActionItem("notifiche", "Notizie", "Comunicazioni di Ateneo", LiquidIcons.Notifications),
    )

    val changelog = listOf(
        ChangelogVersionData("v1.0.0", "26 Agosto 2026", listOf(
            ChangelogItemData("NEW", Color(0xFF4A90D9), "Nuova Esperienza UniApp", "Design system Liquid Monet e navigazione accademica reattiva."),
            ChangelogItemData("NEW", Color(0xFF4A90D9), "Career Hub", "Dashboard unificata per libretto, esami e simulazione proiettiva."),
            ChangelogItemData("FIXED", Color(0xFF00C853), "Sicurezza Sessione", "Crittografia locale con hardware keystore e gestione dispositivi connessi.")
        )),
        ChangelogVersionData("v0.9.5", "15 Agosto 2026", listOf(
            ChangelogItemData("IMPROVED", Color(0xFFFFAB00), "Ottimizzazione Grafica", "Fluidità del rendering e ridotto consumo energetico."),
            ChangelogItemData("FIXED", Color(0xFF00C853), "Notifiche", "Sincronizzazione degli avvisi didattici in tempo reale.")
        ))
    )

    val privacySections = listOf(
        LegalSectionData("Raccolta Dati", "UniApp non raccoglie né trasmette dati personali a server di terze parti. Le credenziali sono memorizzate esclusivamente sul dispositivo."),
        LegalSectionData("Sicurezza", "Utilizziamo le best practice di sicurezza Android per proteggere i tuoi dati. L'accesso biometrico è opzionale."),
        LegalSectionData("Servizi Esterni", "L'app si connette ai portali ufficiali dell'Università per recuperare le informazioni accademiche.")
    )

    val termsSections = listOf(
        LegalSectionData("Limitazione di Responsabilità", "UniApp è un client indipendente e non è affiliato ufficialmente all'Ateneo. Non siamo responsabili per inesattezze nei dati forniti dai portali esterni."),
        LegalSectionData("Uso Consentito", "L'app deve essere utilizzata esclusivamente per scopi personali e accademici. È vietato ogni tentativo di reverse engineering."),
        LegalSectionData("Modifiche ai Termini", "Ci riserviamo il diritto di modificare questi termini in qualsiasi momento per riflettere cambiamenti nel servizio o nella legge.")
    )

    val cookieSections = listOf(
        LegalSectionData("Cosa sono i cookie", "I cookie sono piccoli file di testo utilizzati per memorizzare informazioni sul dispositivo."),
        LegalSectionData("Cookie tecnici", "Utilizziamo esclusivamente cookie tecnici necessari al funzionamento dell'app e alla gestione della sessione."),
        LegalSectionData("Gestione", "Puoi cancellare i dati locali dell'app in qualsiasi momento dalle impostazioni di Android.")
    )

    val languages = listOf(
        LanguageInfo("Italiano", "Italiano (Predefinito)", "it"),
        LanguageInfo("Inglese", "English (UK)", "en")
    )

    val palettes = listOf(
        LiquidPaletteOption("Sapphire", Color(0xFF4A90D9)),
        LiquidPaletteOption("Emerald", Color(0xFF2ECC71)),
        LiquidPaletteOption("Sunset", Color(0xFFE67E22)),
        LiquidPaletteOption("Violet", Color(0xFF9B59B6))
    )
}
