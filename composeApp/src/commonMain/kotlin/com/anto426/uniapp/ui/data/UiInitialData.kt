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
import uniapp.composeapp.generated.resources.*

/**
 * Static configuration metadata, legal disclosures, service directory links, and changelogs.
 */
object UiInitialData {
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
        LegalSectionData(Res.string.app_info_sec_7_title, Res.string.app_info_sec_7_content),
        LegalSectionData(Res.string.app_info_sec_8_title, Res.string.app_info_sec_8_content),
        LegalSectionData(Res.string.app_info_sec_9_title, Res.string.app_info_sec_9_content),
        LegalSectionData(Res.string.app_info_sec_10_title, Res.string.app_info_sec_10_content),
        LegalSectionData(Res.string.app_info_sec_11_title, Res.string.app_info_sec_11_content),
        LegalSectionData(Res.string.app_info_sec_12_title, Res.string.app_info_sec_12_content),
        LegalSectionData(Res.string.app_info_sec_13_title, Res.string.app_info_sec_13_content),
        LegalSectionData(Res.string.app_info_sec_14_title, Res.string.app_info_sec_14_content),
        LegalSectionData(Res.string.app_info_sec_15_title, Res.string.app_info_sec_15_content),
        LegalSectionData(Res.string.app_info_sec_16_title, Res.string.app_info_sec_16_content),
        LegalSectionData(Res.string.app_info_sec_17_title, Res.string.app_info_sec_17_content),
        LegalSectionData(Res.string.app_info_sec_18_title, Res.string.app_info_sec_18_content),
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
        LegalSectionData(Res.string.privacy_sec_10_title, Res.string.privacy_sec_10_content),
        LegalSectionData(Res.string.privacy_sec_11_title, Res.string.privacy_sec_11_content),
        LegalSectionData(Res.string.privacy_sec_12_title, Res.string.privacy_sec_12_content),
        LegalSectionData(Res.string.privacy_sec_13_title, Res.string.privacy_sec_13_content),
        LegalSectionData(Res.string.privacy_sec_14_title, Res.string.privacy_sec_14_content),
        LegalSectionData(Res.string.privacy_sec_15_title, Res.string.privacy_sec_15_content),
        LegalSectionData(Res.string.privacy_sec_16_title, Res.string.privacy_sec_16_content),
        LegalSectionData(Res.string.privacy_sec_17_title, Res.string.privacy_sec_17_content),
        LegalSectionData(Res.string.privacy_sec_18_title, Res.string.privacy_sec_18_content),
        LegalSectionData(Res.string.privacy_sec_19_title, Res.string.privacy_sec_19_content),
        LegalSectionData(Res.string.privacy_sec_20_title, Res.string.privacy_sec_20_content),
        LegalSectionData(Res.string.privacy_sec_21_title, Res.string.privacy_sec_21_content),
        LegalSectionData(Res.string.privacy_sec_22_title, Res.string.privacy_sec_22_content),
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
        LegalSectionData(Res.string.terms_sec_9_title, Res.string.terms_sec_9_content),
        LegalSectionData(Res.string.terms_sec_10_title, Res.string.terms_sec_10_content),
        LegalSectionData(Res.string.terms_sec_11_title, Res.string.terms_sec_11_content),
        LegalSectionData(Res.string.terms_sec_12_title, Res.string.terms_sec_12_content),
        LegalSectionData(Res.string.terms_sec_13_title, Res.string.terms_sec_13_content),
        LegalSectionData(Res.string.terms_sec_14_title, Res.string.terms_sec_14_content),
        LegalSectionData(Res.string.terms_sec_15_title, Res.string.terms_sec_15_content),
        LegalSectionData(Res.string.terms_sec_16_title, Res.string.terms_sec_16_content),
        LegalSectionData(Res.string.terms_sec_17_title, Res.string.terms_sec_17_content),
        LegalSectionData(Res.string.terms_sec_18_title, Res.string.terms_sec_18_content),
        LegalSectionData(Res.string.terms_sec_19_title, Res.string.terms_sec_19_content),
        LegalSectionData(Res.string.terms_sec_20_title, Res.string.terms_sec_20_content),
        LegalSectionData(Res.string.terms_sec_21_title, Res.string.terms_sec_21_content),
        LegalSectionData(Res.string.terms_sec_22_title, Res.string.terms_sec_22_content),
        LegalSectionData(Res.string.terms_sec_23_title, Res.string.terms_sec_23_content),
    )

    val cookieSections = listOf(
        LegalSectionData(Res.string.cookie_sec_1_title, Res.string.cookie_sec_1_content),
        LegalSectionData(Res.string.cookie_sec_2_title, Res.string.cookie_sec_2_content),
        LegalSectionData(Res.string.cookie_sec_3_title, Res.string.cookie_sec_3_content),
    )

    val languages = listOf(
        LanguageInfo("Italiano", "Italiano (Predefinito)", "it"),
        LanguageInfo("Inglese", "English (UK)", "en"),
    )

    val palettes = listOf(
        LiquidPaletteOption("Sapphire", Color(0xFF4A90D9)),
        LiquidPaletteOption("Emerald", Color(0xFF2ECC71)),
        LiquidPaletteOption("Sunset", Color(0xFFE67E22)),
        LiquidPaletteOption("Violet", Color(0xFF9B59B6)),
    )
}
