package hadi.veri.project1.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import hadi.veri.project1.WelcomeActivity
import hadi.veri.project1.databinding.FragmentProfileBinding
import hadi.veri.project1.models.User
import hadi.veri.project1.viewmodels.AuthViewModel
import hadi.veri.project1.viewmodels.ViewModelFactory

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var authViewModel: AuthViewModel
    private var userRole: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        
        // Inisialisasi ViewModel
        val factory = ViewModelFactory.getInstance(requireContext())
        authViewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]
        
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Ambil data profil
        loadProfile()
        setupButtons()
        
        // Observe profil
        observeViewModel()

        // Set title
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            title = "Profil"
            setDisplayHomeAsUpEnabled(true)
        }
    }
    
    private fun observeViewModel() {
        // Observe profile result
        authViewModel.profileResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { response ->
                if (response.success && response.data != null) {
                    updateUI(response.data)
                } else {
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                }
            }.onFailure { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Observe update profile result
        authViewModel.updateProfileResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { response ->
                if (response.success && response.data != null) {
                    Toast.makeText(requireContext(), "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    updateUI(response.data)
                } else {
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                }
            }.onFailure { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Observe logout result
        authViewModel.logoutResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { response ->
                if (response.success) {
                    Toast.makeText(requireContext(), "Logout berhasil", Toast.LENGTH_SHORT).show()
                    navigateToLogin()
                } else {
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                }
            }.onFailure { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun loadProfile() {
        // Tampilkan data user yang sedang login
        if (authViewModel.isLoggedIn()) {
            val user = authViewModel.getUser()
            updateUI(user)
            
            // Update juga dari API untuk memastikan data terbaru
            authViewModel.getProfile()
        } else {
            // Redirect ke halaman login jika belum login
            navigateToLogin()
        }
    }
    
    private fun updateUI(user: User) {
        binding.tvNama.text = user.name
        binding.tvEmail.text = user.email
        binding.tvRole.text = user.role
        binding.etNama.setText(user.name)
        binding.etPhone.setText(user.phone ?: "")
        binding.etAlamat.setText(user.address ?: "")
    }
    
    private fun setupButtons() {
        binding.btnUpdate.setOnClickListener {
            updateProfile()
        }
        
        binding.btnLogout.setOnClickListener {
            authViewModel.logout()
        }
    }
    
    private fun updateProfile() {
        val name = binding.etNama.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val address = binding.etAlamat.text.toString().trim()
        
        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }
        
        authViewModel.updateProfile(name, phone, address)
    }
    
    private fun navigateToLogin() {
        // Implementasikan navigasi ke LoginActivity
        // Contoh:
        // val intent = Intent(requireContext(), LoginActivity::class.java)
        // startActivity(intent)
        // activity?.finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    companion object {
        fun newInstance() = ProfileFragment()
    }
} 