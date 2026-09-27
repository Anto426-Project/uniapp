package com.anto426.uniapp.notifications.runtime

import com.anto426.firebase.PushNotificationConnector
import com.anto426.firebase.RemotePushMessage
import com.anto426.uniapp.notifications.model.NotificationAuthorizationStatus
import com.anto426.uniapp.notifications.platform.NotificationPermissionController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AppNotificationManagerTest {
    @Test
    fun backendTokenRequiresCurrentOwnerConsentAndPlatformPermission() {
        val permission = FakePermissionController()
        val manager = AppNotificationManager(FakeConnector(), permission)
        try {
            manager.bindOwner(null, false)
            assertNull(manager.backendToken())

            manager.bindOwner("account-a", true)
            assertEquals("device-token", manager.backendToken())

            manager.bindOwner("account-b", false)
            manager.setEnabled("account-a", true)
            assertNull(manager.backendToken())

            manager.setEnabled("account-b", true)
            assertEquals("device-token", manager.backendToken())
            permission.authorization.value = NotificationAuthorizationStatus.Denied
            assertNull(manager.backendToken())
        } finally {
            manager.close()
        }
    }

    private class FakePermissionController : NotificationPermissionController {
        val authorization = MutableStateFlow(NotificationAuthorizationStatus.Authorized)
        override val authorizationStatus = authorization
        override fun refresh() = Unit
        override fun requestAuthorization() = Unit
        override fun setRegistrationEnabled(enabled: Boolean) = Unit
    }

    private class FakeConnector : PushNotificationConnector {
        override val tokenFlow = MutableStateFlow<String?>("device-token")
        override val messageFlow: Flow<RemotePushMessage> = emptyFlow()
        override suspend fun getDeviceToken(): String? = tokenFlow.value
        override suspend fun subscribeToTopic(topic: String): Result<Unit> = Result.success(Unit)
        override suspend fun unsubscribeFromTopic(topic: String): Result<Unit> = Result.success(Unit)
        override suspend fun deleteToken(): Result<Unit> = Result.success(Unit)
    }
}
