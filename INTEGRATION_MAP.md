# D.A.V.I.D AI - Component Integration Map

## 🟢 Complete Function Call Flow

This document maps ALL component interactions and function calls across the entire codebase.

---

## 🏛️ Architecture Overview

```
┌────────────────────┐
│  SafeMainActivity  │
│   (Main UI Hub)    │
└────────┬───────────┘
         │
    ┌────┼────┐
    │         │
┌───┴───┐ ┌──┴────┐
│ Voice │ │  Chat  │
│Control│ │ Engine │
└──┬────┘ └──┬────┘
   │         │
   └───┬────┘
       │
   ┌───┴────┐
   │ LLMEngine │
   └─────────┘
```

---

## 1️⃣ SafeMainActivity (Main Hub)

### **Initializes:**
- `VoiceController(context)`
- `GestureController(context)`
- `DeviceController(context)`
- `LanguageManager(context)`
- `ChatHistoryManager(context)`
- `LLMEngine(context)`
- `EncryptionManager(context)`

### **Calls Functions:**

#### Voice Control:
```kotlin
voiceController.startListening { text -> 
    llmEngine.generateResponse(text)
    voiceController.speak(response)
}
voiceController.stopListening()
voiceController.cleanup()
```

#### Chat:
```kotlin
chatHistoryManager.addMessage(text, isUser = true)
chatHistoryManager.getRecentMessages()
llmEngine.generateResponse(userInput)
```

#### Gesture:
```kotlin
gestureController.startGestureRecognition { gesture ->
    voiceController.speak("$gesture detected!")
}
gestureController.stopGestureRecognition()
```

#### Device Control:
```kotlin
deviceController.isWiFiEnabled()
deviceController.toggleWiFi(enable)
deviceController.isBluetoothEnabled()
deviceController.toggleBluetooth(enable)
deviceController.getBrightnessLevel()
deviceController.setBrightnessLevel(level)
```

#### Language:
```kotlin
languageManager.getSupportedLanguages()
languageManager.getDownloadedLanguages()
languageManager.getCurrentLanguage()
languageManager.setCurrentLanguage(code)
```

---

## 2️⃣ VoiceController

### **Dependencies:**
- `VoiceRecognitionEngine`
- `TextToSpeechEngine`
- `HotWordDetector`
- `VoiceCommandProcessor`

### **Called By:**
- `SafeMainActivity` → voice screen interactions
- `GestureController` → voice feedback on gestures
- `DeviceController` → voice confirmations

### **Calls:**
```kotlin
// To VoiceRecognitionEngine
voiceRecognitionEngine.startRecognition(callback)
voiceRecognitionEngine.stopRecognition()

// To TextToSpeechEngine
ttsEngine.speak(text)
ttsEngine.setLanguage(locale)

// To VoiceCommandProcessor
commandProcessor.processCommand(text) → LLMEngine

// To HotWordDetector
hotWordDetector.startListening()
hotWordDetector.onHotWordDetected { startRecognition() }
```

---

## 3️⃣ GestureController

### **Dependencies:**
- Camera API
- MediaPipe (gesture recognition ML)

### **Called By:**
- `SafeMainActivity` → gesture screen

### **Calls:**
```kotlin
// Callbacks to SafeMainActivity
onGestureDetected(gestureName)

// Can trigger:
voiceController.speak("Gesture detected")
deviceController.executeAction(gesture)
```

---

## 4️⃣ ChatHistoryManager

### **Dependencies:**
- `EncryptionManager`

### **Called By:**
- `SafeMainActivity` → chat screen
- `ChatEngine` → message management
- `LLMEngine` → context retrieval

### **Calls:**
```kotlin
// To EncryptionManager
encryptionManager.encrypt(messageData)
encryptionManager.decrypt(encryptedData)
```

### **Functions:**
```kotlin
addMessage(content, isUser) // Store message
getRecentMessages(limit) // Retrieve recent
getAllMessages() // Full history
getContextForLLM(maxMessages) // LLM context
clearHistory() // Delete all
```

---

## 5️⃣ LLMEngine (AI Brain)

### **Called By:**
- `SafeMainActivity` → chat + voice responses
- `VoiceController` → voice command processing
- `ChatEngine` → conversation management
- `VoiceCommandProcessor` → command interpretation

### **Calls:**
```kotlin
// To ChatHistoryManager
chatHistoryManager.getContextForLLM() // Get conversation context

// Response generation (internal)
generateResponse(userInput) → String
generateStreamingResponse(input, onToken)
```

### **Integration Points:**
```kotlin
// Voice Flow:
User speaks → VoiceController → LLMEngine → Response → TTS

// Chat Flow:
User types → SafeMainActivity → LLMEngine → Response → UI

// Command Flow:
Voice command → VoiceCommandProcessor → LLMEngine → Device action
```

---

## 6️⃣ DeviceController

### **Dependencies:**
- Android System Services (WiFi, Bluetooth, Settings)

### **Called By:**
- `SafeMainActivity` → device control screen
- `VoiceCommandProcessor` → voice-triggered actions
- `GestureController` → gesture-triggered actions

### **Functions:**
```kotlin
isWiFiEnabled() → Boolean
toggleWiFi(enable: Boolean)
isBluetoothEnabled() → Boolean
toggleBluetooth(enable: Boolean)
getBrightnessLevel() → Float
setBrightnessLevel(level: Float)
getDeviceInfo() → DeviceInfo
```

---

## 7️⃣ LanguageManager

### **Called By:**
- `SafeMainActivity` → language selector dialog
- `VoiceController` → voice recognition language
- `ChatEngine` → response language
- `SettingsActivity` → language settings

