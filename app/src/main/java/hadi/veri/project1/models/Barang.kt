package hadi.veri.project1.models

data class Barang(
    val id: Int,
    val kode: String,
    val nama: String,
    val satuan: String,
    var jumlahStok: Int,
    var harga: Double
) 
