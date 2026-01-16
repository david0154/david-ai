package com.davidstudioz.david.ai

import android.content.Context
import android.util.Log
import java.io.File

/**
 * UniversalModelLoader - Supports ALL model formats
 * ✅ GGUF (.gguf) - Modern llama.cpp format
 * ✅ GGML (.ggml, .bin) - Legacy llama.cpp format
 * ✅ TFLite (.tflite) - TensorFlow Lite
 * ✅ ONNX (.onnx) - ONNX Runtime
 * ✅ Auto-detection and smart fallback
 */
class UniversalModelLoader(private val context: Context) {
    
    private var loadedModelType: ModelType = ModelType.NONE
    private var loadedModelFile: File? = null
    private var isReady = false
    
    // Framework engines
    private val ggufEngine = GGUFEngine(context)
    private val ggmlEngine = GGMLEngine(context)
    private val tfliteEngine = TFLiteEngine(context)
    private val onnxEngine = ONNXEngine(context)
    
    enum class ModelType {
        GGUF,    // Modern llama.cpp (recommended)
        GGML,    // Legacy llama.cpp
        TFLITE,  // TensorFlow Lite
        ONNX,    // ONNX Runtime
        NONE     // No model loaded
    }
    
    /**
     * Auto-detect and load model from file
     */
    fun loadModel(modelFile: File): Boolean {
        if (!modelFile.exists() || !modelFile.canRead()) {
            Log.e(TAG, "Model file not accessible: ${modelFile.path}")
            return false
        }
        
        val extension = modelFile.extension.lowercase()
        Log.d(TAG, "Loading model: ${modelFile.name} (${modelFile.length() / 1024 / 1024}MB)")
        
        val loaded = when (extension) {
            "gguf" -> loadGGUF(modelFile)
            "ggml", "bin" -> loadGGML(modelFile)
            "tflite" -> loadTFLite(modelFile)
            "onnx" -> loadONNX(modelFile)
            else -> {
                Log.w(TAG, "Unknown model format: .$extension")
                false
            }
        }
        
        if (loaded) {
            loadedModelFile = modelFile
            isReady = true
            Log.d(TAG, "✅ Model loaded successfully as $loadedModelType")
        } else {
            Log.e(TAG, "❌ Failed to load model")
        }
        
        return loaded
    }
    
    /**
     * Load GGUF model (modern llama.cpp format)
     */
    private fun loadGGUF(file: File): Boolean {
        return try {
            val loaded = ggufEngine.loadModel(file)
            if (loaded) {
                loadedModelType = ModelType.GGUF
                Log.d(TAG, "GGUF model loaded")
            }
            loaded
        } catch (e: Exception) {
            Log.e(TAG, "GGUF load error", e)
            false
        }
    }
    
    /**
     * Load GGML model (legacy llama.cpp format)
     */
    private fun loadGGML(file: File): Boolean {
        return try {
            val loaded = ggmlEngine.loadModel(file)
            if (loaded) {
                loadedModelType = ModelType.GGML
                Log.d(TAG, "GGML model loaded")
            }
            loaded
        } catch (e: Exception) {
            Log.e(TAG, "GGML load error", e)
            false
        }
    }
    
    /**
     * Load TFLite model
     */
    private fun loadTFLite(file: File): Boolean {
        return try {
            val loaded = tfliteEngine.loadModel(file)
            if (loaded) {
                loadedModelType = ModelType.TFLITE
                Log.d(TAG, "TFLite model loaded")
            }
            loaded
        } catch (e: Exception) {
            Log.e(TAG, "TFLite load error", e)
            false
        }
    }
    
    /**
     * Load ONNX model
     */
    private fun loadONNX(file: File): Boolean {
        return try {
            val loaded = onnxEngine.loadModel(file)
            if (loaded) {
                loadedModelType = ModelType.ONNX
                Log.d(TAG, "ONNX model loaded")
            }
            loaded
        } catch (e: Exception) {
            Log.e(TAG, "ONNX load error", e)
            false
        }
    }
    
    /**
     * Generate text using loaded model
     */
    fun generate(
        prompt: String,
        maxTokens: Int = 150,
        temperature: Float = 0.7f
    ): String {
        if (!isReady) {
            Log.w(TAG, "No model loaded")
            return ""
        }
        
        return try {
            when (loadedModelType) {
                ModelType.GGUF -> ggufEngine.generate(prompt, maxTokens, temperature)
                ModelType.GGML -> ggmlEngine.generate(prompt, maxTokens, temperature)
                ModelType.TFLITE -> tfliteEngine.generate(prompt, maxTokens, temperature)
                ModelType.ONNX -> onnxEngine.generate(prompt, maxTokens, temperature)
                ModelType.NONE -> ""
            }
        } catch (e: Exception) {
            Log.e(TAG, "Generation error", e)
            ""
        }
    }
    
    /**
     * Scan directory for compatible models
     */
    fun scanForModels(directory: File): List<ModelInfo> {
        if (!directory.exists() || !directory.isDirectory) {
            return emptyList()
        }
        
        val models = mutableListOf<ModelInfo>()
        directory.listFiles()?.forEach { file ->
            val ext = file.extension.lowercase()
            val type = when (ext) {
                "gguf" -> ModelType.GGUF
                "ggml", "bin" -> ModelType.GGML
                "tflite" -> ModelType.TFLITE
                "onnx" -> ModelType.ONNX
                else -> null
            }
            
            if (type != null && file.length() > 1024 * 1024) { // At least 1MB
                models.add(
                    ModelInfo(
                        file = file,
                        type = type,
                        name = file.name,
                        sizeBytes = file.length()
                    )
                )
            }
        }
        
        return models.sortedByDescending { it.sizeBytes }
    }
    
    /**
     * Get model status
     */
    fun getStatus(): String {
        return when {
            !isReady -> "No model loaded"
            else -> {
                val sizeMB = (loadedModelFile?.length() ?: 0) / (1024 * 1024)
                "✅ $loadedModelType: ${loadedModelFile?.name} (${sizeMB}MB)"
            }
        }
    }
    
    fun isReady(): Boolean = isReady
    fun getModelType(): ModelType = loadedModelType
    fun getModelFile(): File? = loadedModelFile
    
    /**
     * Release all resources
     */
    fun release() {
        ggufEngine.release()
        ggmlEngine.release()
        tfliteEngine.release()
        onnxEngine.release()
        loadedModelType = ModelType.NONE
        loadedModelFile = null
        isReady = false
        Log.d(TAG, "Released all model resources")
    }
    
    data class ModelInfo(
        val file: File,
        val type: ModelType,
        val name: String,
        val sizeBytes: Long
    ) {
        val sizeMB: Int get() = (sizeBytes / (1024 * 1024)).toInt()
    }
    
    companion object {
        private const val TAG = "UniversalModelLoader"
    }
}