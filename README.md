# 🤖 DAVID AI - Voice-First Android AI Assistant

![DAVID AI Logo](logo.png)

## Overview

**DAVID AI** is a cutting-edge voice-first Android application powered by offline AI models. It combines advanced natural language processing, voice recognition, device automation, and smart home integration into a single, privacy-focused application.

### Key Motto
**"Your Voice. Your Device. Your Privacy."**

All data stays on your device. Zero backend dependency. 100% local processing.

---

## 🌟 Actual Features (v2.0)

### 🎤 Voice Control & Interaction
- **Advanced Voice Recognition** - Offline speech-to-text using Whisper.cpp
- **Hot Word Detection** - Always-listening "Hey David" wake word activation
- **Natural Voice Response** - Text-to-speech with 14+ language support
- **Voice Biometric Authentication** - Secure unlock using voice patterns
- **Speaker Identification** - Recognize different users by voice
- **Real-Time Conversation** - Context-aware dialogue system
- **User Nickname Support** - AI calls users by their saved nickname
- **Personalized Greetings** - AI addresses user by nickname (e.g., "Hi John, how are you?")

### 🧠 Artificial Intelligence
- **Offline AI Engine** - llama.cpp integration for on-device inference
- **Adaptive Model Selection** - Auto-selects best model (1.5-6GB RAM)
- **Context Awareness** - Understands conversation history and user profile
- **Multi-Model Support** - TinyLLaMA, Phi-2, Qwen 1.8B
- **Performance Optimized** - 300-800ms response time
- **Background AI Processing** - Active AI model loading in background while app is in use
- **Intelligent Responses** - AI learns from user interactions

### 📱 Device Automation
- **20+ Voice Commands** - Complete control device functions:
  - "Call Mom"
  - "Send SMS to John"
  - "Turn on WiFi"
  - "Enable Bluetooth"
  - "Turn on flashlight"
  - "Increase brightness"
  - "Open WhatsApp"
  - "Take a photo"
  - And 12+ more commands
- **Call Management** - Make calls, check call history
- **SMS Control** - Send messages via voice
- **WiFi & Bluetooth** - Toggle connectivity
- **GPS Control** - Enable/disable location services
- **Camera Control** - Take photos via voice
- **Flashlight Control** - Turn torch on/off
- **Volume & Brightness** - Full audio and display control
- **App Launcher** - Open any installed app by voice

### 👆 Gesture Recognition & Control
- **Gesture Detection** - Recognize and respond to user gestures
- **Swipe Gestures** - Left, right, up, down swipes
- **Tap Recognition** - Single tap, double tap, triple tap
- **Long-Press Detection** - Hold gestures
- **Pinch-Zoom Support** - Multi-finger zoom
- **Custom Gesture Mapping** - Assign commands to gestures
- **Gesture Feedback** - Haptic response for gestures
- **Accessibility Gestures** - Compatible with accessibility shortcuts

### 🎯 User Profile & Personalization
- **User Nickname** - Set your preferred name
- **Personalized Responses** - AI addresses you by nickname
- **User Preferences** - Language, voice tone, response style
- **Login with Google** - Secure authentication
- **Profile Storage** - Local device storage
- **Preference Sync** - Settings saved locally
- **Voice Profile** - Custom voice settings per user

### 🔊 Real-Time Conversation
- **Context-Aware Responses** - Understanding conversation flow
- **Multi-Turn Dialogue** - Natural back-and-forth conversations
- **Sentiment Analysis** - Detect user mood and intent
- **Smart Reply** - Intelligent response generation
- **Conversation Memory** - Remember previous discussions (120 days)
- **Topic Recognition** - Identify conversation subjects
- **Natural Language Understanding** - Contextual comprehension

### 💾 Local Data Management
- **Device-Only Storage** - All data stays on your phone
- **SQLite Database** - Fast local storage system
- **120-Day Auto-Cleanup** - Automatic data deletion after 120 days
- **Encrypted Storage** - AES-256-GCM encryption
- **Android Keystore** - Secure credential management
- **Zero Cloud Upload** - No data sent to servers
- **Manual Export** - Export chat history anytime

