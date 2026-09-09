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

// In CI (GitHub Actions sets CI=true automatically) we resolve SDK dependencies
// from GitHub Packages where pre-built, R8-obfuscated AARs are published on every push.
// Locally the includeBuild blocks below substitute the module with the submodule source,
// giving the same fast-iteration experience as before.
val isCI = providers.environmentVariable("CI").orElse("false").get() == "true"

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
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://maven.pkg.jetbrains.space/kotlin/kotlin-dev")

        // GitHub Packages — pre-built SDK AARs published by each SDK's CI workflow.
        // Authentication uses a read-only token; the GITHUB_TOKEN secret is available
        // in every workflow automatically. For local development, set GITHUB_ACTOR and
        // GITHUB_TOKEN in your environment or local.properties.
        maven {
            name = "GitHubPackages-LiquidMonet"
            url = uri("https://maven.pkg.github.com/Anto426/Liquid-Monet")
            credentials {
                username = providers.environmentVariable("GITHUB_ACTOR")
                    .orElse(providers.gradleProperty("gpr.user")).orNull
                password = providers.environmentVariable("GITHUB_TOKEN")
                    .orElse(providers.gradleProperty("gpr.token")).orNull
            }
        }
        maven {
            name = "GitHubPackages-UniSdk"
            url = uri("https://maven.pkg.github.com/Anto426-Project/uni-sdk")
            credentials {
                username = providers.environmentVariable("GITHUB_ACTOR")
                    .orElse(providers.gradleProperty("gpr.user")).orNull
                password = providers.environmentVariable("GITHUB_TOKEN")
                    .orElse(providers.gradleProperty("gpr.token")).orNull
            }
        }
        maven {
            name = "GitHubPackages-SecureStorage"
            url = uri("https://maven.pkg.github.com/Anto426-Project/secure-storage-sdk")
            credentials {
                username = providers.environmentVariable("GITHUB_ACTOR")
                    .orElse(providers.gradleProperty("gpr.user")).orNull
                password = providers.environmentVariable("GITHUB_TOKEN")
                    .orElse(providers.gradleProperty("gpr.token")).orNull
            }
        }
        maven {
            name = "GitHubPackages-Firebase"
            url = uri("https://maven.pkg.github.com/Anto426-Project/firebase-connector-sdk")
            credentials {
                username = providers.environmentVariable("GITHUB_ACTOR")
                    .orElse(providers.gradleProperty("gpr.user")).orNull
                password = providers.environmentVariable("GITHUB_TOKEN")
                    .orElse(providers.gradleProperty("gpr.token")).orNull
            }
        }
    }
}

include(":composeApp")
include(":androidApp")

// includeBuild is skipped in CI: the pre-built AARs from GitHub Packages are used instead.
// Locally the submodule source is used directly (faster incremental builds, full IDE support).
if (!isCI) {
    val liquidMonetDir = file("libs/liquid-monet")
    if (liquidMonetDir.exists()) {
        includeBuild(liquidMonetDir) {
            dependencySubstitution {
                substitute(module("com.anto426:antosdk")).using(project(":sdk"))
            }
        }
    }

    val uniSdkDir = file("libs/uni-sdk")
    if (uniSdkDir.exists()) {
        includeBuild(uniSdkDir) {
            dependencySubstitution {
                substitute(module("com.anto426:unisdk")).using(project(":"))
            }
        }
    }

    val secureStorageSdkDir = file("libs/secure-storage-sdk")
    if (secureStorageSdkDir.exists()) {
        includeBuild(secureStorageSdkDir) {
            dependencySubstitution {
                substitute(module("com.anto426:secure-storage-sdk")).using(project(":"))
            }
        }
    }

    val firebaseConnectorSdkDir = file("libs/firebase-connector-sdk")
    if (firebaseConnectorSdkDir.exists()) {
        includeBuild(firebaseConnectorSdkDir) {
            dependencySubstitution {
                substitute(module("com.anto426:firebase-connector-sdk")).using(project(":"))
            }
        }
    }
}
