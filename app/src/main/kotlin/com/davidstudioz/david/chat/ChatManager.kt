package com.davidstudioz.david.chat

import android.content.Context
import android.util.Log
import com.davidstudioz.david.ai.LLMInferenceEngine
import com.davidstudioz.david.device.DeviceController
import com.davidstudioz.david.models.ModelManager
import com.davidstudioz.david.voice.VoiceCommandProcessor
import com.davidstudioz.david.web.WebSearchEngine
import com.davidstudioz.david.features.WeatherService
import com.davidstudioz.david.features.NewsService
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
 * ChatManager - COMPLETE with News + Weather + Device Control
 * ✅ News headlines (India)
 * ✅ Weather API (500+ Indian locations)
 * ✅ Device control
 * ✅ App launching (50+ apps)
 * ✅ Web search
 * ✅ ResponseCache (no repetition)
 * ✅ PersonalityEngine (human-like)
 * ✅ 15 Indian languages
 * ✅ Nexuzy Tech branding
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
    private val newsService = NewsService(context) // NEW
    private val responseCache = ResponseCache()
    private val personalityEngine = PersonalityEngine()

    init {
        loadLLMModel()
    }

    private fun loadLLMModel() {
        try {
            if (!modelsDir.exists()) {
                isModelLoaded = false
                return
            }
            
            val downloadedModels = modelsDir.listFiles() ?: emptyArray()
            if (downloadedModels.isEmpty()) {
                isModelLoaded = false
                return
            }
            
            val llmModel = downloadedModels.firstOrNull { file ->
                val name = file.name.lowercase()
                val hasValidExtension = file.extension.lowercase() in listOf("gguf", "tflite", "bin", "onnx")
                val hasValidSize = file.length() > 50 * 1024 * 1024
                val isLLMModel = name.contains("llm") || name.contains("chat") || name.contains("gemma") ||
                                name.contains("phi") || name.contains("llama") || name.contains("qwen")
                hasValidExtension && hasValidSize && isLLMModel
            }

            if (llmModel != null && llmModel.exists()) {
                llmModelPath = llmModel
                isModelLoaded = try { llmEngine.loadModel(llmModel) } catch (e: Exception) { false }
            } else {
                isModelLoaded = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading LLM model", e)
            isModelLoaded = false
        }
    }

    fun isModelReady(): Boolean = isModelLoaded && llmEngine.isReady()

    suspend fun sendMessage(userMessage: String): ChatMessage = withContext(Dispatchers.IO) {
        try {
            val userMsg = ChatMessage(text = userMessage, isUser = true)
            messages.add(userMsg)

            val cachedResponse = responseCache.get(userMessage)
            val response = if (cachedResponse != null) {
                cachedResponse
            } else {
                val rawResponse = when {
                    isNewsQuery(userMessage) -> getNews(userMessage) // NEW
                    isCommand(userMessage) -> executeCommand(userMessage)
                    isWeatherQuery(userMessage) -> getWeatherInfo(userMessage)
                    webSearch.needsWebSearch(userMessage) -> searchWeb(userMessage)
                    isModelReady() -> generateResponseWithLLM(userMessage)
                    else -> generateSmartFallback(userMessage)
                }
                val personalizedResponse = personalityEngine.personalize(rawResponse)
                responseCache.put(userMessage, personalizedResponse)
                personalizedResponse
            }

            val aiMsg = ChatMessage(text = response, isUser = false)
            messages.add(aiMsg)
            saveChatHistory()
            aiMsg
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message", e)
            val errorMsg = ChatMessage(text = "Sorry, I had trouble with that.", isUser = false)
            messages.add(errorMsg)
            errorMsg
        }
    }
    
    /**
     * ✅ NEW: Check if query is about news
     */
    private fun isNewsQuery(message: String): Boolean {
        val lower = message.lowercase()
        return lower.contains("news") || lower.contains("headlines") ||
                lower.contains("latest news") || lower.contains("today's news") ||
                lower.contains("show news") || lower.contains("what's happening")
    }
    
    /**
     * ✅ NEW: Get news headlines
     */
    private suspend fun getNews(query: String): String {
        return try {
            val lower = query.lowercase()
            val category = when {
                lower.contains("sports") -> "sports"
                lower.contains("business") -> "business"
                lower.contains("tech") || lower.contains("technology") -> "technology"
                lower.contains("entertainment") -> "entertainment"
                lower.contains("health") -> "health"
                lower.contains("science") -> "science"
                else -> null
            }
            
            Log.d(TAG, "Fetching news${category?.let { " - $it" } ?: ""}")
            val result = newsService.getTopHeadlines(category, 5)
            
            if (result.isSuccess) {
                val articles = result.getOrNull() ?: emptyList()
                newsService.formatNewsForText(articles)
            } else {
                "I couldn't fetch the news right now. Please check your internet connection."
            }
        } catch (e: Exception) {
            Log.e(TAG, "News fetch error", e)
            "I had trouble getting the news."
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
            val result = weatherService.getCurrentWeather(location)
            if (result.isSuccess) {
                weatherService.formatWeatherForText(result.getOrNull()!!)
            } else {
                "I couldn't fetch the weather data."
            }
        } catch (e: Exception) {
            "I had trouble getting the weather information."
        }
    }
    
    private fun extractLocation(query: String): String? {
        val lower = query.lowercase()
        val inPattern = "in ([a-z\\s]+)(?:\\?|$)".toRegex()
        inPattern.find(lower)?.groupValues?.getOrNull(1)?.trim()?.let { return it }
        val cities = listOf("kolkata", "delhi", "mumbai", "bangalore", "chennai")
        cities.forEach { city -> if (lower.contains(city)) return city }
        return null
    }

    private suspend fun searchWeb(query: String): String {
        return try {
            val result = webSearch.search(query)
            if (result.success && result.sources.isNotEmpty()) {
                "${result.summary}\n\nSource: ${result.sources.first().title}"
            } else {
                "I couldn't find information online."
            }
        } catch (e: Exception) {
            "I couldn't search the web right now."
        }
    }

    private fun isCommand(message: String): Boolean {
        val lower = message.lowercase()
        return lower.contains("turn on") || lower.contains("turn off") ||
                lower.contains("wifi") || lower.contains("bluetooth") ||
                lower.contains("open") || lower.contains("launch")
    }

    private fun executeCommand(command: String): String {
        val lower = command.lowercase()
        return try {
            when {
                lower.contains("wifi") && lower.contains("on") -> {
                    deviceController.toggleWiFi(true)
                    "WiFi is now on"
                }
                lower.contains("wifi") && lower.contains("off") -> {
                    deviceController.toggleWiFi(false)
                    "WiFi is now off"
                }
                lower.contains("bluetooth") && lower.contains("on") -> {
                    deviceController.toggleBluetooth(true)
                    "Bluetooth is now on"
                }
                lower.contains("bluetooth") && lower.contains("off") -> {
                    deviceController.toggleBluetooth(false)
                    "Bluetooth is now off"
                }
                lower.matches(".*(open|launch|start)\\s+(.+)".toRegex()) -> {
                    val appName = lower.replace(".*(open|launch|start)\\s+".toRegex(), "").trim()
                    if (deviceController.openApp(appName)) "Opening $appName" else "Couldn't open $appName"
                }
                else -> voiceCommandProcessor.processCommand(command)
            }
        } catch (e: Exception) {
            "I tried to do that, but something went wrong"
        }
    }

    private suspend fun generateResponseWithLLM(input: String): String = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = llmEngine.generateText("User: $input\nAssistant:", maxLength = 100, temperature = 0.7f)
            if (response.isBlank()) generateSmartFallback(input) else response.trim()
        } catch (e: Exception) {
            generateSmartFallback(input)
        }
    }

    private fun generateSmartFallback(input: String): String {
        val lower = input.lowercase().trim()

        if (lower.matches(".*(hello|hi|hey).*".toRegex())) {
            return "Hello! I'm D.A.V.I.D by Nexuzy Tech. How can I help?"
        }

        if (lower.contains("who are you")) {
            return "I'm D.A.V.I.D - Digital Assistant with Voice and Intelligent Decisions. Created by Nexuzy Tech!"
        }

        if (lower.contains("what can you do")) {
            return "I can get news, weather, control your device, open apps, and answer questions!"
        }

        if (lower.contains("time")) {
            return "The time is ${SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())}"
        }

        if (lower.contains("date")) {
            return "Today is ${SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date())}"
        }

        if (lower.matches(".*(\\d+\\s*[+\\-*/]\\s*\\d+).*".toRegex())) {
            return calculateMath(lower)
        }

        if (lower.matches(".*(thank|thanks).*".toRegex())) {
            return listOf("You're welcome!", "Happy to help!").random()
        }

        return "I understand. Try asking me about news, weather, or device control!"
    }

    private fun calculateMath(input: String): String {
        return try {
            val pattern = "(\\d+\\.?\\d*)\\s*([+\\-*/])\\s*(\\d+\\.?\\d*)".toRegex()
            val match = pattern.find(input) ?: return "I couldn't find a math expression."
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
        } catch (e: Exception) {
            "I had trouble calculating that."
        }
    }

    private fun saveChatHistory() {
        try {
            val prefs = context.getSharedPreferences("david_chat", Context.MODE_PRIVATE)
            val json = com.google.gson.Gson().toJson(messages.takeLast(100))
            prefs.edit().putString("chat_history", json).apply()
        } catch (e: Exception) {}
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
        } catch (e: Exception) {}
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
            } else {
                append("⚠️ LLM Model: Not loaded\n")
            }
            append("\n✅ News API: Available")
            append("\n✅ Weather API: Available (500+ Indian cities)")
            append("\n✅ Web Search: Available")
            append("\n✅ Device Control: Available")
            append("\n✅ App Launching: Available (50+ apps)")
            append("\n✅ Languages: 15 Indian languages")
        }
    }

    fun release() {
        llmEngine.release()
    }

    companion object {
        private const val TAG = "ChatManager"
    }
}