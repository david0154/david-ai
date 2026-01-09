# 🤖 Gesture Recognition & Control Guide

## Overview

David AI now includes **advanced gesture recognition** powered by Google MediaPipe Hand Landmarker. This guide covers:

- Hand gesture detection via camera
- Real-time hand position tracking
- Pointer/cursor control for device interaction
- Voice lock/unlock
- Multi-gesture support (swipe, tap, pinch, etc.)
- Complete device access management

---

## Features

### 1. Hand Gesture Recognition

Supported gestures:

```
✅ Open Palm      - All fingers extended
✅ Closed Fist    - All fingers closed
✅ Pointing       - Index finger only
✅ Thumbs Up      - Thumb extended upward
✅ Victory (V)    - Two fingers (index + middle)
✅ Pinch          - Thumb + index together
✅ Swipe Left     - Hand moves left
✅ Swipe Right    - Hand moves right
✅ Single Tap     - Quick tap gesture
✅ Double Tap     - Two taps within 300ms
✅ Long Press     - Hold hand still for 500ms
```

### 2. Hand Position Tracking

- Real-time 21-point hand landmark detection
- Normalized screen coordinates (0-1)
- Confidence scoring for each detection
- Smooth movement interpolation

### 3. Pointer Control

- Virtual mouse cursor controlled by hand
- Click, double-click, long-press actions
- Custom click callbacks
- Overlay with visual feedback (cyan crosshair)

### 4. Voice Commands

```bash
# Device control
"Hey David, lock device"      # Voice-activated device lock
"Call 9876543210"             # Make phone calls
"Send SMS to [name]"          # Send messages

# Connectivity
"WiFi on/off"                 # Toggle WiFi
"Bluetooth on/off"            # Toggle Bluetooth

# Device features
"Turn on flashlight"          # Activate torch
"Open [app name]"             # Launch apps
"What's the weather?"         # Weather info
"Tell me the time"            # Current time

# Chat with AI
"Hello David"                 # Any natural language query
```

### 5. Device Access Management

All permissions grouped and monitored:

```kotlin
enum class PermissionGroup {
    CAMERA,           // Gesture recognition
    MICROPHONE,       // Voice commands
    LOCATION,         // Weather API
    CONTACTS,         // SMS/call
    SMS,              // Send messages
    CALL,             // Make calls
    STORAGE,          // File access
    BLUETOOTH,        // Device pairing
    WIFI,             // Network control
    OVERLAY,          // Pointer display
    DEVICE_ADMIN      // Device lock
}
```

### 6. AI Chat Integration

- Offline AI using llama.cpp
- Context-aware responses
- SMS integration
- Chat history tracking

### 7. Weather & Time

- Real-time time display
- Current weather (via location)
- 3-day forecast
- Temperature conversion

---

## Implementation Details

### Architecture

```
MainActivity
├── GestureController ──────→ Hand gesture detection
│   └── MediaPipe Hand Landmarker
├── PointerController ──────→ Virtual cursor
├── VoiceRecognitionEngine ─→ Speech-to-text (Whisper)
├── DeviceLockManager ──────→ Voice lock/unlock
├── ChatManager ────────────→ AI responses + SMS
├── WeatherTimeProvider ────→ Current time & weather
├── DeviceController ───────→ 20+ device commands
└── DeviceAccessManager ───→ Permission management
```

### Camera & Gesture Flow

```
1. Camera Input (Front camera)
   ↓
2. MediaPipe Processing
   ├─ Hand detection
   ├─ 21-point landmark extraction
   └─ Hand position tracking
   ↓
3. Gesture Classification
   ├─ Compare landmarks
   ├─ Calculate finger distances
   └─ Classify gesture type
   ↓
4. Gesture Handler
   ├─ Swipe detection
   ├─ Tap counting (single/double)
   ├─ Long press detection
   └─ Callback to UI
   ↓
5. Action Execution
   ├─ Move pointer
   ├─ Execute command
   └─ Visual feedback
```

### Voice Recognition Flow

```
1. Audio Capture (16kHz PCM)
   ↓
2. Whisper.cpp Processing
   ├─ Auto-detect language
   ├─ Speech recognition
   └─ Confidence scoring
   ↓
3. Command Processing
   ├─ Parse user intent
   ├─ Extract parameters
   └─ Match command type
   ↓
4. Action Execution
   ├─ Device control
   ├─ AI chat
   ├─ SMS/Call
   └─ TTS response
```

---

## Code Usage Examples

