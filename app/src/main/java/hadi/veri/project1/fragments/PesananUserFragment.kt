package hadi.veri.project1.fragments

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import hadi.veri.project1.R
import hadi.veri.project1.adapters.PesananAdapter
import hadi.veri.project1.database.DBHelper
import hadi.veri.project1.databinding.FragmentPesananUserBinding
import hadi.veri.project1.models.Barang
import hadi.veri.project1.models.ItemPesanan
import hadi.veri.project1.models.Pesanan
import hadi.veri.project1.models.StatusPesanan
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class PesananUserFragment : Fragment() {
    private var _binding: FragmentPesananUserBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var dbHelper: DBHelper
    private lateinit var adapter: PesananAdapter
    private var pesananList = mutableListOf<Pesanan>()
    private val calendar = Calendar.getInstance()
    private val dateFormatter = SimpleDateFormat("dd-MM-yyyy", Locale("id"))
    
    companion object {
        fun newInstance() = PesananUserFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPesananUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        dbHelper = DBHelper(requireContext())
        setupRecyclerView()
        setupTabLayout()
        setupSearchListener()
        setupFabListener()
        loadPesananData()
    }
    
    private fun setupRecyclerView() {
        adapter = PesananAdapter(
            pesananList,
            onStatusChange = { pesanan, status ->
                updateStatusPesanan(pesanan, status)
            },
            onDelete = { pesanan ->
                confirmDeletePesanan(pesanan)
            },
            onEdit = { pesanan ->
                // Edit pesanan tidak diimplementasikan untuk menyederhanakan
                Toast.makeText(requireContext(), "Fitur edit pesanan belum tersedia", Toast.LENGTH_SHORT).show()
            }
        )
        binding.rvPesanan.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPesanan.adapter = adapter
    }
    
    private fun setupTabLayout() {
        binding.tabLayoutPesanan.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                filterPesananByStatus(tab?.position ?: 0)
            }
            
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
    
    private fun setupSearchListener() {
        binding.etCariPesanan.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val query = binding.etCariPesanan.text.toString().trim()
                if (query.isEmpty()) {
                    loadPesananData()
                } else {
                    searchPesanan(query)
                }
            }
        }
    }
    
    private fun setupFabListener() {
        binding.fabAddPesanan.setOnClickListener {
            showAddPesananDialog()
        }
    }
    
    private fun loadPesananData() {
        pesananList.clear()
        pesananList.addAll(dbHelper.getAllPesanan())
        adapter.updateData(pesananList)
    }
    
    private fun filterPesananByStatus(tabPosition: Int) {
        val allPesanan = dbHelper.getAllPesanan()
        
        pesananList.clear()
        pesananList.addAll(when (tabPosition) {
            0 -> allPesanan // Semua
            1 -> allPesanan.filter { it.status == StatusPesanan.PENDING } // Pending
            2 -> allPesanan.filter { it.status == StatusPesanan.PROSES } // Proses
            3 -> allPesanan.filter { it.status == StatusPesanan.SELESAI } // Selesai
            4 -> allPesanan.filter { it.status == StatusPesanan.DIBATALKAN } // Dibatalkan
            else -> allPesanan
        })
        
        adapter.updateData(pesananList)
    }
    
    private fun searchPesanan(query: String) {
        val allPesanan = dbHelper.getAllPesanan()
        
        pesananList.clear()
        pesananList.addAll(allPesanan.filter { 
            it.id.contains(query, ignoreCase = true) || 
            it.namaPembeli.contains(query, ignoreCase = true)
        })
        
        adapter.updateData(pesananList)
    }
    
    private fun updateStatusPesanan(pesanan: Pesanan, newStatus: StatusPesanan) {
        val result = dbHelper.updateStatusPesanan(pesanan.id, newStatus)
        if (result > 0) {
            loadPesananData()
            filterPesananByStatus(binding.tabLayoutPesanan.selectedTabPosition)
            Toast.makeText(requireContext(), "Status pesanan berhasil diubah", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Gagal mengubah status pesanan", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun confirmDeletePesanan(pesanan: Pesanan) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Pesanan")
            .setMessage("Apakah Anda yakin ingin menghapus pesanan ini?")
            .setPositiveButton("Ya") { _, _ ->
                val result = dbHelper.deletePesanan(pesanan.id)
                if (result > 0) {
                    loadPesananData()
                    filterPesananByStatus(binding.tabLayoutPesanan.selectedTabPosition)
                    Toast.makeText(requireContext(), "Pesanan berhasil dihapus", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Gagal menghapus pesanan", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Tidak", null)
            .show()
    }
    
    private fun showAddPesananDialog() {
        val barangList = dbHelper.getAllBarang()
        if (barangList.isEmpty()) {
            Toast.makeText(requireContext(), "Tidak ada data barang", Toast.LENGTH_SHORT).show()
            return
        }
        
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_pesanan, null)
        val etNamaPembeli = dialogView.findViewById<EditText>(R.id.etNamaPembeli)
        val etTanggal = dialogView.findViewById<EditText>(R.id.etTanggal)
        val spinnerBarang = dialogView.findViewById<Spinner>(R.id.spinnerBarang)
        val etJumlah = dialogView.findViewById<EditText>(R.id.etJumlah)
        
        // Setup spinner barang
        val barangAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            barangList.map { "${it.kode} - ${it.nama}" }
        )
        barangAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerBarang.adapter = barangAdapter
        
        // Setup tanggal dengan default hari ini
        etTanggal.setText(dateFormatter.format(calendar.time))
        etTanggal.setOnClickListener {
            showDatePicker(etTanggal)
        }
        
        var selectedBarang: Barang? = null
        spinnerBarang.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedBarang = barangList[position]
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedBarang = null
            }
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("Tambah Pesanan Baru")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                try {
                    val namaPembeli = etNamaPembeli.text.toString().trim()
                    val jumlahStr = etJumlah.text.toString().trim()
                    
                    if (namaPembeli.isEmpty() || jumlahStr.isEmpty()) {
                        Toast.makeText(requireContext(), "Semua field harus diisi", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    
                    val jumlah = jumlahStr.toInt()
                    if (jumlah <= 0) {
                        Toast.makeText(requireContext(), "Jumlah harus lebih dari 0", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    
                    if (selectedBarang == null) {
                        Toast.makeText(requireContext(), "Pilih barang terlebih dahulu", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    
                    // Cek stok
                    if (selectedBarang!!.jumlahStok < jumlah) {
                        Toast.makeText(requireContext(), "Stok tidak mencukupi", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    
                    // Buat item pesanan
                    val hargaSatuan = selectedBarang!!.harga
                    val subTotal = hargaSatuan * jumlah
                    val item = ItemPesanan(
                        selectedBarang!!.kode,
                        selectedBarang!!.nama,
                        jumlah,
                        hargaSatuan,
                        subTotal
                    )
                    
                    // Buat pesanan
                    val pesanan = Pesanan(
                        UUID.randomUUID().toString(),
                        namaPembeli,
                        calendar.time,
                        listOf(item),
                        subTotal,
                        StatusPesanan.PENDING
                    )
                    
                    val result = dbHelper.insertPesanan(pesanan)
                    if (result > 0) {
                        loadPesananData()
                        filterPesananByStatus(binding.tabLayoutPesanan.selectedTabPosition)
                        Toast.makeText(requireContext(), "Pesanan berhasil disimpan", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Gagal menyimpan pesanan", Toast.LENGTH_SHORT).show()
                    }
                    
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Format input tidak valid", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
    
    private fun showDatePicker(editText: EditText) {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        
        DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            calendar.set(Calendar.YEAR, selectedYear)
            calendar.set(Calendar.MONTH, selectedMonth)
            calendar.set(Calendar.DAY_OF_MONTH, selectedDay)
            editText.setText(dateFormatter.format(calendar.time))
        }, year, month, day).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 