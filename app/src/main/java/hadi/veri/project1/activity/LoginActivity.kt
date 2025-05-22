package hadi.veri.project1.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import hadi.veri.project1.MainActivity
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

        // Inisialisasi DBHelper dan SharedPreferences
        dbHelper = DBHelper(this)
        sharedPreferences = getSharedPreferences("login_session", MODE_PRIVATE)

        // Aksi ketika tombol login ditekan
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            // Validasi input
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Username dan password harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Cek login ke database
            val role = dbHelper.getUserRole(username, password)
            if (role != null) {
                // Simpan role ke SharedPreferences
                val editor = sharedPreferences.edit()
                editor.putString("role", role)
                editor.apply()

                // Login berhasil, pindahkan ke MainActivity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish() // Tutup LoginActivity agar tidak bisa kembali dengan tombol back
            } else {
                // Login gagal
                Toast.makeText(this, "Login gagal. Periksa username dan password.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}