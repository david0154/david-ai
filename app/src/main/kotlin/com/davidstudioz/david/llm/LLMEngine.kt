package com.davidstudioz.david.llm

import android.content.Context
import android.util.Log
import com.davidstudioz.david.ai.UniversalModelLoader
import com.davidstudioz.david.chat.ChatHistoryManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * LLMEngine - UNIVERSAL MODEL SUPPORT
 * ✅ GGUF, GGML, TFLite, ONNX - ALL formats
 * ✅ Auto-loads best available model
 * ✅ Smart fallback to rule-based responses
 */
class LLMEngine(private val context: Context) {
    
    private val modelsDir = File(context.filesDir, "david_models")
    private lateinit var chatHistoryManager: ChatHistoryManager
    
    // ✅ UNIVERSAL MODEL LOADER
    private val universalLoader = UniversalModelLoader(context)
    
    init {
        loadBestAvailableModel()
    }
    
    fun setChatHistoryManager(manager: ChatHistoryManager) {
        chatHistoryManager = manager
    }
    
    /**
     * ✅ Load best available model
     */
    private fun loadBestAvailableModel() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                if (!modelsDir.exists()) {
                    modelsDir.mkdirs()
                    return@launch
                }
                
                val models = universalLoader.scanForModels(modelsDir)
                if (models.isEmpty()) {
                    Log.w(TAG, "No models found")
                    return@launch
                }
                
                // Load best model
                val best = models.maxByOrNull { it.sizeBytes }
                if (best != null) {
                    universalLoader.loadModel(best.file)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Load error", e)
            }
        }
    }
    
    suspend fun generateResponse(userInput: String): String = withContext(Dispatchers.IO) {
        try {
            if (universalLoader.isReady()) {
                val response = universalLoader.generate(
                    prompt = "User: $userInput\nAssistant:",
                    maxTokens = 150,
                    temperature = 0.7f
                )
                
                if (response.isNotBlank()) {
                    return@withContext response.trim()
                }
            }
            
            return@withContext generateRuleBasedResponse(userInput)
        } catch (e: Exception) {
            return@withContext "I encountered an error. Please try again."
        }
    }
    
    private fun generateRuleBasedResponse(userInput: String): String {
        return when {
            userInput.matches(Regex("(?i).*(hello|hi|hey).*")) ->
                "Hello! I'm D.A.V.I.D. How can I help?"
            userInput.matches(Regex("(?i).*(time).*")) -> {
                val time = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
                    .format(java.util.Date())
                "The time is $time"
            }
            userInput.matches(Regex("(?i).*(date|today).*")) -> {
                val date = java.text.SimpleDateFormat("EEEE, MMMM d, yyyy", java.util.Locale.getDefault())
                    .format(java.util.Date())
                "Today is $date"
            }
            else -> "I'm here to help! Ask me about time, date, or anything else."
        }
    }
    
    fun getModelStatus(): String = universalLoader.getStatus()
    fun isAIModelLoaded(): Boolean = universalLoader.isReady()
    fun getModelInfo(): Map<String, String> {
        val file = universalLoader.getModelFile()
        return mapOf(
            "type" to universalLoader.getModelType().name,
            "file" to (file?.name ?: "none"),
            "size" to "${(file?.length() ?: 0) / (1024 * 1024)}MB"
        )
    }
    
    fun cleanup() {
        universalLoader.release()
    }
    
    companion object {
        private const val TAG = "LLMEngine"
    }
}