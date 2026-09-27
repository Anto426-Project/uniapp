@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.anto426.uniapp.account.platform

import platform.Foundation.NSBundle
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSFileManager
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSURL
import com.anto426.securestorage.IosSecureStorageFactory
import com.anto426.securestorage.SecureStorageManager
import com.anto426.uniapp.account.storage.UniAccountStore

fun createIosUniAccountStore(): UniAccountStore =
    UniAccountStore(
        resetStorageForRelease = true,
        resetPlatformStorage = {
            NSBundle.mainBundle.bundleIdentifier?.let {
                NSUserDefaults.standardUserDefaults.removePersistentDomainForName(it)
            }
            val files = NSFileManager.defaultManager
            for (directory in listOf(NSCachesDirectory, NSDocumentDirectory, NSApplicationSupportDirectory)) {
                val root = (files.URLsForDirectory(directory, NSUserDomainMask).firstOrNull() as? NSURL)?.path ?: continue
                files.contentsOfDirectoryAtPath(root, error = null).orEmpty().filterIsInstance<String>().forEach { name ->
                    check(files.removeItemAtPath("$root/$name", error = null)) { "Unable to reset application files" }
                }
            }
        },
        storageManager =
            SecureStorageManager(
                factory = IosSecureStorageFactory(servicePrefix = "com.anto426.uniapp.securestorage"),
                rootScope = UNIAPP_STORAGE_SCOPE,
            ),
    )
