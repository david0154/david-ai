plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    id("kotlin-parcelize")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.davidstudioz.david"
    compileSdk = 34  // ✅ FIXED: Match documentation and stable SDK

    defaultConfig {
        applicationId = "com.davidstudioz.david"
        minSdk = 26
        targetSdk = 34  // ✅ FIXED: Match compileSdk for consistency
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        multiDexEnabled = true

        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64")
            debugSymbolLevel = "NONE"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true  // ✅ CRITICAL FIX: Enable ProGuard/R8
            isShrinkResources = true  // ✅ NEW: Remove unused resources
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-Xjvm-default=all"
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
        mlModelBinding = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/gradle/incremental.annotation.processors"
        }
        jniLibs {
            useLegacyPackaging = true
        }
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
        quiet = true
        disable += setOf("Deprecation", "ObsoleteLintCustomCheck")
    }
}

composeCompiler {
    includeSourceInformation.set(true)
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("androidx.appcompat:appcompat:1.7.0")

    // Jetpack Compose
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.5")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.10.0")

    // Networking
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // JSON
    implementation("com.google.code.gson:gson:2.11.0")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Camera
    implementation("androidx.camera:camera-camera2:1.4.1")
    implementation("androidx.camera:camera-lifecycle:1.4.1")
    implementation("androidx.camera:camera-view:1.4.1")

    // ✅ ML/AI - OPTIMIZED: Keep only TensorFlow Lite
    // TensorFlow Lite - Primary ML framework (~12MB total)
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    implementation("org.tensorflow:tensorflow-lite-gpu:2.14.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")

    // ✅ REMOVED: ONNX Runtime (save ~25MB)
    // Use TensorFlow Lite for all model inference instead
    // Can be added back later as dynamic feature module if needed

    // ML Kit - Lightweight vision tasks (~6MB total)
    implementation("com.google.mlkit:text-recognition:16.0.1")
    implementation("com.google.mlkit:face-detection:16.1.7")

    // Permissions
    implementation("com.google.accompanist:accompanist-permissions:0.36.0")

    // Coil
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Lottie
    implementation("com.airbnb.android:lottie-compose:6.6.2")

    // Encryption
    implementation("com.google.crypto.tink:tink-android:1.15.0")

    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:21.3.0")

    // Web scraping
    implementation("org.jsoup:jsoup:1.18.3")

    // Hilt - Dependency Injection
    implementation("com.google.dagger:hilt-android:2.52")
    ksp("com.google.dagger:hilt-compiler:2.52")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    
    // Hilt WorkManager support for @HiltWorker
    implementation("androidx.hilt:hilt-work:1.2.0")
    ksp("androidx.hilt:hilt-compiler:1.2.0")

    // ✅ REMOVED: Room Database (not currently used, save ~2MB)
    // Uncomment if you add database functionality:
    // val roomVersion = "2.6.1"
    // implementation("androidx.room:room-runtime:$roomVersion")
    // implementation("androidx.room:room-ktx:$roomVersion")
    // ksp("androidx.room:room-compiler:$roomVersion")

    // MediaPipe - Gesture recognition
    implementation("com.google.mediapipe:tasks-vision:0.10.18")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
