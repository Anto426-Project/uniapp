package com.anto426.uniapp.account.platform

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.anto426.securestorage.AndroidSecureStorageFactory
import com.anto426.securestorage.SecureStorageManager
import com.anto426.uniapp.account.storage.UniAccountStore

fun createAndroidUniAccountStore(context: Context): UniAccountStore =
    UniAccountStore(
        resetStorageForRelease = true,
        resetPlatformStorage = {
            withContext(Dispatchers.IO) {
                val app = context.applicationContext
                val preferences = java.io.File(app.applicationInfo.dataDir, "shared_prefs")
                preferences.listFiles().orEmpty().filter { it.extension == "xml" }.forEach { file ->
                    check(app.getSharedPreferences(file.nameWithoutExtension, Context.MODE_PRIVATE).edit().clear().commit()) {
                        "Unable to reset application preferences"
                    }
                }
                app.cacheDir.listFiles().orEmpty().forEach { check(it.deleteRecursively()) { "Unable to reset application cache" } }
                app.filesDir.listFiles().orEmpty().forEach { check(it.deleteRecursively()) { "Unable to reset application files" } }
                app.noBackupFilesDir.listFiles().orEmpty().filter { it.name.startsWith("liquid-glass-") }.forEach {
                    check(it.deleteRecursively()) { "Unable to reset rendering calibration" }
                }
            }
        },
        storageManager =
            SecureStorageManager(
                factory = AndroidSecureStorageFactory(context.applicationContext),
                rootScope = UNIAPP_STORAGE_SCOPE,
            ),
    )
