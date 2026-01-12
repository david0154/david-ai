# Privacy Policy for D.A.V.I.D AI

**Effective Date**: January 12, 2026  
**Last Updated**: January 12, 2026

## Overview

D.A.V.I.D AI (Digital Assistant with Voice & Intelligent Decisions) is committed to protecting your privacy. This policy explains our data practices and how we handle your information.

## 🔒 Data Collection

### WE DO NOT COLLECT ANY DATA

D.A.V.I.D AI operates with a privacy-first approach:

- ❌ **No personal information collected** - We don't ask for or store your name, email, phone number, or any identifying information
- ❌ **No data sent to external servers** - All processing happens locally on your device
- ❌ **No user tracking or analytics** - We don't track your usage patterns or behavior
- ❌ **No advertising or marketing data** - We don't collect data for ads or marketing purposes
- ❌ **No account required** - You can use all features without creating an account
- ❌ **No cloud storage** - Your data never leaves your device

## 📱 Data Storage

All data is stored **locally on YOUR device only**:

### Voice Data
- Voice recordings are processed in real-time for speech recognition
- Audio is immediately deleted after processing
- No voice data is stored or transmitted

### Camera/Images
- Camera images are processed locally for gesture recognition
- Images are analyzed frame-by-frame and immediately discarded
- No photos or videos are saved

### AI Models
- AI models (speech, language, vision) are downloaded once from public repositories
- Models are stored locally on your device
- Models are only updated when you explicitly choose to update them

### Chat History
- Your conversations with D.A.V.I.D are stored in a local database (Room)
- Chat history never leaves your device
- You can delete chat history at any time from settings

### Settings & Preferences
- App preferences (language, theme, etc.) are stored in SharedPreferences
- Settings remain on your device only

## 🔐 Permissions

D.A.V.I.D AI requests the following permissions:

### Required Permissions:

**🎤 Microphone (android.permission.RECORD_AUDIO)**
- Purpose: Voice command recognition
- Processing: All voice processing happens locally using on-device models
- Storage: Voice data is not stored

**📷 Camera (android.permission.CAMERA)**
- Purpose: Hand gesture recognition for touchless control
- Processing: Images processed locally for hand tracking
- Storage: No images are saved

**🌐 Internet (android.permission.INTERNET)**
- Purpose: Only for downloading AI models (one-time)
- Usage: No ongoing internet connection required after models are downloaded
- Data: No user data transmitted

### Optional Permissions:

**📞 Phone (android.permission.CALL_PHONE)**
- Purpose: Voice-controlled phone calls ("Call John")
- Your control: Only activates when you use voice commands for calling

**💬 SMS (android.permission.SEND_SMS)**
- Purpose: Voice-controlled messaging ("Send message")
- Your control: Only activates when you use voice commands for messaging

**📍 Location (android.permission.ACCESS_FINE_LOCATION)**
- Purpose: Local weather information
- Processing: Location used only for weather API calls (Open-Meteo)
- Storage: Location coordinates not stored

**📇 Contacts (android.permission.READ_CONTACTS)**
- Purpose: Voice-controlled contact access ("Call Mom")
- Your control: Only accessed when you use contact-related voice commands

**💾 Storage (android.permission.WRITE_EXTERNAL_STORAGE)**
- Purpose: Saving downloaded AI models
- Storage: Only AI models are saved, no personal data

**🔵 Bluetooth (android.permission.BLUETOOTH_CONNECT, BLUETOOTH_SCAN - Android 12+)**
- Purpose: Voice-controlled Bluetooth toggle
- Your control: Only used when you say "Turn on/off Bluetooth"

## 🌍 Third-Party Services

D.A.V.I.D AI uses the following third-party services:

### AI Models (Downloaded Once)

**OpenAI Whisper** (Speech Recognition)
- Source: [OpenAI GitHub](https://github.com/openai/whisper)
- License: MIT License
- Usage: Downloaded once, runs locally
- Data: No data sent to OpenAI

**HuggingFace Models** (Chat AI - TinyLlama, Qwen, Phi-2)
- Source: [HuggingFace](https://huggingface.co)
- License: Apache 2.0 / MIT (varies by model)
- Usage: Downloaded once, runs locally
- Data: No data sent to HuggingFace

**ONNX Models** (Vision Classification)
- Source: [ONNX](https://onnx.ai)
- License: Apache 2.0
- Usage: Downloaded once, runs locally
- Data: No image data transmitted

**Google MediaPipe** (Gesture Recognition)
- Source: [MediaPipe](https://mediapipe.dev)
- License: Apache 2.0
- Usage: Downloaded once, runs locally
- Data: No image data transmitted

**TensorFlow Lite** (Language Models)
- Source: [TensorFlow](https://tensorflow.org/lite)
- License: Apache 2.0
- Usage: Downloaded once, runs locally
- Data: No data transmitted

### Weather API

**Open-Meteo** (Weather Information)
- Source: [Open-Meteo](https://open-meteo.com)
- License: CC BY 4.0
- Usage: Public API for weather forecasts
- Data: Only location coordinates sent (if permission granted)
- Privacy: No user tracking, no API key required

**None of these services receive your personal data, voice recordings, or images.**

## 🚫 Data Sharing

**WE DO NOT SHARE ANY DATA** because **WE DO NOT COLLECT ANY DATA**.

- No data shared with third parties
- No data sold to advertisers
- No data provided to analytics companies
- No data given to data brokers

## 🛡️ Data Security

Even though we don't collect data, we implement security measures:

- All app data stored using Android's encrypted storage
- HTTPS-only for model downloads
- No plaintext sensitive data
- ProGuard code obfuscation
- Runtime permission checks

## 👤 Your Rights

You have complete control over your data:

### Right to Access
- All your data is on your device
- You can view chat history anytime

### Right to Delete
- Delete chat history from settings
- Clear app data from Android settings
- Uninstall app to remove all data

### Right to Export
- Chat history can be exported (if implemented)
- Settings can be backed up manually

### Right to Control
- Revoke permissions anytime from Android settings
- Disable features you don't want to use

## 👶 Children's Privacy

D.A.V.I.D AI does not knowingly collect data from children under 13. Since we don't collect any data at all, the app is safe for all ages. However, parental supervision is recommended for features like phone calls and messaging.

## 🌍 International Users

D.A.V.I.D AI can be used anywhere in the world. Since all data stays on your device:

- No GDPR concerns (EU)
- No CCPA concerns (California)
- No data localization requirements
- Compliant with all data protection laws

## 📱 App Store Compliance

This privacy policy meets requirements for:

- ✅ Google Play Store
- ✅ Apple App Store (if released on iOS)
- ✅ F-Droid
- ✅ Samsung Galaxy Store
- ✅ Amazon Appstore

## 🔄 Changes to This Policy

We may update this privacy policy from time to time. When we do:

- Updated policy will be posted in the app
- Updated policy will be posted on GitHub
- Effective date will be updated
- You'll be notified of significant changes

Continued use of D.A.V.I.D AI after changes means you accept the updated policy.

## 📧 Contact Us

Questions about this privacy policy?

**Email**: [david@nexuzy.in](mailto:david@nexuzy.in)  
**GitHub**: [github.com/david0154/david-ai](https://github.com/david0154/david-ai)  
**Issues**: [github.com/david0154/david-ai/issues](https://github.com/david0154/david-ai/issues)

## 📜 Legal

**Developer**: Nexuzy Tech Ltd.  
**Location**: India  
**Registration**: [Company details if applicable]

---

**© 2026 Nexuzy Tech Ltd. All rights reserved.**

Last Updated: January 12, 2026
