package hadi.veri.project1.api

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface MutationApi {
    @GET("api/stock-mutations")
    fun getStockMutations(
        @Header("Authorization") token: String
    ): Call<StockMutationResponse>

    @POST("api/stock-mutations")
    fun createStockMutation(
        @Header("Authorization") token: String,
        @Body body: StockMutationRequest
    ): Call<StockMutation>
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

@Parcelize
data class StockMutation(
    val id: Int,
    val product_id: Int,
    val type: String, // "in" atau "out"
    val quantity: Int,
    val before_stock: Int,
    val after_stock: Int,
    val date: String, // ISO (ex: "2024-06-01")
    val description: String?,
    val user_id: Int? = null,
    val product: Product? = null
): Parcelable
