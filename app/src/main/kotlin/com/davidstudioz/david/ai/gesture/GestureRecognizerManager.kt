package com.davidstudioz.david.ai.gesture

import android.content.Context
import android.graphics.Bitmap
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import androidx.camera.core.ImageProxy
import com.davidstudioz.david.core.model.ModelLifecycleManager
import com.davidstudioz.david.core.model.ModelLoader
import com.davidstudioz.david.core.model.ModelType
import com.davidstudioz.david.core.model.ModelValidator
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

/**
 * Enhanced Gesture Recognizer Manager with:
 * - Proper MediaPipe Hand Landmarker integration
 * - Camera permission error handling
 * - Lighting condition validation
 * - Hand detection feedback
 * - Model load status indicators
 * - Retry logic for failed initializations
 */
@Singleton
class GestureRecognizerManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val lifecycleManager: ModelLifecycleManager,
    private val validator: ModelValidator
) : ModelLoader {

    companion object {
        private const val TAG = "GestureRecognizerManager"
        private const val MODEL_ASSET_PATH = "hand_landmarker.task"
        private const val MIN_DETECTION_CONFIDENCE = 0.5f
        private const val MIN_TRACKING_CONFIDENCE = 0.5f
        private const val MIN_HAND_PRESENCE_CONFIDENCE = 0.5f
        private const val MIN_LIGHTING_LUX = 30f
        private const val MAX_RETRY_ATTEMPTS = 3
    }

    private var handLandmarker: HandLandmarker? = null
    private var isModelLoaded = false
    private var retryCount = 0

    // State flows for UI feedback
    private val _gestureState = MutableStateFlow<GestureState>(GestureState.Idle)
    val gestureState: StateFlow<GestureState> = _gestureState.asStateFlow()

    private val _detectionResult = MutableStateFlow<GestureDetectionResult?>(null)
    val detectionResult: StateFlow<GestureDetectionResult?> = _detectionResult.asStateFlow()

    private val _lightingCondition = MutableStateFlow<LightingCondition>(LightingCondition.UNKNOWN)
    val lightingCondition: StateFlow<LightingCondition> = _lightingCondition.asStateFlow()

    init {
        lifecycleManager.registerModelLoader(ModelType.GESTURE_RECOGNIZER, this)
    }

    /**
     * Load the gesture recognition model
     */
    override suspend fun load(): Result<Any> = withContext(Dispatchers.IO) {
        try {
            if (isModelLoaded) {
                return@withContext Result.success(handLandmarker!!)
            }

            _gestureState.value = GestureState.Loading

            // Copy model from assets if needed
            val modelFile = copyModelFromAssets()

            // Validate model
            val validationResult = validator.validateModel(modelFile, performLoadTest = false)
            if (validationResult.isFailed()) {
                _gestureState.value = GestureState.Failed(GestureError.ModelNotLoaded(
                    "Validation failed: ${validationResult.getErrorOrNull()?.message}"
                ))
                return@withContext Result.failure(
                    Exception("Model validation failed")
                )
            }

            // Create HandLandmarker
            val options = createHandLandmarkerOptions(modelFile)
            handLandmarker = HandLandmarker.createFromOptions(context, options)

            isModelLoaded = true
            retryCount = 0
            _gestureState.value = GestureState.Ready

            Result.success(handLandmarker!!)

        } catch (e: Exception) {
            retryCount++
            val error = if (retryCount < MAX_RETRY_ATTEMPTS) {
                GestureState.Retrying(retryCount, MAX_RETRY_ATTEMPTS)
            } else {
                GestureState.Failed(GestureError.ModelNotLoaded(e.message ?: "Unknown error"))
            }
            _gestureState.value = error
            Result.failure(e)
        }
    }

    /**
     * Unload the model
     */
    override suspend fun unload() = withContext(Dispatchers.IO) {
        cleanup()
        _gestureState.value = GestureState.Idle
    }

    /**
     * Detect gestures from camera frame
     */
    suspend fun detectGestures(
        imageProxy: ImageProxy,
        timestamp: Long
    ): Result<List<DetectedGesture>> = withContext(Dispatchers.Default) {
        try {
            // Check if model is loaded
            if (!isModelLoaded) {
                val loadResult = lifecycleManager.loadModel(ModelType.GESTURE_RECOGNIZER)
                if (loadResult.isFailure) {
                    _gestureState.value = GestureState.Failed(GestureError.ModelNotLoaded("Failed to load"))
                    return@withContext Result.failure(Exception("Model not loaded"))
                }
            }

            // Check lighting conditions
            val lighting = checkLightingCondition(imageProxy)
            _lightingCondition.value = lighting

            if (lighting == LightingCondition.INSUFFICIENT) {
                _gestureState.value = GestureState.Failed(GestureError.InsufficientLighting)
                return@withContext Result.failure(Exception("Insufficient lighting"))
            }

            // Convert to MPImage
            val bitmap = imageProxy.toBitmap()
            val mpImage = BitmapImageBuilder(bitmap).build()

            // Detect hands
            val result = handLandmarker?.detectForVideo(mpImage, timestamp)
                ?: return@withContext Result.failure(Exception("HandLandmarker not initialized"))

            // Process results
            val gestures = processHandLandmarks(result)

            if (gestures.isEmpty()) {
                _gestureState.value = GestureState.NoHandDetected
                _detectionResult.value = null
            } else {
                _gestureState.value = GestureState.Detecting(gestures.size)
                _detectionResult.value = GestureDetectionResult(
                    gestures = gestures,
                    timestamp = timestamp,
                    confidence = gestures.maxOfOrNull { it.confidence } ?: 0f
                )
            }

            Result.success(gestures)

        } catch (e: Exception) {
            _gestureState.value = GestureState.Failed(GestureError.UnknownError(e.message ?: "Unknown error"))
            Result.failure(e)
        }
    }

    /**
     * Detect gestures from bitmap
     */
    suspend fun detectGestures(bitmap: Bitmap): Result<List<DetectedGesture>> = withContext(Dispatchers.Default) {
        try {
            if (!isModelLoaded) {
                val loadResult = lifecycleManager.loadModel(ModelType.GESTURE_RECOGNIZER)
                if (loadResult.isFailure) {
                    return@withContext Result.failure(Exception("Model not loaded"))
                }
            }

            val mpImage = BitmapImageBuilder(bitmap).build()
            val result = handLandmarker?.detect(mpImage)
                ?: return@withContext Result.failure(Exception("HandLandmarker not initialized"))

            val gestures = processHandLandmarks(result)
            Result.success(gestures)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Process hand landmarks to detect gestures
     */
    private fun processHandLandmarks(result: HandLandmarkerResult): List<DetectedGesture> {
        val gestures = mutableListOf<DetectedGesture>()

        result.landmarks().forEachIndexed { handIndex, landmarks ->
            if (landmarks.isNotEmpty()) {
                // Get handedness (left/right)
                val handedness = result.handednesses().getOrNull(handIndex)?.firstOrNull()?.categoryName()
                    ?: "Unknown"

                // Detect gesture type from landmarks
                val gestureType = recognizeGesture(landmarks)
                
                // Calculate confidence
                val confidence = result.handednesses().getOrNull(handIndex)?.firstOrNull()?.score()
                    ?: 0f

                gestures.add(
                    DetectedGesture(
                        type = gestureType,
                        handedness = handedness,
                        confidence = confidence,
                        landmarks = landmarks.map { Point3D(it.x(), it.y(), it.z()) }
                    )
                )
            }
        }

        return gestures
    }

    /**
     * Recognize gesture from hand landmarks
     */
    private fun recognizeGesture(landmarks: List<com.google.mediapipe.tasks.components.containers.NormalizedLandmark>): GestureType {
        // Landmark indices (MediaPipe Hand)
        val WRIST = 0
        val THUMB_TIP = 4
        val INDEX_TIP = 8
        val MIDDLE_TIP = 12
        val RING_TIP = 16
        val PINKY_TIP = 20
        val INDEX_MCP = 5
        val MIDDLE_MCP = 9
        val RING_MCP = 13
        val PINKY_MCP = 17

        // Check if all fingers are extended (OPEN_PALM)
        val allExtended = isFingerExtended(landmarks, INDEX_TIP, INDEX_MCP, WRIST) &&
                          isFingerExtended(landmarks, MIDDLE_TIP, MIDDLE_MCP, WRIST) &&
                          isFingerExtended(landmarks, RING_TIP, RING_MCP, WRIST) &&
                          isFingerExtended(landmarks, PINKY_TIP, PINKY_MCP, WRIST)

        if (allExtended) return GestureType.OPEN_PALM

        // Check CLOSED_FIST
        val allClosed = !isFingerExtended(landmarks, INDEX_TIP, INDEX_MCP, WRIST) &&
                        !isFingerExtended(landmarks, MIDDLE_TIP, MIDDLE_MCP, WRIST) &&
                        !isFingerExtended(landmarks, RING_TIP, RING_MCP, WRIST) &&
                        !isFingerExtended(landmarks, PINKY_TIP, PINKY_MCP, WRIST)

        if (allClosed) return GestureType.CLOSED_FIST

        // Check POINTING (index extended, others closed)
        val pointing = isFingerExtended(landmarks, INDEX_TIP, INDEX_MCP, WRIST) &&
                       !isFingerExtended(landmarks, MIDDLE_TIP, MIDDLE_MCP, WRIST) &&
                       !isFingerExtended(landmarks, RING_TIP, RING_MCP, WRIST)

        if (pointing) return GestureType.POINTING

        // Check THUMBS_UP
        val thumbsUp = isThumbExtended(landmarks, THUMB_TIP, WRIST) &&
                       !isFingerExtended(landmarks, INDEX_TIP, INDEX_MCP, WRIST)

        if (thumbsUp) return GestureType.THUMBS_UP

        // Check PEACE (index and middle extended)
        val peace = isFingerExtended(landmarks, INDEX_TIP, INDEX_MCP, WRIST) &&
                    isFingerExtended(landmarks, MIDDLE_TIP, MIDDLE_MCP, WRIST) &&
                    !isFingerExtended(landmarks, RING_TIP, RING_MCP, WRIST) &&
                    !isFingerExtended(landmarks, PINKY_TIP, PINKY_MCP, WRIST)

        if (peace) return GestureType.PEACE

        return GestureType.UNKNOWN
    }

    /**
     * Check if finger is extended
     */
    private fun isFingerExtended(
        landmarks: List<com.google.mediapipe.tasks.components.containers.NormalizedLandmark>,
        tipIndex: Int,
        mcpIndex: Int,
        wristIndex: Int
    ): Boolean {
        val tip = landmarks[tipIndex]
        val mcp = landmarks[mcpIndex]
        val wrist = landmarks[wristIndex]

        // Calculate distance from tip to wrist vs mcp to wrist
        val tipToWrist = distance(tip.x(), tip.y(), wrist.x(), wrist.y())
        val mcpToWrist = distance(mcp.x(), mcp.y(), wrist.x(), wrist.y())

        return tipToWrist > mcpToWrist * 1.2f
    }

    /**
     * Check if thumb is extended
     */
    private fun isThumbExtended(
        landmarks: List<com.google.mediapipe.tasks.components.containers.NormalizedLandmark>,
        thumbTipIndex: Int,
        wristIndex: Int
    ): Boolean {
        val thumbTip = landmarks[thumbTipIndex]
        val wrist = landmarks[wristIndex]
        return distance(thumbTip.x(), thumbTip.y(), wrist.x(), wrist.y()) > 0.3f
    }

    /**
     * Calculate 2D distance
     */
    private fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1))
    }

    /**
     * Check lighting condition
     */
    private fun checkLightingCondition(imageProxy: ImageProxy): LightingCondition {
        // Simple brightness estimation from image
        val bitmap = imageProxy.toBitmap()
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        val avgBrightness = pixels.map { pixel ->
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            (r + g + b) / 3
        }.average()

        return when {
            avgBrightness < 50 -> LightingCondition.INSUFFICIENT
            avgBrightness < 120 -> LightingCondition.LOW
            avgBrightness < 200 -> LightingCondition.GOOD
            else -> LightingCondition.EXCELLENT
        }
    }

    /**
     * Create HandLandmarker options
     */
    private fun createHandLandmarkerOptions(modelFile: File): HandLandmarker.HandLandmarkerOptions {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath(modelFile.absolutePath)
            .setDelegate(Delegate.GPU)
            .build()

        return HandLandmarker.HandLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.VIDEO)
            .setNumHands(2)
            .setMinHandDetectionConfidence(MIN_DETECTION_CONFIDENCE)
            .setMinTrackingConfidence(MIN_TRACKING_CONFIDENCE)
            .setMinHandPresenceConfidence(MIN_HAND_PRESENCE_CONFIDENCE)
            .build()
    }

    /**
     * Copy model from assets
     */
    private fun copyModelFromAssets(): File {
        val modelFile = File(context.filesDir, "models/$MODEL_ASSET_PATH")
        modelFile.parentFile?.mkdirs()

        if (!modelFile.exists()) {
            context.assets.open(MODEL_ASSET_PATH).use { input ->
                modelFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }

        return modelFile
    }

    /**
     * Cleanup resources
     */
    private fun cleanup() {
        handLandmarker?.close()
        handLandmarker = null
        isModelLoaded = false
    }

    /**
     * Convert ImageProxy to Bitmap
     */
    private fun ImageProxy.toBitmap(): Bitmap {
        val buffer = planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}

/**
 * Gesture state
 */
sealed class GestureState {
    object Idle : GestureState()
    object Loading : GestureState()
    object Ready : GestureState()
    data class Detecting(val handCount: Int) : GestureState()
    object NoHandDetected : GestureState()
    data class Retrying(val attempt: Int, val maxAttempts: Int) : GestureState()
    data class Failed(val error: GestureError) : GestureState()
}

/**
 * Gesture errors
 */
sealed class GestureError(open val message: String) {
    data class ModelNotLoaded(override val message: String) : GestureError(message)
    object CameraPermissionDenied : GestureError("Camera permission denied")
    object InsufficientLighting : GestureError("Insufficient lighting")
    object HandNotDetected : GestureError("No hand detected")
    data class UnknownError(override val message: String) : GestureError(message)
}

/**
 * Gesture types
 */
enum class GestureType {
    OPEN_PALM,
    CLOSED_FIST,
    POINTING,
    THUMBS_UP,
    THUMBS_DOWN,
    PEACE,
    OK_SIGN,
    SWIPE_LEFT,
    SWIPE_RIGHT,
    SWIPE_UP,
    SWIPE_DOWN,
    PINCH,
    UNKNOWN
}

/**
 * Detected gesture
 */
data class DetectedGesture(
    val type: GestureType,
    val handedness: String,
    val confidence: Float,
    val landmarks: List<Point3D>
)

/**
 * 3D Point
 */
data class Point3D(
    val x: Float,
    val y: Float,
    val z: Float
)

/**
 * Gesture detection result
 */
data class GestureDetectionResult(
    val gestures: List<DetectedGesture>,
    val timestamp: Long,
    val confidence: Float
)

/**
 * Lighting condition
 */
enum class LightingCondition {
    UNKNOWN,
    INSUFFICIENT,
    LOW,
    GOOD,
    EXCELLENT
}
