package hadi.veri.project1.repository

import hadi.veri.project1.api.ApiConfig
import hadi.veri.project1.api.ApiResponse
import hadi.veri.project1.api.AuthResponse
import hadi.veri.project1.api.SessionManager
import hadi.veri.project1.models.User

class AuthRepository(private val sessionManager: SessionManager) {
    
    suspend fun login(email: String, password: String): Result<ApiResponse<AuthResponse>> {
        return try {
            val response = ApiConfig.getApiService().login(email, password)
            
            // Simpan token dan user jika berhasil
            if (response.success && response.data != null) {
                val authData = response.data
                sessionManager.saveAuthUser(authData.token, authData.user)
            }
            
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun register(
        name: String,
        email: String,
        password: String,
        passwordConfirmation: String,
        phone: String? = null,
        address: String? = null
    ): Result<ApiResponse<AuthResponse>> {
        return try {
            val response = ApiConfig.getApiService().register(
                name, email, password, passwordConfirmation, phone, address
            )
            
            // Simpan token dan user jika berhasil
            if (response.success && response.data != null) {
                val authData = response.data
                sessionManager.saveAuthUser(authData.token, authData.user)
            }
            
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun logout(): Result<ApiResponse<Nothing>> {
        return try {
            val token = sessionManager.fetchAuthToken()
            val response = ApiConfig.getApiService(token).logout()
            
            // Hapus data session jika berhasil
            if (response.success) {
                sessionManager.clearData()
            }
            
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getProfile(): Result<ApiResponse<User>> {
        return try {
            val token = sessionManager.fetchAuthToken()
            val response = ApiConfig.getApiService(token).getProfile()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateProfile(name: String, phone: String? = null, address: String? = null): Result<ApiResponse<User>> {
        return try {
            val token = sessionManager.fetchAuthToken()
            val response = ApiConfig.getApiService(token).updateProfile(name, phone, address)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun isLoggedIn(): Boolean {
        return !sessionManager.fetchAuthToken().isNullOrEmpty()
    }
    
    fun isAdmin(): Boolean {
        return sessionManager.getUserRole() == "admin"
    }
    
    fun getUser(): User {
        return sessionManager.getUser()
    }
} 