rootProject.name = "UniApp"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            content {
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
                includeGroupAndSubgroups("androidx")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google {
            content {
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
                includeGroupAndSubgroups("androidx")
            }
        }
        mavenCentral()

        exclusiveContent {
            forRepository { maven { name = "VerifiedSdkBinaries"; url = uri(".sdk-binaries/maven") } }
            filter {
                includeGroup("com.anto426")
                includeGroup("com.anto426.liquidmonet")
            }
        }
    }
}

include(":composeApp")
include(":androidApp")

val useLocalSdks = providers.gradleProperty("useLocalSdks")
    .map { it.toBoolean() }
    .orElse(true)
    .get()

if (useLocalSdks) {
    val rootLocalProperties = file("local.properties")
    fun syncLocalProperties(targetDir: File) {
        if (rootLocalProperties.isFile && targetDir.isDirectory) {
            val targetLocalProps = File(targetDir, "local.properties")
            if (!targetLocalProps.isFile) {
                rootLocalProperties.copyTo(targetLocalProps, overwrite = false)
            }
        }
    }

    val localLiquidMonet = file("../liquid-monet")
    if (localLiquidMonet.isDirectory) {
        syncLocalProperties(localLiquidMonet)
        includeBuild(localLiquidMonet) {
            dependencySubstitution {
                substitute(module("com.anto426.liquidmonet:sdk")).using(project(":sdk"))
            }
        }
    }
    val localUniSdk = file("../uni-sdk")
    if (localUniSdk.isDirectory) {
        syncLocalProperties(localUniSdk)
        includeBuild(localUniSdk) {
            dependencySubstitution {
                substitute(module("com.anto426:uni-sdk")).using(project(":"))
            }
        }
    }
    val localSecureStorage = file("../secure-storage-sdk")
    if (localSecureStorage.isDirectory) {
        syncLocalProperties(localSecureStorage)
        includeBuild(localSecureStorage) {
            dependencySubstitution {
                substitute(module("com.anto426:secure-storage-sdk")).using(project(":"))
            }
        }
    }
    val localFirebase = file("../firebase-connector-sdk")
    if (localFirebase.isDirectory) {
        syncLocalProperties(localFirebase)
        includeBuild(localFirebase) {
            dependencySubstitution {
                substitute(module("com.anto426:firebase-connector-sdk")).using(project(":"))
            }
        }
    }
}


val sdkProperties = java.util.Properties()
val sdkManifest = file(".sdk-binaries/resolved.properties")
if (sdkManifest.isFile) sdkManifest.inputStream().use(sdkProperties::load)
dependencyResolutionManagement.versionCatalogs.create("libs") {
    mapOf("antosdk" to "liquid-monet", "unisdk" to "uni-sdk",
        "secure-storage-sdk" to "secure-storage-sdk", "firebase-connector-sdk" to "firebase-connector-sdk")
        .forEach { (alias, module) -> version(alias, sdkProperties.getProperty("$module.version", "1.0.0-local")) }
}
