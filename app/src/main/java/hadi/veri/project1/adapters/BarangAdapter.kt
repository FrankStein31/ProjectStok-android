package hadi.veri.project1.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import hadi.veri.project1.databinding.ItemMasterBinding
import hadi.veri.project1.models.Barang
import java.text.NumberFormat
import java.util.Locale

class BarangAdapter(
    private val barangList: MutableList<Barang>,
    private val onItemClick: (Barang) -> Unit
) : RecyclerView.Adapter<BarangAdapter.BarangViewHolder>() {

    inner class BarangViewHolder(private val binding: ItemMasterBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(barang: Barang) {
            binding.tvKodeBarang.text = barang.kode
            binding.tvNamaBarang.text = barang.nama
            binding.tvJumlahStok.text = barang.jumlahStok.toString()
            binding.tvSatuan.text = barang.satuan // Set satuan ke tvSatuan

            // Format harga ke Rupiah
            val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            binding.tvHarga.text = formatRupiah.format(barang.harga)

            itemView.setOnClickListener {
                onItemClick(barang)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BarangViewHolder {
        val binding = ItemMasterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BarangViewHolder(binding)
    }

    override fun getItemCount(): Int = barangList.size

    override fun onBindViewHolder(holder: BarangViewHolder, position: Int) {
        holder.bind(barangList[position])
    }

    fun updateData(newList: List<Barang>) {
        barangList.clear()
        barangList.addAll(newList)
        notifyDataSetChanged()
    }

    fun addItem(barang: Barang) {
        barangList.add(barang)
        notifyItemInserted(barangList.size - 1)
    }

    fun updateItem(position: Int, barang: Barang) {
        barangList[position] = barang
        notifyItemChanged(position)
    }

    fun removeItem(position: Int) {
        barangList.removeAt(position)
        notifyItemRemoved(position)
    }
}
