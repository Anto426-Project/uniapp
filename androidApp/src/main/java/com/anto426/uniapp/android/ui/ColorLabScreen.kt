package com.anto426.uniapp.android.ui

import androidx.compose.runtime.Composable
import com.anto426.antoui.components.cards.AntoPreferenceItem
import com.anto426.antoui.icons.AntoIcons
import com.kyant.backdrop.Backdrop

@Composable
fun ColorLabScreen(backdropState: Backdrop, onOpenRgb: () -> Unit) { UniScreenColumn {
    UniSectionTitle("Laboratorio colori", "Scegli una palette o crea il tuo tema")
    AntoPreferenceItem("Colori dinamici basati sul sistema", subtitle = "Palette automatica", icon = AntoIcons.Star, backdropState = backdropState)
    AntoPreferenceItem("Avatar Violet", subtitle = "Anime Style · Ombre Viola", icon = AntoIcons.Star, backdropState = backdropState)
    AntoPreferenceItem("University", subtitle = "Istituzionale · Blu e Oro", icon = AntoIcons.Home, backdropState = backdropState)
    AntoPreferenceItem("Custom Lab", subtitle = "Crea la tua combinazione", icon = AntoIcons.Settings, onClick = onOpenRgb, backdropState = backdropState)
} }
