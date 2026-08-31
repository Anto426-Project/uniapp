package com.anto426.uniapp.model.services

import androidx.compose.ui.graphics.vector.ImageVector

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
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val badgeCount: Int? = null,
)
