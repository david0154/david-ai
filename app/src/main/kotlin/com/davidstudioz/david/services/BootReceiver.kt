package com.davidstudioz.david.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * BootReceiver - Auto-Start Services on Device Boot
 * 
 * ✅ FEATURES:
 * - Listens for BOOT_COMPLETED broadcast
 * - Auto-starts enabled services
 * - Respects user preferences
 * - Handles permissions gracefully
 * 
 * REQUIRED PERMISSIONS:
 * - RECEIVE_BOOT_COMPLETED
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Device boot completed, starting services")
            
            val serviceManager = ServiceManager(context)
            
            // Check if auto-start is enabled
            if (serviceManager.isAutoStartEnabled()) {
                try {
                    // Start all enabled services
                    serviceManager.startAllServices()
                    Log.d(TAG, "Services started on boot")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to start services on boot", e)
                }
            } else {
                Log.d(TAG, "Auto-start disabled, skipping service start")
            }
        }
    }
}
