# Build Instructions for DAVID AI

## Prerequisites

### Required Software
- **Android Studio**: Hedgehog (2023.1.1) or newer
- **JDK**: Version 11 or 17 (NOT Java 21)
- **Minimum SDK**: Android 9.0 (API 28)
- **Target SDK**: Android 14 (API 34)

## Fixing Java Version Issues

### Step 1: Check Your Java Version
In Android Studio:
1. Go to **File** → **Project Structure** → **SDK Location**
2. Check **Gradle Settings** → **Gradle JDK**
3. It should be set to **Java 11** or **Java 17**

### Step 2: If Using Wrong Java Version

#### Option A: Download Correct JDK via Android Studio
1. **File** → **Project Structure** → **SDK Location**
2. Under **Gradle JDK**, click the dropdown
3. Select **Download JDK...**
4. Choose **Version 17** with **Vendor: Eclipse Temurin (AdoptOpenJDK HotSpot)**
5. Click **Download**

#### Option B: Use Embedded JDK
1. **File** → **Settings** (or **Preferences** on Mac)
2. Navigate to **Build, Execution, Deployment** → **Build Tools** → **Gradle**
3. Set **Gradle JDK** to **Embedded JDK (version 17)**

## Building the Project

### Clean Build
```bash
# Stop Gradle daemon
./gradlew --stop

# Clean project
./gradlew clean

# Build debug APK
./gradlew assembleDebug
```

### In Android Studio
1. **File** → **Invalidate Caches** → Select **Invalidate and Restart**
2. After restart: **File** → **Sync Project with Gradle Files**
3. Click **Build** → **Rebuild Project**

### Install on Device
```bash
# Install debug version
./gradlew installDebug

# Or build and install release
./gradlew assembleRelease
adb install app/build/outputs/apk/release/app-release.apk
```

## Troubleshooting

### Error: "TypeNotPresentException: Type T not present"
**Solution**: You're using Java 21+, downgrade to Java 17
- Follow **Step 2** above to change JDK version

### Error: "Could not create task"
**Solution**: Clean build and restart Gradle daemon
```bash
./gradlew --stop
rm -rf .gradle build
./gradlew clean build
```

### Error: "Module() method not found"
**Solution**: Delete local Gradle cache
```bash
rm -rf ~/.gradle/caches
./gradlew clean build --refresh-dependencies
```

### Android Studio Won't Sync
1. **File** → **Invalidate Caches** → **Invalidate and Restart**
2. Delete `.idea` folder and `.iml` files
3. Re-import project: **File** → **Open** → Select project folder

## Project Structure

```
david-ai/
├── app/                    # Main application module
│   ├── src/
│   │   └── main/
│   │       ├── java/       # Kotlin/Java source files
│   │       ├── res/        # Resources (layouts, icons, etc)
│   │       └── AndroidManifest.xml
│   └── build.gradle.kts    # App-level build configuration
├── build.gradle.kts        # Project-level build configuration
├── settings.gradle.kts     # Gradle settings
└── gradle.properties       # Gradle properties
```

## Key Features

- 🎤 **Voice Recognition**: Hot word detection and speech-to-text
- 👁️ **Gesture Recognition**: Camera-based gesture controls
- 🤖 **AI Processing**: MediaPipe and TensorFlow Lite integration
- ⌚ **Wear OS Support**: Compatible with Android smartwatches
- 🔐 **Security**: Biometric authentication and encrypted storage
- 🔥 **Firebase Integration**: Authentication and analytics

## Version Information

- **App Version**: 2.0.0 (Build 200)
- **Gradle**: 7.5.1
- **Android Gradle Plugin**: 7.4.2
- **Kotlin**: 1.8.22
- **Compile SDK**: 34
- **Min SDK**: 28
- **Target SDK**: 34

## Need Help?

If you encounter any issues:
1. Check this file first
2. Review error logs in **Build** → **Build Output**
3. Create an issue on GitHub with full error trace

---

**Last Updated**: January 9, 2026
