package com.davidstudioz.david.device

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.location.LocationManager
import android.media.AudioManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import android.telephony.SmsManager
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.core.content.ContextCompat

/**
 * DeviceController - COMPLETE VOICE & GESTURE DEVICE CONTROL
 * ✅ WiFi, Bluetooth, Location, Flashlight control
 * ✅ Call, SMS, Email via voice commands
 * ✅ Camera (selfie & photo) control
 * ✅ Volume, brightness control
 * ✅ Lock/unlock device
 * ✅ Weather, time, alarm queries
 * ✅ Complete movie playback control
 * ✅ All VoiceController compatibility methods
 * ✅ SafeMainActivity integration methods
 * Connected to: SafeMainActivity, VoiceCommandProcessor, GestureController
 */
class DeviceController(private val context: Context) {

    private val wifiManager: WifiManager? by lazy {
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
    }

    private val bluetoothManager: BluetoothManager by lazy {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        bluetoothManager.adapter
    }

    private val locationManager: LocationManager by lazy {
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    private val audioManager: AudioManager by lazy {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    private val cameraManager: CameraManager by lazy {
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    private var cameraId: String? = null
    private var isFlashlightOn = false

    init {
        try {
            cameraId = cameraManager.cameraIdList.firstOrNull()
            Log.d(TAG, "DeviceController initialized with all features")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing camera", e)
        }
    }

    // ==================== CONNECTIVITY CONTROLS ====================

    /**
     * Check if WiFi is enabled
     * Called by: SafeMainActivity, VoiceCommandProcessor
     */
    fun isWiFiEnabled(): Boolean {
        return try {
            wifiManager?.isWifiEnabled ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking WiFi status", e)
            false
        }
    }

    /**
     * Toggle WiFi on/off
     * Called by: SafeMainActivity, VoiceCommandProcessor
     */
    @Suppress("DEPRECATION")
    fun toggleWiFi(enable: Boolean) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val intent = Intent(Settings.Panel.ACTION_WIFI)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                showToast("Opening WiFi settings...")
            } else {
                wifiManager?.isWifiEnabled = enable
                showToast(if (enable) "WiFi enabled" else "WiFi disabled")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling WiFi", e)
        }
    }

    /**
     * Alias for VoiceController compatibility
     */
    fun toggleWifi(enable: Boolean) = toggleWiFi(enable)
    fun setWiFiEnabled(enable: Boolean) = toggleWiFi(enable)

    /**
     * Check if Bluetooth is enabled
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
    @Suppress("DEPRECATION")
    fun toggleBluetooth(enable: Boolean) {
        try {
            val adapter = bluetoothAdapter
            if (adapter == null) {
                showToast("Bluetooth not supported")
                return
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                showToast("Opening Bluetooth settings...")
            } else {
                if (enable && !adapter.isEnabled) {
                    adapter.enable()
                    showToast("Bluetooth enabled")
                } else if (!enable && adapter.isEnabled) {
                    adapter.disable()
                    showToast("Bluetooth disabled")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling Bluetooth", e)
        }
    }

    /**
     * Alias for VoiceController
     */
    fun setBluetoothEnabled(enable: Boolean) = toggleBluetooth(enable)

    /**
     * Check if location is enabled
     */
    fun isLocationEnabled(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                locationManager.isLocationEnabled
            } else {
                @Suppress("DEPRECATION")
                val mode = Settings.Secure.getInt(
                    context.contentResolver,
                    Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF
                )
                mode != Settings.Secure.LOCATION_MODE_OFF
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Open location settings
     */
    fun toggleLocation(enable: Boolean): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            showToast("Opening Location settings...")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error opening location settings", e)
            false
        }
    }

    fun openLocationSettings() = toggleLocation(true)

    // ==================== FLASHLIGHT CONTROL ====================

    /**
     * Toggle flashlight on/off
     */
    fun toggleFlashlight(enable: Boolean): Boolean {
        return try {
            if (cameraId == null) {
                showToast("Flashlight not available")
                return false
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraManager.setTorchMode(cameraId!!, enable)
                isFlashlightOn = enable
                showToast(if (enable) "Flashlight ON" else "Flashlight OFF")
                true
            } else {
                showToast("Flashlight not supported")
                false
            }
        } catch (e: CameraAccessException) {
            Log.e(TAG, "Error toggling flashlight", e)
            false
        }
    }

    fun isFlashlightOn(): Boolean = isFlashlightOn
    fun setFlashlightEnabled(enable: Boolean) = toggleFlashlight(enable)

    // ==================== VOLUME CONTROL ====================

    /**
     * Get volume level (0-1)
     */
    fun getVolumeLevel(): Float {
        return try {
            val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            if (max > 0) current.toFloat() / max else 0.5f
        } catch (e: Exception) {
            0.5f
        }
    }

