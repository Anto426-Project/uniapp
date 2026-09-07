# Application R8 / ProGuard Configuration for UniApp (Aggressive Optimization Mode)

# Aggressive Optimization & Inlining flags
-allowaccessmodification
-repackageclasses ''
-optimizationpasses 5
-overloadaggressively
-mergeinterfacesaggressively

# Dead code elimination and member stripping
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers

# Preserve line numbers and source file for readable stack traces and debugging
-keepattributes SourceFile,LineNumberTable,*Annotation*,Signature

# Aggressive Obfuscation flags
-renamesourcefileattribute 'SourceFile'

# Jetpack Compose
-keepclassmembers class * extends androidx.compose.runtime.RecomposeScope { *; }
-dontwarn androidx.compose.**

# Kotlinx Serialization - Allow shrinking and obfuscation of unused models and serializers
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers,allowshrinking class * {
    *** Companion;
}
-keepclasseswithmembers,allowshrinking class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers,allowshrinking,allowobfuscation class * implements kotlinx.serialization.KSerializer {
    java.lang.Object deserialize(...);
    void serialize(...);
}

# Android KeyStore and Cryptography (Secure Storage)
-keepclassmembers,allowshrinking,allowobfuscation class * extends java.security.KeyStore$Entry { *; }
-keep,allowshrinking,allowobfuscation class javax.crypto.** { *; }

# Ktor HTTP Client & OkHttp engine
-dontwarn io.ktor.**
-dontwarn okhttp3.**
-dontwarn okio.**
-keep,allowshrinking,allowobfuscation class io.ktor.client.engine.okhttp.** { *; }

# Coil 3 Image Loading
-dontwarn coil3.**
-keep,allowshrinking,allowobfuscation class coil3.** { *; }

# ZScanner QR code scanning
-dontwarn io.github.namantonk.zscanner.**
-keep,allowshrinking,allowobfuscation class io.github.namantonk.zscanner.** { *; }

# Firebase Messaging
-dontwarn com.google.firebase.**
-keep,allowshrinking,allowobfuscation class com.google.firebase.messaging.** { *; }


