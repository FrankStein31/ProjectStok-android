package hadi.veri.project1.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import hadi.veri.project1.R
import hadi.veri.project1.databinding.ItemTransaksiStokBinding
import hadi.veri.project1.models.TipeTransaksi
import hadi.veri.project1.models.TransaksiStok
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class TransaksiStokAdapter(
    private var transaksiList: List<TransaksiStok>
) : RecyclerView.Adapter<TransaksiStokAdapter.TransaksiViewHolder>() {

    private val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    private val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale("id"))

    private var filteredTransaksiList = transaksiList

    // ViewHolder untuk setiap item transaksi
    inner class TransaksiViewHolder(private val binding: ItemTransaksiStokBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(transaksi: TransaksiStok) {
            binding.tvIdTransaksi.text = "${transaksi.id}"
            binding.tvKodeBarang.text = transaksi.kodeBarang
            binding.tvNamaBarang.text = transaksi.namaBarang
            binding.tvJumlah.text = transaksi.jumlah.toString()
            binding.tvTanggal.text = dateFormat.format(transaksi.tanggal)
            binding.tvKeterangan.text = transaksi.keterangan
            binding.tvNilai.text = formatRupiah.format(transaksi.nilaiTransaksi)
            binding.tvPukul.text = transaksi.pukul

            val tipeText = if (transaksi.tipeTransaksi == TipeTransaksi.MASUK) "MASUK" else "KELUAR"
            binding.tvTipeTransaksi.text = tipeText

            val colorRes = if (transaksi.tipeTransaksi == TipeTransaksi.MASUK)
                R.color.primary else R.color.error

            binding.tvTipeTransaksi.setTextColor(
                ContextCompat.getColor(itemView.context, colorRes)
            )
        }
    }

    // Membuat ViewHolder untuk setiap item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransaksiViewHolder {
        val binding = ItemTransaksiStokBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransaksiViewHolder(binding)
    }

    // Mengembalikan jumlah item dalam list transaksi
    override fun getItemCount(): Int = filteredTransaksiList.size

    // Mengikat data transaksi ke tampilan RecyclerView
    override fun onBindViewHolder(holder: TransaksiViewHolder, position: Int) {
        holder.bind(filteredTransaksiList[position])
    }

    // Memperbarui data yang difilter pada adapter
    fun updateData(newList: List<TransaksiStok>) {
        filteredTransaksiList = newList
        notifyDataSetChanged()  // Menyegarkan RecyclerView dengan data baru
    }
}
