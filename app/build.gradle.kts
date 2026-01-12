plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    id("kotlin-parcelize")
    // Hilt for dependency injection
    id("com.google.dagger.hilt.android")
    // Remove Firebase for now - add back when google-services.json is ready
    // id("com.google.gms.google-services")
}

android {
    namespace = "com.davidstudioz.david"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.davidstudioz.david"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // Multidex support for large app
        multiDexEnabled = true
        
        // Room schema export
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
        
        // Suppress native library stripping warnings
        ndk {
            debugSymbolLevel = "NONE"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
        // Suppress deprecation warnings for Google Sign-In (will be updated to Credential Manager later)
        freeCompilerArgs += listOf(
            "-Xsuppress-warning=DEPRECATION"
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/gradle/incremental.annotation.processors"
        }
        jniLibs {
            // Don't strip native libraries (MediaPipe, MLKit)
            useLegacyPackaging = true
        }
    }
    
    // Suppress lint warnings
    lint {
        checkReleaseBuilds = false
        abortOnError = false
        quiet = true
        disable += setOf("Deprecation", "ObsoleteLintCustomCheck")
    }
}

// Compose Compiler Configuration (Modern API)
// REMOVED: StrongSkipping is now enabled by default in Compose Compiler
composeCompiler {
    // Include source information for better debugging
    includeSourceInformation.set(true)
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.multidex:multidex:2.0.1")
    
    // AppCompat for theme compatibility
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

    // WorkManager for background tasks
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

    // ML/AI
    implementation("com.google.mlkit:text-recognition:16.0.1")
    implementation("com.google.mlkit:face-detection:16.1.7")

    // Permissions
    implementation("com.google.accompanist:accompanist-permissions:0.36.0")

    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Lottie for animations
    implementation("com.airbnb.android:lottie-compose:6.6.2")
    
    // Google Tink for encryption (EncryptionManager)
    implementation("com.google.crypto.tink:tink-android:1.15.0")
    
    // Google Sign-In (GoogleSignInScreen, GoogleAuthManager)
    // Note: Deprecated but functional. Migration to Credential Manager planned.
    implementation("com.google.android.gms:play-services-auth:21.3.0")
    
    // Jsoup for web scraping (WebSearchEngine)
    implementation("org.jsoup:jsoup:1.18.3")
    
    // Hilt for dependency injection (AppModule, AuthModule)
    implementation("com.google.dagger:hilt-android:2.52")
    ksp("com.google.dagger:hilt-compiler:2.52")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    
    // Room Database (ChatHistoryManager)
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    
    // MediaPipe for gesture recognition (GestureController)
    implementation("com.google.mediapipe:tasks-vision:0.10.18")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
