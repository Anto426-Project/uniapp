package com.anto426.uniapp.android.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import com.anto426.antoui.components.cards.AntoCard
import com.anto426.antoui.components.cards.AntoPreferenceItem
import com.anto426.antoui.icons.AntoIcons
import com.kyant.backdrop.Backdrop

@Composable
fun AppInfoScreen(backdropState: Backdrop) { UniScreenColumn {
    UniHeroCard(backdropState, "Informazioni App", "UNIAPP", "Area informazioni e sistema")
    AntoCard(backdropState = backdropState) { Column { Text("1.7.3-beta", color = Color.White); Text("VERSIONE · 181 BUILD", color = Color.White.copy(alpha = .7f)) } }
    AntoPreferenceItem("Crediti e Sviluppo", subtitle = "Componenti e librerie utilizzate", icon = AntoIcons.Star, backdropState = backdropState)
    AntoPreferenceItem("Informazioni Legali", subtitle = "Privacy e termini di utilizzo", icon = AntoIcons.Info, backdropState = backdropState)
} }
