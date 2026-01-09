package com.davidstudioz.david.auth

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class GoogleUser(
    val userId: String,
    val email: String,
    val name: String,
    val photoUrl: String?,
    val loginTime: Long = System.currentTimeMillis()
)

@Singleton
class GoogleAuthManager @Inject constructor(
    private val context: Context
) {
    
    private var googleSignInClient: GoogleSignInClient? = null
    
    /**
     * Initialize Google Sign-In (Call once in App startup)
     */
    fun initializeGoogleSignIn(): Result<Unit> {
        return try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("YOUR_WEB_CLIENT_ID") // From Firebase Console
                .requestEmail()
                .requestProfile()
                .build()
            
            googleSignInClient = GoogleSignIn.getClient(context, gso)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get currently signed-in user (Local cache)
     */
    fun getCurrentUser(): GoogleUser? {
        return try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            account?.let {
                GoogleUser(
                    userId = it.id ?: "",
                    email = it.email ?: "",
                    name = it.displayName ?: "User",
                    photoUrl = it.photoUrl?.toString()
                )
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Check if user is logged in
     */
    fun isUserLoggedIn(): Boolean {
        return getCurrentUser() != null
    }
    
    /**
     * Sign out user
     */
    suspend fun signOut(): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            googleSignInClient?.signOut()?.addOnCompleteListener {
                // User signed out
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get Google Sign-In Client for Activity
     */
    fun getGoogleSignInClient(): GoogleSignInClient? = googleSignInClient
}
