package hadi.veri.project1.api

import android.content.Context
import android.os.Parcelable
import com.google.gson.Gson
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

object AuthApi {
    private val gson = Gson()

    /**
     * Login menggunakan Volley dan mengembalikan LoginResponse (data class)
     */
    fun login(
        context: Context,
        loginRequest: LoginRequest,
        onSuccess: (LoginResponse) -> Unit,
        onError: (Exception) -> Unit
    ) {
        VolleyClient.initialize(context)
        val params = JSONObject().apply {
            put("email", loginRequest.email)
            put("password", loginRequest.password)
        }
        VolleyClient.post(
            endpoint = "api/login",
            params = params,
            onSuccess = { response ->
                try {
                    val loginResponse = gson.fromJson(response, LoginResponse::class.java)
                    onSuccess(loginResponse)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            onError = onError
        )
    }

    /**
     * Logout menggunakan Volley dan mengembalikan LogoutResponse (data class)
     * Token harus dalam format "Bearer {token}"
     */
    fun logout(
        context: Context,
        token: String,
        onSuccess: (LogoutResponse) -> Unit,
        onError: (Exception) -> Unit
    ) {
        VolleyClient.initialize(context)
        VolleyClient.post(
            endpoint = "api/logout",
            params = JSONObject(), // body kosong
            headers = mapOf(
                "Authorization" to token,
                "Accept" to "application/json"
            ),
            onSuccess = { response ->
                try {
                    val logoutResponse = gson.fromJson(response, LogoutResponse::class.java)
                    onSuccess(logoutResponse)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            onError = onError
        )
    }

    /**
     * Register user baru menggunakan Volley dan mengembalikan RegisterResponse (data class)
     */
    fun register(
        context: Context,
        request: RegisterRequest,
        onSuccess: (RegisterResponse) -> Unit,
        onError: (Exception) -> Unit
    ) {
        VolleyClient.initialize(context)
        val params = JSONObject().apply {
            put("name", request.name)
            put("email", request.email)
            put("password", request.password)
            put("password_confirmation", request.password_confirmation)
            request.phone?.let { put("phone", it) }
            request.address?.let { put("address", it) }
        }
        VolleyClient.post(
            endpoint = "api/register",
            params = params,
            headers = mapOf("Accept" to "application/json"),
            onSuccess = { response ->
                try {
                    val registerResponse = gson.fromJson(response, RegisterResponse::class.java)
                    onSuccess(registerResponse)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            onError = onError
        )
    }
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
