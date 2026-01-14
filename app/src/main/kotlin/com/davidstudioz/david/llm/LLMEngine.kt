package com.davidstudioz.david.llm

import android.content.Context
import android.util.Log
import com.davidstudioz.david.ai.LlamaCppEngine
import com.davidstudioz.david.chat.ChatHistoryManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * LLMEngine - INTEGRATED with LlamaCpp
 * ✅ Uses LlamaCppEngine for GGUF models
 * ✅ Auto-loads models from david_models/
 * ✅ Smart fallback to rule-based responses
 * ✅ Model status tracking
 */
class LLMEngine(private val context: Context) {
    
    private val modelsDir = File(context.filesDir, "david_models")
    private var isModelLoaded = false
    private var loadedModelFile: File? = null
    private var modelType: String = "rule-based"
    private lateinit var chatHistoryManager: ChatHistoryManager
    
    // ✅ NEW: LlamaCpp engine for GGUF models
    private val llamaCppEngine = LlamaCppEngine(context)
    
    init {
        loadModel()
    }
    
    fun setChatHistoryManager(manager: ChatHistoryManager) {
        chatHistoryManager = manager
    }
    
    /**
     * ✅ FIXED: Uses LlamaCpp for GGUF models
     */
    suspend fun generateResponse(userInput: String): String = withContext(Dispatchers.IO) {
        try {
            // Try GGUF model first if loaded
            if (modelType == "gguf" && llamaCppEngine.isReady()) {
                Log.d(TAG, "🤖 Using GGUF model for inference...")
                val ggufResponse = llamaCppEngine.generate(
                    prompt = "User: $userInput\nAssistant:",
                    maxTokens = 150,
                    temperature = 0.7f
                )
                
                if (ggufResponse.isNotBlank()) {
                    Log.d(TAG, "✅ GGUF response generated: ${ggufResponse.take(50)}...")
                    return@withContext ggufResponse.trim()
                } else {
                    Log.w(TAG, "⚠️ GGUF returned empty, using fallback")
                }
            }
            
            // Fallback to rule-based responses
            return@withContext generateRuleBasedResponse(userInput)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error generating response", e)
            return@withContext "I encountered an error. Please try again."
        }
    }
    
    /**
     * Rule-based intelligent responses
     */
    private fun generateRuleBasedResponse(userInput: String): String {
        val context = if (::chatHistoryManager.isInitialized) {
            chatHistoryManager.getContextForLLM()
        } else ""
        
        return when {
            userInput.matches(Regex("(?i).*(hello|hi|hey|greetings).*")) ->
                "Hello! I'm D.A.V.I.D, your AI assistant. How can I help you today?"
            
            userInput.matches(Regex("(?i).*(who are you|what are you|your name).*")) ->
                "I'm D.A.V.I.D - Digital Assistant with Voice, Intelligence and Device control."
            
            userInput.matches(Regex("(?i).*(time|clock|what time).*")) -> {
                val time = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
                    .format(java.util.Date())
                "The current time is $time."
            }
            
            userInput.matches(Regex("(?i).*(date|today|day).*")) -> {
                val date = java.text.SimpleDateFormat("EEEE, MMMM d, yyyy", java.util.Locale.getDefault())
                    .format(java.util.Date())
                "Today is $date."
            }
            
            userInput.matches(Regex("(?i).*(joke|funny|laugh).*")) -> {
                listOf(
                    "Why did the AI go to school? To improve its neural networks!",
                    "What do you call an AI that sings? A-dell!",
                    "Why don't robots ever get lost? They always follow the algorithm!"
                ).random()
            }
            
            userInput.matches(Regex("(?i).*(thank|thanks).*")) ->
                "You're very welcome! I'm always here to help you."
            
            userInput.matches(Regex("(?i).*(help|what can you|capabilities).*")) ->
                "I can help with voice commands, chat, gestures, device control, and 15 languages. What would you like to try?"
            
            // Math calculations
            userInput.matches(Regex("(?i).*(calculate|math|plus|minus|multiply|divide).*")) -> {
                try {
                    val calcPattern = """(\d+)\s*([+\-*/])\s*(\d+)""".toRegex()
                    val match = calcPattern.find(userInput)
                    if (match != null) {
                        val (num1, op, num2) = match.destructured
                        val result = when (op) {
                            "+" -> num1.toInt() + num2.toInt()
                            "-" -> num1.toInt() - num2.toInt()
                            "*" -> num1.toInt() * num2.toInt()
                            "/" -> if (num2.toInt() != 0) num1.toInt() / num2.toInt() else "undefined"
                            else -> "error"
                        }
                        "$num1 $op $num2 = $result"
                    } else {
                        "Try asking: '5 + 3' or '10 * 2'"
                    }
                } catch (e: Exception) {
                    "Try asking: '5 + 3' or '10 * 2'"
                }
            }
            
            else -> {
                when {
                    userInput.contains("?") ->
                        "That's an interesting question! I'm learning to provide better answers. Could you rephrase?"
                    userInput.length < 10 ->
                        "I heard: '$userInput'. Could you tell me more?"
                    else ->
                        "I'm continuously learning! Right now, I can help with voice control, device settings, gestures, and general questions. What would you like to try?"
                }
            }
        }
    }
    
