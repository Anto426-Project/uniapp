package com.anto426.uniapp.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.anto426.antoui.components.navigation.AntoExpressiveTopBar
import com.anto426.antoui.components.navigation.AntoFluidNavigationBar
import com.anto426.antoui.components.navigation.AntoNavItemData
import com.anto426.antoui.components.navigation.AntoTopBarAction
import com.anto426.antoui.glass.AntoBackgroundEffect
import com.anto426.antoui.glass.AntoGlassScene
import com.anto426.antoui.glass.AntoLiquidBackground
import com.anto426.antoui.icons.AntoIcons
import com.anto426.antoui.theme.LiquidMonetTheme
import com.anto426.antoui.theme.monet.AntoMonetPresets
import com.anto426.antoui.theme.monet.AntoMonetSeed
import com.anto426.uniapp.android.ui.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { UniAppRoot() }
    }
}

private enum class Destination(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val topLevel: Boolean = false) {
    HOME("Home", AntoIcons.Home, true),
    SERVICES("Servizi", AntoIcons.Star, true),
    DIDACTICS("Didattica", AntoIcons.Calendar, true),
    SETTINGS("Impostazioni", AntoIcons.Settings, true),
    CAREER("Carriera", AntoIcons.Calendar),
    INFO("Informazioni App", AntoIcons.Info),
    THEME("Tema applicazione", AntoIcons.Star),
    COLORS("Laboratorio colori", AntoIcons.Star),
    RGB("Seleziona Colore RGB", AntoIcons.Star)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UniAppRoot() {
    val topTabs = listOf(Destination.HOME, Destination.SERVICES, Destination.DIDACTICS, Destination.SETTINGS)
    var destination by remember { mutableStateOf(Destination.HOME) }
    val seed: AntoMonetSeed = AntoMonetPresets.Sapphire
    LiquidMonetTheme(useMonetEngine = true, liquidIntensity = .82f) {
        AntoGlassScene(
            modifier = Modifier.fillMaxSize(),
            background = { AntoLiquidBackground(effect = AntoBackgroundEffect.Aurora, monetSeed = seed, intensity = .72f, speedFactor = .28f) },
            bottomBar = { backdrop ->
                Box(Modifier.fillMaxWidth().align(Alignment.BottomCenter)) {
                    AntoFluidNavigationBar(
                        selectedIndex = topTabs.indexOf(destination).takeIf { it >= 0 } ?: 0,
                        onItemSelected = { destination = topTabs[it] },
                        items = topTabs.map { AntoNavItemData(it.icon, it.title) },
                        backdropState = backdrop
                    )
                }
            }
        ) { backdrop ->
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent,
                topBar = {
                    AntoExpressiveTopBar(
                        title = if (destination.topLevel) "UniApp" else destination.title,
                        subtitle = if (destination.topLevel) destination.title else "UniApp",
                        compactTitle = true,
                        showNavigationIcon = !destination.topLevel,
                        onNavigationClick = { destination = Destination.SETTINGS },
                        navigationIcon = {
                            if (!destination.topLevel) {
                                Icon(AntoIcons.ArrowBack, contentDescription = "Indietro")
                            }
                        },
                        actionItems = emptyList()
                    )
                }
            ) { padding ->
                Box(Modifier.fillMaxSize().padding(padding)) {
                    when (destination) {
                        Destination.HOME -> HomeScreen(backdrop) { destination = Destination.CAREER }
                        Destination.SERVICES -> ServicesScreen(backdrop)
                        Destination.DIDACTICS -> DidacticsScreen(backdrop)
                        Destination.SETTINGS -> SettingsScreen(backdrop, { destination = Destination.INFO }, { destination = Destination.THEME })
                        Destination.CAREER -> DidacticsScreen(backdrop)
                        Destination.INFO -> AppInfoScreen(backdrop)
                        Destination.THEME -> ThemeScreen(backdrop) { destination = Destination.COLORS }
                        Destination.COLORS -> ColorLabScreen(backdrop) { destination = Destination.RGB }
                        Destination.RGB -> RgbScreen(backdrop)
                    }
                }
            }
        }
    }
}
