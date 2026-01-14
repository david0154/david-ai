# ========================================
# ProGuard rules for DAVID AI
# Phase 1 + Phase 2 Model Infrastructure
# ========================================

# ========================================
# Core Android Components
# ========================================
-keep class com.davidstudioz.david.** { *; }
-keep class com.davidstudioz.david.ui.** { *; }
-keep class com.davidstudioz.david.voice.** { *; }
-keep class com.davidstudioz.david.ai.** { *; }
-keep class com.davidstudioz.david.device.** { *; }
-keep class com.davidstudioz.david.web.** { *; }
-keep class com.davidstudioz.david.storage.** { *; }
-keep class com.davidstudioz.david.sync.** { *; }
-keep class com.davidstudioz.david.auth.** { *; }

# ========================================
# Phase 1: Core Model Infrastructure
# ========================================
# Keep all Phase 1 model management classes
-keep class com.davidstudioz.david.core.model.** { *; }
-keep class com.davidstudioz.david.di.AIModelModule { *; }
-keep class com.davidstudioz.david.di.AIModelModule$* { *; }

# Keep ModelDownloadManager and related classes
-keep class com.davidstudioz.david.core.model.ModelDownloadManager { *; }
-keep class com.davidstudioz.david.core.model.ModelDownloadWorker { *; }
-keep class com.davidstudioz.david.core.model.DownloadProgress { *; }
-keep class com.davidstudioz.david.core.model.DownloadProgress$* { *; }

# Keep ModelValidator and related classes
-keep class com.davidstudioz.david.core.model.ModelValidator { *; }
-keep class com.davidstudioz.david.core.model.ValidationResult { *; }
-keep class com.davidstudioz.david.core.model.ValidationResult$* { *; }
-keep class com.davidstudioz.david.core.model.ValidationError { *; }
-keep class com.davidstudioz.david.core.model.ValidationError$* { *; }

# Keep ModelLifecycleManager and related classes
-keep class com.davidstudioz.david.core.model.ModelLifecycleManager { *; }
-keep class com.davidstudioz.david.core.model.ModelType { *; }
-keep class com.davidstudioz.david.core.model.ModelPriority { *; }
-keep class com.davidstudioz.david.core.model.ModelState { *; }
-keep class com.davidstudioz.david.core.model.ModelState$* { *; }
-keep class com.davidstudioz.david.core.model.ModelLoader { *; }

# ========================================
# Phase 2: Enhanced Model Managers
# ========================================

# WhisperModelManager (Voice Recognition)
-keep class com.davidstudioz.david.ai.voice.WhisperModelManager { *; }
-keep class com.davidstudioz.david.ai.voice.WhisperModelInfo { *; }
-keepclassmembers class com.davidstudioz.david.ai.voice.WhisperModelManager {
    public <methods>;
}

# ChatModelManager (LLM)
-keep class com.davidstudioz.david.ai.chat.ChatModelManager { *; }
-keep class com.davidstudioz.david.ai.chat.ChatModel { *; }
-keep class com.davidstudioz.david.ai.chat.ChatMessage { *; }
-keep class com.davidstudioz.david.ai.chat.ChatStreamResult { *; }
-keep class com.davidstudioz.david.ai.chat.ChatStreamResult$* { *; }
-keep class com.davidstudioz.david.ai.chat.KVCache { *; }
-keep class com.davidstudioz.david.ai.chat.Role { *; }
-keepclassmembers class com.davidstudioz.david.ai.chat.ChatModelManager {
    public <methods>;
}

# GestureRecognizerManager (Hand Tracking)
-keep class com.davidstudioz.david.ai.gesture.GestureRecognizerManager { *; }
-keep class com.davidstudioz.david.ai.gesture.GestureState { *; }
-keep class com.davidstudioz.david.ai.gesture.GestureState$* { *; }
-keep class com.davidstudioz.david.ai.gesture.GestureError { *; }
-keep class com.davidstudioz.david.ai.gesture.GestureError$* { *; }
-keep class com.davidstudioz.david.ai.gesture.GestureType { *; }
-keep class com.davidstudioz.david.ai.gesture.DetectedGesture { *; }
-keep class com.davidstudioz.david.ai.gesture.Point3D { *; }
-keep class com.davidstudioz.david.ai.gesture.GestureDetectionResult { *; }
-keep class com.davidstudioz.david.ai.gesture.LightingCondition { *; }
-keepclassmembers class com.davidstudioz.david.ai.gesture.GestureRecognizerManager {
    public <methods>;
}

# LanguageModelManager (Multilingual)
-keep class com.davidstudioz.david.ai.language.LanguageModelManager { *; }
-keep class com.davidstudioz.david.ai.language.Language { *; }
-keep class com.davidstudioz.david.ai.language.LanguageTask { *; }
-keep class com.davidstudioz.david.ai.language.Sentiment { *; }
-keep class com.davidstudioz.david.ai.language.LanguageModelInstance { *; }
-keep class com.davidstudioz.david.ai.language.LanguageUsageStats { *; }
-keep class com.davidstudioz.david.ai.language.SentimentResult { *; }
-keepclassmembers class com.davidstudioz.david.ai.language.LanguageModelManager {
    public <methods>;
}

# ========================================
# TensorFlow Lite
# ========================================
# Critical: Keep all TFLite classes and native methods
-keep class org.tensorflow.lite.** { *; }
-keep interface org.tensorflow.lite.** { *; }
-keepclassmembers class org.tensorflow.lite.** { *; }

# TFLite GPU Delegate
-keep class org.tensorflow.lite.gpu.** { *; }
-keepclassmembers class org.tensorflow.lite.gpu.** { *; }

