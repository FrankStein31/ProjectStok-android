package hadi.veri.project1.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import hadi.veri.project1.R
import hadi.veri.project1.adapters.StockMutationAdapter
import hadi.veri.project1.databinding.FragmentMutasiStokBinding
import hadi.veri.project1.models.Product
import hadi.veri.project1.models.StockMutation
import hadi.veri.project1.viewmodels.ProductViewModel
import hadi.veri.project1.viewmodels.StockMutationViewModel
import hadi.veri.project1.viewmodels.ViewModelFactory
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MutasiStokFragment : Fragment() {
    private var _binding: FragmentMutasiStokBinding? = null
    private val binding get() = _binding!!

    private lateinit var stockMutationViewModel: StockMutationViewModel
    private lateinit var productViewModel: ProductViewModel
    private lateinit var adapter: StockMutationAdapter

    private val stockMutationList = mutableListOf<StockMutation>()
    private val productList = mutableListOf<Product>()
    private var userRole: String? = null
    private var selectedProductId: Int = 0
    private val calendar = Calendar.getInstance()

    private val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    companion object {
        fun newInstance() = MutasiStokFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMutasiStokBinding.inflate(inflater, container, false)

        // Ambil role dari arguments
        arguments?.let {
            userRole = it.getString("role")
        }

        // Inisialisasi ViewModel
        val factory = ViewModelFactory.getInstance(requireContext())
        stockMutationViewModel = ViewModelProvider(this, factory)[StockMutationViewModel::class.java]
        productViewModel = ViewModelProvider(this, factory)[ProductViewModel::class.java]

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Batasi akses berdasarkan role
        if (userRole.equals("user", ignoreCase = true)) {
            binding.btnTambah.visibility = View.GONE
        }

        setupRecyclerView()
        setupDatePickers()
        setupSpinners()
        observeViewModels()
        loadProducts()

        binding.btnTambah.setOnClickListener {
            addStockMutation()
        }

        binding.btnFilter.setOnClickListener {
            filterMutations()
        }

        // Set judul halaman
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            title = "Mutasi Stok"
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun observeViewModels() {
        // Observe products
        productViewModel.products.observe(viewLifecycleOwner) { result ->
            result.onSuccess { response ->
                if (response.success && response.data != null) {
                    productList.clear()
                    productList.addAll(response.data)
                    
                    // Update spinner
                    val productNames = listOf("Semua Barang") + productList.map { it.name }
                    val spinnerAdapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        productNames
                    )
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerBarang.adapter = spinnerAdapter
                } else {
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                }
            }.onFailure { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Observe stock mutations
        stockMutationViewModel.stockMutations.observe(viewLifecycleOwner) { result ->
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
        
        // Observe create stock mutation result
        stockMutationViewModel.createStockMutationResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { response ->
                if (response.success && response.data != null) {
                    Toast.makeText(requireContext(), "Mutasi stok berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                    clearForm()
                    loadStockMutations()
                } else {
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                }
            }.onFailure { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = StockMutationAdapter(stockMutationList) { stockMutation ->
            // Item click - tampilkan detail jika diperlukan
            Toast.makeText(requireContext(), "Mutasi ID: ${stockMutation.id}", Toast.LENGTH_SHORT).show()
        }
        
        binding.rvMutasi.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMutasi.adapter = adapter
    }

    private fun setupDatePickers() {
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        binding.etTanggal.setOnClickListener {
            DatePickerDialog(requireContext(), { _, year, month, day ->
                calendar.set(year, month, day)
                binding.etTanggal.setText(dateFormatter.format(calendar.time))
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
        
        binding.etTanggalMulai.setOnClickListener {
            DatePickerDialog(requireContext(), { _, year, month, day ->
                calendar.set(year, month, day)
                binding.etTanggalMulai.setText(dateFormatter.format(calendar.time))
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
        
        binding.etTanggalSelesai.setOnClickListener {
            DatePickerDialog(requireContext(), { _, year, month, day ->
                calendar.set(year, month, day)
                binding.etTanggalSelesai.setText(dateFormatter.format(calendar.time))
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
        
        // Set tanggal hari ini sebagai default
        binding.etTanggal.setText(dateFormatter.format(Calendar.getInstance().time))
    }

    private fun setupSpinners() {
        // Spinner untuk tipe mutasi
        val tipeList = listOf("Masuk", "Keluar")
        val tipeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            tipeList
        )
        tipeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTipe.adapter = tipeAdapter
        
        // Spinner filter tipe
        val filterTipeList = listOf("Semua Tipe", "Masuk", "Keluar")
        val filterTipeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            filterTipeList
        )
        filterTipeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTipeFilter.adapter = filterTipeAdapter
    }

    private fun loadProducts() {
        productViewModel.getAllProducts()
    }

    private fun loadStockMutations() {
        stockMutationViewModel.getAllStockMutations()
    }

    private fun filterMutations() {
        val productPosition = binding.spinnerBarang.selectedItemPosition
        val productId = if (productPosition > 0 && productPosition <= productList.size) {
            productList[productPosition - 1].id
        } else null
        
        val typePosition = binding.spinnerTipeFilter.selectedItemPosition
        val type = when (typePosition) {
            1 -> "in"
            2 -> "out"
            else -> null
        }
        
        val startDate = binding.etTanggalMulai.text.toString().takeIf { it.isNotEmpty() }
        val endDate = binding.etTanggalSelesai.text.toString().takeIf { it.isNotEmpty() }
        
        stockMutationViewModel.getAllStockMutations(productId, type, startDate, endDate)
    }

    private fun addStockMutation() {
        val productPosition = binding.spinnerBarang.selectedItemPosition
        if (productPosition <= 0 || productPosition > productList.size) {
            Toast.makeText(requireContext(), "Pilih barang terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        val product = productList[productPosition - 1]
        val typeStr = binding.spinnerTipe.selectedItem.toString()
        val type = if (typeStr == "Masuk") "in" else "out"
        val quantityStr = binding.etJumlah.text.toString().trim()
        val description = binding.etKeterangan.text.toString().trim()
        val date = binding.etTanggal.text.toString().trim()
        
        if (quantityStr.isEmpty() || date.isEmpty()) {
            Toast.makeText(requireContext(), "Jumlah dan tanggal harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val quantity = quantityStr.toInt()
            if (quantity <= 0) {
                Toast.makeText(requireContext(), "Jumlah harus lebih dari 0", Toast.LENGTH_SHORT).show()
                return
            }

            stockMutationViewModel.createStockMutation(product.id, type, quantity, date, description)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Format jumlah tidak valid", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearForm() {
        binding.spinnerBarang.setSelection(0)
        binding.spinnerTipe.setSelection(0)
        binding.etJumlah.text.clear()
        binding.etKeterangan.text.clear()
        // Tetapkan tanggal hari ini
        binding.etTanggal.setText(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        
        // Reset tombol back
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }
}