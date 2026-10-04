plugins {
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.androidKotlinMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
}

// The desktop consumes independent Maven binaries while mobile keeps its published releases.
val desktopSdkCoordinates = listOf(
    libs.desktop.liquid.monet.sdk, libs.desktop.unisdk,
    libs.desktop.secure.storage.sdk, libs.desktop.firebase.connector.sdk,
).map { dependency -> dependency.get().let { "${it.module}:${it.versionConstraint.requiredVersion}" } }
subprojects {
    val desktopLauncher = name == "desktopApp"
    configurations.configureEach {
        if (desktopLauncher || name.startsWith("desktop", ignoreCase = true)) {
            resolutionStrategy.force(*desktopSdkCoordinates.toTypedArray())
        }
    }
}
