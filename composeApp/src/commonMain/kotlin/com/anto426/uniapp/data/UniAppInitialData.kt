package com.anto426.uniapp.data

import androidx.compose.ui.graphics.Color
import com.anto426.liquidmonet.components.cards.LiquidStatusType
import com.anto426.liquidmonet.components.pickers.LiquidPaletteOption
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.model.home.QuickActionItem
import com.anto426.uniapp.model.legal.LegalSectionData
import com.anto426.uniapp.model.news.NewsItem
import com.anto426.uniapp.model.services.ServiceData
import com.anto426.uniapp.model.settings.LanguageInfo
import com.anto426.uniapp.model.updates.ChangelogItemData
import com.anto426.uniapp.model.updates.ChangelogVersionData
import uniapp.composeapp.generated.resources.*

/**
 * Static configuration metadata, legal disclosures, service directory links, and changelogs.
 */
object UniAppInitialData {
    val fallbackNews = listOf(
        NewsItem(
            title = "Inaugurazione Anno Accademico",
            description = "La cerimonia ufficiale di apertura dell'anno accademico si terrà il 15 Ottobre presso l'Aula Magna con le massime autorità.",
            fullContent = "L'Università è lieta di invitare tutta la comunità accademica alla solenne cerimonia di inaugurazione del nuovo anno accademico. Saranno presenti le massime autorità accademiche, regionali e nazionali. Al termine della prolusione inaugurale del Magnifico Rettore seguirà un rinfresco per tutti gli studenti e il personale.",
            type = LiquidStatusType.Info,
        ),
        NewsItem(
            title = "Bando Borse di Studio e Alloggi 2026/27",
            description = "Pubblicate le graduatorie definitive e le istruzioni per l'erogazione dei contributi economici e posti alloggio per gli studenti idonei.",
            fullContent = "Sono disponibili sul portale dedicato agli studenti le graduatorie definitive relative all'assegnazione di borse di studio e posti alloggio per l'anno accademico in corso. Gli studenti risultati idonei e beneficiari riceveranno una notifica via email con le modalità operative di accredito.",
            type = LiquidStatusType.Success,
        ),
        NewsItem(
            title = "Sessione Straordinaria Appelli d'Esame",
            description = "Apertura delle prenotazioni per gli appelli della sessione straordinaria. Termine ultimo fissato a 5 giorni prima dell'esame.",
            fullContent = "Si avvisano tutti gli studenti che è aperta la finestra di prenotazione per gli appelli della sessione d'esame. Si ricorda che le iscrizioni si chiudono improrogabilmente 5 giorni prima della data della prova d'esame. Consultare l'area 'Appelli' per il calendario completo.",
            type = LiquidStatusType.Warning,
        ),
        NewsItem(
            title = "Workshop AI & Data Science nel Campus",
            description = "Ciclo di seminari pratici e laboratori con aziende partner del settore tecnologico. Riconoscimento CFU per gli iscritti.",
            fullContent = "Il Dipartimento organizza una serie di laboratori applicati sulle moderne tecnologie di intelligenza artificiale, machine learning e analisi avanzata dei dati. I seminari si terranno nei laboratori informatici e permetteranno agli studenti partecipanti di acquisire CFU aggiuntivi.",
            type = LiquidStatusType.Info,
        ),
    )

    val studentServices = listOf(
        ServiceData(titleRes = Res.string.service_student_transport_title, subtitleRes = Res.string.service_student_transport_sub, icon = LiquidIcons.Time, id = "transport"),
        ServiceData(titleRes = Res.string.service_student_taxes_title, subtitleRes = Res.string.service_student_taxes_sub, icon = LiquidIcons.CreditCard, id = "taxes"),
        ServiceData(titleRes = Res.string.service_student_contacts_title, subtitleRes = Res.string.service_student_contacts_sub, icon = LiquidIcons.AccountCircle, id = "contacts"),
        ServiceData(titleRes = Res.string.service_student_office_title, subtitleRes = Res.string.service_student_office_sub, icon = LiquidIcons.Phone, badgeCount = 1, id = "student-office"),
    )

    val universityPortals = listOf(
        ServiceData(titleRes = Res.string.portal_student_esse3_title, subtitleRes = Res.string.portal_student_esse3_sub, icon = LiquidIcons.Badge, id = "esse3"),
        ServiceData(titleRes = Res.string.portal_student_moodle_title, subtitleRes = Res.string.portal_student_moodle_sub, icon = LiquidIcons.MenuBook, id = "moodle"),
        ServiceData(titleRes = Res.string.portal_student_web_title, subtitleRes = Res.string.portal_student_web_sub, icon = LiquidIcons.Home, id = "university-web"),
        ServiceData(titleRes = Res.string.portal_student_email_title, subtitleRes = Res.string.portal_student_email_sub, icon = LiquidIcons.Notifications, badgeCount = 3, id = "email"),
    )

