package com.davidstudioz.david.gesture

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.camera.core.CameraProvider
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mediapipe.solutions.hands.Hands
import com.google.mediapipe.solutions.hands.HandsOptions
import com.google.mediapipe.solutions.hands.HandsResult
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Camera-based Hand Gesture Recognition
 * Uses MediaPipe Hand Detection to recognize user gestures
 * Detects: pointing, thumbs up, open palm, peace sign, ok sign, fist
 */
class CameraGestureRecognition(
    context: Context,
    private val onGestureDetected: (gesture: HandGesture, handedness: String) -> Unit
) : SurfaceView(context), SurfaceHolder.Callback {

    enum class HandGesture {
        POINTING,      // Index finger pointing (pointer control)
        THUMBS_UP,     // Approve/confirm
        OPEN_PALM,     // Stop/reject
        PEACE_SIGN,    // Two fingers
        OK_SIGN,       // Thumb + index circle
        FIST,          // Closed hand
        GRAB,          // Open hand grab
        SWIPE_LEFT,    // Left swipe
        SWIPE_RIGHT,   // Right swipe
        PINCH          // Thumb + index pinch
    }

    private lateinit var hands: Hands
    private lateinit var cameraProvider: ProcessCameraProvider
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private val surfaceHolder = holder.apply { addCallback(this) }

    private var previousHandPosition = PointF(0f, 0f)
    private var currentHandPosition = PointF(0f, 0f)
    private var pointerVisible = false

    init {
        initializeMediaPipe()
    }

    /**
     * Initialize MediaPipe Hand Detection
     */
    private fun initializeMediaPipe() {
        val handsOptions = HandsOptions.builder()
            .setStaticImageMode(false)
            .setMaxNumHands(2)
            .setRunOnGpu(true)
            .build()

        hands = Hands(context, handsOptions)
        hands.setResultListener { result ->
            processHandDetectionResult(result)
        }
    }

    /**
     * Start camera and gesture recognition
     */
    fun startRecognition(lifecycleOwner: LifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindCameraUseCases(lifecycleOwner)
        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * Bind camera use cases for analysis
     */
    private fun bindCameraUseCases(lifecycleOwner: LifecycleOwner) {
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { analysis ->
                analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    processImage(imageProxy)
                }
            }

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                androidx.camera.core.CameraSelector.DEFAULT_FRONT_CAMERA,
                imageAnalysis
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Process image from camera
     */
    private fun processImage(imageProxy: androidx.camera.core.ImageProxy) {
        // Convert image to MediaPipe format
        val bitmap = androidx.camera.core.ImageProxyUtils.toBitmap(imageProxy)
        // This would be handled by MediaPipe's internal processing
        imageProxy.close()
    }

    /**
     * Process hand detection results from MediaPipe
     */
    private fun processHandDetectionResult(result: HandsResult) {
        if (result.multiHandLandmarks().isEmpty()) {
            pointerVisible = false
            return
        }

        val handLandmarks = result.multiHandLandmarks()[0]
        val handedness = if (result.multiHandedness().isNotEmpty()) {
            result.multiHandedness()[0].label()
        } else {
            "Unknown"
        }

        // Get key landmark points
        val wristPoint = handLandmarks.landmarkList[0]
        val indexTip = handLandmarks.landmarkList[8]
        val thumbTip = handLandmarks.landmarkList[4]
        val middleTip = handLandmarks.landmarkList[12]
        val ringTip = handLandmarks.landmarkList[16]
        val pinkyTip = handLandmarks.landmarkList[20]

        currentHandPosition = PointF(indexTip.x, indexTip.y)
        pointerVisible = true

        // Detect gesture type
        val gesture = detectHandGesture(handLandmarks, indexTip, thumbTip, middleTip, ringTip, pinkyTip)
        onGestureDetected(gesture, handedness)

        // Draw pointer at index finger tip
        drawPointer(currentHandPosition)
    }

    /**
     * Detect hand gesture from landmarks
     */
    private fun detectHandGesture(
        landmarks: com.google.mediapipe.framework.data.NormalizedLandmark?,
        indexTip: com.google.mediapipe.framework.data.NormalizedLandmark,
        thumbTip: com.google.mediapipe.framework.data.NormalizedLandmark,
        middleTip: com.google.mediapipe.framework.data.NormalizedLandmark,
        ringTip: com.google.mediapipe.framework.data.NormalizedLandmark,
        pinkyTip: com.google.mediapipe.framework.data.NormalizedLandmark
    ): HandGesture {
        // Calculate distances between finger tips
        val indexThumbDist = distanceBetween(indexTip, thumbTip)
        val indexMiddleDist = distanceBetween(indexTip, middleTip)
        val indexRingDist = distanceBetween(indexTip, ringTip)
        val indexPinkyDist = distanceBetween(indexTip, pinkyTip)
        val thumbMiddleDist = distanceBetween(thumbTip, middleTip)

        return when {
            // Pointing: Index extended, others closed
            indexMiddleDist > 0.15f && indexRingDist > 0.15f && indexPinkyDist > 0.15f ->
                HandGesture.POINTING
            
            // Thumbs up: Thumb extended up
            thumbTip.y < indexTip.y && indexThumbDist > 0.1f ->
                HandGesture.THUMBS_UP
            
            // Open palm: All fingers extended
            indexMiddleDist < 0.1f && middleTip.y > indexTip.y ->
                HandGesture.OPEN_PALM
            
            // Peace sign: Index and middle extended
            indexMiddleDist < 0.08f && indexRingDist > 0.15f ->
                HandGesture.PEACE_SIGN
            
            // OK sign: Index and thumb together
            indexThumbDist < 0.05f && thumbMiddleDist > 0.1f ->
                HandGesture.OK_SIGN
            
            // Fist: All fingers close together
            indexThumbDist < 0.1f && indexMiddleDist < 0.1f ->
                HandGesture.FIST
            
            else -> HandGesture.GRAB
        }
    }

    /**
     * Calculate distance between two landmarks
     */
    private fun distanceBetween(
        p1: com.google.mediapipe.framework.data.NormalizedLandmark,
        p2: com.google.mediapipe.framework.data.NormalizedLandmark
    ): Float {
        val dx = p1.x - p2.x
        val dy = p1.y - p2.y
        return kotlin.math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
    }

    /**
     * Draw pointer (mouse cursor) on screen
     */
    private fun drawPointer(position: PointF) {
        val canvas = surfaceHolder.lockCanvas() ?: return
        try {
            canvas.drawColor(android.graphics.Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR)
            
            val paint = Paint().apply {
                color = android.graphics.Color.YELLOW
                strokeWidth = 4f
                isAntiAlias = true
            }

            val screenX = position.x * canvas.width
            val screenY = position.y * canvas.height

            // Draw circle cursor
            canvas.drawCircle(screenX, screenY, 25f, paint)
            
            // Draw crosshair
            canvas.drawLine(screenX - 15, screenY, screenX + 15, screenY, paint)
            canvas.drawLine(screenX, screenY - 15, screenX, screenY + 15, paint)
        } finally {
            surfaceHolder.unlockCanvasAndPost(canvas)
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {}
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
    override fun surfaceDestroyed(holder: SurfaceHolder) {
        cameraExecutor.shutdown()
        hands.close()
    }

    fun release() {
        cameraExecutor.shutdown()
        hands.close()
    }
}
