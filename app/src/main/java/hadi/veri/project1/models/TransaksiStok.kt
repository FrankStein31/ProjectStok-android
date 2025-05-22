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


enum class TipeTransaksi {
    MASUK,
    KELUAR
} 