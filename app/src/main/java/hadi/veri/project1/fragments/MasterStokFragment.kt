package hadi.veri.project1.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.BarcodeEncoder
import hadi.veri.project1.R
import hadi.veri.project1.adapters.ProductAdapter
import hadi.veri.project1.databinding.FragmentMasterStokBinding
import hadi.veri.project1.models.Product
import hadi.veri.project1.viewmodels.ProductViewModel
import hadi.veri.project1.viewmodels.ViewModelFactory
import java.io.File
import java.io.FileOutputStream

class MasterStokFragment : Fragment() {
    private var _binding: FragmentMasterStokBinding? = null
    private val binding get() = _binding!!
    lateinit var intentIntegrator: IntentIntegrator

    private lateinit var adapter: ProductAdapter
    private val productList = mutableListOf<Product>()
    private var selectedPosition = -1
    private var userRole: String? = null // Role pengguna
    private var selectedImageUri: Uri? = null
    private var selectedImageFile: File? = null
    
    private lateinit var viewModel: ProductViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMasterStokBinding.inflate(inflater, container, false)

        // Ambil role dari arguments
        arguments?.let {
            userRole = it.getString("role")
        }
        
        // Inisialisasi ViewModel
        val factory = ViewModelFactory.getInstance(requireContext())
        viewModel = ViewModelProvider(this, factory)[ProductViewModel::class.java]
        
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Log role untuk debugging
        Toast.makeText(requireContext(), "Role: $userRole", Toast.LENGTH_SHORT).show()

        // Batasi akses berdasarkan role
        if (userRole.equals("user", ignoreCase = true)) {
            binding.btnSimpan.visibility = View.GONE
            binding.btnHapus.visibility = View.GONE
            binding.btnUpdate.visibility = View.GONE
        }
        
        setupRecyclerView()
        setupButtons()
        setupSpinner()
        observeViewModel()
        loadProductData()

