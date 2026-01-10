package com.davidstudioz.david

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp

/**
 * DAVID AI Application Class
 * - Initializes global components
 * - Sets up crash handlers
 * - Configures WorkManager for background tasks
 * - Provides global error handling
 */
@HiltAndroidApp
class DavidApplication : Application(), Configuration.Provider {

    companion object {
        private const val TAG = "DavidApplication"
        private var instance: DavidApplication? = null

        fun getInstance(): DavidApplication? = instance
        fun getApplicationContext(): Context? = instance?.applicationContext
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        
        try {
            // Initialize WorkManager with custom configuration
            WorkManager.initialize(this, workManagerConfiguration)
            
            // Set up global exception handler to prevent app crashes
            val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
                try {
                    Log.e(TAG, "Uncaught exception in thread ${thread.name}", throwable)
                    
                    // Log error details for debugging
                    val errorMsg = """
                        App Crash Report:
                        Thread: ${thread.name}
                        Exception: ${throwable::class.simpleName}
                        Message: ${throwable.message}
                        Stack: ${throwable.stackTraceToString()}
                    """.trimIndent()
                    Log.e(TAG, errorMsg)
                    
                    // In production, you would send this to a crash reporting service
                    // For now, just log it and continue running
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error in exception handler", e)
                }
                
                // Call the default handler to allow system to handle it
                defaultHandler?.uncaughtException(thread, throwable)
            }
            
            Log.d(TAG, "DAVID AI Application initialized successfully")
            Log.d(TAG, "WorkManager configured")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during Application initialization", e)
            // Don't crash, try to continue
        }
    }

    /**
     * Configure WorkManager for background model downloads
     * Uses proper error handling and constraints
     */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .setTaskExecutor { command ->
                try {
                    command.run()
                } catch (e: Exception) {
                    Log.e(TAG, "Error executing work task", e)
                }
            }
            .build()

    override fun onTerminate() {
        super.onTerminate()
        instance = null
    }
}
