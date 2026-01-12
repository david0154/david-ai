package com.davidstudioz.david.language

/**
 * Language data class for multi-language support
 * Used by: LanguageManager, SafeMainActivity, VoiceController
 */
data class Language(
    val code: String,
    val name: String,
    val nativeName: String,
    val isDownloaded: Boolean = false,
    val voiceSupport: Boolean = true,
    val gestureSupport: Boolean = true
)
