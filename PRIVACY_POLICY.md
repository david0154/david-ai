# Privacy Policy for D.A.V.I.D AI

**Effective Date**: January 12, 2026  
**Last Updated**: January 12, 2026

## Overview

D.A.V.I.D AI (Digital Assistant with Voice & Intelligent Decisions) is committed to protecting your privacy. This policy explains our data practices in detail.

---

## 🔒 WE DO NOT COLLECT ANY DATA

**Your Privacy is Our Top Priority**

D.A.V.I.D AI is designed with privacy at its core. We believe your data belongs to you and should stay on your device.

### What We DON'T Do:

- ❌ **No Data Collection**: We do NOT collect any personal information
- ❌ **No External Servers**: We do NOT send data to external servers
- ❌ **No Tracking**: We do NOT track user behavior or analytics
- ❌ **No Accounts**: We do NOT require user accounts or logins
- ❌ **No Cloud Storage**: We do NOT store your data in the cloud
- ❌ **No Third-Party Sharing**: We do NOT share data with third parties
- ❌ **No Advertising**: We do NOT use your data for advertising

---

## 📱 Data Storage

All data processing happens **locally on YOUR device**:

### Voice Recordings
- Processed in real-time for voice commands
- **Never stored permanently**
- Deleted immediately after processing
- Never uploaded to any server

### Camera Images
- Processed for gesture recognition only
- **Never saved to storage**
- Analyzed frame-by-frame and discarded
- Never uploaded to any server

### AI Models
- Downloaded once from HuggingFace/ONNX repositories
- Stored locally on your device
- Run entirely offline after download
- No model usage data sent anywhere

### Chat History
- Stored in local SQLite database (Room)
- Never leaves your device
- Can be deleted anytime via app settings
- Removed completely on app uninstall

### Settings & Preferences
- Stored in Android SharedPreferences
- Includes: language preference, nickname, theme
- Stays on your device
- Deleted on app uninstall

---

## 🔐 Permissions Explained

D.A.V.I.D AI requests the following permissions:

### Required Permissions:

#### 🎤 **Microphone (RECORD_AUDIO)**
- **Purpose**: Voice commands and speech recognition
- **Processing**: All voice processing done locally using Whisper model
- **Storage**: Voice data not stored, processed and discarded immediately

#### 📷 **Camera (CAMERA)**
- **Purpose**: Hand gesture recognition for device control
- **Processing**: Real-time gesture detection using MediaPipe
- **Storage**: Camera frames not saved, analyzed and discarded

#### 🌐 **Internet (INTERNET)**
- **Purpose**: 
  - Downloading AI models (one-time)
  - Fetching weather data (optional)
- **Data Sent**: Only weather API requests (location only, no personal data)
- **Note**: App works offline after models are downloaded

### Optional Permissions:

#### 📞 **Phone (CALL_PHONE)**
- **Purpose**: Voice-controlled calling ("Call Mom")
- **Usage**: Only when you explicitly use voice commands
- **Data**: Phone numbers stay on your device

#### 💬 **SMS (SEND_SMS)**
- **Purpose**: Voice-controlled messaging
- **Usage**: Only when you explicitly use voice commands
- **Data**: Messages handled by Android, not stored by app

#### 📍 **Location (ACCESS_FINE_LOCATION)**
- **Purpose**: Weather information based on your location
- **Usage**: Only sent to Open-Meteo weather API (free, no tracking)
- **Data**: Location never stored or logged

#### 📇 **Contacts (READ_CONTACTS)**
- **Purpose**: Voice-controlled "Call John" commands
- **Usage**: Read-only access, contacts stay on device
- **Data**: Never uploaded or stored separately

#### 💾 **Storage (WRITE_EXTERNAL_STORAGE)**
- **Purpose**: Saving downloaded AI models
- **Usage**: Only for model storage in app folder
- **Data**: Only AI model files stored

#### 🔵 **Bluetooth (BLUETOOTH_CONNECT, BLUETOOTH_SCAN)**
- **Purpose**: Voice-controlled Bluetooth toggle
- **Usage**: Only when you use "Turn on Bluetooth" command
- **Data**: No Bluetooth data collected

