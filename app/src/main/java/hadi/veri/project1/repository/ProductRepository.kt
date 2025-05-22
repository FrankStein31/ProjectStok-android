package hadi.veri.project1.repository

import hadi.veri.project1.api.ApiConfig
import hadi.veri.project1.api.ApiResponse
import hadi.veri.project1.api.SessionManager
import hadi.veri.project1.models.Product
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class ProductRepository(private val sessionManager: SessionManager) {
    
    suspend fun getAllProducts(): Result<ApiResponse<List<Product>>> {
        return try {
            val token = sessionManager.fetchAuthToken()
            val response = ApiConfig.getApiService(token).getAllProducts()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getProduct(id: Int): Result<ApiResponse<Product>> {
        return try {
            val token = sessionManager.fetchAuthToken()
            val response = ApiConfig.getApiService(token).getProduct(id)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createProduct(
        code: String,
        name: String,
        description: String?,
        price: Double,
        stock: Int,
        unit: String,
        imageFile: File?
    ): Result<ApiResponse<Product>> {
        return try {
            val token = sessionManager.fetchAuthToken()
            
            // Persiapkan image file jika ada
            val imagePart = if (imageFile != null && imageFile.exists()) {
                val requestFile = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("image", imageFile.name, requestFile)
            } else null
            
            val response = ApiConfig.getApiService(token).createProduct(
                code, name, description, price, stock, unit, imagePart
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateProduct(
        id: Int,
        code: String,
        name: String,
        description: String?,
        price: Double,
        stock: Int,
        unit: String,
        imageFile: File?
    ): Result<ApiResponse<Product>> {
        return try {
            val token = sessionManager.fetchAuthToken()
            
            // Persiapkan image file jika ada
            val imagePart = if (imageFile != null && imageFile.exists()) {
                val requestFile = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("image", imageFile.name, requestFile)
            } else null
            
            val response = ApiConfig.getApiService(token).updateProduct(
                id, code, name, description, price, stock, unit, imagePart
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteProduct(id: Int): Result<ApiResponse<Nothing>> {
        return try {
            val token = sessionManager.fetchAuthToken()
            val response = ApiConfig.getApiService(token).deleteProduct(id)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 