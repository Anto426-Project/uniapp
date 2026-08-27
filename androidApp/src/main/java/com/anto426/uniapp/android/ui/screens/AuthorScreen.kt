package com.anto426.uniapp.android.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.LiquidPreferenceGroup
import com.anto426.liquidmonet.components.cards.LiquidPreferenceItem
import com.anto426.liquidmonet.components.display.LiquidAvatar
import com.anto426.liquidmonet.components.display.LiquidHorizontalDivider
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.android.R
import com.anto426.uniapp.android.ui.components.layout.UniHeroCard
import com.anto426.uniapp.android.ui.components.layout.UniScreenColumn
import com.kyant.backdrop.Backdrop

@Composable
fun AuthorScreen(backdropState: Backdrop) {
    UniScreenColumn {
        // 1. Author Hero
        UniHeroCard(
            backdropState = backdropState,
            eyebrow = stringResource(R.string.ui_author),
            title = "Anto426",
            subtitle = stringResource(R.string.ui_author_role),
            leadingContent = {
                LiquidAvatar(
                    initials = "AN",
                    size = 64.dp,
                    backdropState = backdropState
                )
            }
        )

        // 2. Social & Links
        LiquidPreferenceGroup(title = "Links", backdropState = backdropState) {
            LiquidPreferenceItem(
                title = stringResource(R.string.ui_github),
                subtitle = "@Anto426",
                icon = LiquidIcons.Share,
                backdropState = backdropState,
                onClick = {}
            )
            LiquidHorizontalDivider()
            LiquidPreferenceItem(
                title = stringResource(R.string.ui_linkedin),
                subtitle = "Antonio",
                icon = LiquidIcons.AccountCircle,
                backdropState = backdropState,
                onClick = {}
            )
            LiquidHorizontalDivider()
            LiquidPreferenceItem(
                title = stringResource(R.string.ui_portfolio),
                subtitle = "anto426.dev",
                icon = LiquidIcons.Search,
                backdropState = backdropState,
                onClick = {}
            )
        }

        // 3. Location info
        LiquidPreferenceGroup(backdropState = backdropState) {
            LiquidPreferenceItem(
                title = "Location",
                subtitle = stringResource(R.string.ui_author_location),
                icon = LiquidIcons.Home,
                backdropState = backdropState
            )
        }
    }
}
