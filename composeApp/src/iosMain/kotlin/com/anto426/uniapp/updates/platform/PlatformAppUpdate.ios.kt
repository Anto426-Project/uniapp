@file:OptIn(kotlin.experimental.ExperimentalNativeApi::class)

package com.anto426.uniapp.updates.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.anto426.uniapp.app.info.AppBuildMetadata
import com.anto426.unisdk.platform.AppInfo
import com.anto426.unisdk.platform.AppInfoProvider
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSBundle
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIDevice

@Composable
internal actual fun rememberPlatformAppUpdateEnvironment(): PlatformAppUpdateEnvironment =
    remember {
        val bundle = NSBundle.mainBundle
        val storeUrl = AppBuildMetadata.appStoreUrl.trim().takeIf { raw ->
            val url = NSURL.URLWithString(raw)
            url?.scheme == "https" && url.host == "apps.apple.com" && url.path?.contains("/id") == true
        }
        val info = AppInfo(
            versionName = (bundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String)
                ?.takeIf(String::isNotBlank) ?: "unknown",
            versionCode = (bundle.objectForInfoDictionaryKey("CFBundleVersion") as? String)?.toIntOrNull(),
            isDebuggable = kotlin.native.Platform.isDebugBinary,
            applicationId = bundle.bundleIdentifier.orEmpty(),
            platform = "ios",
            osVersion = "${UIDevice.currentDevice.systemName} ${UIDevice.currentDevice.systemVersion}",
            deviceModel = UIDevice.currentDevice.model,
            sourceRevision = AppBuildMetadata.sourceRevision,
            appStoreUrl = storeUrl,
            modules = AppBuildMetadata.modules,
        )
        AppInfoProvider.initialize(info)
        PlatformAppUpdateEnvironment(
            installedBuild = info,
            launcher = PlatformUpdateLauncher { _, _ ->
                if (storeUrl == null) {
                    PlatformUpdateLaunchResult.Failed("Configura UNIAPP_APP_STORE_URL per aprire l’App Store.")
                } else {
                    val opened = suspendCancellableCoroutine<Boolean> { continuation ->
                        UIApplication.sharedApplication.openURL(NSURL.URLWithString(storeUrl)!!,
                            options = emptyMap<Any?, Any?>()) { success ->
                            if (continuation.isActive) continuation.resume(success)
                        }
                    }
                    if (opened) PlatformUpdateLaunchResult.OpenedExternalStore
                    else PlatformUpdateLaunchResult.Failed("Impossibile aprire l’App Store.")
                }
            },
        )
    }
