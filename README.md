# DAVID AI - Your Personal AI Assistant 
│     D A V I D               │
│    Digital Assistant Voice      │
│    Intelligence Device  │ 

**D**ynamic **A**rtificial **V**oice **I**nteractive **D**evice

An advanced Android AI assistant with voice recognition, gesture control, and intelligent automation capabilities.

![Version](https://img.shields.io/badge/version-2.0.0-blue)
![Platform](https://img.shields.io/badge/platform-Android%208.0+-green)
![License](https://img.shields.io/badge/license-MIT-orange)

## 🌟 Features

### Core Capabilities
- 🎤 **Voice Recognition** - Hot word detection and natural speech-to-text
- 👁️ **Gesture Control** - Camera-based hand gesture recognition
- 🤖 **AI Processing** - Advanced ML models with MediaPipe & TensorFlow Lite
- 📱 **Device Control** - WiFi, Bluetooth, calls, SMS automation
- ⌚ **Wear OS Support** - Full smartwatch compatibility
- 🔐 **Security** - Biometric authentication and encrypted storage
- 🔥 **Firebase Integration** - Cloud sync and analytics

### Technical Features
- Material Design 3 UI with dynamic theming
- Jetpack Compose modern UI framework
- Room database for local storage
- Hilt dependency injection
- Coroutines for async operations
- MVVM architecture pattern

## 🛠️ Requirements

### Development Environment
- **Android Studio**: Hedgehog (2023.1.1) or newer
- **JDK**: Version 17 (NOT Java 21)
- **Gradle**: 8.2 (auto-downloaded)
- **Minimum SDK**: Android 9.0 (API 28)
- **Target SDK**: Android 14 (API 34)

### Hardware Requirements
- Microphone (required for voice features)
- Camera (optional for gesture control)
- GPS (optional for location services)
- Bluetooth (optional for connectivity)

## 🚀 Quick Start

### 1. Clone Repository
```bash
git clone https://github.com/david0154/david-ai.git
cd david-ai
```

### 2. Clean Build (IMPORTANT - First Time)

The project needs a clean build to avoid cached resource errors.

**Windows:**
```bash
clean_build.bat
```

**Linux/Mac:**
```bash
chmod +x clean_build.sh
./clean_build.sh
```

### 3. Configure Java in Android Studio

1. Open **File** → **Project Structure** → **SDK Location**
2. Set **Gradle JDK** to **Java 17** (Embedded JDK or download)
3. Click **OK**

### 4. Sync and Build

1. **File** → **Sync Project with Gradle Files**
2. Wait for sync to complete
3. **Build** → **Make Project**

### 5. Run the App

```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Or run from Android Studio
# Click Run button or Shift+F10
```

## 🐛 Troubleshooting

### Error: "TypeNotPresentException" or Java Version Issues

**Problem**: Using Java 21 instead of Java 17

**Solution**:
1. Go to **File** → **Settings** → **Build Tools** → **Gradle**
2. Change **Gradle JDK** to **Java 17** or **Embedded JDK**
3. Restart Android Studio

### Error: "holo_blue_darker not found"

**Problem**: Cached build files with old dependencies

**Solution**:
```bash
# Run clean build script
./clean_build.bat   # Windows
./clean_build.sh    # Linux/Mac

# Then rebuild
./gradlew assembleDebug
```

### Error: "SDK XML version 4 not supported"

**Problem**: Android SDK tools mismatch

**Solution**: Already fixed in latest version. Just pull latest changes:
```bash
git pull origin main
```

### Build Not Working?

**Complete Clean Build**:
```bash
# Stop Gradle
./gradlew --stop

# Delete all build files
rm -rf .gradle build app/build
rm -rf ~/.gradle/caches/transforms-3

# Rebuild
./gradlew clean
./gradlew assembleDebug
```

### Android Studio Won't Sync?

1. **File** → **Invalidate Caches** → **Invalidate and Restart**
2. Delete `.idea` folder and `.iml` files
3. **File** → **Open** → Select project folder again

## 📁 Project Structure

```
david-ai/
├── app/
│   ├── src/main/
│   │   ├── java/com/davidstudioz/david/
│   │   │   ├── MainActivity.kt          # Main app entry
│   │   │   ├── voice/                   # Voice recognition
│   │   │   ├── gesture/                 # Gesture control
│   │   │   ├── chat/                    # AI chat features
│   │   │   └── security/                # Authentication
│   │   ├── res/
│   │   │   ├── layout/                  # XML layouts
│   │   │   ├── values/                  # Colors, strings, themes
│   │   │   └── drawable/                # Icons and images
│   │   └── AndroidManifest.xml      # App configuration
│   └── build.gradle.kts            # App dependencies
├── build.gradle.kts                # Project config
├── settings.gradle.kts             # Gradle settings
├── gradle.properties               # Build properties
├── clean_build.bat                 # Windows clean script
├── clean_build.sh                  # Linux/Mac clean script
└── README.md                       # This file
```

## 📚 Documentation

For detailed setup instructions and troubleshooting, see:
- [BUILD_INSTRUCTIONS.md](BUILD_INSTRUCTIONS.md) - Complete build guide
- [CONTRIBUTING.md](CONTRIBUTING.md) - Contribution guidelines

## 📦 Dependencies

### Core Libraries
- **Kotlin**: 1.9.22
- **Gradle**: 8.2
- **Android Gradle Plugin**: 8.2.2
- **Jetpack Compose**: 2024.01.00

### Key Frameworks
- AndroidX Core & AppCompat
- Material Design 3
- Hilt (Dependency Injection)
- Room (Database)
- Retrofit (Networking)
- TensorFlow Lite (ML)
- MediaPipe (Gesture Recognition)
- Firebase (Auth & Analytics)
- CameraX (Camera API)
- Work Manager (Background Tasks)

### ML & AI
- TensorFlow Lite: 2.14.0
- MediaPipe Tasks Vision: 0.10.14
- TensorFlow Lite GPU Support

## 🔒 Permissions

The app requires various permissions for full functionality:

**Critical**:
- Microphone (voice input)
- Internet (cloud services)

**Optional**:
- Camera (gesture control)
- Location (weather, context)
- Contacts (smart dialing)
- SMS/Phone (messaging automation)
- Bluetooth/WiFi (device control)

## 🎯 Roadmap

- [ ] Advanced gesture recognition
- [ ] Multi-language support
- [ ] Custom wake word training
- [ ] Integration with smart home devices
- [ ] Offline AI processing
- [ ] Voice cloning capabilities
- [ ] Augmented reality features

## 👥 Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📧 Contact

**Developer**: David Studioz  
**GitHub**: [@david0154](https://github.com/david0154)  
**Project Link**: [https://github.com/david0154/david-ai](https://github.com/david0154/david-ai)

## 🚀 Version History

### v2.0.0 (Current)
- Material Design 3 UI
- Gradle 8.2 migration
- Jetpack Compose integration
- Enhanced gesture recognition
- Wear OS support
- Firebase integration
- Modern architecture patterns

### v1.0.0
- Initial release
- Basic voice recognition
- Simple gesture control
- Device automation

---

**Made with ❤️ by David Studioz**
