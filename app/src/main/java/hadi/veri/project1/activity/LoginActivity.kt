package hadi.veri.project1.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import hadi.veri.project1.MainActivity
import hadi.veri.project1.WelcomeActivity
import hadi.veri.project1.database.DBHelper
import hadi.veri.project1.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var dbHelper: DBHelper
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        dbHelper = DBHelper(this)
        sharedPreferences = getSharedPreferences("login_session", MODE_PRIVATE)
        
        // Cek apakah sudah login
        if (isLoggedIn()) {
            navigateToMain()
            finish()
        }
        
        setupListeners()
    }
    
    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
        
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Username dan password harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val user = dbHelper.loginUser(username, password)
            if (user != null) {
                // Simpan session login
                val editor = sharedPreferences.edit()
                editor.putBoolean("is_logged_in", true)
                editor.putString("username", user.username)
                editor.putString("role", user.role)
                editor.apply()
                
                Toast.makeText(this, "Login berhasil sebagai ${user.role}", Toast.LENGTH_SHORT).show()
                navigateToMain()
                finish()
            } else {
                Toast.makeText(this, "Username atau password salah", Toast.LENGTH_SHORT).show()
            }
        }
        
        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }
    
    private fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("is_logged_in", false)
    }
    
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
} 