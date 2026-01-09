# ProGuard configuration for DAVID AI

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
EOF

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

# Keep Retrofit
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }

# Keep OkHttp
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Keep Gson
-keep class com.google.gson.** { *; }
-keep interface com.google.gson.** { *; }

# Keep serializable
-keep class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Remove logging
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
