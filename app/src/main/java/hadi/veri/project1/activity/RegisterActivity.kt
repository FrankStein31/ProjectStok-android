package hadi.veri.project1.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import hadi.veri.project1.WelcomeActivity
import hadi.veri.project1.database.DBHelper
import hadi.veri.project1.databinding.ActivityRegisterBinding
import hadi.veri.project1.models.UserLocal
import java.util.UUID

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        dbHelper = DBHelper(this)
        
        setupListeners()
    }
    
    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
        
        binding.btnRegister.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Username dan password harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Cek apakah username sudah ada
            if (dbHelper.checkUsernameExists(username)) {
                Toast.makeText(this, "Username sudah digunakan", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val jenisKelamin = if (binding.rbLaki.isChecked) "Laki-laki" else "Perempuan"
            val role = if (binding.rbAdmin.isChecked) "Admin" else "User"
            
            val user = UserLocal(
                0,
                username,
                password,
                jenisKelamin,
                role
            )
            
            val result = dbHelper.registerUser(user)
            if (result > 0) {
                Toast.makeText(this, "Registrasi berhasil", Toast.LENGTH_SHORT).show()
                navigateToWelcome()
            } else {
                Toast.makeText(this, "Registrasi gagal", Toast.LENGTH_SHORT).show()
            }
        }
        
        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
    
    private fun navigateToWelcome() {
        startActivity(Intent(this, WelcomeActivity::class.java))
        finish()
    }
} 