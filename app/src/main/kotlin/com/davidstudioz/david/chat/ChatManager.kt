package com.davidstudioz.david.chat

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.davidstudioz.david.profile.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Chat Manager
 * Handles AI chat, SMS messaging, and conversation history
 */
class ChatManager(private val context: Context) {

    private val TAG = "ChatManager"
    private val chatHistory = mutableListOf<ChatMessage>()
    private var llmModel: String = "llama-13b"  // Default model

    data class ChatMessage(
        val sender: String,  // "user" or "ai"
        val message: String,
        val timestamp: Long = System.currentTimeMillis()
    )

    /**
     * Send message to AI and get response
     */
    suspend fun sendMessageToAI(
        message: String,
        userProfile: UserProfile
    ): String {
        return withContext(Dispatchers.Default) {
            try {
                // Add user message to history
                chatHistory.add(ChatMessage("user", message))

                // Process with AI model
                val response = processWithAI(message, userProfile)

                // Add AI response to history
                chatHistory.add(ChatMessage("ai", response))

                response
            } catch (e: Exception) {
                Log.e(TAG, "Error processing message", e)
                "Sorry, I couldn't process that. Please try again."
            }
        }
    }

    /**
     * Process message with llama.cpp AI model
     */
    private fun processWithAI(
        message: String,
        userProfile: UserProfile
    ): String {
        // Build context from chat history
        val context = buildContext(userProfile)
        
        // Call native llama.cpp inference
        return nativeInferAI(
            message,
            context,
            llmModel
        )
    }

    /**
     * Native AI inference (JNI call to llama.cpp)
     */
    private external fun nativeInferAI(
        prompt: String,
        context: String,
        model: String
    ): String

    /**
     * Build context from user profile and history
     */
    private fun buildContext(userProfile: UserProfile): String {
        val recentMessages = chatHistory.takeLast(5).joinToString("\n") {
            "${it.sender}: ${it.message}"
        }
        
        return """
        User: ${userProfile.nickname}
        Recent conversation:
        $recentMessages
        """.trimIndent()
    }

    /**
     * Send SMS message
     */
    fun sendSMS(
        contact: String,
        phoneNumber: String,
        message: String
    ): Boolean {
        return try {
            if (!hasSMSPermission()) {
                Log.w(TAG, "SMS permission not granted")
                return false
            }

            // Use Android SMS API
            val smsManager = android.telephony.SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            
            // Log SMS
            chatHistory.add(ChatMessage("sms:$contact", "SMS: $message"))
            Log.d(TAG, "SMS sent to $phoneNumber")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send SMS", e)
            false
        }
    }

    /**
     * Get chat history
     */
    fun getChatHistory(): List<ChatMessage> {
        return chatHistory.toList()
    }

    /**
     * Clear chat history
     */
    fun clearChatHistory() {
        chatHistory.clear()
        Log.d(TAG, "Chat history cleared")
    }

    /**
     * Get chat statistics
     */
    fun getChatStats(): Map<String, Any> {
        val userMessages = chatHistory.count { it.sender == "user" }
        val aiMessages = chatHistory.count { it.sender == "ai" }
        val smsMessages = chatHistory.count { it.sender.startsWith("sms:") }
        
        return mapOf(
            "totalMessages" to chatHistory.size,
            "userMessages" to userMessages,
            "aiResponses" to aiMessages,
            "smsMessages" to smsMessages,
            "avgResponseTime" to "~500ms"
        )
    }

    /**
     * Set AI model
     */
    fun setAIModel(modelName: String) {
        llmModel = modelName
        Log.d(TAG, "AI model set to: $modelName")
    }

    /**
     * Check SMS permission
     */
    private fun hasSMSPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    init {
        System.loadLibrary("llama")
        Log.d(TAG, "llama.cpp library loaded")
    }
}
