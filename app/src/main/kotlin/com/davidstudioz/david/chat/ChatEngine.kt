package com.davidstudioz.david.chat

import android.content.Context
import android.util.Log
import com.davidstudioz.david.ai.LLMInferenceEngine
import com.davidstudioz.david.device.DeviceController
import com.davidstudioz.david.models.ModelManager
import com.davidstudioz.david.voice.VoiceCommandProcessor
import com.davidstudioz.david.web.WebSearchEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

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
    
    // Real LLM inference engine
    private val llmEngine = LLMInferenceEngine(context)
    
    // ✅ NEW: Web search engine
    private val webSearch = WebSearchEngine(context)
    
    init {
        loadLLMModel()
    }
    
    private fun loadLLMModel() {
        try {
            val downloadedModels = modelManager.getDownloadedModels()
            val llmModel = downloadedModels.firstOrNull { file ->
                (file.name.contains("llm", ignoreCase = true) || 
                 file.name.contains("gemma", ignoreCase = true) ||
                 file.name.contains("phi", ignoreCase = true) ||
                 file.name.endsWith(".tflite")) && 
                file.length() > 1024 * 1024  // At least 1MB
            }
            
            if (llmModel != null && llmModel.exists()) {
                llmModelPath = llmModel
                isModelLoaded = llmEngine.loadModel(llmModel)
                
                if (isModelLoaded) {
                    Log.d(TAG, "✅ LLM model loaded with TensorFlow Lite: ${llmModel.name}")
                } else {
                    Log.w(TAG, "⚠️ LLM model file found but failed to load")
                }
            } else {
                Log.w(TAG, "⚠️ No LLM model downloaded - using smart fallback + web search")
                isModelLoaded = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading LLM model", e)
            isModelLoaded = false
        }
    }
    
    fun isModelReady(): Boolean {
        return isModelLoaded && llmEngine.isReady()
    }
    
    suspend fun sendMessage(userMessage: String): ChatMessage = withContext(Dispatchers.IO) {
        try {
            val userMsg = ChatMessage(text = userMessage, isUser = true)
            messages.add(userMsg)
            
            val response = if (isCommand(userMessage)) {
                executeCommand(userMessage)
            } else if (webSearch.needsWebSearch(userMessage)) {
                // ✅ Use web search for current information
                searchWeb(userMessage)
            } else if (isModelReady()) {
                // Use real LLM inference
                generateResponseWithLLM(userMessage)
            } else {
                // Fallback to smart responses
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
    
    /**
     * ✅ NEW: Search the web for current information
     */
    private suspend fun searchWeb(query: String): String {
        return try {
            Log.d(TAG, "Searching web for: $query")
            val result = webSearch.search(query)
            
            if (result.success && result.sources.isNotEmpty()) {
                // Return summary with source
                val topSource = result.sources.first()
                "${result.summary}\n\nSource: ${topSource.title}"
            } else {
                "I couldn't find current information online. ${result.summary}"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Web search error", e)
            "I couldn't search the web right now. Please check your internet connection."
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
    
    /**
     * Real LLM inference using TensorFlow Lite
     */
    private suspend fun generateResponseWithLLM(input: String): String = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "Using LLM model: ${llmModelPath?.name}")
            
            val prompt = "User: $input\nAssistant:"
            val response = llmEngine.generateText(
                prompt = prompt,
                maxLength = 100,
                temperature = 0.7f
            )
            
            if (response.isBlank() || response.length < 5) {
                Log.w(TAG, "LLM returned invalid response, using fallback")
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
     * ✅ ENHANCED: Smart fallback responses with Nexuzy Tech branding
     */
    private fun generateSmartFallback(input: String): String {
        val lower = input.lowercase().trim()
        
        // 1. GREETINGS
        if (lower.matches(".*(hello|hi|hey|greetings|sup|yo).*".toRegex())) {
            return listOf(
                "Hello! I'm D.A.V.I.D, your AI assistant by Nexuzy Tech. How can I help you?",
                "Hi there! What can I do for you today?",
                "Hey! Ready to assist you. What do you need?"
            ).random()
        }
        
        if (lower.contains("good morning")) return "Good morning! Hope you have a great day!"
        if (lower.contains("good afternoon")) return "Good afternoon! How's your day going?"
        if (lower.contains("good evening")) return "Good evening! What can I help you with?"
        if (lower.contains("good night")) return "Good night! Sleep well!"
        
        // 2. HOW ARE YOU / PERSONAL
        if (lower.matches(".*(how are you|how r u|hows it going|whats up).*".toRegex())) {
            return "I'm doing great! Thanks for asking. How can I help you today?"
        }
        
        // ✅ NEXUZY TECH BRANDING
        if (lower.contains("your name") || lower.contains("who are you")) {
            return "I'm D.A.V.I.D - Digital Assistant with Voice Integration and Device control. I was developed by Nexuzy Tech, lead by David. Visit us at nexuzy.tech!"
        }
        
        if (lower.contains("who made you") || lower.contains("who created you") || lower.contains("who developed you")) {
            return "I was created by Nexuzy Tech - a technology company lead by David. We specialize in AI assistants and innovative solutions. Learn more at nexuzy.tech!"
        }
        
        if (lower.contains("company") || lower.contains("nexuzy")) {
            return "Nexuzy Tech is the company behind D.A.V.I.D AI. We're a tech company lead by David, focused on AI, voice assistants, and innovative solutions. Visit nexuzy.tech for more info!"
        }
        
        if (lower.contains("website") || lower.contains("your site")) {
            return "You can learn more about Nexuzy Tech and our products at nexuzy.tech!"
        }
        
        if (lower.contains("developer") || lower.contains("dev team")) {
            return "D.A.V.I.D is developed by Nexuzy Tech team, lead by David. We're passionate about creating intelligent AI assistants!"
        }
        
        // 3. CAPABILITIES
        if (lower.contains("what can you do") || lower.contains("help") || lower.contains("capabilities")) {
            return "I can:\n• Control device (WiFi, Bluetooth, flashlight, volume)\n• Make calls & send messages\n• Search the web for current info\n• Check weather & time\n• Answer questions\n• Set reminders\n• Open apps\n• And much more! Just ask!"
        }
        
        // 4. TIME & DATE
        if (lower.contains("time") || lower.contains("what time")) {
            val time = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
            return "The time is $time"
        }
        
        if (lower.contains("date") || lower.contains("today") || lower.contains("what day")) {
            val date = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date())
            return "Today is $date"
        }
        
        // 5. DEVICE INFO
        if (lower.contains("battery")) {
            return "Let me check your battery status..."
        }
        
        if (lower.contains("storage") || lower.contains("space")) {
            return "Checking your device storage..."
        }
        
        // 6. WEATHER
        if (lower.contains("weather")) {
            return "Let me check the weather for you..."
        }
        
        // 7. MATH CALCULATIONS
        if (lower.matches(".*(\\d+\\s*[+\\-*/]\\s*\\d+).*".toRegex())) {
            return calculateMath(lower)
        }
        
        // 8. THANK YOU
        if (lower.matches(".*(thank|thanks|thx).*".toRegex())) {
            return listOf(
                "You're welcome!",
                "Happy to help!",
                "Anytime!",
                "My pleasure!"
            ).random()
        }
        
        // 9. GOODBYE
        if (lower.matches(".*(bye|goodbye|see you|cya).*".toRegex())) {
            return "Goodbye! Let me know if you need anything else!"
        }
        
        // 10. GENERAL KNOWLEDGE - Science
        if (lower.contains("speed of light")) return "The speed of light is approximately 299,792,458 meters per second."
        if (lower.contains("gravity")) return "Gravity is the force that attracts objects toward each other. On Earth, it's about 9.8 m/s²."
        if (lower.contains("earth") && lower.contains("sun")) return "Earth is about 93 million miles (150 million km) from the Sun."
        if (lower.contains("planets")) return "There are 8 planets in our solar system: Mercury, Venus, Earth, Mars, Jupiter, Saturn, Uranus, and Neptune."
        
        // 11. GENERAL KNOWLEDGE - Geography
        if (lower.contains("capital of india")) return "The capital of India is New Delhi."
        if (lower.contains("capital of usa") || lower.contains("capital of america")) return "The capital of the USA is Washington, D.C."
        if (lower.contains("capital of france")) return "The capital of France is Paris."
        if (lower.contains("capital of japan")) return "The capital of Japan is Tokyo."
        if (lower.contains("largest country")) return "Russia is the largest country by land area."
        if (lower.contains("smallest country")) return "Vatican City is the smallest country in the world."
        
        // 12. JOKES
        if (lower.contains("joke") || lower.contains("funny")) {
            return listOf(
                "Why don't programmers like nature? It has too many bugs!",
                "What do you call a bear with no teeth? A gummy bear!",
                "Why did the AI go to therapy? It had too many neural issues!"
            ).random()
        }
        
        // 13. COMPLIMENTS
        if (lower.contains("smart") || lower.contains("intelligent") || lower.contains("amazing")) {
            return "Thank you! I try my best to help you!"
        }
        
        // 14. QUESTIONS ABOUT AI
        if (lower.contains("what is ai") || lower.contains("artificial intelligence")) {
            return "AI (Artificial Intelligence) is technology that enables machines to learn, reason, and make decisions like humans. I'm an example of AI, created by Nexuzy Tech!"
        }
        
        // 15. YES/NO RESPONSES
        if (lower == "yes" || lower == "yeah" || lower == "yep") {
            return "Great! What would you like me to do?"
        }
        if (lower == "no" || lower == "nope" || lower == "nah") {
            return "Okay, no problem. Let me know if you need anything else!"
        }
        
        // 16. SORRY/APOLOGY
        if (lower.contains("sorry")) {
            return "No worries! How can I help you?"
        }
        
        // 17. LOVE/LIKE
        if (lower.contains("i love you") || lower.contains("love you")) {
            return "Aww, that's sweet! I'm here to help you anytime!"
        }
        
        // 18. DEFAULT SMART RESPONSES
        return when {
            input.endsWith("?") -> "That's a great question! I can help with device control, web searches, time, weather, and more. What do you need?"
            input.length < 3 -> "I'm listening. What would you like me to do?"
            lower.contains("how") -> "Let me help you with that. I can control your device, search the web, and answer questions."
            lower.contains("why") -> "That's a thoughtful question. I can search the web for current information if you need!"
            lower.contains("when") -> "I can help you check times, dates, and schedules. What specifically do you need?"
            lower.contains("where") -> "I can help with location queries. What are you looking for?"
            else -> "I understand you're asking about that. Try saying 'search for [topic]' and I'll look it up online, or ask me to control your device!"
        }
    }
    
    /**
     * Calculate basic math expressions
     */
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
                "I couldn't find a math expression. Try something like '5 + 3' or '10 * 2'"
            }
        } catch (e: Exception) {
            "I had trouble calculating that. Try a simple expression like '5 + 3'"
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
            "✅ LLM Model: Loaded (TensorFlow Lite - Advanced AI)\n✅ Web Search: Available"
        } else {
            "⚠️ LLM Model: Not loaded (Using smart responses + web search)"
        }
    }
    
    /**
     * Release resources
     */
    fun release() {
        llmEngine.release()
    }
    
    companion object {
        private const val TAG = "ChatManager"
    }
}
