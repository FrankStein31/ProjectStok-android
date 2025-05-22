package hadi.veri.project1.models

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
    val phone: String? = null,
    val address: String? = null,
    val email_verified_at: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null
) 