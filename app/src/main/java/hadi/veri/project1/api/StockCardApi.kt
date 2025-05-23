package hadi.veri.project1.api

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface StockCardApi {
    @GET("api/stock-cards")
    fun getStockCards(
        @Header("Authorization") token: String,
        @Query("product_id") productId: Int,
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String
    ): Call<StockCardResponse>
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
    val data: List<StockCard>
)
