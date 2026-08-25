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
    // Fallback se le librerie non specificano un repository
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
    }
}

include(":composeApp")

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