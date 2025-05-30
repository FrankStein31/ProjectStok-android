package hadi.veri.project1.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import hadi.veri.project1.MainActivity
import hadi.veri.project1.api.AuthApi
import hadi.veri.project1.api.LoginRequest
import hadi.veri.project1.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("login_session", MODE_PRIVATE)

        binding.btnLogin.setOnClickListener {
            val email = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan password harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Ganti Retrofit dengan AuthApi (Volley)
            AuthApi.login(
                context = this,
                loginRequest = LoginRequest(email, password),
                onSuccess = { loginResponse ->
                    val user = loginResponse.data.user
                    val role = user.role
                    val token = loginResponse.data.token

                    sharedPreferences.edit()
                        .putString("role", role)
                        .putString("token", token)
                        .apply()

                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                },
                onError = { error ->
                    Toast.makeText(this@LoginActivity, "Login gagal: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}
