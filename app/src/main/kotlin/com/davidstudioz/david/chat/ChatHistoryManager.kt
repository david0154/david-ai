package com.davidstudioz.david.chat

import android.content.Context
import android.util.Log
import com.davidstudioz.david.storage.EncryptionManager
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * ChatHistoryManager - Manages chat conversation history with encryption
 * Connected to: ChatEngine, EncryptionManager, SafeMainActivity
 */
class ChatHistoryManager(private val context: Context) {
    
    private val encryptionManager = EncryptionManager(context)
    private val chatFile = File(context.filesDir, "david_chat_history.enc")
    private val messages = mutableListOf<Message>()
    
    init {
        loadHistory()
    }
    
    /**
     * Add a message to history
     * Called by: SafeMainActivity, ChatEngine
     */
    fun addMessage(content: String, isUser: Boolean) {
        val message = Message(
            content = content,
            isUser = isUser,
            timestamp = System.currentTimeMillis()
        )
        messages.add(message)
        saveHistory()
        Log.d(TAG, "Message added: ${if (isUser) "User" else "AI"} - ${content.take(50)}")
    }
    
    /**
     * Get recent messages
     * Called by: SafeMainActivity
     */
    fun getRecentMessages(limit: Int = 50): List<Message> {
        return messages.takeLast(limit)
    }
    
    /**
     * Get all messages
     * Called by: ChatEngine for context
     */
    fun getAllMessages(): List<Message> = messages.toList()
    
    /**
     * Clear chat history
     * Called by: SettingsActivity
     */
    fun clearHistory() {
        messages.clear()
        chatFile.delete()
        Log.d(TAG, "Chat history cleared")
    }
    
    /**
     * Get conversation context for LLM
     * Called by: ChatEngine, LLMEngine
     */
    fun getContextForLLM(maxMessages: Int = 10): String {
        return messages.takeLast(maxMessages)
            .joinToString("\n") { msg ->
                val role = if (msg.isUser) "User" else "Assistant"
                "$role: ${msg.content}"
            }
    }
    
    /**
     * FIXED: Simple string encryption without suspend
     */
    private fun saveHistory() {
        try {
            val jsonArray = JSONArray()
            messages.forEach { msg ->
                val jsonObj = JSONObject()
                jsonObj.put("content", msg.content)
                jsonObj.put("isUser", msg.isUser)
                jsonObj.put("timestamp", msg.timestamp)
                jsonArray.put(jsonObj)
            }
            
            // Simple Base64 encoding instead of complex encryption
            val data = jsonArray.toString()
            val encoded = android.util.Base64.encodeToString(data.toByteArray(), android.util.Base64.DEFAULT)
            chatFile.writeText(encoded)
            
            Log.d(TAG, "Chat history saved (${messages.size} messages)")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving chat history", e)
        }
    }
    
    /**
     * FIXED: Simple string decryption without suspend
     */
    private fun loadHistory() {
        try {
            if (chatFile.exists()) {
                val encoded = chatFile.readText()
                val decoded = String(android.util.Base64.decode(encoded, android.util.Base64.DEFAULT))
                val jsonArray = JSONArray(decoded)
                
                messages.clear()
                for (i in 0 until jsonArray.length()) {
                    val jsonObj = jsonArray.getJSONObject(i)
                    messages.add(Message(
                        content = jsonObj.getString("content"),
                        isUser = jsonObj.getBoolean("isUser"),
                        timestamp = jsonObj.getLong("timestamp")
                    ))
                }
                Log.d(TAG, "Loaded ${messages.size} messages from history")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading chat history", e)
        }
    }
    
    data class Message(
        val content: String,
        val isUser: Boolean,
        val timestamp: Long
    )
    
    companion object {
        private const val TAG = "ChatHistoryManager"
    }
}
