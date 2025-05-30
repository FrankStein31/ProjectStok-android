package hadi.veri.project1.api

import android.content.Context
import com.google.gson.Gson
import org.json.JSONObject

object OrderApi {
    private val gson = Gson()

    /**
     * Ambil daftar pesanan (GET /api/orders)
     */
    fun getOrders(
        context: Context,
        token: String,
        onSuccess: (OrderResponse) -> Unit,
        onError: (Exception) -> Unit
    ) {
        VolleyClient.initialize(context)
        val endpoint = "api/orders"
        val headers = mapOf(
            "Authorization" to token,
            "Accept" to "application/json"
        )
        VolleyClient.get(
            endpoint = endpoint,
            headers = headers,
            onSuccess = { response ->
                try {
                    val orderResponse = gson.fromJson(response, OrderResponse::class.java)
                    onSuccess(orderResponse)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            onError = onError
        )
    }

    /**
     * Tambah pesanan (POST /api/orders)
     */
    fun createOrder(
        context: Context,
        token: String,
        body: OrderRequest,
        onSuccess: (Order) -> Unit,
        onError: (Exception) -> Unit
    ) {
        VolleyClient.initialize(context)
        val endpoint = "api/orders"
        val headers = mapOf(
            "Authorization" to token,
            "Accept" to "application/json",
            "Content-Type" to "application/json"
        )
        val params = JSONObject().apply {
            put("shipping_address", body.shipping_address)
            put("notes", body.notes)
            put("items", org.json.JSONArray().apply {
                body.items.forEach { item ->
                    put(JSONObject().apply {
                        put("product_id", item.product_id)
                        put("quantity", item.quantity)
                    })
                }
            })
        }
        VolleyClient.post(
            endpoint = endpoint,
            params = params,
            headers = headers,
            onSuccess = { response ->
                try {
                    val order = gson.fromJson(response, Order::class.java)
                    onSuccess(order)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            onError = onError
        )
    }

    /**
     * Hapus pesanan (DELETE /api/orders/{id})
     */
    fun deleteOrder(
        context: Context,
        token: String,
        id: Int,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        VolleyClient.initialize(context)
        val endpoint = "api/orders/$id"
        val headers = mapOf(
            "Authorization" to token,
            "Accept" to "application/json"
        )
        VolleyClient.delete(
            endpoint = endpoint,
            headers = headers,
            onSuccess = { _ -> onSuccess() },
            onError = onError
        )
    }

    /**
     * Update status pesanan (PUT /api/orders/{id}/status)
     */
    fun updateOrderStatus(
        context: Context,
        token: String,
        orderId: Int,
        status: String,
        onSuccess: (SingleOrderResponse) -> Unit,
        onError: (Exception) -> Unit
    ) {
        VolleyClient.initialize(context)
        val endpoint = "api/orders/$orderId/status"
        val headers = mapOf(
            "Authorization" to token,
            "Accept" to "application/json",
            "Content-Type" to "application/json"
        )
        val params = JSONObject().apply {
            put("status", status)
        }
        VolleyClient.put(
            endpoint = endpoint,
            params = params,
            headers = headers,
            onSuccess = { response ->
                try {
                    val resp = gson.fromJson(response, SingleOrderResponse::class.java)
                    onSuccess(resp)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            onError = onError
        )
    }
}

// --- Data Classes (unchanged except Parcelize annotation) ---

@kotlinx.parcelize.Parcelize
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
) : android.os.Parcelable

@kotlinx.parcelize.Parcelize
data class OrderDetail(
    val id: Int,
    val order_id: Int,
    val product_id: Int,
    val quantity: Int,
    val price: Double,
    val subtotal: Double,
    val product: Product?
) : android.os.Parcelable

data class OrderResponse(
    val success: Boolean,
    val message: String,
    val data: List<Order>
)

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
