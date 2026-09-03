package com.anto426.uniapp.notifications.model

enum class NotificationAuthorizationStatus {
    NotDetermined,
    Denied,
    Authorized,
    Provisional,
    Unsupported,
}

data class AppNotificationState(
    val enabled: Boolean = false,
    val authorizationStatus: NotificationAuthorizationStatus = NotificationAuthorizationStatus.NotDetermined,
    val hasRegistrationToken: Boolean = false,
    val errorMessage: String? = null,
)
