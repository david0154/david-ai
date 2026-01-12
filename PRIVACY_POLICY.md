# Privacy Policy for D.A.V.I.D AI

**Effective Date**: January 12, 2026  
**Last Updated**: January 12, 2026  
**Developer**: Nexuzy Tech Ltd.  
**Contact**: david@nexuzy.in

---

## Overview

D.A.V.I.D AI (Digital Assistant with Voice & Intelligent Decisions) is committed to protecting your privacy. This policy explains our data practices in detail.

---

## 🔒 WE DO NOT COLLECT ANY DATA

**Your Privacy is Our Top Priority**

D.A.V.I.D AI operates with a **privacy-first architecture**. We fundamentally believe that your data belongs to you and should stay on your device.

### What This Means:

- ❌ **No personal information collected**
- ❌ **No data sent to external servers**
- ❌ **No user tracking or analytics**
- ❌ **No behavioral profiling**
- ❌ **No account required**
- ❌ **No cloud storage**
- ❌ **No advertising IDs**
- ❌ **No third-party data sharing**

---

## Data Storage

All data is stored **locally on YOUR device** using standard Android storage mechanisms:

### Voice Recordings:
- Processed in real-time using on-device speech recognition
- Never stored permanently
- Deleted immediately after processing
- No audio files saved

### Camera Images:
- Used only for gesture recognition
- Processed frame-by-frame in memory
- Never saved to storage
- No photos or videos stored

### AI Models:
- Downloaded once from HuggingFace/ONNX repositories
- Stored locally in app-specific storage
- Can be deleted anytime
- No usage data sent back

### Chat History:
- Stored in local SQLite database (Room)
- Encrypted using Android Keystore
- Can be cleared anytime via settings
- Never leaves your device

### Settings & Preferences:
- Stored in SharedPreferences
- Includes language choice, user nickname
- Local to your device only

---

## Permissions

D.A.V.I.D AI requests the following permissions:

### 🔴 Required Permissions:

| Permission | Purpose | Data Collection |
|------------|---------|----------------|
| **Microphone** | Voice commands | Processed locally, not stored |
| **Camera** | Gesture recognition | Processed locally, not stored |
| **Internet** | Download AI models | Only for model downloads |

### 🟡 Optional Permissions:

| Permission | Purpose | Data Collection |
|------------|---------|----------------|
| **Phone** | Voice-controlled calling | No call data collected |
| **SMS** | Voice-controlled messaging | No message content collected |
| **Location** | Weather information | Coordinates used for weather API only |
| **Contacts** | Voice contact access | No contacts uploaded |
| **Storage** | Save AI models | Only app models stored |
| **Bluetooth** | Bluetooth control | No pairing data collected |

**Note**: You can deny optional permissions. Core features will still work.

---

## Third-Party Services

D.A.V.I.D AI uses the following open-source components and APIs:

### AI Models (Downloaded Once):

| Service | Purpose | Data Sharing |
|---------|---------|-------------|
| **OpenAI Whisper** | Speech recognition | No data sent after download |
| **HuggingFace Models** | Chat AI (TinyLlama, Qwen, Phi-2) | No data sent after download |
| **ONNX Models** | Vision classification | No data sent after download |
| **Google MediaPipe** | Hand tracking | Runs locally, no data sent |
| **TensorFlow Lite** | Language processing | Runs locally, no data sent |

### External APIs:

| Service | Purpose | Data Sent |
|---------|---------|----------|
| **Open-Meteo API** | Weather data | Only GPS coordinates (no user ID) |

**Important**: 
- All AI processing happens **on your device**
- Models are downloaded once and cached
- No usage telemetry sent to model providers
- No API keys linked to your identity

---

## Data Sharing

**We do NOT share any data because we do NOT collect any data.**

- No data sold to third parties
- No data shared with advertisers
- No data shared with analytics companies
- No data shared with government agencies (unless legally required)

---

## Your Rights

You have complete control over your data:

### ✅ Right to Access:
- All your data is in your device storage
- Chat history visible in app
- Settings accessible anytime

### ✅ Right to Delete:
- Clear chat history: Settings → Clear Chat
- Delete AI models: Settings → Manage Models
- Delete all data: Android Settings → Apps → D.A.V.I.D AI → Clear Data
- Uninstall app: Removes all app data permanently

### ✅ Right to Export:
- Chat history can be exported as text
- Settings can be backed up (local only)

### ✅ Right to Opt-Out:
- You can disable any permission anytime
- App will work with reduced functionality

---

## Security

### How We Protect Your Data:

- ✅ **Encrypted Storage**: Chat history encrypted using Google Tink
- ✅ **Secure Communications**: HTTPS only for model downloads
- ✅ **No Cloud Sync**: Data never leaves your device
- ✅ **ProGuard**: Code obfuscation enabled
- ✅ **No Hardcoded Keys**: No API keys in code
- ✅ **Permission-Based Access**: Request only needed permissions

---

## Children's Privacy

D.A.V.I.D AI does not knowingly collect data from children under 13. Since we don't collect any data at all, the app is safe for all ages. However, parental supervision is recommended for:

- Voice-controlled phone calls
- Voice-controlled messaging
- Internet access for model downloads

---

## Changes to This Policy

We may update this privacy policy to:

- Reflect new features
- Comply with legal requirements
- Improve clarity

Changes will be posted:
- In the app (Settings → Privacy Policy)
- On GitHub: [https://github.com/david0154/david-ai/blob/main/PRIVACY_POLICY.md](https://github.com/david0154/david-ai/blob/main/PRIVACY_POLICY.md)

**Last Updated**: January 12, 2026

---

## Contact Us

Questions about privacy? Contact us:

**Email**: david@nexuzy.in  
**GitHub**: [https://github.com/david0154/david-ai/issues](https://github.com/david0154/david-ai/issues)  
**Developer**: Nexuzy Tech Ltd.

---

## Legal Compliance

This privacy policy complies with:

- **GDPR** (General Data Protection Regulation - EU)
- **CCPA** (California Consumer Privacy Act - US)
- **Google Play Store** policies
- **Indian IT Act** 2000

---

**© 2026 Nexuzy Tech Ltd. All rights reserved.**

---

## Summary (TL;DR)

🔒 **We collect ZERO data**  
✅ **Everything runs on your device**  
🚫 **No tracking, no analytics, no cloud**  
💾 **You control all your data**  
🗑️ **Delete anytime**  

**Your privacy = Our priority** 🛡️
