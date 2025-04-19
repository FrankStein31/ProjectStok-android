package hadi.veri.project1.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import hadi.veri.project1.databinding.ItemUserBinding
import hadi.veri.project1.models.User

class UserAdapter(
    private val userList: List<User>,
    private val onItemClick: (User) -> Unit,
    private val onDelete: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(private val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.tvUsername.text = user.username
            binding.tvJenisKelamin.text = "Jenis Kelamin: ${user.jenisKelamin}"
            binding.tvRole.text = "Role: ${user.role}"
            
            itemView.setOnClickListener {
                onItemClick(user)
            }
            
            itemView.setOnLongClickListener {
                onDelete(user)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun getItemCount(): Int = userList.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(userList[position])
    }
} 