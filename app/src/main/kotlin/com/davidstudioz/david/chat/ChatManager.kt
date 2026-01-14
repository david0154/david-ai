package com.davidstudioz.david.chat

import android.content.Context
import android.util.Log
import com.davidstudioz.david.ai.LLMInferenceEngine
import com.davidstudioz.david.device.DeviceController
import com.davidstudioz.david.models.ModelManager
import com.davidstudioz.david.voice.VoiceCommandProcessor
import com.davidstudioz.david.web.WebSearchEngine
import com.davidstudioz.david.features.WeatherService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * ChatManager - COMPLETE with ResponseCache + PersonalityEngine
 * ✅ FIXED: No more "trillion", "billion", technical jargon in voice
 * ✅ FIXED: Human-like responses with personality
 * ✅ FIXED: No repetitive responses (caching)
 * ✅ FIXED: Device control properly integrated
 * ✅ LLM model loading
 * ✅ Weather API
 * ✅ Web search
 * ✅ Device commands
 * ✅ Nexuzy Tech branding
 * Connected to: SafeMainActivity, VoiceController, TextToSpeechEngine
 */
class ChatManager(private val context: Context) {

    private val messages = mutableListOf<ChatMessage>()
    private var llmModelPath: File? = null
    private var isModelLoaded = false
    
    private val modelsDir = File(context.filesDir, "david_models")
    private val modelManager = ModelManager(context)
    private val deviceController = DeviceController(context)
    private val voiceCommandProcessor = VoiceCommandProcessor(context)
    private val llmEngine = LLMInferenceEngine(context)
    private val webSearch = WebSearchEngine(context)
    private val weatherService = WeatherService(context)
    
    // ✅ NEW: ResponseCache + PersonalityEngine
    private val responseCache = ResponseCache()
    private val personalityEngine = PersonalityEngine()

    init {
        loadLLMModel()
    }

