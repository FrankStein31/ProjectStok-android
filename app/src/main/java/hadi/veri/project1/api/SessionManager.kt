package hadi.veri.project1.api

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import hadi.veri.project1.models.User

class SessionManager(context: Context) {
    private var prefs: SharedPreferences
    
    companion object {
        const val USER_TOKEN = "user_token"
        const val USER_ID = "user_id"
        const val USER_NAME = "user_name"
        const val USER_EMAIL = "user_email"
        const val USER_ROLE = "user_role"
        const val USER_PHONE = "user_phone"
        const val USER_ADDRESS = "user_address"
    }
    
    init {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        
        prefs = EncryptedSharedPreferences.create(
            context,
            "secret_shared_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    // Fungsi untuk menyimpan data user dan token
    fun saveAuthUser(token: String, user: User) {
        val editor = prefs.edit()
        editor.putString(USER_TOKEN, token)
        editor.putInt(USER_ID, user.id)
        editor.putString(USER_NAME, user.name)
        editor.putString(USER_EMAIL, user.email)
        editor.putString(USER_ROLE, user.role)
        editor.putString(USER_PHONE, user.phone ?: "")
        editor.putString(USER_ADDRESS, user.address ?: "")
        editor.apply()
    }
    
    // Fungsi untuk mendapatkan token
    fun fetchAuthToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }
    
    // Fungsi untuk mendapatkan peran user
    fun getUserRole(): String {
        return prefs.getString(USER_ROLE, "") ?: ""
    }
    
    // Fungsi untuk mendapatkan data user
    fun getUser(): User {
        return User(
            id = prefs.getInt(USER_ID, 0),
            name = prefs.getString(USER_NAME, "") ?: "",
            email = prefs.getString(USER_EMAIL, "") ?: "",
            role = prefs.getString(USER_ROLE, "") ?: "",
            phone = prefs.getString(USER_PHONE, null),
            address = prefs.getString(USER_ADDRESS, null)
        )
    }
    
    // Fungsi untuk menghapus semua data session (logout)
    fun clearData() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
} 