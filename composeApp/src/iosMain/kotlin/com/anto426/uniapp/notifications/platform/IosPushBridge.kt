package com.anto426.uniapp.notifications.platform

import com.anto426.firebase.cacheIosPushToken
import com.anto426.firebase.clearIosPushToken
import com.anto426.firebase.emitIosPushMessage

/** Entry points called by the Swift UIApplicationDelegate. */
fun cachePushNotificationsTokenFromIos(token: String) {
    cacheIosPushToken(token)
}

fun clearPushNotificationsTokenFromIos() {
    clearIosPushToken()
}

fun cacheRemotePushNotificationFromIos(
    id: String?,
    title: String?,
    body: String?,
) {
    emitIosPushMessage(
        id = id,
        title = title,
        body = body,
    )
}
