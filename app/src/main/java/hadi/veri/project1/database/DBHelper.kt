package hadi.veri.project1.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import hadi.veri.project1.models.Barang
import hadi.veri.project1.models.ItemPesanan
import hadi.veri.project1.models.Pesanan
import hadi.veri.project1.models.StatusPesanan
import hadi.veri.project1.models.TipeTransaksi
import hadi.veri.project1.models.TransaksiStok
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "stok_manager.db"
        private const val DATABASE_VERSION = 1

        // Tabel Barang
        private const val TABLE_BARANG = "barang"
        private const val COLUMN_KODE = "kode"
        private const val COLUMN_NAMA = "nama"
        private const val COLUMN_JUMLAH_STOK = "jumlah_stok"
        private const val COLUMN_HARGA = "harga"

        // Tabel Transaksi Stok
        private const val TABLE_TRANSAKSI_STOK = "transaksi_stok"
        private const val COLUMN_ID_TRANSAKSI = "id"
        private const val COLUMN_KODE_BARANG = "kode_barang"
        private const val COLUMN_NAMA_BARANG = "nama_barang"
        private const val COLUMN_JUMLAH = "jumlah"
        private const val COLUMN_TIPE_TRANSAKSI = "tipe_transaksi"
        private const val COLUMN_TANGGAL = "tanggal"
        private const val COLUMN_KETERANGAN = "keterangan"
        private const val COLUMN_NILAI_TRANSAKSI = "nilai_transaksi"

        // Tabel Pesanan
        private const val TABLE_PESANAN = "pesanan"
        private const val COLUMN_ID_PESANAN = "id"
        private const val COLUMN_NAMA_PEMBELI = "nama_pembeli"
        private const val COLUMN_TANGGAL_PESANAN = "tanggal"
        private const val COLUMN_TOTAL_HARGA = "total_harga"
        private const val COLUMN_STATUS = "status"

        // Tabel Item Pesanan
        private const val TABLE_ITEM_PESANAN = "item_pesanan"
        private const val COLUMN_ID_ITEM_PESANAN = "id"
        private const val COLUMN_ID_PESANAN_REF = "id_pesanan"
        private const val COLUMN_KODE_BARANG_PESANAN = "kode_barang"
        private const val COLUMN_NAMA_BARANG_PESANAN = "nama_barang"
        private const val COLUMN_JUMLAH_PESANAN = "jumlah"
        private const val COLUMN_HARGA_SATUAN = "harga_satuan"
        private const val COLUMN_SUBTOTAL = "subtotal"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Buat tabel Barang
        val createBarangTable = """
            CREATE TABLE $TABLE_BARANG (
                $COLUMN_KODE TEXT PRIMARY KEY,
                $COLUMN_NAMA TEXT,
                $COLUMN_JUMLAH_STOK INTEGER,
                $COLUMN_HARGA REAL
            )
        """.trimIndent()
        db.execSQL(createBarangTable)

        // Buat tabel Transaksi Stok
        val createTransaksiStokTable = """
            CREATE TABLE $TABLE_TRANSAKSI_STOK (
                $COLUMN_ID_TRANSAKSI TEXT PRIMARY KEY,
                $COLUMN_KODE_BARANG TEXT,
                $COLUMN_NAMA_BARANG TEXT,
                $COLUMN_JUMLAH INTEGER,
                $COLUMN_TIPE_TRANSAKSI TEXT,
                $COLUMN_TANGGAL TEXT,
                $COLUMN_KETERANGAN TEXT,
                $COLUMN_NILAI_TRANSAKSI REAL,
                FOREIGN KEY($COLUMN_KODE_BARANG) REFERENCES $TABLE_BARANG($COLUMN_KODE)
            )
        """.trimIndent()
        db.execSQL(createTransaksiStokTable)

        // Buat tabel Pesanan
        val createPesananTable = """
            CREATE TABLE $TABLE_PESANAN (
                $COLUMN_ID_PESANAN TEXT PRIMARY KEY,
                $COLUMN_NAMA_PEMBELI TEXT,
                $COLUMN_TANGGAL_PESANAN TEXT,
                $COLUMN_TOTAL_HARGA REAL,
                $COLUMN_STATUS TEXT
            )
        """.trimIndent()
        db.execSQL(createPesananTable)

        // Buat tabel Item Pesanan
        val createItemPesananTable = """
            CREATE TABLE $TABLE_ITEM_PESANAN (
                $COLUMN_ID_ITEM_PESANAN TEXT PRIMARY KEY,
                $COLUMN_ID_PESANAN_REF TEXT,
                $COLUMN_KODE_BARANG_PESANAN TEXT,
                $COLUMN_NAMA_BARANG_PESANAN TEXT,
                $COLUMN_JUMLAH_PESANAN INTEGER,
                $COLUMN_HARGA_SATUAN REAL,
                $COLUMN_SUBTOTAL REAL,
                FOREIGN KEY($COLUMN_ID_PESANAN_REF) REFERENCES $TABLE_PESANAN($COLUMN_ID_PESANAN),
                FOREIGN KEY($COLUMN_KODE_BARANG_PESANAN) REFERENCES $TABLE_BARANG($COLUMN_KODE)
            )
        """.trimIndent()
        db.execSQL(createItemPesananTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Hapus tabel jika database diupgrade
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ITEM_PESANAN")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PESANAN")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TRANSAKSI_STOK")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BARANG")
        onCreate(db)
    }

    // Helper untuk konversi Date ke String dan sebaliknya
    private fun dateToString(date: Date): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(date)
    }

    private fun stringToDate(dateString: String): Date {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.parse(dateString) ?: Date()
    }

    // ======== OPERASI CRUD UNTUK BARANG ========
    
    fun insertBarang(barang: Barang): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_KODE, barang.kode)
            put(COLUMN_NAMA, barang.nama)
            put(COLUMN_JUMLAH_STOK, barang.jumlahStok)
            put(COLUMN_HARGA, barang.harga)
        }
        val result = db.insert(TABLE_BARANG, null, values)
        db.close()
        return result
    }

    fun getAllBarang(): List<Barang> {
        val barangList = mutableListOf<Barang>()
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_BARANG"
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val kode = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_KODE))
                val nama = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAMA))
                val jumlahStok = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_JUMLAH_STOK))
                val harga = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_HARGA))

                val barang = Barang(kode, nama, jumlahStok, harga)
                barangList.add(barang)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return barangList
    }

    fun getBarangByKode(kode: String): Barang? {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_BARANG WHERE $COLUMN_KODE = ?"
        val cursor = db.rawQuery(query, arrayOf(kode))
        var barang: Barang? = null

        if (cursor.moveToFirst()) {
            val nama = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAMA))
            val jumlahStok = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_JUMLAH_STOK))
            val harga = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_HARGA))
            barang = Barang(kode, nama, jumlahStok, harga)
        }
        cursor.close()
        db.close()
        return barang
    }

    fun updateBarang(barang: Barang): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAMA, barang.nama)
            put(COLUMN_JUMLAH_STOK, barang.jumlahStok)
            put(COLUMN_HARGA, barang.harga)
        }
        val result = db.update(TABLE_BARANG, values, "$COLUMN_KODE = ?", arrayOf(barang.kode))
        db.close()
        return result
    }

    fun deleteBarang(kode: String): Int {
        val db = this.writableDatabase
        val result = db.delete(TABLE_BARANG, "$COLUMN_KODE = ?", arrayOf(kode))
        db.close()
        return result
    }

    // ======== OPERASI CRUD UNTUK TRANSAKSI STOK ========
    
    fun insertTransaksiStok(transaksi: TransaksiStok): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ID_TRANSAKSI, transaksi.id)
            put(COLUMN_KODE_BARANG, transaksi.kodeBarang)
            put(COLUMN_NAMA_BARANG, transaksi.namaBarang)
            put(COLUMN_JUMLAH, transaksi.jumlah)
            put(COLUMN_TIPE_TRANSAKSI, transaksi.tipeTransaksi.name)
            put(COLUMN_TANGGAL, dateToString(transaksi.tanggal))
            put(COLUMN_KETERANGAN, transaksi.keterangan)
            put(COLUMN_NILAI_TRANSAKSI, transaksi.nilaiTransaksi)
        }
        val result = db.insert(TABLE_TRANSAKSI_STOK, null, values)

        // Update stok barang
        val barang = getBarangByKode(transaksi.kodeBarang)
        if (barang != null) {
            val newStok = when (transaksi.tipeTransaksi) {
                TipeTransaksi.MASUK -> barang.jumlahStok + transaksi.jumlah
                TipeTransaksi.KELUAR -> barang.jumlahStok - transaksi.jumlah
            }
            val updatedBarang = Barang(barang.kode, barang.nama, newStok, barang.harga)
            updateBarang(updatedBarang)
        }

        db.close()
        return result
    }

    fun getAllTransaksiStok(): List<TransaksiStok> {
        val transaksiList = mutableListOf<TransaksiStok>()
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_TRANSAKSI_STOK ORDER BY $COLUMN_TANGGAL DESC"
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID_TRANSAKSI))
                val kodeBarang = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_KODE_BARANG))
                val namaBarang = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAMA_BARANG))
                val jumlah = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_JUMLAH))
                val tipeTransaksi = TipeTransaksi.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIPE_TRANSAKSI)))
                val tanggal = stringToDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TANGGAL)))
                val keterangan = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_KETERANGAN))
                val nilaiTransaksi = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_NILAI_TRANSAKSI))

                val transaksi = TransaksiStok(id, kodeBarang, namaBarang, jumlah, tipeTransaksi, tanggal, keterangan, nilaiTransaksi)
                transaksiList.add(transaksi)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return transaksiList
    }

    // ======== OPERASI CRUD UNTUK PESANAN ========
    
    fun insertPesanan(pesanan: Pesanan): Long {
        val db = this.writableDatabase
        
        // Insert pesanan
        val pesananValues = ContentValues().apply {
            put(COLUMN_ID_PESANAN, pesanan.id)
            put(COLUMN_NAMA_PEMBELI, pesanan.namaPembeli)
            put(COLUMN_TANGGAL_PESANAN, dateToString(pesanan.tanggal))
            put(COLUMN_TOTAL_HARGA, pesanan.totalHarga)
            put(COLUMN_STATUS, pesanan.status.name)
        }
        val pesananResult = db.insert(TABLE_PESANAN, null, pesananValues)
        
        // Insert item pesanan
        for (item in pesanan.items) {
            val itemPesananValues = ContentValues().apply {
                put(COLUMN_ID_ITEM_PESANAN, UUID.randomUUID().toString())
                put(COLUMN_ID_PESANAN_REF, pesanan.id)
                put(COLUMN_KODE_BARANG_PESANAN, item.kodeBarang)
                put(COLUMN_NAMA_BARANG_PESANAN, item.namaBarang)
                put(COLUMN_JUMLAH_PESANAN, item.jumlah)
                put(COLUMN_HARGA_SATUAN, item.hargaSatuan)
                put(COLUMN_SUBTOTAL, item.subTotal)
            }
            db.insert(TABLE_ITEM_PESANAN, null, itemPesananValues)
            
            // Jika status pesanan SELESAI, kurangi stok
            if (pesanan.status == StatusPesanan.SELESAI) {
                val barang = getBarangByKode(item.kodeBarang)
                if (barang != null) {
                    val newStok = barang.jumlahStok - item.jumlah
                    val updatedBarang = Barang(barang.kode, barang.nama, newStok, barang.harga)
                    updateBarang(updatedBarang)
                    
                    // Tambahkan transaksi stok keluar
                    val transaksi = TransaksiStok(
                        UUID.randomUUID().toString(),
                        item.kodeBarang,
                        item.namaBarang,
                        item.jumlah,
                        TipeTransaksi.KELUAR,
                        pesanan.tanggal,
                        "Terjual dari pesanan: ${pesanan.id}",
                        item.subTotal
                    )
                    insertTransaksiStokWithoutBarangUpdate(transaksi)
                }
            }
        }
        
        db.close()
        return pesananResult
    }
    
    private fun insertTransaksiStokWithoutBarangUpdate(transaksi: TransaksiStok): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ID_TRANSAKSI, transaksi.id)
            put(COLUMN_KODE_BARANG, transaksi.kodeBarang)
            put(COLUMN_NAMA_BARANG, transaksi.namaBarang)
            put(COLUMN_JUMLAH, transaksi.jumlah)
            put(COLUMN_TIPE_TRANSAKSI, transaksi.tipeTransaksi.name)
            put(COLUMN_TANGGAL, dateToString(transaksi.tanggal))
            put(COLUMN_KETERANGAN, transaksi.keterangan)
            put(COLUMN_NILAI_TRANSAKSI, transaksi.nilaiTransaksi)
        }
        val result = db.insert(TABLE_TRANSAKSI_STOK, null, values)
        db.close()
        return result
    }

    fun getAllPesanan(): List<Pesanan> {
        val pesananList = mutableListOf<Pesanan>()
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_PESANAN ORDER BY $COLUMN_TANGGAL_PESANAN DESC"
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID_PESANAN))
                val namaPembeli = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAMA_PEMBELI))
                val tanggal = stringToDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TANGGAL_PESANAN)))
                val totalHarga = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_TOTAL_HARGA))
                val status = StatusPesanan.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS)))

                // Ambil item pesanan
                val items = getItemPesananByPesananId(id)
                
                val pesanan = Pesanan(id, namaPembeli, tanggal, items, totalHarga, status)
                pesananList.add(pesanan)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return pesananList
    }

    private fun getItemPesananByPesananId(pesananId: String): List<ItemPesanan> {
        val itemList = mutableListOf<ItemPesanan>()
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_ITEM_PESANAN WHERE $COLUMN_ID_PESANAN_REF = ?"
        val cursor = db.rawQuery(query, arrayOf(pesananId))

        if (cursor.moveToFirst()) {
            do {
                val kodeBarang = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_KODE_BARANG_PESANAN))
                val namaBarang = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAMA_BARANG_PESANAN))
                val jumlah = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_JUMLAH_PESANAN))
                val hargaSatuan = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_HARGA_SATUAN))
                val subTotal = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_SUBTOTAL))

                val item = ItemPesanan(kodeBarang, namaBarang, jumlah, hargaSatuan, subTotal)
                itemList.add(item)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return itemList
    }

    fun updateStatusPesanan(pesananId: String, newStatus: StatusPesanan): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_STATUS, newStatus.name)
        }
        
        // Ambil pesanan sebelum diupdate
        val oldPesanan = getPesananById(pesananId)
        val result = db.update(TABLE_PESANAN, values, "$COLUMN_ID_PESANAN = ?", arrayOf(pesananId))
        
        // Jika status berubah menjadi SELESAI, update stok
        if (oldPesanan != null && oldPesanan.status != StatusPesanan.SELESAI && newStatus == StatusPesanan.SELESAI) {
            for (item in oldPesanan.items) {
                val barang = getBarangByKode(item.kodeBarang)
                if (barang != null) {
                    val newStok = barang.jumlahStok - item.jumlah
                    val updatedBarang = Barang(barang.kode, barang.nama, newStok, barang.harga)
                    updateBarang(updatedBarang)
                    
                    // Tambahkan transaksi stok keluar
                    val transaksi = TransaksiStok(
                        UUID.randomUUID().toString(),
                        item.kodeBarang,
                        item.namaBarang,
                        item.jumlah,
                        TipeTransaksi.KELUAR,
                        oldPesanan.tanggal,
                        "Terjual dari pesanan: ${oldPesanan.id}",
                        item.subTotal
                    )
                    insertTransaksiStokWithoutBarangUpdate(transaksi)
                }
            }
        }
        
        // Jika status berubah dari SELESAI ke status lain, kembalikan stok
        if (oldPesanan != null && oldPesanan.status == StatusPesanan.SELESAI && newStatus != StatusPesanan.SELESAI) {
            for (item in oldPesanan.items) {
                val barang = getBarangByKode(item.kodeBarang)
                if (barang != null) {
                    val newStok = barang.jumlahStok + item.jumlah
                    val updatedBarang = Barang(barang.kode, barang.nama, newStok, barang.harga)
                    updateBarang(updatedBarang)
                    
                    // Tambahkan transaksi stok masuk
                    val transaksi = TransaksiStok(
                        UUID.randomUUID().toString(),
                        item.kodeBarang,
                        item.namaBarang,
                        item.jumlah,
                        TipeTransaksi.MASUK,
                        Date(),
                        "Pengembalian dari pesanan: ${oldPesanan.id}",
                        item.subTotal
                    )
                    insertTransaksiStokWithoutBarangUpdate(transaksi)
                }
            }
        }
        
        db.close()
        return result
    }

    private fun getPesananById(pesananId: String): Pesanan? {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_PESANAN WHERE $COLUMN_ID_PESANAN = ?"
        val cursor = db.rawQuery(query, arrayOf(pesananId))
        var pesanan: Pesanan? = null

        if (cursor.moveToFirst()) {
            val namaPembeli = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAMA_PEMBELI))
            val tanggal = stringToDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TANGGAL_PESANAN)))
            val totalHarga = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_TOTAL_HARGA))
            val status = StatusPesanan.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS)))

            // Ambil item pesanan
            val items = getItemPesananByPesananId(pesananId)
            
            pesanan = Pesanan(pesananId, namaPembeli, tanggal, items, totalHarga, status)
        }
        cursor.close()
        db.close()
        return pesanan
    }

    fun deletePesanan(pesananId: String): Int {
        val db = this.writableDatabase
        
        // Ambil pesanan sebelum dihapus
        val pesanan = getPesananById(pesananId)
        
        // Hapus item pesanan terlebih dahulu
        db.delete(TABLE_ITEM_PESANAN, "$COLUMN_ID_PESANAN_REF = ?", arrayOf(pesananId))
        
        // Hapus pesanan
        val result = db.delete(TABLE_PESANAN, "$COLUMN_ID_PESANAN = ?", arrayOf(pesananId))
        
        // Jika pesanan berstatus SELESAI, kembalikan stok
        if (pesanan != null && pesanan.status == StatusPesanan.SELESAI) {
            for (item in pesanan.items) {
                val barang = getBarangByKode(item.kodeBarang)
                if (barang != null) {
                    val newStok = barang.jumlahStok + item.jumlah
                    val updatedBarang = Barang(barang.kode, barang.nama, newStok, barang.harga)
                    updateBarang(updatedBarang)
                    
                    // Tambahkan transaksi stok masuk
                    val transaksi = TransaksiStok(
                        UUID.randomUUID().toString(),
                        item.kodeBarang,
                        item.namaBarang,
                        item.jumlah,
                        TipeTransaksi.MASUK,
                        Date(),
                        "Pengembalian dari pesanan yang dihapus: ${pesanan.id}",
                        item.subTotal
                    )
                    insertTransaksiStokWithoutBarangUpdate(transaksi)
                }
            }
        }
        
        db.close()
        return result
    }
}
