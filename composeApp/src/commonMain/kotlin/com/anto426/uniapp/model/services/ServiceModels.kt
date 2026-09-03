package com.anto426.uniapp.model.services

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

enum class ContactCategory {
    TEACHERS,
    SECRETARIAT,
    SERVICES
}

data class ContactData(
    val name: String,
    val role: String,
    val initials: String,
    val email: String = "",
    val phone: String = "",
    val category: ContactCategory = ContactCategory.TEACHERS,
    val department: String = "",
    val office: String = "",
    val officeHours: String = "",
)

data class TaxPaymentData(
    val title: String,
    val date: String,
    val amount: String,
    val isPaid: Boolean,
    val iuv: String,
)

data class ServiceData(
    val titleRes: StringResource? = null,
    val subtitleRes: StringResource? = null,
    val rawTitle: String = "",
    val rawSubtitle: String = "",
    val icon: ImageVector,
    val badgeCount: Int? = null,
    val id: String = "",
) {
    constructor(
        title: String,
        subtitle: String,
        icon: ImageVector,
        badgeCount: Int? = null,
        id: String = "",
    ) : this(null, null, title, subtitle, icon, badgeCount, id)

    constructor(
        titleRes: StringResource,
        subtitleRes: StringResource,
        icon: ImageVector,
        badgeCount: Int? = null,
        id: String = "",
    ) : this(titleRes, subtitleRes, "", "", icon, badgeCount, id)

    val title: String
        @Composable get() = titleRes?.let { stringResource(it) } ?: rawTitle

    val subtitle: String
        @Composable get() = subtitleRes?.let { stringResource(it) } ?: rawSubtitle
}
