# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags from
# android/defaults.pro (which is the standard Android ProGuard configuration).
# Minification is disabled in the release build for this project, so these
# rules are only applied if isMinifyEnabled is later turned on.

# Retrofit / OkHttp
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**

# Kotlin Serialization
-keepattributes *JsonAdapter*
-keep class kotlinx.serialization.** { *; }
