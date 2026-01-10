# Building D.A.V.I.D AI from Source

**Complete guide to building D.A.V.I.D AI from source code**

---

## 📋 Table of Contents

1. [Prerequisites](#prerequisites)
2. [Environment Setup](#environment-setup)
3. [Clone Repository](#clone-repository)
4. [Android Studio Setup](#android-studio-setup)
5. [Build Configuration](#build-configuration)
6. [Building the App](#building-the-app)
7. [Running on Device](#running-on-device)
8. [Troubleshooting](#troubleshooting)

---

## Prerequisites

### System Requirements

**Hardware:**
- 8GB RAM minimum (16GB recommended)
- 10GB free disk space
- Intel i5 or equivalent processor

**Operating System:**
- Windows 10/11 (64-bit)
- macOS 10.15 (Catalina) or later
- Linux (Ubuntu 20.04+ or equivalent)

### Required Software

#### 1. Java Development Kit (JDK)

**Install JDK 17:**

```bash
# macOS (using Homebrew)
brew install openjdk@17

# Ubuntu/Debian
sudo apt-get update
sudo apt-get install openjdk-17-jdk

# Verify installation
java -version
# Should show: openjdk version "17.x.x"
```

**Windows:**
- Download from [Adoptium](https://adoptium.net/)
- Install and add to PATH

#### 2. Git

```bash
# macOS
brew install git

# Ubuntu/Debian
sudo apt-get install git

# Windows
# Download from https://git-scm.com/download/win

# Verify
git --version
```

#### 3. Android Studio

**Download and Install:**
- Visit [Android Studio Download](https://developer.android.com/studio)
- Download latest stable version (Hedgehog 2023.1.1+)
- Install with default settings

**Required Components:**
- Android SDK Platform 34 (Android 14)
- Android SDK Platform 26 (Android 8.0)
- Android SDK Build-Tools 34.0.0
- Android SDK Platform-Tools
- Android Emulator
- NDK (Side by side)
- CMake

---

## Environment Setup

### Configure Android SDK

1. Open Android Studio
2. Go to `Settings/Preferences → Languages & Frameworks → Android SDK`
3. **SDK Platforms** tab:
   - ✅ Android 14.0 (API 34)
   - ✅ Android 8.0 (API 26)
4. **SDK Tools** tab:
   - ✅ Android SDK Build-Tools 34.0.0
   - ✅ NDK (Side by side)
   - ✅ CMake
   - ✅ Android Emulator
5. Click **Apply** and wait for downloads

### Set Environment Variables

**Linux/macOS:**

Add to `~/.bashrc` or `~/.zshrc`:

```bash
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/platform-tools
export PATH=$PATH:$ANDROID_HOME/tools
export PATH=$PATH:$ANDROID_HOME/tools/bin

# Apply changes
source ~/.bashrc  # or ~/.zshrc
```

**Windows:**

1. Open System Properties → Environment Variables
2. Add new User Variable:
   - Variable: `ANDROID_HOME`
   - Value: `C:\Users\YourUsername\AppData\Local\Android\Sdk`
3. Edit PATH and add:
   - `%ANDROID_HOME%\platform-tools`
   - `%ANDROID_HOME%\tools`

---

## Clone Repository

### Using HTTPS

```bash
# Clone the repository
git clone https://github.com/david0154/david-ai.git

# Navigate to directory
cd david-ai

# Check current branch
git branch
```

### Using SSH (Recommended for Contributors)

```bash
# Clone via SSH
git clone git@github.com:david0154/david-ai.git
cd david-ai
```

### Verify Clone

```bash
# Check repository status
git status

# View remote
git remote -v

# List files
ls -la
```

---

## Android Studio Setup

### Open Project

1. Launch Android Studio
2. Click **Open** (or File → Open)
3. Navigate to `david-ai` folder
4. Click **OK**

### Gradle Sync

**First-time sync:**
- Android Studio automatically starts Gradle sync
- Progress shown at bottom of window
- Takes 5-10 minutes (downloading dependencies)
- Wait for "BUILD SUCCESSFUL" message

**If sync fails:**

```bash
# Clean Gradle cache
./gradlew clean

# Sync again
./gradlew --refresh-dependencies
```

### Configure JDK

1. Go to `Settings → Build, Execution, Deployment → Build Tools → Gradle`
2. **Gradle JDK**: Select `JDK 17` or `Embedded JDK 17`
3. Click **Apply**

---

## Build Configuration

### Build Variants

**Debug Build:**
- For development and testing
- Includes debug symbols
- Not optimized
- Larger APK size

**Release Build:**
- For production
- Optimized and minified
- Requires signing key
- Smaller APK size

### Select Build Variant

1. Open **Build Variants** panel (bottom left)
2. Select:
   - `debug` for development
   - `release` for production

---

## Building the App

### Build Debug APK

**Using Android Studio:**
1. Menu: `Build → Build Bundle(s) / APK(s) → Build APK(s)`
2. Wait for build to complete
3. Click **locate** in notification

**Using Command Line:**

```bash
# Clean previous builds
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Output location:
# app/build/outputs/apk/debug/app-debug.apk
```

### Build Release APK

**Generate Signing Key (First Time):**

```bash
# Create keystore directory
mkdir -p app/keystore

# Generate keystore
keytool -genkey -v -keystore app/keystore/david_keystore.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias david-release-key

# Follow prompts:
# - Enter keystore password
# - Re-enter password
# - Enter your details
# - Enter key password
```

**Configure Signing:**

Create `keystore.properties` in project root:

```properties
storePassword=YOUR_KEYSTORE_PASSWORD
keyPassword=YOUR_KEY_PASSWORD
keyAlias=david-release-key
storeFile=keystore/david_keystore.jks
```

⚠️ **Add to `.gitignore`:**
```bash
echo "keystore.properties" >> .gitignore
echo "app/keystore/*.jks" >> .gitignore
```

**Build Release:**

```bash
# Set environment variables
export KEYSTORE_PASSWORD="your_password"
export KEY_PASSWORD="your_password"

# Build release APK
./gradlew assembleRelease

# Output:
# app/build/outputs/apk/release/app-release.apk
```

### Build AAB (Android App Bundle)

**For Google Play Store:**

```bash
# Build release bundle
./gradlew bundleRelease

# Output:
# app/build/outputs/bundle/release/app-release.aab
```

---

## Running on Device

### Using Android Emulator

**Create Emulator:**

1. Open **Device Manager** (`Tools → Device Manager`)
2. Click **Create Device**
3. Select:
   - **Phone**: Pixel 6 Pro
   - **System Image**: Android 14 (API 34) with Google Play
   - **AVD Name**: D.A.V.I.D_AI_Emulator
4. **Advanced Settings**:
   - RAM: 4096 MB
   - Internal Storage: 8192 MB
   - Graphics: Hardware - GLES 2.0
5. Click **Finish**

**Run on Emulator:**

```bash
# List available emulators
emulator -list-avds

# Start emulator
emulator -avd D.A.V.I.D_AI_Emulator -memory 4096 -gpu on &

# Install and run
./gradlew installDebug
adb shell am start -n com.nexuzy.david/.MainActivity
```

### Using Physical Device

**Enable Developer Mode:**

1. Go to **Settings → About Phone**
2. Tap **Build Number** 7 times
3. Developer mode enabled!

**Enable USB Debugging:**

1. Go to **Settings → Developer Options**
2. Enable **USB Debugging**
3. Enable **Install via USB**

**Connect and Run:**

```bash
# Connect device via USB

# Check device connection
adb devices
# Should show: <device-id>  device

# Install debug APK
adb install app/build/outputs/apk/debug/app-debug.apk

# Or use Gradle
./gradlew installDebug

# Launch app
adb shell am start -n com.nexuzy.david/.MainActivity
```

---

## Build Commands Reference

### Common Gradle Commands

```bash
# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Build debug AAB
./gradlew bundleDebug

# Build release AAB
./gradlew bundleRelease

# Install debug on device
./gradlew installDebug

# Uninstall from device
./gradlew uninstallDebug

# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Generate lint report
./gradlew lint

# Check dependencies
./gradlew dependencies

# List all tasks
./gradlew tasks
```

### Build with Options

```bash
# Build with stacktrace
./gradlew assembleDebug --stacktrace

# Build with info logging
./gradlew assembleDebug --info

# Build with debug logging
./gradlew assembleDebug --debug

# Build offline (no internet)
./gradlew assembleDebug --offline

# Refresh dependencies
./gradlew assembleDebug --refresh-dependencies

# Build with parallel execution
./gradlew assembleDebug --parallel
```

---

## Troubleshooting

### Gradle Sync Failed

**Problem:** Gradle sync fails with dependency errors

**Solution:**
```bash
# Clear Gradle cache
rm -rf ~/.gradle/caches/

# Delete build folders
rm -rf build app/build

# Re-sync
./gradlew clean
./gradlew --refresh-dependencies
```

### Build Failed - Memory Issues

**Problem:** `OutOfMemoryError` during build

**Solution:**

Edit `gradle.properties`:

```properties
org.gradle.jvmargs=-Xmx8192m -XX:MaxMetaspaceSize=512m
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configureondemand=true
```

### SDK Not Found

**Problem:** `SDK location not found`

**Solution:**

Create `local.properties`:

```properties
# macOS/Linux
sdk.dir=/Users/YOUR_USERNAME/Library/Android/sdk

# Windows
sdk.dir=C:\\Users\\YOUR_USERNAME\\AppData\\Local\\Android\\Sdk
```

### Device Not Detected

**Problem:** `adb devices` shows no devices

**Solution:**
```bash
# Restart adb server
adb kill-server
adb start-server

# Check device again
adb devices

# macOS: Check System Preferences → Security
# Windows: Install device drivers
```

### Build Too Slow

**Solution:**

1. **Enable Gradle Daemon:**
   ```properties
   # gradle.properties
   org.gradle.daemon=true
   org.gradle.parallel=true
   org.gradle.caching=true
   ```

2. **Increase Heap Size:**
   ```properties
   org.gradle.jvmargs=-Xmx8192m
   ```

3. **Use Build Cache:**
   ```bash
   ./gradlew assembleDebug --build-cache
   ```

---

## Build Optimization

### Speed Up Builds

**gradle.properties:**

```properties
# Use parallel execution
org.gradle.parallel=true

# Enable caching
org.gradle.caching=true

# Configure on demand
org.gradle.configureondemand=true

# Increase heap
org.gradle.jvmargs=-Xmx8192m -XX:MaxMetaspaceSize=512m

# Enable Kotlin incremental compilation
kotlin.incremental=true

# Use AndroidX
android.useAndroidX=true
android.enableJetifier=true
```

### Reduce APK Size

**app/build.gradle.kts:**

```kotlin
android {
    buildTypes {
        release {
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

---

## Next Steps

1. ✅ Build successful
2. 📱 Install on device
3. 🧪 Test all features
4. 🐛 Report any issues
5. 🤝 Contribute improvements

---

## Additional Resources

- 📖 [Android Studio Guide](https://github.com/david0154/david-ai/blob/main/docs/ANDROID_STUDIO_SETUP.md)
- 🤝 [Contributing Guide](Contributing)
- 🏗️ [Architecture](Architecture)
- 📚 [API Reference](API-Reference)
- ❓ [FAQ](FAQ)

---

**Need Help?**

- 📧 Email: [david@nexuzy.in](mailto:david@nexuzy.in)
- 🐛 [Report Issue](https://github.com/david0154/david-ai/issues/new?template=bug_report.md)
- 💬 [Discussions](https://github.com/david0154/david-ai/discussions)

---

**© 2026 Nexuzy Tech Ltd.**
