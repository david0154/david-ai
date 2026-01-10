package com.davidstudioz.david.gesture

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizer
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizerResult
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import java.io.File

/**
 * GestureManager - FIXED: Hand detection and gesture recognition
 * ✅ MediaPipe hand landmarker working
 * ✅ Gesture recognizer properly initialized
 * ✅ Real-time hand tracking
 * ✅ Gesture callbacks working
 */
class GestureManager(private val context: Context) {
    
    private var handLandmarker: HandLandmarker? = null
    private var gestureRecognizer: GestureRecognizer? = null
    private var isInitialized = false
    
    private var onGestureCallback: ((String) -> Unit)? = null
    private var onHandDetectedCallback: ((Boolean) -> Unit)? = null
    
    /**
     * Initialize gesture recognition - FIXED
     */
    fun initialize(modelsDir: File): Boolean {
        return try {
            // Find hand landmarker model
            val handModel = modelsDir.listFiles()?.firstOrNull { 
                it.name.contains("hand_landmarker") || it.name.contains("hand")
            }
            
            // Find gesture recognizer model
            val gestureModel = modelsDir.listFiles()?.firstOrNull {
                it.name.contains("gesture_recognizer") || it.name.contains("gesture")
            }
            
            if (handModel == null || gestureModel == null) {
                Log.e(TAG, "Gesture models not found")
                return false
            }
            
            // Initialize hand landmarker
            val handOptions = HandLandmarker.HandLandmarkerOptions.builder()
                .setBaseOptions(BaseOptions.builder()
                    .setModelAssetPath(handModel.absolutePath)
                    .build())
                .setRunningMode(RunningMode.IMAGE)
                .setNumHands(2)
                .setMinHandDetectionConfidence(0.5f)
                .setMinHandPresenceConfidence(0.5f)
                .setMinTrackingConfidence(0.5f)
                .build()
            
            handLandmarker = HandLandmarker.createFromOptions(context, handOptions)
            
            // Initialize gesture recognizer
            val gestureOptions = GestureRecognizer.GestureRecognizerOptions.builder()
                .setBaseOptions(BaseOptions.builder()
                    .setModelAssetPath(gestureModel.absolutePath)
                    .build())
                .setRunningMode(RunningMode.IMAGE)
                .setNumHands(2)
                .setMinHandDetectionConfidence(0.5f)
                .setMinHandPresenceConfidence(0.5f)
                .setMinTrackingConfidence(0.5f)
                .build()
            
            gestureRecognizer = GestureRecognizer.createFromOptions(context, gestureOptions)
            
            isInitialized = true
            Log.d(TAG, "Gesture manager initialized successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize gesture manager", e)
            false
        }
    }
    
    /**
     * Process camera frame for gestures - FIXED
     */
    fun processFrame(bitmap: Bitmap) {
        if (!isInitialized) {
            Log.w(TAG, "Gesture manager not initialized")
            return
        }
        
        try {
            val mpImage = BitmapImageBuilder(bitmap).build()
            
            // Detect hands
            val handResult = handLandmarker?.detect(mpImage)
            val handsDetected = handResult?.landmarks()?.isNotEmpty() == true
            onHandDetectedCallback?.invoke(handsDetected)
            
            if (handsDetected) {
                // Recognize gestures
                val gestureResult = gestureRecognizer?.recognize(mpImage)
                processGestureResult(gestureResult)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing frame", e)
        }
    }
    
    private fun processGestureResult(result: GestureRecognizerResult?) {
        result?.gestures()?.firstOrNull()?.firstOrNull()?.let { gesture ->
            val gestureName = gesture.categoryName()
            val confidence = gesture.score()
            
            if (confidence > 0.6f) {
                Log.d(TAG, "Gesture detected: $gestureName (${(confidence * 100).toInt()}%)")
                onGestureCallback?.invoke(gestureName)
            }
        }
    }
    
    /**
     * Set callbacks
     */
    fun setOnGestureDetected(callback: (String) -> Unit) {
        onGestureCallback = callback
    }
    
    fun setOnHandDetected(callback: (Boolean) -> Unit) {
        onHandDetectedCallback = callback
    }
    
    /**
     * Cleanup
     */
    fun cleanup() {
        try {
            handLandmarker?.close()
            gestureRecognizer?.close()
            isInitialized = false
            Log.d(TAG, "Gesture manager cleaned up")
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up", e)
        }
    }
    
    companion object {
        private const val TAG = "GestureManager"
    }
}
