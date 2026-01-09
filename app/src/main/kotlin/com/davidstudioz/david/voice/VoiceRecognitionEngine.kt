package com.davidstudioz.david.voice

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*

/**
 * Voice Recognition Engine
 * Uses Whisper.cpp for offline speech-to-text
 * Supports 14 languages
 */
class VoiceRecognitionEngine(
    private val context: Context,
    private val onResult: (String, String) -> Unit  // text, language
) {

    private val TAG = "VoiceRecognitionEngine"
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private val recordingJob = Job()
    private val recordingScope = CoroutineScope(Dispatchers.Default + recordingJob)

    companion object {
        // Supported languages
        val SUPPORTED_LANGUAGES = mapOf(
            "en" to "English",
            "hi" to "Hindi",
            "bn" to "Bengali",
            "ta" to "Tamil",
            "te" to "Telugu",
            "mr" to "Marathi",
            "gu" to "Gujarati",
            "kn" to "Kannada",
            "ml" to "Malayalam",
            "pa" to "Punjabi",
            "ur" to "Urdu",
            "fr" to "French",
            "de" to "German",
            "es" to "Spanish"
        )

        // Audio recording parameters
        const val SAMPLE_RATE = 16000  // Whisper uses 16kHz
        const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }

    /**
     * Start voice recognition
     */
    fun startRecognition(language: String = "en"): Boolean {
        if (!hasAudioPermission()) {
            Log.w(TAG, "Audio permission not granted")
            return false
        }

        return try {
            val bufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT
            )

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize * 2
            ).apply {
                startRecording()
            }

            isRecording = true
            Log.d(TAG, "Voice recognition started (Language: ${SUPPORTED_LANGUAGES[language]})")

            // Start recording in background
            recordingScope.launch {
                captureAudio(language, bufferSize)
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start voice recognition", e)
            false
        }
    }

    /**
     * Stop voice recognition
     */
    fun stopRecognition() {
        isRecording = false
        try {
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
            Log.d(TAG, "Voice recognition stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping voice recognition", e)
        }
    }

    /**
     * Capture audio data
     */
    private suspend fun captureAudio(language: String, bufferSize: Int) {
        val audioData = ShortArray(bufferSize)
        val allAudioData = mutableListOf<Short>()

        while (isRecording) {
            val read = audioRecord?.read(audioData, 0, bufferSize) ?: break
            if (read > 0) {
                allAudioData.addAll(audioData.take(read))
                Log.d(TAG, "Captured $read bytes")
            }
        }

        // Process audio with Whisper
        if (allAudioData.isNotEmpty()) {
            recognizeAudio(allAudioData.toShortArray(), language)
        }
    }

    /**
     * Recognize audio using Whisper model
     * This would call native Whisper.cpp implementation
     */
    private fun recognizeAudio(audioData: ShortArray, language: String) {
        try {
            // Call native Whisper implementation
            val recognizedText = nativeRecognizeAudio(
                audioData,
                SAMPLE_RATE,
                language
            )

            if (recognizedText.isNotEmpty()) {
                onResult(recognizedText, language)
                Log.d(TAG, "Recognized: $recognizedText")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error recognizing audio", e)
        }
    }

    /**
     * Native Whisper recognition (JNI call)
     */
    private external fun nativeRecognizeAudio(
        audioData: ShortArray,
        sampleRate: Int,
        language: String
    ): String

    /**
     * Check audio permission
     */
    private fun hasAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Get supported languages
     */
    fun getSupportedLanguages(): List<String> {
        return SUPPORTED_LANGUAGES.values.toList()
    }

    /**
     * Get language code
     */
    fun getLanguageCode(language: String): String? {
        return SUPPORTED_LANGUAGES.entries.find { it.value.equals(language, ignoreCase = true) }?.key
    }

    /**
     * Release resources
     */
    fun release() {
        stopRecognition()
        recordingJob.cancel()
    }

    init {
        System.loadLibrary("whisper")
        Log.d(TAG, "Whisper library loaded")
    }
}
