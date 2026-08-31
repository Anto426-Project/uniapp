package com.anto426.uniapp.ui.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.buttons.LiquidButton
import com.anto426.liquidmonet.components.buttons.LiquidButtonVariant
import com.anto426.liquidmonet.components.cards.LiquidCard
import com.anto426.liquidmonet.components.display.LiquidBadge
import com.anto426.liquidmonet.components.display.LiquidSectionHeader
import com.anto426.uniapp.account.presentation.AccountSwitcherUiState
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.kyant.backdrop.Backdrop

@Composable
internal fun AccountSwitcherScreen(
    backdropState: Backdrop,
    uiState: AccountSwitcherUiState,
    onSelectAccount: (String) -> Unit,
    onAddAccount: () -> Unit,
) {
    UniScreenColumn {
        LiquidSectionHeader(
            title = "Account salvati",
            subtitle = "Ogni account usa un archivio cifrato separato",
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            uiState.accounts.forEach { account ->
                val isActive = account.accountId == uiState.activeAccountId
                LiquidCard(
                    backdropState = backdropState,
                    onClick = if (isActive) null else ({ onSelectAccount(account.accountId) }),
                    interactiveGelatin = !isActive,
                    contentPadding = 16.dp,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = account.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = account.degreeName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            account.matricola?.let { matricola ->
                                Text(
                                    text = "matr. $matricola",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        if (isActive) {
                            LiquidBadge(
                                text = "ATTIVO",
                                backdropState = backdropState,
                            )
                        }
                    }
                }
            }
        }

        LiquidButton(
            text = "Aggiungi account",
            onClick = onAddAccount,
            variant = LiquidButtonVariant.Primary,
            backdropState = backdropState,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
