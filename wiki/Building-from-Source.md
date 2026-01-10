# 🛠️ Building from Source

**Complete guide to building D.A.V.I.D AI from source code**

---

## 📋 Prerequisites

### System Requirements

**Operating System:**
- ✅ macOS 10.15+ (Catalina or later)
- ✅ Windows 10/11 (64-bit)
- ✅ Linux (Ubuntu 18.04+, Debian 10+, or equivalent)

**Hardware:**
- **CPU**: Intel i5/AMD Ryzen 5 or better
- **RAM**: 8GB minimum (16GB recommended)
- **Storage**: 10GB free space
- **Internet**: For downloading dependencies

---

### Required Software

#### 1. Android Studio

**Download:** https://developer.android.com/studio

**Version:** Hedgehog (2023.1.1) or later

**Installation:**
```bash
# macOS (via Homebrew)
brew install --cask android-studio

# Linux (Snap)
sudo snap install android-studio --classic

# Windows: Download installer from website
```

---

#### 2. Java Development Kit (JDK)

**Required:** JDK 17 or later

```bash
# macOS (via Homebrew)
brew install openjdk@17

# Linux (Ubuntu/Debian)
sudo apt-get update
sudo apt-get install openjdk-17-jdk

# Linux (Fedora)
sudo dnf install java-17-openjdk-devel

# Windows: Download from https://adoptium.net/
```

**Verify Installation:**
```bash
java -version
# Should show: openjdk version "17.x.x"
```

---

#### 3. Git

```bash
# macOS (via Homebrew)
brew install git

# Linux (Ubuntu/Debian)
sudo apt-get install git

# Linux (Fedora)
sudo dnf install git

# Windows: Download from https://git-scm.com/
```

**Verify Installation:**
```bash
git --version
```

---

## 📥 Getting the Source Code

### Clone Repository

```bash
# Clone the repository
git clone https://github.com/david0154/david-ai.git

# Navigate to project directory
cd david-ai

# Check current branch
git branch

# Switch to main branch (if not already)
git checkout main
```

### Fork Repository (Optional)

If you plan to contribute:

1. Go to https://github.com/david0154/david-ai
2. Click "Fork" button (top right)
3. Clone your fork:
   ```bash
   git clone https://github.com/YOUR_USERNAME/david-ai.git
   cd david-ai
   git remote add upstream https://github.com/david0154/david-ai.git
   ```

---

## ⚙️ Android Studio Setup

### 1. Open Project

**Method 1: From Android Studio**
1. Launch Android Studio
2. Click "Open"
3. Navigate to `david-ai` folder
4. Click "OK"

**Method 2: From Command Line**
```bash
# macOS
open -a "Android Studio" .

# Linux
android-studio .

# Windows
start "" "C:\Program Files\Android\Android Studio\bin\studio64.exe" .
```

---

### 2. Gradle Sync

**Automatic:**
- Android Studio automatically syncs Gradle on project open
- Wait for "Gradle sync successful" message
- First sync takes 5-10 minutes (downloads dependencies)

**Manual:**
- Click "Sync Project with Gradle Files" button (toolbar)
- Or: `File > Sync Project with Gradle Files`

**Troubleshooting:**
```bash
# If sync fails, try:
./gradlew clean
./gradlew build --refresh-dependencies
```

---

### 3. SDK Configuration

**Required SDKs:**
- Android SDK 34 (Android 14) - Compile SDK
- Android SDK 26 (Android 8.0) - Minimum SDK

**To Install:**
1. Open `Tools > SDK Manager`
2. Select `SDK Platforms` tab
3. Check:
   - ✅ Android 14.0 (API 34)
   - ✅ Android 8.0 (API 26)
4. Select `SDK Tools` tab
5. Check:
   - ✅ Android SDK Build-Tools 34.0.0
   - ✅ NDK (Side by side)
   - ✅ CMake
   - ✅ Android SDK Platform-Tools
6. Click "Apply" and wait for downloads

---

### 4. JDK Configuration

**Set Gradle JDK:**
1. Open `File > Settings` (Windows/Linux) or `Android Studio > Preferences` (macOS)
2. Navigate to `Build, Execution, Deployment > Build Tools > Gradle`
3. Set `Gradle JDK` to `JDK 17` or higher
4. Click "Apply" and "OK"

