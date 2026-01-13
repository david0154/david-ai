package com.davidstudioz.david.gesture

import android.content.Context
import android.util.Log
import com.davidstudioz.david.device.DeviceController
import com.davidstudioz.david.models.ModelManager
import java.io.File

class GestureManager(private val context: Context) {
    
    private var gestureModelPath: File? = null
    private var isModelLoaded = false
    private val modelManager = ModelManager(context)
    private val deviceController = DeviceController(context)
    
    init {
        loadGestureModel()
    }
    
    private fun loadGestureModel() {
        try {
            // ✅ FIXED: Get downloaded model files and check for gesture models
            val downloadedModels = modelManager.getDownloadedModels()
            val gestureModel = downloadedModels.firstOrNull { file ->
                (file.name.contains("gesture", ignoreCase = true) || 
                 file.name.contains("hand", ignoreCase = true)) && 
                file.length() > 1024 * 1024
            }
            
            if (gestureModel != null && gestureModel.exists()) {
                gestureModelPath = gestureModel
                isModelLoaded = true
                Log.d(TAG, "✅ Gesture model loaded: ${gestureModel.name}")
            } else {
                Log.w(TAG, "⚠️ No gesture model downloaded")
                isModelLoaded = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading gesture model", e)
            isModelLoaded = false
        }
    }
    
    fun isModelReady(): Boolean {
        if (!isModelLoaded || gestureModelPath == null) {
            loadGestureModel()
        }
        return isModelLoaded && gestureModelPath != null && gestureModelPath!!.exists()
    }
    
    fun processGesture(gestureType: String): Boolean {
        return try {
            when (gestureType.lowercase()) {
                "swipe_up" -> {
                    deviceController.volumeUp()
                    true
                }
                "swipe_down" -> {
                    deviceController.volumeDown()
                    true
                }
                "swipe_left" -> {
                    deviceController.mediaPrevious()
                    true
                }
                "swipe_right" -> {
                    deviceController.mediaNext()
                    true
                }
                "pinch" -> {
                    deviceController.mediaPlayPause()
                    true
                }
                "open_palm" -> {
                    deviceController.mediaPause()
                    true
                }
                "closed_fist" -> {
                    deviceController.mediaPlay()
                    true
                }
                "peace_sign" -> {
                    deviceController.takeSelfie()
                    true
                }
                "thumbs_up" -> {
                    deviceController.volumeUp()
                    true
                }
                "thumbs_down" -> {
                    deviceController.volumeDown()
                    true
                }
                "point" -> {
                    true
                }
                else -> {
                    Log.w(TAG, "Unknown gesture: $gestureType")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing gesture", e)
            false
        }
    }
    
    fun getModelStatus(): String {
        return if (isModelReady()) {
            "Gesture Model: Loaded (${gestureModelPath?.name})"
        } else {
            "Gesture Model: Not loaded - Please download gesture model"
        }
    }
    
    fun startGestureRecognition(): Boolean {
        return try {
            if (!isModelReady()) {
                Log.e(TAG, "Cannot start - model not ready")
                return false
            }
            
            val intent = android.content.Intent(context, com.davidstudioz.david.gesture.GestureRecognitionService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            
            Log.d(TAG, "✅ Gesture recognition started")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error starting gesture recognition", e)
            false
        }
    }
    
    fun stopGestureRecognition() {
        try {
            val intent = android.content.Intent(context, com.davidstudioz.david.gesture.GestureRecognitionService::class.java)
            context.stopService(intent)
            Log.d(TAG, "Gesture recognition stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping gesture recognition", e)
        }
    }
    
    companion object {
        private const val TAG = "GestureManager"
    }
}