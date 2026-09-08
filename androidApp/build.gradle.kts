plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

android {
    namespace = "com.anto426.uniapp.android"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.anto426.uniapp"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 200
        versionName = "2.0.0"
        ndk {
            abiFilters += "arm64-v8a"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            matchingFallbacks += listOf("release")
        }
    }

    val signingValues = listOf("RELEASE_STORE_FILE", "RELEASE_STORE_PASSWORD", "RELEASE_KEY_ALIAS", "RELEASE_KEY_PASSWORD")
        .associateWith { key -> providers.gradleProperty(key).orElse(providers.environmentVariable(key)).orNull }
    if (signingValues.values.any { !it.isNullOrBlank() }) {
        require(signingValues.values.all { !it.isNullOrBlank() }) { "Incomplete release signing configuration." }
        val releaseSigning = signingConfigs.create("release") {
            storeFile = rootProject.file(signingValues.getValue("RELEASE_STORE_FILE")!!)
            storePassword = signingValues.getValue("RELEASE_STORE_PASSWORD")
            keyAlias = signingValues.getValue("RELEASE_KEY_ALIAS")
            keyPassword = signingValues.getValue("RELEASE_KEY_PASSWORD")
        }
        buildTypes.getByName("release").signingConfig = releaseSigning
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":composeApp")) {
        exclude(group = "org.jetbrains.compose")
        exclude(group = "org.jetbrains.androidx")
    }
    implementation("com.anto426:antosdk")
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.graphics)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.preview)
}
