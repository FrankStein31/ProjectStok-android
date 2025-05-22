package hadi.veri.project1.models

import java.util.Date

data class TransaksiStok(
    val id: Int,
    val kodeBarang: String,
    val namaBarang: String,
    val jumlah: Int,
    val tipeTransaksi: TipeTransaksi,
    val pukul: String,
    val tanggal: Date,
    val keterangan: String,
    val nilaiTransaksi: Double,
) {
    val subtotal: Double
        get() = jumlah * nilaiTransaksi
}

data class Order(
    val id: Int,
    val order_number: String,
    val user_id: Int,
    val total_amount: Double,
    val status: String, // "pending", "processing", "completed", "cancelled"
    val shipping_address: String? = null,
    val notes: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null,
    val user: User? = null,
    val orderDetails: List<OrderDetail>? = null
)

data class OrderDetail(
    val id: Int,
    val order_id: Int,
    val product_id: Int,
    val quantity: Int,
    val price: Double,
    val subtotal: Double,
    val created_at: String? = null,
    val updated_at: String? = null,
    val product: Product? = null
)

enum class TipeTransaksi {
    MASUK,
    KELUAR
} 