package com.anto426.uniapp.android.ui.data

import androidx.compose.ui.graphics.Color
import com.anto426.liquidmonet.components.cards.LiquidStatusType
import com.anto426.liquidmonet.components.pickers.LiquidPaletteOption
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.android.ui.models.AttendanceData
import com.anto426.uniapp.android.ui.models.ChangelogItemData
import com.anto426.uniapp.android.ui.models.ChangelogVersionData
import com.anto426.uniapp.android.ui.models.ContactData
import com.anto426.uniapp.android.ui.models.CourseStatus
import com.anto426.uniapp.android.ui.models.DeviceInfo
import com.anto426.uniapp.android.ui.models.DeviceType
import com.anto426.uniapp.android.ui.models.ExamRecord
import com.anto426.uniapp.android.ui.models.ExamSession
import com.anto426.uniapp.android.ui.models.GradeExam
import com.anto426.uniapp.android.ui.models.GradeSimulationPreset
import com.anto426.uniapp.android.ui.models.LanguageInfo
import com.anto426.uniapp.android.ui.models.LegalSectionData
import com.anto426.uniapp.android.ui.models.NewsItem
import com.anto426.uniapp.android.ui.models.PastExam
import com.anto426.uniapp.android.ui.models.QuestionnaireData
import com.anto426.uniapp.android.ui.models.QuestionnaireStatus
import com.anto426.uniapp.android.ui.models.QuickActionItem
import com.anto426.uniapp.android.ui.models.ServiceData
import com.anto426.uniapp.android.ui.models.StudyCourse
import com.anto426.uniapp.android.ui.models.StudyYear
import com.anto426.uniapp.android.ui.models.TaxPaymentData
import com.anto426.uniapp.android.ui.models.TransportReservation
import com.anto426.uniapp.android.ui.models.TransportRoute
import com.anto426.uniapp.android.ui.models.TransportTicket

/** Static presentation data used until the screen ViewModels are connected to the backend. */
object UiInitialData {
    val attendance = listOf(
        AttendanceData("Analisi Matematica II", "85%", "17/20"),
        AttendanceData("Fisica Generale", "70%", "14/20"),
        AttendanceData("Sistemi Operativi", "100%", "5/5")
    )

    val transcripts = listOf(
        ExamRecord("Analisi Matematica I", "30", "12 CFU", "15/02/2025"),
        ExamRecord("Programmazione I", "28", "9 CFU", "20/02/2025"),
        ExamRecord("Architettura Elaboratori", "26", "9 CFU", "05/06/2025"),
        ExamRecord("Inglese B2", "Idoneo", "6 CFU", "10/06/2025")
    )

    val questionnaires = listOf(
        QuestionnaireData("Analisi Matematica II", "Prof. Bianchi", "MAT/05", QuestionnaireStatus.PENDING),
        QuestionnaireData("Sistemi Operativi", "Prof. Verdi", "INF/01", QuestionnaireStatus.PENDING),
        QuestionnaireData("Fisica Generale II", "Prof. Rossi", "FIS/01", QuestionnaireStatus.COMPLETED)
    )

    val studyPlan = listOf(
        StudyYear(
            "Primo Anno",
            listOf(
                StudyCourse("1", "Analisi Matematica I", "12 CFU", CourseStatus.COMPLETED, "Enrico De Bernardis", "Lo studio delle funzioni di una variabile reale, limiti, derivate e integrali.", "Primo Semestre"),
                StudyCourse("2", "Programmazione I", "9 CFU", CourseStatus.COMPLETED, "Luca Bianchi", "Fondamenti della programmazione procedurale e orientata agli oggetti.", "Primo Semestre"),
                StudyCourse("3", "Fisica I", "9 CFU", CourseStatus.ACTIVE, "Maria Rossi", "Meccanica classica e termodinamica.", "Secondo Semestre"),
                StudyCourse("4", "Chimica", "6 CFU", CourseStatus.PLANNED, "Anna Neri", "Fondamenti di chimica generale e inorganica.", "Secondo Semestre")
            ),
            LiquidIcons.Home
        ),
        StudyYear(
            "Secondo Anno",
            listOf(
                StudyCourse("5", "Algoritmi e Strutture Dati", "9 CFU", CourseStatus.PLANNED, "Elena Bianchi", "Analisi della complessità computazionale e strutture dati fondamentali.", "Primo Semestre"),
                StudyCourse("6", "Sistemi Operativi", "9 CFU", CourseStatus.PLANNED, "Mario Rossi", "Architettura dei sistemi operativi, gestione processi e memoria.", "Primo Semestre"),
                StudyCourse("7", "Basi di Dati", "9 CFU", CourseStatus.PLANNED, "Luigi Verdi", "Progettazione e gestione di basi di dati relazionali.", "Secondo Semestre")
            ),
            LiquidIcons.Star
        )
    )