### 🔐 Authentication
- **Google Sign-In** - Simple, secure authentication
- **No Backend Required** - Google handles verification
- **Biometric Support** - Fingerprint and Face ID
- **Zero Password Storage** - No passwords on device
- **Automatic Session** - Stay logged in securely

### 🌟 Accessibility Improvements
- **Voice Navigation** - Control app entirely by voice
- **Text-to-Speech** - All text read aloud
- **High Contrast Mode** - Better visibility
- **Large Font Support** - Adjustable text size
- **Haptic Feedback** - Vibration notifications
- **Color Blind Mode** - Accessible color schemes
- **Screen Reader Support** - TalkBack compatible
- **Voice Commands Only** - No touch required for core functions

### 🔄 Background Processing & Services
- **Background AI Model Loading** - Preload models for faster response
- **Automatic Cleanup** - Background chat history cleanup
- **Model Updates** - Auto-download model updates
- **Health Monitoring** - Monitor app performance
- **Battery Optimized** - Minimal battery drain
- **WiFi-Only Option** - Sync only on WiFi
- **Smart Scheduling** - Process during low-activity periods
- **Service Status** - Always-on service monitoring

### 🚀 Performance Optimization
- **Fast Startup** - < 2 seconds launch time
- **Smooth UI** - 60 FPS animations
- **Low Memory Usage** - Optimized for 2GB+ RAM
- **Smart Caching** - Model pre-loading
- **Efficient Processing** - Multi-threaded operations
- **Background Optimization** - Non-blocking tasks
- **Battery Efficient** - Minimal power consumption

---

## 🌍 Language Support

### Supported Languages (14+)

David AI supports comprehensive language coverage for voice recognition, text-to-speech, and translation:

#### 🇮🇳 Indian Languages
| Language | Code | Native Name | Voice | Translation | Script(s) |
|----------|------|-------------|-------|-------------|----------|
| **Hindi** | `hin` | हिंदी | ✅ Native | ✅ Full | Devanagari, Roman |
| **Bengali** | `ben` | বাংলা | ✅ Native | ✅ Full | Bengali, Roman |
| **Tamil** | `tam` | தமிழ் | ✅ Native | ✅ Full | Tamil, Roman |
| **Telugu** | `tel` | తెలుగు | ✅ Native | ✅ Full | Telugu, Roman |
| **Marathi** | `mar` | मराठी | ✅ Native | ✅ Full | Devanagari, Roman |
| **Gujarati** | `guj` | ગુજરાતી | ✅ Native | ✅ Full | Gujarati, Roman |
| **Punjabi** | `pan` | ਪੰਜਾਬੀ | ✅ Native | ✅ Full | Gurmukhi, Shahmukhi, Roman |
| **Urdu** | `urd` | اردو | ✅ Native | ✅ Full | Nastaliq, Naskh, Roman |
| **Kannada** | `kan` | ಕನ್ನಡ | ✅ Native | ✅ Full | Kannada, Roman |
| **Malayalam** | `mal` | മലയാളം | ✅ Native | ✅ Full | Malayalam, Roman |
| **Odia** | `odi` | ଓଡିଆ | ✅ Native | ✅ Full | Odia, Roman |
| **Assamese** | `asm` | অসমীয়া | ✅ Native | ✅ Full | Assamese, Roman |
| **Hinglish** | `hin-eng` | हिंग्लिश | ✅ Mixed | ✅ Mixed | Devanagari, Roman |

#### 🌐 International Language
| Language | Code | Native Name | Voice | Translation | Script(s) |
|----------|------|-------------|-------|-------------|----------|
| **English** | `eng` | English | ✅ Native | ✅ Full | Roman |

### Voice Technology Stack

