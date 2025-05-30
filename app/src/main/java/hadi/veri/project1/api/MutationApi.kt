package hadi.veri.project1.api

import android.content.Context
import com.google.gson.Gson
import org.json.JSONObject

object MutationApi {
    private val gson = Gson()

    /**
     * Ambil daftar mutasi stok (GET /api/stock-mutations)
     */
    fun getStockMutations(
        context: Context,
        token: String,
        onSuccess: (StockMutationResponse) -> Unit,
        onError: (Exception) -> Unit
    ) {
        VolleyClient.initialize(context)
        val endpoint = "api/stock-mutations"
        val headers = mapOf(
            "Authorization" to token,
            "Accept" to "application/json"
        )
        VolleyClient.get(
            endpoint = endpoint,
            headers = headers,
            onSuccess = { response ->
                try {
                    val mutationResponse = gson.fromJson(response, StockMutationResponse::class.java)
                    onSuccess(mutationResponse)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            onError = onError
        )
    }

    /**
     * Tambah mutasi stok (POST /api/stock-mutations)
     */
    fun createStockMutation(
        context: Context,
        token: String,
        body: StockMutationRequest,
        onSuccess: (StockMutation) -> Unit,
        onError: (Exception) -> Unit
    ) {
        VolleyClient.initialize(context)
        val endpoint = "api/stock-mutations"
        val headers = mapOf(
            "Authorization" to token,
            "Accept" to "application/json",
            "Content-Type" to "application/json"
        )

        val params = JSONObject().apply {
            put("product_id", body.product_id)
            put("type", body.type)
            put("quantity", body.quantity)
            put("date", body.date)
            put("description", body.description)
        }
        VolleyClient.post(
            endpoint = endpoint,
            params = params,
            headers = headers,
            onSuccess = { response ->
                try {
                    val mutation = gson.fromJson(response, StockMutation::class.java)
                    onSuccess(mutation)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            onError = onError
        )
    }
}

data class StockMutationRequest(
    val product_id: Int,
    val type: String,
    val quantity: Int,
    val date: String,
    val description: String
)

data class StockMutationResponse(
    val success: Boolean,
    val message: String,
    val data: List<StockMutation>
)

@kotlinx.parcelize.Parcelize
data class StockMutation(
    val id: Int,
    val product_id: Int,
    val type: String,
    val quantity: Int,
    val before_stock: Int,
    val after_stock: Int,
    val date: String,
    val description: String?,
    val user_id: Int? = null,
    val product: Product? = null
) : android.os.Parcelable
