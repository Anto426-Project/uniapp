package com.anto426.uniapp.ui.components.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.navigation.navigationbar.LiquidNavigationItem
import com.anto426.liquidmonet.components.navigation.tabbar.LiquidTabBar

/** Keeps the section selector available even when one independently loaded pane fails. */
@Composable
fun UniTabbedScreen(
    tabs: List<LiquidNavigationItem>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    content: @Composable () -> Unit,
) {
    val screenPadding = LocalUniScreenPadding.current
    Column(Modifier.fillMaxSize()) {
        LiquidTabBar(
            items = tabs,
            selectedIndex = selectedTab,
            onTabSelected = onTabSelected,
            modifier = Modifier.padding(start = 18.dp, end = 18.dp,
                top = screenPadding.calculateTopPadding() + 12.dp, bottom = 8.dp),
        )
        Box(Modifier.weight(1f)) {
            CompositionLocalProvider(LocalUniScreenPadding provides PaddingValues(
                bottom = screenPadding.calculateBottomPadding(),
            )) { content() }
        }
    }
}
