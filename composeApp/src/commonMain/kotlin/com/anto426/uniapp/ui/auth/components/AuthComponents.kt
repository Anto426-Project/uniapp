package com.anto426.uniapp.ui.auth.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.inputs.otpinput.LiquidOtpInput
import com.anto426.liquidmonet.components.inputs.textfield.LiquidTextField
import com.anto426.liquidmonet.components.inputs.textfield.LiquidTextFieldType
import com.anto426.liquidmonet.components.interaction.liquidClickable
import com.kyant.shapes.RoundedRectangle

@Composable
internal fun PinInputSection(
    label: String,
    pin: String,
    onPinChange: (String) -> Unit,
    length: Int,
    enabled: Boolean,
    onComplete: ((String) -> Unit)? = null,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        )
        LiquidOtpInput(
            otpValue = pin,
            onOtpChange = onPinChange,
            length = length,
            obscureText = true,
            enabled = enabled,
            cellWidth = 42.dp,
            cellHeight = 52.dp,
            spacing = 8.dp,
            onComplete = onComplete,
        )
    }
}

@Composable
internal fun UniAuthLabeledField(
    value: String,
    onValueChange: (String) -> Unit,
    type: LiquidTextFieldType,
    label: String,
    placeholder: String = label,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val fieldShape = remember { RoundedRectangle(16.dp) }
    LiquidTextField(
        value = value,
        onValueChange = onValueChange,
        type = type,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        enabled = enabled,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        shape = fieldShape,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
internal fun AuthFooterLink(
    label: String,
    onClick: () -> Unit,
) {
    val shape = remember { RoundedRectangle(12.dp) }
    Box(
        modifier = Modifier
            .defaultMinSize(minHeight = 48.dp)
            .graphicsLayer(clip = false)
            .liquidClickable(shape = shape, role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
