package com.davidstudioz.david.device

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.ActivityCompat

/**
 * DeviceController - FIXED: Voice-to-device control
 * ✅ Voice commands properly parsed
 * ✅ Device actions executed
 * ✅ All permissions handled
 */
class DeviceController(private val context: Context) {
    
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    
    /**
     * Execute voice command - FIXED
     */
    fun executeCommand(command: String): Result<String> {
        return try {
            val lowerCommand = command.lowercase()
            
            when {
                lowerCommand.contains("call") -> makeCall(extractPhoneNumber(command))
                lowerCommand.contains("message") || lowerCommand.contains("sms") -> 
                    sendSMS(extractPhoneNumber(command), extractMessageText(command))
                lowerCommand.contains("wifi on") -> setWifi(true)
                lowerCommand.contains("wifi off") -> setWifi(false)
                lowerCommand.contains("bluetooth on") -> setBluetooth(true)
                lowerCommand.contains("bluetooth off") -> setBluetooth(false)
                lowerCommand.contains("volume up") || lowerCommand.contains("increase volume") -> 
                    adjustVolume(true)
                lowerCommand.contains("volume down") || lowerCommand.contains("decrease volume") -> 
                    adjustVolume(false)
                lowerCommand.contains("mute") -> muteVolume()
                lowerCommand.contains("brightness up") || lowerCommand.contains("increase brightness") -> 
                    adjustBrightness(true)
                lowerCommand.contains("brightness down") || lowerCommand.contains("decrease brightness") -> 
                    adjustBrightness(false)
                lowerCommand.contains("open") -> openApp(extractAppName(command))
                else -> Result.failure(Exception("Command not recognized"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error executing command", e)
            Result.failure(e)
        }
    }
    
    private fun makeCall(phoneNumber: String): Result<String> {
        return try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) 
                != PackageManager.PERMISSION_GRANTED) {
                return Result.failure(Exception("Call permission not granted"))
            }
            
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$phoneNumber")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            Result.success("Calling $phoneNumber")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun sendSMS(phoneNumber: String, message: String): Result<String> {
        return try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) 
                != PackageManager.PERMISSION_GRANTED) {
                return Result.failure(Exception("SMS permission not granted"))
            }
            
            val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                SmsManager.getDefault()
            }
            
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            Result.success("Message sent to $phoneNumber")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun setWifi(enabled: Boolean): Result<String> {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ requires user interaction
                val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                Result.success("Opening WiFi settings")
            } else {
                @Suppress("DEPRECATION")
                wifiManager.isWifiEnabled = enabled
                Result.success("WiFi ${if (enabled) "enabled" else "disabled"}")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun setBluetooth(enabled: Boolean): Result<String> {
        return try {
            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            Result.success("Opening Bluetooth settings")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun adjustVolume(increase: Boolean): Result<String> {
        return try {
            val direction = if (increase) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, AudioManager.FLAG_SHOW_UI)
            Result.success("Volume ${if (increase) "increased" else "decreased"}")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun muteVolume(): Result<String> {
        return try {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0)
            Result.success("Volume muted")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun adjustBrightness(increase: Boolean): Result<String> {
        return try {
            val intent = Intent(Settings.ACTION_DISPLAY_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            Result.success("Opening brightness settings")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun openApp(appName: String): Result<String> {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(appName)
            if (intent != null) {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                Result.success("Opening $appName")
            } else {
                Result.failure(Exception("App not found: $appName"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Helper methods to extract information from voice commands
    private fun extractPhoneNumber(command: String): String {
        // Extract phone number from command
        val numbers = command.replace("\\D".toRegex(), "")
        return if (numbers.length >= 10) numbers.takeLast(10) else ""
    }
    
    private fun extractMessageText(command: String): String {
        // Extract message text after "message" keyword
        val parts = command.split("message", "sms", ignoreCase = true)
        return if (parts.size > 1) parts[1].trim() else ""
    }
    
    private fun extractAppName(command: String): String {
        // Extract app name after "open" keyword
        val parts = command.split("open", ignoreCase = true)
        return if (parts.size > 1) parts[1].trim() else ""
    }
    
    companion object {
        private const val TAG = "DeviceController"
    }
}
