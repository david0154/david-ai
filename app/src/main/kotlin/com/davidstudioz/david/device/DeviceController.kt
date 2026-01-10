package com.davidstudioz.david.device

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Device Controller - Voice-to-Device Control
 * Handles system controls via voice commands
 * FIXED: Proper permission checks and command parsing
 */
class DeviceController(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    
    companion object {
        private const val TAG = "DeviceController"
        
        // Command patterns
        private val VOLUME_UP_PATTERNS = listOf("volume up", "increase volume", "louder", "turn up")
        private val VOLUME_DOWN_PATTERNS = listOf("volume down", "decrease volume", "quieter", "turn down")
        private val VOLUME_MUTE_PATTERNS = listOf("mute", "silence", "quiet")
        private val WIFI_ON_PATTERNS = listOf("wifi on", "enable wifi", "turn on wifi", "connect wifi")
        private val WIFI_OFF_PATTERNS = listOf("wifi off", "disable wifi", "turn off wifi", "disconnect wifi")
        private val BLUETOOTH_ON_PATTERNS = listOf("bluetooth on", "enable bluetooth", "turn on bluetooth")
        private val BLUETOOTH_OFF_PATTERNS = listOf("bluetooth off", "disable bluetooth", "turn off bluetooth")
        private val CALL_PATTERNS = listOf("call", "phone", "dial")
        private val SMS_PATTERNS = listOf("send sms", "send message", "text", "message")
        private val BRIGHTNESS_UP_PATTERNS = listOf("brightness up", "increase brightness", "brighter")
        private val BRIGHTNESS_DOWN_PATTERNS = listOf("brightness down", "decrease brightness", "dimmer")
        private val FLASHLIGHT_ON_PATTERNS = listOf("flashlight on", "torch on", "light on")
        private val FLASHLIGHT_OFF_PATTERNS = listOf("flashlight off", "torch off", "light off")
    }

    /**
     * Process voice command and execute device control
     */
    suspend fun processCommand(command: String): DeviceCommandResult = withContext(Dispatchers.Main) {
        val lowerCommand = command.lowercase().trim()
        Log.d(TAG, "Processing command: $lowerCommand")

        try {
            when {
                // Volume controls
                matchesAnyPattern(lowerCommand, VOLUME_UP_PATTERNS) -> {
                    adjustVolume(AudioManager.ADJUST_RAISE)
                }
                matchesAnyPattern(lowerCommand, VOLUME_DOWN_PATTERNS) -> {
                    adjustVolume(AudioManager.ADJUST_LOWER)
                }
                matchesAnyPattern(lowerCommand, VOLUME_MUTE_PATTERNS) -> {
                    muteVolume()
                }
                
                // WiFi controls
                matchesAnyPattern(lowerCommand, WIFI_ON_PATTERNS) -> {
                    toggleWiFi(true)
                }
                matchesAnyPattern(lowerCommand, WIFI_OFF_PATTERNS) -> {
                    toggleWiFi(false)
                }
                
                // Bluetooth controls
                matchesAnyPattern(lowerCommand, BLUETOOTH_ON_PATTERNS) -> {
                    toggleBluetooth(true)
                }
                matchesAnyPattern(lowerCommand, BLUETOOTH_OFF_PATTERNS) -> {
                    toggleBluetooth(false)
                }
                
                // Phone calls
                matchesAnyPattern(lowerCommand, CALL_PATTERNS) -> {
                    val phoneNumber = extractPhoneNumber(command)
                    makeCall(phoneNumber)
                }
                
                // SMS
                matchesAnyPattern(lowerCommand, SMS_PATTERNS) -> {
                    val (number, message) = extractSMSDetails(command)
                    sendSMS(number, message)
                }
                
                // Brightness
                matchesAnyPattern(lowerCommand, BRIGHTNESS_UP_PATTERNS) -> {
                    adjustBrightness(increase = true)
                }
                matchesAnyPattern(lowerCommand, BRIGHTNESS_DOWN_PATTERNS) -> {
                    adjustBrightness(increase = false)
                }
                
                else -> {
                    DeviceCommandResult(false, "Unknown command: $command")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing command", e)
            DeviceCommandResult(false, "Error: ${e.message}")
        }
    }

    private fun matchesAnyPattern(command: String, patterns: List<String>): Boolean {
        return patterns.any { pattern -> command.contains(pattern) }
    }

    private fun adjustVolume(direction: Int): DeviceCommandResult {
        return try {
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                direction,
                AudioManager.FLAG_SHOW_UI
            )
            val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val percentage = (currentVolume * 100) / maxVolume
            DeviceCommandResult(true, "Volume ${if (direction > 0) "increased" else "decreased"} to $percentage%")
        } catch (e: Exception) {
            Log.e(TAG, "Error adjusting volume", e)
            DeviceCommandResult(false, "Cannot adjust volume")
        }
    }

    private fun muteVolume(): DeviceCommandResult {
        return try {
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_MUTE,
                AudioManager.FLAG_SHOW_UI
            )
            DeviceCommandResult(true, "Volume muted")
        } catch (e: Exception) {
            DeviceCommandResult(false, "Cannot mute volume")
        }
    }

    @Suppress("DEPRECATION")
    private fun toggleWiFi(enable: Boolean): DeviceCommandResult {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ requires user to manually toggle WiFi
                val intent = Intent(Settings.Panel.ACTION_WIFI)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                DeviceCommandResult(true, "Opening WiFi settings")
            } else {
                wifiManager.isWifiEnabled = enable
                DeviceCommandResult(true, "WiFi ${if (enable) "enabled" else "disabled"}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling WiFi", e)
            DeviceCommandResult(false, "Cannot toggle WiFi. Please use settings.")
        }
    }

    @Suppress("DEPRECATION", "MissingPermission")
    private fun toggleBluetooth(enable: Boolean): DeviceCommandResult {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Check BLUETOOTH_CONNECT permission
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return DeviceCommandResult(false, "Bluetooth permission required")
                }
            }

            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val bluetoothAdapter = bluetoothManager.adapter

            if (bluetoothAdapter == null) {
                return DeviceCommandResult(false, "Bluetooth not available")
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Android 13+ requires user interaction
                val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                DeviceCommandResult(true, "Opening Bluetooth settings")
            } else {
                if (enable) {
                    bluetoothAdapter.enable()
                } else {
                    bluetoothAdapter.disable()
                }
                DeviceCommandResult(true, "Bluetooth ${if (enable) "enabled" else "disabled"}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling Bluetooth", e)
            DeviceCommandResult(false, "Cannot toggle Bluetooth")
        }
    }

    private fun makeCall(phoneNumber: String): DeviceCommandResult {
        return try {
            if (phoneNumber.isBlank()) {
                return DeviceCommandResult(false, "Please specify a phone number")
            }

            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CALL_PHONE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return DeviceCommandResult(false, "Phone call permission required")
            }

            val intent = Intent(Intent.ACTION_CALL).apply {
                data = android.net.Uri.parse("tel:$phoneNumber")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            DeviceCommandResult(true, "Calling $phoneNumber")
        } catch (e: Exception) {
            Log.e(TAG, "Error making call", e)
            DeviceCommandResult(false, "Cannot make call")
        }
    }

    @Suppress("DEPRECATION")
    private fun sendSMS(phoneNumber: String, message: String): DeviceCommandResult {
        return try {
            if (phoneNumber.isBlank() || message.isBlank()) {
                return DeviceCommandResult(false, "Please specify phone number and message")
            }

            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.SEND_SMS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return DeviceCommandResult(false, "SMS permission required")
            }

            val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                SmsManager.getDefault()
            }

            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            DeviceCommandResult(true, "SMS sent to $phoneNumber")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending SMS", e)
            DeviceCommandResult(false, "Cannot send SMS")
        }
    }

    private fun adjustBrightness(increase: Boolean): DeviceCommandResult {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.System.canWrite(context)) {
                    return DeviceCommandResult(false, "Write settings permission required")
                }
            }

            val currentBrightness = Settings.System.getInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                128
            )

            val newBrightness = if (increase) {
                (currentBrightness + 25).coerceAtMost(255)
            } else {
                (currentBrightness - 25).coerceAtLeast(0)
            }

            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                newBrightness
            )

            val percentage = (newBrightness * 100) / 255
            DeviceCommandResult(true, "Brightness set to $percentage%")
        } catch (e: Exception) {
            Log.e(TAG, "Error adjusting brightness", e)
            DeviceCommandResult(false, "Cannot adjust brightness")
        }
    }

    private fun extractPhoneNumber(command: String): String {
        // Extract phone number from command
        // Format: "call 1234567890" or "phone 123-456-7890"
        val numbers = command.replace("[^0-9]".toRegex(), "")
        return if (numbers.length >= 10) numbers else ""
    }

    private fun extractSMSDetails(command: String): Pair<String, String> {
        // Extract phone number and message
        // Format: "send sms 1234567890 hello there"
        val parts = command.split(" ", limit = 4)
        if (parts.size >= 4) {
            val number = parts[2].replace("[^0-9]".toRegex(), "")
            val message = parts[3]
            return Pair(number, message)
        }
        return Pair("", "")
    }

    /**
     * Get list of supported commands
     */
    fun getSupportedCommands(): List<String> {
        return listOf(
            "Volume up/down/mute",
            "WiFi on/off",
            "Bluetooth on/off",
            "Call [number]",
            "Send SMS [number] [message]",
            "Brightness up/down",
            "Flashlight on/off"
        )
    }
}

data class DeviceCommandResult(
    val success: Boolean,
    val message: String
)
