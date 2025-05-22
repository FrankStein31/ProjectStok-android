package hadi.veri.project1.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import hadi.veri.project1.databinding.ItemStockCardBinding
import hadi.veri.project1.models.StockCard
import java.text.SimpleDateFormat
import java.util.Locale

class StockCardAdapter(
    private val stockCards: MutableList<StockCard>,
    private val onItemClick: (StockCard) -> Unit
) : RecyclerView.Adapter<StockCardAdapter.StockCardViewHolder>() {

    inner class StockCardViewHolder(private val binding: ItemStockCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(stockCard: StockCard) {
            val productName = stockCard.product?.name ?: "Unknown"
            
            binding.tvProductName.text = productName
            binding.tvDate.text = stockCard.date
            binding.tvInitialStock.text = stockCard.initial_stock.toString()
            binding.tvInStock.text = stockCard.in_stock.toString()
            binding.tvOutStock.text = stockCard.out_stock.toString()
            binding.tvFinalStock.text = stockCard.final_stock.toString()
            binding.tvNotes.text = stockCard.notes ?: "-"
            
            itemView.setOnClickListener {
                onItemClick(stockCard)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockCardViewHolder {
        val binding = ItemStockCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StockCardViewHolder(binding)
    }

    override fun getItemCount(): Int = stockCards.size

    override fun onBindViewHolder(holder: StockCardViewHolder, position: Int) {
        holder.bind(stockCards[position])
    }

    fun updateData(newList: List<StockCard>) {
        stockCards.clear()
        stockCards.addAll(newList)
        notifyDataSetChanged()
    }
} 