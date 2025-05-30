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
import hadi.veri.project1.database.DBHelper
import hadi.veri.project1.databinding.FragmentMasterStokBinding
import hadi.veri.project1.models.Barang

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
        loadProductFromApi()

        // Set judul halaman dan tombol back
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            title = "Master Stok"
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun setupSpinner() {
        val satuanList = listOf("Kg", "Gram", "Liter")
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            R.layout.simple_spinner_item,
            satuanList
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinner.adapter = spinnerAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    private fun setupRecyclerView() {
        adapter = BarangAdapter(barangList) { barang ->
            binding.etKodeBarang.setText(barang.kode)
            binding.etNamaBarang.setText(barang.nama)
            binding.spinner.setSelection((binding.spinner.adapter as ArrayAdapter<String>).getPosition(barang.satuan))
            binding.etJumlahStok.setText(barang.jumlahStok.toString())
            binding.etHargaBarang.setText(barang.harga.toString())
            selectedPosition = barangList.indexOf(barang)
        }

        binding.rvBarang.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBarang.adapter = adapter
    }

    private fun setupButtons() {
        binding.btnSimpan.setOnClickListener { saveBarang() }
        binding.btnUpdate.setOnClickListener { updateBarang() }
        binding.btnHapus.setOnClickListener { deleteBarang() }

        binding.btnScan.setOnClickListener {
            intentIntegrator = IntentIntegrator.forSupportFragment(this)
            intentIntegrator.setBeepEnabled(true)
            intentIntegrator.setPrompt("Scan a QR Code")
            intentIntegrator.setCameraId(0)
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
        if (result != null && result.contents != null) {
            val scannedData = result.contents.split(";")
            if (scannedData.size >= 5) {
                val kode = scannedData[0].trim()
                val nama = scannedData[1].trim()
                val jumlahStr = scannedData[2].trim()
                val satuan = scannedData[3].trim()
                val hargaStr = scannedData[4].trim()
                if (kode.isEmpty() || nama.isEmpty() || jumlahStr.isEmpty() || satuan.isEmpty() || hargaStr.isEmpty()) {
                    Toast.makeText(requireContext(), "Data QR Code Tidak Valid", Toast.LENGTH_SHORT).show()
                    return
                }
                try {
                    val jumlah = jumlahStr.toInt()
                    val harga = hargaStr.toDouble()
                    val barang = Barang(0, kode, nama, satuan, jumlah, harga)
                    if (dbHelper.getBarangByKode(kode) == null) {
                        val resultDb = dbHelper.insertBarang(barang)
                        if (resultDb > 0) {
                            loadBarangData()
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
                binding.txKodeQR.text = kode
                binding.txNamaQR.text = nama
                binding.txJumlahQR.text = jumlahStr
                binding.txSatuanQR.text = satuan
                binding.txHargaQR.text = hargaStr
            } else {
                Toast.makeText(requireContext(), "Format QR Code Tidak Valid", Toast.LENGTH_SHORT).show()
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

    // VERSI VOLLEY UNTUK AMBIL PRODUK
    private fun loadProductFromApi() {
        val sharedPreferences = requireContext().getSharedPreferences("login_session", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("token", null)
        if (token == null) {
            Toast.makeText(context, "Token not found. Please login again.", Toast.LENGTH_LONG).show()
            return
        }
        ProductApi.getProducts(
            context = requireContext(),
            token = "Bearer $token",
            onSuccess = { productResponse ->
                val products = productResponse.products
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
            },
            onError = { error ->
                Toast.makeText(requireContext(), "Gagal memuat produk dari API: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // VERSI VOLLEY UNTUK CREATE PRODUCT
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
            val sharedPreferences = requireContext().getSharedPreferences("login_session", Context.MODE_PRIVATE)
            val token = sharedPreferences.getString("token", null)
            if (token == null) {
                Toast.makeText(requireContext(), "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
                return
            }
            val product = Product(
                id = 0, // ID akan diisi backend
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
            ProductApi.createProduct(
                context = requireContext(),
                token = "Bearer $token",
                product = product,
                onSuccess = {
                    clearForm()
                    Toast.makeText(requireContext(), "Barang berhasil disimpan ke server", Toast.LENGTH_SHORT).show()
                    loadProductFromApi()
                },
                onError = { error ->
                    Toast.makeText(requireContext(), "Gagal simpan ke server: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            )
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Format input tidak valid", Toast.LENGTH_SHORT).show()
        }
    }

    // VERSI VOLLEY UNTUK UPDATE PRODUCT
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
            val updatedProduct = Product(
                id = selectedBarang.id,
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
            ProductApi.updateProduct(
                context = requireContext(),
                token = "Bearer $token",
                id = selectedBarang.id.toString(),
                product = updatedProduct,
                onSuccess = {
                    loadProductFromApi()
                    clearForm()
                    selectedPosition = -1
                    Toast.makeText(requireContext(), "Barang berhasil diupdate di server", Toast.LENGTH_SHORT).show()
                },
                onError = { error ->
                    Toast.makeText(requireContext(), "Gagal update: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            )
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Format input tidak valid: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // VERSI VOLLEY UNTUK DELETE PRODUCT
    private fun deleteBarang() {
        if (selectedPosition == -1) {
            Toast.makeText(requireContext(), "Pilih barang terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }
        val selectedBarang = barangList[selectedPosition]
        val sharedPreferences = requireContext().getSharedPreferences("login_session", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("token", null)
        if (token == null) {
            Toast.makeText(requireContext(), "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }
        ProductApi.deleteProduct(
            context = requireContext(),
            token = "Bearer $token",
            id = selectedBarang.id.toString(),
            onSuccess = {
                loadProductFromApi()
                clearForm()
                selectedPosition = -1
                Toast.makeText(requireContext(), "Barang berhasil dihapus di server", Toast.LENGTH_SHORT).show()
            },
            onError = { error ->
                Toast.makeText(requireContext(), "Gagal menghapus barang: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )
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
