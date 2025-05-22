package hadi.veri.project1.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import hadi.veri.project1.R
import hadi.veri.project1.models.StockMutation
import java.text.SimpleDateFormat
import java.util.Locale

class StockMutationAdapter(
    private val stockMutations: MutableList<StockMutation>,
    private val onItemClick: (StockMutation) -> Unit
) : RecyclerView.Adapter<StockMutationAdapter.StockMutationViewHolder>() {

    inner class StockMutationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNamaBarang: TextView = itemView.findViewById(R.id.tvNamaBarang)
        private val tvJumlah: TextView = itemView.findViewById(R.id.tvJumlah)
        private val tvTipe: TextView = itemView.findViewById(R.id.tvTipe)
        private val tvTanggal: TextView = itemView.findViewById(R.id.tvTanggal)
        private val tvKeterangan: TextView = itemView.findViewById(R.id.tvKeterangan)
        
        fun bind(stockMutation: StockMutation) {
            val productName = stockMutation.product?.name ?: "Unknown"
            val type = if (stockMutation.type == "in") "Masuk" else "Keluar"
            
            tvNamaBarang.text = productName
            tvJumlah.text = "${stockMutation.quantity} ${stockMutation.product?.unit ?: ""}"
            tvTipe.text = type
            
            // Format tanggal
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale("id"))
            try {
                tvTanggal.text = stockMutation.date
            } catch (e: Exception) {
                tvTanggal.text = stockMutation.date
            }
            
            tvKeterangan.text = stockMutation.description ?: "-"
            
            itemView.setOnClickListener {
                onItemClick(stockMutation)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockMutationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaksi, parent, false)
        return StockMutationViewHolder(view)
    }

    override fun getItemCount(): Int = stockMutations.size

    override fun onBindViewHolder(holder: StockMutationViewHolder, position: Int) {
        holder.bind(stockMutations[position])
    }

    fun updateData(newList: List<StockMutation>) {
        stockMutations.clear()
        stockMutations.addAll(newList)
        notifyDataSetChanged()
    }
} 