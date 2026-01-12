package com.davidstudioz.david.storage

import android.content.Context
import android.util.Log
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.aead.AeadKeyTemplates
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * EncryptionManager - Handles data encryption using Google Tink
 * ✅ ADDED: isEncryptionEnabled() method for SettingsActivity
 */
@Singleton
class EncryptionManager @Inject constructor(
    private val context: Context
) {
    
    private var initialized = false
    
    init {
        try {
            // Register AEAD configuration
            AeadConfig.register()
            initialized = true
            Log.d(TAG, "Encryption initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing encryption", e)
            initialized = false
        }
    }
    
    /**
     * Check if encryption is enabled
     * Called by: SettingsActivity
     */
    fun isEncryptionEnabled(): Boolean {
        return initialized
    }
    
    // FIXED: Initialize keysetHandle properly
    private val keysetHandle: KeysetHandle by lazy {
        try {
            AndroidKeysetManager.Builder()
                .withSharedPref(context, "david_keyset", "david_prefs")
                .withKeyTemplate(AeadKeyTemplates.AES256_GCM)
                .withMasterKeyUri("android-keystore://david_master_key")
                .build()
                .keysetHandle
        } catch (e: Exception) {
            Log.e(TAG, "Error creating keyset, using fallback", e)
            // Fallback to memory-only keyset if Android Keystore fails
            KeysetHandle.generateNew(AeadKeyTemplates.AES256_GCM)
        }
    }
    
    /**
     * Encrypt data
     */
    suspend fun encrypt(data: ByteArray): Result<ByteArray> = withContext(Dispatchers.IO) {
        return@withContext try {
            val aead = keysetHandle.getPrimitive(Aead::class.java)
            val encryptedData = aead.encrypt(data, null)
            Result.success(encryptedData)
        } catch (e: Exception) {
            Log.e(TAG, "Encryption error", e)
            Result.failure(e)
        }
    }
    
    /**
     * Decrypt data
     */
    suspend fun decrypt(encryptedData: ByteArray): Result<ByteArray> = withContext(Dispatchers.IO) {
        return@withContext try {
            val aead = keysetHandle.getPrimitive(Aead::class.java)
            val decryptedData = aead.decrypt(encryptedData, null)
            Result.success(decryptedData)
        } catch (e: Exception) {
            Log.e(TAG, "Decryption error", e)
            Result.failure(e)
        }
    }
    
    /**
     * Encrypt string
     */
    suspend fun encryptString(text: String): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val encrypted = encrypt(text.toByteArray())
            encrypted.map { android.util.Base64.encodeToString(it, android.util.Base64.DEFAULT) }
        } catch (e: Exception) {
            Log.e(TAG, "String encryption error", e)
            Result.failure(e)
        }
    }
    
    /**
     * Decrypt string
     */
    suspend fun decryptString(encryptedText: String): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val encryptedBytes = android.util.Base64.decode(encryptedText, android.util.Base64.DEFAULT)
            val decrypted = decrypt(encryptedBytes)
            decrypted.map { String(it) }
        } catch (e: Exception) {
            Log.e(TAG, "String decryption error", e)
            Result.failure(e)
        }
    }
    
    companion object {
        private const val TAG = "EncryptionManager"
    }
}
