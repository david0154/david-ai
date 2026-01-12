package com.davidstudioz.david.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidstudioz.david.language.LanguageManager
import com.davidstudioz.david.storage.EncryptionManager

/**
 * SettingsActivity - Full settings management
 * Connected to: SafeMainActivity, LanguageManager, EncryptionManager
 * ✅ FIXED: Use AutoMirrored icons
 * ✅ FIXED: Use .size property to get language count
 * ✅ FIXED: Remove duplicate SettingsScreen function
 * ✅ FIXED: Use isEncryptionEnabled() instead of isInitialized()
 */
@OptIn(ExperimentalMaterial3Api::class)
class SettingsActivity : ComponentActivity() {
    
    private lateinit var languageManager: LanguageManager
    private lateinit var encryptionManager: EncryptionManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            languageManager = LanguageManager(this)
            encryptionManager = EncryptionManager(this)
            
            setContent {
                MaterialTheme(
                    colorScheme = darkColorScheme(
                        primary = Color(0xFF00E5FF),
                        background = Color(0xFF0A0E27)
                    )
                ) {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = { Text("Settings") },
                                navigationIcon = {
                                    IconButton(onClick = { finish() }) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = Color(0xFF1A1F3A)
                                )
                            )
                        }
                    ) { padding ->
                        SettingsScreen(Modifier.padding(padding))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
        }
    }
    
    @Composable
    private fun SettingsScreen(modifier: Modifier = Modifier) {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "D.A.V.I.D Settings",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            items(getSettingsSections()) { section ->
                SettingsSectionCard(section)
            }
        }
    }
    
    @Composable
    private fun SettingsSectionCard(section: SettingsSection) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E88E5).copy(alpha = 0.1f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "${section.icon} ${section.title}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = section.description,
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF)
                )
            }
        }
    }
    
    /**
     * Build settings sections list with proper type handling
     * ✅ FIXED: Extract .size from List<Language> before string interpolation
     * ✅ FIXED: Use isEncryptionEnabled() method
     */
    private fun getSettingsSections(): List<SettingsSection> {
        // ✅ CORRECT: Get language count as Int from List<Language>
        val downloadedLanguages = languageManager.getDownloadedLanguages()
        val languageCount = downloadedLanguages.size
        
        // ✅ CORRECT: Check encryption status
        val encryptionStatus = if (encryptionManager.isEncryptionEnabled()) "Enabled" else "Disabled"
        
        return listOf(
            SettingsSection("🌐", "Languages", "$languageCount languages available"),
            SettingsSection("🎤", "Voice Settings", "Voice recognition and TTS configuration"),
            SettingsSection("✋", "Gesture Settings", "Gesture recognition sensitivity and controls"),
            SettingsSection("🔔", "Notifications", "Manage notification preferences"),
            SettingsSection("🔒", "Privacy & Security", "Data encryption: $encryptionStatus"),
            SettingsSection("ℹ️", "About D.A.V.I.D", "Version 1.0.0 - AI Assistant")
        )
    }
    
    data class SettingsSection(
        val icon: String,
        val title: String,
        val description: String
    )
    
    companion object {
        private const val TAG = "SettingsActivity"
    }
}
