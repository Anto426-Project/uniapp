package com.anto426.uniapp.android.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.anto426.antoui.components.buttons.AntoButton
import com.anto426.antoui.components.buttons.AntoButtonVariant
import com.anto426.antoui.components.cards.AntoPreferenceItem
import com.anto426.antoui.components.navigation.AntoLiquidTabRow
import com.anto426.antoui.components.navigation.AntoTabData
import com.anto426.antoui.icons.AntoIcons
import com.kyant.backdrop.Backdrop

@Composable
fun ServicesScreen(backdropState: Backdrop) { UniScreenColumn {
    UniHeroCard(backdropState, "Servizio in primo piano", "Moodle", "Accedi ai corsi e ai materiali didattici direttamente in app.")
    var selected by remember { mutableIntStateOf(0) }
    AntoLiquidTabRow(
        tabs = listOf(AntoTabData("Servizi"), AntoTabData("Portali")),
        selectedIndex = selected,
        onTabSelected = { selected = it },
        backdropState = backdropState
    )
    AntoButton(onClick = {}, variant = AntoButtonVariant.Glass, backdropState = backdropState) { androidx.compose.material3.Text("Apri Moodle") }
    UniSectionTitle("Accessi veloci")
    AntoPreferenceItem("Rubrica", subtitle = "Cerca contatti e docenti", icon = AntoIcons.AccountCircle, backdropState = backdropState)
    AntoPreferenceItem("Servizi in app", subtitle = "5 servizi disponibili", icon = AntoIcons.Star, backdropState = backdropState)
    AntoPreferenceItem("Portali esterni", subtitle = "5 collegamenti universitari", icon = AntoIcons.Home, backdropState = backdropState)
} }