    /**
     * ✅ FIXED: Load model and initialize LlamaCpp
     */
    private fun loadModel() {
        try {
            Log.d(TAG, "🔍 Looking for LLM models in: ${modelsDir.absolutePath}")
            
            if (!modelsDir.exists()) {
                Log.w(TAG, "Models directory doesn't exist")
                modelsDir.mkdirs()
                isModelLoaded = true
                modelType = "rule-based"
                return
            }
            
            val files = modelsDir.listFiles() ?: emptyArray()
            Log.d(TAG, "Found ${files.size} files")
            
            // Look for GGUF models
            val ggufModel = files.firstOrNull { file ->
                file.extension.lowercase() == "gguf" && file.length() > 100 * 1024 * 1024
            }
            
            if (ggufModel != null && ggufModel.exists()) {
                Log.d(TAG, "✅ Found GGUF model: ${ggufModel.name}")
                loadedModelFile = ggufModel
                modelType = "gguf"
                
                // Load model in background
                kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
                    val loaded = llamaCppEngine.loadModel(
                        modelFile = ggufModel,
                        nThreads = 4,
                        contextSize = 2048,
                        useGpu = false
                    )
                    isModelLoaded = loaded
                    if (loaded) {
                        Log.d(TAG, "🎉 GGUF model loaded successfully!")
                    } else {
                        Log.e(TAG, "❌ Failed to load GGUF model")
                        modelType = "rule-based"
                    }
                }
            } else {
                Log.w(TAG, "⚠️ No GGUF model found, using rule-based")
                isModelLoaded = true
                modelType = "rule-based"
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error loading model", e)
            isModelLoaded = true
            modelType = "rule-based"
        }
    }
    
    fun getModelStatus(): String {
        return when {
            modelType == "gguf" && llamaCppEngine.isReady() -> {
                "✅ GGUF Model: ${loadedModelFile?.name}\n" +
                "Size: ${(loadedModelFile?.length() ?: 0) / (1024 * 1024)}MB\n" +
                "Status: Ready ✅"
            }
            modelType == "gguf" && !llamaCppEngine.isReady() -> {
                "⏳ GGUF Model: Loading...\n" +
                "File: ${loadedModelFile?.name}"
            }
            else -> {
                "⚠️ Mode: Smart responses\n" +
                "Add GGUF model to david_models/ for AI"
            }
        }
    }
    
    fun isAIModelLoaded(): Boolean = llamaCppEngine.isReady()
    
    fun getModelInfo(): Map<String, String> = mapOf(
        "type" to modelType,
        "file" to (loadedModelFile?.name ?: "none"),
        "size" to "${(loadedModelFile?.length() ?: 0) / (1024 * 1024)}MB",
        "status" to if (llamaCppEngine.isReady()) "ready" else "loading"
    )
    
    fun cleanup() {
        llamaCppEngine.release()
    }
    
    companion object {
        private const val TAG = "LLMEngine"
    }
}