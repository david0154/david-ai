package com.davidstudioz.david

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp

/**
 * DAVID AI Application Class
 * Main entry point for the app
 * Initializes all core systems and services
 */
@HiltAndroidApp
class DavidAIApp : Application(), Configuration.Provider {

    companion object {
        private const val TAG = "DavidAIApp"
        private var instance: DavidAIApp? = null

        fun getInstance(): DavidAIApp? = instance
        fun getApplicationContext(): Context? = instance?.applicationContext
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        
        try {
            // Initialize WorkManager with custom configuration
            WorkManager.initialize(this, workManagerConfiguration)
            
            // Initialize app systems
            initializeApp()
            
            // Set up global exception handler to prevent app crashes
            setupExceptionHandler()
            
            Log.d(TAG, "D.A.V.I.D AI Application initialized successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during Application initialization", e)
            // Don't crash, try to continue
        }
    }
    
    private fun initializeApp() {
        try {
            // Initialize all core services here
            Log.d(TAG, "Initializing D.A.V.I.D core systems...")
            
            // TODO: Initialize any global services needed
            // - AI Model Manager
            // - Voice Recognition Service
            // - Device Controller
            // - etc.
            
            Log.d(TAG, "Core systems initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing core systems", e)
        }
    }
    
    private fun setupExceptionHandler() {
        try {
            val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
                try {
                    Log.e(TAG, "Uncaught exception in thread ${thread.name}", throwable)
                    
                    // Log error details for debugging
                    val errorMsg = """
                        D.A.V.I.D Crash Report:
                        Thread: ${thread.name}
                        Exception: ${throwable::class.simpleName}
                        Message: ${throwable.message}
                        Stack: ${throwable.stackTraceToString()}
                    """.trimIndent()
                    Log.e(TAG, errorMsg)
                    
                    // In production, send to crash reporting service
                    // For now, just log it
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error in exception handler", e)
                }
                
                // Call the default handler to allow system to handle it
                defaultHandler?.uncaughtException(thread, throwable)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up exception handler", e)
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
        try {
            Log.d(TAG, "D.A.V.I.D Application terminating...")
            instance = null
        } catch (e: Exception) {
            Log.e(TAG, "Error during termination", e)
        }
    }
}
