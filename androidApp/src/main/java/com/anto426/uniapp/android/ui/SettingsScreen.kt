package com.anto426.uniapp.android.ui

import androidx.compose.runtime.Composable
import com.anto426.antoui.components.cards.AntoPreferenceItem
import com.anto426.antoui.components.selection.AntoSwitch
import com.anto426.antoui.icons.AntoIcons
import com.kyant.backdrop.Backdrop

@Composable
fun SettingsScreen(backdropState: Backdrop, onOpenInfo: () -> Unit, onOpenTheme: () -> Unit) { UniScreenColumn {
    UniHeroCard(backdropState, "Account e accessi", "Il tuo spazio\npersonale", "Hai un account salvato e puoi controllare accessi e dispositivi attivi.")
    UniSectionTitle("Account")
    AntoPreferenceItem("Cambia account", subtitle = "Passa a un profilo salvato", icon = AntoIcons.AccountCircle, backdropState = backdropState)
    AntoPreferenceItem("Dispositivi attivi", subtitle = "Gestisci i tuoi accessi", icon = AntoIcons.Home, backdropState = backdropState, trailingContent = { AntoSwitch(true, {}, backdropState = backdropState) })
    UniSectionTitle("Impostazioni")
    AntoPreferenceItem("Tema applicazione", subtitle = "Personalizza colori e aspetto", icon = AntoIcons.Star, onClick = onOpenTheme, backdropState = backdropState)
    AntoPreferenceItem("Informazioni App", subtitle = "Versione, build e informazioni legali", icon = AntoIcons.Info, onClick = onOpenInfo, backdropState = backdropState)
} }
