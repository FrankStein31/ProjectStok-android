package hadi.veri.project1.api

import hadi.veri.project1.models.Order
import hadi.veri.project1.models.Product
import hadi.veri.project1.models.StockCard
import hadi.veri.project1.models.StockMutation
import hadi.veri.project1.models.User
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    // AUTH
    @FormUrlEncoded
    @POST("register")
    suspend fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("password_confirmation") passwordConfirmation: String,
        @Field("phone") phone: String? = null,
        @Field("address") address: String? = null
    ): ApiResponse<AuthResponse>
    
    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): ApiResponse<AuthResponse>
    
    @POST("logout")
    suspend fun logout(): ApiResponse<Nothing>
    
    @GET("profile")
    suspend fun getProfile(): ApiResponse<User>
    
    @FormUrlEncoded
    @PUT("profile")
    suspend fun updateProfile(
        @Field("name") name: String,
        @Field("phone") phone: String? = null,
        @Field("address") address: String? = null
    ): ApiResponse<User>
    
    // PRODUCTS
    @GET("products")
    suspend fun getAllProducts(): ApiResponse<List<Product>>
    
    @GET("products/{id}")
    suspend fun getProduct(
        @Path("id") id: Int
    ): ApiResponse<Product>
    
    @Multipart
    @POST("products")
    suspend fun createProduct(
        @Part("code") code: String,
        @Part("name") name: String,
        @Part("description") description: String?,
        @Part("price") price: Double,
        @Part("stock") stock: Int,
        @Part("unit") unit: String,
        @Part image: okhttp3.MultipartBody.Part?
    ): ApiResponse<Product>
    
    @Multipart
    @PUT("products/{id}")
    suspend fun updateProduct(
        @Path("id") id: Int,
        @Part("code") code: String,
        @Part("name") name: String,
        @Part("description") description: String?,
        @Part("price") price: Double,
        @Part("stock") stock: Int,
        @Part("unit") unit: String,
        @Part image: okhttp3.MultipartBody.Part?
    ): ApiResponse<Product>
    
    @DELETE("products/{id}")
    suspend fun deleteProduct(
        @Path("id") id: Int
    ): ApiResponse<Nothing>
    
    // STOCK CARDS
    @GET("stock-cards")
    suspend fun getAllStockCards(
        @Query("product_id") productId: Int? = null,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null
    ): ApiResponse<List<StockCard>>
    
    @GET("stock-cards/{id}")
    suspend fun getStockCard(
        @Path("id") id: Int
    ): ApiResponse<StockCard>
    
    @FormUrlEncoded
    @POST("stock-cards")
    suspend fun createStockCard(
        @Field("product_id") productId: Int,
        @Field("initial_stock") initialStock: Int,
        @Field("in_stock") inStock: Int,
        @Field("out_stock") outStock: Int,
        @Field("final_stock") finalStock: Int,
        @Field("date") date: String,
        @Field("notes") notes: String? = null
    ): ApiResponse<StockCard>
    
    @FormUrlEncoded
    @PUT("stock-cards/{id}")
    suspend fun updateStockCard(
        @Path("id") id: Int,
        @Field("initial_stock") initialStock: Int,
        @Field("in_stock") inStock: Int,
        @Field("out_stock") outStock: Int,
        @Field("final_stock") finalStock: Int,
        @Field("date") date: String,
        @Field("notes") notes: String? = null
    ): ApiResponse<StockCard>
    
    @DELETE("stock-cards/{id}")
    suspend fun deleteStockCard(
        @Path("id") id: Int
    ): ApiResponse<Nothing>
    
    @GET("products/{productId}/stock-cards")
    suspend fun getStockCardsByProduct(
        @Path("productId") productId: Int
    ): ApiResponse<ProductWithStockCardsResponse>
    
    // STOCK MUTATIONS
    @GET("stock-mutations")
    suspend fun getAllStockMutations(
        @Query("product_id") productId: Int? = null,
        @Query("type") type: String? = null,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null
    ): ApiResponse<List<StockMutation>>
    
    @GET("stock-mutations/{id}")
    suspend fun getStockMutation(
        @Path("id") id: Int
    ): ApiResponse<StockMutation>
    
    @FormUrlEncoded
    @POST("stock-mutations")
    suspend fun createStockMutation(
        @Field("product_id") productId: Int,
        @Field("type") type: String,
        @Field("quantity") quantity: Int,
        @Field("date") date: String,
        @Field("description") description: String? = null
    ): ApiResponse<StockMutation>
    
    @GET("products/{productId}/stock-mutations")
    suspend fun getStockMutationsByProduct(
        @Path("productId") productId: Int
    ): ApiResponse<ProductWithStockMutationsResponse>
    
    // ORDERS
    @GET("orders/my-orders")
    suspend fun getMyOrders(): ApiResponse<List<Order>>
    
    @GET("orders")
    suspend fun getAllOrders(
        @Query("user_id") userId: Int? = null,
        @Query("status") status: String? = null,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null
    ): ApiResponse<List<Order>>
    
    @GET("orders/{id}")
    suspend fun getOrder(
        @Path("id") id: Int
    ): ApiResponse<Order>
    
    @POST("orders")
    suspend fun createOrder(
        @Body orderRequest: Map<String, Any>
    ): ApiResponse<Order>
    
    @FormUrlEncoded
    @PUT("orders/{id}/status")
    suspend fun updateOrderStatus(
        @Path("id") id: Int,
        @Field("status") status: String
    ): ApiResponse<Order>
} 