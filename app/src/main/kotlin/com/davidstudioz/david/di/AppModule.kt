package com.davidstudioz.david.di

import android.content.Context
import com.davidstudioz.david.ai.LLMEngine
import com.davidstudioz.david.device.DeviceController
import com.davidstudioz.david.storage.EncryptionManager
import com.davidstudioz.david.voice.VoiceEngine
import com.davidstudioz.david.web.WebSearchEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Singleton
    @Provides
    fun provideVoiceEngine(@ApplicationContext context: Context): VoiceEngine {
        return VoiceEngine(context)
    }
    
    @Singleton
    @Provides
    fun provideLLMEngine(@ApplicationContext context: Context): LLMEngine {
        return LLMEngine(context)
    }
    
    @Singleton
    @Provides
    fun provideDeviceController(@ApplicationContext context: Context): DeviceController {
        return DeviceController(context)
    }
    
    @Singleton
    @Provides
    fun provideWebSearchEngine(@ApplicationContext context: Context): WebSearchEngine {
        // ✅ FIXED: WebSearchEngine now receives context parameter
        return WebSearchEngine(context)
    }
    
    @Singleton
    @Provides
    fun provideEncryptionManager(@ApplicationContext context: Context): EncryptionManager {
        return EncryptionManager(context)
    }
}
