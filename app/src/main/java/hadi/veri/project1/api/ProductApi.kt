package hadi.veri.project1.api

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface ProductApi {
    @GET("api/products")
    fun getProducts(
        @Header("Authorization") token: String
    ): Call<ProductResponse>

    @GET("api/products/{id}")
    fun getProductById(@Path("id") id: String): Call<Product>

    @Multipart
    @POST("api/products")
    fun createProduct(
        @Header("Authorization") token: String,
        @Part("code") code: RequestBody,
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody?,
        @Part("price") price: RequestBody,
        @Part("stock") stock: RequestBody,
        @Part("unit") unit: RequestBody,
        @Part image: MultipartBody.Part?
    ): Call<Product>

    @PUT("api/products/{id}")
    fun updateProduct(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body product: Product
    ): Call<Product>

    @DELETE("api/products/{id}")
    fun deleteProduct(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Call<Void>
}

data class ProductResponse(
    val products: List<Product>
)

@Parcelize
data class Product(
    val id: Int,
    val code: String,
    val name: String,
    val description: String?,
    val price: String,
    val stock: Int,
    val unit: String,
    val image: String?,
    val created_at: String?,
    val updated_at: String?
): Parcelable
