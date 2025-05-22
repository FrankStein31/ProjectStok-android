package hadi.veri.project1.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import hadi.veri.project1.adapters.StockCardAdapter
import hadi.veri.project1.databinding.FragmentCardStokBinding
import hadi.veri.project1.models.Product
import hadi.veri.project1.models.StockCard
import hadi.veri.project1.viewmodels.ProductViewModel
import hadi.veri.project1.viewmodels.StockCardViewModel
import hadi.veri.project1.viewmodels.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CardStokFragment : Fragment() {
    private var _binding: FragmentCardStokBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var stockCardViewModel: StockCardViewModel
    private lateinit var productViewModel: ProductViewModel
    private lateinit var adapter: StockCardAdapter
    
    private val stockCardList = mutableListOf<StockCard>()
    private val productList = mutableListOf<Product>()
    private var userRole: String? = null
    private val calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCardStokBinding.inflate(inflater, container, false)
        
        // Ambil role dari arguments
        arguments?.let {
            userRole = it.getString("role")
        }
        
        // Inisialisasi ViewModel
        val factory = ViewModelFactory.getInstance(requireContext())
        stockCardViewModel = ViewModelProvider(this, factory)[StockCardViewModel::class.java]
        productViewModel = ViewModelProvider(this, factory)[ProductViewModel::class.java]
        
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Batasi akses berdasarkan role
        if (userRole.equals("user", ignoreCase = true)) {
            binding.btnTambah.visibility = View.GONE
            binding.btnEdit.visibility = View.GONE
            binding.btnHapus.visibility = View.GONE
        }
        
        setupRecyclerView()
        setupDatePickers()
        observeViewModels()
        loadProducts()
        loadStockCards()
        
        binding.btnTambah.setOnClickListener {
            addStockCard()
        }
        
        binding.btnFilter.setOnClickListener {
            filterStockCards()
        }
        
        // Set judul halaman
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            title = "Kartu Stok"
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
        
        // Observe stock cards
        stockCardViewModel.stockCards.observe(viewLifecycleOwner) { result ->
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
        
        // Observe create stock card result
        stockCardViewModel.createStockCardResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { response ->
                if (response.success && response.data != null) {
                    Toast.makeText(requireContext(), "Kartu stok berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                    clearForm()
                    loadStockCards()
                } else {
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                }
            }.onFailure { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Observe delete stock card result
        stockCardViewModel.deleteStockCardResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { response ->
                if (response.success) {
                    Toast.makeText(requireContext(), "Kartu stok berhasil dihapus", Toast.LENGTH_SHORT).show()
                    loadStockCards()
                } else {
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                }
            }.onFailure { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun setupRecyclerView() {
        adapter = StockCardAdapter(stockCardList) { stockCard ->
            // Item click - tampilkan detail atau isi form
            binding.etInitialStock.setText(stockCard.initial_stock.toString())
            binding.etInStock.setText(stockCard.in_stock.toString())
            binding.etOutStock.setText(stockCard.out_stock.toString())
            binding.etFinalStock.setText(stockCard.final_stock.toString())
            binding.etTanggal.setText(stockCard.date)
            binding.etNotes.setText(stockCard.notes ?: "")
            
            // Set spinner product
            val productPosition = productList.indexOfFirst { it.id == stockCard.product_id } + 1
            if (productPosition > 0) {
                binding.spinnerBarang.setSelection(productPosition)
            }
        }
        
        binding.rvCardStock.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCardStock.adapter = adapter
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
    
    private fun loadProducts() {
        productViewModel.getAllProducts()
    }
    
    private fun loadStockCards() {
        stockCardViewModel.getAllStockCards()
    }
    
    private fun filterStockCards() {
        val productPosition = binding.spinnerBarang.selectedItemPosition
        val productId = if (productPosition > 0 && productPosition <= productList.size) {
            productList[productPosition - 1].id
        } else null
        
        val startDate = binding.etTanggalMulai.text.toString().takeIf { it.isNotEmpty() }
        val endDate = binding.etTanggalSelesai.text.toString().takeIf { it.isNotEmpty() }
        
        stockCardViewModel.getAllStockCards(productId, startDate, endDate)
    }
    
    private fun addStockCard() {
        val productPosition = binding.spinnerBarang.selectedItemPosition
        if (productPosition <= 0 || productPosition > productList.size) {
            Toast.makeText(requireContext(), "Pilih barang terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }
        
        val product = productList[productPosition - 1]
        val initialStockStr = binding.etInitialStock.text.toString().trim()
        val inStockStr = binding.etInStock.text.toString().trim()
        val outStockStr = binding.etOutStock.text.toString().trim()
        val finalStockStr = binding.etFinalStock.text.toString().trim()
        val date = binding.etTanggal.text.toString().trim()
        val notes = binding.etNotes.text.toString().trim()
        
        if (initialStockStr.isEmpty() || inStockStr.isEmpty() || outStockStr.isEmpty() || finalStockStr.isEmpty() || date.isEmpty()) {
            Toast.makeText(requireContext(), "Semua field wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            val initialStock = initialStockStr.toInt()
            val inStock = inStockStr.toInt()
            val outStock = outStockStr.toInt()
            val finalStock = finalStockStr.toInt()
            
            // Simple validation
            if (initialStock < 0 || inStock < 0 || outStock < 0 || finalStock < 0) {
                Toast.makeText(requireContext(), "Nilai stok tidak boleh negatif", Toast.LENGTH_SHORT).show()
                return
            }
            
            // Verify calculation
            if ((initialStock + inStock - outStock) != finalStock) {
                Toast.makeText(requireContext(), "Perhitungan stok akhir tidak sesuai", Toast.LENGTH_SHORT).show()
                return
            }
            
            stockCardViewModel.createStockCard(
                product.id, initialStock, inStock, outStock, finalStock, date, notes.takeIf { it.isNotEmpty() }
            )
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Format data tidak valid: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun clearForm() {
        binding.spinnerBarang.setSelection(0)
        binding.etInitialStock.text.clear()
        binding.etInStock.text.clear()
        binding.etOutStock.text.clear()
        binding.etFinalStock.text.clear()
        binding.etNotes.text.clear()
        // Tetapkan tanggal hari ini
        binding.etTanggal.setText(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time))
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }
} 