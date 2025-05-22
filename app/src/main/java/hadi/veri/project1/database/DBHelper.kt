package hadi.veri.project1.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import hadi.veri.project1.models.Barang
import hadi.veri.project1.models.Pesanan
import hadi.veri.project1.models.TipeTransaksi
import hadi.veri.project1.models.TransaksiStok
import hadi.veri.project1.models.UserLocal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class DBHelper(private val mContext: Context) : SQLiteOpenHelper(mContext, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "stok_manager.db"
        private const val DATABASE_VERSION = 1

        // Tabel Barang
        private const val TABLE_BARANG = "barang"
        private const val COLUMN_KODE = "kode"
        private const val COLUMN_NAMA = "nama"
        private const val COLUMN_SATUAN = "satuan"
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
        private const val COLUMN_USERNAME_PESANAN = "username"

        // Tabel Item Pesanan
        private const val TABLE_ITEM_PESANAN = "item_pesanan"
        private const val COLUMN_ID_ITEM_PESANAN = "id"
        private const val COLUMN_ID_PESANAN_REF = "id_pesanan"
        private const val COLUMN_KODE_BARANG_PESANAN = "kode_barang"
        private const val COLUMN_NAMA_BARANG_PESANAN = "nama_barang"
        private const val COLUMN_JUMLAH_PESANAN = "jumlah"
        private const val COLUMN_HARGA_SATUAN = "harga_satuan"
        private const val COLUMN_SUBTOTAL = "subtotal"
        private const val COLUMN_PUKUL = "pukul"

        // Tabel Users
        private const val TABLE_USERS = "users"
        private const val COLUMN_ID_USER = "id"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_PASSWORD = "password"
        private const val COLUMN_JENIS_KELAMIN = "jenis_kelamin" 
        private const val COLUMN_ROLE = "role"

        // Referensi versi database sebelumnya
        private const val OLD_DATABASE_VERSION = 0
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Buat tabel Barang
        val createBarangTable = """
            CREATE TABLE $TABLE_BARANG (
                $COLUMN_KODE TEXT PRIMARY KEY,
                $COLUMN_NAMA TEXT,
                $COLUMN_SATUAN TEXT,
                $COLUMN_JUMLAH_STOK INTEGER,
                $COLUMN_HARGA REAL
            )
        """.trimIndent()
        db.execSQL(createBarangTable)


        // Buat tabel Transaksi Stok
        val createTransaksiStokTable = """
    CREATE TABLE $TABLE_TRANSAKSI_STOK (
        $COLUMN_ID_TRANSAKSI INTEGER PRIMARY KEY AUTOINCREMENT,
        $COLUMN_KODE_BARANG TEXT,
        $COLUMN_NAMA_BARANG TEXT,
        $COLUMN_JUMLAH INTEGER,
        $COLUMN_TIPE_TRANSAKSI TEXT,
        $COLUMN_TANGGAL TEXT,
        $COLUMN_PUKUL TEXT,
        $COLUMN_KETERANGAN TEXT,
        $COLUMN_NILAI_TRANSAKSI REAL,
        FOREIGN KEY($COLUMN_KODE_BARANG) REFERENCES $TABLE_BARANG($COLUMN_KODE)
    )
""".trimIndent()
        db.execSQL(createTransaksiStokTable)

        // Buat tabel Pesanan
        val createPesananTable = """
    CREATE TABLE $TABLE_PESANAN (
        $COLUMN_ID_PESANAN INTEGER PRIMARY KEY AUTOINCREMENT,
        $COLUMN_KODE_BARANG TEXT,
        $COLUMN_NAMA_BARANG TEXT,
        $COLUMN_JUMLAH INTEGER,
        $COLUMN_TIPE_TRANSAKSI TEXT
    )
""".trimIndent()

        db.execSQL(createPesananTable)
        val createItemPesananTable = """
    CREATE TABLE $TABLE_ITEM_PESANAN (
        $COLUMN_ID_ITEM_PESANAN INTEGER PRIMARY KEY AUTOINCREMENT,
        $COLUMN_ID_PESANAN_REF INTEGER,
        $COLUMN_KODE_BARANG_PESANAN TEXT,
        $COLUMN_NAMA_BARANG_PESANAN TEXT,
        $COLUMN_JUMLAH_PESANAN INTEGER,
        $COLUMN_PUKUL TEXT,
        $COLUMN_HARGA_SATUAN REAL,
        $COLUMN_SUBTOTAL REAL,
        FOREIGN KEY($COLUMN_ID_PESANAN_REF) REFERENCES $TABLE_PESANAN($COLUMN_ID_PESANAN),
        FOREIGN KEY($COLUMN_KODE_BARANG_PESANAN) REFERENCES $TABLE_BARANG($COLUMN_KODE)
    )
""".trimIndent()

        db.execSQL(createItemPesananTable)


        // Buat tabel Users
        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_ID_USER INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USERNAME TEXT UNIQUE,
                $COLUMN_PASSWORD TEXT,
                $COLUMN_JENIS_KELAMIN TEXT,
                $COLUMN_ROLE TEXT
            )
        """.trimIndent()
        db.execSQL(createUsersTable)
        
        // Tambahkan user admin default
        val adminValues = ContentValues().apply {
            put(COLUMN_ID_USER, UUID.randomUUID().toString())
            put(COLUMN_USERNAME, "admin")
            put(COLUMN_PASSWORD, "admin")
            put(COLUMN_JENIS_KELAMIN, "Laki-laki")
            put(COLUMN_ROLE, "Admin")
        }
        db.insert(TABLE_USERS, null, adminValues)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        android.util.Log.d("DBHelper", "onUpgrade called: oldVersion=$oldVersion, newVersion=$newVersion")

        // Migrasi dari versi lama ke versi baru
        if (oldVersion < 2) {
            // Migrasi dari versi 1 ke 2: Tambahkan kolom baru 'satuan' ke tabel barang
            db.execSQL("ALTER TABLE barang ADD COLUMN satuan TEXT DEFAULT 'Kg'")
            android.util.Log.d("DBHelper", "Migrasi versi 1 ke 2: Kolom 'satuan' ditambahkan")
        }

        if (oldVersion < 3) {
            // Migrasi dari versi 2 ke 3: Tambahkan perubahan lain
            // Misalnya, tambahkan tabel baru atau indeks
            db.execSQL("CREATE INDEX idx_barang_kode ON barang(kode)")
            android.util.Log.d("DBHelper", "Migrasi versi 2 ke 3: Indeks 'kode' pada tabel 'barang' ditambahkan")
        }

        // Tambahkan log migrasi lainnya di sini jika ada versi baru
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
            put(COLUMN_SATUAN, barang.satuan)
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
                val satuan = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SATUAN))
                val jumlahStok = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_JUMLAH_STOK))
                val harga = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_HARGA))

                val barang = Barang(kode, nama, satuan, jumlahStok, harga)
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
            val satuan = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SATUAN))
            val jumlahStok = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_JUMLAH_STOK))
            val harga = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_HARGA))
            barang = Barang(kode, nama, satuan, jumlahStok, harga)
        }
        cursor.close()
        db.close()
        return barang
    }

    fun updateBarang(barang: Barang): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAMA, barang.nama)
            put(COLUMN_SATUAN, barang.satuan)
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
            put(COLUMN_PUKUL, transaksi.pukul)
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
            val updatedBarang = Barang(barang.kode, barang.nama, barang.satuan, newStok, barang.harga)
            updateBarang(updatedBarang)
        }

        db.close()
        return result
    }

    fun getAllTransaksiStok(): List<TransaksiStok> {
        val transaksiList = mutableListOf<TransaksiStok>()
        val db = this.readableDatabase
        val query = """
    SELECT *, ($COLUMN_JUMLAH * $COLUMN_NILAI_TRANSAKSI) AS subtotal
    FROM $TABLE_TRANSAKSI_STOK 
    ORDER BY $COLUMN_TANGGAL DESC
