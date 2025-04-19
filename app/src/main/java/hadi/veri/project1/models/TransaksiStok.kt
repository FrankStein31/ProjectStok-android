package hadi.veri.project1.models

import java.util.Date

data class TransaksiStok(
    val id: String,
    val kodeBarang: String,
    val namaBarang: String,
    val jumlah: Int,
    val tipeTransaksi: TipeTransaksi,
    val tanggal: Date,
    val keterangan: String,
    val nilaiTransaksi: Double
)

enum class TipeTransaksi {
    MASUK,
    KELUAR
} 