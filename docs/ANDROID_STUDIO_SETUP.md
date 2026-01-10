# 🚀 Android Studio Setup Guide - D.A.V.I.D AI

**Complete step-by-step guide for setting up D.A.V.I.D AI project in Android Studio**

**Developed by [Nexuzy Tech Ltd.](mailto:david@nexuzy.in)**

---

## 📋 Table of Contents

1. [Prerequisites](#prerequisites)
2. [Project Configuration](#project-configuration)
3. [Android Studio Setup](#android-studio-setup)
4. [AI Models Setup](#ai-models-setup)
5. [Multi-Language Setup](#multi-language-setup)
6. [App Icons & Branding](#app-icons--branding)
7. [Feature Configuration](#feature-configuration)
8. [App Signing](#app-signing)
9. [Build & Run](#build--run)
10. [Troubleshooting](#troubleshooting)

---

## Prerequisites

### System Requirements

```
✅ macOS 10.15+, Windows 10+, or Linux (Ubuntu 18.04+)
✅ 8GB RAM minimum (16GB recommended)
✅ 8GB free disk space (for AI models)
✅ Internet connection for dependencies & models
```

### Required Software

```bash
# Install Java Development Kit (JDK 17)
# macOS:
brew install openjdk@17

# Windows: Download from https://adoptopenjdk.net/

# Linux (Ubuntu):
sudo apt-get install openjdk-17-jdk
```

### Download Android Studio

- **URL**: https://developer.android.com/studio
- **Recommended**: Latest stable version (Hedgehog 2023.1.1+)
- **Installation**: Standard setup with Android SDK

---

## Project Configuration

### App Name & Package Name

**File**: `app/build.gradle.kts`

```kotlin
android {
    namespace = "com.nexuzy.david"
    
    compileSdk = 34
    
    defaultConfig {
        applicationId = "com.nexuzy.david"
        minSdk = 26  // Android 8.0 Oreo
        targetSdk = 34  // Android 14
        versionCode = 1
        versionName = "1.0.0"
        
        // App display name
        resValue("string", "app_name", "D.A.V.I.D AI")
        
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}
```

### Key Configuration Values

| Property | Value | Description |
|----------|-------|-------------|
| **namespace** | `com.nexuzy.david` | Package namespace |
| **applicationId** | `com.nexuzy.david` | Unique app ID (Play Store) |
| **minSdk** | 26 | Android 8.0+ (for AI features) |
| **targetSdk** | 34 | Target Android 14 |
| **compileSdk** | 34 | Compile against Android 14 |
| **versionCode** | 1 | Internal version (increment) |
| **versionName** | 1.0.0 | User-visible version |
| **appName** | D.A.V.I.D AI | Launcher display name |

### App Branding

**Nexuzy Tech Ltd. Information:**

```kotlin
// In About page:
val DEVELOPER = "Nexuzy Tech Ltd."
val SUPPORT_EMAIL = "david@nexuzy.in"
val GITHUB_REPO = "https://github.com/david0154/david-ai"
```

---

## Android Studio Setup

### 1. Clone and Open Project

```bash
# Clone repository
git clone https://github.com/david0154/david-ai.git
cd david-ai

# Open in Android Studio
open -a "Android Studio" .  # macOS
android-studio .            # Linux
# Windows: File → Open → Select david-ai folder
```

### 2. Wait for Gradle Sync

- Android Studio automatically syncs Gradle
- First sync: 5-10 minutes (dependencies)
- Watch build output (bottom panel)
- Wait for "Build successful"

### 3. Configure SDK Manager

**Path**: `Settings → Languages & Frameworks → Android SDK`

**Install Required SDKs:**

```
✅ Android 14.0 (API 34) - Compile SDK
✅ Android 8.0 (API 26) - Min SDK
✅ Google Play Services
✅ Android SDK Build-Tools 34.0.0
✅ NDK (Side by side) - For native libs
✅ CMake - For build system
```

### 4. Configure JDK

**Path**: `Settings → Build, Execution, Deployment → Build Tools → Gradle`

```
Gradle JDK: JDK 17 (or 21)
```

### 5. Configure Emulator

**Path**: `Tools → Device Manager → Create Device`

**Recommended Configuration:**

```
Device: Pixel 6 Pro
System Image: Android 14 (API 34) with Google Play
RAM: 4GB (for AI models)
Storage: 8GB internal
Graphics: Hardware (GLES 2.0)
```

---

## AI Models Setup

### Dependencies for AI Models

**File**: `app/build.gradle.kts`

```kotlin
dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    
    // Jetpack Compose
    val composeBom = platform("androidx.compose:compose-bom:2024.01.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.activity:activity-compose:1.8.2")
    
    // AI Models Dependencies
    
    // 1. Whisper (Voice Recognition)
    implementation("com.whispercppmobile:whisper:1.0.0")
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    
    // 2. ONNX Runtime (Vision & LLM)
    implementation("com.microsoft.onnxruntime:onnxruntime-android:1.17.0")
    
    // 3. MediaPipe (Gesture Recognition)
    implementation("com.google.mediapipe:tasks-vision:0.10.9")
    implementation("com.google.mediapipe:tasks-hands:0.10.9")
    
    // 4. TensorFlow Lite (Language Models)
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
    implementation("org.tensorflow:tensorflow-lite-select-tf-ops:2.14.0")
    
    // HTTP Client (For Model Downloads)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    
    // Coroutines (Async operations)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    
    // ViewModel & LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    
    // Room Database (Chat history)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    
    // CameraX (For gesture control)
    val camerax_version = "1.3.1"
    implementation("androidx.camera:camera-core:$camerax_version")
    implementation("androidx.camera:camera-camera2:$camerax_version")
    implementation("androidx.camera:camera-lifecycle:$camerax_version")
    implementation("androidx.camera:camera-view:$camerax_version")
}
```

### AI Models Structure

**Location**: `app/src/main/assets/models/`

```
app/src/main/assets/models/
├── voice/
│   ├── ggml-tiny.en.bin          # 75 MB
│   ├── ggml-base.en.bin          # 142 MB
│   └── ggml-small.en.bin         # 466 MB
├── chat/
│   ├── tinyllama-chat.gguf       # 669 MB
│   ├── qwen-chat.gguf            # 1.1 GB
│   └── phi-2-chat.gguf           # 1.6 GB
├── vision/
│   ├── mobilenetv2.onnx          # 14 MB
│   └── resnet50.onnx             # 98 MB
├── gesture/
│   ├── hand_landmarker.task      # 25 MB
│   └── gesture_recognizer.task   # 31 MB
└── languages/
    ├── en_model.tflite           # 50 MB each
    ├── hi_model.tflite
    ├── ta_model.tflite
    └── ... (15 languages total)
```

### Model Download Configuration

**File**: `app/src/main/kotlin/com/nexuzy/david/ai/ModelManager.kt`

```kotlin
object ModelDownloader {
    private val MODELS = mapOf(
        "voice_tiny" to "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-tiny.en.bin",
        "voice_base" to "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-base.en.bin",
        "chat_tiny" to "https://huggingface.co/TheBloke/TinyLlama-1.1B-Chat-v1.0-GGUF/resolve/main/tinyllama-1.1b-chat-v1.0.Q4_K_M.gguf",
        "vision_lite" to "https://github.com/onnx/models/raw/main/vision/classification/mobilenet/model/mobilenetv2-12.onnx",
        "gesture_hand" to "https://storage.googleapis.com/mediapipe-models/hand_landmarker/hand_landmarker/float16/1/hand_landmarker.task",
        "gesture_recognizer" to "https://storage.googleapis.com/mediapipe-models/gesture_recognizer/gesture_recognizer/float16/1/gesture_recognizer.task"
    )
    
    suspend fun downloadModel(modelKey: String, progressCallback: (Int) -> Unit) {
        // Implementation in ModelManager.kt
    }
}
```

---

## Multi-Language Setup

### Supported Languages Configuration

**File**: `app/src/main/kotlin/com/nexuzy/david/language/LanguageManager.kt`

```kotlin
enum class SupportedLanguage(val code: String, val displayName: String, val nativeName: String) {
    ENGLISH("en", "English", "English"),
    HINDI("hi", "Hindi", "हिन्दी"),
    TAMIL("ta", "Tamil", "தமிழ்"),
    TELUGU("te", "Telugu", "తెలుగు"),
    BENGALI("bn", "Bengali", "বাংলা"),
    MARATHI("mr", "Marathi", "मराठी"),
    GUJARATI("gu", "Gujarati", "ગુજરાતી"),
    KANNADA("kn", "Kannada", "ಕನ್ನಡ"),
    MALAYALAM("ml", "Malayalam", "മലയാളം"),
    PUNJABI("pa", "Punjabi", "ਪੰਜਾਬੀ"),
    ODIA("or", "Odia", "ଓଡ଼ିଆ"),
    URDU("ur", "Urdu", "اردو"),
    SANSKRIT("sa", "Sanskrit", "संस्कृतम्"),
    KASHMIRI("ks", "Kashmiri", "कॉशुर"),
    ASSAMESE("as", "Assamese", "অসমীয়া")
}
```

### String Resources for Multiple Languages

**File Structure:**

```
app/src/main/res/
├── values/strings.xml              # English (default)
├── values-hi/strings.xml           # Hindi
├── values-ta/strings.xml           # Tamil
├── values-te/strings.xml           # Telugu
├── values-bn/strings.xml           # Bengali
├── values-mr/strings.xml           # Marathi
├── values-gu/strings.xml           # Gujarati
├── values-kn/strings.xml           # Kannada
├── values-ml/strings.xml           # Malayalam
├── values-pa/strings.xml           # Punjabi
├── values-or/strings.xml           # Odia
├── values-ur/strings.xml           # Urdu
├── values-sa/strings.xml           # Sanskrit
├── values-ks/strings.xml           # Kashmiri
└── values-as/strings.xml           # Assamese
```

**Example**: `app/src/main/res/values/strings.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">D.A.V.I.D AI</string>
    <string name="voice_control">Voice Control</string>
    <string name="gesture_control">Gesture Control</string>
    <string name="settings">Settings</string>
    <string name="about">About</string>
    <string name="developer">Developed by Nexuzy Tech Ltd.</string>
    <string name="support_email">david@nexuzy.in</string>
    <string name="privacy_policy">Privacy Policy</string>
    <string name="no_data_collection">We do not collect any data. Everything is processed locally on your device.</string>
</resources>
```

---

## App Icons & Branding

### Generate D.A.V.I.D AI Icons

#### Method 1: Android Studio Tool

1. Right-click `app/src/main/res`
2. Select `New → Image Asset`
3. Configure:
   - **Asset Type**: Launcher Icons (Adaptive and Legacy)
   - **Name**: `ic_launcher`
   - **Foreground**: Upload your D.A.V.I.D AI logo
   - **Background**: Solid color or gradient
   - **Shape**: Rounded Square
4. Click **Next** → **Finish**

#### Method 2: Manual Icon Creation

**Required Sizes:**

```
✅ mipmap-mdpi: 48×48px
✅ mipmap-hdpi: 72×72px
✅ mipmap-xhdpi: 96×96px
✅ mipmap-xxhdpi: 144×144px
✅ mipmap-xxxhdpi: 192×192px
```

### App Manifest Configuration

**File**: `app/src/main/AndroidManifest.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">
    
    <!-- Permissions -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.BLUETOOTH" />
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
    <uses-permission android:name="android.permission.CALL_PHONE" />
    <uses-permission android:name="android.permission.SEND_SMS" />
    
    <application
        android:name=".DavidApplication"
        android:allowBackup="false"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="false"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.DavidAI"
        android:usesCleartextTraffic="false"
        tools:targetApi="31">
        
        <!-- Main Activity -->
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.DavidAI"
            android:screenOrientation="portrait">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        
        <!-- Settings Activity -->
        <activity
            android:name=".SettingsActivity"
            android:exported="false"
            android:parentActivityName=".MainActivity" />
        
        <!-- About Activity -->
        <activity
            android:name=".AboutActivity"
            android:exported="false"
            android:parentActivityName=".SettingsActivity" />
            
        <!-- Services -->
        <service
            android:name=".service.VoiceRecognitionService"
            android:enabled="true"
            android:exported="false" />
            
        <service
            android:name=".service.GestureOverlayService"
            android:enabled="true"
            android:exported="false" />
    </application>
</manifest>
```

---

## Feature Configuration

### Voice Control Setup

**File**: `app/src/main/kotlin/com/nexuzy/david/voice/VoiceController.kt`

```kotlin
class VoiceController(private val context: Context) {
    private val whisperModel: WhisperModel
    private val commandProcessor: CommandProcessor
    
    init {
        // Load Whisper model based on device RAM
        val modelSize = when {
            getTotalRAM() < 2048 -> "tiny"  // < 2GB RAM
            getTotalRAM() < 4096 -> "base"  // 2-4GB RAM
            else -> "small"                  // 4GB+ RAM
        }
        
        whisperModel = WhisperModel.load("models/voice/ggml-$modelSize.en.bin")
        commandProcessor = CommandProcessor(context)
    }
    
    suspend fun processVoiceCommand(audioData: ByteArray): CommandResult {
        val transcription = whisperModel.transcribe(audioData)
        return commandProcessor.execute(transcription)
    }
}
```

### Gesture Control Setup

**File**: `app/src/main/kotlin/com/nexuzy/david/gesture/GestureController.kt`

```kotlin
class GestureController(private val context: Context) {
    private val handLandmarker: HandLandmarker
    private val gestureRecognizer: GestureRecognizer
    private val overlayManager: OverlayManager
    
    init {
        // Load MediaPipe models
        handLandmarker = HandLandmarker.createFromFile(
            context,
            "models/gesture/hand_landmarker.task"
        )
        
        gestureRecognizer = GestureRecognizer.createFromFile(
            context,
            "models/gesture/gesture_recognizer.task"
        )
        
        overlayManager = OverlayManager(context)
    }
    
    fun processFrame(bitmap: Bitmap): GestureResult {
        val handResult = handLandmarker.detect(bitmap)
        val gestureResult = gestureRecognizer.recognize(bitmap)
        
        // Update pointer position
        if (handResult.landmarks.isNotEmpty()) {
            val indexFinger = handResult.landmarks[0][8] // Index finger tip
            overlayManager.updatePointerPosition(indexFinger.x, indexFinger.y)
        }
        
        return gestureResult
    }
}
```

### Device Control Setup

**File**: `app/src/main/kotlin/com/nexuzy/david/device/DeviceController.kt`

```kotlin
class DeviceController(private val context: Context) {
    private val wifiManager: WifiManager
    private val bluetoothAdapter: BluetoothAdapter?
    private val locationManager: LocationManager
    
    init {
        wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }
    
    fun toggleWifi(enable: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ - Open WiFi settings
            context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        } else {
            wifiManager.isWifiEnabled = enable
        }
    }
    
    fun toggleBluetooth(enable: Boolean) {
        bluetoothAdapter?.let {
            if (enable) it.enable() else it.disable()
        }
    }
    
    fun toggleFlashlight(enable: Boolean) {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = cameraManager.cameraIdList[0]
        cameraManager.setTorchMode(cameraId, enable)
    }
}
```

---

## App Signing

### Create Release Keystore

```bash
# Generate keystore
keytool -genkey -v -keystore david_keystore.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias david-release-key

# Enter details:
# - Password: [secure password]
# - Name: Nexuzy Tech Ltd.
# - Organization: Nexuzy Tech Ltd.
# - City: [Your city]
# - State: [Your state]
# - Country: IN

# Move to secure location
mkdir -p app/keystore
mv david_keystore.jks app/keystore/
```

### Configure Signing in Gradle

**File**: `app/build.gradle.kts`

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("keystore/david_keystore.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "your_password"
            keyAlias = "david-release-key"
            keyPassword = System.getenv("KEY_PASSWORD") ?: "your_password"
        }
    }
    
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }
}
```

### ProGuard Rules for AI Models

**File**: `app/proguard-rules.pro`

```pro
# Keep AI model classes
-keep class org.tensorflow.** { *; }
-keep class com.microsoft.onnxruntime.** { *; }
-keep class com.google.mediapipe.** { *; }
-keep class com.whispercppmobile.** { *; }

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep model files
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# Nexuzy Tech Ltd. classes
-keep class com.nexuzy.david.** { *; }
```

---

## Build & Run

### Build Commands

```bash
# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk

# Build release APK
export KEYSTORE_PASSWORD="your_password"
export KEY_PASSWORD="your_password"
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk

# Build AAB for Play Store
./gradlew bundleRelease
# Output: app/build/outputs/bundle/release/app-release.aab

# Install on device
./gradlew installDebug

# Run tests
./gradlew test
./gradlew connectedAndroidTest
```

### Run on Emulator

```bash
# List available emulators
emulator -list-avds

# Start emulator
emulator -avd Pixel_6_Pro_API_34 -memory 4096 -gpu on &

# Install and run
./gradlew installDebug
adb shell am start -n com.nexuzy.david/.MainActivity
```

### Run on Physical Device

1. Enable Developer Options:
   - Settings → About Phone → Tap Build Number 7 times
2. Enable USB Debugging:
   - Settings → Developer Options → USB Debugging
3. Connect device via USB
4. Accept USB debugging authorization
5. Run: `./gradlew installDebug`

---

## Troubleshooting

### AI Model Loading Issues

```kotlin
// Check if models exist
val modelFile = File(context.filesDir, "models/voice/ggml-tiny.en.bin")
if (!modelFile.exists()) {
    // Download models
    ModelDownloader.downloadAllModels()
}
```

### Memory Issues with Large Models

**File**: `gradle.properties`

```properties
org.gradle.jvmargs=-Xmx8192m -XX:MaxMetaspaceSize=512m
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configureondemand=true
android.enableJetifier=true
android.useAndroidX=true
```

### CameraX Issues

```bash
# Add to AndroidManifest.xml
<uses-feature android:name="android.hardware.camera" />
<uses-feature android:name="android.hardware.camera.autofocus" />
```

### Gesture Overlay Not Showing

```kotlin
// Request SYSTEM_ALERT_WINDOW permission
if (!Settings.canDrawOverlays(context)) {
    val intent = Intent(
        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
        Uri.parse("package:${context.packageName}")
    )
    startActivity(intent)
}
```

### Build Fails with "Duplicate Class" Error

```kotlin
// Add to app/build.gradle.kts
android {
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
        }
    }
}
```

---

## Testing

### Unit Tests

```bash
# Run all unit tests
./gradlew test

# Run specific test
./gradlew test --tests "com.nexuzy.david.VoiceControllerTest"
```

### Instrumented Tests

```bash
# Run all instrumented tests
./gradlew connectedAndroidTest

# Run on specific device
adb -s <device-id> shell am instrument -w com.nexuzy.david.test/androidx.test.runner.AndroidJUnitRunner
```

### Test AI Models

```kotlin
@Test
fun testWhisperModel() {
    val whisper = WhisperModel.load("models/voice/ggml-tiny.en.bin")
    val result = whisper.transcribe(testAudioData)
    assertTrue(result.isNotEmpty())
}

@Test
fun testGestureRecognition() {
    val recognizer = GestureRecognizer.createFromFile(context, "models/gesture/gesture_recognizer.task")
    val result = recognizer.recognize(testImage)
    assertNotNull(result)
}
```

---

## Deployment

### Generate Signed Release

```bash
# Set environment variables
export KEYSTORE_PASSWORD="your_secure_password"
export KEY_PASSWORD="your_secure_password"

# Build release
./gradlew clean
./gradlew bundleRelease

# Verify signature
jarsigner -verify -verbose -certs app/build/outputs/bundle/release/app-release.aab
```

### Upload to Play Store

1. Go to [Google Play Console](https://play.google.com/console)
2. Create new app: "D.A.V.I.D AI"
3. Fill app details:
   - **Developer**: Nexuzy Tech Ltd.
   - **Email**: david@nexuzy.in
   - **Category**: Productivity
4. Upload `app-release.aab`
5. Complete store listing
6. Submit for review

---

## Quick Reference

### Essential Files

```
david-ai/
├── app/
│   ├── build.gradle.kts          # App configuration
│   ├── proguard-rules.pro        # ProGuard rules
│   ├── src/main/
│   │   ├── AndroidManifest.xml   # Permissions & activities
│   │   ├── kotlin/com/nexuzy/david/
│   │   │   ├── MainActivity.kt
│   │   │   ├── ai/ModelManager.kt
│   │   │   ├── voice/VoiceController.kt
│   │   │   ├── gesture/GestureController.kt
│   │   │   └── device/DeviceController.kt
│   │   ├── res/
│   │   │   ├── values/strings.xml
│   │   │   ├── values-*/strings.xml  # 15 languages
│   │   │   └── mipmap-*/ic_launcher.png
│   │   └── assets/models/        # AI models (after download)
│   └── keystore/
│       └── david_keystore.jks    # Release signing key
├── build.gradle.kts              # Root build file
├── gradle.properties             # Gradle configuration
└── settings.gradle.kts           # Project settings
```

### Key Commands

```bash
# Setup
git clone https://github.com/david0154/david-ai.git
cd david-ai
./gradlew build

# Development
./gradlew assembleDebug installDebug

# Testing
./gradlew test connectedAndroidTest

# Release
export KEYSTORE_PASSWORD="password"
export KEY_PASSWORD="password"
./gradlew bundleRelease

# Clean
./gradlew clean
rm -rf .gradle build
```

---

## Security Checklist

- ✅ Never commit keystore files (add to `.gitignore`)
- ✅ Use environment variables for passwords
- ✅ Enable ProGuard/R8 for release
- ✅ Set `debuggable = false` in release
- ✅ Use HTTPS for model downloads
- ✅ Validate all user inputs
- ✅ Request minimum permissions
- ✅ Store sensitive data encrypted
- ✅ Regularly update dependencies
- ✅ Test on multiple devices/Android versions

---

## Support

### Need Help?

- 📧 **Email**: [david@nexuzy.in](mailto:david@nexuzy.in)
- 🐛 **Bug Reports**: [GitHub Issues](https://github.com/david0154/david-ai/issues/new?template=bug_report.md)
- ✨ **Feature Requests**: [GitHub Issues](https://github.com/david0154/david-ai/issues/new?template=feature_request.md)
- 📖 **Documentation**: [GitHub Wiki](https://github.com/david0154/david-ai/wiki)
- 💬 **Discussions**: [GitHub Discussions](https://github.com/david0154/david-ai/discussions)

---

## Next Steps

1. ✅ Complete Android Studio setup
2. ✅ Build and test on emulator
3. ✅ Download AI models
4. ✅ Test voice commands
5. ✅ Test gesture control
6. ✅ Test all 15 languages
7. ✅ Generate release keystore
8. ✅ Build signed APK
9. ✅ Test on physical devices
10. ✅ Prepare Play Store listing

---

**Last Updated**: January 10, 2026  
**Android Studio**: Hedgehog 2023.1.1+  
**Kotlin**: 1.9.22+  
**Gradle**: 8.2+  
**Target SDK**: Android 14 (API 34)  
**Min SDK**: Android 8.0 (API 26)  

**© 2026 Nexuzy Tech Ltd. All rights reserved.**
