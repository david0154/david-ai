package com.davidstudioz.david.device

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat

/**
 * DeviceController - Manages device settings and controls
 * Connected to: SafeMainActivity, VoiceCommandProcessor, GestureController
 */
class DeviceController(private val context: Context) {
    
    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    
    /**
     * Check WiFi status
     * Called by: SafeMainActivity, VoiceCommandProcessor
     */
    fun isWiFiEnabled(): Boolean {
        return try {
            wifiManager.isWifiEnabled
        } catch (e: Exception) {
            Log.e(TAG, "Error checking WiFi status", e)
            false
        }
    }
    
    /**
     * Toggle WiFi on/off
     * Called by: SafeMainActivity, VoiceCommandProcessor
     */
    fun toggleWiFi(enable: Boolean) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ requires user to manually enable/disable WiFi
                val intent = Intent(Settings.Panel.ACTION_WIFI)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                Log.d(TAG, "Opening WiFi settings")
            } else {
                @Suppress("DEPRECATION")
                wifiManager.isWifiEnabled = enable
                Log.d(TAG, "WiFi ${if (enable) "enabled" else "disabled"}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling WiFi", e)
        }
    }
    
    /**
     * Check Bluetooth status
     * Called by: SafeMainActivity, VoiceCommandProcessor
     */
    fun isBluetoothEnabled(): Boolean {
        return try {
            bluetoothAdapter?.isEnabled ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking Bluetooth status", e)
            false
        }
    }
    
    /**
     * Toggle Bluetooth on/off
     * Called by: SafeMainActivity, VoiceCommandProcessor
     */
    fun toggleBluetooth(enable: Boolean) {
        try {
            if (bluetoothAdapter == null) {
                Log.e(TAG, "Bluetooth not supported on this device")
                return
            }
            
            if (enable && !bluetoothAdapter.isEnabled) {
                // Request user to enable Bluetooth
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                Log.d(TAG, "Requesting Bluetooth enable")
            } else if (!enable && bluetoothAdapter.isEnabled) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // Android 13+ requires user action
                    val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                    Log.d(TAG, "Opening Bluetooth settings")
                } else {
                    @Suppress("DEPRECATION")
                    bluetoothAdapter.disable()
                    Log.d(TAG, "Bluetooth disabled")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling Bluetooth", e)
        }
    }
    
    /**
     * Get current brightness level
     * Called by: SafeMainActivity
     */
    fun getBrightnessLevel(): Float {
        return try {
            val brightness = Settings.System.getInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS
            )
            brightness / 255f
        } catch (e: Exception) {
            Log.e(TAG, "Error getting brightness", e)
            0.5f
        }
    }
    
    /**
     * Set brightness level
     * Called by: SafeMainActivity, VoiceCommandProcessor
     */
    fun setBrightnessLevel(level: Float) {
        try {
            val brightness = (level * 255).toInt()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.System.canWrite(context)) {
                    Settings.System.putInt(
                        context.contentResolver,
                        Settings.System.SCREEN_BRIGHTNESS,
                        brightness
                    )
                    Log.d(TAG, "Brightness set to $brightness")
                } else {
                    // Request write settings permission
                    val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting brightness", e)
        }
    }
    
    /**
     * Get device information
     * Called by: SafeMainActivity, VoiceCommandProcessor
     */
    fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            androidVersion = Build.VERSION.RELEASE,
            sdkVersion = Build.VERSION.SDK_INT,
            wifiEnabled = isWiFiEnabled(),
            bluetoothEnabled = isBluetoothEnabled(),
            brightness = getBrightnessLevel()
        )
    }
    
    data class DeviceInfo(
        val manufacturer: String,
        val model: String,
        val androidVersion: String,
        val sdkVersion: Int,
        val wifiEnabled: Boolean,
        val bluetoothEnabled: Boolean,
        val brightness: Float
    )
    
    companion object {
        private const val TAG = "DeviceController"
    }
}