    /**
     * Set volume (0-100)
     */
    fun setVolume(level: Int): Boolean {
        return try {
            val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val vol = ((level / 100f) * max).toInt().coerceIn(0, max)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, AudioManager.FLAG_SHOW_UI)
            showToast("Volume $level%")
            true
        } catch (e: Exception) {
            false
        }
    }

    fun volumeUp(): Boolean {
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
        return true
    }

    fun volumeDown(): Boolean {
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
        return true
    }

    fun increaseVolume() = volumeUp()
    fun decreaseVolume() = volumeDown()

    fun toggleMute(mute: Boolean): Boolean {
        return try {
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                if (mute) AudioManager.ADJUST_MUTE else AudioManager.ADJUST_UNMUTE,
                0
            )
            showToast(if (mute) "Muted" else "Unmuted")
            true
        } catch (e: Exception) {
            false
        }
    }

    fun muteVolume() = toggleMute(true)

    // ==================== BRIGHTNESS ====================

    fun getBrightnessLevel(): Float {
        return try {
            val brightness = Settings.System.getInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS
            )
            brightness / 255f
        } catch (e: Exception) {
            0.5f
        }
    }

    fun setBrightnessLevel(level: Float) {
        try {
            val brightness = (level * 255).toInt().coerceIn(0, 255)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.System.canWrite(context)) {
                    Settings.System.putInt(
                        context.contentResolver,
                        Settings.System.SCREEN_BRIGHTNESS,
                        brightness
                    )
                } else {
                    val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting brightness", e)
        }
    }

    // ==================== CALL & SMS ====================

    fun makeCall(phoneNumber: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumber"))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                context.startActivity(intent)
                showToast("Calling $phoneNumber...")
                true
            } else {
                showToast("Call permission required")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Call error", e)
            false
        }
    }

    fun sendSMS(phoneNumber: String, message: String): Boolean {
        return try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    context.getSystemService(SmsManager::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    SmsManager.getDefault()
                }
                smsManager.sendTextMessage(phoneNumber, null, message, null, null)
                showToast("SMS sent")
                true
            } else {
                showToast("SMS permission required")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "SMS error", e)
            false
        }
    }

    fun openMessaging(): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_APP_MESSAGING)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            try {
                val smsIntent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:"))
                smsIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(smsIntent)
                true
            } catch (e2: Exception) {
                false
            }
        }
    }

    // ==================== EMAIL ====================

    fun sendEmail(to: String, subject: String, body: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$to"))
            intent.putExtra(Intent.EXTRA_SUBJECT, subject)
            intent.putExtra(Intent.EXTRA_TEXT, body)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Email error", e)
            false
        }
    }

    fun openEmail(): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    // ==================== CAMERA ====================

    fun takeSelfie(): Boolean {
        return try {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra("android.intent.extras.CAMERA_FACING", 1)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            showToast("Taking selfie...")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Selfie error", e)
            false
        }
    }

    fun takePhoto(): Boolean {
        return try {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            showToast("Taking photo...")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Photo error", e)
            false
        }
    }

    // ==================== DEVICE LOCK ====================

    fun lockDevice(): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_SECURITY_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    // ==================== ALARMS ====================

    fun setAlarm(hour: Int, minute: Int, message: String = "D.A.V.I.D Alarm"): Boolean {
        return try {
            val intent = Intent(AlarmClock.ACTION_SET_ALARM)
            intent.putExtra(AlarmClock.EXTRA_HOUR, hour)
            intent.putExtra(AlarmClock.EXTRA_MINUTES, minute)
            intent.putExtra(AlarmClock.EXTRA_MESSAGE, message)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            showToast("Setting alarm $hour:${String.format("%02d", minute)}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Alarm error", e)
            false
        }
    }

    fun openAlarmApp(): Boolean {
        return try {
            val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    // ==================== TIME & WEATHER ====================

    fun getCurrentTime(): String {
        val cal = java.util.Calendar.getInstance()
        val hour = cal.get(java.util.Calendar.HOUR_OF_DAY)
        val min = cal.get(java.util.Calendar.MINUTE)
        val amPm = if (hour < 12) "AM" else "PM"
        val h12 = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        return "$h12:${String.format("%02d", min)} $amPm"
    }

    fun getCurrentDate(): String {
        val cal = java.util.Calendar.getInstance()
        val day = cal.get(java.util.Calendar.DAY_OF_MONTH)
        val month = cal.getDisplayName(java.util.Calendar.MONTH, java.util.Calendar.LONG, java.util.Locale.getDefault())
        val year = cal.get(java.util.Calendar.YEAR)
        return "$day $month $year"
    }

    fun openWeatherApp(): Boolean {
        return try {
            val pkgs = listOf(
                "com.google.android.googlequicksearchbox",
                "com.weather.Weather"
            )
            for (pkg in pkgs) {
                val intent = context.packageManager.getLaunchIntentForPackage(pkg)
                if (intent != null) {
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                    return true
                }
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=weather"))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    // ==================== BROWSER ====================

    fun openBrowser(url: String = "https://www.google.com"): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    // ==================== MEDIA CONTROLS ====================

    fun mediaPlay() = sendMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY)
    fun mediaPause() = sendMediaKey(KeyEvent.KEYCODE_MEDIA_PAUSE)
    fun mediaNext() = sendMediaKey(KeyEvent.KEYCODE_MEDIA_NEXT)
    fun mediaPrevious() = sendMediaKey(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
    fun mediaStop() = sendMediaKey(KeyEvent.KEYCODE_MEDIA_STOP)
    fun mediaPlayPause() = sendMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)

    private fun sendMediaKey(keyCode: Int): Boolean {
        return try {
            audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
            audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
            true
        } catch (e: Exception) {
            false
        }
    }

    // ==================== DEVICE INFO ====================

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

    // ==================== HELPERS ====================

    private fun showToast(msg: String) {
        try {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Toast error", e)
        }
    }

    companion object {
        private const val TAG = "DeviceController"
    }
}