    val professorServices = listOf(
        ServiceData(titleRes = Res.string.service_prof_contacts_title, subtitleRes = Res.string.service_prof_contacts_sub, icon = LiquidIcons.AccountCircle, id = "contacts"),
        ServiceData(titleRes = Res.string.service_prof_teachers_title, subtitleRes = Res.string.service_prof_teachers_sub, icon = LiquidIcons.AccountCircle, id = "professors"),
    )

    val professorPortals = listOf(
        ServiceData(titleRes = Res.string.portal_prof_esse3_title, subtitleRes = Res.string.portal_prof_esse3_sub, icon = LiquidIcons.MenuBook, id = "esse3"),
        ServiceData(titleRes = Res.string.portal_prof_moodle_title, subtitleRes = Res.string.portal_prof_moodle_sub, icon = LiquidIcons.MenuBook, id = "moodle"),
        ServiceData(titleRes = Res.string.portal_prof_web_title, subtitleRes = Res.string.portal_prof_web_sub, icon = LiquidIcons.Home, id = "university-web"),
        ServiceData(titleRes = Res.string.portal_prof_library_title, subtitleRes = Res.string.portal_prof_library_sub, icon = LiquidIcons.MenuBook, id = "library"),
    )

    val allQuickActions = listOf(
        QuickActionItem("libretto", Res.string.quick_action_libretto_title, Res.string.quick_action_libretto_sub, LiquidIcons.MenuBook),
        QuickActionItem("statistiche", Res.string.quick_action_statistiche_title, Res.string.quick_action_statistiche_sub, LiquidIcons.Analytics),
        QuickActionItem("media", Res.string.quick_action_media_title, Res.string.quick_action_media_sub, LiquidIcons.Analytics),
        QuickActionItem("appelli", Res.string.quick_action_appelli_title, Res.string.quick_action_appelli_sub, LiquidIcons.Calendar),
        QuickActionItem("didattica", Res.string.quick_action_didattica_title, Res.string.quick_action_didattica_sub, LiquidIcons.Assignment),
        QuickActionItem("trasporti", Res.string.quick_action_trasporti_title, Res.string.quick_action_trasporti_sub, LiquidIcons.Time),
        QuickActionItem("tasse", Res.string.quick_action_tasse_title, Res.string.quick_action_tasse_sub, LiquidIcons.CreditCard),
        QuickActionItem("rubrica", Res.string.quick_action_rubrica_title, Res.string.quick_action_rubrica_sub, LiquidIcons.AccountCircle),
        QuickActionItem("notifiche", Res.string.quick_action_notifiche_title, Res.string.quick_action_notifiche_sub, LiquidIcons.Notifications),
        QuickActionItem("condivisione", Res.string.quick_action_condivisione_title, Res.string.quick_action_condivisione_sub, LiquidIcons.Share),
        QuickActionItem("sicurezza", Res.string.quick_action_sicurezza_title, Res.string.quick_action_sicurezza_sub, LiquidIcons.Lock),
        QuickActionItem("impostazioni", Res.string.quick_action_impostazioni_title, Res.string.quick_action_impostazioni_sub, LiquidIcons.Settings),
    )

    val professorQuickActions = listOf(
        QuickActionItem("insegnamenti", Res.string.quick_action_prof_insegnamenti_title, Res.string.quick_action_prof_insegnamenti_sub, LiquidIcons.MenuBook),
        QuickActionItem("appelli", Res.string.quick_action_prof_appelli_title, Res.string.quick_action_prof_appelli_sub, LiquidIcons.Calendar),
        QuickActionItem("tesi", Res.string.quick_action_prof_tesi_title, Res.string.quick_action_prof_tesi_sub, LiquidIcons.Assignment),
        QuickActionItem("verbali", Res.string.quick_action_prof_verbali_title, Res.string.quick_action_prof_verbali_sub, LiquidIcons.Edit),
        QuickActionItem("rubrica", Res.string.quick_action_prof_rubrica_title, Res.string.quick_action_prof_rubrica_sub, LiquidIcons.AccountCircle),
        QuickActionItem("notifiche", Res.string.quick_action_prof_notifiche_title, Res.string.quick_action_prof_notifiche_sub, LiquidIcons.Notifications),
    )

