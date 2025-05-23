package hadi.veri.project1.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import hadi.veri.project1.R
import hadi.veri.project1.adapters.PesananAdapter
import hadi.veri.project1.api.Order
import hadi.veri.project1.api.OrderApi
import hadi.veri.project1.api.OrderItemRequest
import hadi.veri.project1.api.OrderRequest
import hadi.veri.project1.api.OrderResponse
import hadi.veri.project1.api.Product
import hadi.veri.project1.api.ProductApi
import hadi.veri.project1.api.ProductResponse
import hadi.veri.project1.api.RetrofitClient
import hadi.veri.project1.api.SingleOrderResponse
import hadi.veri.project1.api.StatusBody
import hadi.veri.project1.databinding.FragmentPesananUserBinding
import hadi.veri.project1.models.Pesanan
import retrofit2.Call
import retrofit2.Response

class PesananUserFragment : Fragment() {
    private lateinit var adapter: PesananAdapter
    private var selectedId: Int? = null
    private var userRole: String? = null // Role pengguna
    private var productList: List<Product> = listOf()
    private var barangDipilih: Product? = null

    private lateinit var binding: FragmentPesananUserBinding  // Menggunakan view binding

    companion object {
        fun newInstance(): PesananUserFragment {
            return PesananUserFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentPesananUserBinding.inflate(inflater, container, false)

        // Ambil role dari arguments
        arguments?.let {
            userRole = it.getString("role")
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        // Ambil list produk dari API untuk mapping kode -> id
        loadProductsFromApi()

        // Load pesanan awal
        loadData()
        setupSearchBarang()

        binding.btnSimpan.setOnClickListener {
            val pesanan = getInput()
            if (pesanan.isValid()) {
                val sharedPreferences = requireContext().getSharedPreferences("login_session", Context.MODE_PRIVATE)
                val token = sharedPreferences.getString("token", null)
                if (token == null) {
                    Toast.makeText(requireContext(), "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val productId = getProductIdByKode(pesanan.kodeBarang)
                if (productId == null) {
                    Toast.makeText(requireContext(), "Kode barang tidak valid!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val api = RetrofitClient.instance.create(OrderApi::class.java)
                val request = OrderRequest(
                    items = listOf(
                        OrderItemRequest(
                            product_id = productId,
                            quantity = pesanan.jumlah
                        )
                    ),
                    shipping_address = "alamat pengiriman", // Ganti dengan field input user jika ada
                    notes = null
                )
                api.createOrder("Bearer $token", request).enqueue(object : retrofit2.Callback<Order> {
                    override fun onResponse(call: Call<Order>, response: Response<Order>) {
                        if (response.isSuccessful) {
                            clearForm()
                            loadData()
                            Toast.makeText(requireContext(), "Pesanan berhasil dibuat", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "Gagal simpan pesanan", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<Order>, t: Throwable) {
                        Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                Toast.makeText(requireContext(), "Data tidak lengkap!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnUpdate.setOnClickListener {
            // Implementasi update pesanan ke API jika endpoint tersedia (tidak ada di contoh Laravel)
            Toast.makeText(requireContext(), "Update pesanan belum diimplementasi", Toast.LENGTH_SHORT).show()
        }

        binding.btnHapus.setOnClickListener {
            val id = selectedId ?: return@setOnClickListener
            val sharedPreferences = requireContext().getSharedPreferences("login_session", Context.MODE_PRIVATE)
            val token = sharedPreferences.getString("token", null)
            if (token == null) {
                Toast.makeText(requireContext(), "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val api = RetrofitClient.instance.create(OrderApi::class.java)
            api.deleteOrder("Bearer $token", id).enqueue(object: retrofit2.Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    clearForm()
                    loadData()
                    Toast.makeText(requireContext(), "Pesanan Dihapus", Toast.LENGTH_SHORT).show()
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        // Cari pesanan berdasarkan nama atau kode barang
        binding.etCari.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val keyword = s.toString()
                filterList(keyword)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun filterList(keyword: String) {
        val pesananList = adapter.currentList
        val filteredList = pesananList.filter {
            it.namaBarang.contains(keyword, ignoreCase = true) || it.kodeBarang.contains(keyword, ignoreCase = true)
        }
        adapter.updateData(filteredList)
    }

    private fun loadProductsFromApi() {
        val sharedPreferences = requireContext().getSharedPreferences("login_session", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("token", null) ?: return
        val api = RetrofitClient.instance.create(ProductApi::class.java)
        api.getProducts("Bearer $token").enqueue(object : retrofit2.Callback<ProductResponse> {
            override fun onResponse(call: Call<ProductResponse>, response: Response<ProductResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    productList = response.body()!!.products
                }
            }
            override fun onFailure(call: Call<ProductResponse>, t: Throwable) {
                // Tidak perlu error toast
            }
        })
    }

    private fun getProductIdByKode(kode: String): Int? {
        return productList.firstOrNull { it.code == kode }?.id
    }

    private fun loadData() {
        val sharedPreferences = requireContext().getSharedPreferences("login_session", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("token", null)
        if (token == null) {
            Toast.makeText(requireContext(), "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }
        val api = RetrofitClient.instance.create(OrderApi::class.java)
        api.getOrders("Bearer $token").enqueue(object : retrofit2.Callback<OrderResponse> {
            override fun onResponse(call: Call<OrderResponse>, response: Response<OrderResponse>) {
                if (response.isSuccessful && response.body() != null && response.body()!!.success) {
                    val pesananList = response.body()!!.data.flatMap { order ->
                        order.order_details.map { detail ->
                            Pesanan(
                                id = detail.id,
                                kodeBarang = detail.product?.code ?: "",
                                namaBarang = detail.product?.name ?: "",
                                jumlah = detail.quantity,
                                tipeTransaksi = order.status // Atau "Masuk"/"Keluar" sesuai field
                            )
                        }
                    }
                    adapter.updateData(pesananList)
                } else {
                    Toast.makeText(requireContext(), "Gagal memuat pesanan", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<OrderResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupSearchBarang() {
        binding.etKodeBarangPesanan.setOnClickListener {
            val sharedPreferences = requireContext().getSharedPreferences("login_session", Context.MODE_PRIVATE)
            val token = sharedPreferences.getString("token", null)
            if (token == null) {
                Toast.makeText(requireContext(), "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val api = RetrofitClient.instance.create(ProductApi::class.java)
            api.getProducts("Bearer $token").enqueue(object : retrofit2.Callback<ProductResponse> {
                override fun onResponse(call: Call<ProductResponse>, response: Response<ProductResponse>) {
                    if (response.isSuccessful && response.body() != null && !response.body()!!.products.isNullOrEmpty()) {
                        val barangList = response.body()!!.products
                        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
                            .setTitle("Pilih Barang")
                            .setItems(barangList.map { "${it.code} - ${it.name}" }.toTypedArray()) { _, which ->
                                val selected = barangList[which]
                                barangDipilih = selected
                                binding.etKodeBarangPesanan.setText(selected.code)
                                binding.etNamaBarangPesanan.setText(selected.name)
                            }
                            .setNegativeButton("Batal", null)
                            .create()
                        dialog.show()
                    } else {
                        Toast.makeText(requireContext(), "Tidak ada data barang di server", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<ProductResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Gagal memuat data barang: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun updateOrderStatus(orderId: Int, status: String) {
        val sharedPreferences = requireContext().getSharedPreferences("login_session", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("token", null)
        if (token == null) {
            Toast.makeText(requireContext(), "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }
        val api = RetrofitClient.instance.create(OrderApi::class.java)
        api.updateOrderStatus("Bearer $token", orderId, StatusBody(status))
            .enqueue(object : retrofit2.Callback<SingleOrderResponse> {
                override fun onResponse(call: Call<SingleOrderResponse>, response: Response<SingleOrderResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        loadData()
                        Toast.makeText(requireContext(), "Status pesanan berhasil diupdate", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Gagal update status", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<SingleOrderResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun getInput(): Pesanan {
        val kode = barangDipilih?.code ?: binding.etKodeBarangPesanan.text.toString().trim()
        val nama = barangDipilih?.name ?: binding.etNamaBarangPesanan.text.toString().trim()
        val jumlahText = binding.etJumlahPesanan.text.toString().trim()
        val jumlah = jumlahText.toIntOrNull() ?: 0

        val tipe = if (binding.radioGroupTipeTransaksiPesanan.checkedRadioButtonId == R.id.rbMasukPesanan) "Masuk" else "Keluar"
        return Pesanan(0, kode, nama, jumlah, tipe)
    }

    private fun clearForm() {
        binding.etKodeBarangPesanan.text?.clear()
        binding.etNamaBarangPesanan.text?.clear()
        binding.etJumlahPesanan.text?.clear()
        binding.radioGroupTipeTransaksiPesanan.clearCheck()
        selectedId = null
        barangDipilih = null
    }

    private fun onDetailClicked(pesanan: Pesanan) {
        Toast.makeText(requireContext(), "Detail Pesanan: ${pesanan.jumlah}", Toast.LENGTH_SHORT).show()
        // Implementasikan tampilan detail jika diperlukan
    }

    private fun onProsesClicked(pesanan: Pesanan) {
        // Misal, tampilkan dialog pilihan status
        val statusOptions = arrayOf("pending", "processing", "completed", "cancelled")
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Ubah Status Pesanan")
            .setItems(statusOptions) { _, which ->
                val selectedStatus = statusOptions[which]
                updateOrderStatus(pesanan.id, selectedStatus)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun onHapusClicked(pesanan: Pesanan) {
        val sharedPreferences = requireContext().getSharedPreferences("login_session", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("token", null)
        if (token == null) {
            Toast.makeText(requireContext(), "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }
        val api = RetrofitClient.instance.create(OrderApi::class.java)
        api.deleteOrder("Bearer $token", pesanan.id).enqueue(object: retrofit2.Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                loadData()
                Toast.makeText(requireContext(), "Pesanan Dihapus: ${pesanan.namaBarang}", Toast.LENGTH_SHORT).show()
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
