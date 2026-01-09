package com.davidstudioz.david

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DavidAIApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeApp()
    }
    
    private fun initializeApp() {
        // Initialize all services
    }
}