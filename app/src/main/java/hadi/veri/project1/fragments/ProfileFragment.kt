package hadi.veri.project1.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import hadi.veri.project1.WelcomeActivity
import hadi.veri.project1.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

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
        
        displayUserInfo()
        setupButtons()
    }

    private fun displayUserInfo() {
        binding.tvUserName.text = userName
        binding.tvUserEmail.text = userEmail
        binding.tvUserPhone.text = userPhone
        binding.tvUserPosition.text = userPosition
        
        // Set nilai-nilai di form edit
        binding.etUpdateName.setText(userName)
        binding.etUpdateEmail.setText(userEmail)
        binding.etUpdatePhone.setText(userPhone)
        binding.etUpdatePosition.setText(userPosition)
    }
    
    private fun setupButtons() {
        binding.btnUpdateProfile.setOnClickListener {
            updateUserProfile()
        }
        
        binding.btnChangePhoto.setOnClickListener {
            Toast.makeText(requireContext(), "Belum di koding oiiii", Toast.LENGTH_SHORT).show()
        }
        
        binding.btnLogout.setOnClickListener {
            // Logout dan kembali ke login screen
            Toast.makeText(requireContext(), "Berhasil logout", Toast.LENGTH_SHORT).show()
            val intent = Intent(requireContext(), WelcomeActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }
    
    private fun updateUserProfile() {
        val newName = binding.etUpdateName.text.toString().trim()
        val newEmail = binding.etUpdateEmail.text.toString().trim()
        val newPhone = binding.etUpdatePhone.text.toString().trim()
        val newPosition = binding.etUpdatePosition.text.toString().trim()
        
        if (newName.isEmpty() || newEmail.isEmpty() || newPhone.isEmpty() || newPosition.isEmpty()) {
            Toast.makeText(requireContext(), "Semua field harus diisi", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Simple email validation
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            Toast.makeText(requireContext(), "Format email tidak valid", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Update user info
        userName = newName
        userEmail = newEmail
        userPhone = newPhone
        userPosition = newPosition
        
        displayUserInfo()
        
        Toast.makeText(requireContext(), "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = ProfileFragment()
    }
} 