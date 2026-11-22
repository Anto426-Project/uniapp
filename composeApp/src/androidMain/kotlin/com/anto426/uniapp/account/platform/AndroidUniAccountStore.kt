package com.anto426.uniapp.account.platform

import android.content.Context
import java.io.File
import java.security.KeyStore
import java.security.MessageDigest
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
                val preferences = File(app.applicationInfo.dataDir, "shared_prefs")
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
                val preservedResetScopeHash =
                    MessageDigest.getInstance("SHA-256")
                        .digest("$UNIAPP_STORAGE_SCOPE.vault.storage-reset".encodeToByteArray())
                        .joinToString(separator = "") { byte -> "%02x".format(byte) }
                val secureStorageRoot = File(app.noBackupFilesDir, "secure-storage")
                val keyStore = runCatching { KeyStore.getInstance("AndroidKeyStore").apply { load(null) } }.getOrNull()
                secureStorageRoot.listFiles().orEmpty()
                    .filter { it.name != preservedResetScopeHash }
                    .forEach { orphanDir ->
                        runCatching {
                            val alias = "com.anto426.securestorage.v1.${orphanDir.name}"
                            if (keyStore?.containsAlias(alias) == true) {
                                keyStore.deleteEntry(alias)
                            }
                        }
                        check(orphanDir.deleteRecursively()) { "Unable to reset orphaned secure storage vault" }
                    }
            }
        },
        storageManager =
            SecureStorageManager(
                factory = AndroidSecureStorageFactory(context.applicationContext),
                rootScope = UNIAPP_STORAGE_SCOPE,
            ),
    )
