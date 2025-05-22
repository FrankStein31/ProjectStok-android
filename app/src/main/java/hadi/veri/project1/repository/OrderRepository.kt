package hadi.veri.project1.repository

import hadi.veri.project1.api.ApiConfig
import hadi.veri.project1.api.ApiResponse
import hadi.veri.project1.api.SessionManager
import hadi.veri.project1.models.Order

class OrderRepository(private val sessionManager: SessionManager) {
    
    suspend fun getMyOrders(): Result<ApiResponse<List<Order>>> {
        return try {
            val token = sessionManager.fetchAuthToken()
            val response = ApiConfig.getApiService(token).getMyOrders()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAllOrders(
        userId: Int? = null,
        status: String? = null,
        startDate: String? = null,
        endDate: String? = null
    ): Result<ApiResponse<List<Order>>> {
        return try {
            val token = sessionManager.fetchAuthToken()
            val response = ApiConfig.getApiService(token).getAllOrders(userId, status, startDate, endDate)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getOrder(id: Int): Result<ApiResponse<Order>> {
        return try {
            val token = sessionManager.fetchAuthToken()
            val response = ApiConfig.getApiService(token).getOrder(id)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createOrder(
        items: List<Map<String, Any>>,
        shippingAddress: String,
        notes: String? = null
    ): Result<ApiResponse<Order>> {
        return try {
            val token = sessionManager.fetchAuthToken()
            val orderRequest = mapOf(
                "items" to items,
                "shipping_address" to shippingAddress,
                "notes" to notes
            )
            val response = ApiConfig.getApiService(token).createOrder(orderRequest)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateOrderStatus(orderId: Int, status: String): Result<ApiResponse<Order>> {
        return try {
            val token = sessionManager.fetchAuthToken()
            val response = ApiConfig.getApiService(token).updateOrderStatus(orderId, status)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 