package hadi.veri.project1.api

import hadi.veri.project1.models.Order
import hadi.veri.project1.models.Product
import hadi.veri.project1.models.StockCard
import hadi.veri.project1.models.StockMutation
import hadi.veri.project1.models.User

// Wrapper generik untuk respons API
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null,
    val errors: Map<String, List<String>>? = null
)

// Response untuk login dan register
data class AuthResponse(
    val user: User,
    val token: String
)

// Response untuk detail produk dan kartu stok
data class ProductWithStockCardsResponse(
    val product: Product,
    val stock_cards: List<StockCard>
)

// Response untuk detail produk dan mutasi stok
data class ProductWithStockMutationsResponse(
    val product: Product,
    val stock_mutations: List<StockMutation>
) 