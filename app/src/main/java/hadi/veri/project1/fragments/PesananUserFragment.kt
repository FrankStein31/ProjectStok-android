package hadi.veri.project1.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import hadi.veri.project1.R
import hadi.veri.project1.adapters.PesananAdapter
import hadi.veri.project1.database.DBHelper
import hadi.veri.project1.databinding.FragmentPesananUserBinding
import hadi.veri.project1.models.Pesanan

class PesananUserFragment : Fragment() {
    private lateinit var dbHelper: DBHelper
    private lateinit var adapter: PesananAdapter
    private var selectedId: Int? = null
    private var userRole: String? = null // Role pengguna

    private lateinit var binding: FragmentPesananUserBinding  // Gunakan view binding

    companion object {
        fun newInstance(): PesananUserFragment {
            return PesananUserFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPesananUserBinding.inflate(inflater, container, false)

        // Ambil role dari arguments
        arguments?.let {
            userRole = it.getString("role") // Default ke "user" jika null
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = DBHelper(requireContext())

        // Batasi akses berdasarkan role
        if (userRole.equals("User", ignoreCase = true)) {
            binding.btnHapus.visibility = View.GONE
            binding.btnUpdate.visibility = View.GONE
        }

        adapter = PesananAdapter(listOf(), ::onDetailClicked, ::onProsesClicked, ::onHapusClicked)
        binding.rvPesanan.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPesanan.adapter = adapter

        // Set judul halaman dan tombol back
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            title = "Pesanan User"
            setDisplayHomeAsUpEnabled(true)
        }

        loadData()

        binding.btnSimpan.setOnClickListener {
            val pesanan = getInput()
            if (pesanan.isValid()) {
                dbHelper.insertPesanan(pesanan)
                clearForm()
                loadData()
            } else {
                Toast.makeText(requireContext(), "Data tidak lengkap!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnUpdate.setOnClickListener {
            val id = selectedId ?: return@setOnClickListener
            val pesanan = getInput().copy(id = id)
            dbHelper.updatePesanan(pesanan)
            clearForm()
            loadData()
        }

        binding.btnHapus.setOnClickListener {
            val id = selectedId ?: return@setOnClickListener
            dbHelper.deletePesanan(id)
            clearForm()
            loadData()
        }

        // Cari pesanan berdasarkan nama atau kode barang
        binding.etCari.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val keyword = s.toString()
                val filteredList = dbHelper.getAllPesanan().filter {
                    it.namaBarang.contains(keyword, ignoreCase = true) || it.kodeBarang.contains(keyword, ignoreCase = true)
                }
                adapter.updateData(filteredList)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun loadData() {
        val pesananList = dbHelper.getAllPesanan()
        adapter.updateData(pesananList)
    }

    private fun getInput(): Pesanan {
        val kode = binding.etKodeBarangPesanan.text.toString().trim()
        val nama = binding.etNamaBarangPesanan.text.toString().trim()
        val jumlahText = binding.etJumlahPesanan.text.toString().trim()
        val jumlah = jumlahText.toIntOrNull() ?: 0

        // Log untuk memastikan nilai jumlah
        Log.d("PesananUserFragment", "Jumlah input: $jumlahText, hasil konversi: $jumlah")

        val tipe = if (binding.radioGroupTipeTransaksiPesanan.checkedRadioButtonId == R.id.rbMasukPesanan) "Masuk" else "Keluar"
        return Pesanan(0, kode, nama, jumlah, tipe)
    }


    private fun clearForm() {
        binding.etKodeBarangPesanan.text?.clear()
        binding.etNamaBarangPesanan.text?.clear()
        binding.etJumlahPesanan.text?.clear()
        binding.radioGroupTipeTransaksiPesanan.clearCheck()
        selectedId = null
    }

    private fun onDetailClicked(pesanan: Pesanan) {
        Toast.makeText(requireContext(), "Detail Pesanan: ${pesanan.jumlah}", Toast.LENGTH_SHORT).show()
        // Implementasikan tampilan detail jika diperlukan
    }

    private fun onProsesClicked(pesanan: Pesanan) {
        Toast.makeText(requireContext(), "Proses Pesanan: ${pesanan.tipeTransaksi}", Toast.LENGTH_SHORT).show()
        // Implementasikan perubahan status jika diperlukan
    }

    private fun onHapusClicked(pesanan: Pesanan) {
        dbHelper.deletePesanan(pesanan.id)
        loadData()
        Toast.makeText(requireContext(), "Pesanan Dihapus: ${pesanan.namaBarang}", Toast.LENGTH_SHORT).show()
    }
}


