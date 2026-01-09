package com.davidstudioz.david.device

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.telephony.SmsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceController @Inject constructor(private val context: Context) {
    
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    
    suspend fun makeCall(phoneNumber: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumber"))
            context.startActivity(intent)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun sendSMS(phoneNumber: String, message: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
            smsManager?.sendTextMessage(phoneNumber, null, message, null, null)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun toggleFlashlight(on: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val cameraId = cameraManager.cameraIdList[0]
            cameraManager.setTorchMode(cameraId, on)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun setVolume(level: Int): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, level, 0)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun setScreenBrightness(brightness: Int): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val contentResolver = context.contentResolver
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, brightness)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun toggleWiFi(on: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
            context.startActivity(intent)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun toggleBluetooth(on: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            context.startActivity(intent)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun setAlarm(hourOfDay: Int, minute: Int): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val intent = Intent(Intent.ACTION_SET_ALARM)
            intent.putExtra("android.intent.extra.alarm.HOUR", hourOfDay)
            intent.putExtra("android.intent.extra.alarm.MINUTES", minute)
            context.startActivity(intent)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun openApp(packageName: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                context.startActivity(intent)
                Result.success(Unit)
            } else {
                Result.failure(Exception("App not found: $packageName"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
