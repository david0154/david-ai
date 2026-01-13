package com.davidstudioz.david.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.util.Log
import android.view.accessibility.AccessibilityEvent

/**
 * ✅ DavidAccessibilityService - Enables background control
 * Allows D.A.V.I.D to:
 * - Perform gestures on behalf of user
 * - Click, scroll, swipe
 * - Control apps in background
 */
class DavidAccessibilityService : AccessibilityService() {
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "✅ Accessibility service connected")
        
        // Store service instance for global access
        instance = this
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // We don't need to monitor events for now
        // Just need the service for gesture capabilities
    }
    
    override fun onInterrupt() {
        Log.d(TAG, "Accessibility service interrupted")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        instance = null
        Log.d(TAG, "Accessibility service destroyed")
    }
    
    /**
     * Perform a click at coordinates
     */
    fun performClick(x: Float, y: Float): Boolean {
        val path = Path()
        path.moveTo(x, y)
        
        val gestureBuilder = GestureDescription.Builder()
        val gesture = gestureBuilder
            .addStroke(GestureDescription.StrokeDescription(path, 0, 100))
            .build()
        
        return dispatchGesture(gesture, null, null)
    }
    
    /**
     * Perform a swipe gesture
     */
    fun performSwipe(startX: Float, startY: Float, endX: Float, endY: Float, duration: Long = 300): Boolean {
        val path = Path()
        path.moveTo(startX, startY)
        path.lineTo(endX, endY)
        
        val gestureBuilder = GestureDescription.Builder()
        val gesture = gestureBuilder
            .addStroke(GestureDescription.StrokeDescription(path, 0, duration))
            .build()
        
        return dispatchGesture(gesture, null, null)
    }
    
    /**
     * Perform back button action
     */
    fun performBack(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_BACK)
    }
    
    /**
     * Perform home button action
     */
    fun performHome(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_HOME)
    }
    
    /**
     * Perform recent apps action
     */
    fun performRecents(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_RECENTS)
    }
    
    companion object {
        private const val TAG = "DavidAccessibilityService"
        
        // Global instance for access from anywhere
        var instance: DavidAccessibilityService? = null
            private set
        
        /**
         * Check if accessibility service is enabled
         */
        fun isEnabled(): Boolean {
            return instance != null
        }
    }
}