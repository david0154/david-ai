package com.davidstudioz.david.device

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.AlarmClock
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.bluetooth.BluetoothAdapter
import android.location.LocationManager
import android.media.AudioManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

/**
 * Device Control
 * Execute 20+ voice commands to control device functions
 * All commands use system APIs - no root required
 */
class DeviceController(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    /**
     * Make a phone call
     * Command: "Call Mom" or "Call 9876543210"
     */
    fun makeCall(phoneNumber: String): Boolean {
        return try {
            val cleanNumber = phoneNumber.filter { it.isDigit() }
            if (cleanNumber.isEmpty()) return false

            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$cleanNumber"))
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CALL_PHONE) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Send SMS message
     * Command: "Send SMS to Mom - I'm coming home"
     */
    fun sendSMS(phoneNumber: String, message: String): Boolean {
        return try {
            val cleanNumber = phoneNumber.filter { it.isDigit() }
            if (cleanNumber.isEmpty() || message.isEmpty()) return false

            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.SEND_SMS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                val smsManager = SmsManager.getDefault()
                smsManager.sendTextMessage(cleanNumber, null, message, null, null)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Toggle WiFi
     * Command: "Turn on WiFi" or "Turn off WiFi"
     */
    fun toggleWiFi(enable: Boolean): Boolean {
        return try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as android.net.wifi.WifiManager
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CHANGE_NETWORK_STATE) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                wifiManager.isWifiEnabled = enable
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Toggle Bluetooth
     * Command: "Enable Bluetooth" or "Turn off Bluetooth"
     */
    fun toggleBluetooth(enable: Boolean): Boolean {
        return try {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_ADMIN) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                if (enable) {
                    bluetoothAdapter?.enable()
                } else {
                    bluetoothAdapter?.disable()
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Toggle GPS
     * Command: "Turn on GPS" or "Turn off GPS"
     */
    fun toggleGPS(enable: Boolean): Boolean {
        return try {
            if (enable) {
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            } else {
                // GPS off requires system-level change
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Control volume
     * Command: "Increase volume", "Decrease volume", "Set volume to 50%"
     */
    fun setVolume(level: Int): Boolean {
        return try {
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val newVolume = level.coerceIn(0, maxVolume)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, AudioManager.FLAG_SHOW_UI)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Control brightness
     * Command: "Increase brightness", "Decrease brightness", "Set brightness to 50%"
     */
    fun setBrightness(level: Int): Boolean {
        return try {
            val newLevel = level.coerceIn(0, 255)
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                newLevel
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Turn on/off flashlight
     * Command: "Turn on flashlight" or "Turn off flashlight"
     */
    fun toggleFlashlight(enable: Boolean): Boolean {
        return try {
            val intent = Intent(MediaStore.INTENT_ACTION_VIDEO_CAMERA)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            
            if (enable) {
                // This will open camera for flashlight
                context.startActivity(intent)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Take a screenshot/photo
     * Command: "Take a photo" or "Take screenshot"
     */
    fun takePhoto(): Boolean {
        return try {
            val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Open app by package name
     * Command: "Open WhatsApp" or "Launch Instagram"
     */
    fun openApp(appName: String): Boolean {
        return try {
            val appPackages = mapOf(
                "whatsapp" to "com.whatsapp",
                "instagram" to "com.instagram.android",
                "facebook" to "com.facebook.katana",
                "twitter" to "com.twitter.android",
                "telegram" to "org.telegram.messenger",
                "youtube" to "com.google.android.youtube",
                "gmail" to "com.google.android.gm",
                "maps" to "com.google.android.apps.maps",
                "camera" to "com.android.camera",
                "gallery" to "com.android.gallery3d"
            )

            val packageName = appPackages[appName.lowercase()] ?: return false
            val intent = context.packageManager.getLaunchIntentForPackage(packageName) ?: return false
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Set alarm
     * Command: "Set alarm for 7 AM"
     */
    fun setAlarm(hourOfDay: Int, minute: Int): Boolean {
        return try {
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, hourOfDay)
                putExtra(AlarmClock.EXTRA_MINUTES, minute)
                putExtra(AlarmClock.EXTRA_MESSAGE, "David AI Alarm")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Enable silent mode
     * Command: "Silent mode" or "Enable vibrate"
     */
    fun setSilentMode(silent: Boolean): Boolean {
        return try {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_NOTIFICATION_POLICY) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                audioManager.ringerMode = if (silent) {
                    AudioManager.RINGER_MODE_SILENT
                } else {
                    AudioManager.RINGER_MODE_NORMAL
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Vibrate device
     * Command: "Vibrate"
     */
    fun vibrate(duration: Long = 500): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(duration)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
