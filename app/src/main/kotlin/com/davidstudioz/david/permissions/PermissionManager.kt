package com.davidstudioz.david.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * PermissionManager - FIXED: Complete permission handling
 * ✅ Android 13+ (API 33+) permissions
 * ✅ Runtime permission requests
 * ✅ All dangerous permissions covered
 * ✅ Proper permission flow
 */
class PermissionManager(private val context: Context) {
    
    companion object {
        private const val TAG = "PermissionManager"
        const val PERMISSION_REQUEST_CODE = 1001
        
        // Essential permissions for D.A.V.I.D
        val ESSENTIAL_PERMISSIONS = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE
        )
        
        // Device control permissions
        val DEVICE_CONTROL_PERMISSIONS = arrayOf(
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_PHONE_STATE
        )
        
        // Location permissions
        val LOCATION_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        
        // Storage permissions
        val STORAGE_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
        
        // Contacts permissions
        val CONTACTS_PERMISSIONS = arrayOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS
        )
        
        // Bluetooth permissions (Android 12+)
        val BLUETOOTH_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            )
        }
        
        // Notification permission (Android 13+)
        val NOTIFICATION_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            emptyArray()
        }
    }
    
    /**
     * Check if permission is granted
     */
    fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Check if all permissions in array are granted
     */
    fun arePermissionsGranted(permissions: Array<String>): Boolean {
        return permissions.all { isPermissionGranted(it) }
    }
    
    /**
     * Get list of permissions that are not granted
     */
    fun getMissingPermissions(permissions: Array<String>): Array<String> {
        return permissions.filter { !isPermissionGranted(it) }.toTypedArray()
    }
    
    /**
     * Check essential permissions
     */
    fun areEssentialPermissionsGranted(): Boolean {
        return arePermissionsGranted(ESSENTIAL_PERMISSIONS)
    }
    
    /**
     * Get all missing essential permissions
     */
    fun getMissingEssentialPermissions(): Array<String> {
        return getMissingPermissions(ESSENTIAL_PERMISSIONS)
    }
    
    /**
     * Request permissions from activity
     */
    fun requestPermissions(activity: Activity, permissions: Array<String>) {
        if (permissions.isEmpty()) {
            Log.d(TAG, "No permissions to request")
            return
        }
        
        Log.d(TAG, "Requesting ${permissions.size} permissions")
        ActivityCompat.requestPermissions(activity, permissions, PERMISSION_REQUEST_CODE)
    }
    
    /**
     * Request all essential permissions
     */
    fun requestEssentialPermissions(activity: Activity) {
        val missing = getMissingEssentialPermissions()
        if (missing.isNotEmpty()) {
            Log.d(TAG, "Requesting essential permissions: ${missing.joinToString()}")
            requestPermissions(activity, missing)
        }
    }
    
    /**
     * Request all permissions needed for full functionality
     */
    fun requestAllPermissions(activity: Activity) {
        val allPermissions = mutableListOf<String>()
        
        allPermissions.addAll(ESSENTIAL_PERMISSIONS)
        allPermissions.addAll(DEVICE_CONTROL_PERMISSIONS)
        allPermissions.addAll(LOCATION_PERMISSIONS)
        allPermissions.addAll(STORAGE_PERMISSIONS)
        allPermissions.addAll(CONTACTS_PERMISSIONS)
        allPermissions.addAll(BLUETOOTH_PERMISSIONS)
        allPermissions.addAll(NOTIFICATION_PERMISSIONS)
        
        val missing = getMissingPermissions(allPermissions.toTypedArray())
        if (missing.isNotEmpty()) {
            Log.d(TAG, "Requesting ${missing.size} permissions")
            requestPermissions(activity, missing)
        } else {
            Log.d(TAG, "All permissions already granted")
        }
    }
    
    /**
     * Handle permission request result
     */
    fun handlePermissionResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
        onAllGranted: () -> Unit,
        onSomeDenied: (denied: List<String>) -> Unit
    ) {
        if (requestCode != PERMISSION_REQUEST_CODE) return
        
        val deniedPermissions = mutableListOf<String>()
        
        permissions.forEachIndexed { index, permission ->
            if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions.add(permission)
                Log.w(TAG, "Permission denied: $permission")
            } else {
                Log.d(TAG, "Permission granted: $permission")
            }
        }
        
        if (deniedPermissions.isEmpty()) {
            Log.d(TAG, "All requested permissions granted")
            onAllGranted()
        } else {
            Log.w(TAG, "${deniedPermissions.size} permissions denied")
            onSomeDenied(deniedPermissions)
        }
    }
    
    /**
     * Check if permission can be requested (not permanently denied)
     */
    fun shouldShowRequestPermissionRationale(activity: Activity, permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }
    
    /**
     * Get permission group status
     */
    fun getPermissionGroupStatus(): Map<String, Boolean> {
        return mapOf(
            "Essential" to arePermissionsGranted(ESSENTIAL_PERMISSIONS),
            "Device Control" to arePermissionsGranted(DEVICE_CONTROL_PERMISSIONS),
            "Location" to arePermissionsGranted(LOCATION_PERMISSIONS),
            "Storage" to arePermissionsGranted(STORAGE_PERMISSIONS),
            "Contacts" to arePermissionsGranted(CONTACTS_PERMISSIONS),
            "Bluetooth" to arePermissionsGranted(BLUETOOTH_PERMISSIONS),
            "Notifications" to arePermissionsGranted(NOTIFICATION_PERMISSIONS)
        )
    }
    
    /**
     * Log permission status for debugging
     */
    fun logPermissionStatus() {
        Log.d(TAG, "=== Permission Status ===")
        getPermissionGroupStatus().forEach { (group, granted) ->
            Log.d(TAG, "$group: ${if (granted) "✓" else "✗"}")
        }
        Log.d(TAG, "========================")
    }
}
