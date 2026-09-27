package com.anto426.uniapp.account.platform

import com.anto426.securestorage.IosSecureStorageFactory
import com.anto426.securestorage.SecureStorageManager
import platform.Foundation.NSBundle
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSFileManager
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSURL
import com.anto426.uniapp.account.storage.UniAccountStore

fun createIosUniAccountStore(): UniAccountStore =
    UniAccountStore(
        storageManager =
            SecureStorageManager(
                factory = IosSecureStorageFactory(servicePrefix = "com.anto426.uniapp.securestorage"),
                rootScope = UNIAPP_STORAGE_SCOPE,
            ),
    )
