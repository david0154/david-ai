package com.davidstudioz.david.ai

import android.content.Context
import android.util.Log
import de.kherud.llama.InferenceParameters
import de.kherud.llama.LlamaModel
import de.kherud.llama.ModelParameters
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * LlamaCpp Engine - GGUF Model Inference
 * ✅ Loads GGUF models using llama.cpp-android
 * ✅ Supports CPU and GPU acceleration
 * ✅ Handles Q4, Q5, Q8 quantization formats
 * ✅ Smart memory management
 * ✅ Graceful fallback on errors
 */
class LlamaCppEngine(private val context: Context) {

    private var llamaModel: LlamaModel? = null
    private var isModelLoaded = false
    private var modelPath: String? = null
    
    /**
     * Load GGUF model
     * @param modelFile GGUF model file
     * @param nThreads Number of CPU threads (default: 4)
     * @param contextSize Context window size (default: 2048)
     * @param useGpu Enable GPU acceleration if available
     */
    suspend fun loadModel(
        modelFile: File,
        nThreads: Int = 4,
        contextSize: Int = 2048,
        useGpu: Boolean = false
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔄 Loading GGUF model: ${modelFile.name}")
            Log.d(TAG, "📦 File size: ${modelFile.length() / (1024 * 1024)}MB")
            Log.d(TAG, "⚙️ Threads: $nThreads, Context: $contextSize, GPU: $useGpu")
            
            if (!modelFile.exists()) {
                Log.e(TAG, "❌ Model file not found: ${modelFile.absolutePath}")
                return@withContext false
            }
            
            // Release existing model if any
            release()
            
            // Configure model parameters
            val modelParams = ModelParameters().apply {
                setNThreads(nThreads)
                setContextSize(contextSize)
                setUseGpu(useGpu)
                setVocabOnly(false)
                setUseMlock(false) // Don't lock memory on Android
            }
            
            // Load model
            llamaModel = LlamaModel(modelFile.absolutePath, modelParams)
            modelPath = modelFile.absolutePath
            isModelLoaded = true
            
            Log.d(TAG, "✅ GGUF model loaded successfully!")
            Log.d(TAG, "📊 Model info: ${getModelInfo()}")
            return@withContext true
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to load GGUF model", e)
            isModelLoaded = false
            return@withContext false
        }
    }
    
    /**
     * Generate text using loaded GGUF model
     * @param prompt Input text
     * @param maxTokens Maximum tokens to generate
     * @param temperature Sampling temperature (0.0-2.0)
     * @param topP Nucleus sampling threshold
     * @param topK Top-k sampling
     * @return Generated text
     */
    suspend fun generate(
        prompt: String,
        maxTokens: Int = 100,
        temperature: Float = 0.7f,
        topP: Float = 0.9f,
        topK: Int = 40
    ): String = withContext(Dispatchers.IO) {
        try {
            if (!isModelLoaded || llamaModel == null) {
                Log.w(TAG, "⚠️ Model not loaded - cannot generate")
                return@withContext ""
            }
            
            Log.d(TAG, "🤖 Generating response for prompt: ${prompt.take(50)}...")
            
            // Configure inference parameters
            val inferParams = InferenceParameters().apply {
                setTemperature(temperature)
                setTopP(topP)
                setTopK(topK)
                setNPredict(maxTokens)
            }
            
            // Generate text
            val output = StringBuilder()
            for (token in llamaModel!!.generate(prompt, inferParams)) {
                output.append(token)
            }
            
            val result = output.toString().trim()
            Log.d(TAG, "✅ Generated ${result.length} characters")
            return@withContext result
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Generation failed", e)
            return@withContext ""
        }
    }
    
    /**
     * Get model information
     */
    fun getModelInfo(): String {
        return if (isModelLoaded && modelPath != null) {
            "Model: ${File(modelPath!!).name}\nStatus: Loaded ✅\nEngine: llama.cpp"
        } else {
            "No model loaded"
        }
    }
    
    /**
     * Check if model is ready
     */
    fun isReady(): Boolean = isModelLoaded && llamaModel != null
    
    /**
     * Release model resources
     */
    fun release() {
        try {
            llamaModel?.close()
            llamaModel = null
            isModelLoaded = false
            modelPath = null
            Log.d(TAG, "🧹 LlamaCpp model released")
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing model", e)
        }
    }
    
    companion object {
        private const val TAG = "LlamaCppEngine"
    }
}