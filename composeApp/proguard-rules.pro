# Consumer R8 / ProGuard rules for composeApp (Aggressive Optimization & Obfuscation Mode)

# Preserve annotations and signature for Compose and Kotlinx Serialization
-keepattributes *Annotation*,Signature

# Keep ViewModel constructor for Jetpack ViewModelProvider / Compose viewModel()
-keepclassmembers,allowshrinking,allowobfuscation class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}



# Allow shrinking and obfuscation for navigation models and serializers
-keep,allowshrinking,allowobfuscation class com.anto426.uniapp.navigation.model.** { *; }
-keepclassmembers,allowshrinking,allowobfuscation class com.anto426.uniapp.navigation.model.** { *; }
-keep,allowshrinking,allowobfuscation class com.anto426.uniapp.navigation.runtime.** { *; }

# Allow shrinking for generated Compose resources
-keep,allowshrinking,allowobfuscation class com.anto426.uniapp.compose.generated.resources.** { *; }

# R$string is an Android-generated class that only exists in the final app (androidApp),
# not during library minification. Suppress the missing class error.
-dontwarn com.anto426.uniapp.compose.R$string
