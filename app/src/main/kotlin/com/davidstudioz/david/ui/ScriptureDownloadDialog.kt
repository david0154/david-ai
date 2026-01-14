package com.davidstudioz.david.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.davidstudioz.david.chat.ScriptureDownloadManager
import kotlinx.coroutines.launch

/**
 * ScriptureDownloadDialog - ONE-CLICK DOWNLOAD UI
 * ✅ Auto-download on first launch
 * ✅ Progress bar
 * ✅ Error handling
 * ✅ Download notification
 */
@Composable
fun ScriptureDownloadDialog(
    onDismiss: () -> Unit,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val downloadManager = remember { ScriptureDownloadManager(context) }
    val coroutineScope = rememberCoroutineScope()

    var isDownloading by remember { mutableStateOf(false) }
    var currentScripture by remember { mutableStateOf("") }
    var progress by remember { mutableStateOf(0) }
    var downloadComplete by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = { if (!isDownloading) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A1F3A)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title
                Text(
                    text = "📚 Download Scriptures",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00E5FF)
                )

                // Description
                Text(
                    text = if (downloadComplete) {
                        "✅ Download complete!"
                    } else if (isDownloading) {
                        "Downloading $currentScripture..."
                    } else {
                        "Download 850+ verses from Bhagavad Gita, Ramayana, and Puranas (${downloadManager.getEstimatedSize()})"
                    },
                    fontSize = 14.sp,
                    color = Color(0xFF90CAF9),
                    textAlign = TextAlign.Center
                )

                // Progress bar
                if (isDownloading) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LinearProgressIndicator(
                            progress = progress / 100f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = Color(0xFF00E5FF),
                            trackColor = Color(0xFF64B5F6).copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$progress%",
                            fontSize = 12.sp,
                            color = Color(0xFF64B5F6)
                        )
                    }
                }

                // Error message
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        fontSize = 12.sp,
                        color = Color(0xFFFF6E40),
                        textAlign = TextAlign.Center
                    )
                }

                // Download info
                if (!isDownloading && !downloadComplete) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DownloadInfoItem("📖 Bhagavad Gita", "700 verses")
                        DownloadInfoItem("🏛️ Ramayana", "50+ key verses")
                        DownloadInfoItem("📜 Puranas", "100+ wisdom verses")
                    }
                }

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!downloadComplete) {
                        // Cancel button
                        OutlinedButton(
                            onClick = onDismiss,
                            enabled = !isDownloading,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF00E5FF)
                            )
                        ) {
                            Text("Cancel")
                        }

                        // Download button
                        Button(
                            onClick = {
                                isDownloading = true
                                errorMessage = null
                                coroutineScope.launch {
                                    val result = downloadManager.downloadAllScriptures { scripture, prog ->
                                        currentScripture = scripture
                                        progress = prog
                                    }
                                    isDownloading = false
                                    if (result.success) {
                                        downloadComplete = true
                                    } else {
                                        errorMessage = result.message
                                    }
                                }
                            },
                            enabled = !isDownloading,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00E5FF),
                                contentColor = Color.Black
                            )
                        ) {
                            Text(if (isDownloading) "Downloading..." else "Download")
                        }
                    } else {
                        // Done button
                        Button(
                            onClick = {
                                onComplete()
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00E5FF),
                                contentColor = Color.Black
                            )
                        ) {
                            Text("Done")
                        }
                    }
                }

                // Network info
                if (!isDownloading && !downloadComplete) {
                    Text(
                        text = "⚠️ Requires internet connection",
                        fontSize = 10.sp,
                        color = Color(0xFF64B5F6).copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun DownloadInfoItem(title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color(0xFF1E88E5).copy(alpha = 0.1f),
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            color = Color(0xFFE0E0E0)
        )
        Text(
            text = subtitle,
            fontSize = 12.sp,
            color = Color(0xFF64B5F6)
        )
    }
}