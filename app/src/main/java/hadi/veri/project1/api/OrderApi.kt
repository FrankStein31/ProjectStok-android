package hadi.veri.project1.api

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface OrderApi {
    @GET("api/orders")
    fun getOrders(
        @Header("Authorization") token: String
    ): Call<OrderResponse>

    // Tambah pesanan
    @POST("api/orders")
    fun createOrder(
        @Header("Authorization") token: String,
        @Body body: OrderRequest
    ): Call<Order>

    // Hapus pesanan (misal endpoint DELETE /orders/{id}, jika ada)
    @DELETE("api/orders/{id}")
    fun deleteOrder(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<Void>

    @PUT("api/orders/{id}/status")
    fun updateOrderStatus(
        @Header("Authorization") token: String,
        @Path("id") orderId: Int,
        @Body statusBody: StatusBody
    ): Call<SingleOrderResponse>
}

data class StatusBody(val status: String)

data class SingleOrderResponse(
    val success: Boolean,
    val message: String,
    val data: Order
)

data class OrderRequest(
    val items: List<OrderItemRequest>,
    val shipping_address: String,
    val notes: String? = null
)
data class OrderItemRequest(
    val product_id: Int,
    val quantity: Int
)

@Parcelize
data class Order(
    val id: Int,
    val order_number: String,
    val user_id: Int,
    val total_amount: Double,
    val status: String,
    val shipping_address: String?,
    val notes: String?,
    val created_at: String,
    val updated_at: String,
    val order_details: List<OrderDetail>
): Parcelable

@Parcelize
data class OrderDetail(
    val id: Int,
    val order_id: Int,
    val product_id: Int,
    val quantity: Int,
    val price: Double,
    val subtotal: Double,
    val product: Product?
): Parcelable

data class OrderResponse(
    val success: Boolean,
    val message: String,
    val data: List<Order>
)
