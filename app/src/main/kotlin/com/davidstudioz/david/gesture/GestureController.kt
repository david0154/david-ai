package com.davidstudioz.david.gesture

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.Color as AndroidColor

/**
 * Gesture Recognition Controller
 * Detects hand gestures, tracks hand position, and controls device via hand pointer
 * Uses Google MediaPipe Hand Landmark detection
 */
class GestureController(
    private val context: Context,
    private val onGestureDetected: (GestureType, String) -> Unit
) {

    // Gesture types
    enum class GestureType {
        OPEN_PALM,          // All fingers open
        CLOSED_FIST,        // All fingers closed
        POINTING,           // Index finger pointing
        THUMBS_UP,          // Thumb up
        VICTORY,            // V sign (2 fingers)
        SWIPE_LEFT,         // Hand moves left
        SWIPE_RIGHT,        // Hand moves right
        PINCH,              // Thumb + index pinch
        GRAB,               // Grabbing motion
        SINGLE_TAP,         // Single tap
        DOUBLE_TAP,         // Double tap
        LONG_PRESS,         // Long press
        UNKNOWN             // Unknown gesture
    }

    // Hand tracking data
    data class HandPosition(
        val x: Float,
        val y: Float,
        val confidence: Float,
        val landmarkCount: Int
    )

    private val TAG = "GestureController"
    private var handLandmarker: HandLandmarker? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageAnalysis: ImageAnalysis? = null
    private var lastGesture: GestureType = GestureType.UNKNOWN
    private var gestureConfidence = 0f
    private var tapCounter = 0
    private var lastTapTime = 0L
    private var lastHandPosition: HandPosition? = null
    private var handMovementHistory = mutableListOf<Pair<Float, Float>>()

    init {
        initializeHandLandmarker()
    }

    /**
     * Initialize MediaPipe Hand Landmarker
     */
    private fun initializeHandLandmarker() {
        try {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath("hand_landmarker.task")
                .build()

            handLandmarker = HandLandmarker.createFromOptions(context, baseOptions)
            Log.d(TAG, "Hand Landmarker initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Hand Landmarker", e)
        }
    }

    /**
     * Start gesture recognition with camera
     */
    fun startCameraGestureRecognition(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        if (!hasCameraPermission()) {
            Log.w(TAG, "Camera permission not granted")
            return
        }

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.result
                this.cameraProvider = cameraProvider

                // Preview
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                // Image Analysis for gesture detection
                imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                    .build()
                    .also { imageAnalysis ->
                        imageAnalysis.setAnalyzer(
                            context.mainExecutor,
                            GestureAnalyzer { gesture, confidence, position ->
                                handleDetectedGesture(gesture, confidence, position)
                            }
                        )
                    }

                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )

                Log.d(TAG, "Camera gesture recognition started")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start camera", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * Handle detected gesture
     */
    private fun handleDetectedGesture(
        gesture: GestureType,
        confidence: Float,
        position: HandPosition
    ) {
        lastHandPosition = position
        gestureConfidence = confidence

        // Track hand movement for swipe detection
        trackHandMovement(position.x, position.y)

        // Detect multi-tap
        if (gesture == GestureType.SINGLE_TAP) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastTapTime < 300) { // Double tap within 300ms
                tapCounter++
                if (tapCounter >= 2) {
                    onGestureDetected(GestureType.DOUBLE_TAP, "Double tap at (${position.x}, ${position.y})")
                    tapCounter = 0
                }
            } else {
                tapCounter = 1
            }
            lastTapTime = currentTime
        }

        // Swipe detection
        val swipeGesture = detectSwipe()
        if (swipeGesture != null) {
            onGestureDetected(swipeGesture, "Swipe detected")
            lastGesture = swipeGesture
            return
        }

        // Regular gesture
        if (gesture != lastGesture && confidence > 0.7f) {
            lastGesture = gesture
            onGestureDetected(
                gesture,
                "${gesture.name} (Confidence: $confidence) at (${position.x}, ${position.y})"
            )
        }
    }

    /**
     * Track hand movement for swipe detection
     */
    private fun trackHandMovement(x: Float, y: Float) {
        handMovementHistory.add(Pair(x, y))
        if (handMovementHistory.size > 20) {
            handMovementHistory.removeAt(0)
        }
    }

    /**
     * Detect swipe gesture from movement history
     */
    private fun detectSwipe(): GestureType? {
        if (handMovementHistory.size < 10) return null

        val firstPoint = handMovementHistory[0]
        val lastPoint = handMovementHistory.last()

        val deltaX = lastPoint.first - firstPoint.first
        val deltaY = lastPoint.second - firstPoint.second
        val distance = kotlin.math.sqrt((deltaX * deltaX + deltaY * deltaY).toDouble()).toFloat()

        // Minimum swipe distance
        if (distance < 50f) return null

        // Swipe direction
        return when {
            deltaX < -50 && kotlin.math.abs(deltaY) < 30 -> GestureType.SWIPE_LEFT
            deltaX > 50 && kotlin.math.abs(deltaY) < 30 -> GestureType.SWIPE_RIGHT
            else -> null
        }
    }

    /**
     * Get hand position (for pointer control)
     */
    fun getHandPosition(): HandPosition? {
        return lastHandPosition
    }

    /**
     * Classify gesture from hand landmarks
     */
    fun classifyGesture(landmarks: List<Float>): Pair<GestureType, Float> {
        if (landmarks.size < 21 * 3) return Pair(GestureType.UNKNOWN, 0f)

        // Extract key points
        val thumbTip = FloatArray(3) { i -> landmarks[4 * 3 + i] }    // Thumb
        val indexTip = FloatArray(3) { i -> landmarks[8 * 3 + i] }    // Index
        val middleTip = FloatArray(3) { i -> landmarks[12 * 3 + i] }  // Middle
        val ringTip = FloatArray(3) { i -> landmarks[16 * 3 + i] }    // Ring
        val pinkyTip = FloatArray(3) { i -> landmarks[20 * 3 + i] }   // Pinky
        val wrist = FloatArray(3) { i -> landmarks[0 * 3 + i] }       // Wrist

        // Calculate distances
        val thumbToWrist = distance(thumbTip, wrist)
        val indexToWrist = distance(indexTip, wrist)
        val middleToWrist = distance(middleTip, wrist)
        val ringToWrist = distance(ringTip, wrist)
        val pinkyToWrist = distance(pinkyTip, wrist)

        // Calculate finger openness (compared to wrist)
        val avgDistance = (thumbToWrist + indexToWrist + middleToWrist + ringToWrist + pinkyToWrist) / 5f

        return when {
            // Open palm: all fingers extended
            thumbToWrist > 50 && indexToWrist > 50 && middleToWrist > 50 && ringToWrist > 50 && pinkyToWrist > 50 ->
                Pair(GestureType.OPEN_PALM, 0.9f)

            // Closed fist: all fingers near wrist
            thumbToWrist < 30 && indexToWrist < 30 && middleToWrist < 30 && ringToWrist < 30 && pinkyToWrist < 30 ->
                Pair(GestureType.CLOSED_FIST, 0.9f)

            // Pointing: only index finger extended
            indexToWrist > 50 && thumbToWrist < 35 && middleToWrist < 35 && ringToWrist < 35 && pinkyToWrist < 35 ->
                Pair(GestureType.POINTING, 0.85f)

            // Thumbs up: thumb extended upward
            thumbToWrist > 60 && indexToWrist < 30 && middleToWrist < 30 && ringToWrist < 30 && pinkyToWrist < 30 ->
                Pair(GestureType.THUMBS_UP, 0.8f)

            // Victory (V sign): index and middle extended
            indexToWrist > 50 && middleToWrist > 50 && thumbToWrist < 30 && ringToWrist < 30 && pinkyToWrist < 30 ->
                Pair(GestureType.VICTORY, 0.8f)

            // Pinch: thumb and index very close
            distance(thumbTip, indexTip) < 20 ->
                Pair(GestureType.PINCH, 0.8f)

            else -> Pair(GestureType.UNKNOWN, 0.5f)
        }
    }

    /**
     * Calculate Euclidean distance between two 3D points
     */
    private fun distance(p1: FloatArray, p2: FloatArray): Float {
        val dx = p1[0] - p2[0]
        val dy = p1[1] - p2[1]
        val dz = p1[2] - p2[2]
        return kotlin.math.sqrt((dx * dx + dy * dy + dz * dz).toDouble()).toFloat()
    }

    /**
     * Draw hand landmarks on canvas for visualization
     */
    fun drawHandLandmarks(bitmap: Bitmap, landmarks: List<Float>): Bitmap {
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            color = AndroidColor.GREEN
            strokeWidth = 2f
            isAntiAlias = true
        }

        val radiusPixels = 8f
        for (i in 0 until landmarks.size step 3) {
            val x = landmarks[i] * bitmap.width
            val y = landmarks[i + 1] * bitmap.height
            canvas.drawCircle(x, y, radiusPixels, paint)
        }

        // Draw connections (hand skeleton)
        val connections = intArrayOf(
            0, 1, 1, 2, 2, 3, 3, 4,       // Thumb
            5, 6, 6, 7, 7, 8,             // Index
            9, 10, 10, 11, 11, 12,        // Middle
            13, 14, 14, 15, 15, 16,       // Ring
            17, 18, 18, 19, 19, 20,       // Pinky
            0, 5, 5, 9, 9, 13, 13, 17    // Palm
        )

        paint.color = AndroidColor.BLUE
        paint.strokeWidth = 1f
        for (i in 0 until connections.size step 2) {
            val startIdx = connections[i] * 3
            val endIdx = connections[i + 1] * 3
            if (startIdx + 2 < landmarks.size && endIdx + 2 < landmarks.size) {
                val x1 = landmarks[startIdx] * bitmap.width
                val y1 = landmarks[startIdx + 1] * bitmap.height
                val x2 = landmarks[endIdx] * bitmap.width
                val y2 = landmarks[endIdx + 1] * bitmap.height
                canvas.drawLine(x1, y1, x2, y2, paint)
            }
        }

        return bitmap
    }

    /**
     * Get gesture statistics
     */
    fun getGestureStats(): Map<String, Any> {
        return mapOf(
            "lastGesture" to lastGesture.name,
            "confidence" to gestureConfidence,
            "handPosition" to (lastHandPosition?.toString() ?: "Not detected"),
            "movementHistorySize" to handMovementHistory.size
        )
    }

    /**
     * Check camera permission
     */
    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Stop camera and release resources
     */
    fun stopCameraGestureRecognition() {
        cameraProvider?.unbindAll()
        Log.d(TAG, "Camera gesture recognition stopped")
    }

    /**
     * Release all resources
     */
    fun release() {
        handLandmarker?.close()
        stopCameraGestureRecognition()
    }
}

/**
 * Image analyzer for real-time gesture detection
 */
class GestureAnalyzer(
    private val onGestureDetected: (GestureController.GestureType, Float, GestureController.HandPosition) -> Unit
) : ImageAnalysis.Analyzer {

    private val gestureController = GestureController(android.app.Application(), { _, _ -> })

    override fun analyze(image: ImageProxy) {
        val bitmap = image.toBitmap()
        // Process gesture detection here
        image.close()
    }

    private fun ImageProxy.toBitmap(): Bitmap {
        val ySize = planes[0].buffer.remaining()
        val uvSize = planes[1].buffer.remaining()
        val nv21 = ByteArray(ySize + uvSize)

        planes[0].buffer.get(nv21, 0, ySize)
        planes[1].buffer.get(nv21, ySize, uvSize)

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        return bitmap
    }
}