    val contacts = listOf(
        ContactData("Mario Rossi", "Prof. Ordinario - Analisi Matematica", "MR", "mario.rossi@unimol.it", "+39 0874 123456"),
        ContactData("Luigi Verdi", "Prof. Associato - Programmazione", "LV", "luigi.verdi@unimol.it", "+39 0874 654321"),
        ContactData("Elena Bianchi", "Ricercatore - Basi di Dati", "EB", "elena.bianchi@unimol.it", "+39 0874 111222"),
        ContactData("Anna Neri", "Segreteria Didattica", "AN", "anna.neri@unimol.it", "+39 0874 333444")
    )

    val languages = listOf(
        LanguageInfo("Italiano", "Italia", "it"),
        LanguageInfo("English", "United Kingdom", "en"),
        LanguageInfo("Français", "France", "fr"),
        LanguageInfo("Deutsch", "Deutschland", "de"),
        LanguageInfo("Español", "España", "es")
    )

    val devices = listOf(
        DeviceInfo("OnePlus 12", "Questo dispositivo • Campobasso, Italia", "Attivo ora", DeviceType.PHONE, true),
        DeviceInfo("MacBook Pro 14\"", "Chrome • Milano, Italia", "2 ore fa", DeviceType.PC, false),
        DeviceInfo("iPad Air", "App UniApp • Roma, Italia", "Ieri, 18:45", DeviceType.TABLET, false)
    )

    val pastExams = listOf(
        PastExam("Programmazione I", "15/02/2026", "30L", "Superato"),
        PastExam("Architettura degli Elaboratori", "20/01/2026", "27", "Superato"),
        PastExam("Analisi Matematica I", "12/09/2025", "24", "Superato")
    )

    val examSessions = listOf(
        ExamSession("Analisi Matematica II", "30/06/2026", "09:00", "Aula Magna", "01/06/2026", "25/06/2026", "Scritto e Orale", "Enrico De Bernardis", 124, true),
        ExamSession("Fisica Generale", "05/07/2026", "10:30", "Aula 2", "10/06/2026", "01/07/2026", "Scritto", "Maria Rossi", 85, false),
        ExamSession("Ingegneria del Software", "12/07/2026", "15:00", "Laboratorio Inf.", "15/06/2026", "08/07/2026", "Progetto e Orale", "Luca Bianchi", 42, false)
    )

    val currentGradeExams = listOf(
        GradeExam("Analisi Matematica I", 30, 12),
        GradeExam("Programmazione I", 28, 9),
        GradeExam("Architettura Elaboratori", 26, 9)
    )

    val gradeSimulation = listOf(
        GradeSimulationPreset("Analisi Matematica II", 9, 24),
        GradeSimulationPreset("Fisica Generale", 9, 24),
        GradeSimulationPreset("Sistemi Operativi", 9, 24)
    )

    val taxPayments = listOf(
        TaxPaymentData("II Rata - A.A. 2026/27", "Scadenza: 30 Mag 2026", "€ 456,00", false, "000000123456789"),
        TaxPaymentData("I Rata - Iscrizione", "Pagato il: 15 Ott 2025", "€ 156,00", true, "000000111222333"),
        TaxPaymentData("Contributo Onnicomprensivo", "Pagato il: 12 Gen 2026", "€ 300,00", true, "000000444555666"),
        TaxPaymentData("Tassa Regionale ADISU", "Pagato il: 10 Gen 2026", "€ 140,00", true, "000000777888999")
    )

