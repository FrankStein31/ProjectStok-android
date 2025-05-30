package hadi.veri.project1.api

import android.content.Context
import android.util.Log
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

object VolleyClient {
    /**
     * URL dasar untuk API.
     * Ganti dengan URL server Anda.
     */
    private const val BASE_URL = "http://192.168.1.25:8000/"

    /**
     * RequestQueue untuk mengelola permintaan HTTP.
     */
    private var requestQueue: RequestQueue? = null

    /**
     * Inisialisasi RequestQueue jika belum ada.
     */
    fun initialize(context: Context) {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context.applicationContext)
        }
    }

    /**
     * Fungsi untuk membuat permintaan GET dengan headers.
     */
    fun get(
        endpoint: String,
        headers: Map<String, String> = emptyMap(),
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val url = BASE_URL + endpoint
        val stringRequest = object : StringRequest(
            Method.GET,
            url,
            { response ->
                Log.d("VolleyClient", "Response: $response")
                onSuccess(response)
            },
            { error ->
                Log.e("VolleyClient", "Error: ${error.message}")
                onError(error)
            }
        ) {
            override fun getHeaders(): Map<String, String> {
                return headers
            }
        }
        requestQueue?.add(stringRequest)
    }

    /**
     * Fungsi untuk membuat permintaan POST.
     */
    fun post(
        endpoint: String,
        params: JSONObject,
        headers: Map<String, String> = emptyMap(),
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val url = BASE_URL + endpoint
        val stringRequest = object : StringRequest(
            Method.POST,
            url,
            { response ->
                Log.d("VolleyClient", "Response: $response")
                onSuccess(response)
            },
            { error ->
                Log.e("VolleyClient", "Error: ${error.message}")
                onError(error)
            }
        ) {
            override fun getHeaders(): Map<String, String> = headers

            override fun getBody(): ByteArray = params.toString().toByteArray(Charsets.UTF_8)

            override fun getBodyContentType(): String = "application/json; charset=utf-8"
        }
        requestQueue?.add(stringRequest)
    }

    /**
     * Fungsi untuk membuat permintaan PUT.
     */
    fun put(
        endpoint: String,
        params: JSONObject,
        headers: Map<String, String> = emptyMap(),
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val url = BASE_URL + endpoint
        val stringRequest = object : StringRequest(
            Method.PUT,
            url,
            { response ->
                Log.d("VolleyClient", "Response: $response")
                onSuccess(response)
            },
            { error ->
                Log.e("VolleyClient", "Error: ${error.message}")
                onError(error)
            }
        ) {
            override fun getHeaders(): Map<String, String> = headers

            override fun getBody(): ByteArray = params.toString().toByteArray(Charsets.UTF_8)

            override fun getBodyContentType(): String = "application/json; charset=utf-8"
        }
        requestQueue?.add(stringRequest)
    }

    /**
     * Fungsi untuk membuat permintaan DELETE.
     */
    fun delete(
        endpoint: String,
        headers: Map<String, String> = emptyMap(),
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val url = BASE_URL + endpoint
        val stringRequest = object : StringRequest(
            Method.DELETE,
            url,
            { response ->
                Log.d("VolleyClient", "Response: $response")
                onSuccess(response)
            },
            { error ->
                Log.e("VolleyClient", "Error: ${error.message}")
                onError(error)
            }
        ) {
            override fun getHeaders(): Map<String, String> = headers
        }
        requestQueue?.add(stringRequest)
    }
}
