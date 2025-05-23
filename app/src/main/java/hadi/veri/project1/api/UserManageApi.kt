package hadi.veri.project1.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface UserManageApi {
    @GET("api/users")
    fun getUsers(
        @Header("Authorization") token: String
    ): Call<UserResponse>

    @POST("api/users")
    fun createUser(
        @Header("Authorization") token: String,
        @Body body: User
    ): Call<SingleUserResponse>

    @PUT("api/users/{id}")
    fun updateUser(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body body: User
    ): Call<SingleUserResponse>

    @DELETE("api/users/{id}")
    fun deleteUser(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<SingleUserResponse>
}

data class UserResponse(
    val success: Boolean,
    val data: List<User>?
)

data class SingleUserResponse(
    val success: Boolean,
    val data: User?
)
