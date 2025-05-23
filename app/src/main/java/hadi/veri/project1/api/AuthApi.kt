package hadi.veri.project1.api

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface AuthApi {
    @POST("api/login")
    fun login(
        @Body loginRequest: LoginRequest
    ): Call<LoginResponse>

    @Headers("Accept: application/json")
    @POST("api/logout")
    fun logout(
        @Header("Authorization") token: String
    ): Call<LogoutResponse>

    @Headers("Accept: application/json")
    @POST("api/register")
    fun register(@Body request: RegisterRequest): Call<RegisterResponse>
}

data class LoginRequest(
    val email: String,
    val password: String
)
data class LoginResponse(
    val success: Boolean,
    val message: String,
    val data: LoginData
)

data class LoginData(
    val user: User,
    val token: String
)

data class LogoutResponse(
    val success: Boolean,
    val message: String
)

data class RegisterResponse(
    val success: Boolean,
    val message: String,
    val user: User
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val password_confirmation: String,
    val phone: String? = null,
    val address: String? = null
)

@Parcelize
data class User(
    val id: Int? = null,
    val name: String,
    val email: String,
    val email_verified_at: String? = null,
    val password: String? = null,
    val role: String,
    val jenis_kelamin: String?,
    val phone: String?,
    val address: String?,
    val created_at: String? = null,
    val updated_at: String? = null
): Parcelable
