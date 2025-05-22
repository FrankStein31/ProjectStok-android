package hadi.veri.project1.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import hadi.veri.project1.databinding.ItemMasterBinding
import hadi.veri.project1.models.Product
import java.text.NumberFormat
import java.util.Locale

class ProductAdapter(
    private val productList: MutableList<Product>,
    private val onItemClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(private val binding: ItemMasterBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) {
            binding.tvKodeBarang.text = product.code
            binding.tvNamaBarang.text = product.name
            binding.tvJumlahStok.text = product.stock.toString()
            binding.tvSatuan.text = product.unit

            // Format harga ke Rupiah
            val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            binding.tvHarga.text = formatRupiah.format(product.price)

            itemView.setOnClickListener {
                onItemClick(product)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemMasterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun getItemCount(): Int = productList.size

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(productList[position])
    }

    fun updateData(newList: List<Product>) {
        productList.clear()
        productList.addAll(newList)
        notifyDataSetChanged()
    }

    fun addItem(product: Product) {
        productList.add(product)
        notifyItemInserted(productList.size - 1)
    }

    fun updateItem(position: Int, product: Product) {
        productList[position] = product
        notifyItemChanged(position)
    }

    fun removeItem(position: Int) {
        productList.removeAt(position)
        notifyItemRemoved(position)
    }
} 