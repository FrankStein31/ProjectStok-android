package hadi.veri.project1.models

data class Pesanan(
    val id: Int,
    val kodeBarang: String,
    val namaBarang: String,
    val jumlah: Int,
    val tipeTransaksi: String
) {
    // Menambahkan metode isValid untuk memvalidasi input
    fun isValid(): Boolean {
        return kodeBarang.isNotEmpty() && namaBarang.isNotEmpty() && jumlah > 0
    }
}
