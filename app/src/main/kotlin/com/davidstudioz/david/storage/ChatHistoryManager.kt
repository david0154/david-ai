package com.davidstudioz.david.storage

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

// Chat Message Entity
@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val message: String,
    val response: String,
    val timestamp: Long = System.currentTimeMillis(),
    val command: String? = null
)

// Room Database DAO
@Dao
interface ChatMessageDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)
    
    @Query("SELECT * FROM chat_messages WHERE userId = :userId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMessages(userId: String, limit: Int = 100): List<ChatMessage>
    
    @Query("SELECT * FROM chat_messages WHERE userId = :userId AND timestamp >= :sinceTimestamp ORDER BY timestamp DESC")
    suspend fun getMessagesSince(userId: String, sinceTimestamp: Long): List<ChatMessage>
    
    @Query("DELETE FROM chat_messages WHERE userId = :userId AND timestamp < :beforeTimestamp")
    suspend fun deleteOldMessages(userId: String, beforeTimestamp: Long)
    
    @Query("SELECT COUNT(*) FROM chat_messages WHERE userId = :userId")
    suspend fun getMessageCount(userId: String): Int
    
    @Query("DELETE FROM chat_messages WHERE userId = :userId")
    suspend fun clearUserHistory(userId: String)
}

// Room Database
@Database(entities = [ChatMessage::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatMessageDao(): ChatMessageDao
}

// Chat History Manager
@Singleton
class ChatHistoryManager @Inject constructor(
    private val context: Context,
    private val dao: ChatMessageDao
) {
    
    companion object {
        const val RETENTION_DAYS = 120
    }
    
    /**
     * Save chat message locally (Device only, no backend)
     */
    suspend fun saveChatMessage(
        userId: String,
        userMessage: String,
        assistantResponse: String,
        command: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            cleanOldMessages(userId)
            
            val message = ChatMessage(
                userId = userId,
                message = userMessage,
                response = assistantResponse,
                command = command,
                timestamp = System.currentTimeMillis()
            )
            
            dao.insertMessage(message)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get recent chat history (Last 100 messages)
     */
    suspend fun getRecentHistory(userId: String): Result<List<ChatMessage>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val messages = dao.getRecentMessages(userId, 100)
            Result.success(messages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get chat history for last N days
     */
    suspend fun getHistoryForDays(userId: String, days: Int = 7): Result<List<ChatMessage>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val sinceTimestamp = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
            val messages = dao.getMessagesSince(userId, sinceTimestamp)
            Result.success(messages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Auto-delete messages older than 120 days
     */
    suspend fun cleanOldMessages(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val beforeTimestamp = System.currentTimeMillis() - (RETENTION_DAYS * 24 * 60 * 60 * 1000L)
            dao.deleteOldMessages(userId, beforeTimestamp)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Clear all user chat history
     */
    suspend fun clearAllHistory(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            dao.clearUserHistory(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
