package com.davidstudioz.david.ai.language

import android.content.Context
import com.davidstudioz.david.core.model.ModelDownloadManager
import com.davidstudioz.david.core.model.ModelLifecycleManager
import com.davidstudioz.david.core.model.ModelLoader
import com.davidstudioz.david.core.model.ModelType
import com.davidstudioz.david.core.model.ModelValidator
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enhanced Language Model Manager with:
 * - On-demand language pack downloads
 * - Cache only 2-3 most used languages
 * - Automatic cleanup of unused languages
 * - Lightweight multilingual model option (mBERT)
 * - User-selected language priority
 * - 80% storage reduction (750MB → 150MB)
 */
@Singleton
class LanguageModelManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val lifecycleManager: ModelLifecycleManager,
    private val validator: ModelValidator,
    private val downloadManager: ModelDownloadManager
) : ModelLoader {

    companion object {
        private const val TAG = "LanguageModelManager"
        private const val MAX_CACHED_LANGUAGES = 3
        private const val LANGUAGE_MODEL_SIZE_MB = 50L
        private const val MBERT_MODEL_SIZE_MB = 100L
        private const val CLEANUP_THRESHOLD_DAYS = 7
        private const val BASE_DOWNLOAD_URL = "https://models.nexuzy.in/languages/"
    }

    // Loaded language models
    private val loadedModels = mutableMapOf<Language, LanguageModelInstance>()
    private val languageUsageStats = mutableMapOf<Language, LanguageUsageStats>()
    
    // State flows
    private val _availableLanguages = MutableStateFlow<Set<Language>>(emptySet())
    val availableLanguages: StateFlow<Set<Language>> = _availableLanguages.asStateFlow()

    private val _loadedLanguages = MutableStateFlow<Set<Language>>(emptySet())
    val loadedLanguages: StateFlow<Set<Language>> = _loadedLanguages.asStateFlow()

    private val _downloadProgress = MutableStateFlow<Map<Language, Int>>(emptyMap())
    val downloadProgress: StateFlow<Map<Language, Int>> = _downloadProgress.asStateFlow()

    // Use lightweight mBERT for basic multilingual support
    private var mBertModel: Interpreter? = null
    private var isMBertLoaded = false

    init {
        lifecycleManager.registerModelLoader(ModelType.LANGUAGE_MODEL, this)
        initializeAvailableLanguages()
        loadUsageStats()
    }

    /**
     * Load the base language model (English)
     */
    override suspend fun load(): Result<Any> = withContext(Dispatchers.IO) {
        try {
            // Load mBERT as fallback for basic multilingual support
            loadMBertModel()
            
            // Load user's preferred language
            val preferredLanguage = getPreferredLanguage()
            loadLanguage(preferredLanguage)
            
            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Unload all language models
     */
    override suspend fun unload() = withContext(Dispatchers.IO) {
        loadedModels.values.forEach { it.interpreter.close() }
        loadedModels.clear()
        mBertModel?.close()
        mBertModel = null
        isMBertLoaded = false
        _loadedLanguages.value = emptySet()
    }

    /**
     * Load a specific language model
     */
    suspend fun loadLanguage(language: Language): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Check if already loaded
            if (loadedModels.containsKey(language)) {
                updateUsageStats(language)
                return@withContext Result.success(Unit)
            }

            // Check cache limit
            if (loadedModels.size >= MAX_CACHED_LANGUAGES) {
                unloadLeastUsedLanguage()
            }

            // Check if model file exists
            val modelFile = getLanguageModelFile(language)
            if (!modelFile.exists()) {
                // Download the language pack
                val downloadResult = downloadLanguagePack(language)
                if (downloadResult.isFailure) {
                    return@withContext downloadResult
                }
            }

            // Validate model
            val validationResult = validator.validateModel(modelFile, performLoadTest = false)
            if (validationResult.isFailed()) {
                return@withContext Result.failure(
                    Exception("Validation failed: ${validationResult.getErrorOrNull()?.message}")
                )
            }

            // Load model
            val modelBuffer = loadModelFile(modelFile)
            val options = Interpreter.Options().apply {
                setNumThreads(2)
                setUseXNNPACK(true)
            }
            val interpreter = Interpreter(modelBuffer, options)

            // Store loaded model
            loadedModels[language] = LanguageModelInstance(
                language = language,
                interpreter = interpreter,
                loadedAt = System.currentTimeMillis(),
                modelFile = modelFile
            )

            updateUsageStats(language)
            _loadedLanguages.value = loadedModels.keys

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Unload a specific language
     */
    suspend fun unloadLanguage(language: Language) = withContext(Dispatchers.IO) {
        loadedModels[language]?.interpreter?.close()
        loadedModels.remove(language)
        _loadedLanguages.value = loadedModels.keys
    }

    /**
     * Download language pack
     */
    private suspend fun downloadLanguagePack(language: Language): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val modelInfo = ModelDownloadManager.ModelInfo(
                id = "lang_${language.code}",
                name = "${language.name} Language Model",
                downloadUrl = "$BASE_DOWNLOAD_URL${language.code}_model.tflite",
                checksum = language.checksum,
                sizeBytes = LANGUAGE_MODEL_SIZE_MB * 1024 * 1024
            )

            // Subscribe to progress
            val progressFlow = downloadManager.getDownloadProgress(modelInfo.id)
            
            // Download
            val result = downloadManager.downloadModel(modelInfo)
            
            if (result.isSuccess) {
                Result.success(Unit)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Download failed"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Process text with language model
     */
    suspend fun processText(
        text: String,
        language: Language,
        task: LanguageTask
    ): Result<String> = withContext(Dispatchers.Default) {
        try {
            // Ensure language is loaded
            if (!loadedModels.containsKey(language)) {
                val loadResult = loadLanguage(language)
                if (loadResult.isFailure) {
                    // Fallback to mBERT
                    return@withContext processWithMBert(text, task)
                }
            }

            val model = loadedModels[language]
                ?: return@withContext processWithMBert(text, task)

            // Tokenize input
            val inputTokens = tokenize(text, language)

            // Prepare input buffer
            val inputBuffer = prepareInputBuffer(inputTokens)

            // Prepare output buffer
            val outputBuffer = ByteBuffer.allocateDirect(512 * 4).apply {
                order(ByteOrder.nativeOrder())
            }

            // Run inference
            model.interpreter.run(inputBuffer, outputBuffer)

            // Process output based on task
            val result = processOutput(outputBuffer, task, language)

            updateUsageStats(language)

            Result.success(result)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Detect language from text
     */
    suspend fun detectLanguage(text: String): Result<Language> = withContext(Dispatchers.Default) {
        try {
            // Use mBERT for language detection
            if (!isMBertLoaded) {
                loadMBertModel()
            }

            // Simple heuristic-based detection
            val detected = detectLanguageHeuristic(text)
            Result.success(detected)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Translate text between languages
     */
    suspend fun translate(
        text: String,
        fromLanguage: Language,
        toLanguage: Language
    ): Result<String> = withContext(Dispatchers.Default) {
        try {
            // Load both language models
            loadLanguage(fromLanguage)
            loadLanguage(toLanguage)

            // Encode in source language
            val encoded = processText(text, fromLanguage, LanguageTask.ENCODE)
                .getOrNull() ?: return@withContext Result.failure(Exception("Encoding failed"))

            // Decode in target language
            val decoded = processText(encoded, toLanguage, LanguageTask.DECODE)
                .getOrNull() ?: return@withContext Result.failure(Exception("Decoding failed"))

            Result.success(decoded)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get sentiment analysis
     */
    suspend fun analyzeSentiment(text: String, language: Language): Result<SentimentResult> = withContext(Dispatchers.Default) {
        try {
            val result = processText(text, language, LanguageTask.SENTIMENT)
                .getOrNull() ?: return@withContext Result.failure(Exception("Analysis failed"))

            // Parse sentiment result
            val score = result.toFloatOrNull() ?: 0.5f
            val sentiment = when {
                score > 0.6f -> Sentiment.POSITIVE
                score < 0.4f -> Sentiment.NEGATIVE
                else -> Sentiment.NEUTRAL
            }

            Result.success(SentimentResult(sentiment, score))

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Load mBERT model for fallback
     */
    private suspend fun loadMBertModel() = withContext(Dispatchers.IO) {
        if (isMBertLoaded) return@withContext

        try {
            val modelFile = File(context.filesDir, "models/mbert_multilingual.tflite")
            if (!modelFile.exists()) {
                // Copy from assets or download
                copyMBertFromAssets(modelFile)
            }

            val modelBuffer = loadModelFile(modelFile)
            val options = Interpreter.Options().apply {
                setNumThreads(2)
                setUseXNNPACK(true)
            }

            mBertModel = Interpreter(modelBuffer, options)
            isMBertLoaded = true

        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to load mBERT: ${e.message}")
        }
    }

    /**
     * Process with mBERT fallback
     */
    private suspend fun processWithMBert(text: String, task: LanguageTask): Result<String> = withContext(Dispatchers.Default) {
        try {
            if (!isMBertLoaded) {
                loadMBertModel()
            }

            val mBert = mBertModel
                ?: return@withContext Result.failure(Exception("mBERT not loaded"))

            // Simple processing with mBERT
            val inputTokens = tokenizeMultilingual(text)
            val inputBuffer = prepareInputBuffer(inputTokens)
            val outputBuffer = ByteBuffer.allocateDirect(512 * 4).apply {
                order(ByteOrder.nativeOrder())
            }

            mBert.run(inputBuffer, outputBuffer)

            val result = processOutput(outputBuffer, task, Language.ENGLISH)
            Result.success(result)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Unload least used language
     */
    private fun unloadLeastUsedLanguage() {
        val leastUsed = languageUsageStats.entries
            .filter { loadedModels.containsKey(it.key) }
            .minByOrNull { it.value.lastUsedAt }
            ?.key

        leastUsed?.let { language ->
            loadedModels[language]?.interpreter?.close()
            loadedModels.remove(language)
            _loadedLanguages.value = loadedModels.keys
        }
    }

    /**
     * Update usage statistics
     */
    private fun updateUsageStats(language: Language) {
        val stats = languageUsageStats.getOrPut(language) {
            LanguageUsageStats(language, 0, System.currentTimeMillis())
        }
        languageUsageStats[language] = stats.copy(
            usageCount = stats.usageCount + 1,
            lastUsedAt = System.currentTimeMillis()
        )
        saveUsageStats()
    }

    /**
     * Clean up unused language packs
     */
    suspend fun cleanupUnusedLanguages() = withContext(Dispatchers.IO) {
        val currentTime = System.currentTimeMillis()
        val thresholdTime = currentTime - (CLEANUP_THRESHOLD_DAYS * 24 * 60 * 60 * 1000L)

        val unusedLanguages = languageUsageStats.entries
            .filter { it.value.lastUsedAt < thresholdTime }
            .map { it.key }

        unusedLanguages.forEach { language ->
            val modelFile = getLanguageModelFile(language)
            if (modelFile.exists() && !loadedModels.containsKey(language)) {
                modelFile.delete()
                android.util.Log.d(TAG, "Deleted unused language pack: ${language.name}")
            }
        }
    }

    /**
     * Get total storage used by language models
     */
    fun getStorageUsageMB(): Long {
        val modelsDir = File(context.filesDir, "models")
        return modelsDir.listFiles()?.filter { it.name.startsWith("lang_") }
            ?.sumOf { it.length() } ?: 0L / (1024 * 1024)
    }

    /**
     * Get preferred language from settings
     */
    private fun getPreferredLanguage(): Language {
        val prefs = context.getSharedPreferences("language_prefs", Context.MODE_PRIVATE)
        val code = prefs.getString("preferred_language", "en") ?: "en"
        return Language.fromCode(code)
    }

    /**
     * Helper functions
     */
    private fun getLanguageModelFile(language: Language): File {
        return File(context.filesDir, "models/lang_${language.code}_model.tflite")
    }

    private fun loadModelFile(modelFile: File): MappedByteBuffer {
        FileInputStream(modelFile).use { inputStream ->
            val fileChannel = inputStream.channel
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size())
        }
    }

    private fun copyMBertFromAssets(targetFile: File) {
        targetFile.parentFile?.mkdirs()
        context.assets.open("mbert_multilingual.tflite").use { input ->
            targetFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    private fun tokenize(text: String, language: Language): List<Int> {
        // Placeholder - use proper tokenizer for each language
        return text.split(" ").map { it.hashCode() % 30000 }
    }

    private fun tokenizeMultilingual(text: String): List<Int> {
        // mBERT tokenization
        return text.split(" ").map { it.hashCode() % 30000 }
    }

    private fun prepareInputBuffer(tokens: List<Int>): ByteBuffer {
        return ByteBuffer.allocateDirect(tokens.size * 4).apply {
            order(ByteOrder.nativeOrder())
            tokens.forEach { putInt(it) }
            rewind()
        }
    }

    private fun processOutput(buffer: ByteBuffer, task: LanguageTask, language: Language): String {
        // Placeholder - process based on task
        return "processed_output"
    }

    private fun detectLanguageHeuristic(text: String): Language {
        // Simple heuristic-based language detection
        // In production, use proper language detection model
        return when {
            text.matches(Regex("[\u0980-\u09FF]+")) -> Language.BENGALI
            text.matches(Regex("[\u0900-\u097F]+")) -> Language.HINDI
            text.matches(Regex("[\u4E00-\u9FFF]+")) -> Language.CHINESE
            text.matches(Regex("[\u0600-\u06FF]+")) -> Language.ARABIC
            else -> Language.ENGLISH
        }
    }

    private fun initializeAvailableLanguages() {
        _availableLanguages.value = Language.values().toSet()
    }

    private fun loadUsageStats() {
        val prefs = context.getSharedPreferences("language_stats", Context.MODE_PRIVATE)
        Language.values().forEach { language ->
            val count = prefs.getInt("${language.code}_count", 0)
            val lastUsed = prefs.getLong("${language.code}_last_used", 0L)
            if (count > 0) {
                languageUsageStats[language] = LanguageUsageStats(language, count, lastUsed)
            }
        }
    }

    private fun saveUsageStats() {
        val prefs = context.getSharedPreferences("language_stats", Context.MODE_PRIVATE)
        prefs.edit().apply {
            languageUsageStats.forEach { (language, stats) ->
                putInt("${language.code}_count", stats.usageCount)
                putLong("${language.code}_last_used", stats.lastUsedAt)
            }
            apply()
        }
    }
}

/**
 * Language enum with all supported languages
 */
enum class Language(val code: String, val displayName: String, val checksum: String) {
    ENGLISH("en", "English", "abc123"),
    SPANISH("es", "Español", "def456"),
    FRENCH("fr", "Français", "ghi789"),
    GERMAN("de", "Deutsch", "jkl012"),
    ITALIAN("it", "Italiano", "mno345"),
    PORTUGUESE("pt", "Português", "pqr678"),
    RUSSIAN("ru", "Русский", "stu901"),
    CHINESE("zh", "中文", "vwx234"),
    JAPANESE("ja", "日本語", "yza567"),
    KOREAN("ko", "한국어", "bcd890"),
    ARABIC("ar", "العربية", "efg123"),
    HINDI("hi", "हिन्दी", "hij456"),
    BENGALI("bn", "বাংলা", "klm789"),
    TURKISH("tr", "Türkçe", "nop012"),
    DUTCH("nl", "Nederlands", "qrs345");

    companion object {
        fun fromCode(code: String): Language {
            return values().find { it.code == code } ?: ENGLISH
        }
    }
}

/**
 * Language processing tasks
 */
enum class LanguageTask {
    ENCODE,
    DECODE,
    SENTIMENT,
    TRANSLATE,
    SUMMARIZE,
    CLASSIFY
}

/**
 * Sentiment enum
 */
enum class Sentiment {
    POSITIVE,
    NEUTRAL,
    NEGATIVE
}

/**
 * Language model instance
 */
data class LanguageModelInstance(
    val language: Language,
    val interpreter: Interpreter,
    val loadedAt: Long,
    val modelFile: File
)

/**
 * Language usage statistics
 */
data class LanguageUsageStats(
    val language: Language,
    val usageCount: Int,
    val lastUsedAt: Long
)

/**
 * Sentiment result
 */
data class SentimentResult(
    val sentiment: Sentiment,
    val score: Float
)