# TFLite NNAPI Delegate
-keep class org.tensorflow.lite.nnapi.** { *; }
-keepclassmembers class org.tensorflow.lite.nnapi.** { *; }

# TFLite Support Library
-keep class org.tensorflow.lite.support.** { *; }
-keepclassmembers class org.tensorflow.lite.support.** { *; }

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# ========================================
# MediaPipe
# ========================================
# Critical: Keep all MediaPipe classes
-keep class com.google.mediapipe.** { *; }
-keep interface com.google.mediapipe.** { *; }
-keepclassmembers class com.google.mediapipe.** { *; }

# MediaPipe Tasks
-keep class com.google.mediapipe.tasks.** { *; }
-keepclassmembers class com.google.mediapipe.tasks.** { *; }

# MediaPipe Vision Tasks
-keep class com.google.mediapipe.tasks.vision.** { *; }
-keepclassmembers class com.google.mediapipe.tasks.vision.** { *; }

# MediaPipe Framework
-keep class com.google.mediapipe.framework.** { *; }
-keepclassmembers class com.google.mediapipe.framework.** { *; }

# MediaPipe Components
-keep class com.google.mediapipe.tasks.components.** { *; }
-keepclassmembers class com.google.mediapipe.tasks.components.** { *; }

# ========================================
# ONNX Runtime
# ========================================
-keep class ai.onnxruntime.** { *; }
-keep interface ai.onnxruntime.** { *; }
-keepclassmembers class ai.onnxruntime.** { *; }

# ========================================
# ML Kit
# ========================================
-keep class com.google.mlkit.** { *; }
-keepclassmembers class com.google.mlkit.** { *; }

# ========================================
# Jetpack Compose
# ========================================
-keep @androidx.compose.runtime.Composable class **
-keepclassmembers class ** {
    @androidx.compose.runtime.Composable <methods>;
}

# Compose Runtime
-keep class androidx.compose.runtime.** { *; }
-keep interface androidx.compose.runtime.** { *; }

# Compose UI
-keep class androidx.compose.ui.** { *; }
-keep interface androidx.compose.ui.** { *; }

# Stable markers
-keep @androidx.compose.runtime.Stable class *
-keep @androidx.compose.runtime.Immutable class *

# ========================================
# Hilt / Dagger
# ========================================
-keep class dagger.** { *; }
-keep class dagger.hilt.** { *; }
-keep class hilt_aggregated_deps.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.InstallIn class * { *; }
-keep @dagger.Module class * { *; }
-keep @javax.inject.Inject class * { *; }

# Keep injected constructors
-keepclasseswithmembernames class * {
    @javax.inject.Inject <init>(...);
}

# Keep Hilt generated classes
-keep class **_Factory { *; }
-keep class **_HiltModules { *; }
-keep class **_HiltModules$* { *; }
-keep class **_ComponentTreeDeps { *; }
-keep class **_AssistedFactory { *; }
-keep class **_AssistedFactory_Impl { *; }
-keep class **_GeneratedInjector { *; }

# ========================================
# Room Database
# ========================================
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-keep @androidx.room.Database class *
-keepclassmembers class * {
    @androidx.room.* <methods>;
}

# Room runtime
-keep class androidx.room.** { *; }
-keep interface androidx.room.** { *; }

# ========================================
# WorkManager
# ========================================
-keep class androidx.work.** { *; }
-keep interface androidx.work.** { *; }
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.ListenableWorker { *; }
-keepclassmembers class * extends androidx.work.Worker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# ========================================
# Kotlin Coroutines
# ========================================
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# StateFlow and SharedFlow
-keep class kotlinx.coroutines.flow.** { *; }
-keepclassmembers class kotlinx.coroutines.flow.** { *; }

# ========================================
# Retrofit and OkHttp
# ========================================
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-keepclassmembers class retrofit2.** { *; }

-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keepclassmembers class okhttp3.** { *; }

# Retrofit annotations
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*

# OkHttp platform used only on JVM and when Conscrypt dependency is available.
-dontwarn okhttp3.internal.platform.ConscryptPlatform
-dontwarn org.conscrypt.ConscryptHostnameVerifier

# ========================================
# Gson
# ========================================
-keep class com.google.gson.** { *; }
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Prevent obfuscation of types which use Gson annotations
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ========================================
# CameraX
# ========================================
-keep class androidx.camera.** { *; }
-keep interface androidx.camera.** { *; }
-keepclassmembers class androidx.camera.** { *; }

# ========================================
# General Rules
# ========================================

# Keep serializable classes
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

# Keep Parcelable
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep annotations
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes SourceFile
-keepattributes LineNumberTable

# ========================================
# Performance: Remove Logging in Release
# ========================================
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Remove println
-assumenosideeffects class java.io.PrintStream {
    public void println(%);
    public void print(%);
}

# ========================================
# Suppress Warnings
# ========================================
-dontwarn org.tensorflow.lite.**
-dontwarn com.google.mediapipe.**
-dontwarn ai.onnxruntime.**
-dontwarn javax.annotation.**
-dontwarn org.checkerframework.**
-dontwarn com.google.errorprone.annotations.**
-dontwarn java.lang.instrument.ClassFileTransformer
-dontwarn sun.misc.SignalHandler
-dontwarn java.lang.management.ManagementFactory
-dontwarn java.lang.management.RuntimeMXBean
-dontwarn org.joda.time.**
-dontwarn org.conscrypt.**
-dontwarn okhttp3.internal.platform.**

# ========================================
# Optimization Settings
# ========================================
# Aggressive optimization for smaller APK
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose

# Optimize methods
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*

# Repackage classes into single package
-repackageclasses ''

# Allow access modification during optimization
-allowaccessmodification
