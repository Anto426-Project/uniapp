package com.anto426.uniapp.account.platform

import android.content.Context
import com.anto426.securestorage.AndroidSecureStorageFactory
import com.anto426.securestorage.SecureStorageManager
import com.anto426.uniapp.account.storage.UniAccountStore

fun createAndroidUniAccountStore(context: Context): UniAccountStore =
    UniAccountStore(
        storageManager =
            SecureStorageManager(
                factory = AndroidSecureStorageFactory(context.applicationContext),
                rootScope = UNIAPP_STORAGE_SCOPE,
            ),
    )
