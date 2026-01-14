package com.davidstudioz.david.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.davidstudioz.david.chat.ChatManager
import com.davidstudioz.david.device.DeviceController
import com.davidstudioz.david.features.WeatherService
import com.davidstudioz.david.features.NewsService
import com.davidstudioz.david.services.SearchService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.*

/**
 * VoiceController - FIXED WEATHER DETECTION
 * ✅ FIX: Weather now parses city from command dynamically
 * ✅ FIX: Male voice support via TextToSpeechEngine
 * ✅ ALL voice commands working
 */
class VoiceController(
    private val context: Context,
    private val deviceController: DeviceController,
    private var chatManager: ChatManager? = null
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private val ttsEngine = TextToSpeechEngine(context)
    private var isListening = false
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private val weatherService = WeatherService(context)
    private val newsService = NewsService(context)
    private val searchService = SearchService(context)
    private val _isListening = MutableStateFlow(false)
    val isListeningFlow: StateFlow<Boolean> = _isListening
    private val _recognizedText = MutableStateFlow("")
    val recognizedTextFlow: StateFlow<String> = _recognizedText
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeakingFlow: StateFlow<Boolean> = _isSpeaking
    private val prefs = context.getSharedPreferences("voice_settings", Context.MODE_PRIVATE)
    private var currentLanguage: Locale = Locale.ENGLISH
    private var singleResultCallback: ((String) -> Unit)? = null
    private var onCommandProcessed: ((command: String, response: String) -> Unit)? = null
    
    init {
        loadLanguageSettings()
        initializeSpeechRecognizer()
    }
    
    fun setOnCommandProcessed(callback: (command: String, response: String) -> Unit) {
        onCommandProcessed = callback
    }
    
    private fun loadLanguageSettings() {
        val savedLang = prefs.getString("tts_language", "en") ?: "en"
        ttsEngine.setLanguage(savedLang)
        currentLanguage = getLocaleFromCode(savedLang)
    }
    
    private fun getLocaleFromCode(langCode: String): Locale {
        return when (langCode.lowercase()) {
            "en" -> Locale.ENGLISH
            "hi" -> Locale("hi", "IN")
            "ta" -> Locale("ta", "IN")
            "te" -> Locale("te", "IN")
            "bn" -> Locale("bn", "IN")
            else -> Locale.ENGLISH
        }
    }
    
    fun setChatManager(chatManager: ChatManager) {
        this.chatManager = chatManager
    }
    
    private fun initializeSpeechRecognizer() {
        try {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
                speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) { _isListening.value = true }
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() { _isListening.value = false }
                    override fun onError(error: Int) { _isListening.value = false }
                    override fun onResults(results: Bundle?) {
                        results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.let { matches ->
                            if (matches.isNotEmpty()) {
                                val text = matches[0]
                                _recognizedText.value = text
                                singleResultCallback?.invoke(text)
                                singleResultCallback = null
                                processVoiceCommand(text)
                            }
                        }
                        _isListening.value = false
                    }
                    override fun onPartialResults(partialResults: Bundle?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing speech recognizer", e)
        }
    }
    
    fun startListening(onResult: (String) -> Unit) {
        singleResultCallback = onResult
        startListening()
    }
    
    fun startListening() {
        if (isListening) return
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, currentLanguage)
            }
            speechRecognizer?.startListening(intent)
            isListening = true
        } catch (e: Exception) {
            _isListening.value = false
        }
    }
    
    fun stopListening() {
        speechRecognizer?.stopListening()
        isListening = false
        _isListening.value = false
    }
    
    fun speak(text: String) {
        _isSpeaking.value = true
        ttsEngine.speak(text)
        scope.launch {
            delay((text.length * 50L).coerceIn(1000L, 10000L))
            _isSpeaking.value = false
        }
    }
    
    fun setLanguage(locale: Locale) {
        currentLanguage = locale
        prefs.edit().putString("tts_language", locale.language).apply()
        ttsEngine.setLanguage(locale.language)
    }
    
    /**
     * ✅ FIXED: Dynamic city detection for weather
     */
    private fun processVoiceCommand(command: String) {
        val lower = command.lowercase()
        var response = ""
        
        scope.launch {
            try {
                response = when {
                    lower.contains("news") || lower.contains("headlines") -> {
                        val category = when {
                            lower.contains("sports") -> "sports"
                            lower.contains("business") -> "business"
                            lower.contains("tech") -> "technology"
                            else -> null
                        }
                        val result = newsService.getTopHeadlines(category, 3)
                        if (result.isSuccess) newsService.formatNewsForVoice(result.getOrNull() ?: emptyList())
                        else "I couldn't fetch the news."
                    }
                    
                    // ✅ FIXED: Extract city from weather command
                    lower.contains("weather") -> {
                        val city = extractCity(lower)
                        Log.d(TAG, "Weather requested for: $city")
                        val result = weatherService.getCurrentWeather(city)
                        if (result.isSuccess) {
                            weatherService.formatWeatherForVoice(result.getOrNull()!!)
                        } else {
                            "I couldn't fetch weather data for $city. Please check your internet connection and try again."
                        }
                    }
                    
                    lower.matches(".*(open|launch)\\s+(.+)".toRegex()) -> {
                        val appName = lower.replace(".*(open|launch)\\s+".toRegex(), "").trim()
                        if (deviceController.openApp(appName)) "Opening $appName" else "Couldn't open $appName"
                    }
                    "wifi on" in lower -> { deviceController.toggleWiFi(true); "WiFi turned on" }
                    "wifi off" in lower -> { deviceController.toggleWiFi(false); "WiFi turned off" }
                    "bluetooth on" in lower -> { deviceController.toggleBluetooth(true); "Bluetooth turned on" }
                    "bluetooth off" in lower -> { deviceController.toggleBluetooth(false); "Bluetooth turned off" }
                    "flashlight on" in lower || "torch on" in lower -> { deviceController.toggleFlashlight(true); "Flashlight turned on" }
                    "flashlight off" in lower || "torch off" in lower -> { deviceController.toggleFlashlight(false); "Flashlight turned off" }
                    "volume up" in lower || "increase volume" in lower -> { deviceController.volumeUp(); "Volume increased" }
                    "volume down" in lower || "decrease volume" in lower -> { deviceController.volumeDown(); "Volume decreased" }
                    "brightness up" in lower || "increase brightness" in lower -> { deviceController.increaseBrightness(); "Brightness increased" }
                    "brightness down" in lower || "decrease brightness" in lower -> { deviceController.decreaseBrightness(); "Brightness decreased" }
                    "take photo" in lower || "take picture" in lower -> { deviceController.takePhoto(); "Taking photo" }
                    "take selfie" in lower -> { deviceController.takeSelfie(); "Taking selfie" }
                    "battery" in lower || "battery level" in lower -> {
                        val level = deviceController.getBatteryLevel()
                        "Battery level is $level percent"
                    }
                    "time" in lower -> "The time is ${deviceController.getCurrentTime()}"
                    "date" in lower -> "Today is ${deviceController.getCurrentDate()}"
                    else -> {
                        chatManager?.let {
                            val msg = it.sendMessage(command)
                            msg.text
                        } ?: "I didn't understand that. Could you repeat?"
                    }
                }
                
                if (response.isNotEmpty()) speak(response)
                onCommandProcessed?.invoke(command, response)
            } catch (e: Exception) {
                Log.e(TAG, "Error processing command: $command", e)
                speak("Sorry, I encountered an error processing your request")
            }
        }
    }
    
    /**
     * ✅ NEW: Extract city name from weather command
     */
    private fun extractCity(command: String): String {
        val lower = command.lowercase().trim()
        
        // Remove common weather keywords
        var city = lower
            .replace("what's the weather", "")
            .replace("what is the weather", "")
            .replace("weather", "")
            .replace(" in ", " ")
            .replace(" at ", " ")
            .replace(" for ", " ")
            .replace(" of ", " ")
            .trim()
        
        // If empty, use default
        if (city.isEmpty()) {
            return "Delhi" // Default city
        }
        
        // Capitalize first letter of each word
        return city.split(" ").joinToString(" ") { it.capitalize() }
    }
    
    /**
     * ✅ NEW: Voice selection (male/female)
     */
    fun changeVoice(voiceId: String) = ttsEngine.changeVoice(voiceId)
    fun getAvailableVoices() = ttsEngine.getAvailableVoices()
    fun getCurrentVoice() = ttsEngine.getCurrentVoice()
    
    /**
     * ✅ NEW: Speech customization
     */
    fun setSpeechRate(rate: Float) = ttsEngine.setSpeechRate(rate)
    fun setPitch(pitch: Float) = ttsEngine.setPitch(pitch)
    
    fun cleanup() {
        scope.cancel()
        speechRecognizer?.destroy()
        ttsEngine.shutdown()
    }
    
    companion object {
        private const val TAG = "VoiceController"
    }
}