package hadi.veri.project1.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import hadi.veri.project1.R
import hadi.veri.project1.adapters.TransaksiStokAdapter
import hadi.veri.project1.database.DBHelper
import hadi.veri.project1.databinding.FragmentCardStokBinding
import hadi.veri.project1.models.Barang
import hadi.veri.project1.models.TipeTransaksi
import hadi.veri.project1.models.TransaksiStok
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CardStokFragment : Fragment() {
    private var _binding: FragmentCardStokBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var dbHelper: DBHelper
    private lateinit var adapter: TransaksiStokAdapter
    private lateinit var transaksiList: List<TransaksiStok>
    
    private var periodeAwal: Date? = null
    private var periodeAkhir: Date? = null
    private var barangDipilih: Barang? = null
    
    companion object {
        fun newInstance() = CardStokFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCardStokBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        dbHelper = DBHelper(requireContext())
        setupRecyclerView()
        setupDatePickers()
        setupSearchBarang()
        setupButtonAction()
    }
    
    private fun setupRecyclerView() {
        transaksiList = emptyList()
        adapter = TransaksiStokAdapter(transaksiList)
        binding.rvCardStock.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCardStock.adapter = adapter
    }
    
    private fun setupDatePickers() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        
        // Set default periode awal sebagai awal bulan ini
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        periodeAwal = calendar.time
        binding.etPeriodeAwal.setText(dateFormat.format(periodeAwal!!))
        
        // Set default periode akhir sebagai hari ini
        calendar.time = Date()
        periodeAkhir = calendar.time
        binding.etPeriodeAkhir.setText(dateFormat.format(periodeAkhir!!))
        
        // Setup Date Picker untuk Periode Awal
        binding.etPeriodeAwal.setOnClickListener {
            val cal = Calendar.getInstance()
            if (periodeAwal != null) {
                cal.time = periodeAwal!!
            }
            
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    cal.set(Calendar.YEAR, year)
                    cal.set(Calendar.MONTH, month)
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    periodeAwal = cal.time
                    binding.etPeriodeAwal.setText(dateFormat.format(periodeAwal!!))
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }
        
        // Setup Date Picker untuk Periode Akhir
        binding.etPeriodeAkhir.setOnClickListener {
            val cal = Calendar.getInstance()
            if (periodeAkhir != null) {
                cal.time = periodeAkhir!!
            }
            
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    cal.set(Calendar.YEAR, year)
                    cal.set(Calendar.MONTH, month)
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    periodeAkhir = cal.time
                    binding.etPeriodeAkhir.setText(dateFormat.format(periodeAkhir!!))
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }
    }
    
    private fun setupSearchBarang() {
        binding.etKodeBarang.setOnClickListener {
            // Implementasi dialog pencarian barang
            val barangList = dbHelper.getAllBarang()
            if (barangList.isEmpty()) {
                Toast.makeText(requireContext(), "Belum ada data barang", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Bisa diganti dengan dialog atau fragment untuk memilih barang
            val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Pilih Barang")
                .setItems(barangList.map { "${it.kode} - ${it.nama}" }.toTypedArray()) { _, which ->
                    barangDipilih = barangList[which]
                    binding.etKodeBarang.setText(barangDipilih?.kode ?: "")
                    binding.etNamaBarang.setText(barangDipilih?.nama ?: "")
                }
                .setNegativeButton("Batal", null)
                .create()
            dialog.show()
        }
    }
    
    private fun setupButtonAction() {
        binding.btnLihatLaporan.setOnClickListener {
            if (periodeAwal == null || periodeAkhir == null) {
                Toast.makeText(requireContext(), "Pilih periode terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (barangDipilih == null) {
                Toast.makeText(requireContext(), "Pilih barang terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            loadLaporanKartuStok()
        }
    }
    
    private fun loadLaporanKartuStok() {
        // Ambil semua transaksi stok
        val allTransaksi = dbHelper.getAllTransaksiStok()
        
        // Filter berdasarkan barang dan periode
        transaksiList = allTransaksi.filter { transaksi ->
            transaksi.kodeBarang == barangDipilih?.kode &&
            !transaksi.tanggal.before(periodeAwal) &&
            !transaksi.tanggal.after(periodeAkhir)
        }
        
        if (transaksiList.isEmpty()) {
            Toast.makeText(requireContext(), "Tidak ada data transaksi untuk periode ini", Toast.LENGTH_SHORT).show()
        }
        
        // Update adapter
        adapter = TransaksiStokAdapter(transaksiList)
        binding.rvCardStock.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 