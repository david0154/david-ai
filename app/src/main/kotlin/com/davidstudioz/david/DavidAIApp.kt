package com.davidstudioz.david

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp

/**
 * DAVID AI Application Class
 * Main entry point for the app with Hilt dependency injection
 * ✅ FIXED: Added @HiltAndroidApp annotation for proper Hilt initialization
 */
@HiltAndroidApp  // ✅ CRITICAL FIX: Enable Hilt dependency injection
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
            Log.d(TAG, "D.A.V.I.D AI Application starting with Hilt...")
            
            // Initialize WorkManager with custom configuration
            try {
                WorkManager.initialize(this, workManagerConfiguration)
                Log.d(TAG, "WorkManager initialized")
            } catch (e: Exception) {
                Log.e(TAG, "WorkManager init error (non-fatal)", e)
            }
            
            // Set up global exception handler to prevent app crashes
            setupExceptionHandler()
            
            Log.d(TAG, "D.A.V.I.D AI Application initialized successfully with Hilt DI")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during Application initialization", e)
            // Don't crash, try to continue
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
                        Android Version: ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})
                        Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}
                        Stack: ${throwable.stackTraceToString()}
                    """.trimIndent()
                    Log.e(TAG, errorMsg)
                    
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
     */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
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
