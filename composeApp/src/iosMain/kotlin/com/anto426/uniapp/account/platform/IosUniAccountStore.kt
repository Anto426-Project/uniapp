package com.anto426.uniapp.account.platform

import com.anto426.securestorage.IosSecureStorageFactory
import com.anto426.securestorage.SecureStorageManager
import com.anto426.uniapp.account.storage.UniAccountStore

fun createIosUniAccountStore(): UniAccountStore =
    UniAccountStore(
        storageManager =
            SecureStorageManager(
                factory = IosSecureStorageFactory(servicePrefix = "com.anto426.uniapp.securestorage"),
                rootScope = UNIAPP_STORAGE_SCOPE,
            ),
    )
