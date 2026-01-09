package com.davidstudioz.david.security

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.util.Log

/**
 * Device Lock Manager
 * Controls device locking via voice commands
 * Requires Device Admin permissions
 */
class DeviceLockManager(private val context: Context) {

    private val TAG = "DeviceLockManager"
    private var devicePolicyManager: DevicePolicyManager? = null
    private var adminComponent: ComponentName? = null

    init {
        try {
            devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            adminComponent = ComponentName(context, DavidDeviceAdminReceiver::class.java)
            Log.d(TAG, "Device Lock Manager initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Device Lock Manager", e)
        }
    }

    /**
     * Lock device immediately (requires device admin)
     */
    fun lockDevice(): Boolean {
        return try {
            if (adminComponent == null || !isDeviceAdminEnabled()) {
                Log.w(TAG, "Device admin not enabled")
                return false
            }
            devicePolicyManager?.lockNow()
            Log.d(TAG, "Device locked")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to lock device", e)
            false
        }
    }

    /**
     * Check if device admin is enabled
     */
    fun isDeviceAdminEnabled(): Boolean {
        return try {
            adminComponent?.let {
                devicePolicyManager?.isAdminActive(it) ?: false
            } ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking device admin status", e)
            false
        }
    }

    /**
     * Get lock status
     */
    fun getLockStatus(): Map<String, Any> {
        return mapOf(
            "isDeviceAdminEnabled" to isDeviceAdminEnabled(),
            "canLockDevice" to (adminComponent != null && isDeviceAdminEnabled())
        )
    }
}

/**
 * Device Admin Receiver
 * Handles device admin lifecycle
 */
class DavidDeviceAdminReceiver : android.app.admin.DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: android.content.Intent) {
        Log.d("DavidDeviceAdmin", "Device admin enabled")
    }

    override fun onDisabled(context: Context, intent: android.content.Intent) {
        Log.d("DavidDeviceAdmin", "Device admin disabled")
    }
}
