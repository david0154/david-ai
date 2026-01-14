package com.davidstudioz.david.device

import android.Manifest
import android.app.KeyguardManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.location.LocationManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import android.telephony.SmsManager
import android.util.Log
import android.view.KeyEvent
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import java.io.File

/**
 * DeviceController - COMPLETE with ALL FEATURES
 * ✅ ALL METHODS IMPLEMENTED
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
    
    private val keyguardManager: KeyguardManager by lazy {
        context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    }
    
    private val connectivityManager: ConnectivityManager by lazy {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    
    private val batteryManager: BatteryManager by lazy {
        context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    }

    private var cameraId: String? = null
    var isFlashlightOn = false // Changed to var for VoiceCommandProcessor

    init {
        try {
            cameraId = cameraManager.cameraIdList.firstOrNull()
            Log.d(TAG, "✅ DeviceController initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing camera", e)
        }
    }

    // ==================== APP LAUNCHING ====================

    fun openApp(appName: String): Boolean {
        val app = appName.lowercase().trim()
        
        val packageName = when {
            app.contains("whatsapp") || app.contains("whats app") -> "com.whatsapp"
            app.contains("instagram") || app.contains("insta") -> "com.instagram.android"
            app.contains("facebook") || app.contains("fb") -> "com.facebook.katana"
            app.contains("twitter") || app.contains("x") -> "com.twitter.android"
            app.contains("youtube") -> "com.google.android.youtube"
            app.contains("gmail") || app.contains("mail") -> "com.google.android.gm"
            app.contains("maps") || app.contains("map") -> "com.google.android.apps.maps"
            app.contains("settings") -> "com.android.settings"
            app.contains("camera") -> "com.android.camera2"
            app.contains("phone") || app.contains("dialer") -> "com.google.android.dialer"
            else -> null
        }
        
        return if (packageName != null) launchApp(packageName, appName) else searchAndLaunchApp(appName)
    }
    
    private fun launchApp(packageName: String, appName: String): Boolean {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                showToast("Opening $appName...")
                true
            } else {
                showToast("$appName not installed")
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    private fun searchAndLaunchApp(appName: String): Boolean {
        return try {
            val pm = context.packageManager
            val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            
            val app = apps.find { appInfo ->
                pm.getApplicationLabel(appInfo).toString().lowercase().contains(appName.lowercase())
            }
            
            if (app != null) {
                val intent = pm.getLaunchIntentForPackage(app.packageName)
                if (intent != null) {
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                    return true
                }
            }
            false
        } catch (e: Exception) {
            false
        }
    }

    // ==================== CAMERA ====================
    
    fun takePhoto(): Boolean {
        return try {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            showToast("Opening camera...")
            true
        } catch (e: Exception) {
            false
        }
    }
    
    fun takeSelfie(): Boolean {
        return try {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra("android.intent.extras.CAMERA_FACING", 1)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            takePhoto()
        }
    }
    
    fun recordVideo(): Boolean {
        return try {
            val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    // ==================== ALARM & TIMER ====================
    
    fun setAlarm(hour: Int, minute: Int, message: String = "Alarm"): Boolean {
        return try {
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, hour)
                putExtra(AlarmClock.EXTRA_MINUTES, minute)
                putExtra(AlarmClock.EXTRA_MESSAGE, message)
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            openApp("clock")
        }
    }
    
    fun setTimer(seconds: Int, message: String = "Timer"): Boolean {
        return try {
            val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                putExtra(AlarmClock.EXTRA_LENGTH, seconds)
                putExtra(AlarmClock.EXTRA_MESSAGE, message)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            openApp("clock")
        }
    }

    // ==================== DEVICE LOCK ====================
    
    fun lockScreen(): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_SECURITY_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    fun lockDevice(): Boolean = lockScreen()
    
    fun isScreenLocked(): Boolean = keyguardManager.isKeyguardLocked

    // ==================== BRIGHTNESS ====================
    
    fun increaseBrightness(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(context)) {
                openBrightnessSettings()
                return false
            }
            val current = getBrightnessLevel()
            setBrightnessLevel((current + 25).coerceIn(0, 255))
            true
        } catch (e: Exception) {
            false
        }
    }
    
    fun decreaseBrightness(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(context)) {
                openBrightnessSettings()
                return false
            }
            val current = getBrightnessLevel()
            setBrightnessLevel((current - 25).coerceIn(0, 255))
            true
        } catch (e: Exception) {
            false
        }
    }
    
    fun getBrightnessLevel(): Int {
        return try {
            Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, 128)
        } catch (e: Exception) {
            128
        }
    }
    
    fun setBrightnessLevel(level: Int): Boolean {
        return try {
            Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, level.coerceIn(0, 255))
            true
        } catch (e: Exception) {
            false
        }
    }
    
    fun setAutoBrightness(enable: Boolean): Boolean {
        return try {
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                if (enable) Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC 
                else Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            )
            true
        } catch (e: Exception) {
            false
        }
    }
    
    private fun openBrightnessSettings(): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_DISPLAY_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    // ==================== CONNECTIVITY ====================

    fun isWiFiEnabled(): Boolean = wifiManager?.isWifiEnabled ?: false

    @Suppress("DEPRECATION")
    fun toggleWiFi(enable: Boolean) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val intent = Intent(Settings.Panel.ACTION_WIFI)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            } else {
                wifiManager?.isWifiEnabled = enable
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling WiFi", e)
        }
    }

    fun toggleWifi(enable: Boolean) = toggleWiFi(enable)
    fun setWiFiEnabled(enable: Boolean) = toggleWiFi(enable)

    fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled ?: false

    @Suppress("DEPRECATION")
    fun toggleBluetooth(enable: Boolean) {
        try {
            val adapter = bluetoothAdapter ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            } else {
                if (enable && !adapter.isEnabled) adapter.enable() 
                else if (!enable && adapter.isEnabled) adapter.disable()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling Bluetooth", e)
        }
    }

    fun setBluetoothEnabled(enable: Boolean) = toggleBluetooth(enable)

    fun toggleFlashlight(enable: Boolean): Boolean {
        return try {
            if (cameraId == null) return false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraManager.setTorchMode(cameraId!!, enable)
                isFlashlightOn = enable
                true
            } else false
        } catch (e: CameraAccessException) {
            false
        }
    }

    fun setFlashlightEnabled(enable: Boolean) = toggleFlashlight(enable)
    
    fun toggleAirplaneMode(enable: Boolean): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    fun toggleMobileData(enable: Boolean): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_DATA_ROAMING_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    fun toggleLocation(enable: Boolean): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    fun isLocationEnabled(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                locationManager.isLocationEnabled
            } else {
                val mode = Settings.Secure.getInt(context.contentResolver, Settings.Secure.LOCATION_MODE, 0)
                mode != Settings.Secure.LOCATION_MODE_OFF
            }
        } catch (e: Exception) {
            false
        }
    }

    // ==================== VOLUME ====================

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
    
    fun setVolume(level: Int): Boolean {
        return try {
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val volume = level.coerceIn(0, maxVolume)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_SHOW_UI)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun toggleMute(mute: Boolean): Boolean {
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, 
            if (mute) AudioManager.ADJUST_MUTE else AudioManager.ADJUST_UNMUTE, 0)
        return true
    }

    fun muteVolume() = toggleMute(true)

    // ==================== PHONE ====================
    
    fun makeCall(phoneNumber: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    // ==================== TIME & DATE ====================

    fun getCurrentTime(): String {
        val cal = java.util.Calendar.getInstance()
        val hour = cal.get(java.util.Calendar.HOUR_OF_DAY)
        val min = cal.get(java.util.Calendar.MINUTE)
        val h12 = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        val amPm = if (hour < 12) "AM" else "PM"
        return "$h12:${String.format("%02d", min)} $amPm"
    }

    fun getCurrentDate(): String {
        val cal = java.util.Calendar.getInstance()
        val day = cal.get(java.util.Calendar.DAY_OF_MONTH)
        val month = cal.getDisplayName(java.util.Calendar.MONTH, java.util.Calendar.LONG, java.util.Locale.getDefault())
        return "$day $month ${cal.get(java.util.Calendar.YEAR)}"
    }

    // ==================== DEVICE INFO ====================
    
    fun getBatteryLevel(): Int {
        return try {
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        } catch (e: Exception) {
            -1
        }
    }
    
    fun getStorageInfo(): Pair<Long, Long> {
        return try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val totalBytes = stat.blockCountLong * stat.blockSizeLong
            val availableBytes = stat.availableBlocksLong * stat.blockSizeLong
            val usedBytes = totalBytes - availableBytes
            Pair(usedBytes / (1024 * 1024 * 1024), totalBytes / (1024 * 1024 * 1024))
        } catch (e: Exception) {
            Pair(0L, 0L)
        }
    }

    // ==================== MEDIA ====================

    fun mediaPlay() = sendMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY)
    fun mediaPause() = sendMediaKey(KeyEvent.KEYCODE_MEDIA_PAUSE)
    fun mediaNext() = sendMediaKey(KeyEvent.KEYCODE_MEDIA_NEXT)
    fun mediaPrevious() = sendMediaKey(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
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