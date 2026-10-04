package com.anto426.uniapp.account.platform
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.anto426.securestorage.DesktopSecureStorageFactory
import com.anto426.securestorage.SecureStorageManager
import com.anto426.uniapp.account.storage.UniAccountStore
import java.nio.file.Path

internal fun desktopDataDirectory(): Path {
    val override = System.getProperty("uniapp.dataDir")
    if (!override.isNullOrBlank()) return Path.of(override)
    val home = System.getProperty("user.home")
    val os = System.getProperty("os.name").lowercase()
    return when {
        os.contains("win") -> Path.of(System.getenv("LOCALAPPDATA") ?: "$home/AppData/Local", "UniApp")
        os.contains("mac") -> Path.of(home, "Library", "Application Support", "UniApp")
        else -> Path.of(System.getenv("XDG_DATA_HOME")?.takeIf { it.isNotBlank() } ?: "$home/.local/share", "uniapp")
    }
}
@Composable
internal actual fun rememberPlatformUniAccountStore(): UniAccountStore = remember {
    UniAccountStore(storageManager = SecureStorageManager(
        DesktopSecureStorageFactory(desktopDataDirectory().resolve("vaults")), UNIAPP_STORAGE_SCOPE,
    ))
}
