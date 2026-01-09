package com.davidstudioz.david.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity

/**
 * Permission Manager
 * Handles all required device permissions for DAVID AI
 */
class PermissionManager(private val context: Context) {

    companion object {
        // Core permissions for voice and AI
        private val CORE_PERMISSIONS = arrayOf(
            Manifest.permission.RECORD_AUDIO,           // Microphone for voice input
            Manifest.permission.CAMERA,                 // Camera for gesture recognition
            Manifest.permission.ACCESS_FINE_LOCATION,   // GPS for location
        )

        // Device control permissions
        private val DEVICE_CONTROL_PERMISSIONS = arrayOf(
            Manifest.permission.CALL_PHONE,             // Make calls
            Manifest.permission.SEND_SMS,               // Send SMS
            Manifest.permission.CHANGE_NETWORK_STATE,   // WiFi/Bluetooth
            Manifest.permission.BLUETOOTH_ADMIN,        // Bluetooth control
            Manifest.permission.CHANGE_WIFI_STATE,      // WiFi control
            Manifest.permission.ACCESS_NOTIFICATION_POLICY,  // Silent mode
        )

        // Privacy and security
        private val PRIVACY_PERMISSIONS = arrayOf(
            Manifest.permission.READ_CONTACTS,          // Get contact info
            Manifest.permission.READ_CALL_LOG,          // Access call history
            Manifest.permission.READ_SMS,               // Read messages
        )

        // File and storage
        private val STORAGE_PERMISSIONS = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )

        // All permissions
        val ALL_PERMISSIONS = CORE_PERMISSIONS + DEVICE_CONTROL_PERMISSIONS + PRIVACY_PERMISSIONS + STORAGE_PERMISSIONS
    }

    /**
     * Check if permission is granted
     */
    fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Check if all core permissions are granted
     */
    fun areCorePermissionsGranted(): Boolean {
        return CORE_PERMISSIONS.all { isPermissionGranted(it) }
    }

    /**
     * Check if device control permissions are granted
     */
    fun areDeviceControlPermissionsGranted(): Boolean {
        return DEVICE_CONTROL_PERMISSIONS.all { isPermissionGranted(it) }
    }

    /**
     * Get list of missing permissions
     */
    fun getMissingPermissions(): List<String> {
        return ALL_PERMISSIONS.filter { !isPermissionGranted(it) }
    }

    /**
     * Request permissions
     */
    fun requestPermissions(
        activity: FragmentActivity,
        permissions: Array<String>,
        onResult: (granted: List<String>, denied: List<String>) -> Unit
    ) {
        val launcher = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { results ->
            val granted = results.filter { it.value }.map { it.key }
            val denied = results.filter { !it.value }.map { it.key }
            onResult(granted, denied)
        }
        launcher.launch(permissions)
    }

    /**
     * Request core permissions
     */
    fun requestCorePermissions(
        activity: FragmentActivity,
        onResult: (granted: List<String>, denied: List<String>) -> Unit
    ) {
        requestPermissions(activity, CORE_PERMISSIONS, onResult)
    }

    /**
     * Request device control permissions
     */
    fun requestDeviceControlPermissions(
        activity: FragmentActivity,
        onResult: (granted: List<String>, denied: List<String>) -> Unit
    ) {
        requestPermissions(activity, DEVICE_CONTROL_PERMISSIONS, onResult)
    }

    /**
     * Request all permissions
     */
    fun requestAllPermissions(
        activity: FragmentActivity,
        onResult: (granted: List<String>, denied: List<String>) -> Unit
    ) {
        requestPermissions(activity, ALL_PERMISSIONS, onResult)
    }

    /**
     * Get permission status summary
     */
    fun getPermissionSummary(): String {
        val summary = StringBuilder()
        summary.append("Core Permissions: ${if (areCorePermissionsGranted()) "✅" else "❌"}\n")
        summary.append("Device Control: ${if (areDeviceControlPermissionsGranted()) "✅" else "❌"}\n")
        summary.append("\nMissing Permissions:\n")
        getMissingPermissions().forEach { permission ->
            summary.append("- ${permission.substring(permission.lastIndexOf(".") + 1)}\n")
        }
        return summary.toString()
    }
}
