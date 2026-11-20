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
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

val localLiquidMonet = providers.fileContents(
    layout.settingsDirectory.file(".sdk-binaries/local-liquid-monet.properties"),
).asText.orNull?.let { manifest ->
    java.util.Properties().apply { manifest.reader().use(::load) }
        .getProperty("liquid-monet.version")
}

val localUniSdk = providers.fileContents(
    layout.settingsDirectory.file(".sdk-binaries/local-uni-sdk.properties"),
).asText.orNull?.let { manifest ->
    java.util.Properties().apply { manifest.reader().use(::load) }
        .getProperty("uni-sdk.version")
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    if (localLiquidMonet != null) {
        versionCatalogs.maybeCreate("libs").version("liquid-monet-sdk", localLiquidMonet)
    }
    if (localUniSdk != null) {
        versionCatalogs.maybeCreate("libs").version("unisdk", localUniSdk)
    }

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
