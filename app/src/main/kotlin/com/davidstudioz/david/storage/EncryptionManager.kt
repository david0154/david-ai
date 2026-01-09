package com.davidstudioz.david.storage

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptionManager @Inject constructor(private val context: Context) {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val encryptedPreferences = EncryptedSharedPreferences.create(
        context,
        "david_ai_secure",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    fun saveSecureString(key: String, value: String) {
        encryptedPreferences.edit().putString(key, value).apply()
    }
    
    fun getSecureString(key: String, defaultValue: String = ""): String {
        return encryptedPreferences.getString(key, defaultValue) ?: defaultValue
    }
    
    fun saveSecureInt(key: String, value: Int) {
        encryptedPreferences.edit().putInt(key, value).apply()
    }
    
    fun getSecureInt(key: String, defaultValue: Int = 0): Int {
        return encryptedPreferences.getInt(key, defaultValue)
    }
    
    fun deleteSecureKey(key: String) {
        encryptedPreferences.edit().remove(key).apply()
    }
    
    fun encryptData(plaintext: String): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, masterKey.keysetHandle.primaryKey)
        return cipher.doFinal(plaintext.toByteArray())
    }
    
    fun decryptData(encryptedData: ByteArray): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, masterKey.keysetHandle.primaryKey)
        return String(cipher.doFinal(encryptedData))
    }
}
