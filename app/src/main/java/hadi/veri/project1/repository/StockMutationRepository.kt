package hadi.veri.project1.repository

import hadi.veri.project1.api.ApiConfig
import hadi.veri.project1.api.ApiResponse
import hadi.veri.project1.api.ProductWithStockMutationsResponse
import hadi.veri.project1.api.SessionManager
import hadi.veri.project1.models.StockMutation

class StockMutationRepository(private val sessionManager: SessionManager) {
    
    suspend fun getAllStockMutations(
        productId: Int? = null,
        type: String? = null,
        startDate: String? = null,
        endDate: String? = null
    ): Result<ApiResponse<List<StockMutation>>> {
        return try {
            val token = sessionManager.fetchAuthToken()
            val response = ApiConfig.getApiService(token).getAllStockMutations(productId, type, startDate, endDate)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getStockMutation(id: Int): Result<ApiResponse<StockMutation>> {
        return try {
            val token = sessionManager.fetchAuthToken()
            val response = ApiConfig.getApiService(token).getStockMutation(id)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createStockMutation(
        productId: Int,
        type: String, // in atau out
        quantity: Int,
        date: String,
        description: String? = null
    ): Result<ApiResponse<StockMutation>> {
        return try {
            val token = sessionManager.fetchAuthToken()
            val response = ApiConfig.getApiService(token).createStockMutation(
                productId, type, quantity, date, description
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getStockMutationsByProduct(productId: Int): Result<ApiResponse<ProductWithStockMutationsResponse>> {
        return try {
            val token = sessionManager.fetchAuthToken()
            val response = ApiConfig.getApiService(token).getStockMutationsByProduct(productId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 