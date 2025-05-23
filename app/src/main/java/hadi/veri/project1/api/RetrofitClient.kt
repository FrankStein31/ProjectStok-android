package hadi.veri.project1.api

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    /**
     * URL dasar untuk API.
     * Ganti dengan URL server Anda.
     */
    private const val BASE_URL = "http://192.168.1.155:8000/"

    /**
     * Interceptor untuk mencetak log permintaan dan respons HTTP.
     * Berguna untuk debugging.
     */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /**
     * OkHttpClient untuk mengelola koneksi HTTP.
     * Menambahkan interceptor untuk mencetak log.
     */
    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val response = chain.proceed(chain.request())
            Log.d("Interceptor", "Response Code: ${response.code}")
            response
        }
        .build()

    /**
     * Retrofit instance untuk mengakses API.
     * Menggunakan GsonConverterFactory untuk mengonversi JSON ke objek Kotlin.
     */
    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