        // Set judul halaman dan tombol back
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            title = "Master Stok"
            setDisplayHomeAsUpEnabled(true)
        }
    }
    
    private fun observeViewModel() {
        // Observe products LiveData
        viewModel.products.observe(viewLifecycleOwner) { result ->
            result.onSuccess { response ->
                if (response.success && response.data != null) {
                    adapter.updateData(response.data)
                } else {
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                }
            }.onFailure { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Observe createProductResult LiveData
        viewModel.createProductResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { response ->
                if (response.success && response.data != null) {
                    Toast.makeText(requireContext(), "Produk berhasil disimpan", Toast.LENGTH_SHORT).show()
                    clearForm()
                    loadProductData()
                } else {
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                }
            }.onFailure { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Observe updateProductResult LiveData
        viewModel.updateProductResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { response ->
                if (response.success && response.data != null) {
                    Toast.makeText(requireContext(), "Produk berhasil diupdate", Toast.LENGTH_SHORT).show()
                    clearForm()
                    loadProductData()
                    selectedPosition = -1
                } else {
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                }
            }.onFailure { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Observe deleteProductResult LiveData
        viewModel.deleteProductResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { response ->
                if (response.success) {
                    Toast.makeText(requireContext(), "Produk berhasil dihapus", Toast.LENGTH_SHORT).show()
                    clearForm()
                    loadProductData()
                    selectedPosition = -1
                } else {
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                }
            }.onFailure { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSpinner() {
        // Daftar satuan
        val satuanList = listOf("pcs", "kg", "gram", "liter", "box", "lusin")

        // Buat adapter untuk spinner
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
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
        adapter = ProductAdapter(productList) { product ->
            // Item di klik, update form
            binding.etKodeBarang.setText(product.code)
            binding.etNamaBarang.setText(product.name)
            binding.spinner.setSelection((binding.spinner.adapter as ArrayAdapter<String>).getPosition(product.unit))
            binding.etJumlahStok.setText(product.stock.toString())
            binding.etHargaBarang.setText(product.price.toString())

            // Simpan posisi untuk update/delete
            selectedPosition = productList.indexOf(product)
        }

        binding.rvBarang.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBarang.adapter = adapter
    }

    private fun setupButtons() {
        binding.btnSimpan.setOnClickListener {
            saveProduct()
        }

        binding.btnUpdate.setOnClickListener {
            updateProduct()
        }

        binding.btnHapus.setOnClickListener {
            deleteProduct()
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
                searchProduct(binding.etCari.text.toString())
            }
        }
        
        binding.btnAddImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePickerLauncher.launch(intent)
        }
    }

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data != null && data.data != null) {
                selectedImageUri = data.data
                binding.imageView2.setImageURI(selectedImageUri)
                
                // Convert URI to File
                selectedImageUri?.let { uri ->
                    val inputStream = requireContext().contentResolver.openInputStream(uri)
                    val file = File(requireContext().cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
                    val outputStream = FileOutputStream(file)
                    inputStream?.copyTo(outputStream)
                    inputStream?.close()
                    outputStream.close()
                    selectedImageFile = file
                }
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
                    val code = scannedData[0].trim()
                    val name = scannedData[1].trim()
                    val stockStr = scannedData[2].trim()
                    val unit = scannedData[3].trim()
                    val priceStr = scannedData[4].trim()

                    // Validasi
                    if (code.isEmpty() || name.isEmpty() || stockStr.isEmpty() || unit.isEmpty() || priceStr.isEmpty()) {
                        Toast.makeText(requireContext(), "Data QR Code Tidak Valid", Toast.LENGTH_SHORT).show()
                        return
                    }

                    try {
                        val stock = stockStr.toInt()
                        val price = priceStr.toDouble()

                        // Isi form
                        binding.etKodeBarang.setText(code)
                        binding.etNamaBarang.setText(name)
                        binding.etJumlahStok.setText(stock.toString())
                        binding.etHargaBarang.setText(price.toString())
                        
                        // Coba set spinner
                        val position = (binding.spinner.adapter as? ArrayAdapter<String>)?.getPosition(unit) ?: 0
                        binding.spinner.setSelection(position)
                        
                        // Set TextViews
                        binding.txKodeQR.text = code
                        binding.txNamaQR.text = name
                        binding.txJumlahQR.text = stockStr
                        binding.txSatuanQR.text = unit
                        binding.txHargaQR.text = priceStr
                        
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Format data tidak valid: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
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

    private fun loadProductData() {
        viewModel.getAllProducts()
    }
    
    private fun searchProduct(query: String) {
        // Pencarian sederhana di list lokal
        val filteredList = productList.filter { 
            it.name.contains(query, ignoreCase = true) || 
            it.code.contains(query, ignoreCase = true) 
        }
        adapter.updateData(filteredList)
    }

    private fun saveProduct() {
        val code = binding.etKodeBarang.text.toString().trim()
        val name = binding.etNamaBarang.text.toString().trim()
        val unit = binding.spinner.selectedItem.toString()
        val stockStr = binding.etJumlahStok.text.toString().trim()
        val priceStr = binding.etHargaBarang.text.toString().trim()

        if (code.isEmpty() || name.isEmpty() || stockStr.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(requireContext(), "Semua field harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val stock = stockStr.toInt()
            val price = priceStr.toDouble()
            
            // Panggil ViewModel untuk menyimpan produk
            viewModel.createProduct(code, name, null, price, stock, unit, selectedImageFile)

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Format data tidak valid: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateProduct() {
        if (selectedPosition == -1) {
            Toast.makeText(requireContext(), "Pilih produk terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        val code = binding.etKodeBarang.text.toString().trim()
        val name = binding.etNamaBarang.text.toString().trim()
        val unit = binding.spinner.selectedItem.toString()
        val stockStr = binding.etJumlahStok.text.toString().trim()
        val priceStr = binding.etHargaBarang.text.toString().trim()

        if (code.isEmpty() || name.isEmpty() || stockStr.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(requireContext(), "Semua field harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val stock = stockStr.toInt()
            val price = priceStr.toDouble()

            // Pastikan item dipilih
            if (selectedPosition >= 0 && selectedPosition < productList.size) {
                val product = productList[selectedPosition]
                
                // Update produk via ViewModel
                viewModel.updateProduct(
                    product.id, 
                    code, 
                    name, 
                    null, 
                    price, 
                    stock, 
                    unit, 
                    selectedImageFile
                )
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Format data tidak valid: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteProduct() {
        if (selectedPosition == -1) {
            Toast.makeText(requireContext(), "Pilih produk terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        // Pastikan item dipilih
        if (selectedPosition >= 0 && selectedPosition < productList.size) {
            val product = productList[selectedPosition]
            
            // Delete produk via ViewModel
            viewModel.deleteProduct(product.id)
        }
    }
    
    private fun clearForm() {
        binding.etKodeBarang.text.clear()
        binding.etNamaBarang.text.clear()
        binding.etJumlahStok.text.clear()
        binding.etHargaBarang.text.clear()
        binding.spinner.setSelection(0)
        binding.imageView2.setImageResource(R.drawable.ic_launcher_foreground)
        selectedImageUri = null
        selectedImageFile = null
    }
}