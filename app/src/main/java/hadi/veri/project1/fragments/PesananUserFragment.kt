package hadi.veri.project1.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import hadi.veri.project1.adapters.OrderAdapter
import hadi.veri.project1.databinding.FragmentPesananUserBinding
import hadi.veri.project1.models.Order
import hadi.veri.project1.viewmodels.OrderViewModel
import hadi.veri.project1.viewmodels.ViewModelFactory

class PesananUserFragment : Fragment() {
    private var _binding: FragmentPesananUserBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var orderViewModel: OrderViewModel
    private lateinit var adapter: OrderAdapter
    private val orderList = mutableListOf<Order>()
    private var userRole: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPesananUserBinding.inflate(inflater, container, false)
        
        // Ambil role dari arguments
        arguments?.let {
            userRole = it.getString("role")
        }
        
        // Inisialisasi ViewModel
        val factory = ViewModelFactory.getInstance(requireContext())
        orderViewModel = ViewModelProvider(this, factory)[OrderViewModel::class.java]
        
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        
        // Tampilkan semua pesanan untuk admin, hanya pesanan sendiri untuk user
        if (userRole.equals("admin", ignoreCase = true)) {
            loadAllOrders()
        } else {
            loadMyOrders()
        }

        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            title = "Pesanan"
            setDisplayHomeAsUpEnabled(true)
        }
    }
    
    private fun observeViewModel() {
        // Observe my orders (user)
        orderViewModel.myOrders.observe(viewLifecycleOwner) { result ->
            result.onSuccess { response ->
                if (response.success && response.data != null) {
                    adapter.updateData(response.data)
                    toggleEmptyView(response.data.isEmpty())
                } else {
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                    toggleEmptyView(true)
                }
            }.onFailure { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                toggleEmptyView(true)
            }
        }
        
        // Observe all orders (admin)
        orderViewModel.orders.observe(viewLifecycleOwner) { result ->
            result.onSuccess { response ->
                if (response.success && response.data != null) {
                    adapter.updateData(response.data)
                    toggleEmptyView(response.data.isEmpty())
                } else {
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                    toggleEmptyView(true)
                }
            }.onFailure { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                toggleEmptyView(true)
            }
        }
    }
    
    private fun toggleEmptyView(isEmpty: Boolean) {
        if (isEmpty) {
            binding.rvPesanan.visibility = View.GONE
            binding.emptyView.visibility = View.VISIBLE
        } else {
            binding.rvPesanan.visibility = View.VISIBLE
            binding.emptyView.visibility = View.GONE
        }
    }

    private fun setupRecyclerView() {
        adapter = OrderAdapter(orderList) { order ->
            // Tampilkan detail pesanan ketika item di klik
            Toast.makeText(requireContext(), "Pesanan #${order.order_number}", Toast.LENGTH_SHORT).show()
            // Bisa ditambahkan navigasi ke halaman detail
        }
        
        binding.rvPesanan.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@PesananUserFragment.adapter
        }
    }
    
    private fun loadMyOrders() {
        orderViewModel.getMyOrders()
    }
    
    private fun loadAllOrders() {
        orderViewModel.getAllOrders()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }
}


