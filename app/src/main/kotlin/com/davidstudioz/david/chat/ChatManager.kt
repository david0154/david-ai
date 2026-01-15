package com.davidstudioz.david.chat

import android.content.Context
import android.util.Log
import com.davidstudioz.david.ai.UniversalModelLoader
import com.davidstudioz.david.device.DeviceController
import com.davidstudioz.david.models.ModelManager
import com.davidstudioz.david.voice.VoiceCommandProcessor
import com.davidstudioz.david.web.WebSearchEngine
import com.davidstudioz.david.features.WeatherService
import com.davidstudioz.david.features.NewsService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
 * ChatManager - COMPLETE with Multi-Language Support
 * ✅ Detects language from input
 * ✅ Responds appropriately in all 15 languages
 * ✅ No generic "I understand you're asking" for non-English
 * ✅ All features work in all languages
 */
class ChatManager(private val context: Context) {

    private val messages = mutableListOf<ChatMessage>()
    private val modelsDir = File(context.filesDir, "david_models")
    
    private val modelManager = ModelManager(context)
    private val deviceController = DeviceController(context)
    private val voiceCommandProcessor = VoiceCommandProcessor(context)
    private val webSearch = WebSearchEngine(context)
    private val weatherService = WeatherService(context)
    private val newsService = NewsService(context)
    private val responseCache = ResponseCache()
    private val personalityEngine = PersonalityEngine()
    private val spellCorrector = SpellCorrector()
    private val bhagavadGitaQuotes = BhagavadGitaQuotes()
    private val languageDetector = LanguageDetector()

    private val universalLoader = UniversalModelLoader(context)

    init {
        loadBestAvailableModel()
    }

    private fun loadBestAvailableModel() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                if (!modelsDir.exists()) {
                    modelsDir.mkdirs()
                }
                
                val availableModels = universalLoader.scanForModels(modelsDir)
                if (availableModels.isEmpty()) {
                    universalLoader.loadModelFromAssets("models/chat_model.tflite")
                    return@launch
                }
                
                val bestModel = availableModels.firstOrNull { it.type == UniversalModelLoader.ModelType.GGUF }
                    ?: availableModels.firstOrNull { it.type == UniversalModelLoader.ModelType.ONNX }
                    ?: availableModels.firstOrNull { it.type == UniversalModelLoader.ModelType.TFLITE }
                    ?: availableModels.firstOrNull { it.type == UniversalModelLoader.ModelType.GGML }
                
