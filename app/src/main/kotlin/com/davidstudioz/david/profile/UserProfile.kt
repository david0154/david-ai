package com.davidstudioz.david.profile

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

/**
 * User Profile Manager
 * Stores user nickname, preferences, and settings
 * Persists data locally on device
 */
class UserProfile(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "david_user_profile",
        Context.MODE_PRIVATE
    )
    private val gson = Gson()

    // User data
    var nickname: String
        get() = prefs.getString(KEY_NICKNAME, "Friend") ?: "Friend"
        set(value) = prefs.edit().putString(KEY_NICKNAME, value).apply()

    var preferredLanguage: String
        get() = prefs.getString(KEY_LANGUAGE, "en") ?: "en"
        set(value) = prefs.edit().putString(KEY_LANGUAGE, value).apply()

    var voiceGender: String
        get() = prefs.getString(KEY_VOICE_GENDER, "neutral") ?: "neutral"
        set(value) = prefs.edit().putString(KEY_VOICE_GENDER, value).apply()

    var voiceSpeed: Float
        get() = prefs.getFloat(KEY_VOICE_SPEED, 1.0f)
        set(value) = prefs.edit().putFloat(KEY_VOICE_SPEED, value).apply()

    var hotWordEnabled: Boolean
        get() = prefs.getBoolean(KEY_HOT_WORD_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_HOT_WORD_ENABLED, value).apply()

    var phoneNumber: String
        get() = prefs.getString(KEY_PHONE_NUMBER, "") ?: ""
        set(value) = prefs.edit().putString(KEY_PHONE_NUMBER, value).apply()

    var email: String
        get() = prefs.getString(KEY_EMAIL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_EMAIL, value).apply()

    var googleUserId: String
        get() = prefs.getString(KEY_GOOGLE_USER_ID, "") ?: ""
        set(value) = prefs.edit().putString(KEY_GOOGLE_USER_ID, value).apply()

    var chatHistoryDays: Int
        get() = prefs.getInt(KEY_CHAT_HISTORY_DAYS, 120)
        set(value) = prefs.edit().putInt(KEY_CHAT_HISTORY_DAYS, value).apply()

    var isFirstLaunch: Boolean
        get() = prefs.getBoolean(KEY_FIRST_LAUNCH, true)
        set(value) = prefs.edit().putBoolean(KEY_FIRST_LAUNCH, value).apply()

    var createdAt: Long
        get() = prefs.getLong(KEY_CREATED_AT, System.currentTimeMillis())
        set(value) = prefs.edit().putLong(KEY_CREATED_AT, value).apply()

    /**
     * Get personalized greeting using nickname
     */
    fun getGreeting(): String {
        return "Hi $nickname, I'm here to help!"
    }

    /**
     * Get personalized response
     */
    fun personalizeMessage(message: String): String {
        return message.replace("{name}", nickname)
    }

    /**
     * Get device control preferences
     */
    fun getDeviceControlEnabled(): Boolean {
        return prefs.getBoolean(KEY_DEVICE_CONTROL, true)
    }

    fun setDeviceControlEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DEVICE_CONTROL, enabled).apply()
    }

    /**
     * Get gesture control preferences
     */
    fun getGestureControlEnabled(): Boolean {
        return prefs.getBoolean(KEY_GESTURE_CONTROL, true)
    }

    fun setGestureControlEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_GESTURE_CONTROL, enabled).apply()
    }

    /**
     * Clear all user data
     */
    fun clearProfile() {
        prefs.edit().clear().apply()
    }

    /**
     * Export profile as JSON
     */
    fun exportAsJson(): String {
        val data = mapOf(
            "nickname" to nickname,
            "language" to preferredLanguage,
            "voiceGender" to voiceGender,
            "voiceSpeed" to voiceSpeed,
            "hotWordEnabled" to hotWordEnabled,
            "createdAt" to createdAt
        )
        return gson.toJson(data)
    }

    companion object {
        private const val KEY_NICKNAME = "user_nickname"
        private const val KEY_LANGUAGE = "user_language"
        private const val KEY_VOICE_GENDER = "voice_gender"
        private const val KEY_VOICE_SPEED = "voice_speed"
        private const val KEY_HOT_WORD_ENABLED = "hot_word_enabled"
        private const val KEY_PHONE_NUMBER = "phone_number"
        private const val KEY_EMAIL = "email"
        private const val KEY_GOOGLE_USER_ID = "google_user_id"
        private const val KEY_CHAT_HISTORY_DAYS = "chat_history_days"
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_CREATED_AT = "created_at"
        private const val KEY_DEVICE_CONTROL = "device_control_enabled"
        private const val KEY_GESTURE_CONTROL = "gesture_control_enabled"
    }
}
