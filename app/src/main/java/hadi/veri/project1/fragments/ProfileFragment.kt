package hadi.veri.project1.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import hadi.veri.project1.WelcomeActivity
import hadi.veri.project1.database.DBHelper
import hadi.veri.project1.databinding.FragmentProfileBinding
import hadi.veri.project1.models.User

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var dbHelper: DBHelper
    private var currentUser: User? = null
    private var userName = "Nama"
    private var userEmail = "Email"
    private var userPhone = "+62 88888888888"
    private var userPosition = "Manajer Gudang"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        dbHelper = DBHelper(requireContext())
        
        // Set judul halaman dan tombol back
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            title = "Profil"
            setDisplayHomeAsUpEnabled(false)
        }
        
        loadUserData()
        setupButtons()
    }
    
    private fun loadUserData() {
        // Ambil username dari shared preferences
        val sharedPreferences = requireActivity().getSharedPreferences("login_session", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "") ?: ""
        
        if (username.isNotEmpty()) {
            // Ambil data user dari database
            currentUser = dbHelper.getUserByUsername(username)
            
            currentUser?.let { user ->
                // Gunakan username sebagai nama
                userName = user.username
                
                // Gunakan role sebagai userPosition
                userPosition = user.role
                
                // Gunakan jenis kelamin sebagai info tambahan
                userPhone = user.jenisKelamin
                
                // Email kita set menggunakan username sebagai contoh
                userEmail = "$username@example.com"
                
                // Tampilkan data
                displayUserInfo()
            }
        }
    }

    private fun displayUserInfo() {
        binding.tvUserName.text = userName
//        binding.tvUserEmail.text = userEmail
        binding.tvUserPhone.text = userPhone
        binding.tvUserPosition.text = userPosition
        
        // Set nilai-nilai di form edit
        binding.etUpdateName.setText(userName)
//        binding.etUpdateEmail.setText(userEmail)
        binding.etUpdatePhone.setText(userPhone)
        binding.etUpdatePosition.setText(userPosition)
    }
    
    private fun setupButtons() {
        binding.btnUpdateProfile.setOnClickListener {
            updateUserProfile()
        }
        
        binding.btnChangePhoto.setOnClickListener {
            Toast.makeText(requireContext(), "Fitur ganti foto belum tersedia", Toast.LENGTH_SHORT).show()
        }
        
        binding.btnLogout.setOnClickListener {
            // Logout dan kembali ke welcome screen
            val sharedPreferences = requireActivity().getSharedPreferences("login_session", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()
            
            Toast.makeText(requireContext(), "Berhasil logout", Toast.LENGTH_SHORT).show()
            val intent = Intent(requireContext(), WelcomeActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }
    
    private fun updateUserProfile() {
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Data user tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }
        
        val newName = binding.etUpdateName.text.toString().trim()
//        val newEmail = binding.etUpdateEmail.text.toString().trim()
        val newJenisKelamin = binding.etUpdatePhone.text.toString().trim()
        val newRole = binding.etUpdatePosition.text.toString().trim()
        
        if (newName.isEmpty() || newJenisKelamin.isEmpty() || newRole.isEmpty()) {
            Toast.makeText(requireContext(), "Semua field harus diisi", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Validasi Role harus Admin atau User
        if (newRole != "Admin" && newRole != "User") {
            Toast.makeText(requireContext(), "Role harus Admin atau User", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Validasi Jenis Kelamin
        if (newJenisKelamin != "Laki-laki" && newJenisKelamin != "Perempuan") {
            Toast.makeText(requireContext(), "Jenis Kelamin harus Laki-laki atau Perempuan", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Update user di database
        val updatedUser = User(
            currentUser!!.id,
            newName,
            currentUser!!.password,
            newJenisKelamin,
            newRole
        )
        
        val result = dbHelper.updateUser(updatedUser)
        
        if (result > 0) {
            // Update shared preferences dengan username baru jika diganti
            if (newName != userName) {
                val sharedPreferences = requireActivity().getSharedPreferences("login_session", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("username", newName)
                editor.apply()
            }
            
            // Update UI data
            userName = newName
//            userEmail = newEmail
            userPhone = newJenisKelamin
            userPosition = newRole
            
            displayUserInfo()
            
            // Perbarui current user
            currentUser = updatedUser
            
            Toast.makeText(requireContext(), "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Gagal memperbarui profil", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = ProfileFragment()
    }
} 