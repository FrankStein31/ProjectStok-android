package hadi.veri.project1.models

data class User(
    val id: Int? = null,
    val username: String,
    val password: String,
    val jenisKelamin: String,
    val role: String
) 