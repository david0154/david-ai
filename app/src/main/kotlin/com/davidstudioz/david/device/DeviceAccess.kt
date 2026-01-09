package com.davidstudioz.david.device

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat

/**
 * Device Access Manager
 * Manages all device permissions and access levels
 */
class DeviceAccessManager(private val context: Context) {

    private val TAG = "DeviceAccessManager"

    // Permission groups
    enum class PermissionGroup {
        CAMERA,
        MICROPHONE,
        LOCATION,
        CONTACTS,
        SMS,
        CALL,
        STORAGE,
        BLUETOOTH,
        WIFI,
        OVERLAY,
        DEVICE_ADMIN
    }

    /**
     * Check if permission group is granted
     */
    fun hasPermission(group: PermissionGroup): Boolean {
        return when (group) {
            PermissionGroup.CAMERA -> hasPermission(Manifest.permission.CAMERA)
            PermissionGroup.MICROPHONE -> hasPermission(Manifest.permission.RECORD_AUDIO)
            PermissionGroup.LOCATION -> hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            PermissionGroup.CONTACTS -> hasPermission(Manifest.permission.READ_CONTACTS)
            PermissionGroup.SMS -> hasPermission(Manifest.permission.SEND_SMS)
            PermissionGroup.CALL -> hasPermission(Manifest.permission.CALL_PHONE)
            PermissionGroup.STORAGE -> hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            PermissionGroup.BLUETOOTH -> hasPermission(Manifest.permission.BLUETOOTH)
            PermissionGroup.WIFI -> hasPermission(Manifest.permission.ACCESS_WIFI_STATE)
            PermissionGroup.OVERLAY -> hasPermission(Manifest.permission.SYSTEM_ALERT_WINDOW)
            PermissionGroup.DEVICE_ADMIN -> isDeviceAdminEnabled()
        }
    }

    /**
     * Check single permission
     */
    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Check if device admin is enabled
     */
    private fun isDeviceAdminEnabled(): Boolean {
        val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE)
            as android.app.admin.DevicePolicyManager
        val adminReceiver = android.content.ComponentName(
            context,
            Class.forName("com.davidstudioz.david.security.DavidDeviceAdminReceiver")
        )
        return devicePolicyManager.isAdminActive(adminReceiver)
    }

    /**
     * Get all granted permissions
     */
    fun getGrantedPermissions(): List<PermissionGroup> {
        return PermissionGroup.values().filter { hasPermission(it) }
    }

    /**
     * Get missing permissions
     */
    fun getMissingPermissions(): List<PermissionGroup> {
        return PermissionGroup.values().filter { !hasPermission(it) }
    }

    /**
     * Get access status
     */
    fun getAccessStatus(): Map<String, Boolean> {
        return mapOf(
            "camera" to hasPermission(PermissionGroup.CAMERA),
            "microphone" to hasPermission(PermissionGroup.MICROPHONE),
            "location" to hasPermission(PermissionGroup.LOCATION),
            "contacts" to hasPermission(PermissionGroup.CONTACTS),
            "sms" to hasPermission(PermissionGroup.SMS),
            "call" to hasPermission(PermissionGroup.CALL),
            "storage" to hasPermission(PermissionGroup.STORAGE),
            "bluetooth" to hasPermission(PermissionGroup.BLUETOOTH),
            "wifi" to hasPermission(PermissionGroup.WIFI),
            "overlay" to hasPermission(PermissionGroup.OVERLAY),
            "deviceAdmin" to hasPermission(PermissionGroup.DEVICE_ADMIN)
        )
    }

    /**
     * Get running apps
     */
    fun getRunningApps(): List<String> {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return activityManager.runningAppProcesses.map { it.processName }
    }

    /**
     * Check if app is installed
     */
    fun isAppInstalled(packageName: String): Boolean {
        return try {
            context.packageManager.getApplicationInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * Log device access status
     */
    fun logAccessStatus() {
        Log.d(TAG, "Device Access Status:")
        getAccessStatus().forEach { (permission, granted) ->
            Log.d(TAG, "  $permission: ${if (granted) "GRANTED" else "DENIED"}")
        }
    }
}