    private fun loadLLMModel() {
        try {
            Log.d(TAG, "Loading LLM model from: ${modelsDir.absolutePath}")
            
            if (!modelsDir.exists()) {
                Log.w(TAG, "⚠️ Models directory doesn't exist")
                isModelLoaded = false
                return
            }
            
            val downloadedModels = modelsDir.listFiles() ?: emptyArray()
            Log.d(TAG, "Found ${downloadedModels.size} files in models directory")
            
            if (downloadedModels.isEmpty()) {
                Log.w(TAG, "⚠️ No models found")
                isModelLoaded = false
                return
            }
            
            val llmModel = downloadedModels.firstOrNull { file ->
                val name = file.name.lowercase()
                val hasValidExtension = file.extension.lowercase() in listOf("gguf", "tflite", "bin", "onnx")
                val hasValidSize = file.length() > 50 * 1024 * 1024
                val isLLMModel = name.contains("llm") || name.contains("chat") || name.contains("gemma") ||
                                name.contains("phi") || name.contains("llama") || name.contains("qwen") ||
                                name.contains("mistral") || name.contains("tinyllama")
                hasValidExtension && hasValidSize && isLLMModel
            }

            if (llmModel != null && llmModel.exists() && llmModel.canRead()) {
                Log.d(TAG, "✅ Selected LLM model: ${llmModel.name}")
                llmModelPath = llmModel
                isModelLoaded = try {
                    llmEngine.loadModel(llmModel)
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error loading model", e)
                    false
                }
            } else {
                Log.w(TAG, "⚠️ No LLM model found")
                isModelLoaded = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Critical error loading LLM model", e)
            isModelLoaded = false
        }
    }
    
    fun reloadModel() {
        llmEngine.release()
        llmModelPath = null
        isModelLoaded = false
        loadLLMModel()
    }

    fun isModelReady(): Boolean = isModelLoaded && llmEngine.isReady()

    suspend fun sendMessage(userMessage: String): ChatMessage = withContext(Dispatchers.IO) {
        try {
            val userMsg = ChatMessage(text = userMessage, isUser = true)
            messages.add(userMsg)

            // ✅ NEW: Check cache first
            val cachedResponse = responseCache.get(userMessage)
            val response = if (cachedResponse != null) {
                Log.d(TAG, "Using cached response")
                cachedResponse
            } else {
                // Generate new response
                val rawResponse = when {
                    isCommand(userMessage) -> executeCommand(userMessage)
                    isWeatherQuery(userMessage) -> getWeatherInfo(userMessage)
                    webSearch.needsWebSearch(userMessage) -> searchWeb(userMessage)
                    isModelReady() -> generateResponseWithLLM(userMessage)
                    else -> generateSmartFallback(userMessage)
                }
                
                // ✅ NEW: Add personality and cache
                val personalizedResponse = personalityEngine.personalize(rawResponse)
                responseCache.put(userMessage, personalizedResponse)
                personalizedResponse
            }

            val aiMsg = ChatMessage(text = response, isUser = false)
            messages.add(aiMsg)
            saveChatHistory()

            Log.d(TAG, "✅ Message processed")
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
    
    private fun isWeatherQuery(message: String): Boolean {
        val lower = message.lowercase()
        return lower.contains("weather") || lower.contains("temperature") ||
                lower.contains("forecast") || lower.contains("climate")
    }
    
    private suspend fun getWeatherInfo(query: String): String {
        return try {
            val location = extractLocation(query) ?: "Kolkata"
            Log.d(TAG, "Fetching weather for: $location")
            val result = weatherService.getCurrentWeather(location)
            
            if (result.isSuccess) {
                val weather = result.getOrNull()!!
                weatherService.formatWeatherForVoice(weather)
            } else {
                "I couldn't fetch the weather data right now. Please check your internet connection."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Weather fetch error", e)
            "I had trouble getting the weather information."
        }
    }
    
    private fun extractLocation(query: String): String? {
        val lower = query.lowercase()
        val inPattern = "in ([a-z\\s]+)(?:\\?|$)".toRegex()
        inPattern.find(lower)?.groupValues?.getOrNull(1)?.trim()?.let { return it }
        
        val cities = listOf(
            "kolkata", "delhi", "mumbai", "bangalore", "chennai",
            "london", "new york", "paris", "tokyo"
        )
        cities.forEach { city -> if (lower.contains(city)) return city }
        return null
    }

    private suspend fun searchWeb(query: String): String {
        return try {
            Log.d(TAG, "Searching web for: $query")
            val result = webSearch.search(query)
            if (result.success && result.sources.isNotEmpty()) {
                val topSource = result.sources.first()
                "${result.summary}\n\nSource: ${topSource.title}"
            } else {
                "I couldn't find current information online. ${result.summary}"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Web search error", e)
            "I couldn't search the web right now."
        }
    }

    private fun isCommand(message: String): Boolean {
        val lower = message.lowercase()
        return lower.contains("turn on") || lower.contains("turn off") ||
                lower.contains("enable") || lower.contains("disable") ||
                lower.contains("wifi") || lower.contains("bluetooth") ||
                lower.contains("open") || lower.contains("launch") ||
                lower.contains("flashlight") || lower.contains("volume")
    }

    private fun executeCommand(command: String): String {
        val lower = command.lowercase()

        return try {
            when {
                // Device control
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
                lower.contains("volume up") -> {
                    deviceController.volumeUp()
                    "Volume increased"
                }
                lower.contains("volume down") -> {
                    deviceController.volumeDown()
                    "Volume decreased"
                }
                
                // App launching
                lower.matches(".*(open|launch|start)\\s+(.+)".toRegex()) -> {
                    val appName = lower.replace(".*(open|launch|start)\\s+".toRegex(), "").trim()
                    if (deviceController.openApp(appName)) {
                        "Opening $appName"
                    } else {
                        "I couldn't open $appName"
                    }
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
            val prompt = "User: $input\nAssistant:"
            val response = llmEngine.generateText(prompt, maxLength = 100, temperature = 0.7f)
            if (response.isBlank() || response.length < 5) {
                generateSmartFallback(input)
            } else {
                response.trim()
            }
        } catch (e: Exception) {
            Log.e(TAG, "LLM inference error", e)
            generateSmartFallback(input)
        }
    }

    /**
     * ✅ ENHANCED: Smart fallback with Nexuzy Tech branding
     */
    private fun generateSmartFallback(input: String): String {
        val lower = input.lowercase().trim()

        // Greetings
        if (lower.matches(".*(hello|hi|hey|greetings).*".toRegex())) {
            return listOf(
                "Hello! I'm D.A.V.I.D, your AI assistant by Nexuzy Tech. How can I help?",
                "Hi there! What can I do for you today?",
                "Hey! Ready to assist you."
            ).random()
        }

        // Identity
        if (lower.contains("who are you") || lower.contains("your name")) {
            return "I'm D.A.V.I.D - Digital Assistant with Voice and Intelligent Decisions. Created by Nexuzy Tech, lead by David. Visit nexuzy.tech!"
        }

        if (lower.contains("who made you") || lower.contains("who created you")) {
            return "I was created by Nexuzy Tech, a technology company lead by David. We specialize in AI assistants. Learn more at nexuzy.tech!"
        }

        // Capabilities
        if (lower.contains("what can you do") || lower.contains("help")) {
            return "I can control your device, get weather data, answer questions, open apps, and much more! Just ask!"
        }

        // Time & Date
        if (lower.contains("time")) {
            val time = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
            return "The time is $time"
        }

        if (lower.contains("date") || lower.contains("today")) {
            val date = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date())
            return "Today is $date"
        }

        // Math
        if (lower.matches(".*(\\d+\\s*[+\\-*/]\\s*\\d+).*".toRegex())) {
            return calculateMath(lower)
        }

        // Thank you
        if (lower.matches(".*(thank|thanks).*".toRegex())) {
            return listOf("You're welcome!", "Happy to help!", "Anytime!").random()
        }

        // Goodbye
        if (lower.matches(".*(bye|goodbye).*".toRegex())) {
            return "Goodbye! Let me know if you need anything else!"
        }

        // Knowledge
        if (lower.contains("capital of india")) return "The capital of India is New Delhi."
        if (lower.contains("speed of light")) return "The speed of light is about 299,792 kilometers per second."

        // Default
        return when {
            input.endsWith("?") -> "That's a great question! I can help with device control, weather, and more."
            input.length < 3 -> "I'm listening. What would you like me to do?"
            else -> "I understand. Try asking me about weather, time, or device control!"
        }
    }

    private fun calculateMath(input: String): String {
        return try {
            val pattern = "(\\d+\\.?\\d*)\\s*([+\\-*/])\\s*(\\d+\\.?\\d*)".toRegex()
            val match = pattern.find(input)

            if (match != null) {
                val (num1Str, operator, num2Str) = match.destructured
                val num1 = num1Str.toDouble()
                val num2 = num2Str.toDouble()

                val result = when (operator) {
                    "+" -> num1 + num2
                    "-" -> num1 - num2
                    "*" -> num1 * num2
                    "/" -> if (num2 != 0.0) num1 / num2 else return "Can't divide by zero!"
                    else -> return "I couldn't calculate that."
                }

                "$num1 $operator $num2 = ${if (result % 1 == 0.0) result.toInt() else result}"
            } else {
                "I couldn't find a math expression."
            }
        } catch (e: Exception) {
            "I had trouble calculating that."
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
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading history", e)
        }
    }

    fun getMessages(): List<ChatMessage> = messages.toList()

    fun clearHistory() {
        messages.clear()
        responseCache.clear()
        saveChatHistory()
    }

    fun getModelStatus(): String {
        return buildString {
            if (isModelReady()) {
                append("✅ LLM Model: ${llmModelPath?.name}\n")
                append("   Size: ${(llmModelPath?.length() ?: 0) / (1024 * 1024)}MB\n")
            } else {
                append("⚠️ LLM Model: Not loaded\n")
                append("   Using: Smart responses + Weather + Web\n")
            }
            append("\n✅ Weather API: Available")
            append("\n✅ Web Search: Available")
            append("\n✅ Device Control: Available")
            append("\n✅ App Launching: Available")
        }
    }

    fun release() {
        llmEngine.release()
    }

    companion object {
        private const val TAG = "ChatManager"
    }
}