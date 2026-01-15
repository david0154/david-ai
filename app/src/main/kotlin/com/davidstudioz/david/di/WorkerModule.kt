package com.davidstudioz.david.di

import android.content.Context
import com.davidstudioz.david.chat.ChatManager
import com.davidstudioz.david.health.HealthTracker
import com.davidstudioz.david.models.ModelManager
import com.davidstudioz.david.sync.MultiDeviceSync
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WorkerModule {

    @Singleton
    @Provides
    fun provideChatManager(@ApplicationContext context: Context): ChatManager {
        return ChatManager(context)
    }

    @Singleton
    @Provides
    fun provideModelManager(@ApplicationContext context: Context): ModelManager {
        return ModelManager(context)
    }

    @Singleton
    @Provides
    fun provideMultiDeviceSync(@ApplicationContext context: Context): MultiDeviceSync {
        return MultiDeviceSync(context)
    }

    @Singleton
    @Provides
    fun provideHealthTracker(@ApplicationContext context: Context): HealthTracker {
        return HealthTracker(context)
    }
}
