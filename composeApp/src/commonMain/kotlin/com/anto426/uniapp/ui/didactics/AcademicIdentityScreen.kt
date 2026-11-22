package com.anto426.uniapp.ui.didactics

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.uniapp.didactics.presentation.AcademicIdentityUiState
import com.anto426.uniapp.ui.components.display.UniInsetGroup
import com.anto426.uniapp.ui.components.display.UniInsetRow
import com.anto426.uniapp.ui.components.layout.UniAnimatedEntrance
import com.anto426.uniapp.ui.components.layout.UniScreenColumn
import com.anto426.uniapp.ui.didactics.components.AcademicIdentityBannerCard
import org.jetbrains.compose.resources.stringResource
import uniapp.composeapp.generated.resources.*

@Composable
fun AcademicIdentityScreen(uiState: AcademicIdentityUiState) {
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors

    val detailRows = if (uiState.isProfessor) {
        listOf(
            Triple(
                stringResource(Res.string.ui_professor_username),
                uiState.username.ifBlank { "—" },
                LiquidIcons.AccountCircle to colorScheme.primary,
            ),
            Triple(
                stringResource(Res.string.ui_professor_id),
                uiState.teacherId.ifBlank { "—" },
                LiquidIcons.Badge to colorScheme.secondary,
            ),
            Triple(
                stringResource(Res.string.ui_department_label),
                uiState.departmentName.ifBlank { stringResource(Res.string.ui_university) },
                LiquidIcons.MenuBook to colorScheme.tertiary,
            ),
            Triple(
                stringResource(Res.string.ui_professor_department_id),
                uiState.departmentId.ifBlank { "—" },
                LiquidIcons.Info to colorScheme.primary,
            ),
        )
    } else {
        listOf(
            Triple(
                stringResource(Res.string.ui_matricola),
                uiState.matricola.ifBlank { "—" },
                LiquidIcons.Badge to colorScheme.primary,
            ),
            Triple(
                stringResource(Res.string.ui_degree_label),
                uiState.degreeName.ifBlank { "—" },
                LiquidIcons.MenuBook to colorScheme.secondary,
            ),
            Triple(
                stringResource(Res.string.ui_department_label),
                uiState.departmentName.ifBlank { stringResource(Res.string.ui_university) },
                LiquidIcons.Info to colorScheme.tertiary,
            ),
            Triple(
                stringResource(Res.string.ui_enrollment_status),
                stringResource(Res.string.ui_student_status),
                LiquidIcons.Check to glassColors.success,
            ),
        )
    }

    UniScreenColumn {
        // 1. Hero Tessera Digitale (3D Flip Front/Back)
        UniAnimatedEntrance(order = 0) {
            AcademicIdentityBannerCard(
                uiState = uiState,
            )
        }

        // 2. Dettagli Accademici
        UniAnimatedEntrance(order = 1) {
            LiquidSectionHeader(
                title = stringResource(
                    if (uiState.isProfessor) Res.string.ui_professor_id_details_title
                    else Res.string.ui_student_id_details_title,
                ),
            )
        }

        UniInsetGroup(entranceOrder = 2) {
            detailRows.forEachIndexed { index, (title, value, iconPair) ->
                UniInsetRow(
                    title = title,
                    subtitle = value,
                    icon = iconPair.first,
                    iconTint = iconPair.second,
                    showDivider = index < detailRows.lastIndex,
                )
            }
        }
    }
}
