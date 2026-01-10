package com.davidstudioz.david.gesture

/**
 * CameraGestureRecognition - TEMPORARILY DISABLED
 * 
 * This file uses the deprecated MediaPipe Solutions API which is incompatible
 * with the current MediaPipe Tasks Vision API (v0.10.14).
 * 
 * To re-enable:
 * 1. Migrate from MediaPipe Solutions to Tasks Vision API
 * 2. Update imports from com.google.mediapipe.solutions.hands to
 *    com.google.mediapipe.tasks.vision.handlandmarker
 * 3. Update HandsOptions to HandLandmarkerOptions
 * 4. Update result handling to HandLandmarkerResult
 * 
 * Reference: https://developers.google.com/mediapipe/solutions/vision/hand_landmarker/android
 * See: COMPILATION_ERRORS_REMAINING.md for migration guide
 */

/*
// Original code commented out due to MediaPipe API incompatibility
// Uncomment and migrate to Tasks Vision API when ready

import android.content.Context
import android.graphics.Bitmap
import android.view.SurfaceHolder
import androidx.camera.core.ImageProxy
import com.google.mediapipe.solutions.hands.Hands
import com.google.mediapipe.solutions.hands.HandsOptions
import com.google.mediapipe.solutions.hands.HandsResult

class CameraGestureRecognition(
    private val context: Context,
    private val onGestureDetected: (String) -> Unit
) {
    // Implementation requires MediaPipe migration
}
*/
