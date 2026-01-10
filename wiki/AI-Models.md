# AI Models Guide

## 🤖 Overview

D.A.V.I.D AI uses multiple AI models for different capabilities. All models are downloaded and run locally on your device.

---

## 🎤 Voice Recognition Models

### Whisper (from OpenAI/HuggingFace)

**Purpose**: Speech-to-text conversion

#### Available Models:

**1. Whisper Tiny (75 MB)**
- For devices with 1-2GB RAM
- Fastest processing
- Good accuracy
- English support
- Source: `ggml-tiny.en.bin`

**2. Whisper Base (142 MB)**
- For devices with 2-3GB RAM
- Balanced performance
- Better accuracy
- English support
- Source: `ggml-base.en.bin`

**3. Whisper Small (466 MB)**
- For devices with 3GB+ RAM
- Best accuracy
- Slower processing
- English support
- Source: `ggml-small.en.bin`

**Format**: GGML  
**Source**: [HuggingFace](https://huggingface.co/ggerganov/whisper.cpp)

---

## 💬 Chat AI Models

### TinyLlama, Qwen, Phi-2 (from HuggingFace)

**Purpose**: Conversational AI and natural language understanding

#### Available Models:

**1. TinyLlama Chat Light (669 MB)**
- For devices with 2GB+ RAM
- Fast responses
- Good for basic conversations
- 1.1B parameters (quantized)
- Source: `tinyllama-1.1b-chat-v1.0.Q4_K_M.gguf`

**2. Qwen Chat Standard (1.1 GB)**
- For devices with 3GB+ RAM
- Better understanding
- More coherent responses
- 1.8B parameters (quantized)
- Source: `qwen1.5-1.8b-chat-q4_k_m.gguf`

**3. Phi-2 Chat Pro (1.6 GB)**
- For devices with 4GB+ RAM
- Best quality responses
- Advanced reasoning
- 2.7B parameters (quantized)
- Source: `phi-2.Q4_K_M.gguf`

**Format**: GGUF  
**Source**: [HuggingFace](https://huggingface.co/)

---

## 👁️ Vision Models

### MobileNet, ResNet (from ONNX)

**Purpose**: Image recognition and classification

#### Available Models:

**1. MobileNetV2 Lite (14 MB)**
- For devices with 1GB+ RAM
- Fast image recognition
- 1000 class categories
- Optimized for mobile
- Source: `mobilenetv2-12.onnx`

**2. ResNet50 Standard (98 MB)**
- For devices with 2GB+ RAM
- Better accuracy
- 1000 class categories
- Deep neural network
- Source: `resnet50-v2-7.onnx`

**Format**: ONNX  
**Source**: [ONNX Model Zoo](https://github.com/onnx/models)

---

## 👋 Gesture Recognition Models

### MediaPipe (from Google)

**Purpose**: Hand tracking and gesture recognition

#### Required Models:

**1. Hand Landmarker (25 MB)**
- 21-point hand tracking
- Real-time detection
- Multi-hand support
- Source: `hand_landmarker.task`

**2. Gesture Recognizer (31 MB)**
- Gesture classification
- 5 gesture types
- High accuracy
- Source: `gesture_recognizer.task`

**Format**: TFLite  
**Source**: [Google MediaPipe](https://developers.google.com/mediapipe)

---

## 🌍 Language Models

### Universal Sentence Encoder (from TensorFlow)

**Purpose**: Multi-language support and text understanding

**Per Language**: 50 MB  
**Languages Supported**: 15

#### Supported Languages:
1. English
2. Hindi
3. Tamil
4. Telugu
5. Bengali
6. Marathi
7. Gujarati
8. Kannada
9. Malayalam
10. Punjabi
11. Odia
12. Urdu
13. Sanskrit
14. Kashmiri
15. Assamese

**Format**: TFLite  
**Source**: [TensorFlow Hub](https://tfhub.dev/)

---

## 📊 Model Selection

### Automatic Selection

D.A.V.I.D AI automatically selects models based on your device RAM:

#### Low-End Devices (1-2GB RAM)
- Voice: Tiny (75MB)
- Chat: Light (669MB)
- Vision: Lite (14MB)
- Gesture: Both (56MB)
- Language: English (50MB)
- **Total: ~914 MB**

#### Mid-Range Devices (2-4GB RAM)
- Voice: Base (142MB)
- Chat: Standard (1.1GB)
- Vision: Standard (98MB)
- Gesture: Both (56MB)
- Language: English + 2 others (150MB)
- **Total: ~1.6 GB**

#### High-End Devices (4GB+ RAM)
- Voice: Small (466MB)
- Chat: Pro (1.6GB)
- Vision: Standard (98MB)
- Gesture: Both (56MB)
- Language: All 15 (750MB)
- **Total: ~2.9 GB**

---

## 📥 Model Download

### First Time

1. App detects device RAM
2. Selects appropriate models
3. Downloads sequentially
4. Shows progress
5. Installs models

**Time**: 2-5 minutes

### Sources

All models are downloaded from:
- [HuggingFace](https://huggingface.co/) - Voice & Chat
- [ONNX Model Zoo](https://github.com/onnx/models) - Vision
- [Google MediaPipe](https://storage.googleapis.com/mediapipe-models/) - Gesture
- [TensorFlow Hub](https://tfhub.dev/) - Language

---

## 🗑️ Model Management

### View Downloaded Models

1. Open D.A.V.I.D AI
2. Go to **Settings**
3. Select **Model Management**
4. View all downloaded models

### Delete Models

1. Go to **Settings** → **Model Management**
2. Tap **Delete All**
3. Confirm deletion
4. Models will be deleted
5. Re-download on next launch

### Update Models

*Model updates coming in future versions*

---

## ⚡ Performance

### Processing Speed

**Voice Recognition**:
- Tiny: ~100ms
- Base: ~200ms
- Small: ~500ms

**Chat Responses**:
- Light: 1-2 seconds
- Standard: 2-4 seconds
- Pro: 4-6 seconds

**Vision Processing**:
- Lite: ~50ms
- Standard: ~100ms

**Gesture Detection**:
- Real-time: ~30 FPS

*Times vary by device*

---

## 🔒 Privacy

### All Models Local

✅ Downloaded once from public sources  
✅ Stored on your device  
✅ Processing entirely local  
✅ No data sent to servers  
✅ No tracking or analytics  

### Open Source

All models are from open-source projects:
- Whisper (OpenAI)
- TinyLlama, Qwen, Phi-2 (HuggingFace)
- ONNX (Microsoft)
- MediaPipe (Google)
- TensorFlow (Google)

---

## ❓ FAQ

### Can I choose which models to download?

Currently auto-selected. Manual selection coming soon.

### Why are models so large?

AI models contain millions of parameters. This is normal for on-device AI.

### Can I use custom models?

*Custom model support coming in future updates*

### Do models get updated?

*Model updates will be available in future versions*

---

## 📞 Support

Questions about AI models?
- 📧 Email: [david@nexuzy.in](mailto:david@nexuzy.in)
- 🐛 Report Issues: [GitHub](https://github.com/david0154/david-ai/issues)

---

**© 2026 Nexuzy Tech Ltd.**
