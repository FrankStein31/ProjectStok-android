package hadi.veri.project1.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import hadi.veri.project1.adapters.UserAdapter
import hadi.veri.project1.api.SingleUserResponse
import hadi.veri.project1.api.User
import hadi.veri.project1.api.UserManageApi
import hadi.veri.project1.api.UserResponse
import hadi.veri.project1.database.DBHelper
import hadi.veri.project1.databinding.FragmentUsersBinding

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
                binding.etUsername.setText(user.name)
                binding.etEmail.setText(user.email)
                binding.etPassword.setText("") // Kosongkan, demi keamanan
                binding.etPhone.setText(user.phone ?: "")
                binding.etAddress.setText(user.address ?: "")

                // Jenis Kelamin
                if (user.jenis_kelamin == "L") {
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
            val name = binding.etUsername.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val address = binding.etAddress.text.toString().trim()
            val role = binding.spinnerRole.selectedItem.toString()
            val jenisKelamin = if (binding.rbLaki.isChecked) "L" else "P"

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Nama, email, dan password harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = User(
                id = if (selectedPosition == -1) null else userList[selectedPosition].id,
                name = name,
                email = email,
                password = password,
                role = role,
                jenis_kelamin = jenisKelamin,
                phone = phone.ifEmpty { null },
                address = address.ifEmpty { null }
            )

            val sharedPreferences = requireContext().getSharedPreferences("login_session", Context.MODE_PRIVATE)
            val token = sharedPreferences.getString("token", null)
            if (token == null) {
                Toast.makeText(requireContext(), "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedPosition == -1) {
                // Tambah user
                UserManageApi.createUser(
                    context = requireContext(),
                    token = "Bearer $token",
                    user = user,
                    onSuccess = { response ->
                        if (response.success) {
                            loadUserData()
                            clearForm()
                            Toast.makeText(requireContext(), "User berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "Gagal menambahkan user", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onError = { error ->
                        Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                // Update user
                val userId = userList[selectedPosition].id ?: return@setOnClickListener
                UserManageApi.updateUser(
                    context = requireContext(),
                    token = "Bearer $token",
                    id = userId,
                    user = user,
                    onSuccess = { response ->
                        if (response.success) {
                            loadUserData()
                            clearForm()
                            selectedPosition = -1
                            Toast.makeText(requireContext(), "User berhasil diupdate", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "Gagal mengupdate user", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onError = { error ->
                        Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    private fun loadUserData() {
        val sharedPreferences = requireContext().getSharedPreferences("login_session", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("token", null)
        if (token == null) {
            Toast.makeText(requireContext(), "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }
        UserManageApi.getUsers(
            context = requireContext(),
            token = "Bearer $token",
            onSuccess = { response ->
                userList.clear()
                if (response.data != null) {
                    userList.addAll(response.data)
                }
                adapter.notifyDataSetChanged()
            },
            onError = { error ->
                Toast.makeText(requireContext(), "Gagal memuat user: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun addUser(user: User) {
        val sharedPreferences = requireContext().getSharedPreferences("login_session", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("token", null)
        if (token == null) {
            Toast.makeText(requireContext(), "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }
        UserManageApi.createUser(
            context = requireContext(),
            token = "Bearer $token",
            user = user,
            onSuccess = { response ->
                if (response.success) {
                    loadUserData()
                    clearForm()
                    Toast.makeText(requireContext(), "User berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Gagal menambahkan user", Toast.LENGTH_SHORT).show()
                }
            },
            onError = { error ->
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun updateUser(user: User) {
        val sharedPreferences = requireContext().getSharedPreferences("login_session", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("token", null)
        if (token == null) {
            Toast.makeText(requireContext(), "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }
        user.id?.let { userId ->
            UserManageApi.updateUser(
                context = requireContext(),
                token = "Bearer $token",
                id = userId,
                user = user,
                onSuccess = { response ->
                    if (response.success) {
                        loadUserData()
                        clearForm()
                        Toast.makeText(requireContext(), "User berhasil diupdate", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Gagal mengupdate user", Toast.LENGTH_SHORT).show()
                    }
                },
                onError = { error ->
                    Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun deleteUser(id: Int) {
        val sharedPreferences = requireContext().getSharedPreferences("login_session", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("token", null)
        if (token == null) {
            Toast.makeText(requireContext(), "Token tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }
        UserManageApi.deleteUser(
            context = requireContext(),
            token = "Bearer $token",
            id = id,
            onSuccess = { response ->
                if (response.success) {
                    loadUserData()
                    Toast.makeText(requireContext(), "User berhasil dihapus", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Gagal menghapus user", Toast.LENGTH_SHORT).show()
                }
            },
            onError = { error ->
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun clearForm() {
        binding.etUsername.text?.clear()
        binding.etEmail.text?.clear()
        binding.etPassword.text?.clear()
        binding.etPhone.text?.clear()
        binding.etAddress.text?.clear()
        binding.rbLaki.isChecked = true
        binding.spinnerRole.setSelection(0)
        selectedPosition = -1
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            101 -> {
                val user = userList[item.groupId]
                user.id?.let { userId ->
                    deleteUser(userId)
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
