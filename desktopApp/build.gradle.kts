import org.jetbrains.compose.desktop.application.dsl.TargetFormat
plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}
kotlin { jvmToolchain(21) }
dependencies {
    implementation(projects.composeApp)
    implementation(compose.desktop.currentOs)
}
compose.desktop {
    application {
        mainClass = "com.anto426.uniapp.desktop.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Deb, TargetFormat.Rpm, TargetFormat.Msi, TargetFormat.Dmg)
            packageName = "UniApp"
            packageVersion = "2.0.15"
            description = "UniApp per desktop"
            vendor = "Anto426"
            modules("java.net.http", "java.management", "jdk.management", "jdk.crypto.ec", "jdk.unsupported", "java.naming", "java.sql")
            linux {
                packageName = "uniapp"
                appCategory = "Education"
                iconFile.set(project.file("src/main/resources/icon.png"))
            }
            windows { menu = true; shortcut = true; upgradeUuid = "ec25362c-6208-4c40-a1e4-1649607f3658" }
            macOS { bundleID = "com.anto426.uniapp.desktop" }
        }
    }
}
