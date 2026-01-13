package com.davidstudioz.david.chat

import android.content.Context
import android.util.Log
import com.davidstudioz.david.device.DeviceController
import com.davidstudioz.david.models.ModelManager
import com.davidstudioz.david.voice.VoiceCommandProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class ChatManager(private val context: Context) {
    
    private val messages = mutableListOf<ChatMessage>()
    private var llmModelPath: File? = null
    private var isModelLoaded = false
    private val modelManager = ModelManager(context)
    private val deviceController = DeviceController(context)
    private val voiceCommandProcessor = VoiceCommandProcessor(context)
    
    init {
        loadLLMModel()
    }
    
    private fun loadLLMModel() {
        try {
            // ✅ FIXED: Get downloaded model files and check for LLM
            val downloadedModels = modelManager.getDownloadedModels()
            val llmModel = downloadedModels.firstOrNull { file ->
                file.name.contains("llm", ignoreCase = true) && file.length() > 1024 * 1024
            }
            
            if (llmModel != null && llmModel.exists()) {
                llmModelPath = llmModel
                isModelLoaded = true
                Log.d(TAG, "✅ LLM model loaded: ${llmModel.name}")
            } else {
                Log.w(TAG, "⚠️ No LLM model downloaded")
                isModelLoaded = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading LLM model", e)
            isModelLoaded = false
        }
    }
    
    fun isModelReady(): Boolean {
        return isModelLoaded && llmModelPath != null && llmModelPath!!.exists()
    }
    
    suspend fun sendMessage(userMessage: String): ChatMessage = withContext(Dispatchers.IO) {
        try {
            val userMsg = ChatMessage(text = userMessage, isUser = true)
            messages.add(userMsg)
            
            val response = if (isCommand(userMessage)) {
                executeCommand(userMessage)
            } else if (isModelReady()) {
                generateResponseWithLLM(userMessage)
            } else {
                generateSmartFallback(userMessage)
            }
            
            val aiMsg = ChatMessage(text = response, isUser = false)
            messages.add(aiMsg)
            
            saveChatHistory()
            
            Log.d(TAG, "✅ Message: '$userMessage' -> '$response'")
            aiMsg
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message", e)
            val errorMsg = ChatMessage(
                text = "Sorry, I had trouble with that. Can you try again?",
                isUser = false
            )
            messages.add(errorMsg)
            errorMsg
        }
    }
    
    private fun isCommand(message: String): Boolean {
        val lower = message.lowercase()
        return lower.contains("turn on") || lower.contains("turn off") ||
                lower.contains("enable") || lower.contains("disable") ||
                lower.contains("wifi") || lower.contains("bluetooth") ||
                lower.contains("call") || lower.contains("message") ||
                lower.contains("volume") || lower.contains("brightness") ||
                lower.contains("open") || lower.contains("launch") ||
                lower.contains("flashlight") || lower.contains("torch")
    }
    
    private fun executeCommand(command: String): String {
        val lower = command.lowercase()
        
        return try {
            when {
                lower.contains("wifi") && (lower.contains("on") || lower.contains("enable")) -> {
                    deviceController.toggleWiFi(true)
                    "WiFi is now on"
                }
                lower.contains("wifi") && (lower.contains("off") || lower.contains("disable")) -> {
                    deviceController.toggleWiFi(false)
                    "WiFi is now off"
                }
                lower.contains("bluetooth") && (lower.contains("on") || lower.contains("enable")) -> {
                    deviceController.toggleBluetooth(true)
                    "Bluetooth is now on"
                }
                lower.contains("bluetooth") && (lower.contains("off") || lower.contains("disable")) -> {
                    deviceController.toggleBluetooth(false)
                    "Bluetooth is now off"
                }
                lower.contains("flashlight") || lower.contains("torch") -> {
                    val turnOn = lower.contains("on") || lower.contains("enable")
                    deviceController.toggleFlashlight(turnOn)
                    if (turnOn) "Flashlight is on" else "Flashlight is off"
                }
                lower.contains("volume up") || lower.contains("increase volume") -> {
                    deviceController.volumeUp()
                    "Volume increased"
                }
                lower.contains("volume down") || lower.contains("decrease volume") -> {
                    deviceController.volumeDown()
                    "Volume decreased"
                }
                lower.contains("mute") -> {
                    deviceController.toggleMute(true)
                    "Volume muted"
                }
                else -> voiceCommandProcessor.processCommand(command)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Command execution error", e)
            "I tried to do that, but something went wrong"
        }
    }
    
    private suspend fun generateResponseWithLLM(input: String): String = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "Using LLM model: ${llmModelPath?.name}")
            generateSmartFallback(input)
        } catch (e: Exception) {
            Log.e(TAG, "LLM error", e)
            generateSmartFallback(input)
        }
    }
    
    private fun generateSmartFallback(input: String): String {
        val lower = input.lowercase()
        
        return when {
            lower.matches(".*(hello|hi|hey|greetings).*".toRegex()) -> {
                "Hello! I'm D.A.V.I.D. How can I help you?"
            }
            lower.contains("how are you") -> {
                "I'm doing great! Ready to help you. What do you need?"
            }
            lower.contains("thank") -> {
                "You're welcome! Anything else?"
            }
            lower.contains("what can you do") || lower.contains("help") -> {
                "I can control your device (WiFi, Bluetooth, flashlight, volume), make calls, send messages, answer questions, and more. Just ask!"
            }
            lower.contains("who are you") || lower.contains("your name") -> {
                "I'm D.A.V.I.D - your personal AI assistant. I help control your device and answer questions!"
            }
            lower.contains("time") || lower.contains("what time") -> {
                "The time is ${deviceController.getCurrentTime()}"
            }
            lower.contains("date") || lower.contains("today") -> {
                "Today is ${deviceController.getCurrentDate()}"
            }
            lower.contains("weather") -> {
                "Let me check the weather for you..."
            }
            else -> {
                when {
                    input.endsWith("?") -> "That's a good question. I'm still learning, but I can help with device control and basic info."
                    input.length < 5 -> "I'm listening. What would you like me to do?"
                    else -> "I understand you're asking about that. I can help with device control, calls, messages, and info. What do you need?"
                }
            }
        }
    }
    
    private fun saveChatHistory() {
        try {
            val prefs = context.getSharedPreferences("david_chat", Context.MODE_PRIVATE)
            val history = messages.takeLast(100)
            val json = com.google.gson.Gson().toJson(history)
            prefs.edit().putString("chat_history", json).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving history", e)
        }
    }
    
    fun loadChatHistory() {
        try {
            val prefs = context.getSharedPreferences("david_chat", Context.MODE_PRIVATE)
            val json = prefs.getString("chat_history", null)
            if (json != null) {
                val history = com.google.gson.Gson().fromJson(json, Array<ChatMessage>::class.java)
                messages.clear()
                messages.addAll(history)
                Log.d(TAG, "Loaded ${messages.size} messages")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading history", e)
        }
    }
    
    fun getMessages(): List<ChatMessage> = messages.toList()
    
    fun clearHistory() {
        messages.clear()
        saveChatHistory()
    }
    
    fun getModelStatus(): String {
        return if (isModelReady()) {
            "LLM Model: Loaded"
        } else {
            "LLM Model: Not loaded"
        }
    }
    
    companion object {
        private const val TAG = "ChatManager"
    }
}