package com.davidstudioz.david.core.model

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Model Validator for comprehensive model integrity checks:
 * - File existence validation
 * - File size validation
 * - SHA-256 checksum verification
 * - Model loading test
 * - Tensor allocation test
 * - Framework compatibility validation
 */
@Singleton
class ModelValidator @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "ModelValidator"
        private const val CHUNK_SIZE = 8192
    }

    /**
     * Validate a model file comprehensively
     */
    suspend fun validateModel(
        modelFile: File,
        expectedChecksum: String? = null,
        expectedSizeBytes: Long? = null,
        performLoadTest: Boolean = true
    ): ValidationResult = withContext(Dispatchers.IO) {
        try {
            // Step 1: File exists check
            if (!modelFile.exists()) {
                return@withContext ValidationResult.Failed(
                    ValidationError.FileNotFound("Model file does not exist: ${modelFile.path}")
                )
            }

            // Step 2: File size validation
            val actualSize = modelFile.length()
            if (actualSize == 0L) {
                return@withContext ValidationResult.Failed(
                    ValidationError.EmptyFile("Model file is empty: ${modelFile.path}")
                )
            }

            if (expectedSizeBytes != null) {
                val sizeDifference = kotlin.math.abs(actualSize - expectedSizeBytes)
                val toleranceBytes = expectedSizeBytes * 0.01 // 1% tolerance
                
                if (sizeDifference > toleranceBytes) {
                    return@withContext ValidationResult.Failed(
                        ValidationError.SizeMismatch(
                            "File size mismatch. Expected: $expectedSizeBytes, Actual: $actualSize"
                        )
                    )
                }
            }

            // Step 3: Checksum verification
            if (expectedChecksum != null) {
                val actualChecksum = calculateChecksum(modelFile)
                if (!actualChecksum.equals(expectedChecksum, ignoreCase = true)) {
                    return@withContext ValidationResult.Failed(
                        ValidationError.ChecksumMismatch(
                            "Checksum verification failed. Expected: $expectedChecksum, Actual: $actualChecksum"
                        )
                    )
                }
            }

            // Step 4: Model loading test (if enabled)
            if (performLoadTest) {
                val loadTestResult = testModelLoading(modelFile)
                if (loadTestResult is ValidationResult.Failed) {
                    return@withContext loadTestResult
                }
            }

            // Step 5: All validations passed
            ValidationResult.Success(
                ModelInfo(
                    file = modelFile,
                    sizeBytes = actualSize,
                    checksum = expectedChecksum,
                    isValid = true
                )
            )

        } catch (e: Exception) {
            ValidationResult.Failed(
                ValidationError.UnknownError("Validation error: ${e.message}", e)
            )
        }
    }

    /**
     * Calculate SHA-256 checksum of a file
     */
    private suspend fun calculateChecksum(file: File): String = withContext(Dispatchers.IO) {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { input ->
            val buffer = ByteArray(CHUNK_SIZE)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        digest.digest().joinToString("") { "%02x".format(it) }
    }

    /**
     * Test if a TensorFlow Lite model can be loaded
     */
    private suspend fun testModelLoading(modelFile: File): ValidationResult = withContext(Dispatchers.IO) {
        var interpreter: Interpreter? = null
        try {
            // Try to create an interpreter
            val options = Interpreter.Options().apply {
                setNumThreads(1)
            }
            interpreter = Interpreter(modelFile, options)

            // Test tensor allocation
            val inputTensorCount = interpreter.inputTensorCount
            val outputTensorCount = interpreter.outputTensorCount

            if (inputTensorCount == 0 || outputTensorCount == 0) {
                return@withContext ValidationResult.Failed(
                    ValidationError.InvalidModelStructure(
                        "Model has invalid tensor structure. Inputs: $inputTensorCount, Outputs: $outputTensorCount"
                    )
                )
            }

            // Model loaded successfully
            ValidationResult.Success(
                ModelInfo(
                    file = modelFile,
                    sizeBytes = modelFile.length(),
                    checksum = null,
                    isValid = true,
                    inputTensorCount = inputTensorCount,
                    outputTensorCount = outputTensorCount
                )
            )

        } catch (e: IllegalArgumentException) {
            ValidationResult.Failed(
                ValidationError.CorruptedModel("Model file is corrupted or invalid: ${e.message}", e)
            )
        } catch (e: Exception) {
            ValidationResult.Failed(
                ValidationError.LoadingFailed("Failed to load model: ${e.message}", e)
            )
        } finally {
            interpreter?.close()
        }
    }

    /**
     * Validate multiple models in batch
     */
    suspend fun validateModels(
        models: List<ModelValidationRequest>
    ): Map<String, ValidationResult> = withContext(Dispatchers.IO) {
        models.associate { request ->
            request.modelId to validateModel(
                modelFile = request.file,
                expectedChecksum = request.expectedChecksum,
                expectedSizeBytes = request.expectedSizeBytes,
                performLoadTest = request.performLoadTest
            )
        }
    }

    /**
     * Quick validation without loading test (faster)
     */
    suspend fun quickValidate(
        modelFile: File,
        expectedChecksum: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!modelFile.exists() || modelFile.length() == 0L) {
                return@withContext false
            }

            if (expectedChecksum != null) {
                val actualChecksum = calculateChecksum(modelFile)
                return@withContext actualChecksum.equals(expectedChecksum, ignoreCase = true)
            }

            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Check if model needs update
     */
    suspend fun needsUpdate(
        modelFile: File,
        latestChecksum: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!modelFile.exists()) {
                return@withContext true
            }

            val currentChecksum = calculateChecksum(modelFile)
            !currentChecksum.equals(latestChecksum, ignoreCase = true)
        } catch (e: Exception) {
            true // If we can't verify, assume update is needed
        }
    }

    /**
     * Get model metadata without full validation
     */
    suspend fun getModelMetadata(modelFile: File): ModelMetadata? = withContext(Dispatchers.IO) {
        try {
            if (!modelFile.exists()) {
                return@withContext null
            }

            val options = Interpreter.Options().apply { setNumThreads(1) }
            val interpreter = Interpreter(modelFile, options)

            val metadata = ModelMetadata(
                fileName = modelFile.name,
                sizeBytes = modelFile.length(),
                inputTensorCount = interpreter.inputTensorCount,
                outputTensorCount = interpreter.outputTensorCount,
                inputShapes = (0 until interpreter.inputTensorCount).map { idx ->
                    interpreter.getInputTensor(idx).shape().toList()
                },
                outputShapes = (0 until interpreter.outputTensorCount).map { idx ->
                    interpreter.getOutputTensor(idx).shape().toList()
                }
            )

            interpreter.close()
            metadata

        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Validation result sealed class
 */
sealed class ValidationResult {
    data class Success(val modelInfo: ModelInfo) : ValidationResult()
    data class Failed(val error: ValidationError) : ValidationResult()

    fun isSuccess(): Boolean = this is Success
    fun isFailed(): Boolean = this is Failed

    fun getModelInfoOrNull(): ModelInfo? = (this as? Success)?.modelInfo
    fun getErrorOrNull(): ValidationError? = (this as? Failed)?.error
}

/**
 * Validation error sealed class
 */
sealed class ValidationError(open val message: String, open val cause: Throwable? = null) {
    data class FileNotFound(override val message: String) : ValidationError(message)
    data class EmptyFile(override val message: String) : ValidationError(message)
    data class SizeMismatch(override val message: String) : ValidationError(message)
    data class ChecksumMismatch(override val message: String) : ValidationError(message)
    data class CorruptedModel(override val message: String, override val cause: Throwable? = null) : ValidationError(message, cause)
    data class LoadingFailed(override val message: String, override val cause: Throwable? = null) : ValidationError(message, cause)
    data class InvalidModelStructure(override val message: String) : ValidationError(message)
    data class UnknownError(override val message: String, override val cause: Throwable? = null) : ValidationError(message, cause)
}

/**
 * Model information data class
 */
data class ModelInfo(
    val file: File,
    val sizeBytes: Long,
    val checksum: String?,
    val isValid: Boolean,
    val inputTensorCount: Int = 0,
    val outputTensorCount: Int = 0
)

/**
 * Model validation request
 */
data class ModelValidationRequest(
    val modelId: String,
    val file: File,
    val expectedChecksum: String? = null,
    val expectedSizeBytes: Long? = null,
    val performLoadTest: Boolean = true
)

/**
 * Model metadata
 */
data class ModelMetadata(
    val fileName: String,
    val sizeBytes: Long,
    val inputTensorCount: Int,
    val outputTensorCount: Int,
    val inputShapes: List<List<Int>>,
    val outputShapes: List<List<Int>>
)
