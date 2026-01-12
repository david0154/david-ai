package com.davidstudioz.david.services

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.preference.PreferenceManager
import com.davidstudioz.david.gesture.GestureRecognitionService
import com.davidstudioz.david.voice.HotWordDetectionService

/**
 * ServiceManager - Centralized Background Service Control
 * 
 * ✅ FEATURES:
 * - Start/stop all background services
 * - Service state monitoring
 * - Permission checking
 * - Battery optimization bypass
 * - Auto-start on boot
 * - User preference integration
 * - Service status reporting
 * 
 * MANAGED SERVICES:
 * 1. HotWordDetectionService - Always-on voice detection
 * 2. GestureRecognitionService - Background gesture recognition
 */
class ServiceManager(private val context: Context) {

    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    
    companion object {
        private const val TAG = "ServiceManager"
        
        private const val PREF_HOTWORD_ENABLED = "hotword_service_enabled"
        private const val PREF_GESTURE_ENABLED = "gesture_service_enabled"
        private const val PREF_AUTO_START = "services_auto_start"
        
        // Default settings
        private const val DEFAULT_HOTWORD_ENABLED = true
        private const val DEFAULT_GESTURE_ENABLED = false // Camera battery drain
        private const val DEFAULT_AUTO_START = true
    }

    /**
     * Start all enabled services
     */
    fun startAllServices() {
        Log.d(TAG, "Starting all enabled services")
        
        if (isHotWordServiceEnabled()) {
            startHotWordService()
        }
        
        if (isGestureServiceEnabled()) {
            startGestureService()
        }
    }

    /**
     * Stop all services
     */
    fun stopAllServices() {
        Log.d(TAG, "Stopping all services")
        stopHotWordService()
        stopGestureService()
    }

    /**
     * Start hot word detection service
     */
    fun startHotWordService() {
        try {
            if (!hasRequiredPermissions(ServiceType.HOTWORD)) {
                Log.w(TAG, "Missing permissions for hot word service")
                return
            }
            
            HotWordDetectionService.start(context)
            Log.d(TAG, "Hot word service started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start hot word service", e)
        }
    }

    /**
     * Stop hot word detection service
     */
    fun stopHotWordService() {
        try {
            HotWordDetectionService.stop(context)
            Log.d(TAG, "Hot word service stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop hot word service", e)
        }
    }

    /**
     * Start gesture recognition service
     */
    fun startGestureService() {
        try {
            if (!hasRequiredPermissions(ServiceType.GESTURE)) {
                Log.w(TAG, "Missing permissions for gesture service")
                return
            }
            
            GestureRecognitionService.start(context)
            Log.d(TAG, "Gesture service started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start gesture service", e)
        }
    }

    /**
     * Stop gesture recognition service
     */
    fun stopGestureService() {
        try {
            GestureRecognitionService.stop(context)
            Log.d(TAG, "Gesture service stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop gesture service", e)
        }
    }

    /**
     * Get service status
     */
    fun getServiceStatus(): ServiceStatus {
        return ServiceStatus(
            hotWordRunning = HotWordDetectionService.isServiceRunning,
            gestureRunning = GestureRecognitionService.isServiceRunning,
            hotWordEnabled = isHotWordServiceEnabled(),
            gestureEnabled = isGestureServiceEnabled(),
            autoStartEnabled = isAutoStartEnabled(),
            batteryOptimizationDisabled = isBatteryOptimizationDisabled()
        )
    }

    /**
     * Check if service has required permissions
     */
    private fun hasRequiredPermissions(serviceType: ServiceType): Boolean {
        return when (serviceType) {
            ServiceType.HOTWORD -> {
                // Check microphone permission
                context.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) == 
                    android.content.pm.PackageManager.PERMISSION_GRANTED
            }
            ServiceType.GESTURE -> {
                // Check camera permission
                context.checkSelfPermission(android.Manifest.permission.CAMERA) == 
                    android.content.pm.PackageManager.PERMISSION_GRANTED
            }
        }
    }

    /**
     * Request battery optimization bypass
     */
    fun requestBatteryOptimizationBypass() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                if (!isBatteryOptimizationDisabled()) {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = android.net.Uri.parse("package:${context.packageName}")
                    }
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    Log.d(TAG, "Requesting battery optimization bypass")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to request battery optimization bypass", e)
            }
        }
    }

    /**
     * Check if battery optimization is disabled
     */
    private fun isBatteryOptimizationDisabled(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            true // Not applicable for older versions
        }
    }

    // User preferences
    
    fun isHotWordServiceEnabled(): Boolean {
        return prefs.getBoolean(PREF_HOTWORD_ENABLED, DEFAULT_HOTWORD_ENABLED)
    }
    
    fun setHotWordServiceEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(PREF_HOTWORD_ENABLED, enabled).apply()
        if (enabled) {
            startHotWordService()
        } else {
            stopHotWordService()
        }
    }
    
    fun isGestureServiceEnabled(): Boolean {
        return prefs.getBoolean(PREF_GESTURE_ENABLED, DEFAULT_GESTURE_ENABLED)
    }
    
    fun setGestureServiceEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(PREF_GESTURE_ENABLED, enabled).apply()
        if (enabled) {
            startGestureService()
        } else {
            stopGestureService()
        }
    }
    
    fun isAutoStartEnabled(): Boolean {
        return prefs.getBoolean(PREF_AUTO_START, DEFAULT_AUTO_START)
    }
    
    fun setAutoStartEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(PREF_AUTO_START, enabled).apply()
    }

    enum class ServiceType {
        HOTWORD,
        GESTURE
    }

    data class ServiceStatus(
        val hotWordRunning: Boolean,
        val gestureRunning: Boolean,
        val hotWordEnabled: Boolean,
        val gestureEnabled: Boolean,
        val autoStartEnabled: Boolean,
        val batteryOptimizationDisabled: Boolean
    )
}
