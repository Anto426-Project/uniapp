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


val sdkProperties = java.util.Properties()
val sdkManifest = file(".sdk-binaries/resolved.properties")
if (sdkManifest.isFile) sdkManifest.inputStream().use(sdkProperties::load)
dependencyResolutionManagement.versionCatalogs.create("libs") {
    mapOf("antosdk" to "liquid-monet", "unisdk" to "uni-sdk",
        "secure-storage-sdk" to "secure-storage-sdk", "firebase-connector-sdk" to "firebase-connector-sdk")
        .forEach { (alias, module) -> version(alias, sdkProperties.getProperty("$module.version", "1.0.0-local")) }
}
