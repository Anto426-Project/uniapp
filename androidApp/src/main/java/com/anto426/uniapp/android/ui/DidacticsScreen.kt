package com.anto426.uniapp.android.ui

import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import com.anto426.antoui.components.feedback.AntoLinearProgressIndicator
import com.anto426.antoui.components.cards.AntoPreferenceItem
import com.anto426.antoui.icons.AntoIcons
import com.kyant.backdrop.Backdrop

@Composable
fun DidacticsScreen(backdropState: Backdrop) { UniScreenColumn {
    UniHeroCard(backdropState, "Progresso accademico", "PROGRESSO\nACCADEMICO", "9% completato")
    Text("Il tuo percorso", color = androidx.compose.ui.graphics.Color.White)
    AntoLinearProgressIndicator(progress = .09f, backdropState = backdropState)
    AntoPreferenceItem("Gestione Carriera", subtitle = "Esami, piano di studi e crediti", icon = AntoIcons.Calendar, backdropState = backdropState)
    AntoPreferenceItem("Appelli aperti", subtitle = "0 prenotazioni disponibili", icon = AntoIcons.Star, backdropState = backdropState)
} }
