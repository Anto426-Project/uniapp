package com.anto426.uniapp.ui.updates

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.preferenceitem.LiquidPreferenceItem
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.model.updates.UpdateState
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

internal enum class PackageVerificationStatus { Pending, Downloading, Checking, Passed }

internal fun UpdateState.packageVerificationStatus(): PackageVerificationStatus = when (this) {
    UpdateState.DOWNLOADING -> PackageVerificationStatus.Downloading
    UpdateState.VERIFYING -> PackageVerificationStatus.Checking
    UpdateState.INSTALLING -> PackageVerificationStatus.Passed
    else -> PackageVerificationStatus.Pending
}

/** Reflects the Android updater's real download, validation and install phases. */
@Composable
internal fun UpdatePackageVerificationItem(state: UpdateState) {
    val status = state.packageVerificationStatus()
    LiquidPreferenceItem(
        title = stringResource(Res.string.ui_update_verification_title),
        subtitle = when (status) {
            PackageVerificationStatus.Pending -> stringResource(Res.string.ui_update_verification_pending)
            PackageVerificationStatus.Downloading -> stringResource(Res.string.ui_update_verification_downloading)
            PackageVerificationStatus.Checking -> stringResource(Res.string.ui_update_verification_running)
            PackageVerificationStatus.Passed -> stringResource(Res.string.ui_update_verification_passed)
        },
        icon = LiquidIcons.Lock,
        trailingContent = {
            when (status) {
                PackageVerificationStatus.Checking -> CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                )
                PackageVerificationStatus.Passed -> Icon(
                    imageVector = LiquidIcons.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
                else -> Unit
            }
        },
    )
}
