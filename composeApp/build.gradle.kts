import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {
    tasks.withType<KotlinCompilationTask<*>>().configureEach {
        compilerOptions {
            freeCompilerArgs.add("-Xexpect-actual-classes")
        }
    }

    android {
        namespace = "com.anto426.uniapp.compose"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        androidResources {
            enable = true
        }

        withHostTest {
            isIncludeAndroidResources = true
        }

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }

        optimization {
            minify = true
            keepRules.file("proguard-rules.pro")
            consumerKeepRules.publish = true
            consumerKeepRules.file("proguard-rules.pro")
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            binaryOption("bundleId", "com.anto426.uniapp.compose")
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.core.ktx)
            implementation(libs.ktor.client.okhttp)
        }

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.viewmodel.navigation3)
            implementation(libs.jetbrains.navigation3.ui)

            implementation(libs.antosdk)
            implementation(libs.unisdk)
            implementation(libs.secure.storage.sdk)
            implementation(libs.firebase.connector.sdk)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.contentNegotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
            implementation(libs.zscanner)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
        }
    }
}

// Capture the checked-out modules, including local changes, instead of dependency aliases.
val moduleNames = listOf("liquid-monet", "uni-sdk", "secure-storage-sdk", "firebase-connector-sdk")
val moduleMetadata = moduleNames.map { name ->
    val moduleDir = rootProject.file("libs/$name")
    val declaredVersion = Regex("""version\s*=\s*"([^"]+)"""")
        .find(providers.fileContents(rootProject.layout.projectDirectory.file("libs/$name/build.gradle.kts")).asText.get())
        ?.groupValues?.get(1) ?: "unknown"
    val revision = providers.exec { commandLine("git", "-C", moduleDir.absolutePath, "rev-parse", "HEAD") }.standardOutput.asText.get().trim()
    val dirty = providers.exec { commandLine("git", "-C", moduleDir.absolutePath, "status", "--porcelain", "--untracked-files=normal") }.standardOutput.asText.get().isNotBlank()
    listOf(name, declaredVersion, revision, dirty.toString())
}
val sourceRevision = providers.exec {
    commandLine("git", "-C", rootProject.projectDir.absolutePath, "rev-parse", "HEAD")
}.standardOutput.asText.map { it.trim() }
val appStoreUrl = providers.environmentVariable("UNIAPP_APP_STORE_URL").orElse("")
val generateAppBuildMetadata = tasks.register("generateAppBuildMetadata") {
    val output = layout.buildDirectory.dir("generated/appInfo/commonMain")
    inputs.property("modules", moduleMetadata)
    inputs.property("revision", sourceRevision)
    inputs.property("appStoreUrl", appStoreUrl)
    outputs.dir(output)
    doLast {
        fun literal(value: String) = "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"").replace("$", "\\$").replace("\n", "\\n").replace("\r", "\\r") + "\""
        val modules = (inputs.properties["modules"] as List<List<String>>).joinToString(",\n") { values ->
            "AppModuleInfo(${literal(values[0])}, ${literal(values[1])}, ${literal(values[2])}, ${values[3]})"
        }
        val target = output.get().file("com/anto426/uniapp/app/info/AppBuildMetadata.kt").asFile
        target.parentFile.mkdirs()
        target.writeText("""
            package com.anto426.uniapp.app.info
            import com.anto426.unisdk.platform.AppModuleInfo
            internal object AppBuildMetadata {
                const val sourceRevision = ${literal(inputs.properties["revision"].toString())}
                const val appStoreUrl = ${literal(inputs.properties["appStoreUrl"].toString())}
                val modules = listOf($modules)
            }
        """.trimIndent())
    }
}
kotlin.sourceSets.commonMain { kotlin.srcDir(generateAppBuildMetadata) }
