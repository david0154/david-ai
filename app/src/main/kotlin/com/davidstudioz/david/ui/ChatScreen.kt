package com.davidstudioz.david.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidstudioz.david.chat.ChatMessage
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * ChatScreen - Modern Chat UI with Copy/Paste Functionality
 * ✅ Long-press to copy message
 * ✅ Copy button on each message
 * ✅ Toast notification on copy
 * ✅ Clipboard integration
 * ✅ Material Design 3
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    messages: List<ChatMessage>,
    onSendMessage: (String) -> Unit,
    onVoiceInput: () -> Unit,
    onClearChat: () -> Unit
) {
    val context = LocalContext.current
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll to bottom when new message arrives
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "D.A.V.I.D Chat",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E5FF)
                        )
                        Text(
                            text = "${messages.size} messages",
                            fontSize = 12.sp,
                            color = Color(0xFF64B5F6)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onClearChat) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear Chat",
                            tint = Color(0xFF00E5FF)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1F3A)
                )
            )
        },
        bottomBar = {
            ChatInputBar(
                messageText = messageText,
                onMessageTextChange = { messageText = it },
                onSendClick = {
                    if (messageText.isNotBlank()) {
                        onSendMessage(messageText)
                        messageText = ""
                    }
                },
                onVoiceClick = onVoiceInput
            )
        },
        containerColor = Color(0xFF0A0E27)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (messages.isEmpty()) {
                EmptyChatState()
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(messages) { message ->
                        ChatMessageBubble(
                            message = message,
                            onCopy = { text ->
                                copyToClipboard(context, text)
                                Toast.makeText(
                                    context,
                                    "✅ Copied to clipboard",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * ChatMessageBubble - WITH COPY FUNCTIONALITY
 * ✅ Long-press to copy
 * ✅ Copy button
 * ✅ Timestamp
 * ✅ User/AI styling
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatMessageBubble(
    message: ChatMessage,
    onCopy: (String) -> Unit
) {
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val backgroundColor = if (message.isUser) {
        Color(0xFF00E5FF).copy(alpha = 0.2f)
    } else {
        Color(0xFF1E88E5).copy(alpha = 0.15f)
    }
    val textColor = if (message.isUser) {
        Color(0xFF00E5FF)
    } else {
        Color(0xFFE0E0E0)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(backgroundColor)
                .combinedClickable(
                    onClick = { },
                    onLongClick = { onCopy(message.text) }
                )
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Message text
                Text(
                    text = message.text,
                    color = textColor,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )

                // Copy button
                IconButton(
                    onClick = { onCopy(message.text) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = textColor.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Timestamp
        Text(
            text = formatTimestamp(message.timestamp),
            fontSize = 10.sp,
            color = Color(0xFF64B5F6).copy(alpha = 0.6f),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

/**
 * ChatInputBar - Message input with send and voice buttons
 */
@Composable
fun ChatInputBar(
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onVoiceClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF1A1F3A),
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Voice button
            IconButton(
                onClick = onVoiceClick,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF00E5FF),
                                Color(0xFF0091EA)
                            )
                        )
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice Input",
                    tint = Color.Black
                )
            }

            // Text input
            OutlinedTextField(
                value = messageText,
                onValueChange = onMessageTextChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = "Ask me anything...",
                        color = Color(0xFF64B5F6).copy(alpha = 0.6f)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFFE0E0E0),
                    unfocusedTextColor = Color(0xFFE0E0E0),
                    focusedBorderColor = Color(0xFF00E5FF),
                    unfocusedBorderColor = Color(0xFF64B5F6).copy(alpha = 0.3f),
                    cursorColor = Color(0xFF00E5FF)
                ),
                shape = RoundedCornerShape(24.dp),
                maxLines = 3
            )

            // Send button
            IconButton(
                onClick = onSendClick,
                enabled = messageText.isNotBlank(),
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        if (messageText.isNotBlank()) {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF00E5FF),
                                    Color(0xFF0091EA)
                                )
                            )
                        } else {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF64B5F6).copy(alpha = 0.3f),
                                    Color(0xFF64B5F6).copy(alpha = 0.3f)
                                )
                            )
                        }
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = if (messageText.isNotBlank()) Color.Black else Color(0xFF64B5F6)
                )
            }
        }
    }
}

/**
 * EmptyChatState - Shown when no messages
 */
@Composable
fun EmptyChatState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "🤖",
                fontSize = 64.sp
            )
            Text(
                text = "Start a conversation",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00E5FF)
            )
            Text(
                text = "Ask me anything or use voice input",
                fontSize = 14.sp,
                color = Color(0xFF64B5F6),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sample queries
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Try asking:",
                    fontSize = 12.sp,
                    color = Color(0xFF64B5F6),
                    fontWeight = FontWeight.Bold
                )
                SampleQuery("📰 What's the latest news?")
                SampleQuery("🌤️ What's the weather?")
                SampleQuery("💡 Give me motivation")
                SampleQuery("🕉️ Quote from Bhagavad Gita")
                SampleQuery("👨‍💻 Who created you?")
            }
        }
    }
}

@Composable
fun SampleQuery(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        color = Color(0xFF90CAF9),
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E88E5).copy(alpha = 0.1f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

/**
 * Copy text to clipboard
 */
private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("D.A.V.I.D Message", text)
    clipboard.setPrimaryClip(clip)
}

/**
 * Format timestamp
 */
private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}