package com.anto426.uniapp.ui.data

import androidx.compose.ui.graphics.Color
import com.anto426.liquidmonet.components.cards.LiquidStatusType
import com.anto426.liquidmonet.components.pickers.LiquidPaletteOption
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.model.didactics.AttendanceData
import com.anto426.uniapp.model.didactics.CourseStatus
import com.anto426.uniapp.model.didactics.ExamRecord
import com.anto426.uniapp.model.didactics.ExamSession
import com.anto426.uniapp.model.didactics.GradeExam
import com.anto426.uniapp.model.didactics.GradeSimulationPreset
import com.anto426.uniapp.model.didactics.PastExam
import com.anto426.uniapp.model.didactics.QuestionnaireData
import com.anto426.uniapp.model.didactics.QuestionnaireStatus
import com.anto426.uniapp.model.didactics.StudyCourse
import com.anto426.uniapp.model.didactics.StudyYear
import com.anto426.uniapp.model.home.QuickActionItem
import com.anto426.uniapp.model.legal.LegalSectionData
import com.anto426.uniapp.model.news.NewsItem
import com.anto426.uniapp.model.services.ContactCategory
import com.anto426.uniapp.model.services.ContactData
import com.anto426.uniapp.model.services.ServiceData
import com.anto426.uniapp.model.services.TaxPaymentData
import com.anto426.uniapp.model.settings.DeviceInfo
import com.anto426.uniapp.model.settings.DeviceType
import com.anto426.uniapp.model.settings.LanguageInfo
import com.anto426.uniapp.model.transport.TransportReservation
import com.anto426.uniapp.model.transport.TransportRoute
import com.anto426.uniapp.model.transport.TransportTicket
import com.anto426.uniapp.model.transport.TripDirection
import com.anto426.uniapp.model.updates.ChangelogItemData
import com.anto426.uniapp.model.updates.ChangelogVersionData

/** Static presentation data used until the screen ViewModels are connected to the backend. */
object UiInitialData {
    val attendance = listOf(
        AttendanceData("Analisi Matematica II", "85%", "17/20"),
        AttendanceData("Fisica Generale", "70%", "14/20"),
        AttendanceData("Sistemi Operativi", "100%", "5/5")
    )

    val transcripts = listOf(
        // 1° Anno
        ExamRecord("Analisi Matematica I", "30", "12 CFU", "15/02/2025", year = 1, code = "MAT/05"),
        ExamRecord("Programmazione I", "28", "9 CFU", "20/02/2025", year = 1, code = "INF/01"),
        ExamRecord("Architettura Elaboratori", "26", "9 CFU", "05/06/2025", year = 1, code = "ING-INF/05"),
        ExamRecord("Inglese B2", "Idoneo", "6 CFU", "10/06/2025", year = 1, code = "L-LIN/12"),
        ExamRecord("Fisica Generale", "27", "9 CFU", "18/07/2025", year = 1, code = "FIS/01"),

        // 2° Anno
        ExamRecord("Algoritmi e Strutture Dati", "30L", "9 CFU", "14/02/2026", year = 2, code = "INF/01", lode = true),
        ExamRecord("Basi di Dati", "29", "9 CFU", "26/02/2026", year = 2, code = "INF/01"),
        ExamRecord("Sistemi Operativi", "28", "9 CFU", "12/06/2026", year = 2, code = "INF/01"),
        ExamRecord("Reti di Calcolatori", "30", "9 CFU", "04/07/2026", year = 2, code = "ING-INF/05"),

        // 3° Anno
        ExamRecord("Ingegneria del Software", "30", "9 CFU", "18/01/2027", year = 3, code = "INF/01"),
        ExamRecord("Sicurezza Informatica", "28", "6 CFU", "12/02/2027", year = 3, code = "INF/01")
    )

    val questionnaires = listOf(
        QuestionnaireData("Analisi Matematica II", "Prof. Bianchi", "MAT/05", QuestionnaireStatus.PENDING),
        QuestionnaireData("Sistemi Operativi", "Prof. Verdi", "INF/01", QuestionnaireStatus.PENDING),
        QuestionnaireData("Fisica Generale II", "Prof. Rossi", "FIS/01", QuestionnaireStatus.COMPLETED)
    )

