# 🚀 DAVID AI - Voice-First Android AI Assistant

A production-ready Android application featuring offline AI, voice control, device automation, and cloud synchronization.

## ✨ Key Features

- 🎤 **Voice Control** - 20+ voice commands for device automation
- 🤖 **Offline AI** - llama.cpp powered LLM inference (TinyLLaMA, Phi-2, Qwen)
- 🌍 **Multi-Language** - 14 languages including Indian languages (Hindi, Tamil, Telugu, etc.)
- 🔐 **Security** - End-to-end encryption, biometric auth, voice biometric unlock
- ☁️ **Cloud Sync** - Encrypted cloud synchronization with conflict resolution
- 📱 **Device Control** - Calls, SMS, WiFi, Bluetooth, GPS, brightness, alarms
- 🖼️ **Image Generation** - Replicate API integration for image creation
- 🌐 **Web Search** - DuckDuckGo/Google powered search with HTML parsing
- 💾 **Local Storage** - Room database with AES-256-GCM encryption
- 📊 **Adaptive Models** - Auto-select model based on device RAM (1.5GB - 6GB)

## 🏗️ Architecture

```
DAVID AI
├── Voice Control (Whisper.cpp + Coqui TTS)
├── Offline AI (llama.cpp JNI + CLIP)
├── Device Automation (Accessibility Service)
├── Cloud Sync (WorkManager + Firebase)
├── Local Storage (Room + Encryption)
└── UI (Jetpack Compose + Material Design 3)
```

## 📋 Requirements

- Android 8.0+ (API 26)
- Minimum 1.5 GB RAM
- 2 GB storage for models
- Android Studio Hedgehog or later
- JDK 17

## 🚀 Quick Start

### 1. Clone Repository
```bash
git clone https://github.com/david0154/david-ai.git
cd david-ai
```

### 2. Install Dependencies
```bash
./gradlew build
```

### 3. Download Models
```bash
./scripts/download-models.sh
```

### 4. Build APK
```bash
./gradlew assembleDebug
```

### 5. Install and Run
```bash
./gradlew installDebug
adb logcat | grep DAVID
```

## 📊 Supported Models

| Device RAM | Model | Speed | Context |
|------------|-------|-------|----------|
| 1.5 GB | TinyLLaMA | 3-5 t/s | 512 tokens |
| 2 GB | Phi-2 | 4-6 t/s | 1024 tokens |
| 3 GB | Qwen | 5-8 t/s | 2048 tokens |
| 4 GB | Phi-3 | 6-10 t/s | 4096 tokens |
| 6 GB | Phi-3 | 8-12 t/s | 8192 tokens |

## 🗣️ Voice Commands

```
"David, call mom"
"David, send message to John – I'm running late"
"David, turn on Bluetooth and WiFi"
"David, what's the weather in Kolkata?"
"David, take a photo"
"David, unlock my phone"
"David, set alarm for 7 AM"
"David, play my favorite music"
"David, explain this photo"
"David, create a cartoon cat"
```

## 🔐 Security Features

- ✅ AES-256-GCM encryption for local data
- ✅ Android Keystore for cryptographic keys
- ✅ Biometric + Voice authentication
- ✅ No hardcoded API keys
- ✅ SSL pinning ready
- ✅ Secure OAuth 2.0 token storage
- ✅ Input validation and output encoding
- ✅ Rate limiting on backend

## 📁 Project Structure

```
DAVID-AI/
├── app/src/main/
│   ├── kotlin/com/davidstudioz/david/
│   │   ├── ui/               # Jetpack Compose UI
│   │   ├── voice/            # Voice control engine
│   │   ├── ai/               # AI inference
│   │   ├── device/           # Device automation
│   │   ├── web/              # Web search
│   │   ├── storage/          # Local database
│   │   ├── sync/             # Cloud sync
│   │   ├── auth/             # Authentication
│   │   └── api/              # API clients
│   ├── jni/                  # Native bindings
│   └── res/                  # Resources
├── backend/                  # PHP backend
├── docs/                     # Documentation
├── scripts/                  # Build scripts
└── models/                   # AI models
```

## 🛠️ Build Configuration

- **Min SDK:** 26 (Android 8.0)
- **Target SDK:** 34 (Android 14)
- **Language:** Kotlin 1.9
- **Compose:** 1.5.0
- **Material3:** 1.0.0

## 📦 Dependencies

- Jetpack Compose (UI)
- Material Design 3 (Theme)
- Room (Database)
- Hilt (DI)
- Coroutines (Async)
- WorkManager (Background)
- OkHttp (Networking)
- Retrofit (API)
- Jsoup (HTML parsing)
- TensorFlow Lite (Vision)

## 🚦 Development Phases

- [x] Phase 1: Core Setup
- [x] Phase 2: Offline AI
- [x] Phase 3: Voice
- [x] Phase 4: Device Control
- [x] Phase 5: Image & Web
- [x] Phase 6: Sync & Polish

## 📈 Stats

- **Total Models:** 8
- **Supported Languages:** 14
- **Device Control Commands:** 20+
- **App Size:** 15-20 MB
- **Model Pack:** 1.5-2 GB
- **Min RAM:** 1.5 GB
- **Max RAM:** 6 GB

## 📖 Documentation

See [docs/](docs/) for:
- [VOICE_GUIDE.md](docs/VOICE_GUIDE.md) - Voice features
- [DEVICE_CONTROL.md](docs/DEVICE_CONTROL.md) - Device automation
- [ENCRYPTION.md](docs/ENCRYPTION.md) - Security details
- [BACKEND.md](docs/BACKEND.md) - Backend setup

## 🧪 Testing

```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest

# Code coverage
./gradlew jacocoTestReport
```

## 🚀 Deployment

### Google Play Store
```bash
./gradlew bundleRelease
# Upload .aab file to Play Console
```

### Direct Installation
```bash
./gradlew assembleRelease
adb install app/build/outputs/apk/release/app-release.apk
```

## 📝 License

Apache License 2.0 - See [LICENSE](LICENSE) file

## 👥 Author

**David Powered by Nexuzy Tech**
- GitHub: [@david0154](https://github.com/david0154)
- Email: support@davidai.app
- Location: Kolkata, India

## 🙏 Acknowledgments

- llama.cpp team for model inference
- OpenAI Whisper for speech recognition
- Coqui for TTS
- Google for Android framework
- Material Design team

## 📞 Support

- **Issues:** [GitHub Issues](https://github.com/david0154/david-ai/issues)
- **Discussions:** [GitHub Discussions](https://github.com/david0154/david-ai/discussions)
- **Email:** support@davidai.app

---

**DAVID AI v2.0 - Production Ready** ✨
*Voice-First Android AI Assistant with Offline Intelligence*
