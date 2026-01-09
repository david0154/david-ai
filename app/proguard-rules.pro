# ProGuard rules for DAVID AI

# Keep Android components
-keep class com.davidstudioz.david.** { *; }
-keep class com.davidstudioz.david.ui.** { *; }
-keep class com.davidstudioz.david.voice.** { *; }
-keep class com.davidstudioz.david.ai.** { *; }
-keep class com.davidstudioz.david.device.** { *; }
-keep class com.davidstudioz.david.web.** { *; }
-keep class com.davidstudioz.david.storage.** { *; }
-keep class com.davidstudioz.david.sync.** { *; }
-keep class com.davidstudioz.david.auth.** { *; }

# Keep Jetpack Compose
-keep @androidx.compose.runtime.Composable class **
-keepclassmembers class ** {
    @androidx.compose.runtime.Composable <methods>;
}

# Keep Hilt
-keep class dagger.hilt.** { *; }
-keep class hilt_aggregated_deps.** { *; }
-keep @dagger.hilt.android.HiltAndroidApp class *

# Keep Room
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-keep @androidx.room.Database class *

# Keep Retrofit and OkHttp
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }

# Keep Gson
-keep class com.google.gson.** { *; }

# Keep serializable
-keep class * implements java.io.Serializable { *; }

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}