---

## 🌍 Third-Party Services

D.A.V.I.D AI uses the following services:

### AI Models (Downloaded Once)

1. **OpenAI Whisper** (Speech Recognition)
   - Downloaded from: HuggingFace
   - Privacy: Runs locally, no data sent
   - License: MIT

2. **HuggingFace Models** (Chat AI)
   - Models: TinyLlama, Qwen2.5, Phi-2
   - Downloaded from: HuggingFace.co
   - Privacy: Runs locally, no data sent
   - License: Various open source licenses

3. **ONNX Models** (Vision Classification)
   - Downloaded from: ONNX Model Zoo
   - Privacy: Runs locally, no data sent
   - License: Apache 2.0

4. **Google MediaPipe** (Gesture Recognition)
   - Integrated as library
   - Privacy: Runs locally, no data sent
   - License: Apache 2.0

5. **TensorFlow Lite** (Language Processing)
   - Integrated as library
   - Privacy: Runs locally, no data sent
   - License: Apache 2.0

### External APIs (Optional)

1. **Open-Meteo API** (Weather)
   - Purpose: Weather forecasts
   - Data sent: Geographic coordinates only
   - Privacy: Free API, no tracking, no registration
   - Website: [open-meteo.com](https://open-meteo.com)
   - Policy: No personal data collected

**Important**: None of these services receive your personal data, voice recordings, or images.

---

## 🛡️ Data Security

### How We Protect Your Data:

- ✅ **Local Processing**: All AI runs on your device
- ✅ **No Cloud Sync**: Data never synced to cloud
- ✅ **Encrypted Storage**: Android encrypted storage used
- ✅ **HTTPS Only**: All model downloads use HTTPS
- ✅ **No Analytics**: Zero telemetry or crash reporting
- ✅ **Open Source**: Code is public, auditable on GitHub

---

## 👤 Your Rights

You have complete control over your data:

### You Can:

- ✅ **View Data**: Check chat history anytime
- ✅ **Delete Data**: Clear chat history via settings
- ✅ **Export Data**: No export needed (data stays on device)
- ✅ **Revoke Permissions**: Disable any permission in Android settings
- ✅ **Uninstall**: Removes ALL data completely

### Data Retention:

- **Chat History**: Kept until you delete it or uninstall app
- **AI Models**: Kept until you delete them or uninstall app
- **Settings**: Kept until you clear app data or uninstall
- **Voice/Camera**: Never stored

---

## 🌐 International Users

D.A.V.I.D AI works globally:

- ✅ **GDPR Compliant** (Europe): No data collection = automatic compliance
- ✅ **CCPA Compliant** (California): No data sale = automatic compliance
- ✅ **Works Everywhere**: No region restrictions
- ✅ **Offline Capable**: Works without internet after setup

---

## 👶 Children's Privacy

D.A.V.I.D AI does not knowingly collect data from anyone, including children under 13:

- No age verification required
- No data collection from any age group
- Safe for all ages (with parental supervision for device control features)

---

## 🔄 Changes to This Policy

We may update this privacy policy:

- Changes will be posted in the app
- Changes will be posted on GitHub
- Effective date will be updated
- Major changes will be announced in release notes

**Current Version**: 1.0.0 (January 12, 2026)

---

## 📧 Contact Us

Questions about privacy?

**Email**: [david@nexuzy.in](mailto:david@nexuzy.in)

**GitHub**: [github.com/david0154/david-ai](https://github.com/david0154/david-ai)

**Developer**: Nexuzy Tech Ltd.

**Response Time**: Usually within 48 hours

---

## 📜 Legal

### Disclaimer

D.A.V.I.D AI is provided "as is" without warranties. Users are responsible for how they use device control features.

### Governing Law

This policy is governed by the laws of India.

### Acceptance

By using D.A.V.I.D AI, you agree to this privacy policy.

---

**© 2026 Nexuzy Tech Ltd. All rights reserved.**

*Last Updated: January 12, 2026*