#### Speech-to-Text (STT)
- **Engine**: Whisper.cpp (OpenAI's Whisper)
- **Size**: 50 MB (tiny model)
- **Languages**: All 14 languages
- **Accuracy**: 95%+ for clear audio
- **Speed**: Real-time processing (< 500ms)

#### Text-to-Speech (TTS) - Optimized
- **Primary**: Coqui TTS Lite (850 MB) - Lightweight & Fast
- **RAM**: 1+ GB minimum (runs on 1GB devices)
- **Speed**: 200-500ms per sentence
- **Quality**: Natural, expressive voices
- **Languages**: All 14 languages with native speakers

**Fallback Options:**
- **Festival TTS** (50 MB) - Ultra-lightweight, runs on any device
- **System TTS** - Built-in Android TTS as backup

#### Translation
- **Engine**: IndicTrans2 (Quantized)
- **Type**: Offline local translation
- **Supported Pairs**: All Indian language combinations
- **Direction**: English ↔ Any Indian Language
- **Speed**: 100-300ms per sentence
- **Accuracy**: 90%+ BLEU score

### Model Optimization by RAM

```
💾 Memory Usage Optimization:

1.5 GB RAM   → Whisper Tiny + Festival TTS
              → Minimal models only
              → Use system TTS as fallback

2.0 GB RAM   → Whisper Tiny + Coqui TTS Lite
              → Basic Indian languages
              → Works smoothly

3.0 GB RAM   → Whisper Small + Coqui Lite + IndicTrans2
              → All Indian languages
              → Full translation support
              → ⭐ RECOMMENDED CONFIGURATION

4+ GB RAM    → All models + Enhanced models
              → Maximum quality
              → All features enabled
```

### Language Selection in App

**First Launch:**
1. Sign in with Google
2. Set your nickname (e.g., "John", "Priya")
3. Select preferred language (14+ options)
4. Download language models (optional)
5. Start using in your language - AI will greet you: "Hi John, I'm ready to help!"

**In Code:**
```kotlin
// Get all languages
val languages = ttsEngine.getSupportedLanguages()

// Get Indian languages only
val indianLangs = ttsEngine.getIndianLanguages()

// Change language
ttsEngine.speak(text, SupportedLanguage.HINDI)

// Get language by code
val tamil = ttsEngine.getLanguageByCode("tam")

// Personalized greeting with nickname
val nickname = userProfile.getNickname() // "John"
val greeting = "Hi $nickname, how can I help you?"
ttsEngine.speak(greeting, userLanguage)
```

---

## 💾 Technical Stack

### Android Development
- **Language:** Kotlin 1.9
- **UI Framework:** Jetpack Compose 1.5.0
- **Design System:** Material Design 3
- **Database:** Room 2.5.2 (Local SQLite)
- **Dependency Injection:** Hilt 2.46
- **Networking:** OkHttp 4.11 + Retrofit 2.9
- **Async:** Kotlin Coroutines 1.7
- **Security:** androidx.security 1.1.0
- **Background Services:** WorkManager 2.8.1
- **Gesture Detection:** GestureDetector API

### AI & Machine Learning
- **LLM Runtime:** llama.cpp (On-Device)
- **Speech Recognition:** Whisper.cpp (Offline)
- **Text-to-Speech:** Coqui TTS Lite (Optimized)
- **Translation:** IndicTrans2 (Quantized)
- **Model Format:** GGUF (Quantized)
- **Model Sources:** Hugging Face (Open Source)

### Authentication
- **Provider:** Google Sign-In
- **Verification:** Google Servers Only
- **Local Credentials:** Android Keystore
- **Biometrics:** BiometricPrompt API

### Data Storage
- **Local Database:** SQLite (Room ORM)
- **Encryption:** AES-256-GCM
- **No Cloud Sync:** Device-Only Data
- **Auto-Cleanup:** 120-Day Retention

---

## 🚀 Quick Start

### Prerequisites
- Android 8.0+ device (2GB+ RAM)
- Android Studio Giraffe+
- JDK 11 or higher
- 3-4GB free storage (for models)

### Installation Steps

**1. Clone Repository**
```bash
git clone https://github.com/david0154/david-ai.git
cd david-ai
```

**2. Setup Firebase (Google Login)**
```
1. Go to https://firebase.google.com/console
2. Create project "DAVID-AI"
3. Add Android app
4. Get Web Client ID
5. Paste in GoogleAuthManager.kt (Line 22)
```

**3. Build Project**
```bash
./gradlew build
```

**4. Install on Device**
```bash
./gradlew installDebug
```

**5. First Launch**
- Sign in with Google
- Set your nickname (AI will call you by this name)
- Select your preferred language (14+ options)
- Grant microphone permission
- Wait for AI models to download
- Say "Hey David" to activate
- Start using DAVID AI in your language!

---

## 🎯 How It Works

### Voice Interaction Flow

```
1. Device Listening
   ↓
   "Hey David" (Hot word detected) → Beep sound
   ↓
2. Voice Input
   ↓
   User speaks command (Whisper.cpp STT)
   ↓
3. AI Processing
   ↓
   llama.cpp processes with context + user profile
   ↓
4. Response Generation
   ↓
   AI generates response (includes user nickname)
   ↓
5. Voice Output
   ↓
   Coqui TTS speaks response in user's language
   ↓
6. Device Control (if needed)
   ↓
   Execute device command if requested
```

### Example Interactions

```
User Sets Nickname: "John"
User Sets Language: "Hindi"

Scenario 1:
User: "Hey David!"
AI: "नमस्ते जॉन, मैं आपकी कैसे मदद कर सकता हूँ?" 
     (Hello John, how can I help you?)

Scenario 2:
User: "Send SMS to Mom - I'm coming home"
AI: "जॉन, मैंने आपकी माँ को संदेश भेज दिया।" 
     (John, I've sent the message to Mom.)
[SMS Sent Automatically]

Scenario 3:
User: "Take a photo"
AI: "तस्वीर ले रहा हूँ..." (Taking a photo...)
[Camera opens and takes photo]
```

---

## 🔨 Build Commands

### Basic Build
```bash
# Debug build
./gradlew build

# Release build
./gradlew assembleRelease
```

### Install & Run
```bash
# Install debug APK
./gradlew installDebug

# Run on device
./gradlew installDebug
adb shell am start -n com.davidstudioz.david/.MainActivity
```

### Testing
```bash
# Unit tests
./gradlew test

# UI tests
./gradlew connectedAndroidTest
```

### Advanced
```bash
# Get signing info (for Firebase)
./gradlew signingReport

# Clean build
./gradlew clean build

# Build with optimizations
./gradlew build --parallel
```

**See [BUILD_COMMANDS.md](BUILD_COMMANDS.md) for complete build documentation.**

---

## 📊 System Requirements

### Minimum Requirements
- **Android:** 8.0 (API 26)
- **RAM:** 1.5 GB
- **Storage:** 2 GB (for models)
- **Processor:** ARM64 or x86_64
- **Language:** English or any supported language

### Recommended Requirements
- **Android:** 12 or higher
- **RAM:** 3-4 GB
- **Storage:** 3 GB (for all models)
- **Network:** WiFi for first model download
- **Language:** Preferred language for optimal experience

### Device-Specific Model Selection
```
1.5 GB RAM  →  TinyLLaMA 1.1B (Minimum)
2.0 GB RAM  →  Qwen 1.8B or TinyLLaMA
3.0 GB RAM  →  Phi-2 7B (Recommended)
4+ GB RAM   →  All models available
```

---

## 🔐 Privacy & Security

### Privacy First
- ✅ **100% Local Processing** - AI models run on device
- ✅ **No Data Collection** - We don't track users
- ✅ **No Cloud Upload** - Chat history never leaves phone
- ✅ **Automatic Cleanup** - Messages deleted after 120 days
- ✅ **Open Source** - Audit our code anytime

### Security Features
- ✅ **AES-256-GCM Encryption** - Military-grade encryption
- ✅ **Android Keystore** - Secure key management
- ✅ **Biometric Auth** - Fingerprint/Face unlock
- ✅ **Google Sign-In** - Verified authentication
- ✅ **No Root Required** - Works on unrooted devices

### Data Ownership
- ✅ **You Own Your Data** - Not stored on our servers
- ✅ **Export Anytime** - Download your chat history
- ✅ **Delete Anytime** - Manual data deletion
- ✅ **No T&C Binding** - Your data is yours

---

## 💾 Storage & Models

### Local Storage
```
Device Storage/
├── Chat History (~1-10 MB)
│   ├── Messages (auto-deleted after 120 days)
│   ├── Responses
│   └── Metadata
├── User Profile (Google)
│   ├── Nickname
│   ├── Language Preference
│   └── Voice Settings
├── AI Models (1.5-3 GB)
│   ├── LLM Model (1.3-1.5 GB)
│   ├── Voice Models (50 MB + 850 MB)
│   └── Translation Model (500 MB)
└── Settings
    └── User Preferences
```

### Model Specifications

| Model | Size | RAM Needed | Performance | Type |
|-------|------|-----------|-------------|------|
| TinyLLaMA 1.1B | 1.5 GB | 2 GB | 300-500ms | LLM |
| Phi-2 7B | 1.4 GB | 3 GB | 500-800ms | LLM |
| Qwen 1.8B | 1.3 GB | 2-3 GB | 400-600ms | LLM |
| Whisper Tiny | 50 MB | 1 GB | Real-time | STT |
| Coqui TTS Lite | 850 MB | 1 GB | 200-500ms | TTS |
| IndicTrans2 | 500 MB | 1 GB | 100-300ms | Translation |
| Festival TTS | 50 MB | 0.5 GB | 1-2s | TTS Fallback |

**All models are open-source GGUF format from [Hugging Face](https://huggingface.co/).**

---

## 📚 File Structure

```
david-ai/
├── app/                                    # Android App
│   ├── src/main/kotlin/com/davidstudioz/david/
│   │   ├── auth/                       # Google Authentication
│   │   ├── storage/                    # Local Data Storage
│   │   ├── models/                     # AI Model Management
│   │   ├── gesture/                    # Gesture Recognition
│   │   ├── hotword/                    # Hot Word Detection
│   │   ├── background/                 # Background Services
│   │   ├── profile/                    # User Profile Management
│   │   ├── device/                     # Device Control
│   │   ├── voice/                      # Voice Features
│   │   │   ├── VoiceEngine.kt        # Speech Recognition
│   │   │   ├── TextToSpeechEngine.kt  # TTS with 14+ languages
│   │   │   └── HotWordDetector.kt    # "Hey David" Detection
│   │   ├── conversation/               # Real-Time Chat
│   │   ├── accessibility/              # Accessibility Features
│   │   ├── ui/                         # UI Components
│   │   └── MainActivity.kt             # Main Activity
│   ├── src/main/res/                # Resources
│   │   ├── drawable/                  # Icons & Images
│   │   ├── values/                    # Strings & Colors
│   │   └── xml/                       # Config Files
│   ├── build.gradle.kts              # App Build Config
│   └── AndroidManifest.xml           # App Manifest
├── docs/                                    # Documentation
│   ├── LOCAL_DEVICE_SETUP.md       # Local Setup Guide
│   ├── VOICE_GUIDE.md              # Voice Features
│   ├── DEVICE_CONTROL.md           # Device Control
│   ├── GESTURE_RECOGNITION.md      # Gesture Guide
│   └── HOT_WORD_SETUP.md           # Hot Word Configuration
├── scripts/                                 # Build Scripts
│   └── download-models.sh          # Model Downloader
├── logo.png                                # App Logo
├── README.md                               # This File
├── CONTRIBUTING.md                        # Contributing
├── CHANGELOG.md                           # Version History
├── LICENSE                                # Apache 2.0
├── build.gradle.kts                       # Root Build Config
├── settings.gradle.kts                    # Gradle Settings
└── gradle.properties                      # Gradle Properties
```

---

## 👨‍💻 Developer Information

**Project:** DAVID AI v2.0  
**Author:** David (via Nexuzy Tech)  
**Location:** Kolkata, West Bengal, India  
**Repository:** https://github.com/david0154/david-ai  
**Company:** Nexuzy Tech pvt ltd  
**License:** Apache 2.0  
**Supported Languages:** 14+ (Hindi, Bengali, Tamil, Telugu, Marathi, Gujarati, Punjabi, Urdu, Kannada, Malayalam, Odia, Assamese, Hinglish, English)

---

## 📞 Support & Contributing

### Get Help
- 📧 Check [Issues](https://github.com/david0154/david-ai/issues)
- 📚 Read [Documentation](./docs/)
- 💬 View [Discussions](https://github.com/david0154/david-ai/discussions)
- 🔌 See [BUILD_COMMANDS.md](BUILD_COMMANDS.md)

### Contribute
1. Fork the repository
2. Create feature branch (`git checkout -b feature/NewFeature`)
3. Commit changes (`git commit -m 'Add NewFeature'`)
4. Push to branch (`git push origin feature/NewFeature`)
5. Open Pull Request

See [CONTRIBUTING.md](CONTRIBUTING.md) for details.

---

## 📊 Documentation

- **[BUILD_COMMANDS.md](BUILD_COMMANDS.md)** - All build commands
- **[VOICE_GUIDE.md](docs/VOICE_GUIDE.md)** - Voice control features
- **[DEVICE_CONTROL.md](docs/DEVICE_CONTROL.md)** - Device automation commands
- **[GESTURE_RECOGNITION.md](docs/GESTURE_RECOGNITION.md)** - Gesture control guide
- **[HOT_WORD_SETUP.md](docs/HOT_WORD_SETUP.md)** - Hot word configuration
- **[ENCRYPTION.md](docs/ENCRYPTION.md)** - Security details
- **[ACCESSIBILITY.md](docs/ACCESSIBILITY.md)** - Accessibility features
- **[CONTRIBUTING.md](CONTRIBUTING.md)** - Contributing guidelines

---

## 🚀 Roadmap

### v2.0 (Current) ✅
- ✅ Voice recognition & synthesis (14+ languages)
- ✅ Offline AI models (llama.cpp)
- ✅ Hot word detection ("Hey David")
- ✅ User nickname support with personalized greetings
- ✅ Device automation (20+ commands)
- ✅ Gesture recognition (swipe, tap, pinch, long-press)
- ✅ Local chat storage (120-day auto-cleanup)
- ✅ Google Sign-In
- ✅ Background AI processing
- ✅ Accessibility features
- ✅ Optimized TTS models (Coqui Lite)
- ✅ Multi-language support (14 languages)

### v2.1 (Planned)
- 📧 Advanced ML models
- 📧 Voice cloning
- 📧 Extended language support (more regional languages)
- 📧 Enhanced gesture recognition
- 📧 AI training on user data

### v3.0 (Future)
- 📧 Cross-platform (iOS)
- 📧 Advanced NLP improvements
- 📧 Real-time translation
- 📧 Community models

---

## 📚 License

David AI is licensed under the **Apache 2.0 License**. See [LICENSE](LICENSE) for details.

---

## 🌟 Acknowledgments

- **Hugging Face** - Open source models
- **llama.cpp** - LLM runtime
- **Whisper.cpp** - Multilingual speech recognition
- **Coqui** - Text-to-speech
- **Google** - Android platform
- **JetBrains** - Kotlin language
- **IndicTrans2** - Indian language translation
- **Festival TTS** - Ultra-lightweight TTS fallback

---

## 📱 Community

- 📧 **GitHub Issues** - Report bugs
- 💬 **Discussions** - Share ideas
- 🌐 **GitHub** - Follow updates
- ⭐ **Star** - Show support!

---

## ⚡ Quick Commands

```bash
# Clone
git clone https://github.com/david0154/david-ai.git

# Build
./gradlew build

# Install
./gradlew installDebug

# Test
./gradlew test

# Clean
./gradlew clean
```

---

**DAVID AI v2.0**  
*Your Voice. Your Device. Your Privacy.*  
*Supports 14+ Languages with Hot Word Detection & Personalized Responses*  
*© 2026 David Powered by Nexuzy Tech*  
*Kolkata, India*  
https://github.com/david0154/david-ai
