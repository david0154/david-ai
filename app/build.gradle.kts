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

        multiDexEnabled = true

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }

        ndk {
            // Limit to ARM architectures for smaller APK and fewer conflicts
            abiFilters += listOf("arm64-v8a", "armeabi-v7a")
            debugSymbolLevel = "SYMBOL_TABLE" // Better crash reports
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            
            // Optimization for ML models
            ndk {
                debugSymbolLevel = "NONE"
            }
        }
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            isMinifyEnabled = false
            
            // Keep debug symbols for better crash analysis
            ndk {
                debugSymbolLevel = "FULL"
            }
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
            "-Xjvm-default=all",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=kotlinx.coroutines.FlowPreview"
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
            
            // ========================================
            // CRITICAL: Native Library Conflict Resolution
            // ========================================
            // TensorFlow Lite, MediaPipe, and ONNX Runtime have overlapping native libraries
            // These pickFirst rules resolve conflicts by choosing the first occurrence
            
            // TensorFlow Lite native libraries
            pickFirst("lib/arm64-v8a/libtensorflowlite_jni.so")
            pickFirst("lib/armeabi-v7a/libtensorflowlite_jni.so")
            pickFirst("lib/x86_64/libtensorflowlite_jni.so")
            
            // TensorFlow Lite GPU delegate
            pickFirst("lib/arm64-v8a/libtensorflowlite_gpu_jni.so")
            pickFirst("lib/armeabi-v7a/libtensorflowlite_gpu_jni.so")
            
            // MediaPipe native libraries
            pickFirst("lib/arm64-v8a/libmediapipe_jni.so")
            pickFirst("lib/armeabi-v7a/libmediapipe_jni.so")
            pickFirst("lib/x86_64/libmediapipe_jni.so")
            
            // ONNX Runtime native libraries
            pickFirst("lib/arm64-v8a/libonnxruntime.so")
            pickFirst("lib/armeabi-v7a/libonnxruntime.so")
            pickFirst("lib/x86_64/libonnxruntime.so")
            
            // Additional conflict resolutions
            pickFirst("lib/arm64-v8a/libc++_shared.so")
            pickFirst("lib/armeabi-v7a/libc++_shared.so")
            pickFirst("lib/x86_64/libc++_shared.so")
            
            // Exclude duplicate metadata files
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/license.txt"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.txt"
            excludes += "META-INF/notice.txt"
            excludes += "META-INF/*.kotlin_module"
        }
        
        jniLibs {
            useLegacyPackaging = true
            // Keep debugging symbols in debug builds
            keepDebugSymbols += "**/*.so"
        }
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
        quiet = true
        disable += setOf(
            "Deprecation", 
            "ObsoleteLintCustomCheck",
            "MissingTranslation",
            "ExtraTranslation"
        )
    }
    
    // Increase heap size for model compilation
    dexOptions {
        javaMaxHeapSize = "4g"
    }
}

composeCompiler {
    includeSourceInformation.set(true)
    enableStrongSkippingMode.set(true)
    stabilityConfigurationFile.set(rootProject.file("compose_compiler_config.conf"))
}

