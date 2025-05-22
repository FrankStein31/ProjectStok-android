package hadi.veri.project1.fragments

import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import hadi.veri.project1.adapters.UserAdapter
import hadi.veri.project1.database.DBHelper
import hadi.veri.project1.databinding.FragmentUsersBinding
import hadi.veri.project1.models.User
import java.util.UUID

class UsersFragment : Fragment() {
    private var _binding: FragmentUsersBinding? = null
    private val binding get() = _binding!!

    private lateinit var dbHelper: DBHelper
    private lateinit var adapter: UserAdapter
    private val userList = mutableListOf<User>()
    private var selectedPosition = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUsersBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = DBHelper(requireContext())

        setupRecyclerView()
        setupButtons()
        loadUserData()

        // Set judul halaman dan tombol back
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            title = "Users"
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun setupRecyclerView() {
        adapter = UserAdapter(
            userList,
            onItemClick = { user ->
                binding.etUsername.setText(user.username)
                binding.etPassword.setText(user.password)

                if (user.jenisKelamin == "Laki-laki") {
                    binding.rbLaki.isChecked = true
                } else {
                    binding.rbPerempuan.isChecked = true
                }

                val rolePosition = when (user.role) {
                    "Admin" -> 0
                    "User" -> 1
                    else -> 0
                }
                binding.spinnerRole.setSelection(rolePosition)

                selectedPosition = userList.indexOf(user)
            }
        )

        binding.recyclerUserList.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerUserList.adapter = adapter
        registerForContextMenu(binding.recyclerUserList)
    }

    private fun setupButtons() {
        binding.btnSimpanUser.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Username dan password harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val jenisKelamin = if (binding.rbLaki.isChecked) "Laki-laki" else "Perempuan"
            val role = binding.spinnerRole.selectedItem.toString()

            if (selectedPosition == -1) {
                if (dbHelper.checkUsernameExists(username)) {
                    Toast.makeText(requireContext(), "Username sudah digunakan", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val user = User(
                    null,
                    username,
                    password,
                    jenisKelamin,
                    role
                )

                val result = dbHelper.registerUser(user)
                if (result > 0) {
                    loadUserData()
                    clearForm()
                    Toast.makeText(requireContext(), "User berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Gagal menambahkan user", Toast.LENGTH_SHORT).show()
                }
            } else {
                val user = User(
                    userList[selectedPosition].id,
                    username,
                    password,
                    jenisKelamin,
                    role
                )

                val result = dbHelper.updateUser(user)
                if (result > 0) {
                    loadUserData()
                    clearForm()
                    selectedPosition = -1
                    Toast.makeText(requireContext(), "User berhasil diupdate", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Gagal mengupdate user", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadUserData() {
        userList.clear()
        userList.addAll(dbHelper.getAllUsers())
        adapter.notifyDataSetChanged()
    }

    private fun clearForm() {
        binding.etUsername.text?.clear()
        binding.etPassword.text?.clear()
        binding.rbLaki.isChecked = true
        binding.spinnerRole.setSelection(0)
        selectedPosition = -1
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            101 -> {
                val user = userList[item.groupId] // Ambil user berdasarkan groupId
                val result = user.id?.let { dbHelper.deleteUser(it) } // Hanya panggil deleteUser jika id tidak null

                if (result != null && result > 0) {
                    loadUserData()
                    Toast.makeText(requireContext(), "User berhasil dihapus", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Gagal menghapus user", Toast.LENGTH_SHORT).show()
                }
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = UsersFragment()
    }
}
