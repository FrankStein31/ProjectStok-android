package hadi.veri.project1.api

import android.content.Context
import android.os.Parcelable
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

object StockCardApi {
    private val gson = Gson()

    /**
     * Mendapatkan daftar stock card berdasarkan productId, startDate, dan endDate, via Volley.
     * @param context Context aplikasi.
     * @param token Token autentikasi ("Bearer ...").
     * @param productId ID produk yang dicari.
     * @param startDate Tanggal awal (format: yyyy-MM-dd).
     * @param endDate Tanggal akhir (format: yyyy-MM-dd).
     * @param onSuccess Callback ketika sukses, dengan StockCardResponse.
     * @param onError Callback ketika error.
     */
    fun getStockCards(
        context: Context,
        token: String,
        productId: Int,
        startDate: String,
        endDate: String,
        onSuccess: (StockCardResponse) -> Unit,
        onError: (Exception) -> Unit
    ) {
        VolleyClient.initialize(context)
        val endpoint =
            "api/stock-cards?product_id=$productId&start_date=$startDate&end_date=$endDate"

        // Header untuk autentikasi
        val headers = mapOf(
            "Authorization" to token,
            "Accept" to "application/json"
        )

        VolleyClient.get(
            endpoint = endpoint,
            headers = headers, // <- INI YANG MISSING!
            onSuccess = { response ->
                try {
                    val stockCardResponse = gson.fromJson(response, StockCardResponse::class.java)
                    onSuccess(stockCardResponse)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            onError = onError
        )
    }
}

@Parcelize
data class StockCard(
    val id: Int,
    val product_id: Int,
    val date: String,
    val initial_stock: Int,
    val in_stock: Int,
    val out_stock: Int,
    val final_stock: Int,
    val notes: String?,
    val product: Product?
): Parcelable

data class StockCardResponse(
    val success: Boolean,
    val message: String,
    val data: List<StockCard>?
)