    val transportRoutes = listOf(
        TransportRoute("Linea A: Stazione - Campus", "Prossima: 10:15", "5 min"),
        TransportRoute("Linea B: Centro - Campus", "Prossima: 10:30", "20 min"),
        TransportRoute("Linea C: Periferia - Campus", "Prossima: 10:45", "35 min")
    )

    val availableTickets = listOf(
        TransportTicket("T1", "Corsa Singola", "€ 1,20", "Valido 90 min", "Urbano", LiquidIcons.Star),
        TransportTicket("T2", "Biglietto Giornaliero", "€ 3,50", "Valido 24 ore", "Urbano", LiquidIcons.Star),
        TransportTicket("T3", "Abbonamento Mensile", "€ 25,00", "Solare", "Studenti", LiquidIcons.Lock),
        TransportTicket("T4", "Carnet 10 Corse", "€ 10,00", "Senza scadenza", "Urbano", LiquidIcons.Star)
    )

    val myTransportReservations = listOf(
        TransportReservation("R1", "Linea A: Stazione -> Campus", "28 Ago 2026", "08:15", "Confermato", "UNIMOL-RES-12345"),
        TransportReservation("R2", "Linea B: Centro -> Campus", "29 Ago 2026", "09:00", "In attesa", "UNIMOL-RES-67890")
    )

    val studentServices = listOf(
        ServiceData("Trasporti", "Navette e Orari", LiquidIcons.Time),
        ServiceData("Tasse", "Pagamenti e Scadenze", LiquidIcons.Warning),
        ServiceData("Rubrica", "Docenti e Uffici", LiquidIcons.AccountCircle),
        ServiceData("Segreteria", "Ticket e Supporto", LiquidIcons.Phone, badgeCount = 1)
    )

    val universityPortals = listOf(
        ServiceData("Esse3", "Esami e Carriera", LiquidIcons.Calendar),
        ServiceData("Moodle", "Corsi e Materiale", LiquidIcons.Star),
        ServiceData("Portale Web", "Sito Istituzionale", LiquidIcons.Home),
        ServiceData("Email", "Outlook Ateneo", LiquidIcons.Notifications, badgeCount = 3)
    )

    val homeNews = listOf(
        NewsItem("Sessione Appelli", "Sono aperti 2 nuovi appelli per i corsi del tuo piano di studi.", "La sessione autunnale è ufficialmente aperta. Puoi prenotarti per gli esami di 'Ingegneria del Software' e 'Sistemi Operativi' tramite l'apposita sezione nel portale dello studente.", LiquidStatusType.Info),
        NewsItem("Scadenza Tasse", "La seconda rata scade tra 5 giorni. Evita le more.", "Ti ricordiamo che il termine ultimo per il pagamento della seconda rata delle tasse universitarie è fissato per il 31 Agosto. Il pagamento può essere effettuato tramite PagoPA.", LiquidStatusType.Warning),
        NewsItem("Pubblicazione Orari", "Disponibile il nuovo orario delle lezioni per il semestre.", "Gli orari delle lezioni per il primo semestre dell'anno accademico 2026/27 sono stati pubblicati sul sito di dipartimento. Le lezioni inizieranno il 21 Settembre.", LiquidStatusType.Success)
    )

    val universityNews = listOf(
        NewsItem("Inaugurazione Anno Accademico", "La cerimonia si terrà il 15 Settembre presso l'Aula Magna.", "L'Università degli Studi del Molise è lieta di invitare tutta la comunità accademica alla cerimonia di inaugurazione del nuovo anno accademico. Saranno presenti le massime autorità regionali e nazionali. Seguirà un rinfresco nel giardino del rettorato.", LiquidStatusType.Info),
        NewsItem("Bando Borse di Studio", "Pubblicate le graduatorie definitive per l'anno 2026/27.", "Sono disponibili sul portale dello studente le graduatorie definitive per l'assegnazione delle borse di studio e dei posti alloggio. Gli aventi diritto riceveranno una comunicazione ufficiale via email entro 48 ore.", LiquidStatusType.Success)
    )

