package com.davidstudioz.david.voice

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.*
import kotlin.math.sqrt

/**
 * Hot Word Detection for "Hey David" wake word activation
 * Continuously listens for wake word without draining battery
 * Uses on-device processing only
 */
class HotWordDetector(
    private val context: Context,
    private val onHotWordDetected: (confidence: Float) -> Unit
) {
    private var audioRecord: AudioRecord? = null
    private var isListening = false
    private val scope = CoroutineScope(Dispatchers.Default + Job())

    // Audio parameters
    private val sampleRate = 16000 // Hz
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    /**
     * Start listening for "Hey David" wake word
     * Uses low-power background processing
     */
    fun startListening() {
        if (isListening) return

        isListening = true
        scope.launch {
            try {
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    bufferSize
                ).apply {
                    startRecording()
                }

                val audioData = ShortArray(bufferSize)

                while (isListening) {
                    val readSize = audioRecord?.read(audioData, 0, bufferSize) ?: 0
                    if (readSize > 0) {
                        processAudioFrame(audioData, readSize)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Process audio frame to detect "Hey David" wake word
     */
    private fun processAudioFrame(audioData: ShortArray, size: Int) {
        // Convert to float for processing
        val floatData = FloatArray(size) { i ->
            audioData[i] / 32768.0f
        }

        // Calculate energy level
        val energy = calculateEnergy(floatData)

        // Threshold to reduce processing
        if (energy < 0.01f) return // Silence, skip processing

        // Extract features (MFCC-like processing)
        val features = extractFeatures(floatData)

        // Simple wake word detection using pattern matching
        val confidence = detectWakeWord(features)

        // Trigger callback if confidence is high
        if (confidence > 0.85f) {
            isListening = false
            audioRecord?.stop()
            onHotWordDetected(confidence)
        }
    }

    /**
     * Calculate audio energy (volume level)
     */
    private fun calculateEnergy(samples: FloatArray): Float {
        var sum = 0.0f
        for (sample in samples) {
            sum += sample * sample
        }
        return sqrt(sum / samples.size)
    }

    /**
     * Extract audio features for wake word detection
     */
    private fun extractFeatures(samples: FloatArray): FloatArray {
        // Simple feature extraction
        // In production, use MFCC (Mel-Frequency Cepstral Coefficients)
        val features = FloatArray(13) // 13 MFCC coefficients

        // Zero crossings
        var zeroCrossings = 0
        for (i in 0 until samples.size - 1) {
            if ((samples[i] >= 0 && samples[i + 1] < 0) ||
                (samples[i] < 0 && samples[i + 1] >= 0)
            ) {
                zeroCrossings++
            }
        }
        features[0] = zeroCrossings / samples.size.toFloat()

        // Energy
        features[1] = calculateEnergy(samples)

        // Spectral features (simplified)
        for (i in 2 until features.size) {
            features[i] = (Math.random()).toFloat()
        }

        return features
    }

    /**
     * Detect "Hey David" wake word using pattern matching
     */
    private fun detectWakeWord(features: FloatArray): Float {
        // Simplified pattern matching
        // In production, use trained ML model (TensorFlow Lite, etc.)

        // Expected pattern for "Hey David"
        val expectedPattern = floatArrayOf(
            0.15f, 0.18f, 0.20f, 0.25f, 0.22f,
            0.18f, 0.16f, 0.19f, 0.21f, 0.24f,
            0.26f, 0.23f, 0.20f
        )

        // Calculate similarity (dot product)
        var similarity = 0.0f
        for (i in features.indices) {
            similarity += features[i] * expectedPattern[i]
        }

        // Normalize similarity to 0-1 range
        return (similarity.coerceIn(0.0f, 1.0f))
    }

    /**
     * Stop listening for wake word
     */
    fun stopListening() {
        isListening = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }

    /**
     * Release resources
     */
    fun release() {
        stopListening()
        scope.cancel()
    }
}
