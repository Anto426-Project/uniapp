package com.anto426.uniapp.ui.didactics

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.LiquidPreferenceGroup
import com.anto426.liquidmonet.components.cards.LiquidPreferenceItem
import com.anto426.liquidmonet.components.display.LiquidHorizontalDivider
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.uniapp.didactics.presentation.AcademicIdentityUiState
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.didactics.components.AcademicIdentityBannerCard
import com.kyant.backdrop.Backdrop
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun AcademicIdentityScreen(backdropState: Backdrop, uiState: AcademicIdentityUiState) {
    val rawCode = uiState.badgeDisplayValue.ifBlank {
        uiState.matricola.ifBlank { "—" }
    }

    UniScreenColumn {
        // ==========================================
        // 1. HERO TESSERA STUDENTE (BANNER STYLE - NESSUN DOPPIO TITOLO)
        // ==========================================
        AcademicIdentityBannerCard(
            uiState = uiState,
            rawCode = rawCode,
            backdropState = backdropState,
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ==========================================
        // 2. GRUPPO DETTAGLI ACCADEMICI (PREFERENCE GROUP STYLE)
        // ==========================================
        LiquidPreferenceGroup(
            title = stringResource(
                if (uiState.isProfessor) Res.string.ui_professor_id_details_title
                else Res.string.ui_student_id_details_title,
            ),
            backdropState = backdropState,
        ) {
            if (uiState.isProfessor) {
                LiquidPreferenceItem(
                    title = stringResource(Res.string.ui_professor_username),
                    subtitle = uiState.username.ifBlank { "—" },
                    icon = LiquidIcons.AccountCircle,
                    backdropState = backdropState,
                )

                LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))

                LiquidPreferenceItem(
                    title = stringResource(Res.string.ui_professor_id),
                    subtitle = uiState.teacherId.ifBlank { "—" },
                    icon = LiquidIcons.Badge,
                    backdropState = backdropState,
                )

                LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))

                LiquidPreferenceItem(
                    title = stringResource(Res.string.ui_department_label),
                    subtitle = uiState.departmentName.ifBlank { stringResource(Res.string.ui_university) },
                    icon = LiquidIcons.MenuBook,
                    backdropState = backdropState,
                )

                LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))

                LiquidPreferenceItem(
                    title = stringResource(Res.string.ui_professor_department_id),
                    subtitle = uiState.departmentId.ifBlank { "—" },
                    icon = LiquidIcons.Info,
                    backdropState = backdropState,
                )
            } else {
                LiquidPreferenceItem(
                    title = stringResource(Res.string.ui_matricola),
                    subtitle = uiState.matricola.ifBlank { "—" },
                    icon = LiquidIcons.Badge,
                    backdropState = backdropState,
                )

                LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))

                LiquidPreferenceItem(
                    title = stringResource(Res.string.ui_degree_label),
                    subtitle = uiState.degreeName.ifBlank { "—" },
                    icon = LiquidIcons.MenuBook,
                    backdropState = backdropState,
                )

                LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))

                LiquidPreferenceItem(
                    title = stringResource(Res.string.ui_department_label),
                    subtitle = uiState.departmentName.ifBlank { stringResource(Res.string.ui_university) },
                    icon = LiquidIcons.Info,
                    backdropState = backdropState,
                )

                LiquidHorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))

                LiquidPreferenceItem(
                    title = stringResource(Res.string.ui_enrollment_status),
                    subtitle = stringResource(Res.string.ui_student_status),
                    icon = LiquidIcons.Check,
                    backdropState = backdropState,
                )
            }
        }
    }
}
