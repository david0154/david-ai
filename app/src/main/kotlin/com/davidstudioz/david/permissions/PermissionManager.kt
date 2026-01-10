package com.davidstudioz.david.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Permission Manager - FIXED for Android 13+ (API 33+)
 * Handles all runtime permissions with proper Android 13+ support
 */
class PermissionManager(private val context: Context) {

    companion object {
        private const val TAG = "PermissionManager"
        const val PERMISSION_REQUEST_CODE = 1001
        
        /**
         * Essential permissions for core functionality
         */
        val ESSENTIAL_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA
            )
        }
        
        /**
         * Phone & SMS permissions
         */
        val PHONE_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.CALL_PHONE,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_SMS
            )
        } else {
            arrayOf(
                Manifest.permission.CALL_PHONE,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_SMS,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.WRITE_CALL_LOG
            )
        }
        
        /**
         * Location permissions
         */
        val LOCATION_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        
        /**
         * Storage permissions - different for Android 13+
         */
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
        
        /**
         * Bluetooth permissions - different for Android 12+
         */
        val BLUETOOTH_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            )
        }
        
        /**
         * Contacts permissions
         */
        val CONTACTS_PERMISSIONS = arrayOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS
        )
    }

    /**
     * Check if a specific permission is granted
     */
    fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Check if all permissions in array are granted
     */
    fun arePermissionsGranted(permissions: Array<String>): Boolean {
        return permissions.all { isPermissionGranted(it) }
    }

    /**
     * Get list of denied permissions from array
     */
    fun getDeniedPermissions(permissions: Array<String>): List<String> {
        return permissions.filter { !isPermissionGranted(it) }
    }

    /**
     * Request permissions
     */
    fun requestPermissions(activity: Activity, permissions: Array<String>, requestCode: Int = PERMISSION_REQUEST_CODE) {
        try {
            Log.d(TAG, "Requesting ${permissions.size} permissions")
            ActivityCompat.requestPermissions(activity, permissions, requestCode)
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting permissions", e)
        }
    }

    /**
     * Request essential permissions
     */
    fun requestEssentialPermissions(activity: Activity) {
        val denied = getDeniedPermissions(ESSENTIAL_PERMISSIONS)
        if (denied.isNotEmpty()) {
            Log.d(TAG, "Requesting essential permissions: $denied")
            requestPermissions(activity, denied.toTypedArray())
        }
    }

    /**
     * Check if all essential permissions are granted
     */
    fun hasEssentialPermissions(): Boolean {
        return arePermissionsGranted(ESSENTIAL_PERMISSIONS)
    }

    /**
     * Check if special permissions are granted
     */
    fun hasOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    fun hasWriteSettingsPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.System.canWrite(context)
        } else {
            true
        }
    }

    fun hasManageExternalStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            android.os.Environment.isExternalStorageManager()
        } else {
            true
        }
    }

    /**
     * Request overlay permission
     */
    fun requestOverlayPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            )
            activity.startActivity(intent)
        }
    }

    /**
     * Request write settings permission
     */
    fun requestWriteSettingsPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_WRITE_SETTINGS,
                Uri.parse("package:${context.packageName}")
            )
            activity.startActivity(intent)
        }
    }

    /**
     * Request manage external storage permission
     */
    fun requestManageExternalStoragePermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intent = Intent(
                Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                Uri.parse("package:${context.packageName}")
            )
            activity.startActivity(intent)
        }
    }

    /**
     * Open app settings
     */
    fun openAppSettings(activity: Activity) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:${context.packageName}")
        )
        activity.startActivity(intent)
    }

    /**
     * Get permission status report
     */
    fun getPermissionReport(): Map<String, Boolean> {
        val report = mutableMapOf<String, Boolean>()
        
        // Essential
        report["Essential Permissions"] = hasEssentialPermissions()
        
        // Phone
        report["Phone Permissions"] = arePermissionsGranted(PHONE_PERMISSIONS)
        
        // Location
        report["Location Permissions"] = arePermissionsGranted(LOCATION_PERMISSIONS)
        
        // Storage
        report["Storage Permissions"] = arePermissionsGranted(STORAGE_PERMISSIONS)
        
        // Bluetooth
        report["Bluetooth Permissions"] = arePermissionsGranted(BLUETOOTH_PERMISSIONS)
        
        // Contacts
        report["Contacts Permissions"] = arePermissionsGranted(CONTACTS_PERMISSIONS)
        
        // Special
        report["Overlay Permission"] = hasOverlayPermission()
        report["Write Settings Permission"] = hasWriteSettingsPermission()
        report["Manage Storage Permission"] = hasManageExternalStoragePermission()
        
        return report
    }

    /**
     * Log permission status
     */
    fun logPermissionStatus() {
        val report = getPermissionReport()
        Log.d(TAG, "=== Permission Status ===")
        report.forEach { (name, granted) ->
            Log.d(TAG, "$name: ${if (granted) "✅ GRANTED" else "❌ DENIED"}")
        }
    }
}
