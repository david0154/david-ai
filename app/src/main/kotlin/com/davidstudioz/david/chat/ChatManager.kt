package com.davidstudioz.david.chat

import android.content.Context
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*

/**
 * Chat Message Entity
 * Stores conversation history locally on device
 * Auto-deleted after 120 days
 */
@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String, // "user" or "ai" or "contact_name"
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val contactPhone: String = "", // For SMS/calls
    val messageType: String = "text" // "text", "sms", "call"
)

/**
 * Chat DAO (Data Access Object)
 */
@Dao
interface ChatDAO {
    @Insert
    suspend fun insert(message: ChatMessage)

    @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getMessages(limit: Int = 100): List<ChatMessage>

    @Query("SELECT * FROM chat_messages WHERE sender = :sender ORDER BY timestamp DESC")
    suspend fun getMessagesBySender(sender: String): List<ChatMessage>

    @Query("DELETE FROM chat_messages WHERE timestamp < :expiryTime")
    suspend fun deleteExpiredMessages(expiryTime: Long)

    @Query("DELETE FROM chat_messages")
    suspend fun clearAllMessages()

    @Query("SELECT COUNT(*) FROM chat_messages")
    suspend fun getMessageCount(): Int
}

/**
 * Chat Database
 */
@Database(entities = [ChatMessage::class], version = 1)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun chatDAO(): ChatDAO

    companion object {
        @Volatile
        private var instance: ChatDatabase? = null

        fun getInstance(context: Context): ChatDatabase {
            return instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context,
                    ChatDatabase::class.java,
                    "david_chat_database"
                ).build().also { instance = it }
            }
        }
    }
}

/**
 * Chat Manager
 * Handles chat operations: sending, receiving, storing, retrieving
 */
class ChatManager(context: Context) {
    private val database = ChatDatabase.getInstance(context)
    private val chatDAO = database.chatDAO()
    private val gson = Gson()

    companion object {
        private const val CHAT_RETENTION_DAYS = 120
    }

    /**
     * Send message to AI
     * Process with llama.cpp model on device
     */
    suspend fun sendMessageToAI(message: String, userProfile: com.davidstudioz.david.profile.UserProfile): String {
        // Save user message
        chatDAO.insert(
            ChatMessage(
                sender = userProfile.nickname,
                message = message,
                messageType = "text"
            )
        )

        // Get AI response (uses llama.cpp locally)
        val aiResponse = generateAIResponse(message, userProfile)

        // Save AI response
        chatDAO.insert(
            ChatMessage(
                sender = "David",
                message = aiResponse,
                messageType = "text"
            )
        )

        return aiResponse
    }

    /**
     * Generate AI response using offline LLM (llama.cpp)
     * All processing happens locally on device
     */
    private suspend fun generateAIResponse(userMessage: String, userProfile: com.davidstudioz.david.profile.UserProfile): String {
        // This integrates with llama.cpp via JNI bindings
        // Model runs completely offline on device

        val context = buildContext(userProfile)
        val prompt = "User: $userMessage\nAssistant: "

        // Call native llama.cpp inference
        val response = callLlamaInference(prompt, context)

        // Personalize response with user nickname
        return userProfile.personalizeMessage(response)
    }

    /**
     * Build context for AI from previous messages
     */
    private suspend fun buildContext(userProfile: com.davidstudioz.david.profile.UserProfile): String {
        val messages = chatDAO.getMessages(limit = 5)
        val context = StringBuilder()
        context.append("User's name: ${userProfile.nickname}\n")
        context.append("Language: ${userProfile.preferredLanguage}\n")
        context.append("\nRecent conversation:\n")

        messages.reversed().forEach { msg ->
            context.append("${msg.sender}: ${msg.message}\n")
        }

        return context.toString()
    }

    /**
     * Call native llama.cpp inference via JNI
     * This is a placeholder - actual implementation uses native bindings
     */
    private external fun callLlamaInference(prompt: String, context: String): String

    /**
     * Send SMS to contact
     * Command: "Send SMS to Mom - I'm coming home"
     */
    suspend fun sendSMS(contactName: String, phoneNumber: String, message: String) {
        chatDAO.insert(
            ChatMessage(
                sender = "You",
                message = "SMS to $contactName: $message",
                contactPhone = phoneNumber,
                messageType = "sms"
            )
        )
    }

    /**
     * Log phone call
     * Command: "Call Mom"
     */
    suspend fun logCall(contactName: String, phoneNumber: String, duration: Int) {
        chatDAO.insert(
            ChatMessage(
                sender = "System",
                message = "Called $contactName (${duration}s)",
                contactPhone = phoneNumber,
                messageType = "call"
            )
        )
    }

    /**
     * Get all chat messages
     */
    suspend fun getAllMessages(): List<ChatMessage> {
        cleanupExpiredMessages()
        return chatDAO.getMessages()
    }

    /**
     * Get conversation history with specific contact
     */
    suspend fun getContactHistory(contactName: String): List<ChatMessage> {
        return chatDAO.getMessagesBySender(contactName)
    }

    /**
     * Auto-cleanup messages older than 120 days
     */
    private suspend fun cleanupExpiredMessages() {
        val expiryTime = System.currentTimeMillis() - (CHAT_RETENTION_DAYS * 24 * 60 * 60 * 1000)
        chatDAO.deleteExpiredMessages(expiryTime)
    }

    /**
     * Export chat history as JSON
     * User can download their data anytime
     */
    suspend fun exportChatHistory(): String {
        val messages = chatDAO.getMessages(limit = 1000)
        return gson.toJson(messages)
    }

    /**
     * Clear all chat history
     */
    suspend fun clearAllChat() {
        chatDAO.clearAllMessages()
    }

    /**
     * Get message statistics
     */
    suspend fun getStats(): Map<String, Any> {
        val totalMessages = chatDAO.getMessageCount()
        val messages = chatDAO.getMessages(limit = 100)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val todayMessages = messages.filter { msg ->
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(msg.timestamp)) == today
        }.size

        return mapOf(
            "total" to totalMessages,
            "today" to todayMessages,
            "retention_days" to CHAT_RETENTION_DAYS
        )
    }
}