dependencies {
    // ========================================
    // Core Android
    // ========================================
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("androidx.appcompat:appcompat:1.7.0")

    // ========================================
    // Jetpack Compose
    // ========================================
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.runtime:runtime-livedata")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // ========================================
    // Navigation
    // ========================================
    implementation("androidx.navigation:navigation-compose:2.8.5")

    // ========================================
    // Coroutines
    // ========================================
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")

    // ========================================
    // WorkManager (Phase 1: ModelDownloadManager)
    // ========================================
    implementation("androidx.work:work-runtime-ktx:2.10.0")
    implementation("androidx.work:work-gcm:2.10.0") // For background downloads

    // ========================================
    // Networking
    // ========================================
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // ========================================
    // JSON
    // ========================================
    implementation("com.google.code.gson:gson:2.11.0")

    // ========================================
    // DataStore
    // ========================================
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // ========================================
    // Camera (Phase 2: GestureRecognizerManager)
    // ========================================
    implementation("androidx.camera:camera-camera2:1.4.1")
    implementation("androidx.camera:camera-lifecycle:1.4.1")
    implementation("androidx.camera:camera-view:1.4.1")
    implementation("androidx.camera:camera-extensions:1.4.1")

    // ========================================
    // ML/AI - TensorFlow Lite (Phase 2: Whisper, Chat, Language)
    // ========================================
    // IMPORTANT: Using version 2.16.1 for better compatibility
    implementation("org.tensorflow:tensorflow-lite:2.16.1")
    implementation("org.tensorflow:tensorflow-lite-gpu:2.16.1")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
    implementation("org.tensorflow:tensorflow-lite-select-tf-ops:2.16.1") // For advanced ops
    
    // TFLite Task API (optional, for higher-level APIs)
    implementation("org.tensorflow:tensorflow-lite-task-vision:0.4.4")
    implementation("org.tensorflow:tensorflow-lite-task-text:0.4.4")

    // ========================================
    // ONNX Runtime (Existing)
    // ========================================
    implementation("com.microsoft.onnxruntime:onnxruntime-android:1.17.0")

    // ========================================
    // MediaPipe (Phase 2: GestureRecognizerManager)
    // ========================================
    // IMPORTANT: Using version 0.10.9 for stability
    implementation("com.google.mediapipe:tasks-vision:0.10.9")
    implementation("com.google.mediapipe:tasks-text:0.10.9")
    
    // ========================================
    // ML Kit (Existing)
    // ========================================
    implementation("com.google.mlkit:text-recognition:16.0.1")
    implementation("com.google.mlkit:face-detection:16.1.7")

    // ========================================
    // Permissions
    // ========================================
    implementation("com.google.accompanist:accompanist-permissions:0.36.0")

    // ========================================
    // Image Loading
    // ========================================
    implementation("io.coil-kt:coil-compose:2.7.0")

    // ========================================
    // Animations
    // ========================================
    implementation("com.airbnb.android:lottie-compose:6.6.2")

    // ========================================
    // Encryption
    // ========================================
    implementation("com.google.crypto.tink:tink-android:1.15.0")

    // ========================================
    // Google Sign-In
    // ========================================
    implementation("com.google.android.gms:play-services-auth:21.3.0")

    // ========================================
    // Web Scraping
    // ========================================
    implementation("org.jsoup:jsoup:1.18.3")

    // ========================================
    // Hilt (Dependency Injection - Phase 1 & 2)
    // ========================================
    implementation("com.google.dagger:hilt-android:2.52")
    ksp("com.google.dagger:hilt-compiler:2.52")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("androidx.hilt:hilt-work:1.2.0") // For WorkManager integration
    ksp("androidx.hilt:hilt-compiler:1.2.0")

    // ========================================
    // Room Database
    // ========================================
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // ========================================
    // Testing
    // ========================================
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("com.google.truth:truth:1.4.4")
    testImplementation("org.mockito:mockito-core:5.13.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.work:work-testing:2.10.0")
}

// ========================================
// Custom Tasks for Model Management
// ========================================

tasks.register("validateModels") {
    group = "model"
    description = "Validate all ML model files before build"
    
    doLast {
        val modelsDir = File(projectDir, "src/main/assets")
        println("Validating models in: ${modelsDir.absolutePath}")
        
        if (!modelsDir.exists()) {
            println("Warning: Models directory does not exist. Will be created.")
            modelsDir.mkdirs()
        }
        
        // List expected models
        val expectedModels = listOf(
            "whisper_base_int8.tflite",
            "tinyllama_1_1b_int8.tflite",
            "hand_landmarker.task",
            "mbert_multilingual.tflite"
        )
        
        expectedModels.forEach { modelName ->
            val modelFile = File(modelsDir, modelName)
            if (modelFile.exists()) {
                val sizeMB = modelFile.length() / (1024 * 1024)
                println("✅ Found: $modelName (${sizeMB}MB)")
            } else {
                println("⚠️  Missing: $modelName (will be downloaded on first use)")
            }
        }
    }
}

tasks.register("cleanModels") {
    group = "model"
    description = "Clean cached model files"
    
    doLast {
        val modelsDir = File(projectDir, "src/main/assets")
        if (modelsDir.exists()) {
            modelsDir.listFiles()?.filter { 
                it.extension in listOf("tflite", "task", "onnx") 
            }?.forEach {
                println("Deleting: ${it.name}")
                it.delete()
            }
        }
    }
}

// Run model validation before assembleDebug
tasks.named("assembleDebug") {
    dependsOn("validateModels")
}
