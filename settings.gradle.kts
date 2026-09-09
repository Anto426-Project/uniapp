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
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://maven.pkg.jetbrains.space/kotlin/kotlin-dev")

        // GitHub Packages — pre-built SDK AARs with R8 full-mode obfuscation,
        // published automatically on every push to main by each SDK's CI workflow.
        // GITHUB_TOKEN is injected automatically by GitHub Actions in CI.
        // For local development add gpr.user and gpr.token to local.properties,
        // or export GITHUB_ACTOR / GITHUB_TOKEN in your shell.
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

