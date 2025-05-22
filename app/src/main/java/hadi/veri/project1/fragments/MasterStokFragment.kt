package hadi.veri.project1.fragments

import android.R
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.BarcodeEncoder
import hadi.veri.project1.adapters.BarangAdapter
import hadi.veri.project1.api.Product
import hadi.veri.project1.api.ProductApi
import hadi.veri.project1.api.ProductResponse
import hadi.veri.project1.api.RetrofitClient
import hadi.veri.project1.database.DBHelper
import hadi.veri.project1.databinding.FragmentMasterStokBinding
import hadi.veri.project1.models.Barang
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MasterStokFragment : Fragment() {
    private var _binding: FragmentMasterStokBinding? = null
    private val binding get() = _binding!!
    lateinit var intentIntegrator: IntentIntegrator

    private lateinit var adapter: BarangAdapter
    private val barangList = mutableListOf<Barang>()
    private var selectedPosition = -1
    private lateinit var dbHelper: DBHelper
    private var userRole: String? = null // Role pengguna

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMasterStokBinding.inflate(inflater, container, false)

        // Ambil role dari arguments
        arguments?.let {
            userRole = it.getString("role") // Default ke "user" jika null
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DBHelper(requireContext())
        // Log role untuk debugging
        Toast.makeText(requireContext(), "Role: $userRole", Toast.LENGTH_SHORT).show()

        // Batasi akses berdasarkan role
        if (userRole.equals("User", ignoreCase = true)) {
            binding.btnSimpan.visibility = View.GONE
            binding.btnHapus.visibility = View.GONE
            binding.btnUpdate.visibility = View.GONE
        }
        setupRecyclerView()
        setupButtons()
        setupSpinner()
//        loadBarangData()
        loadProductFromApi()

        // Set judul halaman dan tombol back
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            title = "Master Stok"
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun setupSpinner() {
        // Daftar satuan
        val satuanList = listOf("Kg", "Gram", "Liter")

        // Buat adapter untuk spinner
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            R.layout.simple_spinner_item,
            satuanList
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Pasang adapter ke spinner
        binding.spinner.adapter = spinnerAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        // Reset tombol back ketika fragment dihancurkan
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    private fun setupRecyclerView() {
        adapter = BarangAdapter(barangList) { barang ->
            // Item di klik, update form
            binding.etKodeBarang.setText(barang.kode)
            binding.etNamaBarang.setText(barang.nama)
            binding.spinner.setSelection((binding.spinner.adapter as ArrayAdapter<String>).getPosition(barang.satuan))
            binding.etJumlahStok.setText(barang.jumlahStok.toString())
            binding.etHargaBarang.setText(barang.harga.toString())

            // Simpan posisi untuk update/delete
            selectedPosition = barangList.indexOf(barang)
        }

        binding.rvBarang.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBarang.adapter = adapter
    }

    private fun setupButtons() {
        binding.btnSimpan.setOnClickListener {
            saveBarang()
        }

        binding.btnUpdate.setOnClickListener {
            updateBarang()
        }

        binding.btnHapus.setOnClickListener {
            deleteBarang()
        }

        binding.btnScan.setOnClickListener {
            intentIntegrator = IntentIntegrator.forSupportFragment(this)
            intentIntegrator.setBeepEnabled(true)
            intentIntegrator.setPrompt("Scan a QR Code")
            intentIntegrator.setCameraId(0) // Use a specific camera of the device
            intentIntegrator.initiateScan()
        }

        binding.btnGenerate.setOnClickListener {
            try {
                val content = "${binding.etKodeBarang.text}; ${binding.etNamaBarang.text}; ${binding.etJumlahStok.text}; ${binding.spinner.selectedItem}; ${binding.etHargaBarang.text}"
                val bitMatrix = MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, 300, 300)
                val barcodeEncoder = BarcodeEncoder()
                val bitmap = barcodeEncoder.createBitmap(bitMatrix)
                binding.imageView2.setImageBitmap(bitmap)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Tidak Dapat Membuat QR Code", Toast.LENGTH_SHORT).show()
            }
        }

        binding.etCari.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                searchBarang(binding.etCari.text.toString())
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {

                val scannedData = result.contents.split(";")

                if (scannedData.size >= 5) {
                    val kode = scannedData[0].trim()
                    val nama = scannedData[1].trim()
                    val jumlahStr = scannedData[2].trim()
                    val satuan = scannedData[3].trim()
                    val hargaStr = scannedData[4].trim()

                    // Validasi
                    if (kode.isEmpty() || nama.isEmpty() || jumlahStr.isEmpty() || satuan.isEmpty() || hargaStr.isEmpty()) {
                        Toast.makeText(requireContext(), "Data QR Code Tidak Valid", Toast.LENGTH_SHORT).show()
                        return
                    }

                    try {
                        val jumlah = jumlahStr.toInt()
                        val harga = hargaStr.toDouble()

                        // Insert
                        val barang = Barang(id, kode, nama, satuan, jumlah, harga)

                        // Validasi
                        if (dbHelper.getBarangByKode(kode) == null) {
                            val result = dbHelper.insertBarang(barang)
                            if (result > 0) {
                                loadBarangData() // Refresh the list
                                Toast.makeText(requireContext(), "Barang berhasil ditambahkan dari QR Code", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(requireContext(), "Gagal menambahkan barang ke database", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(requireContext(), "Kode barang sudah ada di database", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Format data tidak valid", Toast.LENGTH_SHORT).show()
                    }

                    // Set TextViews
                    binding.txKodeQR.text = kode
                    binding.txNamaQR.text = nama
                    binding.txJumlahQR.text = jumlahStr
                    binding.txSatuanQR.text = satuan
                    binding.txHargaQR.text = hargaStr
                } else {
                    Toast.makeText(requireContext(), "Format QR Code Tidak Valid", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Tidak Ada Data di QR Code", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Scan Gagal", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadBarangData() {
        barangList.clear()
        val data = dbHelper.getAllBarang()
        barangList.addAll(data)
        adapter.notifyDataSetChanged()
    }

    private fun loadProductFromApi() {
        val sharedPreferences = requireContext().getSharedPreferences("login_session", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("token", null)

        if (token == null) {
            Toast.makeText(context, "Token not found. Please login again.", Toast.LENGTH_LONG).show()
            return
        }

        val productApi = RetrofitClient.instance.create(ProductApi::class.java)
        productApi.getProducts("Bearer $token").enqueue(object : Callback<ProductResponse> {
            override fun onResponse(call: Call<ProductResponse>, response: Response<ProductResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val products = response.body()!!.products
                    // Konversi Product ke Barang jika perlu, atau gunakan Product langsung di adapter
                    barangList.clear()
                    products.forEach {
                        barangList.add(
                            Barang(
                                id = it.id,
                                kode = it.code,
                                nama = it.name,
                                satuan = it.unit,
                                jumlahStok = it.stock,
                                harga = it.price.toDoubleOrNull() ?: 0.0
                            )
                        )
                    }
                    adapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(requireContext(), "Gagal memuat produk dari API", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ProductResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveBarang() {
        val kode = binding.etKodeBarang.text.toString().trim()
        val nama = binding.etNamaBarang.text.toString().trim()
        val satuan = binding.spinner.selectedItem.toString()
        val stokStr = binding.etJumlahStok.text.toString().trim()
        val hargaStr = binding.etHargaBarang.text.toString().trim()

        if (kode.isEmpty() || nama.isEmpty() || stokStr.isEmpty() || hargaStr.isEmpty()) {
            Toast.makeText(requireContext(), "Semua field harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val stok = stokStr.toInt()
            val harga = hargaStr.toDouble()

            // Ambil token dari SharedPreferences
            val sharedPreferences = requireContext().getSharedPreferences("login_session", Context.MODE_PRIVATE)
            val token = sharedPreferences.getString("token", null)
            if (token == null) {
                Toast.makeText(requireContext(), "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
                return
            }

            val api = RetrofitClient.instance.create(ProductApi::class.java)

            // Siapkan data dalam bentuk RequestBody
            val codeBody = kode.toRequestBody("text/plain".toMediaTypeOrNull())
            val nameBody = nama.toRequestBody("text/plain".toMediaTypeOrNull())
            val descBody = "".toRequestBody("text/plain".toMediaTypeOrNull()) // Provide empty description if needed
            val priceBody = hargaStr.toRequestBody("text/plain".toMediaTypeOrNull())
            val stockBody = stokStr.toRequestBody("text/plain".toMediaTypeOrNull())
            val unitBody = satuan.toRequestBody("text/plain".toMediaTypeOrNull())

            // Jika tidak ada gambar
            val imagePart: MultipartBody.Part? = null

            api.createProduct(
                "Bearer $token", // Add the token with Bearer prefix
                codeBody,
                nameBody,
                descBody,
                priceBody,
                stockBody,
                unitBody,
                imagePart
            ).enqueue(object : Callback<Product> {
                override fun onResponse(call: Call<Product>, response: Response<Product>) {
                    if (response.isSuccessful) {
                        clearForm()
                        Toast.makeText(requireContext(), "Barang berhasil disimpan ke server", Toast.LENGTH_SHORT).show()
                        loadProductFromApi() // Refresh list dari API
                    } else {
                        Toast.makeText(requireContext(), "Gagal simpan ke server: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Product>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Format input tidak valid", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateBarang() {
        if (selectedPosition == -1) {
            Toast.makeText(requireContext(), "Pilih barang terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedBarang = barangList[selectedPosition]

        val kode = binding.etKodeBarang.text.toString().trim()
        val nama = binding.etNamaBarang.text.toString().trim()
        val satuan = binding.spinner.selectedItem.toString()
        val stokStr = binding.etJumlahStok.text.toString().trim()
        val hargaStr = binding.etHargaBarang.text.toString().trim()

        if (kode.isEmpty() || nama.isEmpty() || stokStr.isEmpty() || hargaStr.isEmpty()) {
            Toast.makeText(requireContext(), "Semua field harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val stok = stokStr.toInt()
            val harga = hargaStr.toDouble()

            val sharedPreferences = requireContext().getSharedPreferences("login_session", Context.MODE_PRIVATE)
            val token = sharedPreferences.getString("token", null)
            if (token == null) {
                Toast.makeText(requireContext(), "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
                return
            }

            val api = RetrofitClient.instance.create(ProductApi::class.java)

            // Buat Product baru dengan id yang sudah ada
            val updatedProduct = Product(
                id = selectedBarang.id, // <-- gunakan id di sini!
                code = kode,
                name = nama,
                description = null,
                price = harga.toString(),
                stock = stok,
                unit = satuan,
                image = null,
                created_at = null,
                updated_at = null
            )

            api.updateProduct("Bearer $token", selectedBarang.id.toString(), updatedProduct)
                .enqueue(object : Callback<Product> {
                    override fun onResponse(call: Call<Product>, response: Response<Product>) {
                        if (response.isSuccessful) {
                            loadProductFromApi()
                            clearForm()
                            selectedPosition = -1
                            Toast.makeText(requireContext(), "Barang berhasil diupdate di server", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "Gagal update: ${response.message()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Product>, t: Throwable) {
                        Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Format input tidak valid: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteBarang() {
        if (selectedPosition == -1) {
            Toast.makeText(requireContext(), "Pilih barang terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedBarang = barangList[selectedPosition]

        // Ambil token dari SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("login_session", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("token", null)
        if (token == null) {
            Toast.makeText(requireContext(), "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        val api = RetrofitClient.instance.create(ProductApi::class.java)
        api.deleteProduct("Bearer $token", selectedBarang.id.toString())
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        loadProductFromApi() // Refresh data dari server
                        clearForm()
                        selectedPosition = -1
                        Toast.makeText(requireContext(), "Barang berhasil dihapus di server", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Gagal menghapus barang: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun searchBarang(query: String) {
        if (query.isEmpty()) {
            loadBarangData()
            return
        }

        val filteredList = barangList.filter {
            it.kode.contains(query, ignoreCase = true) ||
            it.nama.contains(query, ignoreCase = true)
        }

        adapter.updateData(filteredList)
    }

    private fun clearForm() {
        binding.etKodeBarang.text?.clear()
        binding.etNamaBarang.text?.clear()
        binding.spinner.setSelection(0)
        binding.etJumlahStok.text?.clear()
        binding.etHargaBarang.text?.clear()
    }

    companion object {
        fun newInstance() = MasterStokFragment()
    }
}
