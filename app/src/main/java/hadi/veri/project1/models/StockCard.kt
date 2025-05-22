package hadi.veri.project1.models

data class StockCard(
    val id: Int,
    val product_id: Int,
    val initial_stock: Int,
    val in_stock: Int,
    val out_stock: Int,
    val final_stock: Int,
    val date: String,
    val notes: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null,
    val product: Product? = null
) 