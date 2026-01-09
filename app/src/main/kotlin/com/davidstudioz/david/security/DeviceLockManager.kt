package com.davidstudioz.david.security

import android.content.Context
import android.os.Build
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricManager
import androidx.fragment.app.FragmentActivity
import android.content.Intent
import android.provider.Settings
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.app.admin.AdminReceiver

/**
 * Device Lock Manager
 * Lock device via voice command
 * Unlock via biometric (fingerprint/face)
 * Requires Device Admin permission
 */
class DeviceLockManager(private val context: Context) {

    private val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private var isDeviceAdminEnabled = false

    init {
        checkDeviceAdminStatus()
    }

    /**
     * Check if device admin is enabled
     */
    private fun checkDeviceAdminStatus() {
        val deviceAdminReceiver = ComponentName(context, DavidDeviceAdminReceiver::class.java)
        isDeviceAdminEnabled = devicePolicyManager.isAdminActive(deviceAdminReceiver)
    }

    /**
     * Request Device Admin permission
     * Command: "Enable device security"
     */
    fun requestDeviceAdminPermission() {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(
                DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                ComponentName(context, DavidDeviceAdminReceiver::class.java)
            )
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "DAVID AI needs device admin to lock your phone via voice command"
            )
        }
        context.startActivity(intent)
    }

    /**
     * Lock device via voice
     * Command: "Lock my phone" or "Lock device"
     */
    fun lockDevice(): Boolean {
        return if (isDeviceAdminEnabled) {
            try {
                val deviceAdminReceiver = ComponentName(context, DavidDeviceAdminReceiver::class.java)
                devicePolicyManager.lockNow()
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        } else {
            requestDeviceAdminPermission()
            false
        }
    }

    /**
     * Unlock device via biometric
     * Uses fingerprint or face recognition
     */
    fun unlockDeviceWithBiometric(activity: FragmentActivity, onSuccess: () -> Unit, onFailure: () -> Unit) {
        val biometricManager = BiometricManager.from(context)
        
        val canAuthenticate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
        } else {
            biometricManager.canAuthenticate()
        }

        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            val executor = java.util.concurrent.Executors.newSingleThreadExecutor()
            val biometricPrompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onFailure()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onFailure()
                }
            })

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock DAVID AI")
                .setSubtitle("Use your fingerprint or face")
                .setNegativeButtonText("Cancel")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build()

            biometricPrompt.authenticate(promptInfo)
        } else {
            onFailure()
        }
    }

    /**
     * Set screen timeout (auto lock after N seconds)
     * Command: "Set screen timeout to 2 minutes"
     */
    fun setScreenTimeout(timeoutSeconds: Int): Boolean {
        return if (isDeviceAdminEnabled) {
            try {
                val deviceAdminReceiver = ComponentName(context, DavidDeviceAdminReceiver::class.java)
                devicePolicyManager.setMaximumTimeToLock(deviceAdminReceiver, (timeoutSeconds * 1000).toLong())
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        } else {
            false
        }
    }

    /**
     * Check if device is locked
     */
    fun isDeviceLocked(): Boolean {
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as android.app.KeyguardManager
        return keyguardManager.isDeviceLocked
    }

    /**
     * Get lock status
     */
    fun getLockStatus(): String {
        return if (isDeviceLocked()) "Device is locked" else "Device is unlocked"
    }

    /**
     * Revoke device admin
     */
    fun revokeDeviceAdmin() {
        if (isDeviceAdminEnabled) {
            val deviceAdminReceiver = ComponentName(context, DavidDeviceAdminReceiver::class.java)
            devicePolicyManager.removeActiveAdmin(deviceAdminReceiver)
            isDeviceAdminEnabled = false
        }
    }
}

/**
 * Device Admin Receiver for device security operations
 */
class DavidDeviceAdminReceiver : AdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        // Device admin enabled
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        // Device admin disabled
    }
}
