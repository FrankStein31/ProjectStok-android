package hadi.veri.project1.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import hadi.veri.project1.R
import hadi.veri.project1.models.Pesanan

class PesananAdapter(
    private var pesananList: List<Pesanan>,
    private val onDetailClicked: (Pesanan) -> Unit,
    private val onProsesClicked: (Pesanan) -> Unit,
    private val onHapusClicked: (Pesanan) -> Unit
) : RecyclerView.Adapter<PesananAdapter.PesananViewHolder>() {

    // Untuk filter di fragment
    fun updateData(newList: List<Pesanan>) {
        pesananList = newList
        notifyDataSetChanged()
    }

    // Jika ingin mengambil data terakhir yang ditampilkan di adapter (untuk filter)
    val currentList: List<Pesanan>
        get() = pesananList

    inner class PesananViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvIdPesanan: TextView = itemView.findViewById(R.id.tvIdPesanan)
        val tvKodeBarang: TextView = itemView.findViewById(R.id.tvKodeBarangPesanan)
        val tvNamaBarang: TextView = itemView.findViewById(R.id.tvNamaPesanan)
        val tvJumlahPesanan: TextView = itemView.findViewById(R.id.tvJumlahPesanan)
        val tvTipeTransaksi: TextView = itemView.findViewById(R.id.tvTipeTransaksiPesanan)
        val btnDetail: Button = itemView.findViewById(R.id.btnDetail)
        val btnProses: Button = itemView.findViewById(R.id.btnProses)
        val btnHapus: Button = itemView.findViewById(R.id.btnHapus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PesananViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pesanan, parent, false)
        return PesananViewHolder(view)
    }

    override fun onBindViewHolder(holder: PesananViewHolder, position: Int) {
        val pesanan = pesananList[position]
        holder.tvIdPesanan.text = pesanan.id.toString()
        holder.tvKodeBarang.text = pesanan.kodeBarang
        holder.tvNamaBarang.text = pesanan.namaBarang
        holder.tvJumlahPesanan.text = pesanan.jumlah.toString()
        holder.tvTipeTransaksi.text = pesanan.tipeTransaksi

        holder.btnDetail.setOnClickListener { onDetailClicked(pesanan) }
        holder.btnProses.setOnClickListener { onProsesClicked(pesanan) }
        holder.btnHapus.setOnClickListener { onHapusClicked(pesanan) }
    }

    override fun getItemCount(): Int = pesananList.size
}
