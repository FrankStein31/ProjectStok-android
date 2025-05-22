package hadi.veri.project1.repository

import hadi.veri.project1.api.ApiConfig
import hadi.veri.project1.api.ApiResponse
import hadi.veri.project1.api.ProductWithStockCardsResponse
import hadi.veri.project1.api.SessionManager
import hadi.veri.project1.models.StockCard

class StockCardRepository(private val sessionManager: SessionManager) {
    
    suspend fun getAllStockCards(
        productId: Int? = null,
        startDate: String? = null,
        endDate: String? = null
    ): Result<ApiResponse<List<StockCard>>> {
        return try {
            val token = sessionManager.fetchAuthToken()
            val response = ApiConfig.getApiService(token).getAllStockCards(productId, startDate, endDate)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getStockCard(id: Int): Result<ApiResponse<StockCard>> {
        return try {
            val token = sessionManager.fetchAuthToken()
            val response = ApiConfig.getApiService(token).getStockCard(id)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createStockCard(
        productId: Int,
        initialStock: Int,
        inStock: Int,
        outStock: Int,
        finalStock: Int,
        date: String,
        notes: String? = null
    ): Result<ApiResponse<StockCard>> {
        return try {
            val token = sessionManager.fetchAuthToken()
            val response = ApiConfig.getApiService(token).createStockCard(
                productId, initialStock, inStock, outStock, finalStock, date, notes
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateStockCard(
        id: Int,
        initialStock: Int,
        inStock: Int,
        outStock: Int,
        finalStock: Int,
        date: String,
        notes: String? = null
    ): Result<ApiResponse<StockCard>> {
        return try {
            val token = sessionManager.fetchAuthToken()
            val response = ApiConfig.getApiService(token).updateStockCard(
                id, initialStock, inStock, outStock, finalStock, date, notes
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteStockCard(id: Int): Result<ApiResponse<Nothing>> {
        return try {
            val token = sessionManager.fetchAuthToken()
            val response = ApiConfig.getApiService(token).deleteStockCard(id)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getStockCardsByProduct(productId: Int): Result<ApiResponse<ProductWithStockCardsResponse>> {
        return try {
            val token = sessionManager.fetchAuthToken()
            val response = ApiConfig.getApiService(token).getStockCardsByProduct(productId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 