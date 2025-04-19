package hadi.veri.project1.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import hadi.veri.project1.R
import hadi.veri.project1.databinding.ItemPesananBinding
import hadi.veri.project1.models.Pesanan
import hadi.veri.project1.models.StatusPesanan
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class PesananAdapter(
    private val pesananList: MutableList<Pesanan>,
    private val onStatusChange: (Pesanan, StatusPesanan) -> Unit,
    private val onDelete: (Pesanan) -> Unit,
    private val onEdit: (Pesanan) -> Unit
) : RecyclerView.Adapter<PesananAdapter.PesananViewHolder>() {

    private val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    private val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale("id"))

    inner class PesananViewHolder(private val binding: ItemPesananBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pesanan: Pesanan) {
            binding.tvIdPesanan.text = pesanan.id
            binding.tvNamaPembeli.text = pesanan.namaPembeli
            binding.tvTanggal.text = dateFormat.format(pesanan.tanggal)
            binding.tvTotalHarga.text = formatRupiah.format(pesanan.totalHarga)
            
            // Set status pesanan dengan warna yang sesuai
            binding.tvStatus.text = when (pesanan.status) {
                StatusPesanan.PENDING -> "PENDING"
                StatusPesanan.PROSES -> "PROSES"
                StatusPesanan.SELESAI -> "SELESAI"
                StatusPesanan.DIBATALKAN -> "DIBATALKAN"
            }
            
            val colorRes = when (pesanan.status) {
                StatusPesanan.PENDING -> R.color.accent
                StatusPesanan.PROSES -> R.color.primary
                StatusPesanan.SELESAI -> android.R.color.holo_green_dark
                StatusPesanan.DIBATALKAN -> R.color.error
            }
            
            binding.tvStatus.setTextColor(ContextCompat.getColor(itemView.context, colorRes))

            binding.containerItems.removeAllViews()

            val maxItemsToShow = if (pesanan.items.size > 2) 2 else pesanan.items.size
            
            for (i in 0 until maxItemsToShow) {
                val item = pesanan.items[i]
                val itemView = LayoutInflater.from(itemView.context)
                    .inflate(R.layout.item_pesanan_mini, binding.containerItems, false)
                
                val tvNamaBarang = itemView.findViewById<TextView>(R.id.tvNamaBarangMini)
                val tvJumlah = itemView.findViewById<TextView>(R.id.tvJumlahMini)
                
                tvNamaBarang.text = item.namaBarang
                tvJumlah.text = "${item.jumlah} x ${formatRupiah.format(item.hargaSatuan)}"
                
                binding.containerItems.addView(itemView)
            }

            if (pesanan.items.size > 2) {
                val moreText = TextView(itemView.context).apply {
                    text = "... dan ${pesanan.items.size - 2} item lainnya"
                    setTextColor(ContextCompat.getColor(context, R.color.primary))
                    textSize = 12f
                    setPadding(0, 4, 0, 0)
                }
                binding.containerItems.addView(moreText)
            }
            
            // Atur tombol-tombol berdasarkan status
            when (pesanan.status) {
                StatusPesanan.PENDING -> {
                    // Tampilkan tombol Proses dan Batal
                    binding.btnProses.visibility = View.VISIBLE
                    binding.btnProses.text = "Proses"
                    binding.btnProses.setOnClickListener {
                        onStatusChange(pesanan, StatusPesanan.PROSES)
                    }
                    
                    // Tampilkan tombol Batal dengan warna merah
                    binding.btnDetail.text = "Batal"
                    binding.btnDetail.backgroundTintList = ContextCompat.getColorStateList(
                        itemView.context, 
                        R.color.error
                    )
                    binding.btnDetail.setOnClickListener {
                        onStatusChange(pesanan, StatusPesanan.DIBATALKAN)
                    }
                }
                StatusPesanan.PROSES -> {
                    // Tampilkan tombol Selesai dan Batal
                    binding.btnProses.visibility = View.VISIBLE
                    binding.btnProses.text = "Selesai"
                    binding.btnProses.setOnClickListener {
                        onStatusChange(pesanan, StatusPesanan.SELESAI)
                    }
                    
                    // Tampilkan tombol Batal dengan warna merah
                    binding.btnDetail.text = "Batal"
                    binding.btnDetail.backgroundTintList = ContextCompat.getColorStateList(
                        itemView.context, 
                        R.color.error
                    )
                    binding.btnDetail.setOnClickListener {
                        onStatusChange(pesanan, StatusPesanan.DIBATALKAN)
                    }
                }
                StatusPesanan.SELESAI, StatusPesanan.DIBATALKAN -> {
                    // Sembunyikan tombol Proses
                    binding.btnProses.visibility = View.GONE
                    
                    // Ubah tombol Detail kembali ke warna normal dan fungsi detail
                    binding.btnDetail.text = "Detail"
                    binding.btnDetail.backgroundTintList = ContextCompat.getColorStateList(
                        itemView.context, 
                        R.color.primary
                    )
                    binding.btnDetail.setOnClickListener {
                        onEdit(pesanan)
                    }
                }
            }
            
            binding.btnHapus.setOnClickListener {
                onDelete(pesanan)
            }
            
            itemView.setOnClickListener {
                onEdit(pesanan)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PesananViewHolder {
        val binding = ItemPesananBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PesananViewHolder(binding)
    }

    override fun getItemCount(): Int = pesananList.size

    override fun onBindViewHolder(holder: PesananViewHolder, position: Int) {
        holder.bind(pesananList[position])
    }

    fun updateData(newList: List<Pesanan>) {
        pesananList.clear()
        pesananList.addAll(newList)
        notifyDataSetChanged()
    }

    fun addItem(pesanan: Pesanan) {
        pesananList.add(0, pesanan)
        notifyItemInserted(0)
    }
    
    fun updateItem(position: Int, pesanan: Pesanan) {
        if (position >= 0 && position < pesananList.size) {
            pesananList[position] = pesanan
            notifyItemChanged(position)
        }
    }
    
    fun findPositionById(id: String): Int {
        return pesananList.indexOfFirst { it.id == id }
    }
} 