---

## 🔧 Build Configuration

### Build Variants

D.A.V.I.D AI has two build variants:

**Debug:**
- Debugging enabled
- No code optimization
- Larger APK size
- Includes debug symbols
- Package: `com.nexuzy.david.debug`

**Release:**
- Debugging disabled
- Code optimized (R8/ProGuard)
- Smaller APK size
- Requires signing key
- Package: `com.nexuzy.david`

**Switch Variant:**
- `Build > Select Build Variant`
- Choose `debug` or `release`

---

### Signing Configuration

#### Debug Signing (Automatic)

- Uses debug keystore automatically
- No configuration needed
- Debug keystore location:
  - macOS/Linux: `~/.android/debug.keystore`
  - Windows: `C:\Users\USERNAME\.android\debug.keystore`

#### Release Signing (Manual)

**1. Create Keystore:**

```bash
keytool -genkey -v -keystore david_release.keystore \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias david-release-key
```

**Enter details:**
```
Password: [your secure password]
First and Last Name: Nexuzy Tech Ltd.
Organizational Unit: Development
Organization: Nexuzy Tech Ltd.
City: [Your City]
State: [Your State]
Country Code: IN
```

**2. Configure Signing:**

Create `keystore.properties` in project root:

```properties
storePassword=[your keystore password]
keyPassword=[your key password]
keyAlias=david-release-key
storeFile=./david_release.keystore
```

**⚠️ Important:** Add `keystore.properties` to `.gitignore`

---

## 🏗️ Building the Project

### Using Android Studio

#### Build Debug APK

1. `Build > Make Project` (Ctrl+F9 / Cmd+F9)
2. Wait for build completion
3. APK location: `app/build/outputs/apk/debug/app-debug.apk`

#### Build Release APK

1. `Build > Build Bundle(s) / APK(s) > Build APK(s)`
2. Wait for build completion
3. APK location: `app/build/outputs/apk/release/app-release.apk`

#### Build Release AAB (for Play Store)

1. `Build > Build Bundle(s) / APK(s) > Build Bundle(s)`
2. Wait for build completion
3. AAB location: `app/build/outputs/bundle/release/app-release.aab`

---

### Using Command Line (Gradle)

#### Setup

```bash
# Make gradlew executable (macOS/Linux)
chmod +x gradlew

# Verify Gradle
./gradlew --version
```

#### Clean Build

```bash
# Clean all build artifacts
./gradlew clean

# Or clean and build
./gradlew clean build
```

#### Build Debug

```bash
# Build debug APK
./gradlew assembleDebug

# Output: app/build/outputs/apk/debug/app-debug.apk

# Install on connected device
./gradlew installDebug
```

#### Build Release

```bash
# Set environment variables (for signing)
export KEYSTORE_PASSWORD="your_password"
export KEY_PASSWORD="your_password"

# Build release APK
./gradlew assembleRelease

# Output: app/build/outputs/apk/release/app-release.apk

# Build release AAB
./gradlew bundleRelease

# Output: app/build/outputs/bundle/release/app-release.aab
```

#### Run Tests

```bash
# Unit tests
./gradlew test

# Instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Test coverage
./gradlew jacocoTestReport
```

#### Code Analysis

```bash
# Lint checks
./gradlew lint

# Lint report: app/build/reports/lint-results.html

# Code style checks (if configured)
./gradlew ktlintCheck
```

---

## 📱 Testing & Running

### Using Android Emulator

#### Create Emulator

1. `Tools > Device Manager`
2. Click "Create Device"
3. Select device (e.g., Pixel 6 Pro)
4. Select system image (Android 14, API 34)
5. Configure AVD:
   - RAM: 4GB (for AI models)
   - Internal Storage: 8GB
   - Graphics: Hardware - GLES 2.0
6. Click "Finish"

#### Start Emulator

**From Android Studio:**
- Click device dropdown (toolbar)
- Select your emulator
- Click "Run" (Shift+F10 / Ctrl+R)

