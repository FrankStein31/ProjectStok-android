package hadi.veri.project1.models

data class Product(
    val id: Int,
    val code: String,
    val name: String,
    val description: String? = null,
    val price: Double,
    val stock: Int,
    val unit: String,
    val image: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null
) 