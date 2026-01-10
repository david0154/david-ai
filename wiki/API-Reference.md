# API Reference

**D.A.V.I.D AI Developer API Documentation**

---

## 📋 Table of Contents

1. [Overview](#overview)
2. [Voice Control API](#voice-control-api)
3. [Gesture Recognition API](#gesture-recognition-api)
4. [Chat AI API](#chat-ai-api)
5. [Device Control API](#device-control-api)
6. [Language API](#language-api)
7. [Model Management API](#model-management-api)

---

## Overview

**D.A.V.I.D AI provides programmatic access to all AI features.**

### Base Package

```kotlin
package com.nexuzy.david
```

### Initialization

```kotlin
class DavidAI(private val context: Context) {
    init {
        initializeModels()
    }
    
    suspend fun initialize() {
        ModelManager.loadAllModels()
    }
}
```

---

## Voice Control API

### VoiceController

**Process voice commands and control device**

#### Class Definition

```kotlin
class VoiceController(private val context: Context)
```

#### Methods

##### startListening()

**Start voice recognition**

```kotlin
suspend fun startListening(
    onResult: (String) -> Unit,
    onError: (Exception) -> Unit
)
```

**Parameters:**
- `onResult`: Callback with transcribed text
- `onError`: Error callback

**Example:**

```kotlin
val voiceController = VoiceController(context)

voiceController.startListening(
    onResult = { transcription ->
        println("User said: $transcription")
    },
    onError = { error ->
        println("Error: ${error.message}")
    }
)
```

##### processCommand()

**Process voice command**

```kotlin
suspend fun processCommand(audioData: ByteArray): CommandResult
```

**Parameters:**
- `audioData`: Raw audio bytes (16kHz, 16-bit, mono)

**Returns:** `CommandResult`

```kotlin
data class CommandResult(
    val transcription: String,
    val action: DeviceAction,
    val success: Boolean,
    val message: String
)
```

**Example:**

```kotlin
val audioData = recordAudio()
val result = voiceController.processCommand(audioData)

if (result.success) {
    println("Executed: ${result.action}")
}
```

##### stopListening()

**Stop voice recognition**

```kotlin
fun stopListening()
```

---

## Gesture Recognition API

### GestureController

**Recognize hand gestures from camera**

#### Class Definition

```kotlin
class GestureController(private val context: Context)
```

#### Methods

##### startRecognition()

**Start gesture recognition**

```kotlin
fun startRecognition(
    onGesture: (Gesture) -> Unit,
    onHandPosition: (Point) -> Unit
)
```

**Parameters:**
- `onGesture`: Callback with recognized gesture
- `onHandPosition`: Callback with hand position

**Gesture Types:**

```kotlin
enum class Gesture {
    OPEN_PALM,      // Show pointer
    CLOSED_FIST,    // Hide pointer
    POINTING_UP,    // Move pointer
    VICTORY,        // Click
    THUMBS_UP,      // Confirm
    THUMBS_DOWN,    // Cancel
    UNKNOWN
}
```

**Example:**

```kotlin
val gestureController = GestureController(context)

gestureController.startRecognition(
    onGesture = { gesture ->
        when (gesture) {
            Gesture.VICTORY -> performClick()
            Gesture.POINTING_UP -> movePointer()
            else -> {}
        }
    },
    onHandPosition = { position ->
        updatePointer(position.x, position.y)
    }
)
```

##### stopRecognition()

**Stop gesture recognition**

```kotlin
fun stopRecognition()
```

##### getHandLandmarks()

**Get 21 hand landmark points**

```kotlin
fun getHandLandmarks(image: Bitmap): List<Point>?
```

**Returns:** List of 21 points or null if no hand detected

---

## Chat AI API

### ChatController

**Interact with on-device AI chatbot**

#### Class Definition

```kotlin
class ChatController(private val context: Context)
```

#### Methods

##### sendMessage()

**Send message to AI**

```kotlin
suspend fun sendMessage(
    message: String,
    conversationId: String? = null
): ChatResponse
```

**Parameters:**
- `message`: User message
- `conversationId`: Optional conversation ID for context

**Returns:**

```kotlin
data class ChatResponse(
    val response: String,
    val conversationId: String,
    val timestamp: Long,
    val model: String
)
```

**Example:**

```kotlin
val chatController = ChatController(context)

val response = chatController.sendMessage(
    message = "What's the weather like?"
)

println("AI: ${response.response}")
```

##### getConversationHistory()

**Get chat history**

```kotlin
suspend fun getConversationHistory(
    conversationId: String
): List<ChatMessage>
```

**Returns:**

```kotlin
data class ChatMessage(
    val id: Long,
    val content: String,
    val isUser: Boolean,
    val timestamp: Long
)
```

##### clearHistory()

**Clear chat history**

```kotlin
suspend fun clearHistory(conversationId: String)
```

---

## Device Control API

### DeviceController

**Control device settings and features**

#### Class Definition

```kotlin
class DeviceController(private val context: Context)
```

#### Methods

##### toggleWiFi()

**Turn WiFi on/off**

```kotlin
fun toggleWiFi(enable: Boolean): Boolean
```

**Returns:** Success status

**Example:**

```kotlin
val deviceController = DeviceController(context)

val success = deviceController.toggleWiFi(true)
if (success) {
    println("WiFi enabled")
}
```

##### toggleBluetooth()

**Turn Bluetooth on/off**

```kotlin
fun toggleBluetooth(enable: Boolean): Boolean
```

##### toggleFlashlight()

**Turn flashlight on/off**

```kotlin
fun toggleFlashlight(enable: Boolean): Boolean
```

##### setVolume()

**Set device volume**

```kotlin
fun setVolume(level: Int, stream: Int = AudioManager.STREAM_MUSIC): Boolean
```

**Parameters:**
- `level`: Volume level (0-100)
- `stream`: Audio stream type

##### openApp()

**Open application by package name**

```kotlin
fun openApp(packageName: String): Boolean
```

**Example:**

```kotlin
deviceController.openApp("com.android.settings")
```

##### takeScreenshot()

**Take screenshot**

```kotlin
fun takeScreenshot(): Bitmap?
```

**Returns:** Screenshot bitmap or null

---

## Language API

### LanguageManager

**Manage multi-language support**

#### Supported Languages

```kotlin
enum class SupportedLanguage(val code: String, val nativeName: String) {
    ENGLISH("en", "English"),
    HINDI("hi", "हिन्दी"),
    TAMIL("ta", "தமிழ்"),
    TELUGU("te", "తెలుగు"),
    BENGALI("bn", "বাংলা"),
    MARATHI("mr", "मराठी"),
    GUJARATI("gu", "ગુજરાતી"),
    KANNADA("kn", "ಕನ್ನಡ"),
    MALAYALAM("ml", "മലയാളം"),
    PUNJABI("pa", "ਪੰਜਾਬੀ"),
    ODIA("or", "ଓଡ଼ିଆ"),
    URDU("ur", "اردو"),
    SANSKRIT("sa", "संस्कृतम्"),
    KASHMIRI("ks", "कॉशुर"),
    ASSAMESE("as", "অসমীয়া")
}
```

#### Methods

##### setLanguage()

**Set app language**

```kotlin
fun setLanguage(language: SupportedLanguage)
```

##### getCurrentLanguage()

**Get current language**

```kotlin
fun getCurrentLanguage(): SupportedLanguage
```

##### translate()

**Translate text**

```kotlin
suspend fun translate(
    text: String,
    targetLanguage: SupportedLanguage
): String
```

**Example:**

```kotlin
val languageManager = LanguageManager(context)

languageManager.setLanguage(SupportedLanguage.HINDI)

val translated = languageManager.translate(
    text = "Hello",
    targetLanguage = SupportedLanguage.HINDI
)
// Returns: "नमस्ते"
```

---

## Model Management API

### ModelManager

**Download and manage AI models**

#### Class Definition

```kotlin
object ModelManager
```

#### Methods

##### downloadModel()

**Download specific model**

```kotlin
suspend fun downloadModel(
    modelType: ModelType,
    onProgress: (Int) -> Unit
): Result<Unit>
```

**Model Types:**

```kotlin
enum class ModelType {
    VOICE_TINY,
    VOICE_BASE,
    VOICE_SMALL,
    CHAT_TINYLLAMA,
    CHAT_QWEN,
    CHAT_PHI2,
    VISION_MOBILENET,
    VISION_RESNET,
    GESTURE_HAND,
    GESTURE_RECOGNIZER
}
```

**Example:**

```kotlin
ModelManager.downloadModel(
    modelType = ModelType.VOICE_BASE,
    onProgress = { progress ->
        println("Download: $progress%")
    }
)
```

##### loadModel()

**Load model into memory**

```kotlin
suspend fun loadModel(modelType: ModelType): Boolean
```

##### unloadModel()

**Unload model from memory**

```kotlin
fun unloadModel(modelType: ModelType)
```

##### isModelAvailable()

**Check if model is downloaded**

```kotlin
fun isModelAvailable(modelType: ModelType): Boolean
```

##### getModelInfo()

**Get model information**

```kotlin
fun getModelInfo(modelType: ModelType): ModelInfo
```

**Returns:**

```kotlin
data class ModelInfo(
    val name: String,
    val size: Long,
    val version: String,
    val isDownloaded: Boolean,
    val isLoaded: Boolean
)
```

---

## Exception Handling

### Custom Exceptions

```kotlin
// Model not found
class ModelNotFoundException(message: String) : Exception(message)

// Model not loaded
class ModelNotLoadedException(message: String) : Exception(message)

// Permission denied
class PermissionDeniedException(message: String) : Exception(message)

// Feature not supported
class FeatureNotSupportedException(message: String) : Exception(message)
```

### Error Handling Example

```kotlin
try {
    val result = voiceController.processCommand(audioData)
} catch (e: ModelNotLoadedException) {
    // Download and load model
    ModelManager.downloadModel(ModelType.VOICE_BASE)
} catch (e: PermissionDeniedException) {
    // Request permission
    requestMicrophonePermission()
}
```

---

## Callback Interfaces

### VoiceRecognitionListener

```kotlin
interface VoiceRecognitionListener {
    fun onTranscription(text: String)
    fun onError(error: Exception)
    fun onVolumeChanged(volume: Float)
}
```

### GestureListener

```kotlin
interface GestureListener {
    fun onGestureDetected(gesture: Gesture)
    fun onHandPositionChanged(x: Float, y: Float)
    fun onHandLost()
}
```

### ModelDownloadListener

```kotlin
interface ModelDownloadListener {
    fun onProgress(progress: Int)
    fun onComplete()
    fun onError(error: Exception)
}
```

---

## Permissions

### Required Permissions

```xml
<!-- Microphone for voice -->
<uses-permission android:name="android.permission.RECORD_AUDIO" />

<!-- Camera for gesture -->
<uses-permission android:name="android.permission.CAMERA" />

<!-- Storage for models -->
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />

<!-- Device control -->
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />

<!-- Overlay for gestures -->
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
```

### Runtime Permission Check

```kotlin
fun checkPermissions(): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED
}
```

---

## Code Examples

### Complete Voice Command Flow

```kotlin
class MainActivity : AppCompatActivity() {
    private lateinit var voiceController: VoiceController
    private lateinit var deviceController: DeviceController
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        voiceController = VoiceController(this)
        deviceController = DeviceController(this)
        
        // Check permissions
        if (!checkPermissions()) {
            requestPermissions()
            return
        }
        
        // Start listening
        lifecycleScope.launch {
            voiceController.startListening(
                onResult = { transcription ->
                    handleCommand(transcription)
                },
                onError = { error ->
                    showError(error.message)
                }
            )
        }
    }
    
    private fun handleCommand(command: String) {
        when {
            command.contains("wifi on") -> {
                deviceController.toggleWiFi(true)
            }
            command.contains("bluetooth on") -> {
                deviceController.toggleBluetooth(true)
            }
            command.contains("flashlight on") -> {
                deviceController.toggleFlashlight(true)
            }
        }
    }
}
```

### Complete Gesture Control Flow

```kotlin
class GestureActivity : AppCompatActivity() {
    private lateinit var gestureController: GestureController
    private lateinit var overlayView: GestureOverlayView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        gestureController = GestureController(this)
        overlayView = GestureOverlayView(this)
        
        gestureController.startRecognition(
            onGesture = { gesture ->
                when (gesture) {
                    Gesture.OPEN_PALM -> overlayView.showPointer()
                    Gesture.CLOSED_FIST -> overlayView.hidePointer()
                    Gesture.VICTORY -> overlayView.performClick()
                    else -> {}
                }
            },
            onHandPosition = { position ->
                overlayView.updatePointer(position.x, position.y)
            }
        )
    }
}
```

---

## Best Practices

### 1. Initialize Models Early

```kotlin
class DavidApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        lifecycleScope.launch {
            ModelManager.loadAllModels()
        }
    }
}
```

### 2. Handle Errors Gracefully

```kotlin
try {
    val result = voiceController.processCommand(audio)
} catch (e: Exception) {
    when (e) {
        is ModelNotLoadedException -> downloadModel()
        is PermissionDeniedException -> requestPermission()
        else -> showError(e.message)
    }
}
```

### 3. Release Resources

```kotlin
override fun onDestroy() {
    super.onDestroy()
    voiceController.stopListening()
    gestureController.stopRecognition()
    ModelManager.unloadAllModels()
}
```

### 4. Use Coroutines for AI Operations

```kotlin
lifecycleScope.launch {
    val response = chatController.sendMessage("Hello")
    updateUI(response)
}
```

---

## Versioning

**Current API Version:** 1.0.0

**Compatibility:**
- Android 8.0 (API 26)+
- Kotlin 1.9.0+

---

## Support

📧 Email: [david@nexuzy.in](mailto:david@nexuzy.in)  
🐛 [Report Issues](https://github.com/david0154/david-ai/issues)  
💬 [Discussions](https://github.com/david0154/david-ai/discussions)

---

**© 2026 Nexuzy Tech Ltd.**
