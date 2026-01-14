package com.davidstudioz.david.core.model

import android.app.ActivityManager
import android.content.Context
import android.os.Debug
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Model Lifecycle Manager for:
 * - Automatic model unloading after inactivity
 * - Memory pressure monitoring
 * - Model priority system
 * - Smart preloading based on usage patterns
 * - Memory threshold management
 */
@Singleton
class ModelLifecycleManager @Inject constructor(
    @ApplicationContext private val context: Context
) : DefaultLifecycleObserver {

    companion object {
        private const val TAG = "ModelLifecycleManager"
        private const val INACTIVITY_TIMEOUT_MS = 5 * 60 * 1000L // 5 minutes
        private const val MEMORY_CHECK_INTERVAL_MS = 10000L // 10 seconds
        private const val LOW_MEMORY_THRESHOLD_MB = 200L
        private const val CRITICAL_MEMORY_THRESHOLD_MB = 100L
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    // Track loaded models and their last access time
    private val loadedModels = mutableMapOf<ModelType, LoadedModelInfo>()
    private val modelLoaders = mutableMapOf<ModelType, ModelLoader>()
    
    // Memory pressure state
    private val _memoryPressure = MutableStateFlow(MemoryPressure.NORMAL)
    val memoryPressure: StateFlow<MemoryPressure> = _memoryPressure.asStateFlow()

    // Model loading states
    private val _modelStates = MutableStateFlow<Map<ModelType, ModelState>>(emptyMap())
    val modelStates: StateFlow<Map<ModelType, ModelState>> = _modelStates.asStateFlow()

    private var memoryMonitorJob: Job? = null
    private var inactivityMonitorJob: Job? = null

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        startMemoryMonitoring()
        startInactivityMonitoring()
    }

    /**
     * Register a model loader
     */
    fun registerModelLoader(modelType: ModelType, loader: ModelLoader) {
        modelLoaders[modelType] = loader
    }

    /**
     * Load a model with priority and memory checks
     */
    suspend fun loadModel(modelType: ModelType): Result<Unit> = withContext(Dispatchers.Default) {
        try {
            // Check if already loaded
            if (isModelLoaded(modelType)) {
                updateLastAccessTime(modelType)
                updateModelState(modelType, ModelState.Loaded)
                return@withContext Result.success(Unit)
            }

            // Check memory before loading
            val currentMemory = getAvailableMemoryMB()
            val requiredMemory = modelType.estimatedMemoryMB

            if (currentMemory < requiredMemory + LOW_MEMORY_THRESHOLD_MB) {
                // Try to free memory by unloading lower priority models
                val freed = unloadLowerPriorityModels(modelType.priority, requiredMemory)
                
                if (!freed) {
                    updateModelState(modelType, ModelState.Failed("Insufficient memory"))
                    return@withContext Result.failure(
                        Exception("Insufficient memory to load ${modelType.name}. Required: ${requiredMemory}MB, Available: ${currentMemory}MB")
                    )
                }
            }

            // Load the model
            updateModelState(modelType, ModelState.Loading)
            
            val loader = modelLoaders[modelType]
                ?: return@withContext Result.failure(Exception("No loader registered for ${modelType.name}"))

            val loadResult = loader.load()
            
            if (loadResult.isSuccess) {
                loadedModels[modelType] = LoadedModelInfo(
                    modelType = modelType,
                    loadedAt = System.currentTimeMillis(),
                    lastAccessTime = System.currentTimeMillis(),
                    memoryUsageMB = requiredMemory,
                    instance = loadResult.getOrNull()
                )
                updateModelState(modelType, ModelState.Loaded)
                Result.success(Unit)
            } else {
                updateModelState(modelType, ModelState.Failed(loadResult.exceptionOrNull()?.message ?: "Unknown error"))
                Result.failure(loadResult.exceptionOrNull() ?: Exception("Failed to load ${modelType.name}"))
            }

        } catch (e: Exception) {
            updateModelState(modelType, ModelState.Failed(e.message ?: "Unknown error"))
            Result.failure(e)
        }
    }

    /**
     * Unload a specific model
     */
    fun unloadModel(modelType: ModelType) {
        scope.launch {
            try {
                val modelInfo = loadedModels.remove(modelType)
                if (modelInfo != null) {
                    modelLoaders[modelType]?.unload()
                    updateModelState(modelType, ModelState.Unloaded)
                }
            } catch (e: Exception) {
                // Log error but don't throw
            }
        }
    }

    /**
     * Unload all models
     */
    fun unloadAllModels() {
        scope.launch {
            loadedModels.keys.toList().forEach { modelType ->
                unloadModel(modelType)
            }
        }
    }

    /**
     * Preload critical models
     */
    suspend fun preloadCriticalModels() = withContext(Dispatchers.Default) {
        val criticalModels = ModelType.values().filter { it.priority == ModelPriority.CRITICAL }
        
        criticalModels.forEach { modelType ->
            launch {
                loadModel(modelType)
            }
        }
    }

    /**
     * Smart preload based on usage patterns
     */
    suspend fun smartPreload(usageHistory: Map<ModelType, Int>) {
        val sortedByUsage = usageHistory.entries
            .sortedByDescending { it.value }
            .take(3)
            .map { it.key }

        sortedByUsage.forEach { modelType ->
            if (!isModelLoaded(modelType)) {
                loadModel(modelType)
            }
        }
    }

    /**
     * Check if a model is currently loaded
     */
    fun isModelLoaded(modelType: ModelType): Boolean {
        return loadedModels.containsKey(modelType)
    }

    /**
     * Get loaded model instance
     */
    fun <T> getModelInstance(modelType: ModelType): T? {
        updateLastAccessTime(modelType)
        @Suppress("UNCHECKED_CAST")
        return loadedModels[modelType]?.instance as? T
    }

    /**
     * Update last access time for a model
     */
    private fun updateLastAccessTime(modelType: ModelType) {
        loadedModels[modelType]?.let { info ->
            loadedModels[modelType] = info.copy(lastAccessTime = System.currentTimeMillis())
        }
    }

    /**
     * Unload models with lower priority to free memory
     */
    private suspend fun unloadLowerPriorityModels(targetPriority: ModelPriority, requiredMemoryMB: Long): Boolean {
        val sortedModels = loadedModels.entries
            .filter { it.value.modelType.priority.ordinal < targetPriority.ordinal }
            .sortedBy { it.value.modelType.priority.ordinal }

        var freedMemory = 0L
        for ((modelType, info) in sortedModels) {
            unloadModel(modelType)
            freedMemory += info.memoryUsageMB
            
            if (freedMemory >= requiredMemoryMB) {
                return true
            }
        }

        return freedMemory >= requiredMemoryMB
    }

    /**
     * Start monitoring memory pressure
     */
    private fun startMemoryMonitoring() {
        memoryMonitorJob?.cancel()
        memoryMonitorJob = scope.launch {
            while (isActive) {
                val availableMemory = getAvailableMemoryMB()
                
                val newPressure = when {
                    availableMemory < CRITICAL_MEMORY_THRESHOLD_MB -> MemoryPressure.CRITICAL
                    availableMemory < LOW_MEMORY_THRESHOLD_MB -> MemoryPressure.LOW
                    else -> MemoryPressure.NORMAL
                }

                if (newPressure != _memoryPressure.value) {
                    _memoryPressure.value = newPressure
                    handleMemoryPressure(newPressure)
                }

                delay(MEMORY_CHECK_INTERVAL_MS)
            }
        }
    }

    /**
     * Start monitoring model inactivity
     */
    private fun startInactivityMonitoring() {
        inactivityMonitorJob?.cancel()
        inactivityMonitorJob = scope.launch {
            while (isActive) {
                val currentTime = System.currentTimeMillis()
                val inactiveModels = loadedModels.filter { (modelType, info) ->
                    modelType.priority != ModelPriority.CRITICAL &&
                    currentTime - info.lastAccessTime > INACTIVITY_TIMEOUT_MS
                }

                inactiveModels.keys.forEach { modelType ->
                    unloadModel(modelType)
                }

                delay(MEMORY_CHECK_INTERVAL_MS)
            }
        }
    }

    /**
     * Handle memory pressure events
     */
    private fun handleMemoryPressure(pressure: MemoryPressure) {
        scope.launch {
            when (pressure) {
                MemoryPressure.CRITICAL -> {
                    // Unload all non-critical models
                    loadedModels.keys.filter { it.priority != ModelPriority.CRITICAL }.forEach { modelType ->
                        unloadModel(modelType)
                    }
                }
                MemoryPressure.LOW -> {
                    // Unload optional models
                    loadedModels.keys.filter { it.priority == ModelPriority.OPTIONAL }.forEach { modelType ->
                        unloadModel(modelType)
                    }
                }
                MemoryPressure.NORMAL -> {
                    // No action needed
                }
            }
        }
    }

    /**
     * Get available memory in MB
     */
    private fun getAvailableMemoryMB(): Long {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo.availMem / (1024 * 1024)
    }

    /**
     * Get current memory usage in MB
     */
    fun getCurrentMemoryUsageMB(): Long {
        val memoryInfo = Debug.MemoryInfo()
        Debug.getMemoryInfo(memoryInfo)
        return memoryInfo.totalPss / 1024L
    }

    /**
     * Get total loaded models memory usage
     */
    fun getTotalLoadedMemoryMB(): Long {
        return loadedModels.values.sumOf { it.memoryUsageMB }
    }

    private fun updateModelState(modelType: ModelType, state: ModelState) {
        val currentStates = _modelStates.value.toMutableMap()
        currentStates[modelType] = state
        _modelStates.value = currentStates
    }

    override fun onStop(owner: LifecycleOwner) {
        // App moved to background - unload optional models
        scope.launch {
            loadedModels.keys.filter { it.priority == ModelPriority.OPTIONAL }.forEach { modelType ->
                unloadModel(modelType)
            }
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        memoryMonitorJob?.cancel()
        inactivityMonitorJob?.cancel()
        scope.cancel()
    }
}

/**
 * Model type enum with metadata
 */
enum class ModelType(
    val modelName: String,
    val estimatedMemoryMB: Long,
    val priority: ModelPriority
) {
    WHISPER("Whisper ASR", 200, ModelPriority.CRITICAL),
    CHAT_MODEL("Chat LLM", 400, ModelPriority.HIGH),
    GESTURE_RECOGNIZER("Gesture Recognition", 50, ModelPriority.HIGH),
    VISION_MODEL("Vision Model", 150, ModelPriority.NORMAL),
    LANGUAGE_MODEL("Language Model", 100, ModelPriority.NORMAL),
    TTS_MODEL("Text-to-Speech", 80, ModelPriority.HIGH),
    EMOTION_DETECTOR("Emotion Detection", 60, ModelPriority.OPTIONAL),
    OBJECT_DETECTOR("Object Detection", 120, ModelPriority.OPTIONAL)
}

/**
 * Model priority levels
 */
enum class ModelPriority {
    CRITICAL,  // Never unload automatically
    HIGH,      // Unload only under critical memory pressure
    NORMAL,    // Unload under low memory or after inactivity
    OPTIONAL   // Unload first when memory is needed
}

/**
 * Memory pressure levels
 */
enum class MemoryPressure {
    NORMAL,
    LOW,
    CRITICAL
}

/**
 * Model state sealed class
 */
sealed class ModelState {
    object Unloaded : ModelState()
    object Loading : ModelState()
    object Loaded : ModelState()
    data class Failed(val error: String) : ModelState()
}

/**
 * Loaded model information
 */
data class LoadedModelInfo(
    val modelType: ModelType,
    val loadedAt: Long,
    val lastAccessTime: Long,
    val memoryUsageMB: Long,
    val instance: Any?
)

/**
 * Model loader interface
 */
interface ModelLoader {
    suspend fun load(): Result<Any>
    suspend fun unload()
}