**From Command Line:**
```bash
# List available emulators
emulator -list-avds

# Start emulator
emulator -avd Pixel_6_Pro_API_34 -memory 4096 -gpu on &

# Install APK
adb install app/build/outputs/apk/debug/app-debug.apk

# Launch app
adb shell am start -n com.nexuzy.david.debug/.MainActivity
```

---

### Using Physical Device

#### Enable USB Debugging

1. **Enable Developer Options:**
   - Go to `Settings > About Phone`
   - Tap `Build Number` 7 times
   - Enter PIN/password if prompted

2. **Enable USB Debugging:**
   - Go to `Settings > Developer Options`
   - Enable `USB Debugging`
   - Enable `Install via USB`

3. **Connect Device:**
   - Connect via USB cable
   - Accept "Allow USB debugging" prompt
   - Check "Always allow from this computer"

#### Run on Device

**From Android Studio:**
- Device appears in device dropdown
- Select device
- Click "Run"

**From Command Line:**
```bash
# Verify device connected
adb devices

# Install and run
./gradlew installDebug
adb shell am start -n com.nexuzy.david.debug/.MainActivity
```

---

## 🐛 Debugging

### Logcat

**Android Studio:**
- `View > Tool Windows > Logcat`
- Filter by package: `com.nexuzy.david`

**Command Line:**
```bash
# View all logs
adb logcat

# Filter by tag
adb logcat -s DAVID_AI

# Clear and follow
adb logcat -c && adb logcat
```

### Breakpoints

1. Open Kotlin/Java file
2. Click left gutter to add breakpoint
3. Run in debug mode (Shift+F9 / Cmd+D)
4. App pauses at breakpoint
5. Use debug controls to step through

---

## 📦 Dependencies

### Core Dependencies

```kotlin
// Android
implementation("androidx.core:core-ktx:1.12.0")
implementation("androidx.appcompat:appcompat:1.6.1")
implementation("com.google.android.material:material:1.11.0")

// Jetpack Compose
implementation("androidx.compose.ui:ui:1.6.0")
implementation("androidx.compose.material3:material3:1.2.0")

// AI Models
implementation("com.microsoft.onnxruntime:onnxruntime-android:1.17.0")
implementation("com.google.mediapipe:tasks-vision:0.10.9")
implementation("org.tensorflow:tensorflow-lite:2.14.0")

// Networking
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("com.squareup.retrofit2:retrofit:2.9.0")

// Database
implementation("androidx.room:room-runtime:2.6.1")
```

**See `app/build.gradle.kts` for complete list**

---

## 🔍 Troubleshooting

### Common Build Issues

#### Issue: Gradle Sync Failed

```bash
# Solution 1: Clean and rebuild
./gradlew clean
./gradlew build --refresh-dependencies

# Solution 2: Delete .gradle folder
rm -rf .gradle
./gradlew build

# Solution 3: Invalidate caches
# Android Studio: File > Invalidate Caches / Restart
```

#### Issue: Out of Memory

**Edit `gradle.properties`:**
```properties
org.gradle.jvmargs=-Xmx8192m -XX:MaxMetaspaceSize=512m
org.gradle.parallel=true
org.gradle.caching=true
```

#### Issue: SDK Not Found

**Edit `local.properties`:**
```properties
sdk.dir=/path/to/Android/Sdk

# macOS: /Users/USERNAME/Library/Android/sdk
# Linux: /home/USERNAME/Android/Sdk
# Windows: C:\Users\USERNAME\AppData\Local\Android\Sdk
```

#### Issue: NDK Not Found

1. Open SDK Manager
2. Install NDK (Side by side)
3. Sync project

---

## 📚 Related Documentation

- [Android Studio Setup Guide](../docs/ANDROID_STUDIO_SETUP.md)
- [Contributing Guidelines](Contributing)
- [Architecture](Architecture)
- [API Reference](API-Reference)

---

## 🆘 Need Help?

- 📧 Email: [david@nexuzy.in](mailto:david@nexuzy.in)
- 🐛 Report Issues: [GitHub Issues](https://github.com/david0154/david-ai/issues)
- 💬 Discussions: [GitHub Discussions](https://github.com/david0154/david-ai/discussions)

---

**© 2026 Nexuzy Tech Ltd.**  
*Open Source • MIT License • Community Driven*
