package hadi.veri.project1.fragments

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import hadi.veri.project1.R
import hadi.veri.project1.adapters.TransaksiStokAdapter
import hadi.veri.project1.api.ProductApi
import hadi.veri.project1.api.ProductResponse
import hadi.veri.project1.api.RetrofitClient
import hadi.veri.project1.api.StockCardApi
import hadi.veri.project1.api.StockCardResponse
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
        
        // Set judul halaman dan tombol back
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            title = "Kartu Stok"
            setDisplayHomeAsUpEnabled(true)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        
        // Reset tombol back ketika fragment dihancurkan
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
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
            val sharedPreferences = requireContext().getSharedPreferences("login_session", Context.MODE_PRIVATE)
            val token = sharedPreferences.getString("token", null)
            if (token == null) {
                Toast.makeText(requireContext(), "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val api = RetrofitClient.instance.create(ProductApi::class.java)
            api.getProducts("Bearer $token").enqueue(object : retrofit2.Callback<ProductResponse> {
                override fun onResponse(call: retrofit2.Call<ProductResponse>, response: retrofit2.Response<ProductResponse>) {
                    if (response.isSuccessful && response.body() != null && !response.body()!!.products.isNullOrEmpty()) {
                        val barangList = response.body()!!.products
                        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
                            .setTitle("Pilih Barang")
                            .setItems(barangList.map { "${it.code} - ${it.name}" }.toTypedArray()) { _, which ->
                                val selected = barangList[which]
                                barangDipilih = Barang(
                                    id = selected.id,
                                    kode = selected.code,
                                    nama = selected.name,
                                    satuan = selected.unit,
                                    jumlahStok = selected.stock,
                                    harga = selected.price.toDoubleOrNull() ?: 0.0
                                )
                                binding.etKodeBarang.setText(barangDipilih?.kode ?: "")
                                binding.etNamaBarang.setText(barangDipilih?.nama ?: "")
                            }
                            .setNegativeButton("Batal", null)
                            .create()
                        dialog.show()
                    } else {
                        Toast.makeText(requireContext(), "Tidak ada data barang di server", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: retrofit2.Call<ProductResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Gagal memuat data barang: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
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
        val sharedPreferences = requireContext().getSharedPreferences("login_session", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("token", null)
        if (token == null) {
            Toast.makeText(requireContext(), "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }
        if (barangDipilih == null) {
            Toast.makeText(requireContext(), "Pilih barang terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }
        if (periodeAwal == null || periodeAkhir == null) {
            Toast.makeText(requireContext(), "Pilih periode terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        // Format tanggal sesuai API (yyyy-MM-dd)
        val dateFormatApi = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val startDate = dateFormatApi.format(periodeAwal!!)
        val endDate = dateFormatApi.format(periodeAkhir!!)

        val api = RetrofitClient.instance.create(StockCardApi::class.java)
        api.getStockCards(
            "Bearer $token",
            barangDipilih!!.id,
            startDate,
            endDate
        ).enqueue(object : retrofit2.Callback<StockCardResponse> {
            override fun onResponse(call: retrofit2.Call<StockCardResponse>, response: retrofit2.Response<StockCardResponse>) {
                if (response.isSuccessful && response.body() != null && response.body()!!.success) {
                    val data = response.body()!!.data
                    if (data.isEmpty()) {
                        Toast.makeText(requireContext(), "Tidak ada data transaksi untuk periode ini", Toast.LENGTH_SHORT).show()
                    }
                    // Mapping StockCard ke TransaksiStok
                    transaksiList = data.map {
                        TransaksiStok(
                            id = it.id,
                            kodeBarang = barangDipilih!!.kode,
                            namaBarang = barangDipilih!!.nama,
                            jumlah = it.in_stock - it.out_stock, // atau sesuai kebutuhan
                            tipeTransaksi = if (it.in_stock > 0) TipeTransaksi.MASUK else TipeTransaksi.KELUAR,
                            pukul = "", // jika tidak ada jam di API
                            tanggal = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.date) ?: Date(),
                            keterangan = it.notes ?: "",
                            nilaiTransaksi = it.product?.price?.toDoubleOrNull()?.times(it.in_stock - it.out_stock) ?: 0.0
                        )
                    }
                    adapter = TransaksiStokAdapter(transaksiList)
                    binding.rvCardStock.adapter = adapter
                } else {
                    Toast.makeText(requireContext(), "Gagal memuat kartu stok dari server", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: retrofit2.Call<StockCardResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
} 
