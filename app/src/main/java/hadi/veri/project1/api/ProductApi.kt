package hadi.veri.project1.api

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import android.content.Context
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject

object ProductApi {
    private val gson = Gson()

    /**
     * Mendapatkan daftar produk (GET /api/products)
     */
    fun getProducts(
        context: Context,
        token: String,
        onSuccess: (ProductResponse) -> Unit,
        onError: (Exception) -> Unit
    ) {
        VolleyClient.initialize(context)
        val endpoint = "api/products"
        val headers = mapOf(
            "Authorization" to token,
            "Accept" to "application/json"
        )
        VolleyClient.get(
            endpoint = endpoint,
            headers = headers,
            onSuccess = { response ->
                try {
                    val productResponse = gson.fromJson(response, ProductResponse::class.java)
                    onSuccess(productResponse)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            onError = onError
        )
    }

    /**
     * Mendapatkan produk berdasarkan ID (GET /api/products/{id})
     */
    fun getProductById(
        context: Context,
        id: String,
        onSuccess: (Product) -> Unit,
        onError: (Exception) -> Unit
    ) {
        VolleyClient.initialize(context)
        val endpoint = "api/products/$id"
        VolleyClient.get(
            endpoint = endpoint,
            onSuccess = { response ->
                try {
                    val product = gson.fromJson(response, Product::class.java)
                    onSuccess(product)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            onError = onError
        )
    }

    /**
     * Membuat produk baru (POST /api/products)
     * Tidak mendukung multipart (image upload) pada contoh ini, hanya kirim field JSON saja.
     */
    fun createProduct(
        context: Context,
        token: String,
        product: Product,
        onSuccess: (Product) -> Unit,
        onError: (Exception) -> Unit
    ) {
        VolleyClient.initialize(context)
        val endpoint = "api/products"
        val headers = mapOf(
            "Authorization" to token,
            "Accept" to "application/json",
            "Content-Type" to "application/json"
        )

        val params = JSONObject().apply {
            put("code", product.code)
            put("name", product.name)
            put("description", product.description ?: JSONObject.NULL)
            put("price", product.price)
            put("stock", product.stock)
            put("unit", product.unit)
            // Untuk image multipart, perlu pengembangan custom jika ingin dukung upload file
        }

        VolleyClient.post(
            endpoint = endpoint,
            params = params,
            headers = headers,
            onSuccess = { response ->
                try {
                    val prod = gson.fromJson(response, Product::class.java)
                    onSuccess(prod)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            onError = onError
        )
    }

    /**
     * Update produk dengan ID tertentu (PUT /api/products/{id})
     */
    fun updateProduct(
        context: Context,
        token: String,
        id: String,
        product: Product,
        onSuccess: (Product) -> Unit,
        onError: (Exception) -> Unit
    ) {
        VolleyClient.initialize(context)
        val endpoint = "api/products/$id"
        val headers = mapOf(
            "Authorization" to token,
            "Accept" to "application/json",
            "Content-Type" to "application/json"
        )
        val params = JSONObject().apply {
            put("code", product.code)
            put("name", product.name)
            put("description", product.description ?: JSONObject.NULL)
            put("price", product.price)
            put("stock", product.stock)
            put("unit", product.unit)
            // Untuk image multipart, perlu pengembangan custom jika ingin dukung upload file
        }

        // Untuk PUT, kamu perlu menambahkan method baru di VolleyClient atau lakukan override di post untuk method PUT
        VolleyClient.put(
            endpoint = endpoint,
            params = params,
            headers = headers,
            onSuccess = { response ->
                try {
                    val prod = gson.fromJson(response, Product::class.java)
                    onSuccess(prod)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            onError = onError
        )
    }

    /**
     * Menghapus produk berdasarkan ID (DELETE /api/products/{id})
     */
    fun deleteProduct(
        context: Context,
        token: String,
        id: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        VolleyClient.initialize(context)
        val endpoint = "api/products/$id"
        val headers = mapOf(
            "Authorization" to token,
            "Accept" to "application/json"
        )
        VolleyClient.delete(
            endpoint = endpoint,
            headers = headers,
            onSuccess = {
                onSuccess()
            },
            onError = onError
        )
    }
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
