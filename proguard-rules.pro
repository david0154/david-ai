# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# ✅ DAVID AI - Complete ProGuard Rules for Production

# ========================================
# CORE APPLICATION CLASSES
# ========================================
# Keep all D.A.V.I.D application classes
-keep class com.davidstudioz.david.** { *; }
-keepclassmembers class com.davidstudioz.david.** { *; }

# Keep Application class
-keep class com.davidstudioz.david.DavidAIApp { *; }

# ========================================
# MACHINE LEARNING FRAMEWORKS
# ========================================
# TensorFlow Lite
-keep class org.tensorflow.** { *; }
-keep interface org.tensorflow.** { *; }
-keepclassmembers class org.tensorflow.** { *; }
-dontwarn org.tensorflow.**

# TensorFlow Lite GPU
-keep class org.tensorflow.lite.gpu.** { *; }
-keepclassmembers class org.tensorflow.lite.gpu.** { *; }

# TensorFlow Lite Support
-keep class org.tensorflow.lite.support.** { *; }
-keepclassmembers class org.tensorflow.lite.support.** { *; }

# ML Kit
-keep class com.google.mlkit.** { *; }
-keepclassmembers class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# MediaPipe
-keep class com.google.mediapipe.** { *; }
-keepclassmembers class com.google.mediapipe.** { *; }
-dontwarn com.google.mediapipe.**

# ========================================
# DEPENDENCY INJECTION (HILT)
# ========================================
-keep class dagger.** { *; }
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Keep Hilt generated classes
-keep class **_HiltModules { *; }
-keep class **_HiltComponents { *; }
-keep class **_Factory { *; }
-keep class **_MembersInjector { *; }

# Keep @HiltAndroidApp, @AndroidEntryPoint, @HiltWorker annotated classes
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }
-keep @androidx.hilt.work.HiltWorker class * { *; }

# ========================================
# NETWORKING (RETROFIT + OKHTTP)
# ========================================
# Retrofit
-keep class retrofit2.** { *; }
-keepclassmembers class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**

# OkHttp
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keepclassmembers class okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# ========================================
# SERIALIZATION (GSON)
# ========================================
-keep class com.google.gson.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep data classes used for JSON serialization
-keep class com.davidstudioz.david.models.** { *; }
-keepclassmembers class com.davidstudioz.david.models.** { *; }

# ========================================
# JETPACK COMPOSE
# ========================================
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ========================================
# WORKMANAGER
# ========================================
-keep class androidx.work.** { *; }
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# ========================================
# ANDROID COMPONENTS
# ========================================
# Keep Activities
-keep class * extends android.app.Activity
-keepclassmembers class * extends android.app.Activity { *; }

# Keep Services
-keep class * extends android.app.Service
-keepclassmembers class * extends android.app.Service { *; }

# Keep BroadcastReceivers
-keep class * extends android.content.BroadcastReceiver
-keepclassmembers class * extends android.content.BroadcastReceiver { *; }

# ========================================
# CAMERA & MEDIA
# ========================================
-keep class androidx.camera.** { *; }
-keepclassmembers class androidx.camera.** { *; }
-dontwarn androidx.camera.**

# ========================================
# ENCRYPTION (TINK)
# ========================================
-keep class com.google.crypto.tink.** { *; }
-keepclassmembers class com.google.crypto.tink.** { *; }
-dontwarn com.google.crypto.tink.**

# ========================================
# GENERAL RULES
# ========================================
# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep Parcelable classes
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep Serializable classes
-keepnames class * implements java.io.Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ========================================
# OPTIMIZATION & OBFUSCATION
# ========================================
# Optimize but don't over-optimize (can cause issues with reflection)
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# Allow obfuscation but keep debugging info for crash reports
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Rename packages to reduce APK size
-repackageclasses 'd'

# Remove logging in release builds for security
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# ========================================
# WARNINGS TO IGNORE
# ========================================
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
-dontwarn com.google.errorprone.annotations.**
