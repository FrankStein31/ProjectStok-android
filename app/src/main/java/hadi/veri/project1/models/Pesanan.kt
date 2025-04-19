package hadi.veri.project1.models

import java.util.Date

data class Pesanan(
    val id: String,
    val namaPembeli: String,
    val tanggal: Date,
    val items: List<ItemPesanan>,
    val totalHarga: Double,
    val status: StatusPesanan
)

data class ItemPesanan(
    val kodeBarang: String,
    val namaBarang: String,
    val jumlah: Int,
    val hargaSatuan: Double,
    val subTotal: Double
)

enum class StatusPesanan {
    PENDING,
    PROSES,
    SELESAI,
    DIBATALKAN
} 