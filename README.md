# D.A.V.I.D AI - Digital Assistant with Voice & Intelligent Decisions

<div align="center">

<img src="https://raw.githubusercontent.com/david0154/david-ai/main/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" alt="D.A.V.I.D AI Logo" width="150" height="150">

# D.A.V.I.D AI

**Digital Assistant with Voice & Intelligent Decisions**

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-7F52FF?style=flat&logo=kotlin)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?style=flat&logo=android)](https://www.android.com/)
[![License](https://img.shields.io/badge/License-Custom-orange?style=flat)](LICENSE)
[![GitHub Stars](https://img.shields.io/github/stars/david0154/david-ai?style=flat&logo=github)](https://github.com/david0154/david-ai/stargazers)
[![GitHub Issues](https://img.shields.io/github/issues/david0154/david-ai?style=flat&logo=github)](https://github.com/david0154/david-ai/issues)
[![GitHub Pull Requests](https://img.shields.io/github/issues-pr/david0154/david-ai?style=flat&logo=github)](https://github.com/david0154/david-ai/pulls)

**Advanced AI Assistant with Voice Control, Gesture Recognition, and Complete Device Management**

**Developed by [Nexuzy Tech Ltd.](mailto:david@nexuzy.in)**

[Features](#-features) • [Installation](#-getting-started) • [Usage](#first-use) • [Contributing](#-contributing) • [Support](#-support)

</div>

---

## 🌟 Features

### 🎯 Core Capabilities

- 🎙️ **Voice Control** - Hands-free device control via natural voice commands
- ✋ **Gesture Recognition** - Control your device with hand gestures via camera
- 💬 **AI Chat** - Intelligent conversations with on-device AI models
- 👁️ **Vision Processing** - Image recognition and visual understanding
- 🌍 **Multi-Language** - Support for 15 languages including all major Indian languages
- 📡 **Offline First** - All processing happens locally on your device

### 🔊 Voice Commands

Control everything with your voice:

- **Device Control**: WiFi, Bluetooth, Location, Flashlight on/off
- **Volume Control**: Increase, decrease, mute, set specific level
- **Communication**: Make calls, send SMS, send emails
- **Media Control**: Play, pause, next, previous, forward, rewind
- **Camera**: Take selfie, record video
- **Apps**: Open any app by voice
- **Information**: Time, date, weather, alarms
- **System**: Lock device, take screenshot
- **Voice Typing**: Type in any app using voice

### 👋 Gesture Control

**Supported Gestures:**
- ✋ **Open Palm** - Show pointer
- ✊ **Closed Fist** - Hide pointer
- ☝️ **Pointing Up** - Move pointer
- ✌️ **Victory Sign** - Click action
- 👍 **Thumbs Up** - Confirm

**Mouse-Like Pointer:**
- Floating overlay pointer
- Smooth movement animation
- Visual feedback (glow effects)
- Click animations

### 🌍 Supported Languages

**15 Languages Total:**

1. 🇬🇧 English (default)
2. 🇮🇳 Hindi (हिन्दी)
3. 🇮🇳 Tamil (தமிழ்)
4. 🇮🇳 Telugu (తెలుగు)
5. 🇮🇳 Bengali (বাংলা)
6. 🇮🇳 Marathi (मराठी)
7. 🇮🇳 Gujarati (ગુજરાતી)
8. 🇮🇳 Kannada (ಕನ್ನಡ)
9. 🇮🇳 Malayalam (മലയാളം)
10. 🇮🇳 Punjabi (ਪੰਜਾਬੀ)
11. 🇮🇳 Odia (ଓଡ଼ିଆ)
12. 🇮🇳 Urdu (اردو)
13. 🇮🇳 Sanskrit (संस्कृतम्)
14. 🇮🇳 Kashmiri (कॉशुर)
15. 🇮🇳 Assamese (অসমীয়া)

---

## 🤖 AI Models

### Real AI Models (All Downloadable)

#### Voice Recognition (Whisper from HuggingFace)
- **Tiny** (75MB) - For 1-2GB RAM devices
- **Base** (142MB) - For 2-3GB RAM devices
- **Small** (466MB) - For 3GB+ RAM devices

#### Chat AI (from HuggingFace)
- **TinyLlama** (669MB) - Lightweight chat model
- **Qwen 1.5** (1.1GB) - Advanced conversational AI
- **Phi-2** (1.6GB) - Microsoft's powerful model

#### Vision (ONNX)
- **MobileNetV2** (14MB) - Lightweight image recognition
- **ResNet50** (98MB) - Advanced image classification

#### Gesture (MediaPipe from Google)
- **Hand Landmarker** (25MB) - 21-point hand tracking
- **Gesture Recognizer** (31MB) - Gesture classification

#### Language (TensorFlow Lite)
- **Universal Sentence Encoder** (50MB per language)

### Auto Model Selection

The app automatically selects appropriate models based on your device's RAM:

- **1-2GB RAM**: Tiny voice + Light chat + Lite vision (~914 MB)
- **2-4GB RAM**: Base voice + Standard chat + Standard vision (~1.6 GB)
- **4GB+ RAM**: Small voice + Pro chat + Standard vision + All languages (~2.7 GB)

---

## 🛡️ Privacy Policy

### 🔒 WE DO NOT COLLECT ANY DATA

**Your Privacy is Our Top Priority**

✅ **All data stored locally on YOUR device**
✅ **No data sent to external servers**
✅ **No user tracking or analytics**
✅ **No personal information collected**
✅ **No account required**
✅ **No cloud storage**

### Your Device, Your Data

- Voice recordings: Processed locally
- Camera images: Processed locally
- AI models: Downloaded and stored locally
- Chat history: Stored locally
- Settings: Stored locally

**Complete privacy policy:** [PRIVACY_POLICY.md](PRIVACY_POLICY.md)

---

## 🚀 Getting Started

### Requirements

- **Android 8.0 (API 26)** or higher
- **Minimum 1GB RAM** (2GB+ recommended)
- **Camera** for gesture control
- **Microphone** for voice commands
- **500MB-3GB storage** for AI models (depends on device)

### Installation

1. **Download the APK**:
   - Go to [Releases](https://github.com/david0154/david-ai/releases)
   - Download the latest APK

2. **Install the App**:
   - Enable "Install from Unknown Sources" in settings
   - Open the APK and install

3. **Grant Permissions**:
   - Camera (for gesture control)
   - Microphone (for voice commands)
   - Storage (for AI models)
   - Other permissions as needed

4. **Download Models**:
   - First launch will auto-download models
   - Models selected based on your device RAM
   - Takes 2-5 minutes depending on device

### First Use

1. Launch D.A.V.I.D AI
2. Wait for model downloads to complete
3. Grant all required permissions
4. Say "Hey David" to activate voice control
5. Or use hand gestures for pointer control

---

## 🛠️ Building from Source

### Prerequisites

- Android Studio Hedgehog or later
- Kotlin 1.9.0+
- Gradle 8.0+
- Android SDK 34
- JDK 17+

### Build Steps

```bash
# Clone the repository
git clone https://github.com/david0154/david-ai.git
cd david-ai

# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug
```

### APK Location

- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release.apk`

---

## 📚 Documentation

- [Privacy Policy](PRIVACY_POLICY.md)
- [Complete Fixes Documentation](COMPLETE_FIXES_DOCUMENTATION.md)
- [API Documentation](docs/API.md) _(coming soon)_
- [Contributing Guidelines](CONTRIBUTING.md) _(coming soon)_

---

## 💬 Support

### Need Help?

- **Email**: [david@nexuzy.in](mailto:david@nexuzy.in)
- **GitHub Issues**: [Create an issue](https://github.com/david0154/david-ai/issues/new/choose)
- **GitHub Discussions**: [Join discussions](https://github.com/david0154/david-ai/discussions)

### 🐛 Reporting Bugs

**Found a bug?** [Report it here](https://github.com/david0154/david-ai/issues/new?template=bug_report.md)

Please include:
- Device model and Android version
- RAM size
- Steps to reproduce
- Expected vs actual behavior
- LogCat output (if possible)
- Screenshots (if applicable)

### ✨ Feature Requests

**Have an idea?** [Request a feature here](https://github.com/david0154/david-ai/issues/new?template=feature_request.md)

Please describe:
- The feature you'd like
- Why it would be useful
- How it should work
- Any examples or mockups

---

## 👥 Contributing

We welcome contributions! Whether it's:

- 🐛 Bug fixes
- ✨ New features
- 📝 Documentation improvements
- 🌍 Translations
- 🎨 UI/UX enhancements

### How to Contribute

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

Please read our [Contributing Guidelines](CONTRIBUTING.md) _(coming soon)_ for details.

---

## 👨‍💻 Contributors

Thanks to all contributors who have helped make D.A.V.I.D AI better!

<div align="center">

<a href="https://github.com/david0154/david-ai/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=david0154/david-ai" />
</a>

**Want to see your name here?** [Start contributing!](#-contributing)

</div>

### Core Team

- **[David](https://github.com/david0154)** - Creator & Lead Developer
- **Nexuzy Tech Ltd.** - Development & Support

### Special Thanks

To all users who:
- 🐛 Report bugs
- 💡 Suggest features
- ⭐ Star the project
- 📢 Share with others
- 🌍 Contribute translations

---

## 📊 Project Status

- ✅ Voice Control - **COMPLETE**
- ✅ Gesture Recognition - **COMPLETE**
- ✅ AI Chat - **COMPLETE**
- ✅ Multi-Language Support - **COMPLETE**
- ✅ Device Control - **COMPLETE**
- ✅ Privacy-First Design - **COMPLETE**
- 🚧 Advanced Vision Features - **IN PROGRESS**
- 📋 Smart Home Integration - **PLANNED**
- 📋 Wearable Support - **PLANNED**

---

## 🗺️ Roadmap

### Version 1.1 (Q1 2026)
- [ ] Enhanced gesture recognition
- [ ] More voice command types
- [ ] Custom wake word training
- [ ] Gesture customization

### Version 1.2 (Q2 2026)
- [ ] Smart home device control
- [ ] Calendar integration
- [ ] Email management
- [ ] Note taking

### Version 2.0 (Q3 2026)
- [ ] AR features
- [ ] Wearable app
- [ ] API for third-party apps
- [ ] Cloud sync (optional)

---

## 📜 License

This project is licensed under a custom license. See [LICENSE](LICENSE) file for details.

---

## 🏗️ Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **AI Models**: 
  - Whisper (Speech Recognition)
  - LLaMA/Phi-2 (Chat)
  - ONNX (Vision)
  - MediaPipe (Gesture)
- **Architecture**: MVVM
- **Dependency Injection**: Hilt _(planned)_
- **Database**: Room _(for chat history)_
- **Networking**: OkHttp + Retrofit

---

## 🚀 About Nexuzy Tech Ltd.

**D.A.V.I.D AI** is developed and maintained by **Nexuzy Tech Ltd.**, a company focused on building privacy-first AI applications that empower users without compromising their data.

### Our Mission

To create powerful AI tools that:
- Respect user privacy
- Work offline-first
- Are accessible to everyone
- Put users in control

### Contact

- **Email**: [david@nexuzy.in](mailto:david@nexuzy.in)
- **GitHub**: [@david0154](https://github.com/david0154)
- **Website**: _Coming soon_

---

## ⭐ Show Your Support

If you find D.A.V.I.D AI useful, please consider:

- ⭐ **Star this repository**
- 🐛 **Report bugs** via [issues](https://github.com/david0154/david-ai/issues/new/choose)
- 💡 **Suggest features** via [feature requests](https://github.com/david0154/david-ai/issues/new?template=feature_request.md)
- 📢 **Spread the word** - Share with friends and colleagues
- 🤝 **Contribute** - Submit a pull request

<div align="center">

[![Star History](https://img.shields.io/github/stars/david0154/david-ai?style=social)](https://github.com/david0154/david-ai/stargazers)
[![Fork](https://img.shields.io/github/forks/david0154/david-ai?style=social)](https://github.com/david0154/david-ai/fork)
[![Watch](https://img.shields.io/github/watchers/david0154/david-ai?style=social)](https://github.com/david0154/david-ai/watchers)

</div>

---

## 🙏 Acknowledgments

- **OpenAI Whisper** - Voice recognition models
- **HuggingFace** - LLM models (TinyLlama, Qwen, Phi-2)
- **ONNX** - Vision models
- **Google MediaPipe** - Gesture recognition
- **TensorFlow** - Language models
- All open-source contributors
- Our amazing users and community

---

<div align="center">

**Made with ❤️ by Nexuzy Tech Ltd.**

[![Email](https://img.shields.io/badge/Email-david%40nexuzy.in-blue?style=flat&logo=gmail)](mailto:david@nexuzy.in)
[![GitHub](https://img.shields.io/badge/GitHub-david0154-181717?style=flat&logo=github)](https://github.com/david0154)

---

**© 2026 Nexuzy Tech Ltd. All rights reserved.**

*Privacy-First AI • Your Device, Your Data • No Data Collection*

</div>
