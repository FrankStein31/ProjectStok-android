package hadi.veri.project1.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import hadi.veri.project1.databinding.ItemPesananBinding
import hadi.veri.project1.models.Order
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class OrderAdapter(
    private val orders: MutableList<Order>,
    private val onItemClick: (Order) -> Unit
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(private val binding: ItemPesananBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(order: Order) {
            binding.tvOrderNumber.text = order.order_number
            
            // Format status
            val status = when(order.status) {
                "pending" -> "Menunggu"
                "processing" -> "Diproses"
                "completed" -> "Selesai"
                "cancelled" -> "Dibatalkan"
                else -> order.status
            }
            binding.tvStatus.text = status
            
            // Format tanggal
            val createdAt = order.created_at ?: ""
            if (createdAt.isNotEmpty()) {
                binding.tvTanggal.text = createdAt.substring(0, 10)
            } else {
                binding.tvTanggal.text = "-"
            }
            
            // Format jumlah item
            val itemCount = order.orderDetails?.size ?: 0
            binding.tvJumlahItem.text = "$itemCount item"
            
            // Format total harga
            val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            binding.tvTotalHarga.text = formatRupiah.format(order.total_amount)
            
            itemView.setOnClickListener {
                onItemClick(order)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemPesananBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun getItemCount(): Int = orders.size

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    fun updateData(newList: List<Order>) {
        orders.clear()
        orders.addAll(newList)
        notifyDataSetChanged()
    }
} 