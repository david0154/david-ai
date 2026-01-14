package com.davidstudioz.david.di

import android.content.Context
import com.davidstudioz.david.ai.chat.ChatModelManager
import com.davidstudioz.david.ai.gesture.GestureRecognizerManager
import com.davidstudioz.david.ai.language.LanguageModelManager
import com.davidstudioz.david.ai.voice.WhisperModelManager
import com.davidstudioz.david.core.model.ModelDownloadManager
import com.davidstudioz.david.core.model.ModelLifecycleManager
import com.davidstudioz.david.core.model.ModelValidator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for AI Model dependency injection
 * Provides singleton instances of all AI models with:
 * - Lazy initialization
 * - Proper lifecycle management
 * - Framework-specific providers
 */
@Module
@InstallIn(SingletonComponent::class)
object AIModelModule {

    /**
     * Provides ModelDownloadManager singleton
     */
    @Provides
    @Singleton
    fun provideModelDownloadManager(
        @ApplicationContext context: Context
    ): ModelDownloadManager {
        return ModelDownloadManager(context)
    }

    /**
     * Provides ModelValidator singleton
     */
    @Provides
    @Singleton
    fun provideModelValidator(
        @ApplicationContext context: Context
    ): ModelValidator {
        return ModelValidator(context)
    }

    /**
     * Provides ModelLifecycleManager singleton
     */
    @Provides
    @Singleton
    fun provideModelLifecycleManager(
        @ApplicationContext context: Context
    ): ModelLifecycleManager {
        return ModelLifecycleManager(context)
    }

    /**
     * Provides WhisperModelManager singleton for voice recognition
     * Uses TensorFlow Lite with GPU acceleration support
     */
    @Provides
    @Singleton
    fun provideWhisperModelManager(
        @ApplicationContext context: Context,
        lifecycleManager: ModelLifecycleManager,
        validator: ModelValidator
    ): WhisperModelManager {
        return WhisperModelManager(
            context = context,
            lifecycleManager = lifecycleManager,
            validator = validator
        )
    }

    /**
     * Provides ChatModelManager singleton for LLM inference
     * Uses MediaPipe LLM API with TensorFlow Lite fallback
     */
    @Provides
    @Singleton
    fun provideChatModelManager(
        @ApplicationContext context: Context,
        lifecycleManager: ModelLifecycleManager,
        validator: ModelValidator
    ): ChatModelManager {
        return ChatModelManager(
            context = context,
            lifecycleManager = lifecycleManager,
            validator = validator
        )
    }

    /**
     * Provides GestureRecognizerManager singleton
     * Uses MediaPipe Hand Landmarker
     */
    @Provides
    @Singleton
    fun provideGestureRecognizerManager(
        @ApplicationContext context: Context,
        lifecycleManager: ModelLifecycleManager,
        validator: ModelValidator
    ): GestureRecognizerManager {
        return GestureRecognizerManager(
            context = context,
            lifecycleManager = lifecycleManager,
            validator = validator
        )
    }

    /**
     * Provides LanguageModelManager singleton
     * Manages multilingual support with on-demand loading
     */
    @Provides
    @Singleton
    fun provideLanguageModelManager(
        @ApplicationContext context: Context,
        lifecycleManager: ModelLifecycleManager,
        validator: ModelValidator,
        downloadManager: ModelDownloadManager
    ): LanguageModelManager {
        return LanguageModelManager(
            context = context,
            lifecycleManager = lifecycleManager,
            validator = validator,
            downloadManager = downloadManager
        )
    }

    /**
     * Provides VisionModelManager singleton for image analysis
     * Uses ONNX Runtime
     */
    @Provides
    @Singleton
    fun provideVisionModelManager(
        @ApplicationContext context: Context,
        lifecycleManager: ModelLifecycleManager,
        validator: ModelValidator
    ): VisionModelManager {
        return VisionModelManager(
            context = context,
            lifecycleManager = lifecycleManager,
            validator = validator
        )
    }

    /**
     * Provides TTSModelManager singleton for text-to-speech
     * Uses TensorFlow Lite
     */
    @Provides
    @Singleton
    fun provideTTSModelManager(
        @ApplicationContext context: Context,
        lifecycleManager: ModelLifecycleManager,
        validator: ModelValidator
    ): TTSModelManager {
        return TTSModelManager(
            context = context,
            lifecycleManager = lifecycleManager,
            validator = validator
        )
    }

    /**
     * Provides EmotionDetectorManager singleton
     * Uses TensorFlow Lite - Optional model
     */
    @Provides
    @Singleton
    fun provideEmotionDetectorManager(
        @ApplicationContext context: Context,
        lifecycleManager: ModelLifecycleManager,
        validator: ModelValidator
    ): EmotionDetectorManager {
        return EmotionDetectorManager(
            context = context,
            lifecycleManager = lifecycleManager,
            validator = validator
        )
    }

    /**
     * Provides ObjectDetectorManager singleton
     * Uses MediaPipe Object Detector - Optional model
     */
    @Provides
    @Singleton
    fun provideObjectDetectorManager(
        @ApplicationContext context: Context,
        lifecycleManager: ModelLifecycleManager,
        validator: ModelValidator
    ): ObjectDetectorManager {
        return ObjectDetectorManager(
            context = context,
            lifecycleManager = lifecycleManager,
            validator = validator
        )
    }
}

/**
 * Placeholder classes for model managers that will be enhanced
 * These should be replaced with actual implementations from existing code
 */

/**
 * Vision Model Manager using ONNX Runtime
 */
class VisionModelManager(
    private val context: Context,
    private val lifecycleManager: ModelLifecycleManager,
    private val validator: ModelValidator
) {
    // Implementation will be added in Phase 2
    // This integrates with existing vision models
}

/**
 * TTS Model Manager
 */
class TTSModelManager(
    private val context: Context,
    private val lifecycleManager: ModelLifecycleManager,
    private val validator: ModelValidator
) {
    // Implementation will be added in Phase 2
    // This integrates with existing TTS functionality
}

/**
 * Emotion Detector Manager
 */
class EmotionDetectorManager(
    private val context: Context,
    private val lifecycleManager: ModelLifecycleManager,
    private val validator: ModelValidator
) {
    // Implementation will be added in Phase 2
    // Optional model for emotion detection from voice/video
}

/**
 * Object Detector Manager
 */
class ObjectDetectorManager(
    private val context: Context,
    private val lifecycleManager: ModelLifecycleManager,
    private val validator: ModelValidator
) {
    // Implementation will be added in Phase 2
    // Optional model for object detection in images
}