    val studyPlan = listOf(
        StudyYear(
            yearNumber = 1,
            yearName = "Primo Anno",
            courses = listOf(
                StudyCourse("1", "Analisi Matematica I", "12 CFU", CourseStatus.COMPLETED, "Enrico De Bernardis", "Lo studio delle funzioni di una variabile reale, limiti, derivate e integrali.", "Primo Semestre"),
                StudyCourse("2", "Programmazione I", "9 CFU", CourseStatus.COMPLETED, "Luca Bianchi", "Fondamenti della programmazione procedurale e orientata agli oggetti.", "Primo Semestre"),
                StudyCourse("3", "Architettura Elaboratori", "9 CFU", CourseStatus.COMPLETED, "Roberto Rossi", "Struttura hardware e organizzazione dei calcolatori elettronici.", "Secondo Semestre"),
                StudyCourse("4", "Inglese B2", "6 CFU", CourseStatus.COMPLETED, "Sarah Jenkins", "Competenze linguistiche universitarie di livello B2.", "Secondo Semestre"),
                StudyCourse("5", "Fisica Generale", "9 CFU", CourseStatus.COMPLETED, "Maria Rossi", "Meccanica classica, onde ed elettromagnetismo.", "Secondo Semestre"),
                StudyCourse("6", "Algebra Lineare e Geometria", "9 CFU", CourseStatus.COMPLETED, "Marco Neri", "Spazi vettoriali, matrici e geometria analitica.", "Primo Semestre"),
                StudyCourse("7", "Chimica", "6 CFU", CourseStatus.COMPLETED, "Anna Neri", "Fondamenti di chimica generale ed elettrochimica.", "Secondo Semestre")
            )
        ),
        StudyYear(
            yearNumber = 2,
            yearName = "Secondo Anno",
            courses = listOf(
                StudyCourse("8", "Algoritmi e Strutture Dati", "9 CFU", CourseStatus.COMPLETED, "Elena Bianchi", "Analisi della complessità computazionale e strutture dati fondamentali.", "Primo Semestre"),
                StudyCourse("9", "Basi di Dati", "9 CFU", CourseStatus.COMPLETED, "Luigi Verdi", "Progettazione concettuale, logica e gestione di DBMS relazionali.", "Primo Semestre"),
                StudyCourse("10", "Sistemi Operativi", "9 CFU", CourseStatus.COMPLETED, "Mario Rossi", "Architettura dei sistemi operativi, processi, sincronizzazione e memoria.", "Secondo Semestre"),
                StudyCourse("11", "Reti di Calcolatori", "9 CFU", CourseStatus.COMPLETED, "Davide Riva", "Architettura delle reti di telecomunicazione, protocolli TCP/IP.", "Secondo Semestre"),
                StudyCourse("12", "Calcolo delle Probabilità", "6 CFU", CourseStatus.ACTIVE, "Fabio Conti", "Modelli probabilistici e inferenza statistica per l'ingegneria.", "Primo Semestre"),
                StudyCourse("13", "Programmazione II", "9 CFU", CourseStatus.ACTIVE, "Luca Bianchi", "Programmazione avanzata, design patterns e programmazione funzionale.", "Secondo Semestre"),
                StudyCourse("14", "Elettrotecnica", "9 CFU", CourseStatus.ACTIVE, "Gianni Greco", "Circuiti lineari in regime stazionario e sinusoidale.", "Secondo Semestre")
            )
        ),
        StudyYear(
            yearNumber = 3,
            yearName = "Terzo Anno",
            courses = listOf(
                StudyCourse("15", "Ingegneria del Software", "9 CFU", CourseStatus.COMPLETED, "Paolo Gialli", "Metodologie agili, pattern architetturali e ciclo di vita del software.", "Primo Semestre"),
                StudyCourse("16", "Sicurezza Informatica", "6 CFU", CourseStatus.COMPLETED, "Sara Moro", "Crittografia, sicurezza dei sistemi operativi e reti.", "Primo Semestre"),
                StudyCourse("17", "Intelligenza Artificiale", "9 CFU", CourseStatus.PLANNED, "Alessio Leone", "Machine learning, reti neurali e ragionamento automatico.", "Secondo Semestre"),
                StudyCourse("18", "Sistemi Distribuiti e Cloud", "9 CFU", CourseStatus.PLANNED, "Valerio Fontana", "Microservizi, containerizzazione, Kubernetes e cloud computing.", "Secondo Semestre"),
                StudyCourse("19", "Tirocinio Formativo", "12 CFU", CourseStatus.PLANNED, "Tutor Aziendale", "Attività formativa professionalizzante presso azienda convenzionata.", "Annuale"),
                StudyCourse("20", "Prova Finale / Tesi", "6 CFU", CourseStatus.PLANNED, "Relatore Accademico", "Svolgimento e discussione della tesi di laurea triennale.", "Secondo Semestre")
            )
        )
    )

