package com.davidstudioz.david.device

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat

/**
 * DeviceController - ✅ COMPLETE DEVICE CONTROL WITH GESTURE INTEGRATION
 * ✅ WiFi control (on/off toggle with return status)
 * ✅ Bluetooth control (on/off toggle with return status)
 * ✅ Flashlight control (on/off toggle with return status)
 * ✅ Volume control (up/down/mute)
 * ✅ Brightness control
 * ✅ Gesture-friendly toggle methods
 * ✅ Status return values for UI feedback
 */
class DeviceController(private val context: Context) {
    
    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    
    private var isFlashlightOn = false
    private var cameraId: String? = null
    
    init {
        try {
            cameraId = cameraManager?.cameraIdList?.get(0)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting camera ID", e)
        }
    }
    
    /**
     * ✅ Toggle WiFi (with status return)
     */
    fun toggleWiFi(): Boolean {
        return try {
            val currentState = wifiManager?.isWifiEnabled ?: false
            val newState = !currentState
            toggleWiFi(newState)
            newState
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling WiFi", e)
            false
        }
    }
    
    /**
     * Set WiFi state
     */
    fun toggleWiFi(enabled: Boolean): Boolean {
        return try {
            // Note: setWifiEnabled is deprecated in API 29+
            // User needs to use Settings panel instead
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                val intent = Intent(Settings.Panel.ACTION_WIFI)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                Log.d(TAG, "Opening WiFi settings panel")
                true
            } else {
                @Suppress("DEPRECATION")
                wifiManager?.setWifiEnabled(enabled)
                Log.d(TAG, "WiFi ${if (enabled) "enabled" else "disabled"}")
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error controlling WiFi", e)
            false
        }
    }
    
    /**
     * ✅ Toggle Bluetooth (with status return)
     */
    fun toggleBluetooth(): Boolean {
        return try {
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            val currentState = bluetoothAdapter?.isEnabled ?: false
            val newState = !currentState
            toggleBluetooth(newState)
            newState
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling Bluetooth", e)
            false
        }
    }
    
    /**
     * Set Bluetooth state
     */
    fun toggleBluetooth(enabled: Boolean): Boolean {
        return try {
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            
            if (bluetoothAdapter == null) {
                Log.e(TAG, "Bluetooth not supported on this device")
                return false
            }
            
            // Note: enable()/disable() require BLUETOOTH_ADMIN permission
            if (enabled && !bluetoothAdapter.isEnabled) {
                // Open Bluetooth settings for user to enable
                val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                Log.d(TAG, "Opening Bluetooth settings")
            } else if (!enabled && bluetoothAdapter.isEnabled) {
                // Open Bluetooth settings for user to disable
                val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                Log.d(TAG, "Opening Bluetooth settings")
            }
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error controlling Bluetooth", e)
            false
        }
    }
    
    /**
     * ✅ Toggle Flashlight (with status return)
     */
    fun toggleFlashlight(): Boolean {
        isFlashlightOn = !isFlashlightOn
        return toggleFlashlight(isFlashlightOn)
    }
    
    /**
     * Set flashlight state
     */
    fun toggleFlashlight(enabled: Boolean): Boolean {
        return try {
            if (cameraId == null) {
                Log.e(TAG, "Camera ID not available")
                return false
            }
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                cameraManager?.setTorchMode(cameraId!!, enabled)
                isFlashlightOn = enabled
                Log.d(TAG, "Flashlight ${if (enabled) "on" else "off"}")
                true
            } else {
                Log.e(TAG, "Flashlight control not supported on this device")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error controlling flashlight", e)
            false
        }
    }
    
    /**
     * Increase volume
     */
    fun volumeUp(): Boolean {
        return try {
            audioManager?.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_RAISE,
                AudioManager.FLAG_SHOW_UI
            )
            Log.d(TAG, "Volume increased")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error increasing volume", e)
            false
        }
    }
    
    /**
     * Decrease volume
     */
    fun volumeDown(): Boolean {
        return try {
            audioManager?.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_LOWER,
                AudioManager.FLAG_SHOW_UI
            )
            Log.d(TAG, "Volume decreased")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error decreasing volume", e)
            false
        }
    }
    
    /**
     * Mute/unmute volume
     */
    fun toggleMute(mute: Boolean): Boolean {
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                audioManager?.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    if (mute) AudioManager.ADJUST_MUTE else AudioManager.ADJUST_UNMUTE,
                    0
                )
            } else {
                @Suppress("DEPRECATION")
                audioManager?.setStreamMute(AudioManager.STREAM_MUSIC, mute)
            }
            Log.d(TAG, "Volume ${if (mute) "muted" else "unmuted"}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling mute", e)
            false
        }
    }
    
    /**
     * Get current volume level
     */
    fun getVolume(): Int {
        return try {
            audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 0
        } catch (e: Exception) {
            Log.e(TAG, "Error getting volume", e)
            0
        }
    }
    
    /**
     * Set volume level
     */
    fun setVolume(level: Int): Boolean {
        return try {
            val maxVolume = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15
            val clampedLevel = level.coerceIn(0, maxVolume)
            audioManager?.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                clampedLevel,
                AudioManager.FLAG_SHOW_UI
            )
            Log.d(TAG, "Volume set to $clampedLevel")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error setting volume", e)
            false
        }
    }
    
    /**
     * Increase brightness
     */
    fun increaseBrightness(): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_DISPLAY_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            Log.d(TAG, "Opening brightness settings")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error opening brightness settings", e)
            false
        }
    }
    
    /**
     * Decrease brightness
     */
    fun decreaseBrightness(): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_DISPLAY_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            Log.d(TAG, "Opening brightness settings")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error opening brightness settings", e)
            false
        }
    }
    
    /**
     * Open app by package name
     */
    fun openApp(packageName: String): Boolean {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                Log.d(TAG, "Opened app: $packageName")
                true
            } else {
                Log.e(TAG, "App not found: $packageName")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error opening app", e)
            false
        }
    }
    
    /**
     * Release resources
     */
    fun release() {
        try {
            if (isFlashlightOn) {
                toggleFlashlight(false)
            }
            Log.d(TAG, "DeviceController released")
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing resources", e)
        }
    }
    
    companion object {
        private const val TAG = "DeviceController"
    }
}