### 1. Start Gesture Recognition

```kotlin
val gestureController = GestureController(this) { gesture, details ->
    when (gesture) {
        GestureController.GestureType.OPEN_PALM -> {
            println("Palm detected!")
        }
        GestureController.GestureType.SWIPE_LEFT -> {
            println("Swipe left detected!")
        }
        GestureController.GestureType.DOUBLE_TAP -> {
            println("Double tap at $details")
        }
        else -> {}
    }
}

// Start with camera
gestureController.startCameraGestureRecognition(this, previewView)
```

### 2. Control Pointer

```kotlin
val pointerController = PointerController(this)

// Show pointer
pointerController.showPointer()

// Move pointer (normalized 0-1)
pointerController.movePointer(0.5f, 0.3f)  // Center-top

// Perform actions
pointerController.click()
pointerController.doubleClick()
pointerController.longPress(duration = 1000)

// Listen for clicks
pointerController.setOnClickListener { x, y ->
    println("Clicked at ($x, $y)")
}
```

### 3. Device Lock via Voice

```kotlin
val deviceLockManager = DeviceLockManager(this)

// Check if available
if (deviceLockManager.isDeviceAdminEnabled()) {
    deviceLockManager.lockDevice()  // Lock immediately
}
```

### 4. Voice Recognition

```kotlin
val voiceEngine = VoiceRecognitionEngine(this) { text, language ->
    println("Recognized ($language): $text")
}

// Start listening (blocks until speech detected)
voiceEngine.startRecognition("en")  // English
// ... listening ...
voiceEngine.stopRecognition()

// Supported languages: en, hi, bn, ta, te, mr, gu, kn, ml, pa, ur, fr, de, es
```

### 5. Chat with AI

```kotlin
val chatManager = ChatManager(this)

launchIO {
    val response = chatManager.sendMessageToAI(
        "What's the weather like?",
        userProfile
    )
    println("AI: $response")
}

// Get chat history
val history = chatManager.getChatHistory()
history.forEach { message ->
    println("${message.sender}: ${message.message}")
}
```

### 6. Weather & Time

```kotlin
val weatherProvider = WeatherTimeProvider(this)

// Get time
val time = weatherProvider.getCurrentTime()        // "14:30:45"
val date = weatherProvider.getCurrentDate()        // "Friday, January 09, 2026"
val detailed = weatherProvider.getDetailedTime()   // "It's 2:30 PM on Friday..."

// Get weather
launchIO {
    val weather = weatherProvider.getWeather(0.0, 0.0)  // Auto-detect location
    println(weather)  // "The weather is sunny, 28°C..."
}

// Get forecast
val forecast = weatherProvider.getWeatherForecast(3)
```

### 7. Check Device Permissions

```kotlin
val deviceAccess = DeviceAccessManager(this)

// Check specific permission
if (deviceAccess.hasPermission(DeviceAccessManager.PermissionGroup.CAMERA)) {
    println("Camera available")
}

// Get all permissions
val accessStatus = deviceAccess.getAccessStatus()
accountStatus.forEach { (permission, granted) ->
    println("$permission: ${if (granted) "✓" else "✗"}")
}

// Missing permissions
val missing = deviceAccess.getMissingPermissions()
println("Missing: $missing")
```

---

## Setup Steps

