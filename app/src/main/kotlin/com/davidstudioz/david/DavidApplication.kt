package com.davidstudioz.david

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager

/**
 * DAVID AI Application Class
 * - Initializes global components
 * - Sets up crash handlers
 * - Configures WorkManager for background tasks
 */
class DavidApplication : Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()
        
        // Set up global exception handler to prevent crashes
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(TAG, "Uncaught exception in thread ${thread.name}", throwable)
            // Log crash but don't crash the app
            // In production, send to crash reporting service
        }
        
        Log.d(TAG, "DAVID AI Application initialized")
    }

    /**
     * Configure WorkManager for background model downloads
     */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .build()

    companion object {
        private const val TAG = "DavidApplication"
    }
}
