package hadi.veri.project1.fragments

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import hadi.veri.project1.adapters.TransaksiStokAdapter
import hadi.veri.project1.api.MutationApi
import hadi.veri.project1.api.ProductApi
import hadi.veri.project1.api.Product
import hadi.veri.project1.api.ProductResponse
import hadi.veri.project1.api.StockMutationRequest
import hadi.veri.project1.database.DBHelper
import hadi.veri.project1.databinding.FragmentMutasiStokBinding
import hadi.veri.project1.models.Barang
import hadi.veri.project1.models.TipeTransaksi
import hadi.veri.project1.models.TransaksiStok
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MutasiStokFragment : Fragment() {
    private var _binding: FragmentMutasiStokBinding? = null
    private val binding get() = _binding!!

    private lateinit var dbHelper: DBHelper
    private lateinit var adapter: TransaksiStokAdapter
    private var transaksiList = mutableListOf<TransaksiStok>()

    private var selectedBarang: Barang? = null
    private val calendar = Calendar.getInstance()
    private val dateFormatter = SimpleDateFormat("dd-MM-yyyy", Locale("id"))
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale("id"))
    private val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    companion object {
        fun newInstance() = MutasiStokFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMutasiStokBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = DBHelper(requireContext())
        setupDateTimePickers()
        setupBarangSearch()
        setupRecyclerView()
        setupDefaultValues()
        setupStatusSpinner()
        setupButtonListeners()
        loadTransaksiData()

        // Set judul halaman dan tombol back
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            title = "Mutasi Stock"
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun setupButtonListeners() {
        binding.btnSimpan.setOnClickListener { simpanTransaksi() }
        binding.etCari.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) filterTransaksi()
        }
        binding.checkboxMasuk.setOnCheckedChangeListener { _, _ -> filterTransaksi() }
        binding.checkboxKeluar.setOnCheckedChangeListener { _, _ -> filterTransaksi() }
    }

    private fun filterTransaksi() {
        val query = binding.etCari.text.toString()
        val isMasukChecked = binding.checkboxMasuk.isChecked
        val isKeluarChecked = binding.checkboxKeluar.isChecked

        val filteredList = transaksiList.filter {
            val isMasuk = it.tipeTransaksi == TipeTransaksi.MASUK
            val isKeluar = it.tipeTransaksi == TipeTransaksi.KELUAR

            val matchesTransaksi = when {
                isMasukChecked && isMasuk -> true
                isKeluarChecked && isKeluar -> true
                else -> isMasuk || isKeluar
            }

            matchesTransaksi && (it.kodeBarang.contains(query, ignoreCase = true) ||
                    it.namaBarang.contains(query, ignoreCase = true))
        }
        adapter.updateData(filteredList)
    }

    private fun setupDateTimePickers() {
        binding.etTanggal.setText(dateFormatter.format(calendar.time))
        binding.etTanggal.setOnClickListener { showDatePicker() }
        binding.etPukul.setText(timeFormatter.format(calendar.time))
        binding.etPukul.setOnClickListener { showTimePicker() }
    }

    private fun showDatePicker() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            calendar.set(Calendar.YEAR, selectedYear)
            calendar.set(Calendar.MONTH, selectedMonth)
            calendar.set(Calendar.DAY_OF_MONTH, selectedDay)
            binding.etTanggal.setText(dateFormatter.format(calendar.time))
        }, year, month, day).show()
    }

    private fun showTimePicker() {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
            calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
            calendar.set(Calendar.MINUTE, selectedMinute)
            binding.etPukul.setText(timeFormatter.format(calendar.time))
        }, hour, minute, true).show()
    }

    private fun setupBarangSearch() {
        binding.etKodeBarang.setOnClickListener { showBarangSearchDialog() }
    }

    private fun showBarangSearchDialog() {
        val sharedPreferences = requireContext().getSharedPreferences("login_session", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("token", null)
        if (token == null) {
            Toast.makeText(requireContext(), "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Memuat data barang...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        ProductApi.getProducts(
            context = requireContext(),
            token = "Bearer $token",
            onSuccess = { productResponse ->
                progressDialog.dismiss()
                val barangList = productResponse.products
                if (barangList.isNotEmpty()) {
                    val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("Pilih Barang")
                        .setItems(barangList.map { "${it.code} - ${it.name}" }.toTypedArray()) { _, which ->
                            val selected = barangList[which]
                            selectedBarang = Barang(
                                id = selected.id,
                                kode = selected.code,
                                nama = selected.name,
                                satuan = selected.unit,
                                jumlahStok = selected.stock,
                                harga = selected.price.toDoubleOrNull() ?: 0.0
                            )
                            binding.etKodeBarang.setText(selectedBarang?.kode ?: "")
                            binding.etNamaBarang.setText(selectedBarang?.nama ?: "")
                        }
                        .setNegativeButton("Batal", null)
                        .create()
                    dialog.show()
                } else {
                    Toast.makeText(requireContext(), "Tidak ada data barang di server", Toast.LENGTH_SHORT).show()
                }
            },
            onError = { error ->
                progressDialog.dismiss()
                Toast.makeText(requireContext(), "Gagal memuat data barang: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun setupRecyclerView() {
        adapter = TransaksiStokAdapter(transaksiList)
        binding.rvMutasiStok.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMutasiStok.adapter = adapter
    }

    private fun setupDefaultValues() {
        binding.etTanggal.setText(dateFormatter.format(calendar.time))
        binding.etPukul.setText(timeFormatter.format(calendar.time))
        binding.rbMasuk.isChecked = true
    }

    private fun setupStatusSpinner() {
        val statusOptions = arrayOf("Pending", "Selesai")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, statusOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerStatus.adapter = adapter
    }

    private fun loadTransaksiData() {
        val sharedPreferences = requireContext().getSharedPreferences("login_session", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("token", null)
        if (token == null) {
            Toast.makeText(requireContext(), "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        MutationApi.getStockMutations(
            context = requireContext(),
            token = "Bearer $token",
            onSuccess = { response ->
                transaksiList.clear()
                val mutations = response.data
                transaksiList.addAll(mutations.map {
                    TransaksiStok(
                        id = it.id,
                        kodeBarang = it.product?.code ?: "",
                        namaBarang = it.product?.name ?: "",
                        jumlah = it.quantity,
                        tipeTransaksi = if (it.type == "in") TipeTransaksi.MASUK else TipeTransaksi.KELUAR,
                        pukul = if (it.date.length >= 16) it.date.substring(11, 16) else "",
                        tanggal = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.date.substring(0, 10)) ?: Calendar.getInstance().time,
                        keterangan = it.description ?: "",
                        nilaiTransaksi = it.product?.price?.toDoubleOrNull()?.times(it.quantity) ?: 0.0
                    )
                })
                adapter.updateData(transaksiList)
            },
            onError = { error ->
                Toast.makeText(requireContext(), "Gagal memuat mutasi stok: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun simpanTransaksi() {
        if (selectedBarang == null) {
            Toast.makeText(requireContext(), "Pilih barang terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }
        val jumlahStr = binding.etJumlah.text.toString().trim()
        if (jumlahStr.isEmpty()) {
            Toast.makeText(requireContext(), "Jumlah tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val jumlah = jumlahStr.toInt()
            if (jumlah <= 0) {
                Toast.makeText(requireContext(), "Jumlah harus lebih dari 0", Toast.LENGTH_SHORT).show()
                return
            }

            val tipeTransaksi = if (binding.rbMasuk.isChecked) "in" else "out"
            if (tipeTransaksi == "out" && selectedBarang!!.jumlahStok < jumlah) {
                Toast.makeText(requireContext(), "Stok tidak mencukupi", Toast.LENGTH_SHORT).show()
                return
            }

            val sharedPreferences = requireContext().getSharedPreferences("login_session", Context.MODE_PRIVATE)
            val token = sharedPreferences.getString("token", null)
            if (token == null) {
                Toast.makeText(requireContext(), "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
                return
            }

            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            val time = binding.etPukul.text.toString()
            val dateTime = "$date $time:00"

            val body = StockMutationRequest(
                product_id = selectedBarang!!.id,
                type = tipeTransaksi,
                quantity = jumlah,
                date = dateTime,
                description = "${if (tipeTransaksi == "in") "Stok masuk" else "Stok keluar"}: ${binding.spinnerStatus.selectedItem}"
            )

            MutationApi.createStockMutation(
                context = requireContext(),
                token = "Bearer $token",
                body = body,
                onSuccess = {
                    loadTransaksiData()
                    clearForm()
                    selectedBarang = null
                    Toast.makeText(requireContext(), "Transaksi berhasil disimpan ke server", Toast.LENGTH_SHORT).show()
                },
                onError = { error ->
                    Toast.makeText(requireContext(), "Gagal simpan ke server: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            )
        } catch (e: Exception) {
            Log.e("MutasiStokFragment", "Error ${e.message}")
            Toast.makeText(requireContext(), "Format input tidak valid", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearForm() {
        binding.etKodeBarang.text?.clear()
        binding.etNamaBarang.text?.clear()
        binding.etJumlah.text?.clear()
        binding.rbMasuk.isChecked = true
        binding.spinnerStatus.setSelection(0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