### 1. Add Permissions (Already in AndroidManifest.xml)

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.CALL_PHONE" />
<uses-permission android:name="android.permission.SEND_SMS" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
<!-- ... 40+ more permissions ... -->
```

### 2. Add Dependencies (build.gradle.kts)

```kotlin
dependencies {
    // Camera
    implementation("androidx.camera:camera-core:1.3.0")
    implementation("androidx.camera:camera-camera2:1.3.0")
    implementation("androidx.camera:camera-lifecycle:1.3.0")
    implementation("androidx.camera:camera-view:1.3.0")

    // MediaPipe Hand Landmarker
    implementation("com.google.mediapipe:tasks-vision:0.10.0")

    // TensorFlow Lite (for AI models)
    implementation("org.tensorflow:tensorflow-lite:2.13.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
```

### 3. Download AI Models

```bash
# Run model downloader
./scripts/download-models.sh

# Downloads:
# - hand_landmarker.task (Google MediaPipe)
# - whisper-base.en (Whisper STT)
# - llama-13b-q4.gguf (LLM for chat)
# - clip-vit-base-patch32 (Vision model)
```

### 4. Request Runtime Permissions

```kotlin
val permissionManager = PermissionManager(this)
if (!permissionManager.areCorePermissionsGranted()) {
    permissionManager.requestCorePermissions(this) { granted, denied ->
        Log.d("Permissions", "Granted: $granted")
        Log.d("Permissions", "Denied: $denied")
    }
}
```

---

## Device Admin Setup (For Voice Lock)

### Step 1: Create Device Admin Receiver

```xml
<!-- AndroidManifest.xml -->
<receiver
    android:name=".security.DavidDeviceAdminReceiver"
    android:exported="true"
    android:permission="android.permission.BIND_DEVICE_ADMIN">
    <meta-data
        android:name="android.app.device_admin"
        android:resource="@xml/device_admin" />
    <intent-filter>
        <action android:name="android.app.action.DEVICE_ADMIN_ENABLED" />
    </intent-filter>
</receiver>
```

### Step 2: Create Device Admin Resource

```xml
<!-- res/xml/device_admin.xml -->
<?xml version="1.0" encoding="utf-8"?>
<device-admin xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-policies>
        <force-lock />
    </uses-policies>
</device-admin>
```

### Step 3: Request Device Admin

```kotlin
val devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
val adminComponent = ComponentName(this, DavidDeviceAdminReceiver::class.java)

if (!devicePolicyManager.isAdminActive(adminComponent)) {
    val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
    intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Enable for voice lock")
    startActivity(intent)
}
```

---

## Troubleshooting

### Gesture Not Detected

```kotlin
// Check camera permission
if (!hasCameraPermission()) {
    requestCameraPermission()
}

// Verify MediaPipe model loaded
val stats = gestureController.getGestureStats()
Log.d("Gesture", stats.toString())
```

### Voice Not Recognized

```kotlin
// Check microphone permission
if (!hasAudioPermission()) {
    requestAudioPermission()
}

// Verify Whisper model
System.loadLibrary("whisper")
```

### Pointer Not Showing

```kotlin
// Check overlay permission (Android 6+)
if (!Settings.canDrawOverlays(this)) {
    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
    startActivity(intent)
}
```

### Device Lock Not Working

```kotlin
// Verify device admin is enabled
val deviceLockManager = DeviceLockManager(this)
if (!deviceLockManager.isDeviceAdminEnabled()) {
    // Trigger device admin request
    val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
    startActivity(intent)
}
```

---

## Performance Optimization

### 1. Gesture Detection

```kotlin
// Reduce analysis frequency
imageAnalysis.setAnalyzer(
    context.mainExecutor,
    GestureAnalyzer { /* ... */ }
)  // Processes every frame by default

// For better performance, sample every Nth frame
```

### 2. Model Size

Use smaller models for better performance:

```kotlin
// Low-end devices
llmModel = "llama-7b-q8"      // ~4GB RAM
whisperModel = "whisper-tiny"  // ~39MB

// Mid-range devices
llmModel = "llama-13b-q4"      // ~8GB RAM
whisperModel = "whisper-base"  // ~140MB

// High-end devices
llmModel = "llama-70b-q2"      // ~16GB RAM
whisperModel = "whisper-large" // ~3GB
```

### 3. Camera Processing

```kotlin
// Reduce resolution for faster processing
imageAnalysis = ImageAnalysis.Builder()
    .setTargetResolution(Size(640, 480))  // Instead of 1080p
    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
    .build()
```

---

## Security Considerations

1. **Device Admin** - Only used for voice lock, user controls when enabled
2. **SMS Permission** - Requires explicit user confirmation before sending
3. **Audio Recording** - Only when user activates via wake word
4. **Camera Access** - Only for gesture recognition, no cloud upload
5. **Offline Processing** - All AI processing local, no cloud data

---

## Next Steps

1. ✅ Gesture recognition working
2. ✅ Voice commands operational
3. ✅ Device lock via voice
4. ✅ Pointer control
5. ⏳ Advanced hand tracking (coming soon)
6. ⏳ Full-device control pointer (coming soon)
7. ⏳ Custom gesture training (coming soon)

---

## Related Documentation

- [Voice Guide](VOICE_GUIDE.md) - Speech-to-text and TTS
- [Device Control Guide](DEVICE_CONTROL.md) - 20+ device commands
- [Security Guide](ENCRYPTION.md) - Data encryption and biometrics
- [Backend Integration](BACKEND.md) - Cloud sync and authentication

---

**Last Updated:** January 9, 2026  
**Status:** ✅ Fully Implemented  
**Tested On:** Android 6.0 - 14.0