""".trimIndent()

        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID_TRANSAKSI))
                val kodeBarang = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_KODE_BARANG))
                val namaBarang = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAMA_BARANG))
                val jumlah = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_JUMLAH))
                val tipeTransaksi = TipeTransaksi.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIPE_TRANSAKSI)))
                val pukul = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PUKUL))
                val tanggal = stringToDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TANGGAL)))
                val keterangan = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_KETERANGAN))
                val nilaiTransaksi = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_NILAI_TRANSAKSI))

                val transaksi = TransaksiStok(
                    id,
                    kodeBarang,
                    namaBarang,
                    jumlah,
                    tipeTransaksi,
                    pukul,
                    tanggal,
                    keterangan,
                    nilaiTransaksi)
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
            put(COLUMN_KODE_BARANG, pesanan.kodeBarang)
            put(COLUMN_NAMA_BARANG, pesanan.namaBarang)
            put(COLUMN_JUMLAH, pesanan.jumlah)
            put(COLUMN_TIPE_TRANSAKSI, pesanan.tipeTransaksi)
        }

        // Insert dan mendapatkan id pesanan
        val pesananResult = db.insert(TABLE_PESANAN, null, pesananValues)

        db.close()
        return pesananResult
    }


    fun getAllPesanan(): List<Pesanan> {
        val pesananList = mutableListOf<Pesanan>()
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_PESANAN ORDER BY $COLUMN_ID_PESANAN DESC"
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID_PESANAN))
                val kodeBarang = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_KODE_BARANG))
                val namaBarang = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAMA_BARANG))
                val jumlah = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_JUMLAH))
                val tipeTransaksi = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIPE_TRANSAKSI))

                val pesanan = Pesanan(
                    id = id,
                    kodeBarang = kodeBarang,
                    namaBarang = namaBarang,
                    jumlah = jumlah,
                    tipeTransaksi = tipeTransaksi
                )
                pesananList.add(pesanan)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return pesananList
    }

    // Fungsi untuk update pesanan
    fun updatePesanan(pesanan: Pesanan): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("kode_barang", pesanan.kodeBarang)
            put("nama_barang", pesanan.namaBarang)
            put("jumlah", pesanan.jumlah)
            put("tipe_transaksi", pesanan.tipeTransaksi)
        }

        val result = db.update("pesanan", values, "id = ?", arrayOf(pesanan.id.toString()))
        db.close()
        return result > 0
    }


    fun deletePesanan(pesananId: Int): Int {
        val db = this.writableDatabase

        // Hapus item pesanan terlebih dahulu
        db.delete(TABLE_ITEM_PESANAN, "$COLUMN_ID_PESANAN_REF = ?", arrayOf(pesananId.toString()))

        // Hapus pesanan
        val result = db.delete(TABLE_PESANAN, "$COLUMN_ID_PESANAN = ?", arrayOf(pesananId.toString()))

        db.close()
        return result
    }

    // ======== OPERASI CRUD UNTUK USERS ========

    fun registerUser(user: UserLocal): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, user.username)
            put(COLUMN_PASSWORD, user.password)
            put(COLUMN_JENIS_KELAMIN, user.jenisKelamin)
            put(COLUMN_ROLE, user.role)
        }
        // Simpan user baru ke database dan dapatkan hasilnya
        val result = db.insert(TABLE_USERS, null, values)
        db.close()
        return result
    }

    fun getUserRole(username: String, password: String): String? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT role FROM users WHERE username = ? AND password = ?",
            arrayOf(username, password)
        )
        var role: String? = null
        if (cursor.moveToFirst()) {
            role = cursor.getString(cursor.getColumnIndexOrThrow("role"))
        }
        cursor.close()
        db.close()
        return role
    }

    fun loginUser(username: String, password: String): UserLocal? {
        val db = this.readableDatabase

        // Debug: Log info untuk troubleshooting
        android.util.Log.d("DBHelper", "Login attempt: username=$username, password=$password")

        // Cek apakah tabel users sudah ada
        val cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='$TABLE_USERS'", null)
        val tableExists = cursor.count > 0
        cursor.close()

        if (!tableExists) {
            // Buat tabel users jika belum ada
            val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_ID_USER INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USERNAME TEXT UNIQUE,
                $COLUMN_PASSWORD TEXT,
                $COLUMN_JENIS_KELAMIN TEXT,
                $COLUMN_ROLE TEXT
            )
        """.trimIndent()
            db.execSQL(createUsersTable)

            // Tambahkan user admin default tanpa mengisi ID (karena autoincrement)
            val adminValues = ContentValues().apply {
                put(COLUMN_USERNAME, "admin")
                put(COLUMN_PASSWORD, "admin")
                put(COLUMN_JENIS_KELAMIN, "Laki-laki")
                put(COLUMN_ROLE, "Admin")
            }
            db.insert(TABLE_USERS, null, adminValues)

            return null
        }

        val query = "SELECT * FROM $TABLE_USERS WHERE $COLUMN_USERNAME = ? AND $COLUMN_PASSWORD = ?"
        val cursorLogin = db.rawQuery(query, arrayOf(username, password))

        // Debug: Log info rows returned
        android.util.Log.d("DBHelper", "Login query result rows: ${cursorLogin.count}")

        var user: UserLocal? = null

        if (cursorLogin.moveToFirst()) {
            val id = cursorLogin.getInt(cursorLogin.getColumnIndexOrThrow(COLUMN_ID_USER))
            val jenisKelamin = cursorLogin.getString(cursorLogin.getColumnIndexOrThrow(COLUMN_JENIS_KELAMIN))
            val role = cursorLogin.getString(cursorLogin.getColumnIndexOrThrow(COLUMN_ROLE))

            user = UserLocal(id, username, password, jenisKelamin, role)
            android.util.Log.d("DBHelper", "Login success: user found - ${user.username}, role=${user.role}")
        } else {
            android.util.Log.d("DBHelper", "Login failed: user not found")
        }

        cursorLogin.close()
        db.close()
        return user
    }


    fun getAllUsers(): List<UserLocal> {
        val userList = mutableListOf<UserLocal>()
        val db = this.readableDatabase

        // Cek apakah tabel users sudah ada
        val cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='$TABLE_USERS'", null)
        val tableExists = cursor.count > 0
        cursor.close()

        if (!tableExists) {
            // Buat tabel users jika belum ada
            val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_ID_USER INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USERNAME TEXT UNIQUE,
                $COLUMN_PASSWORD TEXT,
                $COLUMN_JENIS_KELAMIN TEXT,
                $COLUMN_ROLE TEXT
            )
        """.trimIndent()
            db.execSQL(createUsersTable)

            // Tidak lanjut ambil user karena belum ada data
            return userList
        }

        val query = "SELECT * FROM $TABLE_USERS"
        val cursorUsers = db.rawQuery(query, null)

        if (cursorUsers.moveToFirst()) {
            do {
                val id = cursorUsers.getInt(cursorUsers.getColumnIndexOrThrow(COLUMN_ID_USER))
                val username = cursorUsers.getString(cursorUsers.getColumnIndexOrThrow(COLUMN_USERNAME))
                val password = cursorUsers.getString(cursorUsers.getColumnIndexOrThrow(COLUMN_PASSWORD))
                val jenisKelamin = cursorUsers.getString(cursorUsers.getColumnIndexOrThrow(COLUMN_JENIS_KELAMIN))
                val role = cursorUsers.getString(cursorUsers.getColumnIndexOrThrow(COLUMN_ROLE))

                val user = UserLocal(id, username, password, jenisKelamin, role)
                userList.add(user)
            } while (cursorUsers.moveToNext())
        }

        cursorUsers.close()
        db.close()
        return userList
    }


    fun updateUser(user: UserLocal): Int {
        val db = this.writableDatabase

        // Cek apakah tabel users sudah ada
        val cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='$TABLE_USERS'", null)
        val tableExists = cursor.count > 0
        cursor.close()

        if (!tableExists) {
            val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_ID_USER INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USERNAME TEXT UNIQUE,
                $COLUMN_PASSWORD TEXT,
                $COLUMN_JENIS_KELAMIN TEXT,
                $COLUMN_ROLE TEXT
            )
        """.trimIndent()
            db.execSQL(createUsersTable)
            return 0
        }

        val values = ContentValues().apply {
            put(COLUMN_USERNAME, user.username)
            put(COLUMN_PASSWORD, user.password)
            put(COLUMN_JENIS_KELAMIN, user.jenisKelamin)
            put(COLUMN_ROLE, user.role)
        }

        val result = db.update(TABLE_USERS, values, "$COLUMN_ID_USER = ?", arrayOf(user.id.toString()))
        db.close()
        return result
    }

    fun deleteUser(userId: Int): Int {
        val db = this.writableDatabase

        // Cek apakah tabel users sudah ada
        val cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='$TABLE_USERS'", null)
        val tableExists = cursor.count > 0
        cursor.close()

        if (!tableExists) {
            val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_ID_USER INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USERNAME TEXT UNIQUE,
                $COLUMN_PASSWORD TEXT,
                $COLUMN_JENIS_KELAMIN TEXT,
                $COLUMN_ROLE TEXT
            )
        """.trimIndent()
            db.execSQL(createUsersTable)
            return 0
        }

        val result = db.delete(TABLE_USERS, "$COLUMN_ID_USER = ?", arrayOf(userId.toString()))
        db.close()
        return result
    }

    fun checkUsernameExists(username: String): Boolean {
        val db = this.readableDatabase

        // Cek apakah tabel users sudah ada
        val cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='$TABLE_USERS'", null)
        val tableExists = cursor.count > 0
        cursor.close()

        if (!tableExists) {
            val createUsersTable = """
        CREATE TABLE $TABLE_USERS (
            $COLUMN_ID_USER INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_USERNAME TEXT UNIQUE,
            $COLUMN_PASSWORD TEXT,
            $COLUMN_JENIS_KELAMIN TEXT,
            $COLUMN_ROLE TEXT
        )
        """.trimIndent()
            db.execSQL(createUsersTable)

            // Tambahkan user admin default tanpa isi id
            val adminValues = ContentValues().apply {
                put(COLUMN_USERNAME, "admin")
                put(COLUMN_PASSWORD, "admin")
                put(COLUMN_JENIS_KELAMIN, "Laki-laki")
                put(COLUMN_ROLE, "Admin")
            }
            db.insert(TABLE_USERS, null, adminValues)

            return false
        }

        // Query untuk cek apakah username sudah ada
        val queryCek = "SELECT * FROM $TABLE_USERS WHERE $COLUMN_USERNAME = ?"
        val cursorCek = db.rawQuery(queryCek, arrayOf(username))
        val exists = cursorCek.count > 0
        cursorCek.close()
        db.close()
        return exists
    }

}
