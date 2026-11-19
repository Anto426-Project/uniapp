package com.anto426.uniapp.data

import org.jetbrains.compose.resources.getString

import androidx.compose.ui.graphics.Color
import com.anto426.liquidmonet.components.pickers.paletteselector.LiquidPaletteOption
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.model.home.QuickActionItem
import com.anto426.uniapp.model.legal.LegalSectionData
import com.anto426.uniapp.model.news.NewsItem
import com.anto426.uniapp.model.services.ServiceData
import com.anto426.uniapp.model.settings.LanguageInfo
import uniapp.composeapp.generated.resources.*

/**
 * Static configuration metadata, legal disclosures, service directory links, and changelogs.
 */
object UniAppInitialData {
    val fallbackNews = emptyList<NewsItem>()

    val studentServices = listOf(
        ServiceData(titleRes = Res.string.service_student_transport_title, subtitleRes = Res.string.service_student_transport_sub, icon = LiquidIcons.Time, id = "transport"),
        ServiceData(titleRes = Res.string.service_student_taxes_title, subtitleRes = Res.string.service_student_taxes_sub, icon = LiquidIcons.CreditCard, id = "taxes"),
        ServiceData(titleRes = Res.string.service_student_contacts_title, subtitleRes = Res.string.service_student_contacts_sub, icon = LiquidIcons.AccountCircle, id = "contacts"),
        ServiceData(titleRes = Res.string.nav_route_news_title, subtitleRes = Res.string.nav_route_news_subtitle, icon = LiquidIcons.Notifications, id = "news"),
        ServiceData(
            titleRes = Res.string.portal_prof_library_title,
            subtitleRes = Res.string.portal_prof_library_sub,
            icon = LiquidIcons.MenuBook,
            id = "library",
        ),
    )

    val universityPortals = listOf(
        ServiceData(titleRes = Res.string.portal_student_esse3_title, subtitleRes = Res.string.portal_student_esse3_sub, icon = LiquidIcons.Badge, id = "esse3"),
        ServiceData(titleRes = Res.string.portal_student_moodle_title, subtitleRes = Res.string.portal_student_moodle_sub, icon = LiquidIcons.MenuBook, id = "moodle"),
        ServiceData(titleRes = Res.string.portal_student_web_title, subtitleRes = Res.string.portal_student_web_sub, icon = LiquidIcons.Home, id = "university-web"),
        ServiceData(titleRes = Res.string.service_student_office_title, subtitleRes = Res.string.service_student_office_sub, icon = LiquidIcons.Assignment, id = "student-office"),
        ServiceData(titleRes = Res.string.portal_student_email_title, subtitleRes = Res.string.portal_student_email_sub, icon = LiquidIcons.Notifications, id = "email"),
    )

    val professorServices = listOf(
        ServiceData(titleRes = Res.string.service_prof_contacts_title, subtitleRes = Res.string.service_prof_contacts_sub, icon = LiquidIcons.AccountCircle, id = "contacts"),
        ServiceData(
            titleRes = Res.string.portal_prof_library_title,
            subtitleRes = Res.string.portal_prof_library_sub,
            icon = LiquidIcons.MenuBook,
            id = "library",
        ),
    )

    val professorPortals = listOf(
        ServiceData(titleRes = Res.string.portal_prof_esse3_title, subtitleRes = Res.string.portal_prof_esse3_sub, icon = LiquidIcons.MenuBook, id = "esse3"),
        ServiceData(titleRes = Res.string.portal_prof_moodle_title, subtitleRes = Res.string.portal_prof_moodle_sub, icon = LiquidIcons.MenuBook, id = "moodle"),
        ServiceData(titleRes = Res.string.portal_prof_web_title, subtitleRes = Res.string.portal_prof_web_sub, icon = LiquidIcons.Home, id = "university-web"),
    )

    val allQuickActions = listOf(
        QuickActionItem("libretto", Res.string.quick_action_libretto_title, Res.string.quick_action_libretto_sub, LiquidIcons.MenuBook),
        QuickActionItem("statistiche", Res.string.quick_action_statistiche_title, Res.string.quick_action_statistiche_sub, LiquidIcons.Analytics),
        QuickActionItem("appelli", Res.string.quick_action_appelli_title, Res.string.quick_action_appelli_sub, LiquidIcons.Calendar),
        QuickActionItem("didattica", Res.string.quick_action_didattica_title, Res.string.quick_action_didattica_sub, LiquidIcons.Assignment),
        QuickActionItem("trasporti", Res.string.quick_action_trasporti_title, Res.string.quick_action_trasporti_sub, LiquidIcons.Time),
        QuickActionItem("tasse", Res.string.quick_action_tasse_title, Res.string.quick_action_tasse_sub, LiquidIcons.CreditCard),
        QuickActionItem("rubrica", Res.string.quick_action_rubrica_title, Res.string.quick_action_rubrica_sub, LiquidIcons.AccountCircle),
        QuickActionItem("notifiche", Res.string.nav_route_news_title, Res.string.nav_route_news_subtitle, LiquidIcons.Notifications),
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
        QuickActionItem("notifiche", Res.string.nav_route_news_title, Res.string.nav_route_news_subtitle, LiquidIcons.Notifications),
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
        LanguageInfo(Res.string.ui_course_lang_italian, Res.string.ui_language_italian_default, "it"),
        LanguageInfo(Res.string.ui_language_english, Res.string.ui_language_english_region, "en"),
        LanguageInfo(Res.string.ui_language_french, Res.string.ui_language_french_region, "fr"),
        LanguageInfo(Res.string.ui_language_spanish, Res.string.ui_language_spanish_region, "es"),
        LanguageInfo(Res.string.ui_language_german, Res.string.ui_language_german_region, "de"),
        LanguageInfo(Res.string.ui_language_portuguese, Res.string.ui_language_portuguese_region, "pt"),
        LanguageInfo(Res.string.ui_language_dutch, Res.string.ui_language_dutch_region, "nl"),
        LanguageInfo(Res.string.ui_language_polish, Res.string.ui_language_polish_region, "pl"),
        LanguageInfo(Res.string.ui_language_chinese, Res.string.ui_language_chinese_region, "zh"),
        LanguageInfo(Res.string.ui_language_japanese, Res.string.ui_language_japanese_region, "ja"),
    )

    val palettes = listOf(
        LiquidPaletteOption("Sapphire", Color(0xFF4A90D9)),
        LiquidPaletteOption("Emerald", Color(0xFF2ECC71)),
        LiquidPaletteOption("Sunset", Color(0xFFE67E22)),
        LiquidPaletteOption("Violet", Color(0xFF9B59B6)),
    )
}

typealias UiInitialData = UniAppInitialData
