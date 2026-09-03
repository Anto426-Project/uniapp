package com.anto426.uniapp.updates.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.anto426.uniapp.updates.model.InstalledAppBuild
import platform.Foundation.NSBundle
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

@Composable
internal actual fun rememberPlatformAppUpdateEnvironment(): PlatformAppUpdateEnvironment =
    remember {
        val bundle = NSBundle.mainBundle
        val versionName =
            (bundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String)
                ?.takeIf { it.isNotBlank() }
                ?: "unknown"
        val versionCode =
            (bundle.objectForInfoDictionaryKey("CFBundleVersion") as? String)?.toIntOrNull()
        PlatformAppUpdateEnvironment(
            installedBuild = InstalledAppBuild(versionName, versionCode),
            launcher =
                PlatformUpdateLauncher { downloadUrl, _ ->
                    val opened =
                        NSURL.URLWithString(downloadUrl)
                            ?.let(UIApplication.sharedApplication::openURL)
                            ?: false
                    if (opened) {
                        PlatformUpdateLaunchResult.OpenedExternalStore
                    } else {
                        PlatformUpdateLaunchResult.Failed("Impossibile aprire l’App Store.")
                    }
                },
        )
    }
