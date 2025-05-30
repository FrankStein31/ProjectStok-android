package hadi.veri.project1.api

import android.content.Context
import com.google.gson.Gson
import org.json.JSONObject

object UserManageApi {
    private val gson = Gson()

    /**
     * Mendapatkan daftar user (GET /api/users)
     */
    fun getUsers(
        context: Context,
        token: String,
        onSuccess: (UserResponse) -> Unit,
        onError: (Exception) -> Unit
    ) {
        VolleyClient.initialize(context)
        val endpoint = "api/users"
        val headers = mapOf(
            "Authorization" to token,
            "Accept" to "application/json"
        )
        VolleyClient.get(
            endpoint = endpoint,
            headers = headers,
            onSuccess = { response ->
                try {
                    val userResponse = gson.fromJson(response, UserResponse::class.java)
                    onSuccess(userResponse)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            onError = onError
        )
    }

    /**
     * Membuat user baru (POST /api/users)
     */
    fun createUser(
        context: Context,
        token: String,
        user: User,
        onSuccess: (SingleUserResponse) -> Unit,
        onError: (Exception) -> Unit
    ) {
        VolleyClient.initialize(context)
        val endpoint = "api/users"
        val headers = mapOf(
            "Authorization" to token,
            "Accept" to "application/json",
            "Content-Type" to "application/json"
        )
        val params = JSONObject().apply {
            put("name", user.name)
            put("email", user.email)
            put("role", user.role)
            put("phone", user.phone)
            put("address", user.address)
            // Tambahkan field lain sesuai kebutuhan model User
        }
        VolleyClient.post(
            endpoint = endpoint,
            params = params,
            headers = headers,
            onSuccess = { response ->
                try {
                    val singleUserResponse = gson.fromJson(response, SingleUserResponse::class.java)
                    onSuccess(singleUserResponse)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            onError = onError
        )
    }

    /**
     * Update user (PUT /api/users/{id})
     */
    fun updateUser(
        context: Context,
        token: String,
        id: Int,
        user: User,
        onSuccess: (SingleUserResponse) -> Unit,
        onError: (Exception) -> Unit
    ) {
        VolleyClient.initialize(context)
        val endpoint = "api/users/$id"
        val headers = mapOf(
            "Authorization" to token,
            "Accept" to "application/json",
            "Content-Type" to "application/json"
        )
        val params = JSONObject().apply {
            put("name", user.name)
            put("email", user.email)
            put("role", user.role)
            put("phone", user.phone)
            put("address", user.address)
            // Tambahkan field lain sesuai kebutuhan model User
        }
        VolleyClient.put(
            endpoint = endpoint,
            params = params,
            headers = headers,
            onSuccess = { response ->
                try {
                    val singleUserResponse = gson.fromJson(response, SingleUserResponse::class.java)
                    onSuccess(singleUserResponse)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            onError = onError
        )
    }

    /**
     * Hapus user (DELETE /api/users/{id})
     */
    fun deleteUser(
        context: Context,
        token: String,
        id: Int,
        onSuccess: (SingleUserResponse) -> Unit,
        onError: (Exception) -> Unit
    ) {
        VolleyClient.initialize(context)
        val endpoint = "api/users/$id"
        val headers = mapOf(
            "Authorization" to token,
            "Accept" to "application/json"
        )
        VolleyClient.delete(
            endpoint = endpoint,
            headers = headers,
            onSuccess = { response ->
                try {
                    val singleUserResponse = gson.fromJson(response, SingleUserResponse::class.java)
                    onSuccess(singleUserResponse)
                } catch (e: Exception) {
                    onError(e)
                }
            },
            onError = onError
        )
    }
}

data class UserResponse(
    val success: Boolean,
    val data: List<User>?
)

data class SingleUserResponse(
    val success: Boolean,
    val data: User?
)
