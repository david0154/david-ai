package com.davidstudioz.david.di

import android.content.Context
import com.davidstudioz.david.auth.GoogleAuthManager
// import com.davidstudioz.david.models.ModelManager
import com.davidstudioz.david.sync.DeviceOnlySync
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {
    
    @Singleton
    @Provides
    fun provideGoogleAuthManager(@ApplicationContext context: Context): GoogleAuthManager {
        return GoogleAuthManager(context)
    }
    
    // Temporarily commented to isolate build error
    // @Singleton
    // @Provides
    // fun provideModelManager(@ApplicationContext context: Context): ModelManager {
    //     return ModelManager(context)
    // }
    
    @Singleton
    @Provides
    fun provideDeviceOnlySync(@ApplicationContext context: Context): DeviceOnlySync {
        return DeviceOnlySync(context)
    }
}
