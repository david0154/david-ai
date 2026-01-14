# D.A.V.I.D - AI Assistant 🤖

**Developed by Nexuzy Tech**  
**Lead Developer: David**

---

## 🎯 Overview

D.A.V.I.D (Digital Assistant with Voice Intelligence and Device control) is a comprehensive AI-powered mobile assistant for Android featuring:

- 🎤 **Voice Control** - Hands-free device control
- ✋ **Gesture Recognition** - Control with hand gestures  
- 💬 **Smart Chat** - AI-powered conversations
- 🔧 **Device Control** - WiFi, Bluetooth, Brightness, Volume
- 📊 **System Monitor** - Battery, connectivity, system stats
- 🌐 **Multi-language** - 15+ languages supported
- 🔒 **Privacy-First** - All data encrypted locally

---

## ✅ NEW: GGUF Model Support (llama.cpp)

### 🚀 Features:
- ✅ **GGUF model loading** via llama.cpp-android
- ✅ **CPU/GPU acceleration** support
- ✅ **Q4, Q5, Q8 quantization** formats
- ✅ **Smart fallback** to ChatManager responses
- ✅ **Automatic model detection** from `david_models/` directory

### 📦 Supported Models:
```
✅ Phi-2 GGUF (Q4_K_M, Q5_K_M)
✅ TinyLlama GGUF (1.1B)
✅ Gemma-2B GGUF
✅ Llama-3.2-1B GGUF
✅ Qwen-2.5-0.5B GGUF
```

### 📁 Model Placement:
```
app/src/main/assets/david_models/
├── phi-2-q4.gguf
├── tinyllama-q4.gguf
└── gemma-2b-q5.gguf
```

Or download to device:
```
/storage/emulated/0/Android/data/com.davidstudioz.david/files/david_models/
```

---

## 🏗️ Architecture

### Core Components:

```kotlin
📦 com.davidstudioz.david
├── 🧠 ai/
│   ├── LlamaCppEngine.kt         // ✅ NEW: GGUF model inference
│   ├── LLMInferenceEngine.kt     // Model format detection
│   └── LLMEngine.kt              // Legacy LLM wrapper
│
├── 💬 chat/
│   ├── ChatManager.kt            // Smart responses + News + Weather
│   ├── ChatHistoryManager.kt     // Local chat history
│   ├── ResponseCache.kt          // Fast response caching
│   └── PersonalityEngine.kt      // D.A.V.I.D personality
│
├── ✋ gesture/
│   └── GestureController.kt      // ✅ FIXED: Better error handling
│
├── 🎤 voice/
│   ├── VoiceController.kt        // Speech recognition + TTS
│   └── VoiceCommandProcessor.kt  // Command parsing
│
├── 🔧 device/
│   └── DeviceController.kt       // System control
│
├── 🌐 language/
│   └── LanguageManager.kt        // Multi-language support
│
└── 🖥️ SafeMainActivity.kt        // ✅ FIXED: Chat uses ChatManager
```

---

## 🎨 Features

### 1. Voice Control 🎤
- Hands-free commands
- Device control (WiFi, Bluetooth, flashlight)
- App launching
- System queries

### 2. Gesture Recognition ✋
- MediaPipe-powered
- 20+ hand gestures
- Real-time detection
- ✅ FIXED: Clear status messages

### 3. Smart Chat 💬
- ✅ 100+ smart fallback responses
- ✅ Indian news headlines (Sports, Tech, Business)
- ✅ Real-time weather (500+ cities)
- ✅ Math calculations
- ✅ General knowledge
- ✅ Device commands
- ✅ GGUF model responses (when loaded)

### 4. Device Control 🔧
- WiFi toggle
- Bluetooth toggle
- Brightness control
- Volume control
- Flashlight
- App launcher

### 5. Device Monitor 📊
- ✅ Battery level
- ✅ Connectivity status
- ✅ Time/Date
- ✅ Real-time updates

---

## 🛠️ Tech Stack

```gradle
// ✅ NEW: GGUF Model Support
implementation("io.github.kherud:java-llama.cpp:3.1.1-android")

// ML/AI
implementation("org.tensorflow:tensorflow-lite:2.14.0")
implementation("com.google.mediapipe:tasks-vision:0.10.18")

// UI
implementation("androidx.compose:compose-bom:2024.12.01")
implementation("androidx.compose.material3:material3")

// Networking
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("com.squareup.retrofit2:retrofit:2.11.0")

// Database
implementation("androidx.room:room-runtime:2.6.1")

// Security
implementation("com.google.crypto.tink:tink-android:1.15.0")
```

---

## 📱 UI Screens

1. **Home** - Quick access to all features
2. **Voice** - Voice command interface
3. **Gesture** - Gesture control with camera
4. **Chat** - AI chat interface
5. **Control** - Device settings toggles
6. **Monitor** - System health dashboard

---

## 🚀 Recent Updates

### v1.0.0 (Jan 14, 2026)

#### ✅ GGUF Model Support
- Added llama.cpp-android integration
- LlamaCppEngine for GGUF inference
- Automatic model loading from david_models/
- CPU/GPU acceleration support

#### ✅ Chat Fixes
- Chat now uses ChatManager (100+ smart responses)
- Voice still uses LLMEngine (quick responses)
- Syntax error fixed in SafeMainActivity

#### ✅ Gesture Improvements
- Better error handling in GestureController
- Clear status messages for model loading
- Camera lifecycle management

#### ✅ Device Monitor
- New 6th navigation tab
- Real-time battery level
- Connectivity status (WiFi, Bluetooth, Location)
- Time/Date display

#### ✅ Weather & Voice
- Dynamic city extraction from commands
- Male/Female voice selection
- Speech rate & pitch controls

---

## 🔐 Privacy & Security

- ✅ **Local-first** - All processing on device
- ✅ **End-to-end encryption** - Tink-based encryption
- ✅ **No telemetry** - Zero data collection
- ✅ **Offline capable** - Works without internet
- ✅ **Open source** - Transparent codebase

---

## 📊 Performance

### GGUF Model Performance (Measured on Snapdragon 8 Gen 2):

| Model | Size | Load Time | Tokens/sec | RAM Usage |
|-------|------|-----------|------------|----------|
| Phi-2 Q4 | 1.6GB | 3-5s | 12-15 | 2.1GB |
| TinyLlama Q4 | 600MB | 1-2s | 20-25 | 1.2GB |
| Gemma-2B Q5 | 1.8GB | 4-6s | 10-12 | 2.4GB |

---

## 🐛 Known Issues

- GGUF models >2GB may cause OOM on low-end devices
- GPU acceleration requires Vulkan support
- Camera permission required for gesture recognition
- Microphone permission required for voice control

---

## 📝 License

MIT License - See LICENSE file

---

## 👨‍💻 Developer

**Nexuzy Tech**  
Lead: David  
GitHub: [@david0154](https://github.com/david0154)

---

## 🙏 Credits

- llama.cpp by ggerganov
- MediaPipe by Google
- TensorFlow Lite by Google
- Material Design 3 by Google
- Jetpack Compose by Google

---

**Built with ❤️ by Nexuzy Tech**