    val contacts = listOf(
        // Docenti
        ContactData(
            name = "Mario Rossi",
            role = "Prof. Ordinario - Analisi Matematica",
            initials = "MR",
            email = "mario.rossi@unimol.it",
            phone = "+39 0874 123456",
            category = ContactCategory.TEACHERS,
            department = "Dipartimento di Bioscienze e Territorio",
            office = "Studio 204, Edificio III",
            officeHours = "Martedì 11:00 - 13:00"
        ),
        ContactData(
            name = "Luigi Verdi",
            role = "Prof. Associato - Programmazione",
            initials = "LV",
            email = "luigi.verdi@unimol.it",
            phone = "+39 0874 654321",
            category = ContactCategory.TEACHERS,
            department = "Dipartimento di Bioscienze e Territorio",
            office = "Laboratorio Info 2, Edificio II",
            officeHours = "Mercoledì 15:00 - 17:00"
        ),
        ContactData(
            name = "Elena Bianchi",
            role = "Ricercatore - Basi di Dati",
            initials = "EB",
            email = "elena.bianchi@unimol.it",
            phone = "+39 0874 111222",
            category = ContactCategory.TEACHERS,
            department = "Dipartimento di Bioscienze e Territorio",
            office = "Studio 112, Edificio I",
            officeHours = "Lunedì 10:00 - 12:00"
        ),
        ContactData(
            name = "Enrico De Bernardis",
            role = "Prof. Ordinario - Ingegneria del Software",
            initials = "ED",
            email = "enrico.debernardis@unimol.it",
            phone = "+39 0874 987654",
            category = ContactCategory.TEACHERS,
            department = "Dipartimento di Bioscienze e Territorio",
            office = "Studio 301, Edificio III",
            officeHours = "Giovedì 14:30 - 16:30"
        ),

        // Segreterie
        ContactData(
            name = "Segreteria Studenti Area Scientifica",
            role = "Sportello Didattico e Immatricolazioni",
            initials = "SS",
            email = "segreteria.scienze@unimol.it",
            phone = "+39 0874 404100",
            category = ContactCategory.SECRETARIAT,
            department = "Centro Servizi Studenti",
            office = "Sportello 3, Piano Terra",
            officeHours = "Lun-Ven 09:00 - 12:30"
        ),
        ContactData(
            name = "Ufficio Tasse e Diritto allo Studio",
            role = "Gestione Contributi e ISEE Universitario",
            initials = "UT",
            email = "tasse.studenti@unimol.it",
            phone = "+39 0874 404220",
            category = ContactCategory.SECRETARIAT,
            department = "Direzione Didattica",
            office = "Sportello 5, Piano Terra",
            officeHours = "Mar e Gio 10:00 - 13:00"
        ),

        // Uffici & Servizi
        ContactData(
            name = "Ufficio Relazioni Internazionali ed Erasmus+",
            role = "Mobilità Internazionale e Accordi Bilaterali",
            initials = "ER",
            email = "erasmus@unimol.it",
            phone = "+39 0874 404350",
            category = ContactCategory.SERVICES,
            department = "Rettorato",
            office = "Stanza 12, Piano 1",
            officeHours = "Lun, Mer, Ven 10:00 - 12:00"
        ),
        ContactData(
            name = "Helpdesk ICT e Servizi Digitali",
            role = "Supporto Account, Wi-Fi e Posta Istituzionale",
            initials = "HD",
            email = "helpdesk@unimol.it",
            phone = "+39 0874 404999",
            category = ContactCategory.SERVICES,
            department = "Centro Informatico di Ateneo",
            office = "Blocco Tecnologico",
            officeHours = "Lun-Ven 08:30 - 17:30"
        )
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
        // Oggi
        TransportReservation(
            id = "R1",
            route = "Linea Campus: Stazione FS → Campus Vazzieri",
            date = "Oggi, 28 Ago",
            time = "08:15",
            direction = TripDirection.ANDATA,
            qrCodeData = "UNIMOL-BUS-2026-A1",
            departureStop = "Stazione Centrale FS",
            arrivalStop = "Campus Vazzieri - Edificio I",
            busNumber = "Navetta 1"
        ),
        TransportReservation(
            id = "R2",
            route = "Linea Campus: Campus Vazzieri → Stazione FS",
            date = "Oggi, 28 Ago",
            time = "17:30",
            direction = TripDirection.RITORNO,
            qrCodeData = "UNIMOL-BUS-2026-R1",
            departureStop = "Campus Vazzieri - Edificio I",
            arrivalStop = "Stazione Centrale FS",
            busNumber = "Navetta 2"
        ),

        // Domani
        TransportReservation(
            id = "R3",
            route = "Linea Campus: Stazione FS → Campus Vazzieri",
            date = "Domani, 29 Ago",
            time = "09:00",
            direction = TripDirection.ANDATA,
            qrCodeData = "UNIMOL-BUS-2026-A2",
            departureStop = "Stazione Centrale FS",
            arrivalStop = "Campus Vazzieri - Edificio I",
            busNumber = "Navetta 1"
        ),
        TransportReservation(
            id = "R4",
            route = "Linea Campus: Campus Vazzieri → Stazione FS",
            date = "Domani, 29 Ago",
            time = "18:15",
            direction = TripDirection.RITORNO,
            qrCodeData = "UNIMOL-BUS-2026-R2",
            departureStop = "Campus Vazzieri - Edificio I",
            arrivalStop = "Stazione Centrale FS",
            busNumber = "Navetta 1"
        ),

        // Lunedì
        TransportReservation(
            id = "R5",
            route = "Linea Centro: Piazza Prefettura → Campus Pesche",
            date = "Lunedì, 31 Ago",
            time = "08:30",
            direction = TripDirection.ANDATA,
            qrCodeData = "UNIMOL-BUS-2026-A3",
            departureStop = "Piazza Prefettura",
            arrivalStop = "Campus Pesche - Polo Didattico",
            busNumber = "Linea 3"
        ),
        TransportReservation(
            id = "R6",
            route = "Linea Centro: Campus Pesche → Piazza Prefettura",
            date = "Lunedì, 31 Ago",
            time = "16:45",
            direction = TripDirection.RITORNO,
            qrCodeData = "UNIMOL-BUS-2026-R3",
            departureStop = "Campus Pesche - Polo Didattico",
            arrivalStop = "Piazza Prefettura",
            busNumber = "Linea 3"
        )
    )

    val studentServices = listOf(
        ServiceData("Trasporti", "Navette e Orari", LiquidIcons.Time),
        ServiceData("Tasse", "Pagamenti e Scadenze", LiquidIcons.Warning),
        ServiceData("Statistiche", "Grafici e Andamento", LiquidIcons.Star),
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
        QuickActionItem("statistiche", "Statistiche", "Grafici e andamento", LiquidIcons.Star),
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
