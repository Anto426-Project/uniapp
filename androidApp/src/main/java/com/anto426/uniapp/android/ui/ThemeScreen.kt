package com.anto426.uniapp.android.ui

import androidx.compose.runtime.Composable
import com.anto426.antoui.components.cards.AntoPreferenceItem
import com.anto426.antoui.icons.AntoIcons
import com.kyant.backdrop.Backdrop

@Composable
fun ThemeScreen(backdropState: Backdrop, onOpenColors: () -> Unit) { UniScreenColumn {
    UniHeroCard(backdropState, "Tema applicazione", "Tema e Aspetto", "Personalizza l'interfaccia di UniApp come preferisci.")
    UniSectionTitle("Esperienza visiva")
    AntoPreferenceItem("Sistema", subtitle = "Segui il tema del dispositivo", icon = AntoIcons.Settings, backdropState = backdropState)
    AntoPreferenceItem("Chiaro", subtitle = "Interfaccia luminosa", icon = AntoIcons.Home, backdropState = backdropState)
    AntoPreferenceItem("Scuro", subtitle = "Interfaccia scura", icon = AntoIcons.Star, backdropState = backdropState)
    UniSectionTitle("Personalità cromatica")
    AntoPreferenceItem("Laboratorio colori", subtitle = "Crea la tua combinazione", icon = AntoIcons.Star, onClick = onOpenColors, backdropState = backdropState)
} }
