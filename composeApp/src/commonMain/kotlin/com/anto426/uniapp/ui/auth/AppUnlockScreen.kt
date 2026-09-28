package com.anto426.uniapp.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.buttons.button.LiquidButton
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonVariant
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.display.avatar.LiquidAvatar
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.inputs.textfield.LiquidTextField
import com.anto426.liquidmonet.components.inputs.textfield.LiquidTextFieldType
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.session.presentation.AppPasswordUnlockError
import com.anto426.uniapp.session.presentation.AppUnlockUiState
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.kyant.shapes.RoundedRectangle
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

/**
 * Schermata di Sblocco dell'Applicazione.
 *
 * Ispirata visivamente e strutturalmente a [LoginScreen]:
 * 1. Brand Presentation Header con LiquidAvatar (icona Lock Monet), nome app e messaggio di benvenuto.
 * 2. Master Glass Card con autenticazione del dispositivo e password UniApp.
 * 3. Messaggi di errore e azione rapida per cambio account.
 */
@Composable
internal fun AppUnlockScreen(
    accountName: String?,
    accountId: String?,
    unlockUiState: AppUnlockUiState,
    onRequestUnlock: () -> Unit,
    onCancelUnlock: () -> Unit,
    onPasswordUnlock: (String) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val canUnlock = !accountId.isNullOrBlank()
    var password by remember(accountId) { mutableStateOf("") }
    val submitPassword = {
        if (canUnlock && !unlockUiState.isAuthenticating && password.isNotEmpty()) {
            onPasswordUnlock(password)
            password = ""
        }
    }

    UniScreenColumn(modifier = Modifier.imePadding()) {
        Spacer(Modifier.height(12.dp))

        // 1. Brand Presentation Header (Stile LoginScreen)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            LiquidAvatar(
                size = 80.dp,
                icon = LiquidIcons.Lock,
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stringResource(Res.string.ui_app_name),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.8).sp,
                        fontSize = 32.sp,
                    ),
                    color = colorScheme.onSurface,
                )

                Text(
                    text = stringResource(Res.string.ui_unlock_title),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = colorScheme.primary,
                )

                Text(
                    text = if (!accountName.isNullOrBlank()) {
                        stringResource(Res.string.ui_unlock_account, accountName)
                    } else {
                        stringResource(Res.string.ui_unlock_methods)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }

        // 2. Master Glass Authentication Card
        LiquidCard(
            shape = RoundedRectangle(28.dp),
            contentPadding = 24.dp,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                // Header Card con Titolo e Badge Sicurezza
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(Res.string.ui_unlock_protected_access),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = colorScheme.onSurface,
                    )
                    LiquidBadge(
                        text = stringResource(Res.string.ui_unlock_security_badge),
                        containerColor = colorScheme.primaryContainer.copy(alpha = 0.5f),
                        contentColor = colorScheme.primary,
                    )
                }

                // Messaggio di Errore o Stato Autenticazione (esclude 'Accesso annullato' mostrato via toast)
                val errorMessage = unlockUiState.errorMessage
                val cancelMessage = stringResource(Res.string.msg_accesso_annullato)
                if (errorMessage != null && !errorMessage.equals(cancelMessage, ignoreCase = true)) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                // Sezione Password UniApp
                LiquidTextField(
                    value = password,
                    onValueChange = { password = it },
                    type = LiquidTextFieldType.Password,
                    label = stringResource(Res.string.ui_app_password_label),
                    enabled = canUnlock && !unlockUiState.isAuthenticating,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { submitPassword() }),
                )

                unlockUiState.passwordError?.let { error ->
                    Text(
                        text = stringResource(when (error) {
                            AppPasswordUnlockError.Invalid -> Res.string.ui_app_password_invalid
                            AppPasswordUnlockError.RetryLater -> Res.string.ui_app_password_retry_later
                            AppPasswordUnlockError.Failed -> Res.string.ui_app_password_unlock_failed
                        }),
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.error,
                    )
                }

                // Pulsante Sblocca con password
                LiquidButton(
                    text = stringResource(Res.string.ui_app_password_unlock),
                    onClick = submitPassword,
                    enabled = canUnlock && !unlockUiState.isAuthenticating && password.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                    variant = LiquidButtonVariant.Primary,
                )

                // Pulsante Sblocca con il dispositivo in basso insieme a sblocca con password
                LiquidButton(
                    text = if (unlockUiState.isAuthenticating) {
                        stringResource(Res.string.ui_unlock_authenticating)
                    } else {
                        stringResource(Res.string.ui_unlock_action)
                    },
                    onClick = onRequestUnlock,
                    enabled = canUnlock && !unlockUiState.isAuthenticating,
                    isLoading = unlockUiState.isAuthenticating,
                    leadingIcon = {
                        Icon(
                            imageVector = LiquidIcons.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    variant = LiquidButtonVariant.Secondary,
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // 3. Azione Alternativa: Usa un altro account
        LiquidButton(
            text = stringResource(Res.string.ui_unlock_other_account),
            onClick = onCancelUnlock,
            enabled = canUnlock && !unlockUiState.isAuthenticating,
            modifier = Modifier.fillMaxWidth(),
            variant = LiquidButtonVariant.Text,
        )
    }
}
