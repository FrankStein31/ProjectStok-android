package hadi.veri.project1.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import hadi.veri.project1.adapters.BarangAdapter
import hadi.veri.project1.database.DBHelper
import hadi.veri.project1.databinding.FragmentMasterStokBinding
import hadi.veri.project1.models.Barang

class MasterStokFragment : Fragment() {
    private var _binding: FragmentMasterStokBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var adapter: BarangAdapter
    private val barangList = mutableListOf<Barang>()
    private var selectedPosition = -1
    private lateinit var dbHelper: DBHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMasterStokBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DBHelper(requireContext())
        setupRecyclerView()
        setupButtons()
        loadBarangData()
    }

    private fun setupRecyclerView() {
        adapter = BarangAdapter(barangList) { barang ->
            // Item di klik, update form
            binding.etKodeBarang.setText(barang.kode)
            binding.etNamaBarang.setText(barang.nama)
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
        
        binding.etCari.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                searchBarang(binding.etCari.text.toString())
            }
        }
    }
    
    private fun loadBarangData() {
        barangList.clear()
        val data = dbHelper.getAllBarang()
        barangList.addAll(data)
        adapter.notifyDataSetChanged()
    }
    
    private fun saveBarang() {
        val kode = binding.etKodeBarang.text.toString().trim()
        val nama = binding.etNamaBarang.text.toString().trim()
        val stokStr = binding.etJumlahStok.text.toString().trim()
        val hargaStr = binding.etHargaBarang.text.toString().trim()
        
        if (kode.isEmpty() || nama.isEmpty() || stokStr.isEmpty() || hargaStr.isEmpty()) {
            Toast.makeText(requireContext(), "Semua field harus diisi", Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            val stok = stokStr.toInt()
            val harga = hargaStr.toDouble()
            
            // Cek jika kode sudah ada
            if (dbHelper.getBarangByKode(kode) != null) {
                Toast.makeText(requireContext(), "Kode barang sudah digunakan", Toast.LENGTH_SHORT).show()
                return
            }
            
            val barang = Barang(kode, nama, stok, harga)
            val result = dbHelper.insertBarang(barang)
            
            if (result > 0) {
                loadBarangData()
                clearForm()
                Toast.makeText(requireContext(), "Barang berhasil disimpan", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Gagal menyimpan barang", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Format input tidak valid", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun updateBarang() {
        if (selectedPosition == -1) {
            Toast.makeText(requireContext(), "Pilih barang terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }
        
        val kode = binding.etKodeBarang.text.toString().trim()
        val nama = binding.etNamaBarang.text.toString().trim()
        val stokStr = binding.etJumlahStok.text.toString().trim()
        val hargaStr = binding.etHargaBarang.text.toString().trim()
        
        if (kode.isEmpty() || nama.isEmpty() || stokStr.isEmpty() || hargaStr.isEmpty()) {
            Toast.makeText(requireContext(), "Semua field harus diisi", Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            val stok = stokStr.toInt()
            val harga = hargaStr.toDouble()
            
            val barang = Barang(kode, nama, stok, harga)
            val result = dbHelper.updateBarang(barang)
            
            if (result > 0) {
                loadBarangData()
                clearForm()
                selectedPosition = -1
                Toast.makeText(requireContext(), "Barang berhasil diupdate", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Gagal mengupdate barang", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Format input tidak valid", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun deleteBarang() {
        if (selectedPosition == -1) {
            Toast.makeText(requireContext(), "Pilih barang terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }
        
        val kode = binding.etKodeBarang.text.toString().trim()
        val result = dbHelper.deleteBarang(kode)
        
        if (result > 0) {
            loadBarangData()
            clearForm()
            selectedPosition = -1
            Toast.makeText(requireContext(), "Barang berhasil dihapus", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Gagal menghapus barang", Toast.LENGTH_SHORT).show()
        }
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
        binding.etJumlahStok.text?.clear()
        binding.etHargaBarang.text?.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = MasterStokFragment()
    }
} 