package com.davidstudioz.david.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.davidstudioz.david.BuildConfig
import com.davidstudioz.david.language.Language
import com.davidstudioz.david.language.LanguageManager
import kotlinx.coroutines.launch

/**
 * SettingsActivity - Complete Settings Screen
 * ✅ Language Selection (15 languages)
 * ✅ About Page with app info
 * ✅ Privacy Policy viewer
 * ✅ GitHub Repository Link
 * ✅ Open Source Acknowledgments
 * ✅ Bug reporting
 * ✅ Contact developer
 * ✅ Beautiful Jarvis-style UI
 */
class SettingsActivity : ComponentActivity() {

    private lateinit var languageManager: LanguageManager
    
    private var languages by mutableStateOf<List<Language>>(emptyList())
    private var currentLanguage by mutableStateOf("en")
    private var showLanguageDialog by mutableStateOf(false)
    private var showAboutDialog by mutableStateOf(false)
    private var showPrivacyDialog by mutableStateOf(false)
    private var showAcknowledgmentsDialog by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        languageManager = LanguageManager(this)
        loadLanguages()
        
        setContent {
            SettingsTheme {
                SettingsScreen()
            }
        }
    }
    
    private fun loadLanguages() {
        languages = languageManager.getSupportedLanguages()
        currentLanguage = languageManager.getCurrentLanguage()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun SettingsScreen() {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            "Settings",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { finish() }) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF0A0E27),
                        titleContentColor = Color(0xFF00E5FF),
                        navigationIconContentColor = Color(0xFF00E5FF)
                    )
                )
            },
            containerColor = Color(0xFF0A0E27)
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Language Section
                item {
                    SectionHeader("Language")
                }
                item {
                    SettingsCard(
                        icon = Icons.Default.Language,
                        title = "App Language",
                        subtitle = languages.find { it.code == currentLanguage }?.name ?: "English",
                        onClick = { showLanguageDialog = true }
                    )
                }
                
                item { Spacer(modifier = Modifier.height(16.dp)) }
                
                // About Section
                item {
                    SectionHeader("About")
                }
                item {
                    SettingsCard(
                        icon = Icons.Default.Info,
                        title = "About D.A.V.I.D AI",
                        subtitle = "Version ${BuildConfig.VERSION_NAME}",
                        onClick = { showAboutDialog = true }
                    )
                }
                item {
                    SettingsCard(
                        icon = Icons.Default.PrivacyTip,
                        title = "Privacy Policy",
                        subtitle = "We don't collect any data",
                        onClick = { showPrivacyDialog = true }
                    )
                }
                item {
                    SettingsCard(
                        icon = Icons.Default.Favorite,
                        title = "Open Source",
                        subtitle = "Acknowledgments & Credits",
                        onClick = { showAcknowledgmentsDialog = true }
                    )
                }
                
                item { Spacer(modifier = Modifier.height(16.dp)) }
                
                // Links Section
                item {
                    SectionHeader("Links")
                }
                item {
                    SettingsCard(
                        icon = Icons.Default.Code,
                        title = "GitHub Repository",
                        subtitle = "github.com/david0154/david-ai",
                        onClick = { openGitHub() }
                    )
                }
                item {
                    SettingsCard(
                        icon = Icons.Default.BugReport,
                        title = "Report Bug",
                        subtitle = "Help us improve",
                        onClick = { openGitHubIssues() }
                    )
                }
                item {
                    SettingsCard(
                        icon = Icons.Default.Email,
                        title = "Contact Developer",
                        subtitle = "david@nexuzy.in",
                        onClick = { openEmail() }
                    )
                }
            }
        }
        
        // Dialogs
        if (showLanguageDialog) {
            LanguageSelectionDialog(
                languages = languages,
                currentLanguage = currentLanguage,
                onDismiss = { showLanguageDialog = false },
                onLanguageSelected = { language ->
                    lifecycleScope.launch {
                        languageManager.setCurrentLanguage(language.code)
                        currentLanguage = language.code
                        showLanguageDialog = false
                    }
                }
            )
        }
        
        if (showAboutDialog) {
            AboutDialog(onDismiss = { showAboutDialog = false })
        }
        
        if (showPrivacyDialog) {
            PrivacyPolicyDialog(onDismiss = { showPrivacyDialog = false })
        }
        
        if (showAcknowledgmentsDialog) {
            AcknowledgmentsDialog(onDismiss = { showAcknowledgmentsDialog = false })
        }
    }

    @Composable
    private fun SectionHeader(title: String) {
        Text(
            text = title.uppercase(),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF64B5F6),
            letterSpacing = 1.sp,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }

    @Composable
    private fun SettingsCard(
        icon: ImageVector,
        title: String,
        subtitle: String,
        onClick: () -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E88E5).copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Open",
                    tint = Color(0xFF64B5F6)
                )
            }
        }
    }

    @Composable
    private fun LanguageSelectionDialog(
        languages: List<Language>,
        currentLanguage: String,
        onDismiss: () -> Unit,
        onLanguageSelected: (Language) -> Unit
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    "Select Language",
                    color = Color(0xFF00E5FF),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                LazyColumn {
                    items(languages) { language ->
                        LanguageItem(
                            language = language,
                            isSelected = language.code == currentLanguage,
                            onClick = { onLanguageSelected(language) }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("Close", color = Color(0xFF00E5FF))
                }
            },
            containerColor = Color(0xFF1F2937),
            textContentColor = Color.White
        )
    }

    @Composable
    private fun LanguageItem(
        language: Language,
        isSelected: Boolean,
        onClick: () -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .background(
                    if (isSelected) Color(0xFF00E5FF).copy(alpha = 0.2f)
                    else Color.Transparent,
                    RoundedCornerShape(8.dp)
                )
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = language.name,
                    fontSize = 16.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color(0xFF00E5FF) else Color.White
                )
                Text(
                    text = language.nativeName,
                    fontSize = 14.sp,
                    color = Color(0xFF9CA3AF)
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color(0xFF00E5FF)
                )
            }
        }
    }

    @Composable
    private fun AboutDialog(onDismiss: () -> Unit) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "D.A.V.I.D AI",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E5FF)
                    )
                    Text(
                        "Digital Assistant with Voice & Intelligent Decisions",
                        fontSize = 10.sp,
                        color = Color(0xFF64B5F6)
                    )
                }
            },
            text = {
                Column(modifier = Modifier.padding(8.dp)) {
                    InfoRow("Version", BuildConfig.VERSION_NAME)
                    InfoRow("Build", "2026.01.12")
                    InfoRow("Developer", "Nexuzy Tech Ltd.")
                    InfoRow("Email", "david@nexuzy.in")
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Features:",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E5FF)
                    )
                    FeatureItem("🎤 Voice Control")
                    FeatureItem("✋ Gesture Recognition")
                    FeatureItem("💬 AI Chat")
                    FeatureItem("🌍 15 Languages")
                    FeatureItem("🔒 Privacy First")
                    FeatureItem("🔌 100% Offline")
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "© 2026 Nexuzy Tech Ltd.",
                        fontSize = 10.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("Close", color = Color(0xFF00E5FF))
                }
            },
            containerColor = Color(0xFF1F2937),
            textContentColor = Color.White
        )
    }

    @Composable
    private fun PrivacyPolicyDialog(onDismiss: () -> Unit) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    "Privacy Policy",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00E5FF)
                )
            },
            text = {
                LazyColumn(modifier = Modifier.height(400.dp)) {
                    item {
                        Text(
                            "🔒 WE DO NOT COLLECT ANY DATA",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00FF88)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        PolicySection("Data Collection", listOf(
                            "❌ No personal information collected",
                            "❌ No data sent to external servers",
                            "❌ No user tracking or analytics",
                            "❌ No account required",
                            "❌ No cloud storage"
                        ))
                        
                        PolicySection("Data Storage", listOf(
                            "✅ Voice processed locally",
                            "✅ Images processed locally",
                            "✅ AI models stored locally",
                            "✅ Chat history stored locally",
                            "✅ Settings stored locally"
                        ))
                        
                        PolicySection("Your Rights", listOf(
                            "✅ Complete control over your data",
                            "✅ Delete chat history anytime",
                            "✅ Clear all data via settings",
                            "✅ Uninstall removes all data"
                        ))
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Full policy: github.com/david0154/david-ai/blob/main/PRIVACY_POLICY.md",
                            fontSize = 10.sp,
                            color = Color(0xFF64B5F6),
                            modifier = Modifier.clickable {
                                openUrl("https://github.com/david0154/david-ai/blob/main/PRIVACY_POLICY.md")
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Questions? Contact: david@nexuzy.in",
                            fontSize = 12.sp,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("Close", color = Color(0xFF00E5FF))
                }
            },
            containerColor = Color(0xFF1F2937),
            textContentColor = Color.White
        )
    }

    @Composable
    private fun AcknowledgmentsDialog(onDismiss: () -> Unit) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    "Open Source Acknowledgments",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00E5FF)
                )
            },
            text = {
                LazyColumn(modifier = Modifier.height(400.dp)) {
                    item {
                        Text(
                            "D.A.V.I.D AI is built with amazing open source technologies:",
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        AcknowledgmentItem(
                            "OpenAI Whisper",
                            "Speech recognition models",
                            "https://github.com/openai/whisper"
                        )
                        
                        AcknowledgmentItem(
                            "HuggingFace",
                            "TinyLlama, Qwen, Phi-2 models",
                            "https://huggingface.co"
                        )
                        
                        AcknowledgmentItem(
                            "ONNX",
                            "Vision classification models",
                            "https://onnx.ai"
                        )
                        
                        AcknowledgmentItem(
                            "Google MediaPipe",
                            "Hand tracking & gesture recognition",
                            "https://mediapipe.dev"
                        )
                        
                        AcknowledgmentItem(
                            "TensorFlow Lite",
                            "Language processing models",
                            "https://tensorflow.org/lite"
                        )
                        
                        AcknowledgmentItem(
                            "Open-Meteo",
                            "Free weather API",
                            "https://open-meteo.com"
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Thank you to all open source developers! 🙏",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00FF88)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("Close", color = Color(0xFF00E5FF))
                }
            },
            containerColor = Color(0xFF1F2937),
            textContentColor = Color.White
        )
    }

    @Composable
    private fun InfoRow(label: String, value: String) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 14.sp, color = Color(0xFF9CA3AF))
            Text(value, fontSize = 14.sp, color = Color.White)
        }
    }

    @Composable
    private fun FeatureItem(text: String) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = Color.White,
            modifier = Modifier.padding(vertical = 2.dp)
        )
    }

    @Composable
    private fun PolicySection(title: String, points: List<String>) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00E5FF)
            )
            Spacer(modifier = Modifier.height(4.dp))
            points.forEach { point ->
                Text(
                    point,
                    fontSize = 12.sp,
                    color = Color.White,
                    modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
                )
            }
        }
    }

    @Composable
    private fun AcknowledgmentItem(name: String, description: String, url: String) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(
                name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00E5FF)
            )
            Text(
                description,
                fontSize = 12.sp,
                color = Color(0xFF9CA3AF)
            )
            Text(
                url,
                fontSize = 10.sp,
                color = Color(0xFF64B5F6),
                modifier = Modifier.clickable {
                    openUrl(url)
                }
            )
        }
    }

    @Composable
    private fun SettingsTheme(content: @Composable () -> Unit) {
        MaterialTheme(
            colorScheme = darkColorScheme(
                primary = Color(0xFF00E5FF),
                secondary = Color(0xFF9CA3AF)
            ),
            content = content
        )
    }

    private fun openGitHub() {
        openUrl("https://github.com/david0154/david-ai")
    }

    private fun openGitHubIssues() {
        openUrl("https://github.com/david0154/david-ai/issues")
    }

    private fun openEmail() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:david@nexuzy.in")
            putExtra(Intent.EXTRA_SUBJECT, "D.A.V.I.D AI Feedback")
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            // Fallback: copy email to clipboard or show toast
        }
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        try {
            startActivity(intent)
        } catch (e: Exception) {
            // URL open failed
        }
    }
}