    val departmentNews = listOf(
        NewsItem("Seminario AI & Ethics", "Incontro con esperti del settore domani alle ore 10:00 in Sala Riunioni.", "Il dipartimento organizza un workshop intensivo sull'impatto etico dell'intelligenza artificiale generativa nella società moderna. Interverranno docenti di fama internazionale e rappresentanti dell'industria tech.", LiquidStatusType.Info),
        NewsItem("Sospensione Lezioni", "Le lezioni di Ingegneria del Software sono sospese per motivi tecnici.", "A causa di lavori di manutenzione straordinaria agli impianti elettrici dell'edificio polifunzionale, tutte le lezioni previste per oggi nel Blocco A sono rinviate a data da destinarsi.", LiquidStatusType.Warning)
    )

    val eventNews = listOf(
        NewsItem("UniApp Hackathon 2026", "Iscrizioni aperte per la maratona di programmazione di Ottobre.", "Partecipa alla terza edizione dell'UniApp Hackathon! 48 ore di codice, creatività e divertimento. In palio premi tecnologici e stage presso le aziende partner. Formate il vostro team e iscrivetevi subito!", LiquidStatusType.Success),
        NewsItem("Career Day", "Incontra le aziende partner il 20 Ottobre nel piazzale antistante.", "Il Career Day torna in presenza. Oltre 50 aziende nazionali e internazionali incontreranno studenti e laureati per colloqui conoscitivi e presentazioni aziendali. Non dimenticare il tuo CV!", LiquidStatusType.Info)
    )

    val allQuickActions = listOf(
        QuickActionItem("libretto", "Libretto", "Voti e CFU", LiquidIcons.Calendar),
        QuickActionItem("media", "Media", "Simulazione", LiquidIcons.Star),
        QuickActionItem("appelli", "Appelli", "Prenotazioni", LiquidIcons.Edit),
        QuickActionItem("didattica", "Didattica", "Corsi e orari", LiquidIcons.Star),
        QuickActionItem("trasporti", "Trasporti", "Navette e orari", LiquidIcons.Time),
        QuickActionItem("tasse", "Tasse", "Bollettini", LiquidIcons.Lock),
        QuickActionItem("rubrica", "Rubrica", "Docenti", LiquidIcons.AccountCircle),
        QuickActionItem("notifiche", "Avvisi", "Comunicazioni", LiquidIcons.Notifications),
        QuickActionItem("condivisione", "Orario", "Calendario lezioni", LiquidIcons.Share),
        QuickActionItem("sicurezza", "Accessi", "Dispositivi", LiquidIcons.Lock),
        QuickActionItem("impostazioni", "Preferenze", "Personalizzazione", LiquidIcons.Settings)
    )

    val changelog = listOf(
        ChangelogVersionData("v1.0.0", "26 Agosto 2026", listOf(
            ChangelogItemData("NEW", Color(0xFF4A90D9), "Motore Ottico Liquid 2.0", "Rifrazione Snell 1:1 e dispersione cromatica dinamica su tutti i componenti in vetro."),
            ChangelogItemData("NEW", Color(0xFF4A90D9), "Career Hub", "Nuova dashboard per la gestione del libretto e simulazione proiettiva dei voti."),
            ChangelogItemData("FIXED", Color(0xFF00C853), "Correzione Login", "Risolto un problema di timeout durante l'autenticazione con i servizi di ateneo.")
        )),
        ChangelogVersionData("v0.9.5", "15 Agosto 2026", listOf(
            ChangelogItemData("IMPROVED", Color(0xFFFFAB00), "Performance UI", "Ottimizzato il rendering del LiquidBackground per ridurre il consumo di batteria."),
            ChangelogItemData("FIXED", Color(0xFF00C853), "Notifiche", "Corretto il bug che impediva la ricezione degli avvisi didattici in background.")
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

    val palettes = listOf(
        LiquidPaletteOption("Sapphire", Color(0xFF4A90D9)),
        LiquidPaletteOption("Emerald", Color(0xFF2ECC71)),
        LiquidPaletteOption("Sunset", Color(0xFFE67E22)),
        LiquidPaletteOption("Violet", Color(0xFF9B59B6))
    )
}