### **Functions:**
```kotlin
getSupportedLanguages() → List<Language>
getDownloadedLanguages() → List<Language>
getCurrentLanguage() → Language
setCurrentLanguage(code: String)
getEnabledLanguages() → List<Language>
enableLanguage(code: String)
disableLanguage(code: String)
```

---

## 8️⃣ EncryptionManager

### **Called By:**
- `ChatHistoryManager` → message encryption
- `VoiceProfile` → voice data encryption
- `SettingsActivity` → privacy status

### **Functions:**
```kotlin
encrypt(data: String) → String
decrypt(encryptedData: String) → String
isInitialized() → Boolean
```

---

## 9️⃣ SettingsActivity

### **Dependencies:**
- `LanguageManager`
- `EncryptionManager`

### **Called By:**
- `SafeMainActivity` → settings button/icon

### **Calls:**
```kotlin
languageManager.getDownloadedLanguages()
encryptionManager.isInitialized()
```

---

## 🔄 Complete Data Flow Examples

### 🎤 Voice Command Flow:
```
1. User speaks: "Turn on WiFi"
2. SafeMainActivity.onToggleListening()
3. VoiceController.startListening()
4. VoiceRecognitionEngine recognizes text
5. Callback returns: "turn on wifi"
6. LLMEngine.generateResponse("turn on wifi")
7. LLMEngine detects device control intent
8. SafeMainActivity gets response
9. DeviceController.toggleWiFi(true)
10. VoiceController.speak("WiFi enabled")
11. TextToSpeechEngine speaks response
```

### 💬 Chat Flow:
```
1. User types: "What time is it?"
2. SafeMainActivity.onSendMessage()
3. ChatHistoryManager.addMessage("What time is it?", isUser = true)
4. LLMEngine.generateResponse("What time is it?")
5. LLMEngine detects time query
6. Generates: "The current time is 4:51 PM"
7. ChatHistoryManager.addMessage(response, isUser = false)
8. SafeMainActivity updates UI with response
```

### ✋ Gesture Flow:
```
1. User taps "Start Detection"
2. SafeMainActivity.onToggleActive()
3. GestureController.startGestureRecognition()
4. Camera captures hand
5. ML model detects "thumbs_up"
6. Callback: onGestureDetected("thumbs_up")
7. SafeMainActivity updates UI
8. VoiceController.speak("Thumbs up detected!")
```

---

## ✅ Integration Status

| Component | Status | Connected To | Functions Exposed |
|-----------|--------|--------------|-------------------|
| **SafeMainActivity** | ✅ Complete | All controllers | UI event handlers |
| **VoiceController** | ✅ Complete | LLMEngine, TTS, Recognition | 15+ functions |
| **GestureController** | ✅ Complete | SafeMainActivity, Camera | 8+ functions |
| **ChatHistoryManager** | ✅ Complete | EncryptionManager, LLMEngine | 6 functions |
| **LLMEngine** | ✅ Complete | ChatHistoryManager, All UIs | 3 main functions |
| **DeviceController** | ✅ Complete | System APIs, SafeMainActivity | 7 functions |
| **LanguageManager** | ✅ Complete | VoiceController, ChatEngine | 7 functions |
| **EncryptionManager** | ✅ Complete | ChatHistoryManager, Storage | 3 functions |
| **SettingsActivity** | ✅ Complete | LanguageManager, Encryption | UI management |

---

## 🔍 Testing Each Connection

### Voice → LLM → Response:
```kotlin
// Test in SafeMainActivity
voiceController.startListening { recognizedText ->
    scope.launch {
        val response = llmEngine.generateResponse(recognizedText)
        voiceController.speak(response)
    }
}
```

### Chat → History → Encryption:
```kotlin
// Test in SafeMainActivity
val userMessage = "Test message"
chatHistoryManager.addMessage(userMessage, isUser = true) // Auto-encrypts
val history = chatHistoryManager.getRecentMessages() // Auto-decrypts
```

### Gesture → Action → Voice:
```kotlin
// Test in SafeMainActivity
gestureController.startGestureRecognition { gesture ->
    voiceController.speak("$gesture detected!")
    // Optional: trigger device action
    if (gesture == "thumbs_up") {
        deviceController.toggleWiFi(true)
    }
}
```

---

## 🛠️ Debugging Guide

### Check Controller Initialization:
```kotlin
Log.d("DAVID", "VoiceController: ${::voiceController.isInitialized}")
Log.d("DAVID", "LLMEngine: ${::llmEngine.isInitialized}")
Log.d("DAVID", "ChatHistory: ${::chatHistoryManager.isInitialized}")
```

### Verify Function Calls:
```kotlin
// Add to each controller
override fun functionName() {
    Log.d(TAG, "functionName called from ${Thread.currentThread().stackTrace[3].className}")
    // ... function code
}
```

### Test Data Flow:
```kotlin
// SafeMainActivity onCreate
scope.launch {
    // Test voice
    val voiceTest = llmEngine.generateResponse("hello")
    Log.d("INTEGRATION_TEST", "Voice test: $voiceTest")
    
    // Test chat
    chatHistoryManager.addMessage("test", true)
    Log.d("INTEGRATION_TEST", "Chat count: ${chatHistoryManager.getRecentMessages().size}")
    
    // Test gesture
    Log.d("INTEGRATION_TEST", "Gestures: ${gestureController.getSupportedGestures()}")
}
```

---

## 📊 Performance Notes

- **LLMEngine**: Async operations, uses `withContext(Dispatchers.IO)`
- **ChatHistoryManager**: File I/O on background thread
- **VoiceController**: Callbacks on main thread
- **GestureController**: ML inference on dedicated thread
- **DeviceController**: System calls, some may open settings UI

---

## 🆕 Version

Integration Map Version: **1.0.0**  
Last Updated: January 12, 2026  
All components: **Fully Connected ✅**
