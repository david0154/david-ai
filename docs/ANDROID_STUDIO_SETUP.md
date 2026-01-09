# 🚀 Android Studio Setup Guide - DAVID AI

**Complete step-by-step guide for setting up DAVID AI project in Android Studio**

---

## 📋 Table of Contents

1. [Prerequisites](#prerequisites)
2. [Project Configuration](#project-configuration)
3. [Android Studio Setup](#android-studio-setup)
4. [App Icons & Logo](#app-icons--logo)
5. [Splash Screen](#splash-screen)
6. [Google Login API Setup](#google-login-api-setup)
7. [App Signing](#app-signing)
8. [Build & Run](#build--run)
9. [Troubleshooting](#troubleshooting)

---

## Prerequisites

### System Requirements

```
✅ macOS 10.15+, Windows 10+, or Linux (Ubuntu 18.04+)
✅ 8GB RAM minimum (16GB recommended)
✅ 5GB free disk space
✅ Internet connection for Gradle dependencies
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
- **Recommended**: Latest stable version (2024.1+)
- **Installation**: Standard setup with Android SDK

---

## Project Configuration

### App Name & Package Name

**File**: `app/build.gradle.kts`

```kotlin
android {
    namespace = "com.davidstudioz.david"
    
    compileSdk = 34
    
    defaultConfig {
        applicationId = "com.davidstudioz.david"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "2.0.0"
        
        // App display name
        resValue "string", "app_name", "DAVID AI"
    }
}
```

### Key Configuration Values

| Property | Value | Description |
|----------|-------|-------------|
| **namespace** | `com.davidstudioz.david` | Package namespace |
| **applicationId** | `com.davidstudioz.david` | Unique app identifier (for Play Store) |
| **minSdk** | 28 | Minimum Android version (Android 9) |
| **targetSdk** | 34 | Target Android version (Android 14) |
| **compileSdk** | 34 | Compile against Android 14 |
| **versionCode** | 1 | Internal version (increment for each build) |
| **versionName** | 2.0.0 | User-visible version |
| **appName** | DAVID AI | Display name on launcher |

### Update Version Before Release

```kotlin
versionCode = 2  // Must increment for each Play Store release
versionName = "2.0.1"  // User-visible version
```

---

## Android Studio Setup

### 1. Open Project in Android Studio

```bash
# Clone repository
git clone https://github.com/david0154/david-ai.git
cd david-ai

# Open in Android Studio
open -a "Android Studio" .  # macOS
android-studio .            # Linux
# Or manually: File → Open → Select david-ai folder
```

### 2. Wait for Gradle Sync

- Android Studio automatically syncs Gradle files
- First sync takes 5-10 minutes (downloading dependencies)
- Watch the build output in the bottom panel
- Wait for "Build successful" or green checkmark

### 3. Configure SDK Manager

**Path**: `Android Studio → Preferences → Languages & Frameworks → Android SDK`

**Install these SDKs:**

```
✅ Android SDK 34 (API 34)
✅ Android SDK 28 (API 28) - minSdk
✅ Google Play Services
✅ Google Repository
✅ Android Support Repository
✅ NDK (for native modules)
✅ CMake (for build)
```

### 4. Configure JDK

**Path**: `Android Studio → Preferences → Build, Execution, Deployment → Build Tools → Gradle`

```
Gradle JDK: JDK 17 (or higher)
```

### 5. Virtual Device Setup (Android Emulator)

**Path**: `Tools → Device Manager`

**Create a Test Device:**

```
Device: Pixel 6
OS: Android 14 (API 34)
RAM: 2GB
Storage: 100GB
Skin: Included
```

**Quick Commands:**

```bash
# Start emulator from terminal
emulator -avd Pixel_6_API_34

# Or use Android Studio Device Manager
```

---

## App Icons & Logo

### Generate App Icons

#### Method 1: Android Studio Built-in Tool

1. **Right-click**: `app/src/main/res`
2. **Select**: `New → Image Asset`
3. **Configure**:
   - **Asset Type**: Launcher Icons
   - **Image File**: Select your logo (1024×1024 PNG recommended)
   - **Foreground**: Your logo
   - **Background**: Color or image
   - **Shape**: Rounded Square / Square

#### Method 2: Online Tool

- **Website**: https://roipixel.com/icon-resizer/
- **Upload**: Your 1024×1024 logo
- **Download**: Multi-resolution icon pack
- **Place in**: `app/src/main/res/mipmap-*/ic_launcher.png`

### Icon Sizes Required

```
✅ hdpi (72×72)
✅ mdpi (48×48)
✅ xhdpi (96×96)
✅ xxhdpi (144×144)
✅ xxxhdpi (192×192)
```

### File Structure

```
app/src/main/res/
├── mipmap-hdpi/ic_launcher.png
├── mipmap-mdpi/ic_launcher.png
├── mipmap-xhdpi/ic_launcher.png
├── mipmap-xxhdpi/ic_launcher.png
├── mipmap-xxxhdpi/ic_launcher.png
└── mipmap-anydpi-v33/ic_launcher.xml  # Themed icon
```

### AndroidManifest.xml Configuration

**File**: `app/src/main/AndroidManifest.xml`

```xml
<application
    android:icon="@mipmap/ic_launcher"
    android:roundIcon="@mipmap/ic_launcher_round"
    android:label="@string/app_name"
    android:theme="@style/Theme.DavidAI"
    android:debuggable="false"
    android:usesCleartextTraffic="false">
    
    <!-- Activities defined here -->
</application>
```

---

## Splash Screen

### Modern Splash Screen (Android 12+)

**File**: `app/src/main/res/values/themes.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.DavidAI.Splash" parent="Theme.MaterialComponents.DayNight.NoActionBar">
        <!-- Splash screen branding image -->
        <item name="android:windowBackground">@drawable/splash_screen</item>
        <item name="android:windowNoTitle">true</item>
        <item name="android:windowFullscreen">true</item>
        <item name="android:windowActionBar">false</item>
        <item name="android:windowDrawsSystemBarBackgrounds">true</item>
        <item name="android:statusBarColor">@android:color/transparent</item>
    </style>
</resources>
```

### Splash Activity

**File**: `app/src/main/kotlin/com/davidstudioz/david/SplashActivity.kt`

```kotlin
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Navigate to MainActivity after 2 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 2000)  // 2 second delay
    }
}
```

### AndroidManifest Configuration

```xml
<activity
    android:name=".SplashActivity"
    android:theme="@style/Theme.DavidAI.Splash"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>

<activity
    android:name=".MainActivity"
    android:exported="true"
    android:theme="@style/Theme.DavidAI" />
```

### Splash Screen Image

**File**: `app/src/main/res/drawable/splash_screen.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<layer-list xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- Background color -->
    <item android:drawable="@color/splash_background" />
    
    <!-- Logo centered -->
    <item>
        <bitmap
            android:src="@drawable/logo"
            android:gravity="center" />
    </item>
</layer-list>
```

---

## Google Login API Setup

### Step 1: Create Firebase Project

1. **Go to**: https://console.firebase.google.com
2. **Click**: "Add project"
3. **Enter Project Name**: "DAVID AI"
4. **Accept terms** → **Create project**
5. **Wait** for project initialization

### Step 2: Register Android App

1. **Click**: "Android" icon
2. **Enter Package Name**: `com.davidstudioz.david`
3. **Enter App Nickname**: "DAVID AI (Android)"
4. **Get SHA-1 Certificate**:

```bash
# Generate SHA-1 fingerprint
./gradlew signingReport

# Or manually:
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

5. **Paste SHA-1** into Firebase
6. **Download**: `google-services.json`
7. **Place file**: `app/google-services.json`

### Step 3: Add Firebase Dependencies

**File**: `app/build.gradle.kts`

```kotlin
plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    kotlin("android")
    kotlin("plugin.serialization")
}

dependencies {
    // Firebase
    implementation("com.google.firebase:firebase-auth:22.3.1")
    implementation("com.google.firebase:firebase-auth-ktx:22.3.1")
    implementation("com.google.firebase:firebase-analytics:21.5.0")
    
    // Google Play Services
    implementation("com.google.android.gms:play-services-auth:20.7.0")
}
```

**File**: `build.gradle.kts` (Root)

```kotlin
plugins {
    id("com.google.gms.google-services") version "4.4.0" apply false
}
```

### Step 4: Configure Firebase Authentication

**Firebase Console → Authentication → Sign-in method**

1. **Enable**: Google
2. **Select Project**: Your Firebase project
3. **Click**: "Save"

### Step 5: Implement Google Login in Code

**File**: `app/src/main/kotlin/com/davidstudioz/david/auth/GoogleLoginManager.kt`

```kotlin
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class GoogleLoginManager(context: Context) {
    private val googleSignInClient: GoogleSignInClient
    private val firebaseAuth = FirebaseAuth.getInstance()
    
    init {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        
        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }
    
    fun signIn(activity: Activity, callback: (Boolean, String?) -> Unit) {
        val signInIntent = googleSignInClient.signInIntent
        activity.startActivityForResult(signInIntent, RC_SIGN_IN)
    }
    
    fun handleSignInResult(data: Intent, callback: (Boolean, String?) -> Unit) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            firebaseAuthWithGoogle(account.idToken!!, callback)
        } catch (e: ApiException) {
            callback(false, e.message)
        }
    }
    
    private fun firebaseAuthWithGoogle(idToken: String, callback: (Boolean, String?) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, firebaseAuth.currentUser?.email)
                } else {
                    callback(false, task.exception?.message)
                }
            }
    }
    
    companion object {
        const val RC_SIGN_IN = 9001
    }
}
```

### Step 6: Google Services Configuration

**File**: `app/src/main/res/values/strings.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">DAVID AI</string>
    <!-- Firebase Web Client ID from google-services.json -->
    <string name="default_web_client_id">YOUR_WEB_CLIENT_ID_HERE</string>
</resources>
```

---

## App Signing

### Create Signing Key (Release Build)

#### Method 1: Android Studio UI

1. **Menu**: `Build → Generate Signed Bundle / APK`
2. **Select**: APK
3. **Click**: "Create new"
4. **Fill Details**:
   - **Key store path**: `/path/to/david_keystore.jks`
   - **Password**: Your secure password (min 6 chars)
   - **Alias**: `david-key`
   - **Alias password**: Same as above
   - **Validity**: 25+ years
   - **Certificate info**: Your name, organization, etc.
5. **Click**: "Create"

#### Method 2: Command Line

```bash
keytool -genkey -v -keystore david_keystore.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias david-key

# Enter password and information when prompted
# Store keystore file securely: app/keystore/david_keystore.jks
```

### Configure Release Signing

**File**: `app/build.gradle.kts`

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("keystore/david_keystore.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "password"
            keyAlias = "david-key"
            keyPassword = System.getenv("KEY_PASSWORD") ?: "password"
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
    }
}
```

### Build Signed Release APK

```bash
# Set passwords as environment variables
export KEYSTORE_PASSWORD="your_keystore_password"
export KEY_PASSWORD="your_key_password"

# Build release APK
./gradlew assembleRelease

# Output: app/build/outputs/apk/release/app-release.apk
```

### Verify Signed APK

```bash
keytool -printcert -jarfile app/build/outputs/apk/release/app-release.apk
```

---

## Build & Run

### Build Debug APK

```bash
# Build debug APK
./gradlew assembleDebug

# Output: app/build/outputs/apk/debug/app-debug.apk

# Install on connected device
./gradlew installDebug
```

### Run on Emulator

```bash
# Make sure emulator is running
emulator -avd Pixel_6_API_34 &

# Build and run
./gradlew installDebug

# Or click "Run" in Android Studio (Shift+F10)
```

### Run on Physical Device

1. **Connect device** via USB
2. **Enable Developer Mode** on device:
   - `Settings → About Phone → Tap Build Number 7 times`
   - `Settings → Developer Options → Enable USB Debugging`
3. **Accept USB Authorization** on device
4. **Run in Android Studio**: `Shift+F10`

### Build Release APK for Play Store

```bash
# Set environment variables
export KEYSTORE_PASSWORD="your_password"
export KEY_PASSWORD="your_password"

# Build release
./gradlew bundleRelease

# Output: app/build/outputs/bundle/release/app-release.aab
# Upload to Google Play Console
```

---

## Troubleshooting

### Gradle Sync Issues

```bash
# Clear Gradle cache
./gradlew clean

# Rebuild
./gradlew build

# If still failing:
rm -rf ~/.gradle
./gradlew --refresh-dependencies build
```

### Build Fails: "google-services.json not found"

```bash
# Ensure file exists at correct path
ls -la app/google-services.json

# If missing:
# 1. Go to Firebase Console
# 2. Project settings → Download google-services.json
# 3. Place in app/ folder
```

### Emulator Slow or Crashes

```bash
# Start with more RAM
emulator -avd Pixel_6_API_34 -memory 2048 -gpu on

# Or use hardware acceleration:
# Settings → Virtual Devices → Edit → Hardware acceleration → "Automatic"
```

### SHA-1 Fingerprint Error

```bash
# Regenerate SHA-1
./gradlew signingReport

# Copy output SHA-1 value
# Go to Firebase Console → Project Settings → Android Apps
# Add/Update the SHA-1 fingerprint
```

### Port Already in Use (Gradle Daemon)

```bash
# Kill Gradle daemon
./gradlew --stop

# Rebuild
./gradlew build
```

### Memory Errors During Build

**File**: `gradle.properties`

```properties
org.gradle.jvmargs=-Xmx4096m
org.gradle.parallel=true
org.gradle.caching=true
```

### Kotlin Version Mismatch

```bash
# Update Kotlin plugin in Android Studio
Tools → Kotlin → Configure Kotlin Plugin Updates

# Or manually update: app/build.gradle.kts
kotlin("android") version "1.9.22"
```

---

## Environment Variables for CI/CD

**For automated builds (GitHub Actions, etc.):**

```bash
# Set these in your CI/CD platform:
export KEYSTORE_PASSWORD="your_keystore_password"
export KEY_PASSWORD="your_key_password"
export FIREBASE_TOKEN="your_firebase_token"
```

---

## Quick Reference Commands

```bash
# Clone and setup
git clone https://github.com/david0154/david-ai.git
cd david-ai
./gradlew build

# Download AI models
./scripts/download-models.sh

# Debug build and run
./gradlew installDebug

# Run tests
./gradlew test
androidTest

# Build release
export KEYSTORE_PASSWORD="password"
export KEY_PASSWORD="password"
./gradlew bundleRelease

# Clean cache
./gradlew clean

# Generate signed APK
./gradlew assembleRelease
```

---

## Security Checklist

- ✅ Never commit `google-services.json` (add to `.gitignore`)
- ✅ Never commit keystore files (add `*.jks` to `.gitignore`)
- ✅ Use environment variables for sensitive data
- ✅ Regenerate keys if credentials are exposed
- ✅ Enable ProGuard/R8 for release builds
- ✅ Use HTTPS for all API calls
- ✅ Disable debuggable in release builds
- ✅ Regularly update dependencies

---

## Next Steps

1. ✅ Complete all setup steps above
2. ✅ Build and test on emulator/device
3. ✅ Generate release signing key
4. ✅ Download models with `./scripts/download-models.sh`
5. ✅ Read `README.md` for feature overview
6. ✅ Check other guides in `/docs` folder
7. ✅ Test voice features and device integration
8. ✅ Prepare for Play Store submission

---

**For questions or issues**, open an issue on [GitHub](https://github.com/david0154/david-ai/issues)

**Last Updated**: January 2026  
**Android Studio Version**: 2024.1+  
**Kotlin Version**: 1.9.22+  
**Gradle Version**: 8.1+  
