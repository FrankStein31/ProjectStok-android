package hadi.veri.project1.models

data class StockMutation(
    val id: Int,
    val product_id: Int,
    val type: String, // "in" atau "out"
    val quantity: Int,
    val before_stock: Int,
    val after_stock: Int,
    val date: String,
    val description: String? = null,
    val user_id: Int,
    val created_at: String? = null,
    val updated_at: String? = null,
    val product: Product? = null,
    val user: User? = null
) 