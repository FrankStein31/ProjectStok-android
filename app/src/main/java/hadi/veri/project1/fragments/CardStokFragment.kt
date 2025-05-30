package hadi.veri.project1.fragments

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import hadi.veri.project1.adapters.TransaksiStokAdapter
import hadi.veri.project1.api.ProductApi
import hadi.veri.project1.api.StockCardApi
import hadi.veri.project1.database.DBHelper
import hadi.veri.project1.databinding.FragmentCardStokBinding
import hadi.veri.project1.models.Barang
import hadi.veri.project1.models.TipeTransaksi
import hadi.veri.project1.models.TransaksiStok
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CardStokFragment : Fragment() {
    private var _binding: FragmentCardStokBinding? = null
    private val binding get() = _binding!!

    private lateinit var dbHelper: DBHelper
    private lateinit var adapter: TransaksiStokAdapter
    private var transaksiList: List<TransaksiStok> = emptyList()

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

        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            title = "Kartu Stok"
            setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

        binding.etPeriodeAwal.setOnClickListener {
            val cal = Calendar.getInstance()
            if (periodeAwal != null) cal.time = periodeAwal!!
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

        binding.etPeriodeAkhir.setOnClickListener {
            val cal = Calendar.getInstance()
            if (periodeAkhir != null) cal.time = periodeAkhir!!
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
            ProductApi.getProducts(
                context = requireContext(),
                token = "Bearer $token",
                onSuccess = { productResponse ->
                    val barangList = productResponse.products
                    if (barangList.isEmpty()) {
                        Toast.makeText(requireContext(), "Tidak ada data barang di server", Toast.LENGTH_SHORT).show()
                    } else {
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
                    }
                },
                onError = { error ->
                    Toast.makeText(requireContext(), "Gagal memuat data barang: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            )
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

        val dateFormatApi = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val startDate = dateFormatApi.format(periodeAwal!!)
        val endDate = dateFormatApi.format(periodeAkhir!!)

        StockCardApi.getStockCards(
            context = requireContext(),
            token = "Bearer $token",
            productId = barangDipilih!!.id,
            startDate = startDate,
            endDate = endDate,
            onSuccess = { response ->
                val data = response.data
                if (data == null || data.isEmpty()) {
                    Toast.makeText(requireContext(), "Tidak ada data transaksi untuk periode ini", Toast.LENGTH_SHORT).show()
                    adapter.updateData(emptyList())
                    return@getStockCards
                }

                // Defensive: Parse date string format dari API
                val dateParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val fallbackParser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                transaksiList = data.map { stockCard ->
                    val tanggal: Date = try {
                        // Strip milliseconds and timezone if present
                        val cleanDate = stockCard.date.substring(0, 19)
                        dateParser.parse(cleanDate) ?: fallbackParser.parse(stockCard.date) ?: Date()
                    } catch (e: Exception) {
                        try {
                            fallbackParser.parse(stockCard.date) ?: Date()
                        } catch (e: Exception) {
                            Date()
                        }
                    }
                    TransaksiStok(
                        id = stockCard.id,
                        kodeBarang = (stockCard.product?.code ?: barangDipilih?.kode) ?: "",
                        namaBarang = (stockCard.product?.name ?: barangDipilih?.nama) ?: "",
                        jumlah = stockCard.in_stock - stockCard.out_stock,
                        tipeTransaksi = when {
                            stockCard.in_stock > 0 -> TipeTransaksi.MASUK
                            stockCard.out_stock > 0 -> TipeTransaksi.KELUAR
                            else -> TipeTransaksi.MASUK
                        },
                        pukul = "", // Tidak ada waktu, hanya tanggal
                        tanggal = tanggal,
                        keterangan = stockCard.notes ?: "",
                        nilaiTransaksi = try {
                            (stockCard.product?.price?.toDoubleOrNull() ?: barangDipilih?.harga
                            ?: 0.0) * (stockCard.in_stock - stockCard.out_stock)
                        } catch (e: Exception) {
                            0.0
                        }
                    )
                }
                adapter.updateData(transaksiList)
            },
            onError = { error ->
                Toast.makeText(requireContext(), "Gagal memuat kartu stok dari server: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("KartuStokFragment", "Error: ${error.message}", error)
                adapter.updateData(emptyList())
            }
        )
    }
}
