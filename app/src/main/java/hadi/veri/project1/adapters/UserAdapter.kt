package hadi.veri.project1.adapters

import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import hadi.veri.project1.databinding.ItemUserBinding
import hadi.veri.project1.models.User

class UserAdapter(
    private val userList: List<User>,
    private val onItemClick: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(private val binding: ItemUserBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnCreateContextMenuListener {

        init {
            itemView.setOnCreateContextMenuListener(this)
        }

        fun bind(user: User) {
            binding.tvUsername.text = user.username
            binding.tvJenisKelamin.text = "Jenis Kelamin: ${user.jenisKelamin}"
            binding.tvRole.text = "Role: ${user.role}"

            itemView.setOnClickListener {
                onItemClick(user)
            }
        }

        override fun onCreateContextMenu(menu: ContextMenu, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
            menu.setHeaderTitle("Pilih Aksi")
            menu.add(adapterPosition, 101, 0, "Hapus") // 101 = ID Hapus
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(userList[position])
    }

    override fun getItemCount(): Int = userList.size
}
