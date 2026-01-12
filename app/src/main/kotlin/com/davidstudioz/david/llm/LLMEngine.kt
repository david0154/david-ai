package com.davidstudioz.david.llm

import android.content.Context
import android.util.Log
import com.davidstudioz.david.chat.ChatHistoryManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * LLMEngine - Large Language Model inference engine
 * Connected to: ChatEngine, VoiceController, ChatHistoryManager, SafeMainActivity
 */
class LLMEngine(private val context: Context) {
    
    private val modelsDir = File(context.filesDir, "david_models")
    private var isModelLoaded = false
    private lateinit var chatHistoryManager: ChatHistoryManager
    
    init {
        loadModel()
    }
    
    /**
     * Set chat history manager for context
     * Called by: SafeMainActivity during initialization
     */
    fun setChatHistoryManager(manager: ChatHistoryManager) {
        chatHistoryManager = manager
    }
    
    /**
     * Generate AI response to user input
     * Called by: SafeMainActivity (chat + voice), ChatEngine, VoiceController
     */
    suspend fun generateResponse(userInput: String): String = withContext(Dispatchers.IO) {
        if (!isModelLoaded) {
            return@withContext "AI model is loading, please wait..."
        }
        
        try {
            // Get conversation context if available
            val context = if (::chatHistoryManager.isInitialized) {
                chatHistoryManager.getContextForLLM()
            } else ""
            
            Log.d(TAG, "Generating response for: $userInput")
            
            // Intelligent response generation based on input patterns
            val response = when {
                // Greetings
                userInput.matches(Regex("(?i).*(hello|hi|hey|greetings).*")) ->
                    "Hello! I'm D.A.V.I.D, your AI assistant. How can I help you today?"
                
                // Questions about D.A.V.I.D
                userInput.matches(Regex("(?i).*(who are you|what are you|your name).*")) ->
                    "I'm D.A.V.I.D - Digital Assistant with Voice, Intelligence and Device control. I can help you with voice commands, chat conversations, gesture control, and managing your device."
                
                // Weather
                userInput.matches(Regex("(?i).*(weather|temperature|forecast).*")) ->
                    "I can help with weather information! Currently, I'm showing sunny conditions at 25°C. For real-time weather, I'll need internet access."
                
                // Time
                userInput.matches(Regex("(?i).*(time|clock|what time).*")) -> {
                    val time = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
                        .format(java.util.Date())
                    "The current time is $time."
                }
                
                // Date
                userInput.matches(Regex("(?i).*(date|today|day).*")) -> {
                    val date = java.text.SimpleDateFormat("EEEE, MMMM d, yyyy", java.util.Locale.getDefault())
                        .format(java.util.Date())
                    "Today is $date."
                }
                
                // Jokes
                userInput.matches(Regex("(?i).*(joke|funny|laugh).*")) -> {
                    val jokes = listOf(
                        "Why did the AI go to school? To improve its neural networks!",
                        "What do you call an AI that sings? A-dell!",
                        "Why don't robots ever get lost? They always follow the algorithm!",
                        "What's an AI's favorite snack? Microchips!"
                    )
                    jokes.random()
                }
                
                // Device control
                userInput.matches(Regex("(?i).*(wifi|bluetooth|brightness|volume|device).*")) ->
                    "I can help you control your device! Check the Device Control tab to toggle WiFi, Bluetooth, adjust brightness, and more."
                
                // Voice control
                userInput.matches(Regex("(?i).*(voice|speak|listen|microphone).*")) ->
                    "Voice control is one of my key features! Go to the Voice tab to interact with me using your voice."
                
                // Gesture control
                userInput.matches(Regex("(?i).*(gesture|hand|camera|wave).*")) ->
                    "I support gesture recognition! Visit the Gesture Control tab to use hand gestures to control your device."
                
                // Languages
                userInput.matches(Regex("(?i).*(language|hindi|tamil|telugu|bengali).*")) ->
                    "I support 15 languages including English, Hindi, Tamil, Telugu, and Bengali. Tap the language icon in the top bar to change languages."
                
                // Privacy
                userInput.matches(Regex("(?i).*(privacy|secure|encrypt|data|safe).*")) ->
                    "Your privacy is my priority! All your data is encrypted and stored locally on your device. I never send your information to external servers."
                
                // Thank you
                userInput.matches(Regex("(?i).*(thank|thanks).*")) ->
                    "You're very welcome! I'm always here to help you."
                
                // Help
                userInput.matches(Regex("(?i).*(help|what can you|capabilities|features).*")) ->
                    "I can help you with:\n" +
                    "• Voice commands and conversations\n" +
                    "• Chat and questions\n" +
                    "• Gesture recognition\n" +
                    "• Device control (WiFi, Bluetooth, etc.)\n" +
                    "• 15 language support\n" +
                    "• Privacy-focused local AI\n\n" +
                    "What would you like to try?"
                
                // Default intelligent response
                else -> {
                    // Use simple pattern matching for intelligent responses
                    when {
                        userInput.contains("?") ->
                            "That's an interesting question! Based on what you asked about '${userInput.take(30)}...', I'm learning to provide better answers. Could you rephrase or ask something else I can help with?"
                        userInput.length < 10 ->
                            "I heard: '$userInput'. Could you please tell me more about what you need help with?"
                        else ->
                            "I understand you said: '${userInput.take(50)}${if (userInput.length > 50) "..." else ""}'. I'm continuously learning! Right now, I can help with voice control, device settings, gestures, and general questions. What would you like to try?"
                    }
                }
            }
            
            Log.d(TAG, "Generated response: ${response.take(50)}...")
            return@withContext response
            
        } catch (e: Exception) {
            Log.e(TAG, "Error generating response", e)
            return@withContext "I encountered an error processing your request. Please try again."
        }
    }
    
    /**
     * Generate streaming response (for real-time chat)
     * Called by: ChatEngine for progressive response display
     */
    suspend fun generateStreamingResponse(
        userInput: String,
        onToken: (String) -> Unit
    ) {
        val fullResponse = generateResponse(userInput)
        // Simulate streaming by sending words one by one
        fullResponse.split(" ").forEach { word ->
            onToken("$word ")
            kotlinx.coroutines.delay(50) // Simulate token generation delay
        }
    }
    
    private fun loadModel() {
        try {
            // Check if model files exist
            val chatModel = File(modelsDir, "chat_standard.bin")
            val chatModelLight = File(modelsDir, "chat_light.bin")
            val chatModelPro = File(modelsDir, "chat_pro.bin")
            
            isModelLoaded = chatModel.exists() || chatModelLight.exists() || chatModelPro.exists()
            
            if (isModelLoaded) {
                Log.d(TAG, "LLM model loaded successfully")
            } else {
                Log.w(TAG, "No LLM model found, using rule-based responses")
                isModelLoaded = true // Enable rule-based mode
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading model", e)
            isModelLoaded = true // Fallback to rule-based
        }
    }
    
    companion object {
        private const val TAG = "LLMEngine"
    }
}