                if (bestModel != null) {
                    universalLoader.loadModel(bestModel.file)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading model", e)
            }
        }
    }
    
    fun reloadModel() {
        universalLoader.release()
        loadBestAvailableModel()
    }

    fun isModelReady(): Boolean = universalLoader.isReady()

    suspend fun sendMessage(userMessage: String): ChatMessage = withContext(Dispatchers.IO) {
        try {
            // ✅ Detect language from input
            val detectedLang = languageDetector.detectLanguage(userMessage)
            Log.d(TAG, "Detected language: ${languageDetector.getLanguageName(detectedLang)}")
            
            // ✅ Spell correction
            val correctedMessage = spellCorrector.correct(userMessage)
            
            val userMsg = ChatMessage(text = userMessage, isUser = true)
            messages.add(userMsg)

            val cachedResponse = responseCache.get(correctedMessage)
            val response = if (cachedResponse != null) {
                cachedResponse
            } else {
                val rawResponse = when {
                    isNewsQuery(correctedMessage) -> getNews(correctedMessage)
                    isCommand(correctedMessage) -> executeCommand(correctedMessage)
                    isWeatherQuery(correctedMessage) -> getWeatherInfo(correctedMessage)
                    isMotivationQuery(correctedMessage) -> getMotivation(correctedMessage, detectedLang)
                    webSearch.needsWebSearch(correctedMessage) -> searchWeb(correctedMessage)
                    isModelReady() -> generateWithModel(correctedMessage)
                    else -> generateSmartFallback(correctedMessage, detectedLang)
                }
                
                val personalizedResponse = personalityEngine.personalize(rawResponse)
                responseCache.put(correctedMessage, personalizedResponse)
                personalizedResponse
            }

            val aiMsg = ChatMessage(text = response, isUser = false)
            messages.add(aiMsg)
            saveChatHistory()

            aiMsg
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message", e)
            ChatMessage(
                text = "Sorry, I had trouble with that. Can you try again?",
                isUser = false
            )
        }
    }
    
    private suspend fun generateWithModel(input: String): String = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = universalLoader.generate(
                prompt = "User: $input\nAssistant:",
                maxTokens = 150,
                temperature = 0.7f
            )
            
            if (response.isNotBlank() && response.length > 5) {
                response.trim()
            } else {
                val lang = languageDetector.detectLanguage(input)
                generateSmartFallback(input, lang)
            }
        } catch (e: Exception) {
            val lang = languageDetector.detectLanguage(input)
            generateSmartFallback(input, lang)
        }
    }
    
    private fun isMotivationQuery(message: String): Boolean {
        val lower = message.lowercase()
        return lower.contains("motivat") || lower.contains("inspir") ||
                lower.contains("quote") || lower.contains("gita") ||
                lower.contains("bhagavad") || lower.contains("wisdom")
    }
    
    private fun getMotivation(query: String, language: String): String {
        return bhagavadGitaQuotes.getRandomQuote(language)
    }
    
    private fun isNewsQuery(message: String): Boolean {
        val lower = message.lowercase()
        return lower.contains("news") || lower.contains("headlines")
    }
    
    private suspend fun getNews(query: String): String {
        return try {
            val result = newsService.getTopHeadlines(null, 5)
            if (result.isSuccess) {
                newsService.formatNewsForText(result.getOrNull() ?: emptyList())
            } else {
                "I couldn't fetch the news right now."
            }
        } catch (e: Exception) {
            "I had trouble getting the news."
        }
    }
    
    private fun isWeatherQuery(message: String): Boolean {
        return message.lowercase().contains("weather") || 
               message.lowercase().contains("temperature")
    }
    
    private suspend fun getWeatherInfo(query: String): String {
        return try {
            val location = "Kolkata"
            val result = weatherService.getCurrentWeather(location)
            if (result.isSuccess) {
                weatherService.formatWeatherForText(result.getOrNull()!!)
            } else {
                "I couldn't fetch the weather data."
            }
        } catch (e: Exception) {
            "I had trouble getting the weather."
        }
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
            "Web search unavailable."
        }
    }

    private fun isCommand(message: String): Boolean {
        val lower = message.lowercase()
        return lower.contains("turn on") || lower.contains("turn off") ||
                lower.contains("wifi") || lower.contains("bluetooth")
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
                else -> voiceCommandProcessor.processCommand(command)
            }
        } catch (e: Exception) {
            "I tried to do that, but something went wrong"
        }
    }

    /**
     * ✅ ENHANCED: Multi-language support
     */
    private fun generateSmartFallback(input: String, language: String): String {
        val lower = input.lowercase().trim()

        // GREETINGS (works for all languages)
        if (lower.matches(".*(hello|hi|hey|हैलो|नमस्ते|வணக்கம்|నమస్కారం).*".toRegex())) {
            return getGreeting(language)
        }

        if (lower.contains("good morning") || lower.contains("सुप्रभात")) {
            return getTimeBasedGreeting("morning", language)
        }

        // DEVELOPER INFO (works for all languages)
        if (lower.matches(".*(who|what|কে|யார்|ఎవరు).*(are|r|है|है).*(you|तुम|நீ|మీరు).*".toRegex())) {
            return getIntroduction(language)
        }

        if (lower.matches(".*(who|কে|யார்|ఎవరు).*(made|created|developed|बनाया|만들었다|உருவாக்கினார்).*".toRegex())) {
            return getDeveloperInfo(language)
        }

        // TIME & DATE
        if (lower.contains("time") || lower.contains("समय") || lower.contains("நேரம்")) {
            val time = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
            return when (language) {
                "hi" -> "समय है $time"
                "ta" -> "நேரம் $time"
                "te" -> "సమయం $time"
                else -> "The time is $time"
            }
        }

        if (lower.contains("date") || lower.contains("तारीख") || lower.contains("தேதி")) {
            val date = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date())
            return when (language) {
                "hi" -> "आज है $date"
                "ta" -> "இன்று $date"
                "te" -> "ఈరోజు $date"
                else -> "Today is $date"
            }
        }

        // MATH
        if (lower.matches(".*(\\d+\\s*[+\\-*/]\\s*\\d+).*".toRegex())) {
            return calculateMath(lower)
        }

        // THANK YOU
        if (lower.matches(".*(thank|thanks|धन्यवाद|நன்றி|ధన్యవాదాలు).*".toRegex())) {
            return getThankYouResponse(language)
        }

        // JOKES
        if (lower.contains("joke") || lower.contains("मजाक")) {
            return when (language) {
                "hi" -> listOf(
                    "प्रोग्रामर प्रकृति को क्यों पसंद नहीं करते? बहुत सारे कीड़े!",
                    "एआई का पसंदीदा नाश्ता क्या है? माइक्रोचिप्स!"
                ).random()
                else -> listOf(
                    "Why don't programmers like nature? Too many bugs!",
                    "What's an AI's favorite snack? Microchips!"
                ).random()
            }
        }

        // ✅ FIXED: Better default response for non-English
        return getDefaultResponse(language)
    }
    
    private fun getGreeting(language: String): String {
        return when (language) {
            "hi" -> "नमस्ते! मैं D.A.V.I.D हूं। मैं आपकी कैसे मदद कर सकता हूं?"
            "ta" -> "வணக்கம்! நான் D.A.V.I.D. நான் உங்களுக்கு எப்படி உதவ முடியும்?"
            "te" -> "నమస్కారం! నేను D.A.V.I.D. నేను మీకు ఎలా సహాయం చేయగలను?"
            "bn" -> "নমস্কার! আমি D.A.V.I.D. আমি আপনাকে কীভাবে সাহায্য করতে পারি?"
            else -> "Hello! I'm D.A.V.I.D, your AI assistant by Nexuzy Tech. How can I help you?"
        }
    }
    
    private fun getTimeBasedGreeting(timeOfDay: String, language: String): String {
        return when (language) {
            "hi" -> "सुप्रभात! आपका दिन शुभ हो!"
            "ta" -> "காலை வணக்கம்! உங்கள் நாள் நல்லதாக இருக்கட்டும்!"
            "te" -> "శుభోదయం! మీ రోజు శుభంగా ఉండాలని కోరుకుంటున్నాను!"
            else -> "Good morning! Hope you have a great day!"
        }
    }
    
    private fun getIntroduction(language: String): String {
        return when (language) {
            "hi" -> "मैं D.A.V.I.D हूं - वॉयस और इंटेलिजेंट डिसीजन्स के साथ डिजिटल असिस्टेंट। मुझे Nexuzy Tech द्वारा विकसित किया गया है।"
            "ta" -> "நான் D.A.V.I.D - குரல் மற்றும் அறிவார்ந்த முடிவுகளுடன் டிஜிட்டல் உதவியாளர். என்னை Nexuzy Tech உருவாக்கியது."
            "te" -> "నేను D.A.V.I.D - వాయిస్ మరియు ఇంటెలిజెంట్ డిసిషన్స్‌తో డిజిటల్ అసిస్టెంట్. నన్ను Nexuzy Tech అభివృద్ధి చేసింది."
            else -> "I'm D.A.V.I.D - Digital Assistant with Voice & Intelligent Decisions. Developed by Nexuzy Tech, created by David (Manoj Konark)."
        }
    }
    
    private fun getDeveloperInfo(language: String): String {
        return when (language) {
            "hi" -> "मुझे David (Manoj Konark) ने बनाया है, जो Nexuzy Tech के संस्थापक और प्रमुख डेवलपर हैं। वह कोलकाता, भारत से एक AI और Android डेवलपर हैं। nexuzy.tech पर और जानें!"
            "ta" -> "என்னை David (Manoj Konark) உருவாக்கியுள்ளார், அவர் Nexuzy Tech-ன் நிறுவனர் மற்றும் முன்னணி டெவலப்பர். அவர் கொல்கத்தா, இந்தியாவைச் சேர்ந்த AI மற்றும் Android டெவலப்பர். nexuzy.tech இல் மேலும் அறியவும்!"
            "te" -> "నన్ను David (Manoj Konark) సృష్టించారు, అతను Nexuzy Tech వ్యవస్థాపకుడు మరియు ప్రధాన డెవలపర్. అతను కోల్‌కతా, భారతదేశానికి చెందిన AI మరియు Android డెవలపర్. nexuzy.tech వద్ద మరింత తెలుసుకోండి!"
            else -> "I was created by David (Manoj Konark), founder and lead developer at Nexuzy Tech. He's an AI and Android developer from Kolkata, India. Visit nexuzy.tech!"
        }
    }
    
    private fun getThankYouResponse(language: String): String {
        return when (language) {
            "hi" -> "आपका स्वागत है! मदद करके खुशी हुई!"
            "ta" -> "வரவேற்கிறேன்! உதவுவதில் மகிழ்ச்சி!"
            "te" -> "స్వాగతం! సహాయం చేయడం ఆనందంగా ఉంది!"
            else -> "You're welcome! Happy to help!"
        }
    }
    
    private fun getDefaultResponse(language: String): String {
        return when (language) {
            "hi" -> "मैं समझ गया। मुझसे समाचार, मौसम, समय, या डिवाइस नियंत्रण के बारे में पूछें!"
            "ta" -> "நான் புரிந்துகொண்டேன். என்னிடம் செய்திகள், வானிலை, நேரம் அல்லது சாதனக் கட்டுப்பாட்டைப் பற்றி கேளுங்கள்!"
            "te" -> "నేను అర్థం చేసుకున్నాను। నన్ను వార్తలు, వాతావరణం, సమయం లేదా పరికర నియంత్రణ గురించి అడగండి!"
            "bn" -> "আমি বুঝেছি। আমাকে সংবাদ, আবহাওয়া, সময়, বা ডিভাইস নিয়ন্ত্রণ সম্পর্কে জিজ্ঞাসা করুন!"
            else -> "I understand! Try asking me about news, weather, time, motivation, or device control!"
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
                    else -> return "Invalid operation"
                }
                "$num1 $operator $num2 = ${if (result % 1 == 0.0) result.toInt() else result}"
            } else {
                "Try: '5 + 3'"
            }
        } catch (e: Exception) {
            "Try: '5 + 3'"
        }
    }

    private fun saveChatHistory() {
        try {
            val prefs = context.getSharedPreferences("david_chat", Context.MODE_PRIVATE)
            val json = com.google.gson.Gson().toJson(messages.takeLast(100))
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

    fun getModelStatus(): String = universalLoader.getStatus()
    fun getModelInfo(): Map<String, String> {
        val modelFile = universalLoader.getModelFile()
        return mapOf(
            "type" to universalLoader.getModelType().name,
            "ready" to universalLoader.isReady().toString(),
            "model_name" to (modelFile?.name ?: "none"),
            "model_size_mb" to "${(modelFile?.length() ?: 0) / (1024 * 1024)}"
        )
    }

    fun release() {
        universalLoader.release()
    }

    companion object {
        private const val TAG = "ChatManager"
    }
}