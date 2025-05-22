package hadi.veri.project1.models

data class Barang(
    val kode: String,
    val nama: String,
    val satuan: String,
    val jumlahStok: Int,
    val harga: Double,
    val id: Int = 0, // ID untuk kebutuhan mapping dengan Product dari API
    val deskripsi: String? = null,
    val image: String? = null
)

// Fungsi ekstensi untuk konversi antara Barang dan Product
fun Barang.toProduct(): Product {
    return Product(
        id = this.id,
        code = this.kode,
        name = this.nama,
        description = this.deskripsi,
        price = this.harga,
        stock = this.jumlahStok,
        unit = this.satuan,
        image = this.image
    )
}

fun Product.toBarang(): Barang {
    return Barang(
        id = this.id,
        kode = this.code,
        nama = this.name,
        deskripsi = this.description,
        harga = this.price,
        jumlahStok = this.stock,
        satuan = this.unit,
        image = this.image
    )
} 