    val changelog = listOf(
        ChangelogVersionData("v1.0.0", Res.string.changelog_v1_0_0_date, listOf(
            ChangelogItemData("NEW", Color(0xFF4A90D9), Res.string.changelog_v1_0_0_item_1_title, Res.string.changelog_v1_0_0_item_1_desc),
            ChangelogItemData("NEW", Color(0xFF4A90D9), Res.string.changelog_v1_0_0_item_2_title, Res.string.changelog_v1_0_0_item_2_desc),
            ChangelogItemData("FIXED", Color(0xFF00C853), Res.string.changelog_v1_0_0_item_3_title, Res.string.changelog_v1_0_0_item_3_desc),
        )),
        ChangelogVersionData("v0.9.5", Res.string.changelog_v0_9_5_date, listOf(
            ChangelogItemData("IMPROVED", Color(0xFFFFAB00), Res.string.changelog_v0_9_5_item_1_title, Res.string.changelog_v0_9_5_item_1_desc),
            ChangelogItemData("FIXED", Color(0xFF00C853), Res.string.changelog_v0_9_5_item_2_title, Res.string.changelog_v0_9_5_item_2_desc),
        )),
    )

    val appInfoSections = listOf(
        LegalSectionData(Res.string.app_info_sec_1_title, Res.string.app_info_sec_1_content),
        LegalSectionData(Res.string.app_info_sec_2_title, Res.string.app_info_sec_2_content),
        LegalSectionData(Res.string.app_info_sec_3_title, Res.string.app_info_sec_3_content),
        LegalSectionData(Res.string.app_info_sec_4_title, Res.string.app_info_sec_4_content),
        LegalSectionData(Res.string.app_info_sec_5_title, Res.string.app_info_sec_5_content),
        LegalSectionData(Res.string.app_info_sec_6_title, Res.string.app_info_sec_6_content),
    )

    val privacySections = listOf(
        LegalSectionData(Res.string.privacy_sec_1_title, Res.string.privacy_sec_1_content),
        LegalSectionData(Res.string.privacy_sec_2_title, Res.string.privacy_sec_2_content),
        LegalSectionData(Res.string.privacy_sec_3_title, Res.string.privacy_sec_3_content),
        LegalSectionData(Res.string.privacy_sec_4_title, Res.string.privacy_sec_4_content),
        LegalSectionData(Res.string.privacy_sec_5_title, Res.string.privacy_sec_5_content),
        LegalSectionData(Res.string.privacy_sec_6_title, Res.string.privacy_sec_6_content),
        LegalSectionData(Res.string.privacy_sec_7_title, Res.string.privacy_sec_7_content),
        LegalSectionData(Res.string.privacy_sec_8_title, Res.string.privacy_sec_8_content),
        LegalSectionData(Res.string.privacy_sec_9_title, Res.string.privacy_sec_9_content),
    )

    val termsSections = listOf(
        LegalSectionData(Res.string.terms_sec_1_title, Res.string.terms_sec_1_content),
        LegalSectionData(Res.string.terms_sec_2_title, Res.string.terms_sec_2_content),
        LegalSectionData(Res.string.terms_sec_3_title, Res.string.terms_sec_3_content),
        LegalSectionData(Res.string.terms_sec_4_title, Res.string.terms_sec_4_content),
        LegalSectionData(Res.string.terms_sec_5_title, Res.string.terms_sec_5_content),
        LegalSectionData(Res.string.terms_sec_6_title, Res.string.terms_sec_6_content),
        LegalSectionData(Res.string.terms_sec_7_title, Res.string.terms_sec_7_content),
        LegalSectionData(Res.string.terms_sec_8_title, Res.string.terms_sec_8_content),
    )

    val cookieSections = listOf(
        LegalSectionData(Res.string.cookie_sec_1_title, Res.string.cookie_sec_1_content),
        LegalSectionData(Res.string.cookie_sec_2_title, Res.string.cookie_sec_2_content),
        LegalSectionData(Res.string.cookie_sec_3_title, Res.string.cookie_sec_3_content),
        LegalSectionData(Res.string.cookie_sec_4_title, Res.string.cookie_sec_4_content),
    )

    val languages = listOf(
        LanguageInfo("Italiano", "Italiano (Predefinito)", "it"),
    )

    val palettes = listOf(
        LiquidPaletteOption("Sapphire", Color(0xFF4A90D9)),
        LiquidPaletteOption("Emerald", Color(0xFF2ECC71)),
        LiquidPaletteOption("Sunset", Color(0xFFE67E22)),
        LiquidPaletteOption("Violet", Color(0xFF9B59B6)),
    )
}

typealias UiInitialData = UniAppInitialData
