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
 * Supports multiple manufacturer-specific boot actions
 */
class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            ACTION_QUICKBOOT_POWERON,
            ACTION_HTC_QUICKBOOT_POWERON,
            ACTION_XIAOMI_QUICKBOOT_POWERON,
            ACTION_MY_PACKAGE_REPLACED -> {
                Log.d(TAG, "Boot completed (${intent.action}) - starting background services")
                
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
        
        // Manufacturer-specific boot actions (not in Android SDK)
        private const val ACTION_QUICKBOOT_POWERON = "android.intent.action.QUICKBOOT_POWERON"
        private const val ACTION_HTC_QUICKBOOT_POWERON = "com.htc.intent.action.QUICKBOOT_POWERON"
        private const val ACTION_XIAOMI_QUICKBOOT_POWERON = "android.intent.action.REBOOT"
        private const val ACTION_MY_PACKAGE_REPLACED = "android.intent.action.MY_PACKAGE_REPLACED"
    }
}