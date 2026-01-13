package com.davidstudioz.david.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.davidstudioz.david.voice.HotWordDetectionService

/**
 * ✅ BootReceiver - Auto-start services on device boot
 * Enables always-on voice assistant functionality
 */
class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_QUICKBOOT_POWERON,
            "android.intent.action.MY_PACKAGE_REPLACED" -> {
                Log.d(TAG, "Boot completed - starting background services")
                
                try {
                    // Check if hot word service is enabled
                    val prefs = context.getSharedPreferences("david_settings", Context.MODE_PRIVATE)
                    val isHotWordEnabled = prefs.getBoolean("hot_word_enabled", true)
                    
                    if (isHotWordEnabled) {
                        // Start hot word detection service
                        HotWordDetectionService.start(context)
                        Log.d(TAG, "✅ Hot word service started after boot")
                    } else {
                        Log.d(TAG, "Hot word service disabled in settings")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error starting services after boot", e)
                }
            }
        }
    }
    
    companion object {
        private const val TAG = "BootReceiver"
    }
}