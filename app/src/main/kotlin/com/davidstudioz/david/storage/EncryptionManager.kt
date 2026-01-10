package com.davidstudioz.david.storage

import android.content.Context
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.aead.AeadKeyTemplates
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptionManager @Inject constructor(
    private val context: Context
) {
    
    init {
        // Register AEAD configuration
        AeadConfig.register()
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
            Result.failure(e)
        }
    }
}
